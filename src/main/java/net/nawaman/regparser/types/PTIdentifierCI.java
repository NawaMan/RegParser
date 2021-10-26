/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2021 Nawapunth Manusitthipol. Implements with and for Java 11 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.types;

import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;

/**
 * Parser for detecting C-like identifier (for case-insensitive)
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTIdentifierCI extends PTIdentifier {
    
    static public String Name = "$IdentifierCI";
    
    @Override
    public String getName() {
        return Name;
    }
    
    @Override
    public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            PTypeProvider pProvider) {
        String S = pThisResult.text();
        if (S == pParam)
            return true;
        if ((S == null) || (pParam == null))
            return false;
        return (S.toLowerCase().equals(pParam.toLowerCase()));
    }
    
}
