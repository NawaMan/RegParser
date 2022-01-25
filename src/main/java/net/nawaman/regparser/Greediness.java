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
 * Greediness specified how quantifier should be handled.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public enum Greediness {
	
	/** Maximum possible match that still allow the later part to match */
	Maximum,
	/** Minimum possible match that still allow the later part to match */
	Minimum,
	/** Eat all token until no longer can and do not care about the later part */
	Possessive;
	
	/** Symbol for Maximum in RegParser language */
	public static final String MaximumSign = "+";
	public static final String MinimumSign = "*";
	
	/** Checks if this is a maximum greediness */
	public boolean isMaximum() {
		return this == Maximum;
	}
	
	/** Checks if this is a minimum greediness */
	public boolean isMinimum() {
		return this == Minimum;
	}
	
	/** Checks if this is a possessive greediness */
	public boolean isPossessive() {
		return this == Possessive;
	}
	
	/** Returns the sign of this greediness */
	public String sign() {
		switch (this) {
			case Maximum:
				return MaximumSign;
			
			case Minimum:
				return MinimumSign;
			
			case Possessive:
			return "";
		}
		return "";
	}
	
}
