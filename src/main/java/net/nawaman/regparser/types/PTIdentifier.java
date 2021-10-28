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
 * package. You can inform me via nawaman<at>gmail<dot>com.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.types;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.PredefinedCharClasses;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;
import net.nawaman.regparser.result.ParseResult;

/**
 * Parser for detecting C-like identifier
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class PTIdentifier extends PType {
    
    static public String Name = "$Identifier";
    
    @Override
    public String name() {
        return Name;
    }
    
    Checker Checker = RegParser.newRegParser(new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_')),
            new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_'), PredefinedCharClasses.Digit),
            Quantifier.ZeroOrMore);
    
    @Override
    public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
        return this.Checker;
    }
    
}
