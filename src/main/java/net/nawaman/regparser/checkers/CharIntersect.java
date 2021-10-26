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

import static java.lang.reflect.Array.getLength;

import java.util.ArrayList;
import java.util.HashSet;

import net.nawaman.regparser.Checker;

/**
 * Char checker that is the character is must be in all CharCheckers.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharIntersect extends CharChecker {
    
    private static final long serialVersionUID = 2351321651321651211L;
    
    /** Constructs a char set */
    public CharIntersect(CharChecker... charCheckers) {
        // Combine if one of them is alternative
        
        var list = new ArrayList<CharChecker>();
        for (int i = 0; i < charCheckers.length; i++) {
            var charChecker = charCheckers[i];
            if (charChecker != null) {
                if (charChecker instanceof CharIntersect) {
                    var charIntersect = (CharIntersect) charChecker;
                    for (int c = 0; c < charIntersect.charCheckers.length; c++)
                        list.add(charIntersect.charCheckers[c]);
                } else {
                    list.add(charCheckers[i]);
                }
            }
        }
        // Generate the array
        this.charCheckers = new CharChecker[list.size()];
        for (int i = 0; i < list.size(); i++) {
            this.charCheckers[i] = list.get(i);
        }
    }
    
    private final CharChecker[] charCheckers;
    
    @Override
    public boolean inSet(char c) {
        for (var charChecker : this.charCheckers) {
            if (!charChecker.inSet(c)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        var buffer = new StringBuffer();
        buffer.append("[");
        if (this.charCheckers != null) {
            for (int i = 0; i < getLength(this.charCheckers); i++) {
                var checker = this.charCheckers[i];
                if (checker == null) {
                    continue;
                }
                if (i != 0) {
                    buffer.append("&&");
                }
                buffer.append(checker.toString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this) {
            return true;
        }
        if (!(O instanceof CharIntersect)) {
            return false;
        }
        var checkers = new HashSet<CharChecker>();
        for (var charChecker : this.charCheckers) {
            checkers.add(charChecker);
        }
        for (var charChecker : ((CharIntersect) O).charCheckers) {
            if (!checkers.contains(charChecker)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return "CharIntersect".hashCode() + this.charCheckers.hashCode();
    }
    
    @Override
    public Checker optimize() {
        if (this.charCheckers.length == 1) {
            return this.charCheckers[0];
        }
        return this;
    }
    
}
