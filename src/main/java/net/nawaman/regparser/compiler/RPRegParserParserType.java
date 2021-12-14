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
import static net.nawaman.regparser.Checker.EMPTY_CHECKER_ARRAY;
import static net.nawaman.regparser.PredefinedCharClasses.WhiteSpace;
import static net.nawaman.regparser.Quantifier.One;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.RegParserEntry.EmptyRegParserEntryArray;
import static net.nawaman.regparser.RegParserEntry.newParserEntry;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerNot;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

public class RPRegParserParserType extends ParserType {
	
	private static final long serialVersionUID = -7826702993650008404L;
	
	public static String                name     = "RegParser";
	public static RPRegParserParserType instance = new RPRegParserParserType();
	public static ParserTypeRef         typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	public RPRegParserParserType() {
		checker =
		        either(newRegParser(
		            "#ItemQuantifier",
		            newRegParser()
		            .entry(RPRegParserItemParserType.typeRef)
		            .entry("#Ignored[]", WhiteSpace, ZeroOrMore)
		            .entry(RPQuantifierParserType.typeRef)))
		        .or(newRegParser("#Comment", RPCommentParserType.typeRef))
		        .orDefault(
		            either    (newRegParser("#Item[]",    RPRegParserItemParserType.typeRef))
		            .orDefault(newRegParser("#Ignored[]", new CharSet(" \t\n\r\f"))))
		        .oneOrMore()
		        .asChecker();
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
		
		var entry = thisResult.entryAt(entryIndex);
		if ((entry == null) || (entry.subResult() == null)) {
			var nearBy = thisResult.originalText().substring(thisResult.startPositionOf(0));
			var errMsg = String.format("Mal-formed RegParser Type near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		thisResult = entry.subResult();
		
		var     parsers    = new ArrayList<RegParser>();
		var     entries    = new ArrayList<RegParserEntry>();
		boolean isNot      = false;
		boolean isOr       = false;
		boolean isDefault  = false;
		int     entryCount = thisResult.rawEntryCount();
		for (int i = 0; i < entryCount; i++) {
			var resultEntry = thisResult.entryAt(i);
			var entryName   = resultEntry.name();
			
			if ("#Ignored[]".equals(entryName))
				continue;
			
			if ("#Comment".equals(entryName))
				continue;
			
			if ("#Error[]".equals(entryName)) {
				var nearBy = thisResult.originalText().substring(thisResult.startPositionOf(i));
				var errMsg = format("Mal-formed RegParser Type near \"%s\".", nearBy);
				throw new CompilationException(errMsg);
			}
			
			boolean hasSubResult = resultEntry.hasSubResult();
			
			if (!hasSubResult) {
				var entryText = thisResult.textOf(i);
				if ((entryName == null) && (entryText.equals("(")))
					continue;
				
				if ("#NOT".equals(entryName)) { // Process not
					isNot = true;
					
				} else if ("#Default".equals(entryName)) { // Process Default
					if (!isDefault) {
						isOr      = true;
						isDefault = true;
					} else {
						if (entries.size() > 0) {
							var newParser = newRegParser((RegParserEntry[]) entries.toArray(EmptyRegParserEntryArray));
							parsers.add(newParser);
							
							entries.clear();
						}
						var alternative = new CheckerAlternative(true, parsers.toArray(RegParser[]::new));
						parsers.clear();
						parsers.add(newRegParser(alternative));
					}
					
				} else if ("#OR".equals(entryName)) { // Process OR
					isOr = true;
					
				} else if (!entryText.equals(")")) { // A Word
					var word = thisResult.textOf(i);
					entries.add(newParserEntry(new WordChecker(word)));
					
				}
			} else {
				var parserEntry = prepareParserEntry(thisResult, resultEntry, i, entryName, compilationContext, typeProvider);
				
				if (parserEntry != null) {
					entries.add(parserEntry);
				}
			}
			
			// Ending
			if (isOr) {
				var newParser = newRegParser((RegParserEntry[]) entries.toArray(EmptyRegParserEntryArray));
				parsers.add(newRegParser(newParser));
				
				entries  = new ArrayList<RegParserEntry>();
				isOr = false;
			}
		}
		
		// Ending
		if (entries.size() > 0) {
			var newParser = newRegParser((RegParserEntry[]) entries.toArray(EmptyRegParserEntryArray));
			parsers.add(newParser);
		}
		
		if (parsers.size() == 1) {
			var parser = parsers.get(0);
			return isNot ? new CheckerNot(parser) : parser;
		}
		
		var newParsers = parsers.toArray(EMPTY_CHECKER_ARRAY);
		var checker    = new CheckerAlternative(isDefault, newParsers);
		
		return isNot ? new CheckerNot(checker) : checker;
	}
	
	private RegParserEntry prepareParserEntry(
							ParseResult        thisResult,
							ParseResultEntry   resultEntry,
							int                entryIndex,
							String             entryName,
							CompilationContext compilationContext,
							ParserTypeProvider typeProvider) {
		if (RPRegParserParserType.name.equals(resultEntry.typeName())) {
			var checker = (Checker) this.compile(thisResult, entryIndex, null, compilationContext, typeProvider);
			var parserEntry = newParserEntry(checker);
			
			if (parserEntry.checker() instanceof RegParser) {
				var parserEntries = ((RegParser) (parserEntry.checker())).entries();
				var twoEntries    = parserEntries.limit(2).toArray(RegParserEntry[]::new);
				
				if (twoEntries.length == 0) {
					parserEntry = null;
				} else if (twoEntries.length == 1) {
					parserEntry = twoEntries[0];
				}
			}
			return parserEntry;
			
		} else if ("#Item[]".equals(entryName)) { // Process items
			return (RegParserEntry) typeProvider
			        .type(RPRegParserItemParserType.name)
			        .compile(thisResult, entryIndex, null, compilationContext, typeProvider);
			
		} else {
			var subResult = resultEntry.subResult();
			var parserEntry
			        = (RegParserEntry) typeProvider
			        .type(RPRegParserItemParserType.name)
			        .compile(subResult, 0, null, compilationContext, typeProvider);
			
			return applyQuantifier(parserEntry, resultEntry, compilationContext, typeProvider);
			
		}
	}
	
	private RegParserEntry applyQuantifier(
							RegParserEntry     parserEntry,
							ParseResultEntry   resultEntry,
							CompilationContext compilationContext,
							ParserTypeProvider typeProvider) {
		var subResult = resultEntry.subResult();
		
		var quantifier = One;
		if (subResult.rawEntryCount() == 2) {
			quantifier
			        = (Quantifier) typeProvider
			        .type(RPQuantifierParserType.name)
			        .compile(subResult, 1, null, compilationContext, typeProvider);
		}
		if (quantifier != One) {
			var name        = parserEntry.name();
			var checker     = parserEntry.checker();
			var typeRef     = parserEntry.typeRef();
			var type        = parserEntry.type();
			var secondStage = parserEntry.secondStage();
			if (parserEntry.checker() != null) {
				parserEntry = newParserEntry(name, checker, quantifier, secondStage);
				
			} else if (parserEntry.typeRef() != null) {
				parserEntry = newParserEntry(name, typeRef, quantifier, secondStage);
				
			} else if (parserEntry.type() != null) {
				parserEntry = newParserEntry(name, type, quantifier, secondStage);
			}
		}
		return parserEntry;
	}
}
