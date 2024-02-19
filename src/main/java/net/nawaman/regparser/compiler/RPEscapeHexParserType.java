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
package net.nawaman.regparser.compiler;

import static java.lang.String.format;
import static net.nawaman.regparser.PredefinedCharClasses.HexadecimalDigit;
import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser type for RegParser escape for hexadecimal number.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RPEscapeHexParserType extends ParserType {
    
    private static final long serialVersionUID = -8357960494054126599L;
    
    public static String                name     = "EscapeHex";
    public static RPEscapeHexParserType instance = new RPEscapeHexParserType();
    public static ParserTypeRef         typeRef  = instance.typeRef();
    public static RegParser             parser   = instance.typeRef().asRegParser();
    
    final public static String HEX = "0123456789ABCDEF";
    
    private final Checker checker;
    
    public RPEscapeHexParserType() {
        // ~\\x[0-9A-Fa-f][0-9A-Fa-f]~
        checker = newRegParser()
                .entry(new WordChecker("\\x"))
                .entry(HexadecimalDigit)
                .entry(HexadecimalDigit)
                .build();
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
    public final Boolean isDeterministic() {
        return true;
    }
    
    @Override
    public Object doCompile(
                    ParseResult        thisResult,
                    int                entryIndex,
                    String             parameter,
                    CompilationContext compilationContext,
                    ParserTypeProvider typeProvider) {
        
        // Ensure type
        var typeName = thisResult.typeNameOf(entryIndex);
        if (!name.equals(typeName)) {
            var nearBy = thisResult.originalText().substring(thisResult.startPosition());
            var errMsg = format("Mal-formed RegParser Escape near \"%s\".", nearBy);
            throw new CompilationException(errMsg);
        }
        
        var text = thisResult.textOf(entryIndex).toUpperCase();
        return (char)(HEX.indexOf(text.charAt(2)) * 16
                    + HEX.indexOf(text.charAt(3)));
    }
    
}
