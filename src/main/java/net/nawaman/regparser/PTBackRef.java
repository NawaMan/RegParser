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
 * RegParser Type for Back referencing.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTBackRef extends PType {
	
	static public final PTBackRef BackRef_Instance = new PTBackRef();
	
	PTBackRef() {}
	
	/**{@inheritDoc}*/ @Override
	public String getName() {
		return "$BackRef?";
	}
	
	String getLastMatchByName(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
		if(pHostResult == null) return null;
		String W = pHostResult.getLastStrMatchByName(pParam);
		
		// Elevate up (in case pHostResult is a node)
		while(W == null) {
			if     (pHostResult instanceof ParseResult.Temp) pHostResult = ((ParseResult.Temp)pHostResult).First;
			else if(pHostResult instanceof ParseResult.Node) pHostResult = ((ParseResult.Node)pHostResult).Parent;
			if(pHostResult == null) return null;
			else {		
				ParseResult.Entry E = pHostResult.getLastMatchByName(pParam);
				if((E == null) && (pHostResult instanceof ParseResult.Root)) return null;
				W = pHostResult.getLastStrMatchByName(pParam);
			}
		}
		return W;
	}
	
	/**{@inheritDoc}*/ @Override
	public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
		String W = this.getLastMatchByName(pHostResult, pParam, pProvider);
		if(W == null) return WordChecker.EmptyWord;
		return CheckerAny.getCheckerAny(W.length());
	}

	/**{@inheritDoc}*/ @Override
	public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam, PTypeProvider pProvider) {
		String W = this.getLastMatchByName(pHostResult, pParam, pProvider);
		if(W == null) return false;
		return W.equals(pThisResult.getText());
	}
}