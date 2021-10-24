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

package net.nawaman.regparser;

import static java.lang.String.format;
import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.Greediness.Minimum;
import static net.nawaman.regparser.Greediness.Possessive;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Quantifier for matching
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class Quantifier implements Serializable {
    
    private static final long serialVersionUID = 1930305369240858722L;
    
    /** Predefine Quantifier for Zero */
    public static final Quantifier Zero       = new Quantifier(0, 0, Possessive);
    /** Predefine Quantifier for One */
    public static final Quantifier One        = new Quantifier(1, 1, Possessive);
    /** Predefine Quantifier for ZeroOrOne */
    public static final Quantifier ZeroOrOne  = new Quantifier(0, 1, Possessive);
    /** Predefine Quantifier for ZeroOrMore */
    public static final Quantifier ZeroOrMore = new Quantifier(0, Possessive);
    /** Predefine Quantifier for OneOrMore */
    public static final Quantifier OneOrMore  = new Quantifier(1, Possessive);
    
    /** Predefine Quantifier for ZeroOrOne */
    public static final Quantifier ZeroOrOne_Maximum     = new Quantifier(0, 1, Maximum);
    /** Predefine Quantifier for ZeroOrMore */
    public static final Quantifier ZeroOrMore_Maximum    = new Quantifier(0, Maximum);
    /** Predefine Quantifier for OneOrMore */
    public static final Quantifier OneOrMore_Maximum     = new Quantifier(1, Maximum);
    /** Predefine Quantifier for ZeroOrOne */
    public static final Quantifier ZeroOrOne_Minimum     = new Quantifier(0, 1, Minimum);
    /** Predefine Quantifier for ZeroOrMore */
    public static final Quantifier ZeroOrMore_Minimum    = new Quantifier(0, Minimum);
    /** Predefine Quantifier for OneOrMore */
    public static final Quantifier OneOrMore_Minimum     = new Quantifier(1, Minimum);
    /** Predefine Quantifier for ZeroOrOne */
    public static final Quantifier ZeroOrOne_Possessive  = ZeroOrOne;
    /** Predefine Quantifier for ZeroOrMore */
    public static final Quantifier ZeroOrMore_Possessive = ZeroOrMore;
    /** Predefine Quantifier for OneOrMore */
    public static final Quantifier OneOrMore_Possessive  = OneOrMore;
    
    /** The upper bound value for no upper bound. */
    public static final int NO_UPPERBOUND = -1;
    
    /**
     * Returns toString for the quantifier or empty string if the given value is null.
     * 
     * @param quantifier  the quantifier.
     * @return            the toString for the quantifier or empty string if the given value is null.
     */
    public static String toString(Quantifier quantifier) {
        return (quantifier == null) ? "" : quantifier.toString();
    }
    
    private final int        lowerBound;
    private final int        upperBound;
    private final Greediness greediness;
    
    private transient final AtomicReference<String> toString = new AtomicReference<>(null);
    
    public Quantifier(int lowerBound, Greediness greediness) {
        this(lowerBound, -1, greediness);
    }
    
    public Quantifier(int lowerBound, int upperBound) {
        this(lowerBound, upperBound, null);
    }
    
    public Quantifier(int lowerBound, int upperBound, Greediness greediness) {
        if ((lowerBound < 0) || ((upperBound >= 0) && (lowerBound > upperBound))) {
            var errMsg = format("Invalid qualifier: lowerBound=[%d] upperBound=[%d]", lowerBound, upperBound);
            throw new IllegalArgumentException(errMsg);
        }
        
        if (upperBound < 0)
            upperBound = -1;
        
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.greediness = (greediness != null) ? greediness : Possessive;
    }
    
    
    /** Checks if this Quantifier is a one and possessive */
    public boolean isOne_Possessive() {
        return this.isOne()
            && this.isPossessive();
    }
    
    /** Checks if this Quantifier is a zero */
    public boolean isZero() {
        return (this.lowerBound == 0)
            && (this.upperBound == 0);
    }
    
    /** Checks if this Quantifier is a one */
    public boolean isOne() {
        return (this.lowerBound == 1)
            && (this.upperBound == 1);
    }
    
    /** Checks if this Quantifier is a zero or one */
    public boolean isZeroOrOne() {
        return (this.lowerBound == 0)
            && (this.upperBound == 1);
    }
    
    /** Checks if this Quantifier is a zero or more */
    public boolean isZeroOrMore() {
        return (this.lowerBound == 0)
            && (this.upperBound == -1);
    }
    
    /** Checks if this Quantifier is a one or more */
    public boolean isOneOrMore() {
        return (this.lowerBound == 1)
            && (this.upperBound == -1);
    }
    
    /** Returns the lower bound */
    public int lowerBound() {
        return this.lowerBound;
    }
    
    /** Returns the upper bound (-1 for unlimited) */
    public int upperBound() {
        return this.upperBound;
    }
    
    /** Checks if this quantifier has no upper bound */
    public boolean hasNoUpperBound() {
        return this.upperBound == NO_UPPERBOUND;
    }
    
    /** Checks if this quantifier has no upper bound */
    public boolean hasUpperBound() {
        return this.upperBound != NO_UPPERBOUND;
    }
    
    /** Returns how greedy this quali */
    public Greediness greediness() {
        return this.greediness;
    }
    
    /** Checks if this is a maximum greediness */
    public boolean isMaximum() {
        return this.greediness == Greediness.Maximum;
    }
    
    /** Checks if this is a minimum greediness */
    public boolean isMinimum() {
        return this.greediness == Greediness.Minimum;
    }
    
    /** Checks if this is a possessive greediness */
    public boolean isPossessive() {
        return this.greediness == Greediness.Possessive;
    }
    
    /** @return  a quantifier with this lower/upper bound but with possessive greediness. */
    public Quantifier withPossessive() {
        return (greediness == Possessive)
                ? this
                : new Quantifier(lowerBound, upperBound, Possessive);
    }
    
    /** @return  a quantifier with this lower/upper bound but with maximum greediness. */
    public Quantifier withMaximum() {
        return (greediness == Maximum)
                ? this
                : new Quantifier(lowerBound, upperBound, Maximum);
    }
    
    /** @return  a quantifier with this lower/upper bound but with minimum greediness. */
    public Quantifier withMinimum() {
        return (greediness == Minimum)
                ? this
                : new Quantifier(lowerBound, upperBound, Minimum);
    }
    
    /** Returns the string representation of the qualifier */
    @Override
    public String toString() {
        return toString.updateAndGet(oldValue -> {
            if (oldValue != null) {
                return oldValue;
            }
            
            if (this.isZeroOrOne()) {
                return "?" + this.greediness.getSign();
            }
            if (this.isZeroOrMore()) {
                return "*" + this.greediness.getSign();
            }
            if (this.isOneOrMore()) {
                return "+" + this.greediness.getSign();
            }
            
            if (this.isZero()) {
                return "{0}";
            }
            if (this.isOne()) {
                return "" + this.greediness.getSign();
            }
            
            if (this.lowerBound == this.upperBound) {
                return "{" + this.lowerBound + "}" + this.greediness.getSign();
            }
            if (this.lowerBound == 0) {
                return "{," + this.upperBound + "}" + this.greediness.getSign();
            }
            if (this.upperBound == -1) {
                return "{" + this.lowerBound + ",}" + this.greediness.getSign();
            }
            
            return "{" + this.lowerBound + "," + this.upperBound + "}" + this.greediness.getSign();
        });
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(greediness, lowerBound, toString, upperBound);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        var other = (Quantifier)obj;
        return greediness == other.greediness
            && lowerBound == other.lowerBound
            && upperBound == other.upperBound;
    }
    
}
