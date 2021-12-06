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

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.Greediness.MaximumSign;
import static net.nawaman.regparser.Greediness.Minimum;
import static net.nawaman.regparser.Greediness.MinimumSign;
import static net.nawaman.regparser.Greediness.Possessive;
import static net.nawaman.regparser.PredefinedCharClasses.Blank;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.Quantifier.One;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.OneOrMore_Minimum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Maximum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Minimum;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.Quantifier.ZeroOrOne_Maximum;
import static net.nawaman.regparser.Quantifier.ZeroOrOne_Minimum;
import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.result.ParseResult;

public class RPQuantifierParserType extends ParserType {
	
	private static final long serialVersionUID = -5308399615865809113L;
	
	public static String                 name     = "Quantifier";
	public static RPQuantifierParserType instance = new RPQuantifierParserType();
	public static ParserTypeRef          typeRef  = instance.typeRef();
	
	private final Checker checker;
	
	@Override
	public String name() {
		return name;
	}
	
	public RPQuantifierParserType() {
		// ((?|*|+|{\s[\d]*\s}|{\s[\d]*\s,\s}|{\s,\s[\d]*\s}|{\s[\d]*\s,\s[\d]*\s})(*|+)?)?
		checker = newRegParser(
		            "#Quantifier", new CheckerAlternative(true,
		                               new CharSet("+*?"),
		                               newRegParser(
		                                   new CharSingle('{'), Blank, ZeroOrMore,
		                                   newRegParser("#BothBound", newRegParser(Digit, OneOrMore)),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle('}')),
		                               newRegParser(
		                                   new CharSingle('{'), Blank, ZeroOrMore,
		                                   new CharSingle(','), Blank, ZeroOrMore,
		                                   newRegParser("#UpperBound", newRegParser(Digit, OneOrMore)),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle('}')),
		                               newRegParser(
		                                   new CharSingle('{'), One,
		                                   Blank, ZeroOrMore,
		                                   newRegParser("#LowerBound", newRegParser(Digit, OneOrMore)),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle(','),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle('}')),
		                               newRegParser(
		                                   new CharSingle('{'),
		                                   Blank, ZeroOrMore,
		                                   newRegParser("#LowerBound", newRegParser(Digit, OneOrMore)),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle(','),
		                                   Blank, ZeroOrMore,
		                                   newRegParser("#UpperBound", newRegParser(Digit, OneOrMore)),
		                                   Blank, ZeroOrMore,
		                                   new CharSingle('}')),
		                               newRegParser(
		                                   new CharSingle('{'),
		                                   newRegParser("#Error[]", newRegParser(new CharNot(new CharSingle('}')), ZeroOrMore)),
		                                   new CharSingle('}'))
		                               ),
		            "#Greediness", new CharSet("+*"), ZeroOrOne);
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
		
		int position = thisResult.startPosition();
		var nearBy   = thisResult.originalText().substring(position);
		if (!name.equals(thisResult.typeNameOf(entryIndex))) {
			var errMsg = format("Mal-formed RegParser quatifier near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		
		thisResult = thisResult.entryAt(entryIndex).subResult();
		
		var quantifier = thisResult.lastStringOf("#Quantifier");
		var greediness = thisResult.lastStringOf("#Greediness");
		
		switch (quantifier.charAt(0)) {
		case '?': {
			if (greediness == null)
				return ZeroOrOne;
			
			if (greediness.charAt(0) == MaximumSign.charAt(0))
				return ZeroOrOne_Maximum;
			
			if (greediness.charAt(0) == MinimumSign.charAt(0))
				return ZeroOrOne_Minimum;
			
			break;
		}
		case '*': {
			if (greediness == null)
				return ZeroOrMore;
			
			if (greediness.charAt(0) == MaximumSign.charAt(0))
				return ZeroOrMore_Maximum;
			
			if (greediness.charAt(0) == MinimumSign.charAt(0))
				return ZeroOrMore_Minimum;
			
			break;
		}
		case '+': {
			if (greediness == null)
				return Quantifier.OneOrMore;
			
			if (greediness.charAt(0) == MaximumSign.charAt(0))
				return Quantifier.OneOrMore_Maximum;
			
			if (greediness.charAt(0) == MinimumSign.charAt(0))
				return Quantifier.OneOrMore_Minimum;
			
			break;
		}
		case '{': {
			thisResult = thisResult.entryAt(0).subResult();
			
			var error = thisResult.lastStringOf("#Error[]");
			if (error != null)
				break;
			
			var bothBound = thisResult.lastStringOf("#BothBound");
			int bound  = -1;
			if (bothBound == null) {
				var upperBoundString = thisResult.lastStringOf("#UpperBound");
				var lowerBoundString = thisResult.lastStringOf("#LowerBound");
				int upperBound       = (upperBoundString == null) ? -1 : parseInt(upperBoundString);
				int lowerBound       = (lowerBoundString == null) ?  0 : parseInt(lowerBoundString);
				if ((upperBound != -1) && (upperBound < lowerBound)) {
					var errMsg = format("\"Upper bound must not be lower than its lower bound near \"%s\".", nearBy);
					throw new CompilationException(errMsg);
				}
				
				if (upperBound != lowerBound) {
					if ((lowerBound == 0) && (lowerBound == 1)) {
						if (greediness == null)
							return ZeroOrOne;
						
						if (greediness.charAt(0) == MaximumSign.charAt(0))
							return ZeroOrOne_Maximum;
						
						if (greediness.charAt(0) == MinimumSign.charAt(0))
							return Quantifier.ZeroOrOne_Minimum;
						
						break;
					}
					if ((lowerBound == 0) && (upperBound == -1)) {
						if (greediness == null)
							return ZeroOrMore;
						
						if (greediness.charAt(0) == MaximumSign.charAt(0))
							return ZeroOrMore_Maximum;
						
						if (greediness.charAt(0) == MinimumSign.charAt(0))
							return ZeroOrMore_Minimum;
						
						break;
					}
					if ((lowerBound == 1) && (upperBound == -1)) {
						if (greediness == null)
							return OneOrMore;
						
						if (greediness.charAt(0) == MaximumSign.charAt(0))
							return Quantifier.OneOrMore_Maximum;
						
						if (greediness.charAt(0) == MinimumSign.charAt(0))
							return OneOrMore_Minimum;
						
						break;
					}
					
					if (greediness == null)
						return new Quantifier(lowerBound, upperBound);
					
					if (greediness.charAt(0) == MaximumSign.charAt(0))
						return new Quantifier(lowerBound, upperBound, Maximum);
					
					if (greediness.charAt(0) == MinimumSign.charAt(0))
						return new Quantifier(lowerBound, upperBound, Minimum);
					
					break;
				}
				bound = lowerBound;
			} else {
				bound = parseInt(bothBound);
			}
			
			if (greediness == null) {
				if (bound == 0)
					return Quantifier.Zero;
			}
			if (bound == 1)
				return Quantifier.One;
			
			if (greediness == null)
				return new Quantifier(bound, bound, Possessive);
			
			if (greediness.charAt(0) == MaximumSign.charAt(0))
				return new Quantifier(bound, bound, Maximum);
			
			if (greediness.charAt(0) == MinimumSign.charAt(0))
				return new Quantifier(bound, bound, Minimum);
			
			break;
		}
		}
		
		var errMsg = format("Mal-formed RegParser Type near \"%s\".", nearBy);
		throw new CompilationException(errMsg);
	}
	
}