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
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.types;

import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser for detecting String literal "'`
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class StringLiteralParserType extends ParserType {
	
	private static final long serialVersionUID = -5671933716521874182L;

	public static String                  name     = "$StringLiteral";
	public static StringLiteralParserType instance = new StringLiteralParserType();
	public static ParserTypeRef           typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	public StringLiteralParserType() {
		checker
		    = newRegParser(
		        "#String",
		        new CheckerAlternative(
		            newRegParser(
		                new CharSingle('\"'),
		                newRegParser(
		                    new CheckerAlternative(
		                        new CharNot(new CharSingle('\'')),
		                        new WordChecker("\\\""))
		                ), Quantifier.ZeroOrMore,
		                new CharSingle('\"')),
		            newRegParser(
		                new CharSingle('\''),
		                newRegParser(
		                    new CheckerAlternative(
		                        new CharNot(new CharSingle('\'')),
		                        new WordChecker("\\\'"))
		                ), Quantifier.ZeroOrMore,
		                new CharSingle('\'')),
		            newRegParser(
		                new CharSingle('`'),
		                newRegParser(
		                    new CheckerAlternative(
		                        new CharNot(new CharSingle('`')),
		                        new WordChecker("\\`"))
		                ), Quantifier.ZeroOrMore,
		                new CharSingle('`'))));
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return checker;
	}
	
}