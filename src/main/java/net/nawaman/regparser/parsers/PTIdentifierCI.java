/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
 *----------------------------------------------------------------------------------------------------------------------
 * LICENSE:
 * 
 * This file is part of Nawa's RegParser.
 * 
 * The project is a free software; you can redistribute it and/or modify it under the SIMILAR terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 * You are only required to inform me about your modification and redistribution as or as part of commercial software
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.parsers;

import net.nawaman.regparser.*;

/**
 * Parser for detecting C-like identifier (for case-insensitive)
 *
 * @author Nawapunth Manusitthipol
 */
@SuppressWarnings("serial")
public class PTIdentifierCI extends PTIdentifier {
	@SuppressWarnings("hiding")
	static public String Name = "$IdentifierCI";
	@Override public String getName() { return Name; }
	@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
			PTypeProvider pProvider) {
		String S = pThisResult.getText();
		if(S == pParam)                     return  true;
		if((S == null) || (pParam == null)) return false;
		return (S.toLowerCase().equals(pParam.toLowerCase()));
	}

}