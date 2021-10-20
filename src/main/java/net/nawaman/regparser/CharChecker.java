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

/**
 * Checker that involves a character.
 * 
 * General implementation of this class is that if the first character is in a set, 1 will be returned. Otherwise -1 is
 *   returned. 
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
abstract public class CharChecker implements Checker {
    
    /** Returns the empty array of CharCheckers */
    static public final CharChecker[] EmptyCharCheckerArray = new CharChecker[0];
    
    /** Checks of the char c is in this char checker */
    abstract public boolean inSet(char c);

    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
        return this.getStartLengthOf(S, pOffset, pProvider, null);
    }
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
        if ((pOffset < 0) || (pOffset >= S.length()))
            return -1;
        char c = S.charAt(pOffset);
        return this.inSet(c) ? 1 : -1;
    }
    
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker getOptimized() {
        return this;
    }
}
