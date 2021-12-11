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
package net.nawaman.regparser.compiler;

import static java.lang.String.format;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;
import static net.nawaman.regparser.utils.Util.unescapeText;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.types.IdentifierParserType;
import net.nawaman.regparser.types.StringLiteralParserType;

public class RPTypeParserType extends ParserType {
	
	private static final long serialVersionUID = 534187749649740618L;
	
	public static String           name     = "Type";
	public static RPTypeParserType instance = new RPTypeParserType();
	public static ParserTypeRef    typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	@Override
	public String name() {
		return name;
	}
	
	public RPTypeParserType() {
		checker = newRegParser()
		        .entry(new CharSingle('!'))
		        .entry(
		            either(
		                newRegParser()
		                .entry("#AsText",     new CharSingle('$'),          ZeroOrOne)
		                .entry("#TypeName",   IdentifierParserType.typeRef)
		                .entry("#TypeOption", new CharSet("*+"),            ZeroOrOne)
		                .entry("#Validate",   new CharSet("~?"),            ZeroOrOne)
		                .entry("#Collective", new WordChecker("[]"),        ZeroOrOne)
		                .entry(
		                    "#Param",
		                    newRegParser()
		                    .entry(new CharSingle('('))
		                    .entry("#ParamValue", StringLiteralParserType.typeRef, ZeroOrOne)
		                    .entry(new CharSingle(')')),
		                    ZeroOrOne)
		                .entry(new CharSingle('!')))
		            .orDefault(
		                newRegParser()
		                .entry("#Error[]", new CharNot(new CharSingle('!')).zeroOrMore())
		                .entry(new CharSingle('!'))
		            )
		        )
		        .build();
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return this.checker;
	}
	
	@Override
	public Object doCompile(
					ParseResult        thisResult,
					int                entryIndex,
					String             parameter,
					CompilationContext compilationContext,
					ParserTypeProvider typeProvider) {
		thisResult = thisResult.entryAt(entryIndex).subResult();
		
		var typeName = thisResult.lastStringOf("#TypeName");
		if (typeName == null) {
			int position = thisResult.startPosition();
			var nearBy   = thisResult.originalText().substring(position);
			var errMsg   = format("Mal-formed RegParser Type near \"%s\".", nearBy) ;
			throw new CompilationException(errMsg);
		}
		
		var type       = thisResult.lastStringOf("#AsText");
		var option     = thisResult.lastStringOf("#TypeOption");
		var validate   = thisResult.lastStringOf("#Validate");
		var collective = thisResult.lastStringOf("#Collective");
		
		typeName = ((type == null) ? "" : type)
		         + typeName
		         + ((option     == null) ? "" : option)
		         + ((validate   == null) ? "" : validate)
		         + ((collective == null) ? "" : collective);
		
		var param      = (String)null;
		var paramEntry = thisResult.lastEntryOf("#Param");
		if ((paramEntry != null) && paramEntry.hasSubResult()) {
			param = paramEntry.subResult().lastStringOf("#ParamValue");
			if (param != null) {
				var text = param.substring(1, param.length() - 1);
				param    = unescapeText(text).toString();
			}
			
		}
		return new ParserTypeRef.Simple(typeName, param);
	}
	
}
