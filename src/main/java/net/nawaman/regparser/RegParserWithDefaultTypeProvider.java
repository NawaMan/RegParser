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

import net.nawaman.regparser.result.ParseResult;

/** TypeProvider with Default Type */
public class RegParserWithDefaultTypeProvider extends RegParser {
	
	private static final long serialVersionUID = -7904907723214116873L;
	
	public static RegParser attachDefaultTypeProvider(RegParser regParser, ParserTypeProvider typeProvider) {
		if ((regParser == null) || (typeProvider == null))
			return regParser;
		
		var originalProvider = regParser.getDefaultTypeProvider();
		if ((regParser instanceof RegParserWithDefaultTypeProvider)
		 && (originalProvider instanceof ParserTypeProvider.Library)) {
			((ParserTypeProvider.Library) originalProvider).addProvider(typeProvider);
		} else {
			var parserEntries   = ((RegParser)regParser).entries();
			var entries         = (parserEntries == null) ? null : parserEntries.toArray(RegParserEntry[]::new);
			var newTypeProvider = ((originalProvider != null) && (originalProvider != typeProvider))
			                    ? null
			                    : new ParserTypeProvider.Library(typeProvider, originalProvider);
			regParser = new RegParserWithDefaultTypeProvider(entries, newTypeProvider);
		}
		return regParser;
	}
	
	RegParserWithDefaultTypeProvider(RegParserEntry[] entries, ParserTypeProvider typeProvider) {
		super(entries);
		this.typeProvider = typeProvider;
	}
	
	private ParserTypeProvider typeProvider = null;
	
	@Override
	ParserTypeProvider getDefaultTypeProvider() {
		return this.typeProvider;
	}
	
	// Parse
	
	/** {@inheritDoc} */
	@Override
	/**
	 * Returns the length of the match if the text is start with a match or -1 if
	 * not
	 */
	protected ParseResult parse(
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			ParserType         parserType,
			String             parameter,
			int                tabs) {
		var combinedTypeProvider = ParserTypeProvider.Library.either(typeProvider, this.typeProvider);
		return super.parse(text, offset, index, times, parseResult, combinedTypeProvider, parserType, parameter, tabs);
	}
	
}