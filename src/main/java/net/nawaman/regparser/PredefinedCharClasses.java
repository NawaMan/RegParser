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

import net.nawaman.regparser.checkers.CharChecker;
import net.nawaman.regparser.checkers.CharClass;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;

/**
 * Predefine character classes
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class PredefinedCharClasses {
	
	// Nawa ------------------------------------------------------------------------------------------------------------
	// For easier escape
	static public final CharSingle Tilde    = new CharSingle('~');
	static public final CharSingle Stress   = new CharSingle('`');
	static public final CharSingle XMark    = new CharSingle('!');
	static public final CharSingle AtSign   = new CharSingle('@');
	static public final CharSingle Hash     = new CharSingle('#');
	static public final CharSingle Dollar   = new CharSingle('$');
	static public final CharSingle Percent  = new CharSingle('%');
	static public final CharSingle Caret    = new CharSingle('^');
	static public final CharSingle AndSign  = new CharSingle('&');
	static public final CharSingle Asterisk = new CharSingle('*');
	static public final CharSingle ORound   = new CharSingle('(');
	static public final CharSingle CRound   = new CharSingle(')');
	static public final CharSingle OSquare  = new CharSingle('[');
	static public final CharSingle CSquare  = new CharSingle(']');
	static public final CharSingle OCurl    = new CharSingle('{');
	static public final CharSingle CCurl    = new CharSingle('}');
	static public final CharSingle OAngle   = new CharSingle('<');
	static public final CharSingle CAngle   = new CharSingle('>');
	static public final CharSingle UScore   = new CharSingle('_');
	static public final CharSingle Minus    = new CharSingle('-');
	static public final CharSingle Plus     = new CharSingle('+');
	static public final CharSingle Equal    = new CharSingle('=');
	static public final CharSingle Pipe     = new CharSingle('|');
	static public final CharSingle BSlash   = new CharSingle('\\');
	static public final CharSingle Colon    = new CharSingle(':');
	static public final CharSingle SColon   = new CharSingle(';');
	static public final CharSingle SQoute   = new CharSingle('\'');
	static public final CharSingle DQoute   = new CharSingle('\"');
	static public final CharSingle Comma    = new CharSingle(',');
	static public final CharSingle Dot      = new CharSingle('.');
	static public final CharSingle QMark    = new CharSingle('?');
	static public final CharSingle Slash    = new CharSingle('/');
	
	static public final CharSingle NewLine = new CharSingle('\n');
	static public final CharSingle Return  = new CharSingle('\r');
	static public final CharSingle Tab     = new CharSingle('\t');
	static public final CharSingle Space   = new CharSingle(' ');
	
	// For speed
	static public final CharChecker Any           = new CharRange((char)0, Character.MAX_VALUE);
	static public final CharChecker Digit         = new CharRange('0', '9');
	static public final CharChecker NonDigit      = new CharNot(Digit);
	static public final CharChecker WhiteSpace    = new CharSet(" \t\n\r\u000B\u000C");
	static public final CharChecker NonWhiteSpace = new CharNot(WhiteSpace);
	static public final CharChecker Blank         = new CharSet(" \t");
	static public final CharChecker NonBlank      = new CharNot(Blank);
	
	static public final CharChecker OctalDigit          = new CharRange('0', '7');
	static public final CharChecker HexadecimalDigit    = new CharUnion(
	                                                              new CharRange('0', '9'),
	                                                              new CharRange('a', 'f'),
	                                                              new CharRange('A', 'F')
	                                                      );
	static public final CharChecker NonOctalDigit       = new CharNot(OctalDigit);
	static public final CharChecker NonHexadecimalDigit = new CharNot(HexadecimalDigit);
	
	static public final CharChecker LowerCaseAlphabet = new CharRange('a', 'z');
	static public final CharChecker UpperCaseAlphabet = new CharRange('A', 'Z');
	static public final CharChecker ASCII             = new CharRange((char)0, (char)0x7F);
	static public final CharChecker Alphabet          = new CharUnion(LowerCaseAlphabet, UpperCaseAlphabet);
	static public final CharChecker AlphabetAndDigit  = new CharUnion(Digit, Alphabet);
	static public final CharChecker Punctuation       = new CharSet("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");
	static public final CharChecker Visible           = new CharUnion(Digit, Alphabet, Punctuation);
	static public final CharChecker Printable         = new CharUnion(Digit, Alphabet, Punctuation, WhiteSpace);
	
	static public final CharChecker Word    = AlphabetAndDigit;
	static public final CharChecker NonWord = new CharNot(Word);
	
	// Java ------------------------------------------------------------------------------------------------------------
	// For compatibility
	
	static public final CharClass Java_Any                 = new CharClass(".",           "[:Any:]");
	static public final CharClass Java_Digit               = new CharClass("\\d",         "[:Digit:]");
	static public final CharClass Java_NonDigit            = new CharClass("\\D",         "[:NonDigit:]");
	static public final CharClass Java_WhiteSpace          = new CharClass("\\s",         "[:WhiteSpace:]");
	static public final CharClass Java_NonWhiteSpace       = new CharClass("\\S",         "[:NonWhiteSpace:]");
	static public final CharClass Java_WhiteSpaceNoNewLine = new CharClass("[ \\t\\x0B]", "[:WhiteSpaceNoNewLine:]");
	static public final CharClass Java_Word                = new CharClass("\\w",         "[:Word:]");
	static public final CharClass Java_NonWord             = new CharClass("\\W",         "[:NonWord:]");
	
	static public final CharClass Java_LowerCaseAlphabet = new CharClass("\\p{Lower}",  "[:LowerCaseAlphabet:]");
	static public final CharClass Java_UpperCaseAlphabet = new CharClass("\\p{Upper}",  "[:UpperCaseAlphabet:]");
	static public final CharClass Java_ASCII             = new CharClass("\\p{ASCII}",  "[:ASCII:]");
	static public final CharClass Java_Alphabet          = new CharClass("\\p{Alpha}",  "[:Alphabet:]");
	static public final CharClass Java_AlphabetAndDigit  = new CharClass("\\p{Alnum}",  "[:AlphabetAndDigit:]");
	static public final CharClass Java_Punctuation       = new CharClass("\\p{Punct}",  "[:Punctuation:]");
	static public final CharClass Java_Visible           = new CharClass("\\p{Graph}",  "[:Visible:]");
	static public final CharClass Java_Printable         = new CharClass("\\p{Print}",  "[:Printable:]");
	static public final CharClass Java_Blank             = new CharClass("\\p{Blank}",  "[:SpaceOrTab:]");
	static public final CharClass Java_ControlCharacter  = new CharClass("\\p{Cntrl}",  "[:ControlCharacter:]");
	static public final CharClass Java_HexadecimalDigit  = new CharClass("\\p{XDigit}", "[:HexadecimalDigit:]");
	
	static public final CharClass Java_Greek          = new CharClass("\\p{InGreek}", "[:Greek:]");
	static public final CharClass Java_CurrencySimbol = new CharClass("\\p{Sc}",      "[:CurrencySimbol:]");
	
}
