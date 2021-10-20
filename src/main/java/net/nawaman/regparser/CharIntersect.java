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

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Vector;

/**
 * Char checker that is the character is must be in all CharCheckers.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharIntersect extends CharChecker {
    
    static private final long serialVersionUID = 2351321651321651211L;
    
    /** Constructs a char set */
    public CharIntersect(CharChecker... pCharCheckers) {
        // Combine if one of them is alternative
        
        Vector<CharChecker> CCs = new Vector<CharChecker>();
        for (int i = 0; i < pCharCheckers.length; i++) {
            CharChecker C = pCharCheckers[i];
            if (C != null) {
                if (C instanceof CharIntersect) {
                    CharIntersect CI = (CharIntersect) C;
                    for (int c = 0; c < CI.CharCheckers.length; c++)
                        CCs.add(CI.CharCheckers[c]);
                } else
                    CCs.add(pCharCheckers[i]);
            }
        }
        // Generate the array
        this.CharCheckers = new CharChecker[CCs.size()];
        for (int i = 0; i < CCs.size(); i++)
            this.CharCheckers[i] = CCs.get(i);
    }
    
    CharChecker[] CharCheckers;
    
    /** Checks of the char c is in this char checker */
    @Override
    public boolean inSet(char c) {
        for (CharChecker CC : this.CharCheckers)
            if (!CC.inSet(c))
                return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuffer SB = new StringBuffer();
        SB.append("[");
        if (this.CharCheckers != null) {
            for (int i = 0; i < Array.getLength(this.CharCheckers); i++) {
                Checker C = this.CharCheckers[i];
                if (C == null)
                    continue;
                if (i != 0)
                    SB.append("&&");
                SB.append(C.toString());
            }
        }
        SB.append("]");
        return SB.toString();
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        if (!(O instanceof CharIntersect))
            return false;
        HashSet<CharChecker> CCs = new HashSet<CharChecker>();
        for (CharChecker CC : this.CharCheckers)
            CCs.add(CC);
        for (CharChecker CC : ((CharIntersect) O).CharCheckers) {
            if (!CCs.contains(CC))
                return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return "CharIntersect".hashCode() + this.CharCheckers.hashCode();
    }
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker getOptimized() {
        if (this.CharCheckers.length == 1)
            return this.CharCheckers[0];
        return this;
    }
    
}
