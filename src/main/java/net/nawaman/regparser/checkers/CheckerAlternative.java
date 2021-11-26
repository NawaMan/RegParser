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

import java.util.ArrayList;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.result.ParseResult;

/**
 * Checker for alternative values.
 * 
 * If HasDefault is false (which is the default), all sub checkers are checked against the CharSequence and the longest
 *   match length will be accepted as the match value. The checking is done in reverse order so the first longest match
 *   from the last Checker is accepted as match.
 * If HasDefault is true, all Checkers except the last will be checked. If and only if no match is found, the last
 *   Checker will be used.
 * For example:  
 *     "5" / (($Low:~[0-5]~) |  ($High:~[5-9]~)) will match as '$High'
 *     "5" / (($Low:~[0-5]~) || ($High:~[5-9]~)) will match as '$Low'
 * 
 * NOTE: The reverse order used in CheckerAlternative is only because of a historical reason. This will likely to
 *         changed in the later version.
 * NOTE: In the later version, multiple default checker should also be implemented as it used much more often than
 *         first expected. Have that built-in should improve performance.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CheckerAlternative implements Checker {
	
	private static final long serialVersionUID = 2146515415886541851L;
	
	public final Checker   defaultChecker;
	public final Checker[] checkers;
	
	/** Constructs a char set */
	public CheckerAlternative(Checker... checkers) {
		this(false, checkers);
	}
	
	/** Constructs a char set */
	public CheckerAlternative(boolean hasDefault, Checker... checkers) {
		// Combine if one of them is alternative
		
		var list      = new ArrayList<Checker>();
		int lastIndex = checkers.length - (hasDefault ? 1 : 0);
		for (int i = 0; i < lastIndex; i++) {
			var checker = checkers[i];
			if (checker == null)
				continue;
			
			if ((checker instanceof CheckerAlternative) && !((CheckerAlternative)checker).hasDefault()) {
				var checkerAlternative = (CheckerAlternative)checker;
				for (int c = 0; c < checkerAlternative.checkers.length; c++) {
					list.add(checkerAlternative.checkers[c]);
				}
			} else {
				list.add(checker);
			}
		}
		
		// Generate the array
		this.checkers = new Checker[list.size()];
		for (int i = 0; i < list.size(); i++) {
			var checker = list.get(i);
			if (checker instanceof RegParser) {
				checker = ((RegParser)checker).optimize();
			}
			this.checkers[i] = checker;
		}
		
		var defaultValue = hasDefault ? checkers[checkers.length - 1] : null;
		if (defaultValue != null) {
			defaultValue = defaultValue.optimize();
		}
		this.defaultChecker = defaultValue;
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
		return startLengthOf(text, offset, typeProvider, null);
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
		for (int i = checkers.length; --i >= 0;) {
			var checker = checkers[i];
			int index   = checker.startLengthOf(text, offset, typeProvider, parseResult);
			if (index != -1)
				return index;
		}
		return (defaultChecker != null)
		        ? defaultChecker.startLengthOf(text, offset, typeProvider, parseResult)
		        : -1;
	}
	
	public boolean hasDefault() {
		return defaultChecker != null;
	}
	
	public Checker defaultChecker() {
		return defaultChecker;
	}
	
	// Object ----------------------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		var buffer = new StringBuffer();
		buffer.append("(");
		if (checkers != null) {
			for (int i = 0; i < checkers.length; i++) {
				var checker = checkers[i];
				if (checker == null)
					continue;
				
				if (i != 0) {
					buffer.append("|");
				}
				buffer.append(checker.toString());
			}
		}
		if (defaultChecker != null) {
			buffer.append("||").append(this.defaultChecker);
		}
		buffer.append(")");
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CheckerAlternative))
			return false;
		
		if (checkers.length != ((CheckerAlternative)O).checkers.length)
			return false;
		
		for (int i = checkers.length; --i >= 0;) {
			if (!checkers[i].equals(((CheckerAlternative)O).checkers[i]))
				return false;
		}
		return (defaultChecker != null)
		        ? defaultChecker.equals(((CheckerAlternative)O).defaultChecker)
		        : true;
	}
	
	@Override
	public int hashCode() {
		int h = "CheckerAlternative".hashCode();
		for (int i = 0; i < checkers.length; i++) {
			h += checkers[i].hashCode();
		}
		
		return h + ((defaultChecker != null) ? defaultChecker.hashCode() : 0);
	}
	
	@Override
	public Checker optimize() {
		return this;
	}
	
}
