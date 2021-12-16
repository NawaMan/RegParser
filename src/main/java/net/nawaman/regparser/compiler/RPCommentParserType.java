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

import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CheckerNot;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser type for RegParser comment.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RPCommentParserType extends ParserType {
	
	private static final long serialVersionUID = -3778591162776321408L;
	
	public static String              name     = "Comment";
	public static RPCommentParserType instance = new RPCommentParserType();
	public static ParserTypeRef       typeRef  = instance.typeRef();
	
	@Override
	public String name() {
		return name;
	}
	
	private final Checker checker;
	
	public RPCommentParserType() {
		checker = either(newRegParser()
		            .entry(new WordChecker("/*"))
		            .entry(new CheckerNot(new WordChecker("*/")).zeroOrMore())
		            .entry(new WordChecker("*/")))
		        .or(newRegParser()
		            .entry(new WordChecker("(*"))
		            .entry(new CheckerNot(new WordChecker("*)")).zeroOrMore())
		            .entry(new WordChecker("*)")))
		        .or(newRegParser()
		            .entry(new WordChecker("//"))
		            .entry(new CheckerNot(either(new WordChecker("\n")).or(Any.zero()).build()).zeroOrMore())
		            .entry(either(new WordChecker("\n")).or(Any.zero()).build()))
		        .build();
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return checker;
	}
}
