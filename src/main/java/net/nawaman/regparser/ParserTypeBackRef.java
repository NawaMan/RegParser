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

import net.nawaman.regparser.checkers.CheckerAny;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.RootParseResult;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.ParseResult;

/**
 * RegParser Type for Back referencing.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class ParserTypeBackRef extends ParserType {
	
	private static final long serialVersionUID = 4619586708646758390L;
	
	public static final ParserTypeBackRef BackRef_Instance = new ParserTypeBackRef();
	
	ParserTypeBackRef() {
	}
	
	/**{@inheritDoc}*/
	@Override
	public String name() {
		return "$BackRef?";
	}
	
	/**{@inheritDoc}*/
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		var word = lastMatchByName(hostResult, parameter, typeProvider);
		return (word == null)
		        ? WordChecker.EmptyWord
		        : CheckerAny.getCheckerAny(word.length());
	}
	
	/**{@inheritDoc}*/
	@Override
	public boolean doValidate(
					ParseResult   hostResult,
					ParseResult   thisResult,
					String        parameter,
					ParserTypeProvider typeProvider) {
		var word = lastMatchByName(hostResult, parameter, typeProvider);
		if (word == null)
			return false;
		
		return word.equals(thisResult.text());
	}
	
	private String lastMatchByName(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		if (hostResult == null)
			return null;
		
		var word = hostResult.lastStringOf(parameter);
		
		// Elevate up (in case pHostResult is a node)
		while (word == null) {
			if (hostResult instanceof TemporaryParseResult) {
				hostResult = ((TemporaryParseResult)hostResult).first();
			} else if (hostResult instanceof ParseResultNode) {
				hostResult = ((ParseResultNode)hostResult).parent();
			}
			
			if (hostResult == null)
				return null;
			
			var entry = hostResult.lastEntryOf(parameter);
			if ((entry == null)
			 && (hostResult instanceof RootParseResult))
				return null;
			
			word = hostResult.lastStringOf(parameter);
		}
		return word;
	}
	
}
