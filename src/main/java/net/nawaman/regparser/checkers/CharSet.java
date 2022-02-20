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

package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.EscapeHelpers.escapeOfRegParser;

import java.util.HashSet;

/**
 * Checker form a set of character (represented by a string)
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class CharSet extends CharChecker {
	
	private static final long serialVersionUID = 2165464135465416515L;
	
	public final String set;
	
	public CharSet(String set) {
		this.set = (set == null)
		         ? ""
		         : set;
	}
	
	/** Checks of the char c is in this char checker */
	@Override
	public boolean inSet(char c) {
		return (set.indexOf(c) != -1);
	}
	
	@Override
	public final Boolean isDeterministic() {
		return true;
	}
	
	@Override
	public String toString() {
		var escapeOfRegParser = escapeOfRegParser(this.set);
		return "[" + escapeOfRegParser + "]";
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharSet))
			return false;
		
		var chars = new HashSet<Character>();
		for (var chr : set.toCharArray()) {
			chars.add(chr);
		}
		for (var ch : ((CharSet)O).set.toCharArray()) {
			if (!chars.contains(ch))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return "CharSet".hashCode() + set.hashCode();
	}
	
}
