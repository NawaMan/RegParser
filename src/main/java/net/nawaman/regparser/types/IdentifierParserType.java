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

import static net.nawaman.regparser.PredefinedCharClasses.Alphabet;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser for detecting C-like identifier
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class IdentifierParserType extends ParserType {
    
    private static final long serialVersionUID = -3805785789736448768L;
    
    public static String               name     = "$Identifier";
    public static IdentifierParserType instance = new IdentifierParserType();
    public static ParserTypeRef        typeRef  = instance.typeRef();
    
    private final Checker checker;
    
    public IdentifierParserType() {
        checker = newRegParser()
                .entry(new CharUnion(Alphabet, new CharSingle('_')))
                .entry(new CharUnion(Alphabet, new CharSingle('_'), Digit), ZeroOrMore)
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
        return checker.isDeterministic();
    }
    
}
