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
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker that involves a character.
 * 
 * General implementation of this class is that if the first character is in a set, 1 will be returned.
 * Otherwise -1 is returned. 
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
abstract public class CharChecker implements Checker {
    
    /** Returns the empty array of CharCheckers */
    public static final CharChecker[] EMPTY_CHAR_CHECKER_ARRAY = new CharChecker[0];
    
    /** Checks of the char c is in this char checker */
    abstract public boolean inSet(char c);
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider) {
        return this.startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        if ((offset < 0) || (offset >= text.length())) {
            return -1;
        }
        
        char c = text.charAt(offset);
        return this.inSet(c) ? 1 : -1;
    }
    
    @Override
    public Checker optimize() {
        return this;
    }
}
