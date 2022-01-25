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

import static net.nawaman.regparser.Checker.EMPTY_CHECKER_ARRAY;
import static net.nawaman.regparser.PredefinedCharClasses.WhiteSpace;
import static net.nawaman.regparser.PredefinedCheckers.charClassName;
import static net.nawaman.regparser.PredefinedCheckers.getCharClass;
import static net.nawaman.regparser.PredefinedCheckers.predefinedCharChecker;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.Zero;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.checkers.CharChecker.EMPTY_CHAR_CHECKER_ARRAY;

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CharChecker;
import net.nawaman.regparser.checkers.CharIntersect;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

public class RPCharSetItemParserType extends ParserType {
	
	private static final long serialVersionUID = -5308399615865809113L;
	
	public static String                  name     = "CharSetItem";
	public static RPCharSetItemParserType instance = new RPCharSetItemParserType();
	public static ParserTypeRef           typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	public RPCharSetItemParserType() {
		var typeRef  = new ParserTypeRef.Simple(name);
		var checkers = new ArrayList<Checker>();
		checkers.add(predefinedCharChecker);
		checkers.add(
		        newRegParser()
		        .entry(typeRef)
		        .entry(newRegParser().entry("#Intersect", new WordChecker("&&")).entry(typeRef), ZeroOrMore)
		        .build());
		checkers.add(newRegParser("#Ignored[]", WhiteSpace.oneOrMore()));
		checkers.add(newRegParser("#Range",     RPRangeParserType.typeRef));
		
		// Create the checker
		checker = newRegParser()
		        .entry(
		            newRegParser()
		            .entry(new CharSingle('['))
		            .entry("#NOT", new CharSingle('^'), ZeroOrOne)
		            .entry(new CharSingle(':'), Zero)
	                      // "#Content",
		            .entry(new CheckerAlternative(true, checkers.toArray(EMPTY_CHECKER_ARRAY)), OneOrMore)
		            .entry(new CharSingle(']'))
		        )
		        .entry(
		            newRegParser()
		            .entry("#Intersect", new WordChecker("&&"))
		            .entry(
		                "#Set",
		                newRegParser()
		                .entry(new CharSingle('['))
		                .entry("#NOT", new CharSingle('^'), ZeroOrOne)
		                .entry(new CharSingle(':'), Zero)
		                       // "#Content",
		                .entry(new CheckerAlternative(checkers.toArray(EMPTY_CHECKER_ARRAY)), OneOrMore)
		                .entry(new CharSingle(']'))
		            ),
		            ZeroOrMore
		        )
		        .build();
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public Checker checker(ParseResult pHostResult, String pParam, ParserTypeProvider pProvider) {
		return checker;
	}
	
	@Override
	public Object doCompile(
					ParseResult        thisResult,
					int                entryIndex,
					String             parameter,
					CompilationContext compilationContext,
					ParserTypeProvider typeProvider) {
		thisResult = thisResult.entryAt(entryIndex).subResult();
		
		var     allCheckers  = new ArrayList<CharChecker>();
		var     eachCheckers = new ArrayList<CharChecker>();
		boolean negate       = false;
		int     entryCount   = thisResult.rawEntryCount();
		for (int i = 0; i < entryCount; i++) {
			var entry = thisResult.entryAt(i);
			var name  = entry.name();
			var text  = thisResult.textOf(i);
			var type  = entry.typeName();
			if ((name == null)
			 && (text.equals("[")))
				continue;
			
			if ("#NOT".equals(name)) { // Process not
				negate = true;
				
			} else if (charClassName.equals(name) || "#Any".equals(name)) { // Extract CharClass
				eachCheckers.add(getCharClass(thisResult, i));
				
			} else if ("#Range".equals(name)) { // Extract Range
				var charChecker
				        = (CharChecker)typeProvider.type(RPRangeParserType.name)
				                                   .compile(thisResult, i, null, compilationContext, typeProvider);
				
				if ((charChecker instanceof CharSingle) && (eachCheckers.size() > 0)) {
					var eachCharChecker = eachCheckers.get(eachCheckers.size() - 1);
					
					// Append the previous one if able
					if (eachCharChecker instanceof CharSingle) {
						charChecker = new CharSet("" + ((CharSingle) eachCharChecker).ch + ((CharSingle) charChecker).ch);
						eachCheckers.remove(eachCheckers.size() - 1);
						
					} else if (eachCharChecker instanceof CharSet) {
						charChecker = new CharSet("" + ((CharSet) eachCharChecker).set + ((CharSingle) charChecker).ch);
						eachCheckers.remove(eachCheckers.size() - 1);
					}
				}
				eachCheckers.add(charChecker);
				
			} else if ("#Set".equals(name) || RPCharSetItemParserType.name.equals(type)) { // Extract Nested
				eachCheckers.add((CharChecker) this.compile(thisResult, i, null, compilationContext, typeProvider));
				
			}
			
			// Ending and intersect
			if ("#Intersect".equals(name)) {
				var newCharChecker
				        = (eachCheckers.size() == 1)
				        ? eachCheckers.get(0)
				        : new CharUnion(eachCheckers.toArray(EMPTY_CHAR_CHECKER_ARRAY));
				
				if (negate) {
					newCharChecker = new CharNot(newCharChecker);
				}
				
				allCheckers.add(newCharChecker);
				eachCheckers = new ArrayList<CharChecker>();
				negate = false;
			}
		}
		
		if (eachCheckers.size() > 0) {
			var newCharChecker
			        = (eachCheckers.size() == 1)
			        ? eachCheckers.get(0)
			        : new CharUnion(eachCheckers.toArray(EMPTY_CHAR_CHECKER_ARRAY));
			if (negate) {
				newCharChecker = new CharNot(newCharChecker);
			}
			allCheckers.add(newCharChecker);
		}
		
		if (allCheckers.size() == 1)
			return allCheckers.get(0);
		
		return new CharIntersect(allCheckers.toArray(EMPTY_CHAR_CHECKER_ARRAY));
	}
	
}
