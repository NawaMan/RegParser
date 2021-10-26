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
    
    private static final long serialVersionUID = 1468541215412541527L;
    
    static Hashtable<Integer, CheckerAny> checkerAnys = new Hashtable<Integer, CheckerAny>();
    
    /** Get an instance of Checker any */
    public static CheckerAny getCheckerAny(int length) {
        if (length < -1) {
            length = -1;
        }
        var checkerAny = checkerAnys.get(length);
        if (checkerAny == null) {
            checkerAny = new CheckerAny(length);
            checkerAnys.put(length, checkerAny);
        }
        return checkerAny;
    }
    
    CheckerAny(int length) {
        this.length = (length < -1) ? 1 : length;
    }
    
    private final int length;
    
    /** Returns the length of this checker */
    public int length() {
        return this.length;
    }
    
    /**{@inheritDoc}*/
    @Override
    public Checker optimize() {
        return this;
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider) {
        return this.startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        int SL = (text == null) ? 0 : text.length();
        if (offset >= SL) {
            return 0;
        }
        if (this.length == -1) {
            return (SL - offset);
        }
        return this.length;
    }
    
    
    @Override
    public String toString() {
        return "." + ((this.length == -1) ? "*" : "{" + this.length + "}");
    }
}
