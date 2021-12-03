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
 * package. You can inform me via nawa<at>nawaman<dot>net.
 * 
 * The project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the 
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * ---------------------------------------------------------------------------------------------------------------------
 */

package net.nawaman.regparser.checkers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;

import net.nawaman.regparser.Checker;

/**
 * Char checker that is the character is must be in at least of the CharCheckers.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharUnion extends CharChecker {
	
	private static final long serialVersionUID = 2263513546518947854L;
	
	private final CharChecker[] charCheckers;
	
	/** Constructs a char set */
	public CharUnion(CharChecker... charCheckers) {
		// Combine if one of them is alternative
		
		var list = new ArrayList<CharChecker>();
		for (int i = 0; i < charCheckers.length; i++) {
			var charChecker = charCheckers[i];
			if (charChecker == null)
				continue;
			
			if (charChecker instanceof CharUnion) {
				var charUnion = (CharUnion)charChecker;
				for (int c = 0; c < charUnion.charCheckers.length; c++) {
					list.add(charUnion.charCheckers[c]);
				}
			} else {
				list.add(charCheckers[i]);
			}
		}
		// Generate the array
		int checkerCount = list.size();
		this.charCheckers = new CharChecker[checkerCount];
		for (int i = 0; i < checkerCount; i++) {
			this.charCheckers[i] = list.get(i);
		}
	}
	
	/** Checks of the char c is in this char checker */
	@Override
	public boolean inSet(char c) {
		for (var charChecker : charCheckers) {
			if (charChecker.inSet(c))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		var buffer = new StringBuffer();
		buffer.append("[");
		int length = Array.getLength(charCheckers);
		for (int i = 0; i < length; i++) {
			var checker = charCheckers[i];
			if (checker == null)
				continue;
			
			buffer.append(checker.toString());
		}
		buffer.append("]");
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharUnion))
			return false;
		
		var set = new HashSet<CharChecker>();
		for (var charChecker : this.charCheckers) {
			set.add(charChecker);
		}
		for (var charChecker : ((CharUnion)O).charCheckers) {
			if (!set.contains(charChecker))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int h = "CharUnion".hashCode();
		int length = charCheckers.length;
		for (int i = 0; i < length; i++) {
			h += charCheckers[i].hashCode();
		}
		
		return h;
	}
	
	/** Return the optimized version of this Checker */
	@Override
	public Checker optimize() {
		if (charCheckers.length == 1)
			return charCheckers[0];
		
		return this;
	}
}
