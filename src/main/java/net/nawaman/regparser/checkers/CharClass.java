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

package net.nawaman.regparser.checkers;

import java.util.regex.Pattern;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker that is associated with Java Pattern Character set.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class CharClass extends CharChecker {
    
    private static final long serialVersionUID = 1235456543213546515L;
    
    private final String className;
    private final String javaCharClass;
    
    private Pattern patternSingle   = null;
    private Pattern patternMultiple = null;
    
    public CharClass(String javaCharClass, String className) {
        this.javaCharClass = javaCharClass;
        this.className     = className;
    }
    
    private Pattern getJavaCharacterClassPatternSingle() {
        if (patternSingle == null) {
            patternSingle = Pattern.compile(javaCharClass + "{1}", Pattern.DOTALL);
        }
        return patternSingle;
    }
    
    private Pattern getJavaCharacterClassPatternMultiple() {
        if (patternMultiple == null) {
            patternMultiple = Pattern.compile(javaCharClass + "+", Pattern.DOTALL);
        }
        return patternMultiple;
    }
    
    @Override
    public boolean inSet(char c) {
        var pattern = getJavaCharacterClassPatternSingle();
        var matcher = pattern.matcher(new StringBuffer().append(c));
        return matcher.find();
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
        if (offset >= text.length())
            return -1;
        
        var  pattern = getJavaCharacterClassPatternMultiple();
        char ch      = text.charAt(offset);
        var  matcher = pattern.matcher(new StringBuffer().append(ch));
        if (!matcher.find()
         || (matcher.start() != 0))
            return -1;
        
        return (matcher.end() < 0) ? -1 : matcher.end();
    }
    
    @Override
    public final Boolean isDeterministic() {
        return true;
    }
    
    @Override
    public String toString() {
        return className;
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        
        return (O instanceof CharClass)
            && ((CharClass)O).className.equals(this.className);
    }
    
    @Override
    public int hashCode() {
        return "CharClass".hashCode() + this.className.hashCode();
    }
    
    @Override
    public Checker optimize() {
        return this;
    }
    
}
