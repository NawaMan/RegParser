/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

package net.nawaman.regparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checker that is associated with Java Pattern Character set.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
final public class CharClass extends CharChecker {
    
    static private final long serialVersionUID = 1235456543213546515L;
    
    CharClass(String pJavaCharClass, String pClassName) {
        this.JavaCharClass = pJavaCharClass;
        this.ClassName     = pClassName;
    }
    
    String  ClassName;
    String  JavaCharClass;
    Pattern JCCPatternSingle   = null;
    Pattern JCCPatternMultiple = null;
    
    private Pattern getJCCPatternSingle() {
        if (this.JCCPatternSingle == null)
            this.JCCPatternSingle = Pattern.compile(this.JavaCharClass + "{1}", Pattern.DOTALL);
        return this.JCCPatternSingle;
    }
    
    private Pattern getJCCPatternMultiple() {
        if (this.JCCPatternMultiple == null)
            this.JCCPatternMultiple = Pattern.compile(this.JavaCharClass + "+", Pattern.DOTALL);
        return this.JCCPatternMultiple;
    }
    
    /** Checks of the char c is in this char checker */
    @Override
    public boolean inSet(char c) {
        Pattern P = this.getJCCPatternSingle();
        Matcher M = P.matcher(new StringBuffer().append(c));
        return M.find();
    }
    
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
        if (pOffset >= S.length())
            return -1;
        
        Pattern P = this.getJCCPatternMultiple();
        Matcher M = P.matcher(new StringBuffer().append(S.charAt(pOffset)));
        if (!M.find() || (M.start() != 0))
            return -1;
        return (M.end() < 0) ? -1 : M.end();
    }
    
    
    @Override
    public String toString() {
        return this.ClassName;
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        return (O instanceof CharClass) && ((CharClass) O).ClassName.equals(this.ClassName);
    }
    
    @Override
    public int hashCode() {
        return "CharClass".hashCode() + this.ClassName.hashCode();
    }
    
    
    /** Return the optimized version of this Checker */
    @Override
    public Checker getOptimized() {
        return this;
    }
    
}
