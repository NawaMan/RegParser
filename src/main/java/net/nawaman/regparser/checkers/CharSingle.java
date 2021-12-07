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
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.EscapeHelpers.escapeOfRegParser;

/**
 * Single character char set
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
/**  */
public class CharSingle extends CharChecker {
	
	private static final long serialVersionUID = 1651564132135121525L;
	
	public final char ch;
	
	/** Construct a character range */
	public CharSingle(char ch) {
		this.ch = ch;
	}
	
	/** Checks of the char c is in this char checker */
	@Override
	public boolean inSet(char c) {
		return (c == this.ch);
	}
	
	@Override
	public String toString() {
		var escapeOfRegParser = escapeOfRegParser("" + this.ch);
		return "[" + escapeOfRegParser + "]";
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharSingle))
			return false;
		
		return ch == ((CharSingle)O).ch;
	}
	
	@Override
	public int hashCode() {
		return "CharSingle".hashCode() + ch;
	}
	
}
