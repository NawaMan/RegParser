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
package net.nawaman.regparser;

import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.RegParser.newRegParser;

import java.util.Hashtable;

import net.nawaman.regparser.checkers.CharChecker;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFirstFound;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

public class PredefinedCheckers {
	
	static public final String charClassName = "#CharClass";
	
	// NOTE - This is not very performance effective, but I have no time to refactor now
	// TODOLATER - Reorder this (to take advantage of CheckerFirstFound)
	static public final CheckerAlternative predefinedCharChecker 
	    = new CheckerAlternative(
	        newRegParser("#Any", new CharSingle('.')),
	        newRegParser(
	            charClassName,
	            newRegParser(
	                new CharSingle('\\'),
	                new CheckerFirstFound( // ~\[dDdDwWoOxX]~
	                    newRegParser("$Digit",               new CharSingle('d')),
	                    newRegParser("$NonDigit",            new CharSingle('D')),
	                    newRegParser("$WhiteSpace",          new CharSingle('s')),
	                    newRegParser("$NonWhiteSpace",       new CharSingle('S')),
	                    newRegParser("$Blank",               new CharSingle('b')),
	                    newRegParser("$NonBlank",            new CharSingle('B')),
	                    newRegParser("$Word",                new CharSingle('w')),
	                    newRegParser("$NonWord",             new CharSingle('W')),
	                    newRegParser("$OctalDigit",          new CharSingle('o')),
	                    newRegParser("$NonOctalDigit",       new CharSingle('O')),
	                    newRegParser("$HexadecimalDigit",    new CharSingle('x')),
	                    newRegParser("$NonHexadecimalDigit", new CharSingle('X')),
	                    newRegParser(
	                        new WordChecker("p{"), // \p{Lower}
	                        new CheckerFirstFound(
	                            newRegParser("$LowerCaseAlphabet", new WordChecker("Lower")),
	                            newRegParser("$UpperCaseAlphabet", new WordChecker("Upper")),
	                            newRegParser("$ASCII",             new WordChecker("ASCII")),
	                            newRegParser("$AlphabetAndDigit",  new WordChecker("Alnum")), // A&D first
	                            newRegParser("$Alphabet",          new WordChecker("Alpha")),
	                            newRegParser("$Punctuation",       new WordChecker("Punct")),
	                            newRegParser("$Visible",           new WordChecker("Graph")),
	                            newRegParser("$Printable",         new WordChecker("Print")),
	                            newRegParser("$Blank",             new WordChecker("Blank")),
	                            newRegParser("$OctalDigit",        new WordChecker("ODigit")),
	                            newRegParser("$HexadecimalDigit",  new WordChecker("XDigit"))
	                        ),
	                        new CharSingle('}')
	                    ),
	                    newRegParser(
	                        new CharSingle('j'),
	                        new CheckerFirstFound(
	                            newRegParser("$JDigit",         new CharSingle('d')),
	                            newRegParser("$JNonDigit",      new CharSingle('D')),
	                            newRegParser("$JWhiteSpace",    new CharSingle('s')),
	                            newRegParser("$JNonWhiteSpace", new CharSingle('S')),
	                            newRegParser("$JWord",          new CharSingle('w')),
	                            newRegParser("$JNonWord",       new CharSingle('W')),
	                            newRegParser(
	                                new WordChecker("p{"), // \jp{}
	                                new CheckerFirstFound(
	                                    newRegParser("$JLowerCaseAlphabet", new WordChecker("Lower")),
	                                    newRegParser("$JUpperCaseAlphabet", new WordChecker("Upper")),
	                                    newRegParser("$JASCII",             new WordChecker("ASCII")),
	                                    newRegParser("$JAlphabetAndDigit",  new WordChecker("Alnum")), // A&D
	                                                                                                  // First
	                                    newRegParser("$JAlphabet",         new WordChecker("Alpha")),
	                                    newRegParser("$JPunctuation",      new WordChecker("Punct")),
	                                    newRegParser("$JVisible",          new WordChecker("Graph")),
	                                    newRegParser("$JPrintable",        new WordChecker("Print")),
	                                    newRegParser("$JBlank",            new WordChecker("Blank")),
	                                    newRegParser("$JControlCharacter", new WordChecker("Cntrl")),
	                                    newRegParser("$JHexadecimalDigit", new WordChecker("XDigit")),
	                                    newRegParser("$JGreek",            new WordChecker("InGreek")),
	                                    newRegParser("$JCurrencySimbol",   new WordChecker("Sc"))
	                                ),
	                                new CharSingle('}')
	                            )
	                        )
	                    )
	                )
	            )
	        ),
	        newRegParser(
	            charClassName, newRegParser(
	                new WordChecker("[:"),
	                new CheckerFirstFound(
	                    // Single Char for better escape
	                    newRegParser("$Tilde",    new WordChecker("Tilde")),
	                    newRegParser("$Tilde",    new CharSingle('~')),
	                    newRegParser("$Stress",   new WordChecker("Stress")),
	                    newRegParser("$Stress",   new CharSingle('`')),
	                    newRegParser("$XMark",    new WordChecker("XMark")),
	                    newRegParser("$XMark",    new CharSingle('!')),
	                    newRegParser("$AtSign",   new WordChecker("AtSign")),
	                    newRegParser("$AtSign",   new CharSingle('@')),
	                    newRegParser("$Hash",     new WordChecker("Hash")),
	                    newRegParser("$Hash",     new CharSingle('#')),
	                    newRegParser("$Dollar",   new WordChecker("Dollar")),
	                    newRegParser("$Dollar",   new CharSingle('$')),
	                    newRegParser("$Percent",  new WordChecker("Percent")),
	                    newRegParser("$Percent",  new CharSingle('%')),
	                    newRegParser("$Caret",    new WordChecker("Caret")),
	                    newRegParser("$Caret",    new CharSingle('^')),
	                    newRegParser("$AndSign",  new WordChecker("AndSign")),
	                    newRegParser("$AndSign",  new CharSingle('&')),
	                    newRegParser("$Asterisk", new WordChecker("Asterisk")),
	                    newRegParser("$Asterisk", new CharSingle('*')),
	                    newRegParser("$ORound",   new WordChecker("ORound")),
	                    newRegParser("$ORound",   new CharSingle('(')),
	                    newRegParser("$CRound",   new WordChecker("CRound")),
	                    newRegParser("$CRound",   new CharSingle(')')),
	                    newRegParser("$OSquare",  new WordChecker("OSquare")),
	                    newRegParser("$OSquare",  new CharSingle('[')),
	                    newRegParser("$CSquare",  new WordChecker("CSquare")),
	                    newRegParser("$CSquare",  new CharSingle(']')),
	                    newRegParser("$OCurl",    new WordChecker("OCurl")),
	                    newRegParser("$OCurl",    new CharSingle('{')),
	                    newRegParser("$CCurl",    new WordChecker("CCurl")),
	                    newRegParser("$CCurl",    new CharSingle('}')),
	                    newRegParser("$OAngle",   new WordChecker("OAngle")),
	                    newRegParser("$OAngle",   new CharSingle('<')),
	                    newRegParser("$CAngle",   new WordChecker("CAngle")),
	                    newRegParser("$CAngle",   new CharSingle('>')),
	                    newRegParser("$UScore",   new WordChecker("UScore")),
	                    newRegParser("$UScore",   new CharSingle('_')),
	                    newRegParser("$Minus",    new WordChecker("Minus")),
	                    newRegParser("$Minus",    new CharSingle('-')),
	                    newRegParser("$Plus",     new WordChecker("Plus")),
	                    newRegParser("$Plus",     new CharSingle('+')),
	                    newRegParser("$Equal",    new WordChecker("Equal")),
	                    newRegParser("$Equal",    new CharSingle('=')),
	                    newRegParser("$Pipe",     new WordChecker("Pipe")),
	                    newRegParser("$Pipe",     new CharSingle('|')),
	                    newRegParser("$BSlash",   new WordChecker("BSlash")),
	                    newRegParser("$BSlash",   new CharSingle('\\')),
	                    newRegParser("$Colon",    new WordChecker("Colon")),
	                    newRegParser("$Colon",    new CharSingle(':')),
	                    newRegParser("$SColon",   new WordChecker("SColon")),
	                    newRegParser("$SColon",   new CharSingle(';')),
	                    newRegParser("$SQoute",   new WordChecker("SQoute")),
	                    newRegParser("$SQoute",   new CharSingle('\'')),
	                    newRegParser("$DQoute",   new WordChecker("DQoute")),
	                    newRegParser("$DQoute",   new CharSingle('\"')),
	                    newRegParser("$Comma",    new WordChecker("Comma")),
	                    newRegParser("$Comma",    new CharSingle(',')),
	                    newRegParser("$Dot",      new WordChecker("Dot")),
	                    newRegParser("$Dot",      new CharSingle('.')),
	                    newRegParser("$QMark",    new WordChecker("QMark")),
	                    newRegParser("$QMark",    new CharSingle('?')),
	                    newRegParser("$Slash",    new WordChecker("Slash")),
	                    newRegParser("$Slash",    new CharSingle('/')),
	                    
	                    newRegParser("$NewLine", new WordChecker("NewLine")),
	                    newRegParser("$NewLine", new CharSingle('\n')),
	                    newRegParser("$Return",  new WordChecker("Return")),
	                    newRegParser("$Return",  new CharSingle('\r')),
	                    newRegParser("$Tab",     new WordChecker("Tab")),
	                    newRegParser("$Tab",     new CharSingle('\t')),
	                    newRegParser("$Space",   new WordChecker("Space")),
	                    newRegParser("$Space",   new CharSingle(' ')),
	                    
	                    newRegParser("$Any",                 new WordChecker("Any")),
	                    newRegParser("$Digit",               new WordChecker("Digit")),
	                    newRegParser("$NonDigit",            new WordChecker("NonDigit")),
	                    newRegParser("$WhiteSpace",          new WordChecker("WhiteSpace")),
	                    newRegParser("$NonWhiteSpace",       new WordChecker("NonWhiteSpace")),
	                    newRegParser("$WhiteSpaceNoNewLine", new WordChecker("WhiteSpaceNoNewLine")),
	                    newRegParser("$Word",                new WordChecker("Word")),
	                    newRegParser("$NonWord",             new WordChecker("NonWord")),
	                    newRegParser("$Blank",               new WordChecker("Blank")),
	                    newRegParser("$NonBlank",            new WordChecker("NonBlank")),
	                    newRegParser("$OctalDigit",          new WordChecker("OctDigit")),
	                    newRegParser("$NonOctalDigit",       new WordChecker("NonOctDigit")),
	                    newRegParser("$HexadecimalDigit",    new WordChecker("HexDigit")),
	                    newRegParser("$NonHexadecimalDigit", new WordChecker("NonHexDigit")),
	                    
	                    newRegParser("$LowerCaseAlphabet", new WordChecker("LowerCaseAlphabet")),
	                    newRegParser("$UpperCaseAlphabet", new WordChecker("UpperCaseAlphabet")),
	                    newRegParser("$ASCII",             new WordChecker("ASCII")),
	                    newRegParser("$AlphabetAndDigit",  new WordChecker("AlphabetAndDigit")), // A&D First
	                    newRegParser("$Alphabet",          new WordChecker("Alphabet")),
	                    newRegParser("$Punctuation",       new WordChecker("Punctuation")),
	                    newRegParser("$Visible",           new WordChecker("Visible")),
	                    newRegParser("$Printable",         new WordChecker("Printable")),
	                    
	                    newRegParser("$JAny",           new WordChecker("JAny")),
	                    newRegParser("$JDigit",         new WordChecker("JDigit")),
	                    newRegParser("$JNonDigit",      new WordChecker("JNonDigit")),
	                    newRegParser("$JWhiteSpace",    new WordChecker("JWhiteSpace")),
	                    newRegParser("$JNonWhiteSpace", new WordChecker("JNonWhiteSpace")),
	                    newRegParser("$JWord",          new WordChecker("JWord")),
	                    newRegParser("$JNonWord",       new WordChecker("JNonWord")),
	                    
	                    newRegParser("$JLowerCaseAlphabet", new WordChecker("JLowerCaseAlphabet")),
	                    newRegParser("$JUpperCaseAlphabet", new WordChecker("JUpperCaseAlphabet")),
	                    newRegParser("$JASCII",             new WordChecker("JASCII")),
	                    newRegParser("$JAlphabetAndDigit",  new WordChecker("JAlphabetAndDigit")), // A&D First
	                    newRegParser("$JAlphabet",          new WordChecker("JAlphabet")),
	                    newRegParser("$JPunctuation",       new WordChecker("JPunctuation")),
	                    newRegParser("$JVisible",           new WordChecker("JVisible")),
	                    newRegParser("$JPrintable",         new WordChecker("JPrintable")),
	                    newRegParser("$JBlank",             new WordChecker("JBlank")),
	                    newRegParser("$JControlCharacter",  new WordChecker("JControlCharacter")),
	                    newRegParser("$JHexadecimalDigit",  new WordChecker("JHexadecimalDigit")),
	                    newRegParser("$JGreek",             new WordChecker("JGreek")),
	                    newRegParser("$JCurrencySimbol",    new WordChecker("JCurrencySimbol"))
	                ),
	                new WordChecker(":]")
	            )
	        )
	    );
	
