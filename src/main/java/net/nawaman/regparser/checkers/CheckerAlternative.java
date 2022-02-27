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

import static java.util.Objects.hash;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.nawaman.regparser.AsChecker;
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
	
	public static class Builder implements AsChecker {
		private final List<AsChecker> checkers = new ArrayList<>();
		
		private boolean isDeterministic = true;
		
		public Builder or(AsChecker checker) {
			if ((checker != null) && !checker.isDeterministic()) {
				isDeterministic = false;
			}
			
			checkers.add(checker);
			return this;
		}
		
		public CheckerAlternative build() {
			var array = checkers.stream()
			          .filter(Objects::nonNull)
			          .toArray(AsChecker[]::new);
			return new CheckerAlternative(false, array);
		}
		
		public CheckerAlternative orDefault(AsChecker checker) {
			if ((checker != null) && !checker.isDeterministic()) {
				isDeterministic = false;
			}
			
			boolean hasDefault = (checker != null);
			checkers.add(checker);
			var array = checkers.stream()
			          .filter(Objects::nonNull)
			          .toArray(AsChecker[]::new);
			return new CheckerAlternative(hasDefault, array);
		}
		
		@Override
		public Checker asChecker() {
			return build();
		}
		
		@Override
		public final Boolean isDeterministic() {
			return isDeterministic;
		}
	}
	
	public static CheckerAlternative.Builder of(AsChecker checker) {
		return new CheckerAlternative.Builder()
				.or(checker);
	}
	
	public static CheckerAlternative.Builder either(AsChecker checker) {
		return new CheckerAlternative.Builder()
				.or(checker);
	}
	
	private final Checker   defaultChecker;
	private final Checker[] checkers;
	private final boolean   isDeterministic;
	
	/** Constructs a char set */
	public CheckerAlternative(AsChecker... checkers) {
		this(false, checkers);
	}
	
	/** Constructs a char set */
	public CheckerAlternative(boolean hasDefault, AsChecker ... checkers) {
		// Combine if one of them is alternative
		
		var     list            = new ArrayList<Checker>();
		int     lastIndex       = checkers.length - (hasDefault ? 1 : 0);
		boolean isDeterministic = true;
		for (int i = 0; i < lastIndex; i++) {
			var checker = checkers[i].asChecker();
			if (checker == null)
				continue;
			
			isDeterministic &= checker.isDeterministic();
			
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
		
		var defaultValue = hasDefault ? checkers[checkers.length - 1].asChecker() : null;
		if (defaultValue != null) {
			defaultValue = defaultValue.optimize();
		}
		this.defaultChecker = defaultValue;
		
		this.isDeterministic = isDeterministic;
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider) {
		return startLengthOf(text, offset, typeProvider, null);
	}
	
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult parseResult) {
		int maxLength = -1;
		for (int i = checkers.length; --i >= 0;) {
			var checker = checkers[i];
			int length  = checker.startLengthOf(text, offset, typeProvider, parseResult);
			if (length != -1) {
				if (length >= maxLength) {
					maxLength = length;
				}
			}
		}
		if (maxLength != -1)
			return maxLength;
		
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
	
	public Stream<Checker> checkers() {
		return Stream.of(checkers);
	}
	
	public void forEachInReverse(Consumer<Checker> action) {
		for (int i = checkers.length; --i >= 0;) {
			var checker = checkers[i];
			action.accept(checker );
		}
	}
	
	@Override
	public final Boolean isDeterministic() {
		return isDeterministic;
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
	
	private int hashCode = 0;
	
	@Override
	public int hashCode() {
		if (hashCode != 0) {
			return hashCode;
		}
		
		hashCode = hash(this.getClass(), defaultChecker) * 31
				 + hash((Object[])checkers);
		return hashCode;
	}
	
	@Override
	public Checker optimize() {
		return this;
	}
	
}
