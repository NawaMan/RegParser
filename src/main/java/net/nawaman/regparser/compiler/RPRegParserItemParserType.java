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

import static net.nawaman.regparser.Checker.EMPTY_CHECKER_ARRAY;
import static net.nawaman.regparser.ParserTypeBackRef.BackRef_Instance;
import static net.nawaman.regparser.ParserTypeBackRefCaseInsensitive.BackRefCI_Instance;
import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.PredefinedCharClasses.WhiteSpace;
import static net.nawaman.regparser.PredefinedCheckers.getCharClass;
import static net.nawaman.regparser.PredefinedCheckers.predefinedCharChecker;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.OneOrMore_Minimum;
import static net.nawaman.regparser.Quantifier.Zero;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.EscapeHelpers.escapable;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.RegParserEntry.newParserEntry;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import java.util.Vector;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.PredefinedCheckers;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.types.IdentifierParserType;
import net.nawaman.regparser.types.TextCaseInsensitiveParserType;

public class RPRegParserItemParserType extends ParserType {
	
	private static final long serialVersionUID = -9022541167055232126L;
	
	public static String                    name     = "RegParserItem[]";
	public static RPRegParserItemParserType instance = new RPRegParserItemParserType();
	public static ParserTypeRef             typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	public RPRegParserItemParserType() {
		var checkers = new Vector<Checker>();
		checkers.add(predefinedCharChecker);
		// Escape
		checkers.add(newRegParser(RPEscapeParserType.typeRef));
		checkers.add(newRegParser(RPEscapeOctParserType.typeRef));
		checkers.add(newRegParser(RPEscapeHexParserType.typeRef));
		checkers.add(newRegParser(RPEscapeUnicodeParserType.typeRef));
		// CharSet
		checkers.add(newRegParser(RPTypeParserType.typeRef));
		// Type
		checkers.add(newRegParser(new ParserTypeRef.Simple(RPCharSetItemParserType.name)));
		
		var regParserTypeRef = new ParserTypeRef.Simple("RegParser");
		
		var unnamedGroup 
				= newRegParser()
				.entry(new CharSet("#$"), Zero)
				.entry(
					either(newRegParser()
						.entry("#NOT", new CharSingle('^'), ZeroOrOne)
						.entry(regParserTypeRef, OneOrMore)
						.entry(newRegParser()
							.entry("#OR", new CharSingle('|'))
							.entry(regParserTypeRef, OneOrMore),
							ZeroOrMore)
						.entry(newRegParser()
							.entry("#Default", new WordChecker("||"))
							.entry(regParserTypeRef, OneOrMore),
							ZeroOrMore)
						.entry(new CharSingle(')')))
					.orDefault(newRegParser()
						.entry("#Error[]", new CharNot(new CharSet(")")), ZeroOrMore)
						.entry(new CharSingle(')'))));
		var definition 
				= either(newRegParser()
					.entry("#Defined", new CharSingle(':'))
					.entry(WhiteSpace, ZeroOrMore)
					.entry(either(newRegParser("#Type", RPTypeParserType.typeRef))
							.or(newRegParser("#Error[]", newRegParser(
									new CharSingle('!'),
									new CharNot(new CharSet("!)")).zeroOrMore())))
							.or(newRegParser()
									.entry(new CharSingle('~'))
									.entry("#GroupRegParser", regParserTypeRef)
									.entry(new CharSingle('~')))
							.or(newRegParser("#Error[]", newRegParser(new CharNot(new CharSet(":!)~")), ZeroOrMore))))
					.entry(WhiteSpace, ZeroOrMore)
					.entry("#Second", newRegParser()
						.entry(new CharSingle(':'))
						.entry(WhiteSpace, ZeroOrMore)
						.entry(either(newRegParser("#Type", RPTypeParserType.typeRef)) // Type
								.or(newRegParser("#Error[]", newRegParser(  // Error of Type
										new CharSingle('!'),
										new CharNot(new CharSet("!)")).zeroOrMore())))
								.or(newRegParser() // Nested-RegParser
										.entry(new CharSingle('~'))
										.entry("#GroupRegParser",  regParserTypeRef)
										.entry(new CharSingle('~')))
								.or(newRegParser("#Error[]", newRegParser(new CharNot(new CharSet(":!)~")), ZeroOrMore))))
						.entry(WhiteSpace, ZeroOrMore), ZeroOrOne)
					.entry(new CharSingle(')')))
				.or(newRegParser() // BackRef
					.entry("#BackRefCI", new CharSingle('\''), ZeroOrOne)
					.entry("#BackRef",   new CharSingle(';'))
					.entry(WhiteSpace, ZeroOrMore)
					.entry(either(new CharSingle(')'))
							.or(newRegParser()
								.entry("#Error[]", new CharNot(new CharSet(")")), ZeroOrOne)
								.entry(new CharSingle(')')))))
				.orDefault(newRegParser()
					.entry("#Error[]", new CharNot(new CharSet(")")), ZeroOrMore)
					.entry(new CharSingle(')')));
		
		var namedGroup 
				= newRegParser()
				.entry("#Name", new CharSet("#$"))
				.entry(either(newRegParser()
							.entry("#Group-Name",   IdentifierParserType.typeRef)
							.entry("#Group-Option", new CharSet("*+"),     ZeroOrOne)
							.entry("#Multiple",     new WordChecker("[]"), ZeroOrOne)
							.entry(WhiteSpace.zeroOrMore())
							.entry(definition))
						.orDefault(newRegParser()
							.entry("#Error[]", new CharNot(new CharSet(")")), ZeroOrMore)
							.entry(new CharSingle(')'))));
		checkers.add(
			newRegParser(
				"#Group",
				newRegParser()
				.entry(new CharSingle('('))
				.entry(new CharSingle('*'), Zero)
				.entry(either(unnamedGroup).or(namedGroup))
			)
		);
		
		checkers.add(
			newRegParser(
				"$TextCI",
				newRegParser()
				.entry(new CharSet("'"))
				.entry(new CharNot(new CharSingle('\'')), ZeroOrMore)
				.entry(new WordChecker("'"))
			)
		);
		
		// Other char
		checkers.add(newRegParser(new CharNot(new CharSet(escapable)), OneOrMore_Minimum));
		
		// Create the checker
		checker = newRegParser(new CheckerAlternative(true, checkers.toArray(EMPTY_CHECKER_ARRAY)));
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
		
		var     entry        = thisResult.entryAt(entryIndex);
		boolean hasSubResult = entry.hasSubResult();
		
		if (!hasSubResult) { // A word
			var text = thisResult.textOf(entryIndex);
			return newParserEntry(
			        (text.length() == 0)
			        ? new CharSingle(text.charAt(0))
			        : new WordChecker(text));
		}
		
		// Go into the sub
		thisResult = entry.subResult();
		entry      = thisResult.entryAt(0);
		
		var name = entry.name();
		
		if ("#Any".equals(name))
			return newParserEntry(Any);
		
		if (PredefinedCheckers.charClassName.equals(name))
			return newParserEntry(getCharClass(thisResult, 0));
		
		var typeName = entry.typeName();
		if (RPEscapeParserType.name.equals(typeName)) {
			var chr = (Character) typeProvider
			        .type(RPEscapeParserType.name)
			        .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(new CharSingle(chr));
		}
		
		if (RPEscapeOctParserType.name.equals(typeName)) {
			var chr = (Character) typeProvider
			        .type(RPEscapeOctParserType.name)
			        .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(new CharSingle(chr));
		}
		
		if (RPEscapeHexParserType.name.equals(typeName)) {
			var chr = (Character) typeProvider
			        .type(RPEscapeHexParserType.name)
			        .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(new CharSingle(chr));
		}
		
		if (RPEscapeUnicodeParserType.name.equals(typeName)) {
			var chr = (Character) typeProvider
			        .type(RPEscapeUnicodeParserType.name)
			        .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(new CharSingle(chr));
		}
		
		if (RPCharSetItemParserType.name.equals(typeName)) {
			var checker = (Checker) typeProvider
			            .type(RPCharSetItemParserType.name)
			            .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(checker);
		}
		
		if ("$TextCI".equals(name)) {
			var text = thisResult.textOf(0);
			// Return as Word if its lower case and upper case is the same
			if (text.toUpperCase().equals(text.toLowerCase()))
				return newParserEntry(new WordChecker(text));
			
			text = text.substring(1, text.length() - 1);
			return newParserEntry(new ParserTypeRef.Simple(TextCaseInsensitiveParserType.name, text));
		}
		
		if (RPTypeParserType.name.equals(typeName)) {
			var parserTypeRef
			        = (ParserTypeRef) typeProvider
			        .type(RPTypeParserType.name)
			        .compile(thisResult, 0, null, compilationContext, typeProvider);
			return newParserEntry(parserTypeRef);
		}
		
		if ("#Group".equals(name)) {
			var nameValue = entry.subResult().lastStringOf("#Name");
			if (nameValue == null) {
				var checker
				        = (Checker) typeProvider
				        .type(RPRegParserParserType.name)
				        .compile(thisResult, 0, null, compilationContext, typeProvider);
				return newParserEntry(checker);
			}
			
			thisResult = entry.subResult();
			
			var groupName   = thisResult.lastStringOf("#Group-Name");
			var groupOption = thisResult.lastStringOf("#Group-Option");
			groupOption = (groupOption == null) ? "" : groupOption;
			
			var multiple = thisResult.lastStringOf("#Multiple");
			multiple = (multiple == null) ? "" : multiple;
			
			var backRef = thisResult.lastStringOf("#BackRef");
			if (backRef != null) {
				var backRefParam = nameValue + groupName + multiple;
				if (thisResult.lastStringOf("#BackRefCI") != null) {
					var typeRef = new ParserTypeRef.Simple(BackRefCI_Instance.name(), backRefParam);
					return newParserEntry(typeRef);
				}
				
				var typeRef = new ParserTypeRef.Simple(BackRef_Instance.name(), backRefParam);
				return newParserEntry(typeRef);
			}
			
			var secondParser = (RegParser) null;
			var secondEntry  = thisResult.lastEntryOf("#Second");
			if ((secondEntry != null) && secondEntry.hasSubResult()) {
				var secondSubResult = secondEntry.subResult();
				
				int typeIndex = secondSubResult.indexOf("#Type");
				if (typeIndex != -1) { // TypeRef with Name
					var secondTypeRef
					        = (ParserTypeRef) typeProvider
					        .type(RPTypeParserType.name)
					        .compile(secondSubResult, typeIndex, null, compilationContext, typeProvider);
					secondParser = newRegParser(secondTypeRef);
				} else {
					int parserIndex = secondSubResult.indexOf("#GroupRegParser");
					// Named Group
					var secondChecker 
					        = (Checker) typeProvider
					        .type(RPRegParserParserType.name)
					        .compile(secondSubResult, parserIndex, null, compilationContext, typeProvider);
					secondParser = newRegParser(secondChecker);
				}
			}
			
			int typeIndex = thisResult.indexOf("#Type");
			var entryName = nameValue + groupName + groupOption + multiple;
			if (typeIndex != -1) { // TypeRef with Name
				var parserTypeRef
				        = (ParserTypeRef) typeProvider
				        .type(RPTypeParserType.name)
				        .compile(thisResult, typeIndex, null, compilationContext, typeProvider);
				return newParserEntry(entryName, parserTypeRef, null, secondParser);
			}
			
			int IE = thisResult.indexOf("#GroupRegParser");
			// Named Group
			var groupChecker
			        = (Checker) typeProvider
			        .type(RPRegParserParserType.name)
			        .compile(thisResult, IE, null, compilationContext, typeProvider);
			return newParserEntry(entryName, groupChecker, null, secondParser);
		}
		return super.compile(thisResult, parameter, compilationContext, typeProvider);
	}
	
}
