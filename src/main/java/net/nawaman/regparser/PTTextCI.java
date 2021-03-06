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

// Usage: !text("Text")! will match everything that is equals to "Text" case insensitively

import java.util.*;

/**
 * Parser Type for Case-Insensitive Text
 *  
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTTextCI extends PType {
	
	static Hashtable<Integer, Checker> Checkers = new Hashtable<Integer, Checker>();
	
	static public String Name = "textCI";
	@Override public String getName() { return Name; }
	@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
		if(pParam == null) pParam = "";
		int     L = pParam.length();
		Checker C = Checkers.get(L);
		if(C != null) return C;
		C = RegParser.newRegParser(PredefinedCharClasses.Any, new Quantifier(L, L));
		Checkers.put(L, C);
		return C;
	}
	@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
			PTypeProvider pProvider) {
		String S = pThisResult.getText();
		if(S == pParam)                     return  true;
		if((S == null) || (pParam == null)) return false;
		return (S.toLowerCase().equals(pParam.toLowerCase()));
	}
}
