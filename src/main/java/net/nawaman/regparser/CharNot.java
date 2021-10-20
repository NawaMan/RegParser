/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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
 * The char set of a opposite of a given char set.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharNot extends CharChecker {
    
    static private final long serialVersionUID = 5313543126135165121L;
    
    /** Construct a character range */
    public CharNot(CharChecker pCharChecker) {
        if (pCharChecker == null)
            throw new NullPointerException();
        this.CC = (pCharChecker instanceof CharNot) ? ((CharNot) pCharChecker).CC : pCharChecker;
    }
    
    CharChecker CC;
    
    /** Checks of the char c is in this char checker */
    @Override
    public boolean inSet(char c) {
        return !this.CC.inSet(c);
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        if (!(O instanceof CharNot))
            return false;
        return this.CC.equals(((CharNot) O).CC);
    }
    
    @Override
    public int hashCode() {
        return "CharNot".hashCode() + this.CC.hashCode();
    }
    
    @Override
    public String toString() {
        return "[^" + this.CC.toString() + "]";
    }
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker getOptimized() {
        if (this.CC instanceof CharNot)
            return ((CharNot) this.CC).CC;
        return this;
    }
}
