/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

package net.nawaman.regparser.parsers;

import net.nawaman.regparser.*;

/**
 * Parser for detecting String literal "'`
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTStrLiteral extends PType {
	static public String Name = "$StringLiteral";
	@Override public String getName() { return Name; }
	Checker Checker = RegParser.newRegParser("#String",
		new CheckerAlternative(
			RegParser.newRegParser(
				new CharSingle('\"'),
				RegParser.newRegParser(
					new CheckerAlternative(
						new CharNot(new CharSingle('\'')),
						new WordChecker("\\\"")
					)
				),
				Quantifier.ZeroOrMore,
				new CharSingle('\"')
			),
			RegParser.newRegParser(
				new CharSingle('\''),
				RegParser.newRegParser(
					new CheckerAlternative(
						new CharNot(new CharSingle('\'')),
						new WordChecker("\\\'")
					)
				),
				Quantifier.ZeroOrMore,
				new CharSingle('\'')
			),
			RegParser.newRegParser(
					new CharSingle('`'),
					RegParser.newRegParser(
						new CheckerAlternative(
							new CharNot(new CharSingle('`')),
							new WordChecker("\\`")
						)
					), Quantifier.ZeroOrMore,
					new CharSingle('`')
				)
		)
	);
	@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
	
}