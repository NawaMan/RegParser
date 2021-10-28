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
 * package. You can inform me via me<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.checkers;

import java.util.regex.Pattern;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker that is associated with Java Pattern Character set.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
final public class CharClass extends CharChecker {
    
    private static final long serialVersionUID = 1235456543213546515L;
    
    public CharClass(String javaCharClass, String className) {
        this.javaCharClass = javaCharClass;
        this.className     = className;
    }
    
    private final String className;
    private final String javaCharClass;
    
    private Pattern patternSingle   = null;
    private Pattern patternMultiple = null;
    
    private Pattern getJCCPatternSingle() {
        if (this.patternSingle == null) {
            this.patternSingle = Pattern.compile(this.javaCharClass + "{1}", Pattern.DOTALL);
        }
        return this.patternSingle;
    }
    
    private Pattern getJCCPatternMultiple() {
        if (this.patternMultiple == null) {
            this.patternMultiple = Pattern.compile(this.javaCharClass + "+", Pattern.DOTALL);
        }
        return this.patternMultiple;
    }
    
    @Override
    public boolean inSet(char c) {
        var pattern = this.getJCCPatternSingle();
        var matcher = pattern.matcher(new StringBuffer().append(c));
        return matcher.find();
    }
    
    @Override
    public int startLengthOf(CharSequence text, int offset, PTypeProvider typeProvider, ParseResult parseResult) {
        if (offset >= text.length()) {
            return -1;
        }
        
        var pattern = this.getJCCPatternMultiple();
        var matcher = pattern.matcher(new StringBuffer().append(text.charAt(offset)));
        if (!matcher.find() || (matcher.start() != 0)) {
            return -1;
        }
        return (matcher.end() < 0) ? -1 : matcher.end();
    }
    
    
    @Override
    public String toString() {
        return this.className;
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        return (O instanceof CharClass) && ((CharClass) O).className.equals(this.className);
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
