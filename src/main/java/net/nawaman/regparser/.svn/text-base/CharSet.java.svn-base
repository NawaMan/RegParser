/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

import java.util.*;

/**
 * Checker form a set of character (represented by a string)
 *
 * @author Nawapunth Manusitthipol
 */
public class CharSet extends CharChecker {
	
	static private final long serialVersionUID = 2165464135465416515L;
	
	public CharSet(String pSet) {
		this.Set = (pSet == null)?"":pSet;
	}
	
	String Set;
	
	/** Checks of the char c is in this char checker */
	@Override public boolean inSet(char c) {
		return (this.Set.indexOf(c) != -1);
	}
	
	@Override public String toString() {
		return "[" + RPCompiler_ParserTypes.escapeOfRegParser(this.Set) + "]";
	}
	@Override public boolean equals(Object O) {
		if(O == this) return true;
		if(!(O instanceof CharSet)) return false;
		HashSet<Character> CCs = new HashSet<Character>();
		for(Character CC : this.Set.toCharArray()) CCs.add(CC);
		for(Character CC : ((CharSet)O).Set.toCharArray()) {
			if(!CCs.contains(CC)) return false;
		}
		return true;
	}
	
	@Override public int hashCode() {
		return "CharSet".hashCode() + this.Set.hashCode();
	}
}