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

import java.io.Serializable;

/**
 * A basic building block for RegParser.
 * 
 * Checker can check if a given CharSequence starts with characters in the right sequence. If match, the length if the
 *   match is returned; Otherwise, -1 is returned. 
 *
 * @author Nawapunth Manusitthipol
 */
public interface Checker extends Serializable {
	
	/** Returns the empty array of Checkers */
	static public final Checker[] EmptyCheckerArray = new Checker[0];
	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider);
	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult);
	
	/** Return the optimized version of this Checker */
	public Checker getOptimized();
	
}
