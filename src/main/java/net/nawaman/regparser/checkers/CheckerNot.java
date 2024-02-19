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

package net.nawaman.regparser.checkers;

import static java.util.Objects.requireNonNull;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker for checking the the given checker is not found. 0 will be returned if that is the case.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class CheckerNot implements Checker {
    
    private static final long serialVersionUID = 4485946546354964247L;
    
    private final Checker checker;
    
    public CheckerNot(Checker checker) {
        this.checker = requireNonNull(checker);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
        return startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
        int startLength = checker.startLengthOf(text, offset, typeProvider, parseResult);
        return (startLength != -1) ? -1 : 1;
    }
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker optimize() {
        return (checker instanceof CheckerNot)
                ? ((CheckerNot)checker).checker
                : this;
    }
    
    @Override
    public final Boolean isDeterministic() {
        return checker.isDeterministic();
    }
    
    @Override
    public String toString() {
        return "(^" + checker.toString() + ")";
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        
        if (!(O instanceof CheckerNot))
            return false;
        
        return checker.equals(((CheckerNot)O).checker);
    }
    
    private int hashCode = 0;
    
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        
        hashCode = "CheckerNot".hashCode() + checker.hashCode();
        return hashCode;
    }
}
