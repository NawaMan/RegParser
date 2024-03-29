/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2024 Nawapunth Manusitthipol.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */
package net.nawaman.regparser.result;

import java.util.ArrayList;
import java.util.List;

import net.nawaman.regparser.result.entry.ParseResultEntry;

/**
 * Temporary parse result.
 * 
 * @author nawa
 */
public final class TemporaryParseResult extends ParseResult {
    
    private static final long serialVersionUID = 3255656565625655652L;
    
    private final ParseResult first;
    
    TemporaryParseResult(ParseResult first) {
        this(first, null);
    }
    
    private TemporaryParseResult(ParseResult first, List<ParseResultEntry> resultEntries) {
        super(null);
        this.first = first;
    }
    
    public final ParseResult first() {
        return first;
    }
    
    @Override
    public final int entryCount() {
        int count  = super.entryCount();
        var result = this.first;
        while (result instanceof TemporaryParseResult) {
            count  += result.rawEntryCount();
            result  = ((TemporaryParseResult)result).first;
        }
        return result.entryCount() + count;
    }
    
    @Override
    public final ParseResultEntry entryAt(int index) {
        if ((index < 0)
          || index >= entryCount())
            return null;
        
        if (index < first.entryCount()) {
            var result = this;
            while (index < result.first.entryCount()) {
                if (!(result.first instanceof TemporaryParseResult))
                    return result.first.entryAt(index);
                
                result = (TemporaryParseResult)result.first;
            }
            return result.entryAt(index);
        }
        return entries()
                .skip(index - first.entryCount())
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public final int startPosition() {
        return first.startPosition();
    }
    
    @Override
    public final CharSequence originalCharSequence() {
        return first.originalCharSequence();
    }
    
    @Override
    public final ParseResult duplicate() {
        // This was initially implement using recursive but it was too slow.
        // The optimization is done by going to the root or the first 'First' part that is not a Temp and then all
        //     all entries from then down to the current Temp Result.
        if (!(first instanceof TemporaryParseResult)) {
            var resultEntries  = entryList();
            var duplicateFirst = first.duplicate();
            return new TemporaryParseResult(duplicateFirst, resultEntries);
        }
        
        var firsts = new ArrayList<TemporaryParseResult>();
        firsts.add(this);
        
        var first = (TemporaryParseResult)this.first;
        firsts.add(first);
        
        while (first.first instanceof TemporaryParseResult) {
            first = (TemporaryParseResult)first.first;
            firsts.add(first);
        }
        
        var resultEntries = new ArrayList<ParseResultEntry>();
        for (int i = firsts.size(); --i >= 0;) {
            var firstEntry = firsts.get(i);
            if (firstEntry.entryCount() != 0) {
                var entryList = firstEntry.entryList();
                resultEntries.addAll(entryList);
            }
        }
        var duplicate = first.first.duplicate();
        return new TemporaryParseResult(duplicate, resultEntries);
    }
    
}
