/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via me<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser;

/**
 * RegParser Type for Back referencing with case-insensitive checking.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTBackRefCI extends PTBackRef {
    
    static public final PTBackRefCI BackRefCI_Instance = new PTBackRefCI();
    
    PTBackRefCI() {
    }
    
    @Override
    public String getName() {
        return "$BackRefCI?";
    }
    
    @Override
    public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            PTypeProvider pProvider) {
        if (pHostResult == null)
            return false;
        String W = pHostResult.getLastStrMatchByName(pParam);
        String T = pThisResult.getText().toLowerCase();
        if (W == T)
            return true;
        if ((W == null) || (T == null))
            return true;
        return W.toLowerCase().equals(pThisResult.getText().toLowerCase());
    }
    
}
