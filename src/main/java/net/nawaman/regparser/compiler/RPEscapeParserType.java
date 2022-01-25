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
package net.nawaman.regparser.compiler;

import static java.lang.String.format;
import static net.nawaman.regparser.EscapeHelpers.escapable;
import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser type for RegParser escapable.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RPEscapeParserType extends ParserType {
	
	private static final long serialVersionUID = -4408409684157875525L;
	
	public static String             name     = "Escape";
	public static RPEscapeParserType instance = new RPEscapeParserType();
	public static ParserTypeRef      typeRef  = instance.typeRef();
	public static RegParser          parser   = instance.typeRef().asRegParser();
	
	private final Checker checker;
	
	public RPEscapeParserType() {
		checker = newRegParser()
		        .entry(new CharSingle('\\'))
		        .entry(new CharSet(escapable))
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
	public Object doCompile(
					ParseResult        thisResult,
					int                entryIndex,
					String             parameter,
					CompilationContext compilationContext,
					ParserTypeProvider typeProvider) {
		var typeName = thisResult.typeNameOf(entryIndex);
		if (!name.equals(typeName)) {
			var nearBy = thisResult.originalText().substring(thisResult.startPosition());
			var errMsg = format("Mal-formed RegParser Escape near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		
		return thisResult.textOf(entryIndex).charAt(1);
	}
}
