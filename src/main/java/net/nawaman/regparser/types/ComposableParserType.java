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

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

/**
 * RegParser that is constructed from difference component.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
public class ComposableParserType extends SimpleParserType {
    
    private static final long serialVersionUID = 8825339043204700404L;
    
    public ComposableParserType(String pTheName, Checker pTheChecker, ResultVerifier pVerifier, ResultCompiler pCompiler) {
        super(pTheName, pTheChecker);
        this.verifier = pVerifier;
        this.compiler = pCompiler;
    }
    
    public ComposableParserType(String pTheName, CheckerProvider pTheGetChecker, ResultVerifier pVerifier, ResultCompiler pCompiler) {
        super(pTheName, pTheGetChecker);
        this.verifier = pVerifier;
        this.compiler = pCompiler;
    }
    
    private final ResultVerifier verifier;
    private final ResultCompiler compiler;
    
//    protected boolean setVerifier(ResultVerifier pVerifier) {
//        if (this.Verifier != null)
//            return false;
//        this.Verifier = pVerifier;
//        return true;
//    }
//    
//    protected boolean setCompiler(ResultCompiler pCompiler) {
//        if (this.Compiler != null)
//            return false;
//        this.Compiler = pCompiler;
//        return true;
//    }
    
    /**{@inheritDoc}*/
    @Override
    final public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
            ParserTypeProvider pProvider) {
        if (this.verifier == null)
            return super.validate(pHostResult, pThisResult, pParam, pProvider);
        return this.verifier.validate(pHostResult, pThisResult, pParam, pProvider);
    }
    
    /**{@inheritDoc}*/
    @Override
    final public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
            ParserTypeProvider pProvider) {
        Object Return = null;
        if (this.compiler == null)
            Return = pThisResult.textOf(pEntryIndex);
        else
            Return = this.compiler.compile(pThisResult, pEntryIndex, pParam, pContext, pProvider);
        return Return;
    }
}
