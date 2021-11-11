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
 * package. You can inform me via me<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser;

import net.nawaman.regparser.checkers.CheckerAny;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.PRNode;
import net.nawaman.regparser.result.PRRoot;
import net.nawaman.regparser.result.PRTemp;

/**
 * RegParser Type for Back referencing.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTBackRef extends PType {
    
    static public final PTBackRef BackRef_Instance = new PTBackRef();
    
    PTBackRef() {
    }
    
    /**{@inheritDoc}*/
    @Override
    public String name() {
        return "$BackRef?";
    }
    
    String getLastMatchByName(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
        if (pHostResult == null)
            return null;
        String W = pHostResult.textOf(pParam);
        
        // Elevate up (in case pHostResult is a node)
        while (W == null) {
            if (pHostResult instanceof PRTemp)
                pHostResult = ((PRTemp) pHostResult).first();
            else
                if (pHostResult instanceof PRNode)
                    pHostResult = ((PRNode) pHostResult).parent();
            if (pHostResult == null)
                return null;
            else {
                var E = pHostResult.lastEntryOf(pParam);
                if ((E == null) && (pHostResult instanceof PRRoot))
                    return null;
                W = pHostResult.textOf(pParam);
            }
        }
        return W;
    }
    
    /**{@inheritDoc}*/
    @Override
    public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
        String W = this.getLastMatchByName(pHostResult, pParam, pProvider);
        if (W == null)
            return WordChecker.EmptyWord;
        return CheckerAny.getCheckerAny(W.length());
    }
    
    /**{@inheritDoc}*/
    @Override
    public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            PTypeProvider pProvider) {
        String W = this.getLastMatchByName(pHostResult, pParam, pProvider);
        if (W == null)
            return false;
        return W.equals(pThisResult.text());
    }
}
