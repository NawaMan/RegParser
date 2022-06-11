/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2022 Nawapunth Manusitthipol.
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
package net.nawaman.regparser;

/**
 * Classes implemening this interface can has quantifier attach to it.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public interface Quantifiable<SOURCE> {
	
	public SOURCE quantifier(Quantifier quantifier);
	
	public default Quantifier quantifier() {
		return Quantifier.One;
	}
	
	/** Set the quantifier to lowerBound (Possessive) */
	public default SOURCE from(int lowerBound) {
		return quantifier(new Quantifier(lowerBound, Greediness.Default));
	}
	
	/** Set the quantifier to upperBound (Possessive) */
	public default SOURCE upto(int upperBound) {
		return quantifier(new Quantifier(-1, upperBound, Greediness.Default));
	}
	
	/** Set the quantifier to the given quantifier */
	public default SOURCE bound(Quantifier quantifier) {
		return quantifier(quantifier);
	}
	
	/** Set the quantifier to lowerBound,upperBound (Possessive) */
	public default SOURCE bound(int lowerBound, int upperBound) {
		return quantifier(new Quantifier(lowerBound, upperBound, Greediness.Default));
	}
	
	/** Set the quantifier to lowerBound,upperBound with greediness */
	public default SOURCE bound(int lowerBound, int upperBound, Greediness greediness) {
		return quantifier(new Quantifier(lowerBound, upperBound, greediness));
	}
	
	/** Set the quantifier to Zero (Possessive) */
	public default SOURCE zero() {
		return quantifier(Quantifier.Zero);
	}
	
	/** Set the quantifier to One (Possessive) */
	public default SOURCE one() {
		return quantifier(Quantifier.One);
	}
	
	/** Set the quantifier to ZeroOrOne (Possessive) */
	public default SOURCE zeroOrOne() {
		return quantifier(Quantifier.ZeroOrOne);
	}
	
	/** Set the quantifier to ZeroOrMore (Possessive) */
	public default SOURCE zeroOrMore() {
		return quantifier(Quantifier.ZeroOrMore);
	}
	
	/** Set the quantifier to OneOrMore (Possessive) */
	public default SOURCE oneOrMore() {
		return quantifier(Quantifier.OneOrMore);
	}
	
	/** Set the quantifier to have maximum greediness */
	public default SOURCE maximum() {
		var quantifier = quantifier();
		return quantifier(quantifier.withMaximum());
	}
	
	/** Set the quantifier to have minimum greediness */
	public default SOURCE minimum() {
		var quantifier = quantifier();
		return quantifier(quantifier.withMinimum());
	}
	
	/** Set the quantifier to have obsessive greediness */
	public default SOURCE obsessive() {
		var quantifier = quantifier();
		return quantifier(quantifier.withObsessive());
	}
	
	/** Set the quantifier to have default greediness */
	public default SOURCE defaultGreediness() {
		var quantifier = quantifier();
		return quantifier(quantifier.withDefault());
	}
	
}
