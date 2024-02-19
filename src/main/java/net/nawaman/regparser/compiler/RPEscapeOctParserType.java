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

import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.CompilationException;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser type for RegParser escape for octave number.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RPEscapeOctParserType extends ParserType {
    
    private static final long serialVersionUID = 7092438661222948500L;
    
    public static String                name     = "EscapeOct";
    public static RPEscapeOctParserType instance = new RPEscapeOctParserType();
    public static ParserTypeRef         typeRef  = instance.typeRef();
    public static RegParser             parser   = instance.typeRef().asRegParser();
    
    private static final String OCT = "01234567";
    
    private final Checker checker;
    
    public RPEscapeOctParserType() {
        // ~\\0[0-3]?[0-7]?[0-7]~
        checker = newRegParser()
                .entry(new WordChecker("\\0"))
                .entry(new CharRange('0', '3').zeroOrOne())
                .entry(new CharRange('0', '7').bound(1, 2))
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
        
        var typeName = thisResult.typeNameOf(entryIndex);
        if (!name.equals(typeName)) {
            var nearBy = thisResult.originalText().substring(thisResult.startPosition());
            var errMsg = String.format("Mal-formed RegParser Escape near \"%s\".", nearBy);
            throw new CompilationException(errMsg);
        }
        
        var text = thisResult.textOf(entryIndex).substring(2);
        while (text.length() < 3) {
            text = "0" + text;
        }
        return (char)(OCT.indexOf(text.charAt(0)) * 8 * 8
                    + OCT.indexOf(text.charAt(1)) * 8
                    + OCT.indexOf(text.charAt(2)));
    }
}
