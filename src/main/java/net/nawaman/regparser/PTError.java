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
 * Parser type for detecting error.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 **/
@SuppressWarnings("serial")
public class PTError extends PType {

	protected PTError(String pTheName, String pErrMessage, boolean pIsFatal) {
		this.TheName    = pTheName;
		this.ErrMessage = pErrMessage;
		this.IsFatal    = pIsFatal;
	}
	protected PTError(String pTheName, String pErrMessage) {
		this(pTheName, pErrMessage, false);
	}
	public PTError(String pTheName, Checker pTheChecker, String pErrMessage) {
		this(pTheName, pErrMessage, false);
		this.TheChecker = pTheChecker;
	}
	public PTError(String pTheName, Checker pTheChecker, String pErrMessage, boolean pIsFatal) {
		this(pTheName, pErrMessage, pIsFatal);
		this.TheChecker = pTheChecker;
	}

	String  TheName;
	Checker TheChecker;
	String  ErrMessage;
	boolean IsFatal = false;

	
	/**{@inheritDoc}*/ @Override final public String getName() { return this.TheName; }

	/**{@inheritDoc}*/ @Override final public Checker getChecker(ParseResult pHostResult, String pParam,
			PTypeProvider pProvider) {
		return this.TheChecker;
	}
	
	final protected void setChecker(Checker pChecker) {
		if(this.TheChecker != null) return;
		this.TheChecker = pChecker;
	} 
	
	/**{@inheritDoc}*/ @Override public Object doCompile(ParseResult pThisResult, int pEntryIndex, String pParam,
			CompilationContext pCContext, PTypeProvider pProvider) {
		
		String ErrMsg = String.format("%s%s\n",
				this.ErrMessage,
				(pParam == null)?"":String.format(" (%s)", pParam),
				pCContext.getLocationAsString(pThisResult.getStartPositionOf(pEntryIndex)));
		
		if(pCContext != null) pCContext.reportError(ErrMsg, null);
		if(this.IsFatal) throw new RuntimeException("FATAL ERROR! The compilation cannot be continued: " + ErrMsg);
		return null;
	}

}
