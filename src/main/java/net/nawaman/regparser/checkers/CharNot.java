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

import static java.util.Objects.requireNonNull;

import net.nawaman.regparser.Checker;

/**
 * The char set of a opposite of a given char set.
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class CharNot extends CharChecker {
	
	private static final long serialVersionUID = 5313543126135165121L;
	
	private final CharChecker charChecker;
	
	/** Construct a character range */
	public CharNot(CharChecker charChecker) {
		requireNonNull(charChecker);
		this.charChecker = (charChecker instanceof CharNot)
		                 ? ((CharNot)charChecker).charChecker
		                 : charChecker;
	}
	
	/** Checks of the char c is in this char checker */
	@Override
	public boolean inSet(char c) {
		return !charChecker.inSet(c);
	}
	
	@Override
	public boolean equals(Object O) {
		if (O == this)
			return true;
		
		if (!(O instanceof CharNot))
			return false;
		
		return charChecker.equals(((CharNot)O).charChecker);
	}
	
	@Override
	public int hashCode() {
		return "CharNot".hashCode() + charChecker.hashCode();
	}
	
	@Override
	public String toString() {
		return "[^" + charChecker.toString() + "]";
	}
	
	/** Return the optimized version of this Checker */
	@Override
	public Checker optimize() {
		if (charChecker instanceof CharNot)
			return ((CharNot)charChecker).charChecker;
		
		return this;
	}
	
}
