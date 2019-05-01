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

/**
 * RegParser that is constructed from difference component.
 * 
 * @author Nawapunth Manusitthipol
 **/
@SuppressWarnings("serial")
public class PTComposable extends PTSimple {
	
	protected PTComposable(String pTheName) {
		super(pTheName);
	}
	public PTComposable(String pTheName, Checker pTheChecker, RPVerifier pVerifier, RPCompiler pCompiler) {
		super(pTheName, pTheChecker);
		this.Verifier = pVerifier;
		this.Compiler = pCompiler;
	}
	public PTComposable(String pTheName, RPGetChecker pTheGetChecker, RPVerifier pVerifier, RPCompiler pCompiler) {
		super(pTheName, pTheGetChecker);
		this.Verifier = pVerifier;
		this.Compiler = pCompiler;
	}

	RPVerifier Verifier;
	RPCompiler Compiler;
	
	protected boolean setVerifier(RPVerifier pVerifier) {
		if(this.Verifier != null) return false;
		this.Verifier = pVerifier;
		return true;
	}
	protected boolean setCompiler(RPCompiler pCompiler) {
		if(this.Compiler != null) return false;
		this.Compiler = pCompiler;
		return true;
	}
	
	/**{@inheritDoc}*/ @Override
	final public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
			PTypeProvider pProvider) {
		if(this.Verifier == null) return super.validate(pHostResult, pThisResult, pParam, pProvider);
		return this.Verifier.validate(pHostResult, pThisResult, pParam, pProvider);
	}
	
	/**{@inheritDoc}*/ @Override
	final public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam, CompilationContext pContext,
			PTypeProvider pProvider) {
		Object Return = null;
		if(this.Compiler == null) Return = pThisResult.textOf(pEntryIndex);
		else                      Return = this.Compiler.compile(pThisResult, pEntryIndex, pParam, pContext, pProvider);
		return Return;
	}
}
