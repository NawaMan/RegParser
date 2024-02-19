/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2024 Nawapunth Manusitthipol.
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
package net.nawaman.regparser.compiler;

import static java.lang.String.format;
import static net.nawaman.regparser.Checker.EMPTY_CHECKER_ARRAY;
import static net.nawaman.regparser.EscapeHelpers.escapable;
import static net.nawaman.regparser.PredefinedCharClasses.WhiteSpace;
import static net.nawaman.regparser.PredefinedCheckers.predefinedCharChecker;
import static net.nawaman.regparser.RegParser.newRegParser;

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.result.ParseResult;

public class RPRangeParserType extends ParserType {
	
	private static final long serialVersionUID = -5308399615865809113L;
	
	public static String            name     = "Range";
	public static RPRangeParserType instance = new RPRangeParserType();
	public static ParserTypeRef     typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	RPRangeParserType() {
		var checkers = new ArrayList<Checker>();
		checkers.add(newRegParser(new CharNot(new CharSet(escapable + "-"))));
		checkers.add(RPEscapeParserType.parser);
		checkers.add(RPEscapeOctParserType.parser);
		checkers.add(RPEscapeHexParserType.parser);
		checkers.add(RPEscapeUnicodeParserType.parser);
		
		// Last
		checkers.add(newRegParser("#Error[]", new CharNot(new CharSingle(']'))));
		
		// Create the checker
		checker = newRegParser()
		        .entry(predefinedCharChecker.zero())
		        .entry("#Start", new CheckerAlternative(true, checkers.toArray(EMPTY_CHECKER_ARRAY)))
		        .entry(WhiteSpace.zeroOrMore())
		        .entry(newRegParser()
		            .entry(new CharSingle('-'))
		            .entry(predefinedCharChecker.zero())
		            .entry(WhiteSpace.zeroOrMore())
		            .entry("#End", new CheckerAlternative(true, checkers.toArray(EMPTY_CHECKER_ARRAY)))
		            .zeroOrOne())
		        .build();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return checker;
	}
	
	@Override
	public final Boolean isDeterministic() {
		return true;
	}
	
	@Override
	public Object doCompile(
					ParseResult        thisResult,
					int                entryIndex,
					String             parameter,
					CompilationContext compilationContext,
					ParserTypeProvider typeProvider) {
		
		// Ensure type
		var nearBy = thisResult.originalText().substring(thisResult.startPosition());
		if (!name.equals(thisResult.typeNameOf(entryIndex))) {
			var errMsg = format("Mal-formed RegParser character range near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		
		thisResult = thisResult.entryAt(entryIndex).subResult();
		
		var startEntry = thisResult.lastEntryOf("#Start");
		if (startEntry.hasSubResult()) {
			if (startEntry.subResult().hasName("#Error[]")) {
				var errMsg = format("There is an invalid character near \"%s\".", nearBy);
				throw new CompilationException(errMsg);
			}
		}
		
		var  start     = thisResult.lastStringOf("#Start");
		char startChar = start.charAt(0);
		if (start.length() > 1) {
			// Only possibility is that it is an escape
			var parseResult = startEntry.subResult();
			var escapeType  = typeProvider.type(RPEscapeParserType.name);
			startChar = (Character)(escapeType.compile(parseResult, typeProvider));
		}
		
		var end = thisResult.lastStringOf("#End");
		if (end == null)
			return new CharSingle(startChar);
		
		var endEntry = thisResult.lastEntryOf("#End");
		if (endEntry.hasSubResult()) {
			if (endEntry.subResult().hasName("#Error[]")) {
				var errMsg     = format("There is an invalid character near \"%s\".", nearBy);
				throw new CompilationException(errMsg);
			}
		}
		
		char endChar = end.charAt(0);
		if (end.length() > 1) {
			// Only possibility is that it is an escape
			var parseResult = endEntry.subResult();
			var escapeType  = typeProvider.type(RPEscapeParserType.name);
			endChar = (Character)(escapeType.compile(parseResult, typeProvider));
		}
		if (startChar > endChar) {
			var errMsg = format("Range starter must not be greater than its ender - near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		
		return new CharRange(startChar, endChar);
	}
	
}
