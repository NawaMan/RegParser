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

package net.nawaman.regparser;

import net.nawaman.regparser.result.ParseResult;

/**
 * RegParser Type for Back referencing with case-insensitive checking.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class ParserTypeBackRefCaseInsensitive extends ParserTypeBackRef {
	
	private static final long serialVersionUID = 4711433513141637509L;
	
	public static final ParserTypeBackRefCaseInsensitive BackRefCI_Instance = new ParserTypeBackRefCaseInsensitive();
	
	ParserTypeBackRefCaseInsensitive() {
	}
	
	@Override
	public String name() {
		return "$BackRefCI?";
	}
	
	@Override
	public boolean doValidate(
					ParseResult   hostResult,
					ParseResult   thisResult,
					String        parameter,
					ParserTypeProvider typeProvider) {
		if (hostResult == null)
			return false;
		
		var word = hostResult.lastStringOf(parameter);
		var text = thisResult.text();
		
		if (word == text)
			return true;
		
		if ((word == null)
		 || (text == null))
			return false ;
		
		word = word.toLowerCase();
		text = text.toLowerCase();
		return word.equals(text);
	}
	
}
