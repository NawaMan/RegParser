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
 * Checker for checking a sequence of character or a word
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
@SuppressWarnings("serial")
public class WordChecker implements Checker {
	
	static public final WordChecker EmptyWord = new WordChecker();
	
	private WordChecker() {
		this.Word = "";
	}
	public WordChecker(String pWord) {
		if(pWord          == null) throw new NullPointerException();
		if(pWord.length() == 0) throw new IllegalArgumentException();
		this.Word = pWord;
	}
	
	String Word;
	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
		return this.getStartLengthOf(S, pOffset, pProvider, null);
	}
	
	/**
	 * Returns the length of the match if the string S starts with this checker.<br />
	 * @param	S is the string to be parse
	 * @param	pOffset the starting point of the checking
	 * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
	 * @return	the length of the match or -1 if the string S does not start with this checker
	 */
	@Override public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
		return S.toString().startsWith(this.Word, pOffset)?this.Word.length():-1;
	}
	
	@Override public String toString() {
		return RPCompiler_ParserTypes.escapeOfRegParser(this.Word);
	}
	@Override public boolean equals(Object O) {
		if(O == this) return true;
		if(!(O instanceof WordChecker)) return false;
		return this.Word.equals(((WordChecker)O).Word);
	}
	
	/** Return the optimized version of this RegParser */
	@Override public Checker getOptimized() { return this; }
	
}