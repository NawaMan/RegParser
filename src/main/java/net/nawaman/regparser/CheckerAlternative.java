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

import java.util.*;

/**
 * Checker for alternative values.
 * 
 * If HasDefault is false (which is the default), all sub checkers are checked against the CharSequence and the longest
 *   match length will be accepted as the match value. The checking is done in reverse order so the first longest match
 *   from the last Checker is accepted as match.
 * If HasDefault is true, all Checkers except the last will be checked. If and only if no match is found, the last
 *   Checker will be used.
 * For example:  
 * 	"5" / (($Low:~[0-5]~) |  ($High:~[5-9]~)) will match as '$High'
 * 	"5" / (($Low:~[0-5]~) || ($High:~[5-9]~)) will match as '$Low'
 * 
 * NOTE: The reverse order used in CheckerAlternative is only because of a historical reason. This will likely to
 *         changed in the later version.
 * NOTE: In the later version, multiple default checker should also be implemented as it used much more often than
 *         first expected. Have that built-in should improve performance.
 * 
 * @author Nawapunth Manusitthipol
 */
public class CheckerAlternative implements Checker {
	
	static private final long serialVersionUID = 2146515415886541851L;
	
	/** Constructs a char set */
	public CheckerAlternative(Checker ...  pCheckers) {
		this(false, pCheckers);
	}
	/** Constructs a char set */
	public CheckerAlternative(boolean pHasDefault, Checker ...  pCheckers) {
		// Combine if one of them is alternative
		
		Vector<Checker> CCs = new Vector<Checker>();
		for(int i = 0; i < (pCheckers.length - (pHasDefault?1:0)); i++) {
			Checker C = pCheckers[i];
			if(C != null) {
				if((C instanceof CheckerAlternative) && !((CheckerAlternative)C).hasDefault()) {
					CheckerAlternative CA = (CheckerAlternative)C;
					for(int c = 0; c < CA.Checkers.length; c++ ) CCs.add(CA.Checkers[c]);
				} else  CCs.add(C);
			}
		}
		// Generate the array
		this.Checkers = new Checker[CCs.size()];
		for(int i = 0; i < CCs.size(); i++) {
			Checker C = CCs.get(i);
			if(C instanceof RegParser) C = ((RegParser)C).getOptimized();
			this.Checkers[i] = C;
		}
		
		this.Default = pHasDefault?pCheckers[pCheckers.length - 1]:null;
		if(this.Default != null) this.Default = this.Default.getOptimized();
	}
	
	Checker   Default;
	Checker[] Checkers;

	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	@Override public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
		return this.getStartLengthOf(S, pOffset, pProvider, null);
	}
	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	@Override public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
		for(int i = this.Checkers.length; --i >= 0; ) {
			Checker C = this.Checkers[i];
			int I = C.getStartLengthOf(S, pOffset, pProvider, pResult);
			if(I != -1) return I;
		}
		return (this.Default != null)?this.Default.getStartLengthOf(S, pOffset, pProvider, pResult):-1;
	}
	
	public boolean hasDefault() { return this.Default != null; }
	public Checker getDefault() { return this.Default;         }
	
	// Object ----------------------------------------------------------------------------------------------------------

	@Override public String toString() {
		StringBuffer SB = new StringBuffer();
		SB.append("(");
		if(this.Checkers != null) {
			for(int i = 0; i < this.Checkers.length; i++) {
				Checker C = this.Checkers[i];
				if(C == null) continue; 
				if(i != 0) SB.append("|");
				SB.append(C.toString());
			}
		}
		if(this.Default != null) SB.append("||").append(this.Default);
		SB.append(")");
		return SB.toString();
	}
	@Override public boolean equals(Object O) {
		if(O == this) return true;
		if(!(O instanceof CheckerAlternative)) return false;
		if(this.Checkers.length != ((CheckerAlternative)O).Checkers.length) return false;
		for(int i = this.Checkers.length; --i >= 0; ) {
			if(!this.Checkers[i].equals(((CheckerAlternative)O).Checkers[i])) return false;
		}
		return (this.Default != null)?this.Default.equals(((CheckerAlternative)O).Default):true;
	}
	@Override public int hashCode() {
		int h = "CheckerAlternative".hashCode();
		for(int i = 0; i < this.Checkers.length; i++)
			h += this.Checkers[i].hashCode();
		
		return h + ((this.Default != null)?this.Default.hashCode():0);
	}

	/** Return the optimized version of this Checker */
	@Override public Checker getOptimized() { return this; }
	
}
