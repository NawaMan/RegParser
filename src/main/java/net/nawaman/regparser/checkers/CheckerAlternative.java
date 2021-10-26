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

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.RegParser;

/**
 * Checker for alternative values.
 * 
 * If HasDefault is false (which is the default), all sub checkers are checked against the CharSequence and the longest
 *   match length will be accepted as the match value. The checking is done in reverse order so the first longest match
 *   from the last Checker is accepted as match.
 * If HasDefault is true, all Checkers except the last will be checked. If and only if no match is found, the last
 *   Checker will be used.
 * For example:  
 *     "5" / (($Low:~[0-5]~) |  ($High:~[5-9]~)) will match as '$High'
 *     "5" / (($Low:~[0-5]~) || ($High:~[5-9]~)) will match as '$Low'
 * 
 * NOTE: The reverse order used in CheckerAlternative is only because of a historical reason. This will likely to
 *         changed in the later version.
 * NOTE: In the later version, multiple default checker should also be implemented as it used much more often than
 *         first expected. Have that built-in should improve performance.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CheckerAlternative implements Checker {
    
    private static final long serialVersionUID = 2146515415886541851L;
    
    /** Constructs a char set */
    public CheckerAlternative(Checker... checkers) {
        this(false, checkers);
    }
    
    /** Constructs a char set */
    public CheckerAlternative(boolean hasDefault, Checker... checkers) {
        // Combine if one of them is alternative
        
        var list = new ArrayList<Checker>();
        for (int i = 0; i < (checkers.length - (hasDefault ? 1 : 0)); i++) {
            var checker = checkers[i];
            if (checker != null) {
                if ((checker instanceof CheckerAlternative) && !((CheckerAlternative) checker).hasDefault()) {
                    var checkerAlternative = (CheckerAlternative) checker;
                    for (int c = 0; c < checkerAlternative.checkers.length; c++) {
                        list.add(checkerAlternative.checkers[c]);
                    }
                } else {
                    list.add(checker);
                }
            }
        }
        
        // Generate the array
        this.checkers = new Checker[list.size()];
        for (int i = 0; i < list.size(); i++) {
            var checker = list.get(i);
            if (checker instanceof RegParser) {
                checker = ((RegParser) checker).optimize();
            }
            this.checkers[i] = checker;
        }
        
        var defaultValue = hasDefault ? checkers[checkers.length - 1] : null;
        if (defaultValue != null) {
            defaultValue = defaultValue.optimize();
        }
        this.defaultChecker = defaultValue;
    }
    
    public final Checker   defaultChecker;
    public final Checker[] checkers;
    
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider) {
        return this.startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        for (int i = this.checkers.length; --i >= 0;) {
            var checker = this.checkers[i];
            int index   = checker.startLengthOf(text, offset, typeProvider, parseResult);
            if (index != -1) {
                return index;
            }
        }
        return (this.defaultChecker != null)
                ? this.defaultChecker.startLengthOf(text, offset, typeProvider, parseResult)
                : -1;
    }
    
    public boolean hasDefault() {
        return this.defaultChecker != null;
    }
    
    public Checker defaultChecker() {
        return this.defaultChecker;
    }
    
    // Object ----------------------------------------------------------------------------------------------------------
    
    @Override
    public String toString() {
        var buffer = new StringBuffer();
        buffer.append("(");
        if (this.checkers != null) {
            for (int i = 0; i < this.checkers.length; i++) {
                var checker = this.checkers[i];
                if (checker == null) {
                    continue;
                }
                if (i != 0) {
                    buffer.append("|");
                }
                buffer.append(checker.toString());
            }
        }
        if (this.defaultChecker != null) {
            buffer.append("||").append(this.defaultChecker);
        }
        buffer.append(")");
        return buffer.toString();
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this) {
            return true;
        }
        if (!(O instanceof CheckerAlternative)) {
            return false;
        }
        if (this.checkers.length != ((CheckerAlternative) O).checkers.length) {
            return false;
        }
        for (int i = this.checkers.length; --i >= 0;) {
            if (!this.checkers[i].equals(((CheckerAlternative) O).checkers[i])) {
                return false;
            }
        }
        return (this.defaultChecker != null) ? this.defaultChecker.equals(((CheckerAlternative) O).defaultChecker) : true;
    }
    
    @Override
    public int hashCode() {
        int h = "CheckerAlternative".hashCode();
        for (int i = 0; i < this.checkers.length; i++) {
            h += this.checkers[i].hashCode();
        }
        
        return h + ((this.defaultChecker != null) ? this.defaultChecker.hashCode() : 0);
    }
    
    @Override
    public Checker optimize() {
        return this;
    }
    
}