	private static Hashtable<String, CharChecker> cachedPredefineds = new Hashtable<String, CharChecker>();
	
	public static CharChecker getCharClass(ParseResult thisResult) {
		return getCharClass(thisResult, 0);
	}
	
	public static CharChecker getCharClass(ParseResult thisResult, int entryIndex) {
		// Any
		if (".".equals(thisResult.textOf(entryIndex)))
			return Any;
		
		// Ensure type
		if (!charClassName.equals(thisResult.nameOf(entryIndex))) {
			var nearBy = thisResult.originalText().substring(thisResult.startPosition());
			var errMsg = String.format("Mal-formed RegParser character class near \"%s\".", nearBy);
			throw new CompilationException(errMsg);
		}
		
		// CharClass
		var name = thisResult.nameOf(entryIndex, 1).substring(1); // Remove the '$'
		if (name.startsWith("J")) {
			name = "Java_" + name.substring(1);
		}
		
		return cachedPredefineds.computeIfAbsent(name, className -> {
			var fields = PredefinedCharClasses.class.getFields();
			for (var field : fields) {
				var fieldName = field.getName();
				if (!className.equals(fieldName))
					continue;
				
				try {
					return (CharChecker) field.get(null);
				} catch (Exception E) {
				}
			}
			return null;
		});
	}
	
}
