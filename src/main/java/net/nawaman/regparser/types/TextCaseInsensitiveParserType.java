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

package net.nawaman.regparser.types;

import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.Quantifier.bound;
import static net.nawaman.regparser.RegParser.newRegParser;

// Usage: !text("Text")! will match everything that is equals to "Text" case insensitively
import java.util.Hashtable;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser Type for Case-Insensitive Text
 *  
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class TextCaseInsensitiveParserType extends ParserType {
	
	private static Hashtable<Integer, Checker> checkers = new Hashtable<Integer, Checker>();
	
	public static String                        name     = "textCI";
	public static TextCaseInsensitiveParserType instance = new TextCaseInsensitiveParserType();
	public static ParserTypeRef                 typeRef  = instance.typeRef();
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		if (parameter == null) {
			parameter = "";
		}
		int length  = parameter.length();
		return checkers.computeIfAbsent(length, __ ->{
			return newRegParser()
					.entry(Any, bound(length))
					.build();
		});
	}
	
	@Override
	public boolean doValidate(
					ParseResult        hostResult,
					ParseResult        thisResult,
					String             parameter,
					ParserTypeProvider typeProvider) {
		var text = thisResult.text();
		if (text == parameter)
			return true;
		
		if ((text      == null)
		 || (parameter == null))
			return false;
		
		return text.toLowerCase().equals(parameter.toLowerCase());
	}
}
