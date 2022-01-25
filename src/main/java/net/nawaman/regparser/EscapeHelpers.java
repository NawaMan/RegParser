/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2022 Nawapunth Manusitthipol.
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

import net.nawaman.regparser.utils.Util;

/**
 * Compiler of RegParser language.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class EscapeHelpers {
    
    EscapeHelpers() {}
    
    static final public String escapable = ".?*-+^&'{}()[]|\\~ \t\n\r\f\u000B\u000C";
    
    public static String escapeOfRegParser(String pWord) {
        if(pWord == null) return null;
        pWord = Util.escapeText(pWord).toString();
        
        StringBuffer SB = new StringBuffer();
        for(int i = 0; i < pWord.length(); i++)  {
            char C = pWord.charAt(i);
            if((C != '\\') && (escapable.indexOf(C) != -1)) SB.append('\\'); 
            SB.append(C);
        }
        return SB.toString();
    }
    
}