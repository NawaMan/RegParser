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

package net.nawaman.regparser.types;

import static java.lang.String.format;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser type for detecting error.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class ErrorParserType extends ParserType {
	
	private static final long serialVersionUID = -7968950980059799341L;
	
	private final String  name;
	private final Checker checker;
	private final String  errorMessage;
	private final boolean isFatal;
	
	public ErrorParserType(String name, Checker checker, String errorMessage) {
		this(name, checker, errorMessage, false);
	}
	
	public ErrorParserType(String name, Checker checker, String errorMessage, boolean isFatal) {
		this.name         = name;
		this.errorMessage = errorMessage;
		this.isFatal      = isFatal;
		this.checker      = checker;
	}
	
	
	/**{@inheritDoc}*/
	@Override
	public final String name() {
		return this.name;
	}
	
	/**{@inheritDoc}*/
	@Override
	public final Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return this.checker;
	}
	
	/**{@inheritDoc}*/
	@Override
	public Object doCompile(
					ParseResult        result,
					int                entryIndex,
					String             parameter,
					CompilationContext compilationContext,
					ParserTypeProvider typeProvider) {
		
		var param    = (parameter == null) ? "" : String.format(" (%s)", parameter);
		var location = compilationContext.getLocationAsString(result.startPositionOf(entryIndex));
		var errMsg   = format("%s%s\n%s", errorMessage, param, location);
		
		if (compilationContext != null) {
			compilationContext.reportError(errMsg, null);
		}
		
		if (this.isFatal)
			throw new RuntimeException("FATAL ERROR! The compilation cannot be continued: " + errorMessage);
		
		return null;
	}
	
	@Override
	public final Boolean isDeterministic() {
		return checker.isDeterministic();
	}
	
}
