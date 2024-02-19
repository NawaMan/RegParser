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

import static net.nawaman.regparser.RegParserEntry.newParserEntry;

import java.io.Serializable;

import net.nawaman.regparser.result.ParseResult;

/**
 * A basic building block for RegParser.
 * 
 * Checker can check if a given CharSequence starts with characters in the right sequence.
 * If match, the length if the match is returned; Otherwise, -1 is returned. 
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface Checker extends AsChecker, AsRegParserEntry, Quantifiable<RegParserEntry>, Serializable {
    
    /** Returns the empty array of Checkers */
    public static final Checker[] EMPTY_CHECKER_ARRAY = new Checker[0];
    
    /**
     * Returns the length of the match if the string S starts (from offset) with this checker.<br />
     * @param  text    the string to be parsed
     * @param  offset  the starting point of the checking
     * @return         the length of the match or -1 if the string S does not start with this checker
     */
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider);
    
    /**
     * Returns the length of the match if the string S starts (from offset) with this checker.<br />
     * @param  text         the string to be parsed
     * @param  offset       the starting point of the checking
     * @param  parseResult  the parse result of the current parsing. Only given in special case from RegParser.
     * @return              the length of the match or -1 if the string S does not start with this checker
     */
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult);
    
    /** Return the optimized version of this Checker */
    public default Checker optimize() {
        return this;
    }
    
    @Override
    public default Checker asChecker() {
        return this;
    }
    
    @Override
    public default Quantifier quantifier() {
        return Quantifier.One;
    }
    
    @Override
    public default RegParserEntry asRegParserEntry() {
        return newParserEntry(null, this, null);
    }
    
    public default RegParserEntry with(String name, Quantifier quantifier) {
        return newParserEntry(name, this, quantifier);
    }
    
    public default RegParserEntry with(Quantifier quantifier) {
        return newParserEntry(null, this, quantifier);
    }
    
    public default RegParserEntry with(int bound) {
        return newParserEntry(null, this, new Quantifier(bound, bound));
    }
    
    public default RegParserEntry with(int lowerBound, int upperBound) {
        return newParserEntry(null, this, new Quantifier(lowerBound, upperBound));
    }
    
    public default RegParserEntry withName(String name) {
        return newParserEntry(name, this, null);
    }
    
    public default RegParserEntry quantifier(Quantifier quantifier) {
        return newParserEntry(null, this, quantifier);
    }
    
}
