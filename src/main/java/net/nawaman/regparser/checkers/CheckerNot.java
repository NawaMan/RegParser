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

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;

/**
 * Checker for checking the the given checker is not found. 0 will be returned if that is the case.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CheckerNot implements Checker {
    
    static private final long serialVersionUID = 4485946546354964247L;
    
    public CheckerNot(Checker pChecker) {
        if (pChecker == null)
            throw new NullPointerException();
        this.Checker = pChecker;
    }
    
    Checker Checker;
    
    
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
        if (this.Checker.startLengthOf(S, pOffset, pProvider, pResult) != -1)
            return -1;
        return 1;
    }
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker getOptimized() {
        if (this.Checker instanceof CheckerNot)
            return ((CheckerNot) this.Checker).Checker;
        return this;
    }
    
    @Override
    public String toString() {
        return "(^" + this.Checker.toString() + ")";
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        if (!(O instanceof CheckerNot))
            return false;
        return this.Checker.equals(((CheckerNot) O).Checker);
    }
    
    @Override
    public int hashCode() {
        return "CheckerNot".hashCode() + this.Checker.hashCode();
    }
}
