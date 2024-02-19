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

import java.util.List;

import net.nawaman.regparser.result.entry.ParseResultEntry;


/**
 * This class is a normal parse result.
 * 
 * @author nawa
 */
abstract public class NormalParseResult extends ParseResult {
    
    static private final long serialVersionUID = 4121353565468546546L;
    
    private final int startPosition;
    
    NormalParseResult(int startPosition, List<ParseResultEntry> resultEntries) {
        super(resultEntries);
        this.startPosition = startPosition;
    }
    
    @Override
    public final int startPosition() {
        return startPosition;
    }
    
}
