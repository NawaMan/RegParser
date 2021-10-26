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

import static java.util.Objects.requireNonNull;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;

/**
 * Checker for checking the the given checker is not found. 0 will be returned if that is the case.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CheckerNot implements Checker {
    
    private static final long serialVersionUID = 4485946546354964247L;
    
    public CheckerNot(Checker checker) {
        this.checker = requireNonNull(checker);
    }
    
    private final Checker checker;
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider) {
        return this.startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        if (this.checker.startLengthOf(text, offset, typeProvider, parseResult) != -1) {
            return -1;
        }
        return 1;
    }
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker optimize() {
        if (this.checker instanceof CheckerNot) {
            return ((CheckerNot) this.checker).checker;
        }
        return this;
    }
    
    @Override
    public String toString() {
        return "(^" + this.checker.toString() + ")";
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this) {
            return true;
        }
        if (!(O instanceof CheckerNot)) {
            return false;
        }
        return this.checker.equals(((CheckerNot) O).checker);
    }
    
    @Override
    public int hashCode() {
        return "CheckerNot".hashCode() + this.checker.hashCode();
    }
}
