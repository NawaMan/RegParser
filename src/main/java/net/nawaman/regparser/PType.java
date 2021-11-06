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

package net.nawaman.regparser;

import java.io.Serializable;

import net.nawaman.regparser.result.ParseResult;

/**
 * Regular Parser Type
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class PType implements Serializable {
    
    static private final long serialVersionUID = 7148744076563340787L;
    
    /** An empty array of RPType */
    static public final PType[] EmptyTypeArray = new PType[0];
    
    /** Returns the name of the type */
    abstract public String name();
    
    PTypeRef DefaultRef = null;
    
    /** Return the default TypeRef of this type */
    final public PTypeRef getTypeRef() {
        return (this.DefaultRef != null) ? this.DefaultRef
                : (this.DefaultRef = new PTypeRef.Simple(this.name(), null));
    }
    
    /** Return the default TypeRef of this type with the parameter */
    final public PTypeRef getTypeRef(String pParam) {
        return (pParam == null) ? this.getTypeRef() : new PTypeRef.Simple(this.name(), pParam);
    }
    
    /** Returns the checker for parsing the type */
    abstract public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider);
    
    int Flags = 0;
    
    /** Checks if this type will not record the sub-result but record as a text */
    final public boolean isText() {
        if ((this.Flags & 0x80) != 0)
            return ((this.Flags & 0x08) != 0);
        boolean IsText = this.name().startsWith("$");
        this.Flags = (this.Flags | 0x80) | (IsText ? 0x08 : 0x00);
        return IsText;
    }
    
    /** Checks if the continuous text results of this type will collapse into one */
    final public boolean isCollective() {
        if ((this.Flags & 0x40) != 0)
            return ((this.Flags & 0x04) != 0);
        boolean IsCollective = this.name().endsWith("[]");
        this.Flags = (this.Flags | 0x40) | (IsCollective ? 0x04 : 0x00);
        return IsCollective;
    }
    
    /**
     * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
     *    mandatory to determine its length
     **/
    final public boolean hasValidation() {
        if ((this.Flags & 0x20) != 0)
            return ((this.Flags & 0x02) != 0);
        String  N             = this.name();
        boolean HasValidation = N.contains("?") || N.contains("~");
        this.Flags = (this.Flags | 0x20) | (HasValidation ? 0x02 : 0x00);
        return HasValidation;
    }
    
    /**
     * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
     *    mandatory to determine its length
     **/
    final public boolean isSelfContain() {
        if ((this.Flags & 0x10) != 0)
            return ((this.Flags & 0x01) != 0);
        String  N             = this.name();
        boolean IsSelfContain = !N.contains("~");
        this.Flags = (this.Flags | 0x10) | (IsSelfContain ? 0x01 : 0x00);
        return IsSelfContain;
    }
    
    /**
     * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
     *    mandatory to determine its length
     **/
    final public boolean hasFlatAlways() {
        if ((this.Flags & 0x2000) != 0)
            return ((this.Flags & 0x0200) != 0);
        String  N             = this.name();
        boolean HasValidation = N.contains("*");
        this.Flags = (this.Flags | 0x2000) | (HasValidation ? 0x0200 : 0x0000);
        return HasValidation;
    }
    
    /**
     * Checks if the boundary of the result of this type can be determine by the its checker alone and validation is not
     *    mandatory to determine its length
     **/
    final public boolean isFlatSingle() {
        if ((this.Flags & 0x1000) != 0)
            return ((this.Flags & 0x0100) != 0);
        String  N             = this.name();
        boolean IsSelfContain = !N.contains("+");
        this.Flags = (this.Flags | 0x1000) | (IsSelfContain ? 0x0100 : 0x0000);
        return IsSelfContain;
    }
    
    // Parsing and Matching ------------------------------------------------------------------------
    
    RegParser     ThisRP    = null;
    PTypeProvider TProvider = null;
    
    PTypeProvider getDefaultTypeProvider() {
        return this.TProvider;
    }
    
    /** Returns the RegParser wrapping this type */
    public RegParser getRegParser() {
        if (this.ThisRP == null)
            this.ThisRP = RegParser.newRegParser(this);
        return this.ThisRP;
    }
    
    // Parse
    
    /** Returns the the match if the text is start with a match or -1 if not */
    final public ParseResult parse(CharSequence pText) {
        return this.parse(pText, 0, null);
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    final public ParseResult parse(CharSequence pText, int pOffset) {
        return this.parse(pText, pOffset, null);
    }
    
    /** Returns the the match if the text is start with a match or -1 if not */
    final public ParseResult parse(CharSequence pText, PTypeProvider pProvider) {
        return this.parse(pText, 0, pProvider);
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    final public ParseResult parse(CharSequence pText, int pOffset, PTypeProvider pProvider) {
        return this.doParse(pText, pOffset, pProvider);
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    public ParseResult doParse(CharSequence pText, int pOffset, PTypeProvider pProvider) {
        PTypeProvider TP = PTypeProvider.Library.getEither(pProvider, this.TProvider);
        return this.getRegParser().parse(pText, pOffset, TP);
    }
    
    // Match
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    final public ParseResult match(CharSequence pText) {
        return this.match(pText, 0, pText.length(), null);
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    final public ParseResult match(CharSequence pText, int pOffset, int pEndPosition) {
        return this.match(pText, pOffset, (pEndPosition == -1) ? pText.length() : pEndPosition, null);
    }
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    final public ParseResult match(CharSequence pText, PTypeProvider pProvider) {
        return this.match(pText, 0, pText.length(), pProvider);
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    final public ParseResult match(CharSequence pText, int pOffset) {
        return this.match(pText, pOffset, pText.length(), null);
    }
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    final public ParseResult match(CharSequence pText, int pOffset, PTypeProvider pProvider) {
        return this.match(pText, pOffset, pText.length(), pProvider);
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    final public ParseResult match(CharSequence pText, int pOffset, int pEndPosition, PTypeProvider pProvider) {
        return this.doMatch(pText, pOffset, pEndPosition, pProvider);
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    protected ParseResult doMatch(CharSequence pText, int pOffset, int pEndPosition, PTypeProvider pProvider) {
        PTypeProvider TP = PTypeProvider.Library.getEither(pProvider, this.TProvider);
        return this.getRegParser().match(pText, pOffset, (pEndPosition == -1) ? pText.length() : pEndPosition, TP);
    }
    
    // Validation ----------------------------------------------------------------------------------
    
    /** Returns a display string that represent a validation code */
    public String getValidation_toString() {
        return "...";
    }
    
    /** Validate the parse result */
    final public boolean validate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            PTypeProvider pProvider) {
        PTypeProvider TP = PTypeProvider.Library.getEither(pProvider, this.TProvider);
        return this.doValidate(pHostResult, pThisResult, pParam, TP);
    }
    
    /** Validate the parse result */
    protected boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            PTypeProvider pProvider) {
        return true;
    }
    
    // Compilation ---------------------------------------------------------------------------------
    
    /** Returns a display string that represent a compilation code */
    public String getCompilation_toString() {
        return "...";
    }
    
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(String pText) {
        ParseResult ThisResult = RegParser.newRegParser(this).match(pText);
        if (ThisResult == null)
            return null;
        return this.compile(ThisResult, 0, null, null, null);
    }
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(String pText, PTypeProvider pProvider) {
        ParseResult ThisResult = RegParser.newRegParser(this).match(pText, pProvider);
        if (ThisResult == null)
            return null;
        
        return this.compile(ThisResult, 0, null, null, pProvider);
        
    }
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(String pText, String pParam, PTypeProvider pProvider) {
        return this.compile(pText, pParam, null, pProvider);
    }
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(String pText, String pParam, CompilationContext pContext, PTypeProvider pProvider) {
        RegParser RP = null;
        
        if (pParam == null)
            RP = RegParser.newRegParser(this);
        else {
            // The provide does not hold this type
            if (pProvider.getType(this.name()) == null) {
                // Add it in
                PTypeProvider         NewProvider = new PTypeProvider.Extensible();
                PTypeProvider.Library NewLibrary  = new PTypeProvider.Library(pProvider, NewProvider);
                ((PTypeProvider.Extensible) NewProvider).addRPType(this);
                pProvider = NewLibrary;
            }
            RP = RegParser.newRegParser(new PTypeRef.Simple(this.name(), pParam));
        }
        ParseResult ThisResult = RP.match(pText, pProvider);
        if (ThisResult == null)
            return null;
        
        return this.compile(ThisResult, 0, pParam, pContext, pProvider);
    }
    
    // Parse from Result -----------------------------------------------------------------------------------------------
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(ParseResult pThisResult) {
        return this.compile(pThisResult, 0, null, null, null);
    }
    
    /** Compiles a ParseResult in to an object */
    final public Object compile(ParseResult pThisResult, PTypeProvider pProvider) {
        return this.compile(pThisResult, 0, null, null, pProvider);
    }
    
    /** Compiles a ParseResult in to an object with a parameter */
    final public Object compile(ParseResult pThisResult, String pParam, CompilationContext pContext,
            PTypeProvider pProvider) {
        return this.compile(pThisResult, 0, null, pContext, pProvider);
    }
    
    /** Compiles a ParseResult in to an object with a parameter */
    final public Object compile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
            PTypeProvider pProvider) {
        PTypeProvider TP = PTypeProvider.Library.getEither(pProvider, this.TProvider);
        return this.doCompile(pThisResult, pEntryIndex, pParam, pContext, TP);
    }
    
    /** Compiles a ParseResult in to an object with a parameter */
    protected Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
            PTypeProvider pProvider) {
        return (pThisResult == null) ? null : pThisResult.textOf(pEntryIndex);
    }
    
    // Object --------------------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "!" + this.name() + "!";
    }
    
}
