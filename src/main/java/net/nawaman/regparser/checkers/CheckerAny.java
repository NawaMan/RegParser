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

import java.util.HashMap;
import java.util.Map;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * The checker of any character.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public final class CheckerAny implements Checker {
    
    private static final long serialVersionUID = 1468541215412541527L;
    
    static Map<Integer, CheckerAny> checkerAnys = new HashMap<Integer, CheckerAny>();
    
    private final int length;
    
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
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
        return startLengthOf(text, offset, typeProvider, null);
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
        int SL = (text == null) ? 0 : text.length();
        if (offset >= SL)
            return 0;
        
        if (length == -1)
            return (SL - offset);
        
        return length;
    }
    
    @Override
    public final Boolean isDeterministic() {
        return (length >= 0);
    }
    
    @Override
    public String toString() {
        return "." + ((length == -1) ? "*" : "{" + length + "}");
    }
    
    private int hashCode = 0;
    
    @Override
    public int hashCode() {
        if (hashCode != 0) {
            return hashCode;
        }
        
        hashCode = toString().hashCode();
        return hashCode;
    }
}
