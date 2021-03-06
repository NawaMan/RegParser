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
 * Single character char set
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
/**  */
public class CharSingle extends CharChecker {
	
	static private final long serialVersionUID = 1651564132135121525L;
	
	/** Construct a character range */
	public CharSingle(char pC) { this.C = pC; }
	
	char C;

	/** Checks of the char c is in this char checker */
	@Override public boolean inSet(char c) {
		return (c == this.C);
	}

	@Override public String toString() {
		return "[" + RPCompiler_ParserTypes.escapeOfRegParser("" + this.C) + "]";
	}
	@Override public boolean equals(Object O) {
		if(O == this) return true;
		if(!(O instanceof CharSingle)) return false;
		return this.C == ((CharSingle)O).C;
	}
	
	@Override public int hashCode() {
		return "CharSingle".hashCode() + this.C;
	}
}
