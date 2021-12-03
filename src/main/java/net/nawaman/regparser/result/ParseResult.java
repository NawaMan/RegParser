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
package net.nawaman.regparser.result;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;
import static net.nawaman.regparser.utils.Util.dashes;
import static net.nawaman.regparser.utils.Util.indents;
import static net.nawaman.regparser.utils.Util.spaces;
import static net.nawaman.regparser.utils.Util.tabs;
import static net.nawaman.regparser.utils.Util.textWidth;
import static net.nawaman.regparser.utils.Util.zeros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.result.entry.ParseResultEntry;
import net.nawaman.regparser.result.entry.ParseResultEntryWithParserEntryAndSubResult;
import net.nawaman.regparser.result.entry.ParseResultEntryWithSubResult;
import net.nawaman.regparser.types.ErrorParserType;
import net.nawaman.regparser.utils.IStream;
import net.nawaman.regparser.utils.Util;

/**
 * The result of the parsing
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParseResult implements Serializable {
	
	private static final long serialVersionUID = 4543543556454654354L;
	
	
	public static RootParseResult newResult(int startPosition, CharSequence originalText) {
		return new RootParseResult(startPosition, originalText);
	}
	
	public static TemporaryParseResult newResult(ParseResult first) {
		return new TemporaryParseResult(first);
	}
	
	public static ParseResultNode newResult(int startPosition, ParseResult parseResult) {
		return new ParseResultNode(startPosition, parseResult);
	}
	
	// == Static utility ==
	
	public static String nameOf(RegParserEntry entry) {
		return nameOf(entry, null);
	}
	
	public static String nameOf(RegParserEntry entry, String defaultValue) {
		return ((entry != null) && (entry.name() != null))
		        ? entry.name()
		        : defaultValue;
	}
	
	public static String typeNameOf(ParseResultEntry entry) {
		return typeNameOf(entry, null);
	}
	
	public static String typeNameOf(ParseResultEntry entry, String defaultValue) {
		if (entry != null) {
			var type = entry.type();
			if (type != null)
				return type.name();
			
			var typeRef = entry.typeRef();
			if (typeRef != null)
				return typeRef.name();
		}
		return defaultValue;
	}
	
	//== Instance ==
	
	private List<ParseResultEntry> entries;
	
	/** Constructor */
	ParseResult() {
		this(null);
	}
	
	/** Constructor */
	ParseResult(List<ParseResultEntry> entries) {
		this.entries = (entries == null)
		             ? new ArrayList<>()
		             : entries.stream().collect(toList());
	}
	
	/** Duplicate this result - to be used when verifying */
	public abstract ParseResult duplicate();
	
	/** Get the original text as a CharSequence */
	public abstract CharSequence originalCharSequence();
	
	/** Get the original text as a string */
	public final String originalText() {
		return this.originalCharSequence().toString();
	}
	
	// Parent --------------------------------------------------------------------------------------
	
	protected ParseResult parent() {
		return null;
	}
	
	public final ParseResult root() {
		ParseResult result = this;
		ParseResult parent = null;
		while ((parent = result.parent()) != null) {
			result = parent;
		}
		return result;
	}
	
	/** Returns how deep the parse result is nested */
	public final int depth() {
		int depth = 0;
		for (int i = entryCount(); --i >= 0;) {
			var subResult = subResultOf(i);
			if (subResult != null) {
				int d = subResult.depth();
				if (depth < d) {
					depth = d;
				}
			}
		}
		return depth + 1;
	}
	
	//-- Entries -------------------------------------------------------------------------------------------------------
	
	/** @return  the entries as a streams. */
	public final Stream<ParseResultEntry> entries() {
		return (entries == null)
		        ? Stream.empty()
		        : entries.stream();
	}
	
	/** @return  the entries as a list. */
	public final List<ParseResultEntry> entryList() {
		var stream = (entries == null)
		           ? Stream.<ParseResultEntry>empty()
		           : entries.stream();
		return stream.collect(toList());
	}
	
	/** Returns the number of the result entry - may be overridden by sub classes. */
	public int entryCount() {
		return rawEntryCount();
	}
	
	/** Returns the entry list size -- Used internally and cannot be overridden. */
	public final int rawEntryCount() {
		return (entries == null)
		        ? 0
		        : entries.size();
	}
	
	/** Returns a result entry at the index. or null if the index of out of bound. */
	public ParseResultEntry entryAt(int index) {
		if ((index < 0)
		  || index >= entryCount())
			return null;
		
		return entries.get(index);
	}
	
	/** Returns the nested result entry at the indexes. or null if the index of out of bound. */
	public final ParseResultEntry entryAt(int firstIndex, int secondIndex, int... restIndexes) {
		try {
			var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
			int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
			              ? secondIndex
			              : restIndexes[restIndexes.length - 1];
			return result.entryAt(lastIndex);
			
		} catch (ArrayIndexOutOfBoundsException exception) {
			var errMsg = format("Error getting a result entry at [%d, %d, %s]: %s", 
			                    firstIndex,
			                    secondIndex,
			                    Util.toString(restIndexes, "", "", ", "),
			                    exception.getMessage());
			throw new RuntimeException(errMsg, exception);
		}
	}
	
	/** Returns the last match */
	public final ParseResultEntry lastEntryOf(String name) {
		int size = entries.size();
		for (int i = size; --i >= 0;) {
			var resultEntry = entries.get(i);
			var parserEntry = resultEntry.parserEntry();
			if (parserEntry != null) {
				var entryName = parserEntry.name();
				if (name.equals(entryName))
					return resultEntry;
			}
		}
		return null;
	}
	
	/** Returns the last group of continuous match */
	public final ParseResultEntry[] lastEntriesOf(String name) {
		var entryList = new ArrayList<ParseResultEntry>();
		int size = entries.size();
		for (int i = size; --i >= 0;) {
			var resultEntry = entries.get(i);
			var parserEntry = resultEntry.parserEntry();
			if ((parserEntry != null) && name.equals(parserEntry.name())) {
				entryList.add(resultEntry);
			} else if (entryList.size() > 0) {
				break;
			}
		}
		
		if (entryList.size() == 0)
			return null;
		
		var lastEntries = new ParseResultEntry[entryList.size()];
		for (int i = lastEntries.length; --i >= 0;) {
			lastEntries[i] = entryList.get(lastEntries.length - i - 1);
		}
		return lastEntries;
	}
	
	/** Returns the all the match */
	public final ParseResultEntry[] entriesOf(String name) {
		var entryList = new ArrayList<ParseResultEntry>();
		for (var resultEntry : entries) {
			var parserEntry = resultEntry.parserEntry();
			if (parserEntry != null) {
				var entryName = parserEntry.name();
				if (name.equals(entryName)) {
					entryList.add(resultEntry);
				}
			}
		}
		
		if (entryList.size() == 0)
			return null;
		
		return entryList.toArray(ParseResultEntry[]::new);
	}
	
	/** Returns the all the match */
	public final ParseResultEntry[][] allEntriesOf(String name) {
		var allEntryList  = new ArrayList<ParseResultEntry[]>();
		var eachEntryList = new ArrayList<ParseResultEntry>();
		int size          = entryCount();
		for (int i = 0; i < size; i++) {
			var resultEntry = entryAt(i);
			var parserEntry = resultEntry.parserEntry();
			if ((parserEntry != null)
			  && name.equals(parserEntry.name())) {
				eachEntryList.add(resultEntry);
				
			} else if (eachEntryList.size() > 0) {
				if (eachEntryList.size() == 0)
					continue;
				
				var allEntries = new ParseResultEntry[eachEntryList.size()];
				for (int e = allEntries.length; --e >= 0;) {
					allEntries[e] = eachEntryList.get(e);
				}
				allEntryList.add(allEntries);
				eachEntryList.clear();
			}
		}
		
		if (allEntryList.size() == 0)
			return null;
		
		var allEntries = new ParseResultEntry[allEntryList.size()][];
		for (int i = allEntries.length; --i >= 0;) {
			allEntries[i] = allEntryList.get(i);
		}
		return allEntries;
	}
	
	/**
	 * FOR INTERNAL USE ONLY
	 * 
	 * Get the nested sub result up to but not the last index.
	 * 
	 * Example:
	 *   nestedSubResultABOVE(3, 2)    returns the entry #3 of this result
	 *   nestedSubResultABOVE(3, 2, 4) returns the entry #2 of the entry #3 of this result
	 * 
	 * This method is useful to get some property of a nested result.
	 * Some of property such as name, of a entry can only be obtained from its parent.
	 * 
	 * @param firstIndex   the first index.
	 * @param secondIndex  the second index.
	 * @param restIndexes  the rest of the indexes.
	 * @return             the nested sub result before the last given index.
	 */
	protected final ParseResult nestedSubResultABOVE(int firstIndex, int secondIndex, int... restIndexes) {
		try {
			var result = this;
			var entry  = result.entryAt(firstIndex);
			
			result = entry.subResult();
			if ((restIndexes == null)
			 || (restIndexes.length == 0))
				return result;
			
			entry  = result.entryAt(secondIndex);
			result = entry.subResult();
			for (int i = 0; i < (restIndexes.length - 1); i++) {
				int index = restIndexes[i];
				result = result.entryAt(index).subResult();
			}
			return result;
			
		} catch (ArrayIndexOutOfBoundsException exception) {
			var errMsg = format("Error getting a result entry at [%d, %d, %s]: %s", 
			                    firstIndex,
			                    secondIndex,
			                    Util.toString(restIndexes, "", "", ", "),
			                    exception.getMessage());
			throw new RuntimeException(errMsg, exception);
		}
	}
	
	//-- Easy to use utilities methods ---------------------------------------------------------------------------------
	// These methods allow a short method name but with the assumption that the accessing name 
	//     refers to the last match only.
	// Some of these method are multiple access counterpart (added for completion) and the name
	//     access refers to all match
	
	//-- Text ----------------------------------------------------------------------------------------------------------
	
	/** Get the match text */
	public final String text() {
		var orgText = originalCharSequence();
		// Ensure proper range
		int start = startPosition();
		int end   = endPosition();
		if ((orgText == null)
		 || (start > orgText.length())
		 || (start < 0))
			return null;
		
		if ((end > orgText.length())
		 || (end < 0))
			return null;
		
		// Quick Return
		if (start == end)
			return "";
		
		// Normal Return
		var sequence = orgText.subSequence(start, end);
		return sequence.toString();
	}
	
	/** Returns the text of an entry at the index */
	public final String textOf(int index) {
		var text = originalCharSequence();
		if (text == null)
			return null;
		
		int start = startPositionOf(index);
		int end   = endPositionOf(index);
		if ((start < 0)
		 || (end   < 0)
		 || (start > text.length())
		 || (end   > text.length()))
			return null;
		
		if (start == end)
			return "";
		
		return text.subSequence(start, end)
		           .toString();
	}
	
	/** Returns the start position of a sub entry at the index */
	public final String textOf(int firstIndex, int secondIndex, int... restIndexes) {
		try {
			var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
			int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
			              ? secondIndex
			              : restIndexes[restIndexes.length - 1];
			return result.textOf(lastIndex);
			
		} catch (ArrayIndexOutOfBoundsException exception) {
			var errMsg = format("Error getting a result entry at [%d, %d, %s]: %s",
			                    firstIndex,
			                    secondIndex,
			                    Util.toString(restIndexes, "", "", ", "),
			                    exception.getMessage());
			throw new RuntimeException(errMsg, exception);
		}
	}
	
	/** Get text result of the last match */
	public final String textOf(String name) {
		int lastIndex = indexOf(name);
		return textOf(lastIndex);
	}
	
	/** Get texts result of the last match */
	public final String[] textsOf(String name) {
		int[] indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var texts = new String[length];
		for (int i = texts.length; --i >= 0;) {
			texts[i] = textOf(indexes[i]);
		}
		return texts;
	}
	
	/** Returns the text of the the last match */
	public String lastStringOf(String name) {
		for (int i = entryCount(); --i >= 0;) {
			var entry = entryAt(i);
			if (!entry.hasParserEntry())
				continue;
			
			var parseEntry = entry.parserEntry();
			var entryName  = parseEntry.name();
			if (name.equals(entryName))
				return textOf(i);
		}
		return null;
	}
	
	/** Returns the all the match */
	public final String[][] allStringsOf(String name) {
		var arrayMatches = new ArrayList<String[]>();
		var matchers     = new ArrayList<String>();
		var orgText      = originalCharSequence();
		var prevEntry    = (ParseResultEntry)null;
		for (var entry : entries) {
			if (entry.hasParserEntry() && name.equals(entry.parserEntry().name())) {
				int start = (prevEntry == null) ? 0 : prevEntry.endPosition();
				int end   = entry.endPosition();
				var text  = orgText.subSequence(start, end).toString();
				matchers.add(text);
			} else if (matchers.size() > 0) {
				var matchStrings = matchers.toArray(String[]::new);
				arrayMatches.add(matchStrings);
				matchers.clear();
			}
			prevEntry = entry;
		}
		
		if (arrayMatches.size() == 0) {
			return null;
		}
		
		var arrayMatchStrings = new String[arrayMatches.size()][];
		for (int i = arrayMatchStrings.length; --i >= 0;) {
			arrayMatchStrings[i] = arrayMatches.get(i);
		}
		return arrayMatchStrings;
	}
	
	//-- Entry Name ----------------------------------------------------------------------------------------------------
	
	/** Returns the name of the sub entry at the indexes */
	public final String nameOf(int index) {
		var entry = entryAt(index);
		return (entry == null)
		        ? null
		        : entry.name();
	}
	
	/** Get text result of the last match */
	public final String nameOf(String name) {
		int index = indexOf(name);
		return nameOf(index);
	}
	
	/** Returns the name of the sub entry at the indexes */
	public final String nameOf(int firstIndex, int secondIndex, int... restIndexes) {
		var entry = entryAt(firstIndex, secondIndex, restIndexes);
		return (entry == null)
		        ? null
		        : entry.name();
	}
	
	/** Get texts result of the last match */
	public final String[] namesOf(String name) {
		int[] indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var names = new String[length];
		for (int i = names.length; --i >= 0;)
			names[i] = nameOf(indexes[i]);
		
		return names;
	}
	
	/** Checks if the result has entries with name */
	final boolean hasNames() {
		for (var entry : entries) {
			var parserEntry = entry.parserEntry();
			if ((parserEntry        != null)
			 && (parserEntry.name() != null))
				return true;
			
			var subResult = entry.subResult();
			if ((subResult != null)
			  && subResult.hasNames())
				return true;
		}
		return false;
	}
	
	public final boolean hasName(String name) {
		if ((name == null)
		  || name.isBlank())
			return false;
		
		for (var entry : entries) {
			var parserEntry = entry.parserEntry();
			if (parserEntry != null) {
				var entryName = parserEntry.name();
				if (name.equals(entryName))
					return true;
			}
			var subResult = entry.subResult();
			if ((subResult != null)
			  && subResult.hasName(name))
				return true;
		}
		return false;
	}
	
	/** Returns names of all result entries */
	public IStream<String> names() {
		if (entries == null)
			return IStream.empty();
		
		var names = new HashSet<String>();
		entries
		.forEach(entry -> {
			var parserEntry = entry.parserEntry();
			if (parserEntry != null) {
				var name = parserEntry.name();
				if (name != null) {
					names.add(name);
				}
			}
		});
		return IStream.forStream(names.stream());
	}
	
	//-- SubResult -----------------------------------------------------------------------------------------------------
	
	/** Returns the sub result at the index. or null if the index of out of bound. */
	public final ParseResult subResultOf(int index) {
		var entry = entryAt(index);
		return (entry == null)
		        ? null
		        : entry.subResult();
	}
	
	/** Returns the nested sub result at the indexes. or null if the index of out of bound. */
	public final ParseResult subResultOf(int firstIndex, int secondIndex, int... restIndexes) {
		var entry = entryAt(firstIndex, secondIndex, restIndexes);
		return (entry == null)
		        ? null
		        : entry.subResult();
	}
	
	/** Get sub result of the last match */
	public final ParseResult subResultOf(String name) {
		var entry = lastEntryOf(name);
		return ((entry == null) || !entry.hasSubResult())
		        ? null
		        : entry.subResult();
	}
	
	/** Get subs result of the last match */
	public final ParseResult[] subResultsOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new ParseResult[0];
		
		var PRs = new ParseResult[length];
		for (int i = PRs.length; --i >= 0;) {
			int index = indexes[i];
			PRs[i] = subResultOf(index);
		}
		return PRs;
	}
	
	//-- Type Name -----------------------------------------------------------------------------------------------------
	
	/** Returns the type name of the sub entry at the indexes */
	public final String typeNameOf(int firstIndex, int secondIndex, int... restIndexes) {
		var entry = entryAt(firstIndex, secondIndex, restIndexes);
		return (entry == null)
		        ? null
		        : entry.typeName();
	}
	
	/** Get Type name of the last match */
	public final String typeNameOf(int I) {
		var entry = entryAt(I);
		if (entry == null)
			return null;
		
		var type = entry.type();
		return (type != null)
		        ? type.name()
		        : entry.typeName();
	}
	
	/** Get Type name of the last match */
	public final String typeNameOf(String name) {
		int index = indexOf(name);
		return typeNameOf(index);
	}
	
	/** Get subs result of the last match */
	public final String[] typeNamesOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var typeNames = new String[length];
		for (int i = typeNames.length; --i >= 0;) {
			int index = indexes[i];
			typeNames[i] = typeNameOf(index);
		}
		return typeNames;
	}
	
	//-- Type ----------------------------------------------------------------------------------------------------------
	
	/** Checks if the result has entries with name */
	final boolean hasTypes() {
		for (var entry : entries) {
			var parserEntry = entry.parserEntry();
			if (parserEntry != null) {
				if ((parserEntry.type()    != null)
				 || (parserEntry.typeRef() != null))
					return true;
			}
			var subResult = entry.subResult();
			if ((subResult != null)
			  && subResult.hasNames())
				return true;
		}
		return false;
	}
	
	/** Get Type of the last match */
	public final ParserType typeOf(int index, ParserTypeProvider typeProvider) {
		var entry = entryAt(index);
		if (entry == null)
			return null;
		
		var type = entry.type();
		if (type != null)
			return type;
		
		var typeName = entry.typeName();
		if (typeName == null)
			return null;
		
		if (typeProvider == null)
			return null;
		
		return typeProvider.type(typeName);
	}
	
	/** Get Type of the last match */
	public final ParserType typeOf(String name, ParserTypeProvider typeProvider) {
		int index = indexOf(name);
		return typeOf(index, typeProvider);
	}
	
	/** Get subs result of the last match */
	public final ParserType[] typesOf(String name, ParserTypeProvider typeProvider) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new ParserType[0];
		
		var types = new ParserType[length];
		for (int i = types.length; --i >= 0; ) {
			int index = indexes[i];
			types[i] = typeOf(index, typeProvider);
		}
		return types;
	}
	
	//-- Parameter -----------------------------------------------------------------------------------------------------
	
	/** Returns the type reference parameters of the sub entry at the index */
	public final String parameterOf(int index) {
		var entry = entryAt(index);
		return (entry == null)
		        ? null
		        : entry.parameter();
	}
	
	/** Returns the type reference parameters of the sub entry at the index */
	public final String parameterOf(int firstIndex, int secondIndex, int... restIndexes) {
		var entry = entryAt(firstIndex, secondIndex, restIndexes);
		return (entry == null)
		        ? null
		        : entry.parameter();
	}
	
	/** Get Type name of the last match */
	public final String parameterOf(String name) {
		int index = indexOf(name);
		return parameterOf(index);
	}
	
	/** Get subs result of the last match */
	public final String[] parametersOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var parameters = new String[length];
		for (int i = parameters.length; --i >= 0;) {
			int index = indexes[i];
			parameters[i] = parameterOf(index);
		}
		return parameters;
	}
	
	//-- Index ---------------------------------------------------------------------------------------------------------
	
	/** Returns the index of the last entry that has the same name with the given name */
	public final int indexOf(String name) {
		for (int i = entries.size(); --i >= 0;) {
			var entry       = entries.get(i);
			var parserEntry = entry.parserEntry();
			if (parserEntry != null) {
				var entryName = parserEntry.name();
				if (name.equals(entryName)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/** Returns the all indexes of the result entries that has the same name with the given name */
	public final int[] indexesOf(String name) {
		var indexes = new ArrayList<Integer>();
		for (int i = 0; i < entries.size(); i++) {
			var entry       = entries.get(i);
			var parserEntry = entry.parserEntry();
			if ((parserEntry != null)
			  && name.equals(parserEntry.name())) {
				indexes.add(i);
			}
		}
		if (indexes.size() == 0)
			return null;
		
		return indexes.stream()
		        .mapToInt(Integer::intValue)
		        .toArray();
	}
	
	/** Returns the all the match */
	public final int[][] allIndexesOf(String name) {
		var allMatches = new ArrayList<int[]>();
		var matches    = new ArrayList<Integer>();
		for (int i = 0; i < this.entryCount(); i++) {
			var entry       = this.entryAt(i);
			var parserEntry = entry.parserEntry();
			if ((parserEntry != null)
			  && name.equals(parserEntry.name())) {
				matches.add(i);
			
			} else if (matches.size() > 0) {
				if (matches.size() == 0)
					continue;
				
				var eachMatches = new int[matches.size()];
				for (int m = eachMatches.length; --m >= 0;) {
					eachMatches[m] = matches.get(m);
				}
				allMatches.add(eachMatches);
				
				matches.clear();
			}
		}
		
		if (allMatches.size() == 0)
			return null;
		
		var allMatcheArray = new int[allMatches.size()][];
		for (int i = allMatcheArray.length; --i >= 0;) {
			allMatcheArray[i] = allMatches.get(i);
		}
		return allMatcheArray;
	}
	
	//-- Start Position ------------------------------------------------------------------------------------------------
	
	/** Returns the start position of the match */
	abstract public int startPosition();
	
	/** Returns the start position of an entry at the index */
	public final int startPositionOf(int index) {
		if (index == 0)
			return startPosition();
		
		if (index < 0)
			return -1;
		
		if (index >= entryCount())
			return -1;
		
		int previousIndex = index - 1;
		var previousEntry = entryAt(previousIndex);
		return (previousEntry == null)
		        ? -1
		        : previousEntry.endPosition();
	}
	
	/** Returns the start position of sub entry at the indexes */
	public final int startPositionOf(int firstIndex, int secondIndex, int... restIndexes) {
		try {
			var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
			int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
			              ? secondIndex
			              : restIndexes[restIndexes.length - 1];
			return result.startPositionOf(lastIndex);
			
		} catch (ArrayIndexOutOfBoundsException exception) {
			var errMsg = format("Error getting a result entry at [%d, %d, %s]: %s",
			                    firstIndex,
			                    secondIndex,
			                    Util.toString(restIndexes, "", "", ", "),
			                    exception.getMessage());
			throw new RuntimeException(errMsg, exception);
		}
	}
	
	/** Get start position of the last entry named name */
	public final int startPositionOf(String name) {
		int index = indexOf(name);
		return startPositionOf(index);
	}
	
	/** Get start positions of the all entries named name */
	public final int[] startPositionsOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new int[0];
		
		var starts = new int[length];
		for (int i = starts.length; --i >= 0;) {
			int index = indexes[i];
			starts[i] = startPositionOf(index);
		}
		return starts;
	}
	
	//-- End Position --------------------------------------------------------------------------------------------------
	
	/** Returns the end position that this parse result match */
	public final int endPosition() {
		int lastIndex = entryCount() - 1;
		return endPositionOf(lastIndex);
	}
	
	/** Returns the end position of an entry at the index */
	public final int endPositionOf(int index) {
		if (index == -1)
			return startPosition();
		
		if (index < 0)
			return -1;
		
		if (index >= entryCount())
			return -1;
		
		var entry = entryAt(index);
		return entry.endPosition();
	}
	
	/** Returns the end position of sub entry at the indexes */
	public final int endPositionOf(int firstIndex, int secondIndex, int... restIndexes) {
		try {
			var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
			int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
			              ? secondIndex
			              : restIndexes[restIndexes.length - 1];
			return result.endPositionOf(lastIndex);
			
		} catch (ArrayIndexOutOfBoundsException exception) {
			var errMsg = format("Error getting a result entry at [%d, %d, %s]: %s",
			                    firstIndex,
			                    secondIndex,
			                    Util.toString(restIndexes, "", "", ", "),
			                    exception.getMessage());
			throw new RuntimeException(errMsg, exception);
		}
	}
	
	// Entry Location in Col,Row --------------------------------------------------------
	
	/** Get locationCR of the entry at the index */
	public final Coordinate coordinateOf(int index) {
		var entry = entryAt(index);
		if (entry == null)
			return null;
		
		int Pos = startPositionOf(index);
		if (Pos == -1)
			return null;
		
		var originalText = originalCharSequence();
		return Coordinate.of(originalText, Pos);
	}
	
	/** Get locationRC of the last entry named name */
	public final Coordinate coordinateOf(String name) {
		int index = indexOf(name);
		return coordinateOf(index);
	}
	
	/** Get locationRC of the all entries named name */
	public final Coordinate[] coordinatesOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new Coordinate[0];
		
		var coordinates = new Coordinate[length];
		for (int i = coordinates.length; --i >= 0;) {
			int index = indexes[i];
			coordinates[i] = coordinateOf(index);
		}
		return coordinates;
	}
	
	//-- Entry Location with Cursor pointer ----------------------------------------------------------------------------
	
	/** Get location of the entry at the index */
	public final String locationOf(int index) {
		var entry = entryAt(index);
		if (entry == null)
			return null;
		
		int Pos = startPositionOf(index);
		if (Pos == -1)
			return null;
		
		var originalText = originalCharSequence();
		return Location.of(originalText, Pos);
	}
	
	/** Get location of the last entry named name */
	public final String locationOf(String name) {
		int index = indexOf(name);
		return locationOf(index);
	}
	
	/** Get location of the all entries named name */
	public final String[] locationsOf(String name) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var locations = new String[length];
		for (int i = locations.length; --i >= 0; ) {
			int index = indexes[i];
			locations[i] = locationOf(index);
		}
		return locations;
	}
	
	//-- Compiled value ------------------------------------------------------------------------------------------------
	
	/** Get compile value of the last match */
	public final Object valueOf(int index, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		var type = typeOf(index, typeProvider);
		if (type == null) {
			var typeName = this.typeNameOf(index);
			// The entry has no type, so just return the text value of it
			if (typeName == null)
				return textOf(index);
			
			type = typeProvider.type(typeName);
			if (type == null)
				throw new RuntimeException(format("Unknown type `%s`.", typeName));
		}
		
		var parameter = parameterOf(index);
		return type.compile(this, index, parameter, compilationContext, typeProvider);
	}
	
	/** Get compile value of the last match */
	public final Object valueOf(String name, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		int index = indexOf(name);
		return valueOf(index, typeProvider, compilationContext);
	}
	
	/** Get subs result of the last match */
	public final Object[] valuesOf(String name, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var values = new Object[length];
		for (int i = 0; i < values.length; i++) {
			int index = indexes[i];
			values[i] = valueOf(index, typeProvider, compilationContext);
		}
		return values;
	}
	
	//-- Compiled value as Text ----------------------------------------------------------------------------------------
	
	/** Get compile value as text of the last match */
	public final String valueAsTextOf(int index, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		var type = typeOf(index, typeProvider);
		if (type == null) {
			var typeName = typeNameOf(index);
			if (typeName == null) {
				// The entry has no type, so compile each sub and concatenate them
				var subResult = subResultOf(index);
				int subResultCount;
				if ((subResult == null)
				 || ((subResultCount = subResult.entryCount()) == 0))
					return textOf(index);
				
				// Compile each sub of the sub entry and concatenate them
				var buffer = new StringBuilder();
				for (int i = 0; i < subResultCount; i++) {
					var value = subResult.valueAsTextOf(i, typeProvider, compilationContext);
					buffer.append(value);
				}
				return buffer.toString();
			}
			
			type = typeProvider.type(typeName);
			if (type == null)
				throw new RuntimeException(format("Unknown type `%s`.", typeName));
		}
		
		var parameter = parameterOf(index);
		var value     = type.compile(this, index, parameter, compilationContext, typeProvider);
		return (value == null)
		        ? ""
		        : value.toString();
	}
	
	/** Get compile value as text of the last match */
	public final String valueAsTextOf(String name, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		int index = indexOf(name);
		return valueAsTextOf(index, typeProvider, compilationContext);
	}
	
	/** Get compile value as text of the all match */
	public final String[] valueAsTextsOf(String name, ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		var indexes = indexesOf(name);
		if (indexes == null)
			return null;
		
		int length = indexes.length;
		if (length == 0)
			return new String[0];
		
		var valueAsText = new String[length];
		for (int i = 0; i < valueAsText.length; i++) {
			int index = indexes[i];
			valueAsText[i] = valueAsTextOf(index, typeProvider, compilationContext);
		}
		return valueAsText;
	}
	
	//-- Message -------------------------------------------------------------------------------------------------------
	
	static public final String WARNING_PREFIX     = "$WARNING_";
	static public final String ERROR_PREFIX       = "$ERROR_";
	static public final String FATAL_ERROR_PREFIX = "$FATAL_ERROR_";
	
	public final String message(String name) {
		var buffer = new StringBuffer();
		int length = name.length();
		for (int i = 0; i < length; i++) {
			char ch = name.charAt(i);
			if (ch == '_') {
				if (name.startsWith("___")) {
					buffer.append("_");
					i += 2;
				} else {
					buffer.append(" ");
				}
			} else {
				buffer.append(ch);
			}
		}
		
		var string = buffer.toString();
		return string.endsWith("[]")
		        ? string.substring(0, string.length() - 2)
		        : string;
	}
	
	/** Detect and report all error and warning - returns if there is at least one error or warning */
	public final boolean hasNoError(ParserTypeProvider typeProvider) {
		boolean haveErrorOrWanrning = false;
		int     entryCount          = entryCount();
		for (int i = 0; i < entryCount; i++) {
			var entry = entryAt(i);
			if (entry == null)
				continue;
			
			var type = entry.type();
			if (type == null) {
				var typeName = entry.typeName();
				if (typeName != null) {
					type = (typeProvider == null)
					     ? null
					     : typeProvider.type(typeName);
					if (type == null) {
						type = ParserTypeProvider.Extensible.defaultProvider().type(typeName);
						if (type == null)
							throw new RuntimeException(format("Unknown type `%s`.", typeName));
					}
				}
			}
			// Found an error type, so there is error
			if (type instanceof ErrorParserType)
				return false;
			
			var name = entry.name();
			if (name != null) {
				if (name.startsWith(WARNING_PREFIX)
				 || name.startsWith(ERROR_PREFIX)
				 || name.startsWith(FATAL_ERROR_PREFIX))
					return false;
			}
			
			var subResult = subResultOf(i);
			if ((subResult != null)
			 && !subResult.hasNoError(typeProvider))
				return false;
		}
		return !haveErrorOrWanrning;
	}
	
	/** Detect and report all error and warning - returns if there is at least one error or warning */
	public final boolean ensureNoError(ParserTypeProvider typeProvider, CompilationContext compilationContext) {
		boolean haveErrorOrWanrning = false;
		int     entryCount          = entryCount();
		for (int i = 0; i < entryCount; i++) {
			var entry = entryAt(i);
			if (entry == null)
				continue;
			
			var type = entry.type();
			if (type == null) {
				var typeName = entry.typeName();
				if (typeName != null) {
					type = (typeProvider == null)
					     ? null
					     : typeProvider.type(typeName);
					if (type == null) {
						type = ParserTypeProvider.Extensible.defaultProvider().type(typeName);
						if (type == null)
							throw new RuntimeException(format("Unknown type `%s`.", typeName));
					}
				}
			}
			if (type instanceof ErrorParserType) {
				// The type is an error type - let it solve it own business
				var parameter = entry.parameter();
				type.compile(this, i, parameter, compilationContext, typeProvider);
				haveErrorOrWanrning = true;
				continue;
			}
			
			var name = entry.name();
			if (name != null) {
				boolean isWarning    = false;
				boolean isError      = false;
				boolean isFatalError = false;
				if ((isWarning    = name.startsWith(WARNING_PREFIX))
				 || (isError      = name.startsWith(ERROR_PREFIX))
				 || (isFatalError = name.startsWith(FATAL_ERROR_PREFIX))) {
					haveErrorOrWanrning = true;
					
					int kindLength = 0;
					if (isWarning) {
						kindLength = WARNING_PREFIX.length();
					} else if (isError) {
						kindLength = ERROR_PREFIX.length();
					} else if (isFatalError) {
						kindLength = FATAL_ERROR_PREFIX.length();
					}
					
					var msg = (typeProvider == null)
					        ? null
					        : typeProvider.errorMessage(name.substring(1));
					// NOTE: 1 is to eliminate $ prefix >-----------------------------------^
					msg = (msg != null)
					    ? msg
					    : this.message(name.substring(kindLength, name.length()));
					int startPosition = startPositionOf(i);
					compilationContext.reportError(msg, null, startPosition);
					if (isFatalError)
						throw new RuntimeException(format("FATAL ERROR! The compilation cannot be continued: %s", msg));
				}
			}
			
			var subResult = subResultOf(i);
			if ((subResult != null)
			 && !subResult.ensureNoError(typeProvider, compilationContext))
				return false;
		}
		return !haveErrorOrWanrning;
	}
	
	//-- Modifying the results -----------------------------------------------------------------------------------------
	
	/** Appends the result with an entry */
	public final ParseResult append(ParseResultEntry entry) {
		if (entry == null)
			throw new NullPointerException();
		
		if (entries == null) {
			entries = new ArrayList<>();
		}
		entries.add(entry);
		return this;
	}
	
	/** Reset the entry since the index */
	public final void reset(int index) {
		if (entries == null)
			return;
		
		for (int i = entries.size(); --i >= index;) {
			entries.remove(i);
		}
	}
	
	/** Merge a temporary result with this result */
	public final void mergeWith(TemporaryParseResult tempResult) {
		if ((tempResult == null)
		 || (tempResult.entryCount() == 0))
			return;
		
		var tempEntries = tempResult.entries().collect(toList());
		if (entries == null) {
			entries = new ArrayList<>();
		}
		entries.addAll(tempEntries);
		
		for (int i = entries.size(); --i > 0;) {
			var entry = entries.get(i);
			if (entry.hasSubResult()) {
				var subResult = entry.subResult();
				if (subResult instanceof ParseResultNode) {
					var subNode = (ParseResultNode)subResult;
					subNode.parent(this);
				}
			}
		}
	}
	
	/** Collapse the result so entry with $ and [] will be combine */
	public final void collapse(ParserTypeProvider typeProvider) {
		if (this.entries == null)
			return;
		
		if (this.entries.size() == 0)
			return;
		
		int     entrySize          = entries.size();
		var     lastEntry          = entries.get(entrySize - 1);
		boolean lastEntryHasNoName = (lastEntry.name()    == null)
		                          && (lastEntry.type()    == null)
		                          && (lastEntry.typeRef() == null);
		
		// Basic collapse of the no name and no type
		for (int i = (entries.size() - 1); --i >= 0;) {
			var     entry          = entries.get(i);
			boolean entryHasNoName = ((entry.name()    == null)
			                       && (entry.type()    == null)
			                       && (entry.typeRef() == null));
			// If both has no name, collapse
			if (entryHasNoName && lastEntryHasNoName) {
				entries.remove(i);
			}
			
			lastEntryHasNoName = entryHasNoName;
			lastEntry          = entry;
		}
		
		// Collapse sub entry that does not have name or type
		// Remove the entry without sub/type then replace it with the one with out a sub
		for (int i = entries.size(); --i >= 0;) {
			var thisEntry = entries.get(i);
			
			if (thisEntry.hasSubResult()
			&& !thisEntry.subResult().hasNames()
			&& !thisEntry.subResult().hasTypes()) {
				int thisEndPosition = thisEntry.endPosition();
				if (thisEntry instanceof ParseResultEntryWithSubResult) {
					entries.remove(i);
					
					var newEntry = newEntry(thisEndPosition);
					entries.add(i, newEntry);
				} else
					if (thisEntry instanceof ParseResultEntryWithParserEntryAndSubResult) {
						entries.remove(i);
						
						var thisParseEntry = thisEntry.parserEntry();
						var newEntry       = newEntry(thisEndPosition, thisParseEntry);
						entries.add(i, newEntry);
					}
			}
			
			if (!thisEntry.hasSubResult())
				continue;
			
			thisEntry
				.subResult()
				.collapse(typeProvider);
		}
		
		// Collapse entry that its sub does not contain any named or typed entry
		for (int i = entries.size(); --i >= 0;) {
			var thisEntry = entries.get(i);
			if (!thisEntry.hasSubResult())
				continue;
			
			if (thisEntry.subResult().hasNames())
				continue;
			
			if (thisEntry.subResult().hasTypes())
				continue;
			
			entries.remove(i);
			
			int thisEndPosition = thisEntry.endPosition();
			var thisParseEntry  = thisEntry.parserEntry();
			var newEntry        = newEntry(thisEndPosition, thisParseEntry);
			entries.add(i, newEntry);
		}
		
		// Collapse the same type and same name that end with '[]'
		lastEntry = entries.get(entries.size() - 1);
		ParserType    LatestType = lastEntry.type();
		ParserTypeRef LatestTRef = lastEntry.typeRef();
		String   LatestName = lastEntry.name();
		
		for (int i = (entries.size() - 1); --i >= 0;) {
			var thisEntry   = entries.get(i);
			var thisType    = thisEntry.type();
			var thisTypeRef = thisEntry.typeRef();
			var thisName    = thisEntry.name();
			
			if (!lastEntry.hasSubResult()
			 && !thisEntry.hasSubResult()
			 && Objects.equals(LatestType, thisType)
			 && Objects.equals(LatestTRef, thisTypeRef)
			 && Objects.equals(LatestName, thisName)
			 && (((thisName    != null) && thisName          .endsWith("[]"))
			  || ((thisType    != null) && thisType   .name().endsWith("[]"))
			  || ((thisTypeRef != null) && thisTypeRef.name().endsWith("[]")))) {
				entries.remove(i);
			}
			lastEntry  = thisEntry;
			LatestType = thisType;
			LatestTRef = thisTypeRef;
			LatestName = thisName;
		}
		
		// Process Second Stage Entry
		for (int i = entries.size(); --i >= 0;) {
			var thisEntry   = entries.get(i);
			var parserEntry = thisEntry.parserEntry();
			if (parserEntry == null)
				continue;
			
			var secondStage = parserEntry.secondStage();
			if (secondStage == null)
				continue;
			
			parseEntry(i, secondStage, typeProvider);
		}
		
		if (RegParser.DebugMode) {
			RegParser.DebugPrintStream.println("Before Flating:------------------------------------------------------");
			RegParser.DebugPrintStream.println(toString());
		}
		
		// Collapse auto skip name that end with '*'
		for (int i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			if (!entry.hasSubResult())
				continue;
			
			var name = entry.name();
			var type = entry.typeName();
			if (((name == null) || !name.contains("*"))
			 && ((type == null) || !type.contains("*")))
				continue;
			
			if (RegParser.DebugMode) {
				RegParser.DebugPrintStream
				        .printf("Flating '%s':'%s' START:----------------------------------------------\n", name, type);
			}
			flatEntry(i);
			if (RegParser.DebugMode) {
				RegParser.DebugPrintStream
				        .printf("Flating '%s':'%s' END:------------------------------------------------\n", name, type);
			}
			i--;
		}
		
		// Collapse auto skip name that end with '+', has sub and only one entry
		for (int i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			if (!entry.hasSubResult())
				continue;
			
			if (entry.subResult().entryCount() != 1)
				continue;

			var name = entry.name();
			var type = entry.typeName();
			if (((name == null) || !name.contains("+"))
			 && ((type == null) || !type.contains("+")))
				continue;
			
			if (RegParser.DebugMode) {
				RegParser.DebugPrintStream
				        .printf("Flating '%s':'%s' START:----------------------------------------------\n", name, type);
			}
			flatEntry(i);
			if (RegParser.DebugMode) {
				RegParser.DebugPrintStream
				        .printf("Flating '%s':'%s' END:------------------------------------------------\n", name, type);
			}
			i--;
		}
		
		if (RegParser.DebugMode) {
			RegParser.DebugPrintStream.println("End Flating:---------------------------------------------------------");
		}
	}
	
	//-- Second Stage related ------------------------------------------------------------------------------------------
	
	/** Parse the result entry - Only when the entry has no sub */
	public final boolean parseEntry(int index, RegParser parser) {
		return parseEntry(index, parser, null);
	}
	
	/** Parse the result entry - Only when the entry has no sub */
	public final boolean parseEntry(int index, RegParser parser, ParserTypeProvider typeProvider) {
		if (parser == null)
			return false;
		
		var Entry = entryAt(index);
		if ((Entry == null)
		  || Entry.hasSubResult())
			return false;
		
		int endPosition   = endPositionOf(index);
		int startPosition = startPositionOf(index);
		var text          = originalText().substring(0, endPosition);
		var result        = parser.parse(text, startPosition, typeProvider);
		if (result == null)
			return false;
		
		if (result.entryCount() == 0)
			return true;
		
		if (result.endPosition() == text.length()) {
			// If the end position is equal, there is no need to keep the old one.
			entries.remove(index);
		}
		for (int i = result.entryCount(); --i >= 0;) {
			var resultEntry = result.entries.get(i);
			entries.add(index, resultEntry);
		}
		
		return true;
	}
	
	//-- Flat entry ----------------------------------------------------------------------------------------------------
	
	/** Appends entry of the sub (entry of the node) to the current parse result and erase the entry */
	public final boolean flatEntry(int index) {
		var entry = entryAt(index);
		if ((entry == null)
		 || !entry.hasSubResult())
			return false;
		
		var subResult = entry.subResult();
		
		// Remove the main one
		entries.remove(index);
		
		// Replace with the sub of the main one
		for (int i = subResult.entryCount(); --i >= 0;) {
			var subEntry = subResult.entries.get(i);
			entries.add(index, subEntry);
		}
		
		return true;
	}
	
	/** Parse the result entry - Only when the entry has no sub */
	public final void flatEntry(int[] indexes) {
		if (indexes == null)
			return;
		
		for (int i = indexes.length; --i >= 0;) {
			int index = indexes[i];
			flatEntry(index);
		}
	}
	
	/** Parse the result entry - Only when the entry has no sub */
	public final void flatEntry(String name) {
		if (name == null)
			return;
		
		var indexes = indexesOf(name);
		this.flatEntry(indexes);
	}
	
	/** Parse the result entry - Only when the entry has no sub */
	public final boolean flatLastEntryOf(String name) {
		if (name == null)
			return false;
		
		int index = indexOf(name);
		if (index < 0)
			return false;
		
		return flatEntry(index);
	}
	
	//-- Object --------------------------------------------------------------------------------------------------------
	
	/** Returns the detail string representation of the parse result */
	@Override
	public final String toString() {
		return toString(0, 0);
	}
	
	/** An internal service for toDetail() */
	public final String toString(int pIndent, int pTab) {
		return toString(pIndent, pTab, this.depth() - 1);
	}
	
	/** An internal service for toDetail() */
	final String toString(int indent, int tab, int depth) {
		int count   = entries.size();
		int last    = count - 1;
		var tabs    = indents(indent) + tabs(tab);
		var width   = Math.max(2, textWidth(count));
		var buffer  = new StringBuffer();
		var orgText = originalCharSequence();
		
		int startPosition = startPosition();
		
		buffer.append("\n");
		for (int i = 0; i < count; i++) {
			var entry       = entries.get(i);
			int endPosition = entry.endPosition();
			
			try {
				var parserEntry  = entry.parserEntry();
				var entryName    = nameOf(parserEntry, "<NoName>");
				var typeName     = typeNameOf(entry, "<NoType>");
				var subResult    = entry.subResult();
				var showPosition = spaces(5 - textWidth(endPosition)) + endPosition;
				var showName     = entryName + spaces(16 - entryName.length());
				var showType     = typeName + spaces(16 - typeName.length());
				var text         = orgText.subSequence(startPosition, endPosition);
				text = (text == null) ? "null" : "\"" + Util.escapeText(this.textOf(i)) + "\"";
				
				buffer.append(tabs);
				buffer.append(zeros(width - textWidth(i)) + i);
				buffer.append(dashes(depth));
				buffer.append(" => [").append(showPosition);
				buffer.append("] = ") .append(showName);
				buffer.append(":")    .append(showType);
				buffer.append(" = ")  .append(text);
				
				if (subResult != null) {
					buffer.append(subResult.toString(indent, tab + 1, depth - 1));
				}
				if (i != last) {
					buffer.append("\n");
				}
			} finally {
				startPosition = endPosition;
			}
		}
		return buffer.toString();
	}
	
}
