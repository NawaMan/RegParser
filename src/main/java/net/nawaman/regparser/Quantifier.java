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

import java.io.*;

/**
 * Quantifier for matching
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
final public class Quantifier implements Serializable {
	
	/** Predefine Quantifier for Zero */
	static public final Quantifier Zero       = new Quantifier(0,  0, Greediness.Possessive);
	/** Predefine Quantifier for One */
	static public final Quantifier One        = new Quantifier(1,  1, Greediness.Possessive);
	/** Predefine Quantifier for ZeroOrOne */
	static public final Quantifier ZeroOrOne  = new Quantifier(0,  1, Greediness.Possessive);
	/** Predefine Quantifier for ZeroOrMore */
	static public final Quantifier ZeroOrMore = new Quantifier(0,     Greediness.Possessive);
	/** Predefine Quantifier for OneOrMore */
	static public final Quantifier OneOrMore  = new Quantifier(1,     Greediness.Possessive);

	/** Predefine Quantifier for ZeroOrOne */
	static public final Quantifier ZeroOrOne_Maximum     = new Quantifier(0,  1, Greediness.Maximum);
	/** Predefine Quantifier for ZeroOrMore */
	static public final Quantifier ZeroOrMore_Maximum    = new Quantifier(0,     Greediness.Maximum);
	/** Predefine Quantifier for OneOrMore */
	static public final Quantifier OneOrMore_Maximum     = new Quantifier(1,     Greediness.Maximum);
	/** Predefine Quantifier for ZeroOrOne */
	static public final Quantifier ZeroOrOne_Minimum     = new Quantifier(0,  1, Greediness.Minimum);
	/** Predefine Quantifier for ZeroOrMore */
	static public final Quantifier ZeroOrMore_Minimum    = new Quantifier(0,     Greediness.Minimum);
	/** Predefine Quantifier for OneOrMore */
	static public final Quantifier OneOrMore_Minimum     = new Quantifier(1,     Greediness.Minimum);
	/** Predefine Quantifier for ZeroOrOne */
	static public final Quantifier ZeroOrOne_Possessive  = ZeroOrOne;
	/** Predefine Quantifier for ZeroOrMore */
	static public final Quantifier ZeroOrMore_Possessive = ZeroOrMore;
	/** Predefine Quantifier for OneOrMore */
	static public final Quantifier OneOrMore_Possessive  = OneOrMore;
	
	public Quantifier(int pLBound, Greediness pGness) { this(pLBound, -1, pGness);    }
	public Quantifier(int pLBound, int pUBound)       { this(pLBound, pUBound, null); }
	public Quantifier(int pLBound, int pUBound, Greediness pGness) {
		if((pLBound < 0) || ((pUBound >= 0) && (pLBound > pUBound))) throw new IllegalArgumentException();
		if( pUBound < 0) pUBound = -1;
		
		this.LBound = pLBound;
		this.UBound = pUBound;
		this.Gness  = (pGness == null)?Greediness.Possessive:pGness;
	}
	
	int LBound;
	int UBound;
	Greediness Gness;


	/** Checks if this Quantifier is a one and posessive */
	public boolean isOne_Possessive() { return this.isOne() && this.isPossessive(); }

	/** Checks if this Quantifier is a zero */
	public boolean isZero()       { return (this.LBound == 0) && (this.UBound ==  0); }
	/** Checks if this Quantifier is a one */
	public boolean isOne()        { return (this.LBound == 1) && (this.UBound ==  1); }
	/** Checks if this Quantifier is a zero or one */
	public boolean isZeroOrOne()  { return (this.LBound == 0) && (this.UBound ==  1); }
	/** Checks if this Quantifier is a zero or more */
	public boolean isZeroOrMore() { return (this.LBound == 0) && (this.UBound == -1); }
	/** Checks if this Quantifier is a one or more */
	public boolean isOneOrMore()  { return (this.LBound == 1) && (this.UBound == -1); }
	
	/** Returns the lower bound */
	public int getLowerBound() { return this.LBound; }
	/** Returns the upper bound (-1 for unlimited) */
	public int getUpperBound() { return this.UBound; }
	
	/** Checks if this quatifier has no upper bound */
	public boolean isNoUpperBound() { return this.UBound == -1; }
	
	/** Returns how greedy this quali */
	public Greediness getGreediness() { return this.Gness; }


	/** Checks if this is a maximum greediness */
	public boolean isMaximum()    { return this.Gness == Greediness.Maximum;    }
	/** Checks if this is a minimum greediness */
	public boolean isMinimum()    { return this.Gness == Greediness.Minimum;    }
	/** Checks if this is a possessive greediness */
	public boolean isPossessive() { return this.Gness == Greediness.Possessive; }

	/** Returns the string representation of the qualifier */
	@Override public String toString() {

		if(this.isZeroOrOne())  return "?" + this.Gness.getSign();
		if(this.isZeroOrMore()) return "*" + this.Gness.getSign();
		if(this.isOneOrMore())  return "+" + this.Gness.getSign();
		
		if(this.isZero())       return "{0}";
		if(this.isOne())        return "" + this.Gness.getSign();
		
		if(this.LBound == this.UBound) return "{"+this.LBound +"}" + this.Gness.getSign();
		if(this.LBound ==           0) return "{,"+this.UBound+"}" + this.Gness.getSign();
		if(this.UBound ==          -1) return "{"+this.LBound+",}" + this.Gness.getSign();
		
		return "{"+this.LBound+","+this.UBound+"}" + this.Gness.getSign();
	}
	@Override public boolean equals(Object O) {
		if(O == this) return true;
		
		if(O == null) return (this.isOne_Possessive());
		
		if(!(O instanceof Quantifier)) return false;
		Quantifier Q = (Quantifier)O;
		if(this.UBound != Q.UBound)     return false;
		if(this.LBound != Q.LBound)     return false;
		if(!this.Gness.equals(Q.Gness)) return false;
		return true;
	}
	
	static public String toString(Quantifier Q) {
		return (Q == null)?"":Q.toString();
	}
}