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

import static java.lang.reflect.Array.getLength;
import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.HashSet;

import net.nawaman.regparser.Checker;

/**
 * Char checker that is the character is must be in all CharCheckers.
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public final class CharIntersect extends CharChecker {
	
	private static final long serialVersionUID = 2351321651321651211L;
	
	private final CharChecker[] charCheckers;
	private final boolean       isDeterministic;
	
	/** Constructs a char set */
	public CharIntersect(CharChecker... charCheckers) {
		// Combine if one of them is alternative
		
		var     checkers        = new ArrayList<CharChecker>();
		int     checkerCount    = charCheckers.length;
		boolean isDeterministic = true;
		for (int i = 0; i < checkerCount; i++) {
			var charChecker = charCheckers[i];
			if (charChecker == null)
				continue;
			
			isDeterministic &= charChecker.isDeterministic();
			
			if (charChecker instanceof CharIntersect) {
				var charIntersect   = (CharIntersect)charChecker;
				int intersectLength = charIntersect.charCheckers.length;
				for (int c = 0; c < intersectLength; c++) {
					var checker = charIntersect.charCheckers[c];
					checkers.add(checker);
				}
			} else {
				var checker = charCheckers[i];
				checkers.add(checker);
			}
		}
		// Generate the array
		this.charCheckers = new CharChecker[checkers.size()];
		for (int i = 0; i < checkers.size(); i++) {
			this.charCheckers[i] = checkers.get(i);
		}
		
		this.isDeterministic = isDeterministic;
	}
	
	@Override
	public boolean inSet(char c) {
		for (var charChecker : charCheckers) {
			if (!charChecker.inSet(c))
				return false;
		}
		return true;
	}
	
	@Override
	public final Boolean isDeterministic() {
		return isDeterministic;
	}
	
	@Override
	public String toString() {
		var buffer = new StringBuffer();
		buffer.append("[");
		int length = getLength(charCheckers);
		for (int i = 0; i < length; i++) {
			var checker = charCheckers[i];
			if (checker == null)
				continue;
			
			if (i != 0) {
				buffer.append("&&");
			}
			buffer.append(checker.toString());
		}
		buffer.append("]");
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharIntersect))
			return false;
		
		var checkers = new HashSet<CharChecker>();
		for (var charChecker : charCheckers) {
			checkers.add(charChecker);
		}
		for (var charChecker : ((CharIntersect)O).charCheckers) {
			if (!checkers.contains(charChecker))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return "CharIntersect".hashCode() + hash((Object[])charCheckers);
	}
	
	@Override
	public Checker optimize() {
		if (charCheckers.length == 1) {
			return charCheckers[0];
		}
		return this;
	}
	
}
