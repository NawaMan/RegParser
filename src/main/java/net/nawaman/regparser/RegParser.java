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
package net.nawaman.regparser;

import static java.util.Objects.requireNonNullElse;
import static net.nawaman.regparser.utils.Util.prependArray;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.nawaman.regparser.result.ParseResult;

/**
 * The regular parser
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RegParser implements Checker, Serializable {
	
	private static final long serialVersionUID = 5789453645656854655L;
	
	/** Returns the empty array of RegParsers */
	public static final RegParser[] EmptyRegParserArray = new RegParser[0];
	public static boolean           isDebugMode         = false;
	public static PrintStream       DebugPrintStream    = null;
	
	public static RegParserBuilder newRegParser() {
		return new RegParserBuilder();
	}
	
	public static RegParser newRegParser(AsRegParserEntry[] entries) {
		return newRegParser((ParserTypeProvider)null, entries);
	}
	
	public static RegParser newRegParser(AsRegParserEntry entry, AsRegParserEntry... moreEntries) {
		var array = prependArray(AsRegParserEntry.class, entry, moreEntries);
		return newRegParser((ParserTypeProvider)null, array);
	}
	
	public static RegParser newRegParser(ParserTypeProvider typeProvider, AsRegParserEntry... entries) {
		if (entries == null)
			throw new NullPointerException();
		
		var entryArray
				= Stream.of(entries)
				.filter(Objects::nonNull)
				.map(AsRegParserEntry::asRegParserEntry)
				.toArray(RegParserEntry[]::new);
		return (typeProvider == null)
				? new RegParser(entryArray)
				: new RegParserWithDefaultTypeProvider(entryArray, typeProvider);
	}
	
	public static RegParser newRegParser(List<RegParserEntry> entries) {
		return RegParser.newRegParser(null, entries);
	}
	
	public static RegParser newRegParser(ParserTypeProvider typeProvider, List<RegParserEntry> entries) {
		var entryArray 
				= entries.stream()
				.filter(Objects::nonNull)
				.toArray(RegParserEntry[]::new);
		return (typeProvider == null)
				? new RegParser(entryArray)
				: new RegParserWithDefaultTypeProvider(entryArray, typeProvider);
	}
	
	public static RegParser newRegParser(ParserTypeProvider typeProvider, ParserType parserType) {
		return new RegParserBuilder()
				.typeProvider(typeProvider)
				.entry(parserType)
				.build();
	}
	
	public static RegParser newRegParser(ParserTypeProvider typeProvider, ParserTypeRef parserTypeRef) {
		return new RegParserBuilder()
				.typeProvider(typeProvider)
				.entry(parserTypeRef)
				.build();
	}
	
	public static RegParser newRegParser(String name, AsChecker checker) {
		return new RegParserBuilder()
				.entry(name, checker)
				.build();
	}
	
	public static RegParser newRegParser(String name, ParserType parserType) {
		return new RegParserBuilder().
				entry(name, parserType)
				.build();
	}
	
	public static RegParser newRegParser(String name, ParserTypeRef typeRef) {
		return new RegParserBuilder()
				.entry(name, typeRef)
				.build();
	}
	
	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compileRegParser(String regParserText) {
		return RegParser.compile(null, regParserText);
	}
	
	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compileRegParser(ParserTypeProvider typeProvider, String regParserText) {
		return RegParser.compile(typeProvider, regParserText);
	}
	
	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compile(String regParserText) {
		return RegParser.compile(null, regParserText);
	}
	
	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compile(ParserTypeProvider typeProvider, String regParserText) {
		return RegParserCompiler.compile(typeProvider, regParserText);
	}
	
	// Data ------------------------------------------------------------------------------------------------------------
	
	private final RegParserEntry[] entries;
	
	private boolean isOptimized     = false;
	private boolean isDeterministic = true;
	
	RegParser(RegParserEntry[] entries) {
		this.entries         = requireNonNullElse(entries, RegParserEntry.EmptyRegParserEntryArray);
		this.isOptimized     = false;
		this.isDeterministic = isDeterministic(entries);
	}
	
	RegParser(boolean isOptimized, RegParserEntry[] entries) {
		this.entries         = requireNonNullElse(entries, RegParserEntry.EmptyRegParserEntryArray);
		this.isOptimized     = true;
		this.isDeterministic = isDeterministic(entries);
	}
	
	private boolean isDeterministic(RegParserEntry[] entries) {
		for (var entry : entries) {
			if (entry == null) {
				continue;
			}
			if (Boolean.FALSE.equals(entry.isDeterministic())) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public final Boolean isDeterministic() {
		return isDeterministic;
	}
	
	public Stream<RegParserEntry> entries() {
		return Stream.of(entries);
	}
	
	/** Returns the number of RegParser entry this RegParser composes of */
	public int getEntryCount() {
		return this.entries.length;
	}
	
	public RegParserEntry getEntryAt(int pIndex) {
		if ((pIndex < 0) || (pIndex >= this.getEntryCount()))
			return null;
		
		return this.entries[pIndex];
	}
	
	public RegParser attachDefaultTypeProvider(ParserTypeProvider typeProvider) {
		return RegParserWithDefaultTypeProvider.attachDefaultTypeProvider(this, typeProvider);
	}
	
	// Default TypePackage ---------------------------------------------------------------------------------------------
	
	ParserTypeProvider getDefaultTypeProvider() {
		return null;
	}
	
	// Public services -------------------------------------------------------------------------------------------------
	
	// Parse - as far as it can go.
	
	/** Returns the the match if the text is start with a match or -1 if not */
	public ParseResult parse(CharSequence text) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, 0, 0, 0, null, null, null, null, 0);
		if (parseResult == null)
			return null;
		
		parseResult.collapse(null);
		return parseResult;
	}
	
	/** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
	public ParseResult parse(CharSequence text, int offset) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, offset, 0, 0, null, null, null, null, 0);
		if (parseResult == null)
			return null;
		
		parseResult.collapse(null);
		return parseResult;
	}
	
	/** Returns the the match if the text is start with a match or -1 if not */
	public ParseResult parse(CharSequence text, ParserTypeProvider typeProvider) {
		var parseResult = parse(text, 0, 0, 0, null, typeProvider, null, null, 0);
		if (parseResult == null)
			return null;
		
		parseResult.collapse(typeProvider);
		return parseResult;
	}
	
	/** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
	public ParseResult parse(CharSequence text, int offset, ParserTypeProvider typeProvider) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, offset, 0, 0, null, typeProvider, null, null, 0);
		if (parseResult == null)
			return null;
		
		parseResult.collapse(typeProvider);
		return parseResult;
	}
	
	// Match - to the end or fail.
	
	/** Returns the match if the text is start with a match (from start to the end) or -1 if not */
	public ParseResult match(CharSequence text) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, 0, 0, 0, null, null, null, null, 0);
		if (parseResult == null)
			return null;
		
		if (parseResult.endPosition() != text.length())
			return null;
		
		parseResult.collapse(null);
		return parseResult;
	}
	
	/** Returns the match if the text is start with a match (from start to the endPosition) or -1 if not */
	public ParseResult match(CharSequence text, int offset, int endPosition) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, offset, 0, 0, null, null, null, null, 0);
		if (parseResult == null)
			return null;
		
		if (parseResult.endPosition() != text.length())
			return null;
		
		parseResult.collapse(null);
		return parseResult;
	}
	
	/** Returns the match if the text is start with a match (from start to the end) or -1 if not */
	public ParseResult match(CharSequence text, ParserTypeProvider typeProvider) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, 0, 0, 0, null, typeProvider, null, null, 0);
		if (parseResult == null)
			return null;
		
		if (parseResult.endPosition() != text.length())
			return null;
		
		parseResult.collapse(typeProvider);
		return parseResult;
	}
	
	/** Returns the match if the text is start with a match (from start to the endPosition) or -1 if not */
	public ParseResult match(CharSequence text, int offset, int endPosition, ParserTypeProvider typeProvider) {
		if (text == null)
			return null;
		
		var parseResult = parse(text, offset, 0, 0, null, typeProvider, null, null, 0);
		if (parseResult == null)
			return null;
		
		if (parseResult.endPosition() != text.length())
			return null;
		
		parseResult.collapse(typeProvider);
		return parseResult;
	}
	
	/** Returns the length of the match if the text is start with a match or -1 if not */
	ParseResult parse(
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        result,
			ParserTypeProvider typeProvider,
			ParserType         type,
			String             parameter,
			int                tabCount) {
		return RegParserSolver.startParse(entries , text, offset, index, times, result, typeProvider, type, parameter, tabCount);
	}
	
	/** Return the optimized version of this Checker */
	public Checker optimize() {
		if (isOptimized)
			return this;
		
		if (entries.length == 1) {
			var entry = entries[0];
			if ((entry.name() == null)
			 && (entry.typeRef() == null)
			 && ((entry.quantifier() == null) || entry.quantifier().isOne_Default())) {
				return entry.checker();
			}
		}
		
		return RegParserOptimizer.optimize(this);
	}
	
	// To Satisfy Checker ----------------------------------------------------------------------------------------------
	
	/**
	 * Returns the length of the match if the string S starts with this checker.
	 * <br />
	 * 
	 * @param  text    is the string to be parse
	 * @param  offset  the starting point of the checking
	 * @return         the length of the match or -1 if the string S does not start with this checker
	 */
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider provider) {
		return this.startLengthOf(text, offset, provider, null);
	}
	
	/**
	 * Returns the length of the match if the string S starts with this
	 * checker.<br />
	 * 
	 * @param  text    is the string to be parse
	 * @param  offset  the starting point of the checking
	 * @param  result  the parse result of the current parsing.
	 *                   This is only available when this checker is called from a RegParser
	 * @return  the length of the match or -1 if the string S does not start with this checker
	 */
	@Override
	public int startLengthOf(CharSequence text, int offset, ParserTypeProvider typeProvider, ParseResult result) {
		if (text == null)
			return -1;
		
		if (result == null) {
			var parseResult = parse(text, offset, 0, 0, null, typeProvider, null, null, 0);
			if (parseResult == null)
				return -1;
			
			int endPosition = parseResult.endPosition();
			return endPosition - offset;
		}
		
		var attemptResult = parse(text, offset, 0, 0, result, typeProvider, null, null, 0);
		if (attemptResult == null) {
			int rawEntryCount = result.rawEntryCount();
			// Recover what may have been added in the fail attempt
			result.reset(rawEntryCount);
			return -1;
		}
		
		int endPosition = result.endPosition();
		return endPosition - offset;
	}
	
	@Override
	public String toString() {
		var buffer = new StringBuffer();
		for (var entry : entries) {
			buffer.append(entry.toString());
		}
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		
		if (!(other instanceof RegParser))
			return false;
		
		return Arrays.equals(this.entries, ((RegParser)other).entries);
	}
	
	private int hashCode = 0;
	
	@Override
	public int hashCode() {
		if (hashCode != 0) {
			return hashCode;
		}
		
		hashCode = toString().hashCode();
		return hashCode;
	}
	
}
