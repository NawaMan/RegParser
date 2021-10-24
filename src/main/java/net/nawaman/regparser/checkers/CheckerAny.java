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

package net.nawaman.regparser.checkers;

import java.util.Hashtable;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;

/**
 * The checker of any character.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class CheckerAny implements Checker {
    
    static private final long serialVersionUID = 1468541215412541527L;
    
    static Hashtable<Integer, CheckerAny> CheckerAnys = new Hashtable<Integer, CheckerAny>();
    
    /** Get an instance of Checker any */
    static public CheckerAny getCheckerAny(int pLength) {
        if (pLength < -1)
            pLength = -1;
        CheckerAny CA = CheckerAnys.get(pLength);
        if (CA == null) {
            CA = new CheckerAny(pLength);
            CheckerAnys.put(pLength, CA);
        }
        return CA;
    }
    
    CheckerAny(int pLength) {
        this.Length = (pLength < -1) ? 1 : pLength;
    }
    
    int Length;
    
    /** Returns the length of this checker */
    public int getLength() {
        return this.Length;
    }
    
    /**{@inheritDoc}*/
    @Override
    public Checker getOptimized() {
        return this;
    }
    
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int startLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
        return this.startLengthOf(S, pOffset, pProvider, null);
    }
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int startLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
        int SL = (S == null) ? 0 : S.length();
        if (pOffset >= SL)
            return 0;
        if (this.Length == -1)
            return (SL - pOffset);
        return this.Length;
    }
    
    
    /**{@inheritDoc}*/
    @Override
    public String toString() {
        return "." + ((this.Length == -1) ? "*" : "{" + this.Length + "}");
    }
}
