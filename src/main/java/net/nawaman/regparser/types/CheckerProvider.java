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

import java.io.Serializable;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * The provider of a checker use as a strategy for PType (used with PTSimple and PTComposable).
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
@FunctionalInterface
public interface CheckerProvider extends Serializable {
    
    public static CheckerProvider of(Checker checker) {
        return (hostResult, parameter, typeProvider) -> checker;
    }
    
    
    /** Returns the checker */
    public Checker getChecker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider);
    
}
