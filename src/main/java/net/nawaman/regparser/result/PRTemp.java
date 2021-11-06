package net.nawaman.regparser.result;

import java.util.ArrayList;
import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;

/**
 * Temporary parse result.
 * 
 * @author nawa
 */
public final class PRTemp extends ParseResult {
    
    static private final long serialVersionUID = 3255656565625655652L;
    
    private final ParseResult first;
    
    PRTemp(ParseResult first) {
        this(first, null);
    }
    
    private PRTemp(ParseResult first, List<PREntry> resultEntries) {
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
        while (result instanceof PRTemp) {
            count += result.rawEntryCount();
            result = ((PRTemp)result).first;
        }
        return result.entryCount() + count;
    }
    
    @Override
    public final PREntry entryAt(int index) {
        if ((index < 0) || index >= entryCount()) {
            return null;
        }
        if (index < first.entryCount()) {
            var result = this;
            while (index < result.first.entryCount()) {
                if (!(result.first instanceof PRTemp)) {
                    return result.first.entryAt(index);
                }
                
                result = (PRTemp)result.first;
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
        if (!(first instanceof PRTemp)) {
            var resultEntries  = entryList();
            var duplicateFirst = first.duplicate();
            return new PRTemp(duplicateFirst, resultEntries);
        }
        
        var firsts = new ArrayList<PRTemp>();
        firsts.add(this);
        
        var first = (PRTemp) this.first;
        firsts.add(first);
        
        while (first.first instanceof PRTemp) {
            first = (PRTemp)first.first;
            firsts.add(first);
        }
        
        var resultEntries = new ArrayList<PREntry>();
        for (int i = firsts.size(); --i >= 0;) {
            var firstEntry = firsts.get(i);
            if (firstEntry.entryCount() != 0) {
                var entryList = firstEntry.entryList();
                resultEntries.addAll(entryList);
            }
        }
        var duplicate = first.first.duplicate();
        return new PRTemp(duplicate, resultEntries);
    }
    
}