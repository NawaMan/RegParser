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

import net.nawaman.regparser.RPCompiler_ParserTypes;

/**
 * The checker that is associated with a range of character
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharRange extends CharChecker {
    
    static private final long serialVersionUID = 2356484436956456452L;
    
    /** Construct a character range */
    public CharRange(char pStartC, char pEndC) {
        this.StartC = pStartC;
        this.EndC   = pEndC;
        if (this.StartC > this.EndC) {
            this.StartC = pEndC;
            this.EndC   = pStartC;
        }
    }
    
    char StartC;
    char EndC;
    
    /** Checks of the char c is in this char checker */
    @Override
    public boolean inSet(char c) {
        return (c >= this.StartC) && (c <= this.EndC);
    }
    
    @Override
    public String toString() {
        if ((this.StartC == 0) && (this.EndC == Character.MAX_VALUE))
            return ".";
        return "[" + RPCompiler_ParserTypes.escapeOfRegParser("" + this.StartC) + "-"
                + RPCompiler_ParserTypes.escapeOfRegParser("" + this.EndC) + "]";
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        if (!(O instanceof CharRange))
            return false;
        return (this.StartC == ((CharRange) O).StartC) && (this.EndC == ((CharRange) O).EndC);
    }
    
    @Override
    public int hashCode() {
        return "CharRange".hashCode() + this.StartC + this.EndC;
    }
}
