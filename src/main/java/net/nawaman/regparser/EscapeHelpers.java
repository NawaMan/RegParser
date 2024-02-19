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

package net.nawaman.regparser;

import static net.nawaman.regparser.utils.Util.escapeText;

/**
 * Compiler of RegParser language.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class EscapeHelpers {
    
    private EscapeHelpers() {}
    
    static final public String escapable = ".?*-+^&'{}()[]|\\~ \t\n\r\f\u000B\u000C";
    
    /**
     * Produce an escape string of the given word for RefParser.
     * 
     * @param  word  the word.
     * @return       the RegParser escape string.
     **/
    public static String escapeOfRegParser(String word) {
        if(word == null) return null;
        word = escapeText(word).toString();
        
        var sb = new StringBuffer();
        for(int i = 0; i < word.length(); i++)  {
            char C = word.charAt(i);
            if((C != '\\') && (escapable.indexOf(C) != -1)) sb.append('\\'); 
            sb.append(C);
        }
        return sb.toString();
    }
    
}