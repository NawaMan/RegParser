/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2024 Nawapunth Manusitthipol.
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

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * A simple type (type without a compiler).
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class SimpleParserType extends ParserType {
	
	private static final long serialVersionUID = 5886175272511843777L;
	
	public SimpleParserType(String name, Checker checker) {
		this(name, CheckerProvider.of(checker));
	}
	
	public SimpleParserType(String name, CheckerProvider checkerProvider) {
		this.name            = name;
		this.checkerProvider = checkerProvider;
	}
	
	private final String          name;
	private final CheckerProvider checkerProvider;
	
	/**{@inheritDoc}*/
	@Override
	public final String name() {
		return this.name;
	}
	
	/**{@inheritDoc}*/
	@Override
	public final Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		
		if (this.checkerProvider instanceof CheckerProvider)
			return ((CheckerProvider)checkerProvider).getChecker(hostResult, parameter, typeProvider);
		
		return (Checker)checkerProvider;
	}
	
	@Override
	public final Boolean isDeterministic() {
		return null;
	}
	
}
