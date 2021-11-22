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

import static net.nawaman.regparser.RPCompiler_ParserTypes.escapeOfRegParser;

/**
 * The checker that is associated with a range of character
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharRange extends CharChecker {
	
	private static final long serialVersionUID = 2356484436956456452L;
	
	private final char startChar;
	private final char endChar;
	
	/** Construct a character range */
	public CharRange(char startChar, char endChar) {
		this.startChar = (startChar < endChar) ? startChar : endChar;
		this.endChar   = (startChar < endChar) ? endChar   : startChar;
	}
	
	@Override
	public boolean inSet(char c) {
		return (c >= startChar)
		    && (c <= endChar);
	}
	
	@Override
	public String toString() {
		if ((this.startChar == 0)
		 && (this.endChar   == Character.MAX_VALUE))
			return ".";
		
		var start = escapeOfRegParser("" + this.startChar);
		var end   = escapeOfRegParser("" + this.endChar);
		return "[" + start + "-" + end + "]";
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharRange))
			return false;
		
		return (startChar == ((CharRange)O).startChar)
		    && (endChar   == ((CharRange)O).endChar);
	}
	
	@Override
	public int hashCode() {
		return "CharRange".hashCode() + startChar + endChar;
	}
	
}
