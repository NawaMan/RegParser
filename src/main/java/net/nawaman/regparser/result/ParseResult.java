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

package net.nawaman.regparser.result;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static net.nawaman.regparser.Util.dashes;
import static net.nawaman.regparser.Util.indents;
import static net.nawaman.regparser.Util.spaces;
import static net.nawaman.regparser.Util.tabs;
import static net.nawaman.regparser.Util.textWidth;
import static net.nawaman.regparser.Util.zeros;
import static net.nawaman.regparser.result.entry.PREntry.newEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.nawaman.regparser.CompilationContext;
import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.PTypeRef;
import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.Util;
import net.nawaman.regparser.result.entry.PREntry;
import net.nawaman.regparser.result.entry.PREntryWithParserEntryAndSub;
import net.nawaman.regparser.result.entry.PREntryWithSub;
import net.nawaman.regparser.types.PTError;
import net.nawaman.regparser.utils.IStream;

/**
 * The result of the parsing
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParseResult implements Serializable {
    
    private static final long serialVersionUID = 4543543556454654354L;
    
    
    public static PRRoot newResult(int startPosition, CharSequence originalText) {
        return new PRRoot(startPosition, originalText);
    }
    
    public static PRTemp newResult(ParseResult first) {
        return new PRTemp(first);
    }
    
    public static PRNode newResult(int startPosition, ParseResult parseResult) {
        return new PRNode(startPosition, parseResult);
    }
    
    public static String nameOf(RPEntry entry) {
        return nameOf(entry, null);
    }
    public static String nameOf(RPEntry entry, String defaultValue) {
        return ((entry != null) && (entry.name() != null))
                ? entry.name()
                : defaultValue;
    }
    
    public static String typeNameOf(PREntry entry) {
        return typeNameOf(entry, null);
    }
    public static String typeNameOf(PREntry entry, String defaultValue) {
        if (entry != null) {
            var type = entry.type();
            if (type != null) {
                return type.name();
            }
            
            var typeRef = entry.typeRef();
            if (typeRef != null) {
                return typeRef.name();
            }
        }
        return defaultValue;
    }
    
    
    private List<PREntry> entries;
    
    /** Constructor */
    ParseResult() {
        this(null);
    }
    
    /** Constructor */
    ParseResult(List<PREntry> entries) {
        this.entries 
                = (entries == null)
                ? new ArrayList<>()
                : entries.stream().collect(toList());
    }
    
    /** Duplicate this result - to be used when verifying */
    public abstract ParseResult duplicate();
    
    
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
    
    
    // Entries ---------------------------------------------------------------------------------------------------------
    
    /** @return  the entries as a streams. */
    public final Stream<PREntry> entries() {
        return (entries == null)
                ? Stream.empty()
                : entries.stream();
    }
    
    /** @return  the entries as a list. */
    public final List<PREntry> entryList() {
        var stream = (entries == null)
                    ? Stream.<PREntry>empty()
                    : entries.stream();
        return stream.collect(toList());
    }
    
    /** Returns the number of the result entry - may be overridden by sub classes. */
    public int entryCount() {
        return rawEntryCount();
    }
    
    /** Returns the entry list size -- Used internally and cannot be overridden. */
    public final int rawEntryCount() {
        return (entries == null) ? 0 : entries.size();
    }
    
    // Text ----------------------------------------------------------------------------------------
    
    /** Get the original text as a CharSequence */
    public abstract CharSequence originalText();
    
    /** Get the original text as a string */
    public final String originalString() {
        return this.originalText().toString();
    }
    
    /** Get the match text */
    public final String text() {
        var orgText = originalText();
        // Ensure proper range
        int start = startPosition();
        int end   = endPosition();
        if ((orgText == null) || (start > orgText.length()) || (start < 0)) {
            return null;
        }
        if ((end > orgText.length()) || (end < 0)) {
            return null;
        }
        // Quick Return
        if (start == end) {
            return "";
        }
        // Normal Return
        var sequence = orgText.subSequence(start, end);
        return sequence.toString();
    }
    
    /** Returns the start position of the match */
    abstract public int startPosition();
    
    /** Returns the end position that this parse result match */
    public final int endPosition() {
        int lastIndex = entryCount() - 1;
        return endPositionAt(lastIndex);
    }
    
    /** Returns a result entry at the index. or null if the index of out of bound. */
    public PREntry entryAt(int index) {
        if ((index < 0) || index >= entryCount()) {
            return null;
        }
        return entries.get(index);
    }
    
    /** Returns the nested result entry at the indexes. or null if the index of out of bound. */
    public final PREntry entryAt(int firstIndex, int secondIndex, int... restIndexes) {
        try {
            var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
            int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
                          ? secondIndex
                          : restIndexes[restIndexes.length - 1];
            return result.entryAt(lastIndex);
        } catch (ArrayIndexOutOfBoundsException exception) {
            var errMsg = format(
                        "Error getting a result entry at [%d, %d, %s]: %s",
                        firstIndex, secondIndex, Util.toString(restIndexes, "", "", ", "), exception.getMessage());
            throw new RuntimeException(errMsg, exception);
        }
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
            if ((restIndexes == null) || (restIndexes.length == 0)) {
                return result;
            }
            
            entry  = result.entryAt(secondIndex);
            result = entry.subResult();
            for (int i = 0; i < (restIndexes.length - 1); i++) {
                int index = restIndexes[i];
                result = result
                        .entryAt(index)
                        .subResult();
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException exception) {
            var errMsg = format(
                        "Error getting a result entry at [%d, %d, %s]: %s",
                        firstIndex, secondIndex, Util.toString(restIndexes, "", "", ", "), exception.getMessage());
            throw new RuntimeException(errMsg, exception);
        }
    }
    
    /** Returns the sub result at the index. or null if the index of out of bound. */
    public final ParseResult subResultAt(int index) {
        var entry = entryAt(index);
        return (entry == null) ? null : entry.subResult();
    }
    
    /** Returns the nested sub result at the indexes. or null if the index of out of bound. */
    public final ParseResult subResultAt(int firstIndex, int secondIndex, int... restIndexes) {
        var entry = entryAt(firstIndex, secondIndex, restIndexes);
        return (entry == null) ? null : entry.subResult();
    }
    
    /** Returns the name of the sub entry at the indexes */
    public final String nameAt(int index) {
        var entry = entryAt(index);
        return (entry == null) ? null : entry.name();
    }
    
    /** Returns the name of the sub entry at the indexes */
    public final String nameAt(int firstIndex, int secondIndex, int... restIndexes) {
        var entry = entryAt(firstIndex, secondIndex, restIndexes);
        return (entry == null) ? null : entry.name();
    }
    
    /** Returns the type name of the sub entry at the indexes */
    public final String typeNameAt(int index) {
        var entry = entryAt(index);
        return (entry == null) ? null : entry.typeName();
    }
    
    /** Returns the type name of the sub entry at the indexes */
    public final String typeNameAt(int firstIndex, int secondIndex, int... restIndexes) {
        var entry = entryAt(firstIndex, secondIndex, restIndexes);
        return (entry == null) ? null : entry.typeName();
    }
    
    /** Returns the type reference parameters of the sub entry at the index */
    public final String parameterAt(int index) {
        var entry = entryAt(index);
        return (entry == null) ? null : entry.parameter();
    }
    
    /** Returns the type reference parameters of the sub entry at the index */
    public final String parameterAt(int firstIndex, int secondIndex, int... restIndexes) {
        var entry = entryAt(firstIndex, secondIndex, restIndexes);
        return (entry == null) ? null : entry.parameter();
    }
    
    /** Returns the start position of an entry at the index */
    public final int startPositionAt(int index) {
        if (index == 0) {
            return startPosition();
        }
        if (index < 0) {
            return -1;
        }
        if (index >= entryCount()) {
            return -1;
        }
        int previousIndex = index - 1;
        var previousEntry = entryAt(previousIndex);
        return (previousEntry == null) ? -1 : previousEntry.endPosition();
    }
    
    /** Returns the start position of sub entry at the indexes */
    public final int startPositionAt(int firstIndex, int secondIndex, int... restIndexes) {
        try {
            var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
            int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
                          ? secondIndex
                          : restIndexes[restIndexes.length - 1];
            return result.startPositionAt(lastIndex);
        } catch (ArrayIndexOutOfBoundsException exception) {
            var errMsg = format(
                        "Error getting a result entry at [%d, %d, %s]: %s",
                        firstIndex, secondIndex, Util.toString(restIndexes, "", "", ", "), exception.getMessage());
            throw new RuntimeException(errMsg, exception);
        }
    }
    
    /** Returns the end position of an entry at the index */
    public final int endPositionAt(int index) {
        if (index == -1) {
            return startPosition();
        }
        if (index < 0) {
            return -1;
        }
        if (index >= entryCount()) {
            return -1;
        }
        var entry = entryAt(index);
        return entry.endPosition();
    }
    
    /** Returns the end position of sub entry at the indexes */
    public final int endPositionAt(int firstIndex, int secondIndex, int... restIndexes) {
        try {
            var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
            int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
                          ? secondIndex
                          : restIndexes[restIndexes.length - 1];
            return result.endPositionAt(lastIndex);
        } catch (ArrayIndexOutOfBoundsException exception) {
            var errMsg = format(
                        "Error getting a result entry at [%d, %d, %s]: %s",
                        firstIndex, secondIndex, Util.toString(restIndexes, "", "", ", "), exception.getMessage());
            throw new RuntimeException(errMsg, exception);
        }
    }
    
    /** Returns the text of an entry at the index */
    public final String textAt(int index) {
        var text = originalText();
        if (text == null) {
            return null;
        }
        int start = startPositionAt(index);
        int end   = endPositionAt(index);
        if ((start < 0) || (end < 0) || (start > text.length()) || (end > text.length())) {
            return null;
        }
        if (start == end) {
            return "";
        }
        return text.subSequence(start, end).toString();
    }
    
    /** Returns the start position of a sub entry at the index */
    public final String textAt(int firstIndex, int secondIndex, int... restIndexes) {
        try {
            var result    = nestedSubResultABOVE(firstIndex, secondIndex, restIndexes);
            int lastIndex = ((restIndexes == null) || (restIndexes.length == 0))
                          ? secondIndex
                          : restIndexes[restIndexes.length - 1];
            return result.textAt(lastIndex);
        } catch (ArrayIndexOutOfBoundsException exception) {
            var errMsg = format(
                        "Error getting a result entry at [%d, %d, %s]: %s",
                        firstIndex, secondIndex, Util.toString(restIndexes, "", "", ", "), exception.getMessage());
            throw new RuntimeException(errMsg, exception);
        }
    }
    
    // Get Element by name -----------------------------------------------------------------------
    
    /** Checks if the result has entries with name */
    final boolean hasNames() {
        for (var entry : entries) {
            var parserEntry = entry.parserEntry();
            if ((parserEntry != null) && (parserEntry.name() != null)) {
                return true;
            }
            var subResult = entry.subResult();
            if ((subResult != null) && subResult.hasNames()) {
                return true;
            }
        }
        return false;
    }
    
    /** Checks if the result has entries with name */
    final boolean hasTypes() {
        for (var entry : entries) {
            var parserEntry = entry.parserEntry();
            if (parserEntry != null) {
                if ((parserEntry.type()    != null)
                 || (parserEntry.typeRef() != null)) {
                    return true;
                }
            }
            var subResult = entry.subResult();
            if ((subResult != null) && subResult.hasNames()) {
                return true;
            }
        }
        return false;
    }
    
    public final boolean hasName(String name) {
        if ((name == null) || name.isBlank()) {
            return false;
        }
        for (var entry : entries) {
            var parserEntry = entry.parserEntry();
            if (parserEntry != null) {
                var entryName = parserEntry.name();
                if (name.equals(entryName)) {
                    return true;
                }
            }
            var subResult = entry.subResult();
            if ((subResult != null) && subResult.hasName(name)) {
                return true;
            }
        }
        return false;
    }
    
    /** Returns names of all result entries */
    public IStream<String> names() {
        var names = new HashSet<String>();
        entries()
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
    
    /** Returns the index of the last entry that has the same name with the given name */
    public final int lastIndexOf(String name) {
        for (int i = entries.size(); --i >= 0;) {
            var entry = entries.get(i);
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
            if ((parserEntry != null) && name.equals(parserEntry.name())) {
                indexes.add(i);
            }
        }
        if (indexes.size() == 0)
            return null;
        
        return indexes
                .stream()
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
            if ((parserEntry != null) && name.equals(parserEntry.name())) {
                matches.add(i);
            } else if (matches.size() > 0) {
                if (matches.size() == 0) {
                    continue;
                }
                
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
    
    /** Returns the text of the the last match */
    public String lastStringOf(String name) {
        for (int i = entries.size(); --i >= 0;) {
            var entry = entryAt(i);
            if (!entry.hasParserEntry()) {
                continue;
            }
            var parseEntry = entry.parserEntry();
            if (name.equals(parseEntry.name())) {
                return textAt(i);
            }
        }
        return null;
    }
    
    /** Returns the all the match */
    public final String[][] allStringsOf(String name) {
        var arrayMatches = new ArrayList<String[]>();
        var matchers     = new ArrayList<String>();
        var orgText      = originalText();
        var prevEntry    = (PREntry)null;
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
    
    /** Returns the last match */
    public final PREntry lastEntryOf(String name) {
        for (int i = entries.size(); --i >= 0;) {
            var resultEntry = entries.get(i);
            var parserEntry = resultEntry.parserEntry();
            if (parserEntry != null) {
                var entryName = parserEntry.name();
                if (name.equals(entryName)) {
                    return resultEntry;
                }
            }
        }
        return null;
    }
    
    /** Returns the last group of continuous match */
    public final PREntry[] lastEntriesOf(String name) {
        var entries = new ArrayList<PREntry>();
        for (int i = this.entries.size(); --i >= 0;) {
            var resultEntry = this.entries.get(i);
            var parserEntry = resultEntry.parserEntry();
            if ((parserEntry != null) && name.equals(parserEntry.name())) {
                entries.add(resultEntry);
            } else if (entries.size() > 0) {
                break;
            }
        }
        
        if (entries.size() == 0)
            return null;
        
        var lastEntries = new PREntry[entries.size()];
        for (int i = lastEntries.length; --i >= 0;) {
            lastEntries[i] = entries.get(lastEntries.length - i - 1);
        }
        return lastEntries;
    }
    
    /** Returns the all the match */
    public final PREntry[] entriesOf(String name) {
        var resultEntries = new ArrayList<PREntry>();
        for (var resultEntry : entries) {
            var parserEntry = resultEntry.parserEntry();
            if (parserEntry != null) {
                var entryName = parserEntry.name();
                if (name.equals(entryName)) {
                    resultEntries.add(resultEntry);
                }
            }
        }
        
        if (resultEntries.size() == 0) {
            return null;
        }
        
        return resultEntries.toArray(PREntry[]::new);
    }
    
    /** Returns the all the match */
    public final PREntry[][] allEntriesOf(String name) {
        var entryArray = new ArrayList<PREntry[]>();
        var entryList  = new ArrayList<PREntry>();
        for (int i = 0; i < this.entryCount(); i++) {
            var resultEntry = this.entryAt(i);
            var parserEntry = resultEntry.parserEntry();
            if ((parserEntry != null) && name.equals(parserEntry.name())) {
                entryList.add(resultEntry);
            }
            
            else
                if (entryList.size() > 0) {
                    if (entryList.size() == 0)
                        continue;
                    
                    var allEntries = new PREntry[entryList.size()];
                    for (int e = allEntries.length; --e >= 0;) {
                        allEntries[e] = entryList.get(e);
                    }
                    entryArray.add(allEntries);
                    entryList.clear();
                }
        }
        
        if (entryArray.size() == 0) {
            return null;
        }
        
        var allEntries = new PREntry[entryArray.size()][];
        for (int i = allEntries.length; --i >= 0;) {
            allEntries[i] = entryArray.get(i);
        }
        return allEntries;
    }
    
    /** Returns how deep the parse result is nested */
    public final int depth() {
        int depth = 0;
        for (int i = this.entryCount(); --i >= 0;) {
            var subResult = this.subResultAt(i);
            if (subResult != null) {
                int d = subResult.depth();
                if (depth < d) {
                    depth = d;
                }
            }
        }
        return depth + 1;
    }
    
    // Easy to use utilities methods ------------------------------------------------------------------------
    // These method allow a short method name but with the assumption that the accessing name 
    //     refers to the last match only.
    // Some of these method are multiple access counterpart (added for completion) and the name
    //     access refers to all match
    
    // Message ------------------------------------------------------------------------------------
    
    static public final String WARNING_PREFIX     = "$WARNING_";
    static public final String ERROR_PREFIX       = "$ERROR_";
    static public final String FATAL_ERROR_PREFIX = "$FATAL_ERROR_";
    
    public final String message(String name) {
        var buffer = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (ch == '_') {
                if (name.startsWith("___")) {
                    buffer.append("_");
                    i += 2;
                } else
                    buffer.append(" ");
            } else
                buffer.append(ch);
        }
        var string = buffer.toString();
        return string.endsWith("[]") ? string.substring(0, string.length() - 2) : string;
    }
    
    /** Detect and report all error and warning - returns if there is at least one error or warning */
    public final boolean hasNoError(PTypeProvider typeProvider) {
        boolean haveErrorOrWanrning = false;
        int     entryCount          = this.entryCount();
        for (int i = 0; i < entryCount; i++) {
            var entry = this.entryAt(i);
            if (entry == null)
                continue;
            
            var type = entry.type();
            if (type == null) {
                var typeName = entry.typeName();
                if (typeName != null) {
                    type = (typeProvider == null) ? null : typeProvider.getType(typeName);
                    if (type == null) {
                        type = PTypeProvider.Extensible.getDefault().getType(typeName);
                        if (type == null) {
                            throw new RuntimeException("Unknown type `" + typeName + "`.");
                        }
                    }
                }
            }
            // Found an error type, so there is error
            if (type instanceof PTError)
                return false;
            
            var name = entry.name();
            if (name != null) {
                if (name.startsWith(WARNING_PREFIX)
                 || name.startsWith(ERROR_PREFIX)
                 || name.startsWith(FATAL_ERROR_PREFIX)) {
                    return false;
                }
            }
            
            var subResult = this.subResultAt(i);
            if ((subResult != null) && !subResult.hasNoError(typeProvider)) {
                return false;
            }
        }
        return !haveErrorOrWanrning;
    }
    
    /** Detect and report all error and warning - returns if there is at least one error or warning */
    public final boolean ensureNoError(PTypeProvider typeProvider, CompilationContext compilationContext) {
        boolean haveErrorOrWanrning = false;
        int     entryCount          = this.entryCount();
        for (int i = 0; i < entryCount; i++) {
            var entry = this.entryAt(i);
            if (entry == null) {
                continue;
            }
            
            var type = entry.type();
            if (type == null) {
                var typeName = entry.typeName();
                if (typeName != null) {
                    type = (typeProvider == null) ? null : typeProvider.getType(typeName);
                    if (type == null) {
                        type = PTypeProvider.Extensible.getDefault().getType(typeName);
                        if (type == null) {
                            throw new RuntimeException("Unknown type `" + typeName + "`.");
                        }
                    }
                }
            }
            if (type instanceof PTError) {
                // The type is an error type - let it solve it own business
                type.compile(this, i, entry.parameter(), compilationContext, typeProvider);
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
                    if (isWarning)
                        kindLength = WARNING_PREFIX.length();
                    else
                        if (isError)
                            kindLength = ERROR_PREFIX.length();
                        else
                            if (isFatalError)
                                kindLength = FATAL_ERROR_PREFIX.length();
                            
                    var msg = (typeProvider == null) ? null : typeProvider.getErrorMessage(name.substring(1));
                    // NOTE: 1 is to eliminate $ prefix >-----------------------------------^
                    msg = (msg != null) ? msg : this.message(name.substring(kindLength, name.length()));
                    compilationContext.reportError(msg, null, this.startPositionAt(i));
                    if (isFatalError) {
                        throw new RuntimeException("FATAL ERROR! The compilation cannot be continued: " + msg);
                    }
                    
                }
            }
            
            var subResult = this.subResultAt(i);
            if ((subResult != null) && !subResult.ensureNoError(typeProvider, compilationContext)) {
                return false;
            }
        }
        return !haveErrorOrWanrning;
    }
    
    // Text value -----------------------------------------------------------------------
    
    /** Get text result of the last match */
    public final String textOf(int i) {
        return this.textAt(i);
    }
    
    /** Get text result of the last match */
    public final String textOf(String name) {
        return this.textAt(this.lastIndexOf(name));
    }
    
    /** Get texts result of the last match */
    public final String[] textsOf(String name) {
        int[] Is = this.indexesOf(name);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] PRENs = new String[Is.length];
        for (int i = PRENs.length; --i >= 0;)
            PRENs[i] = this.textAt(Is[i]);
        return PRENs;
    }
    
    // Entry Name -----------------------------------------------------------------------
    
    /** Get text result of the last match */
    public final String nameOf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        return PRE.name();
    }
    
    /** Get text result of the last match */
    public final String nameOf(String pEName) {
        return this.nameOf(this.lastIndexOf(pEName));
    }
    
    /** Get texts result of the last match */
    public final String[] namesOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] PRENs = new String[Is.length];
        for (int i = PRENs.length; --i >= 0;)
            PRENs[i] = this.nameOf(Is[i]);
        return PRENs;
    }
    
    // Entry SubEntry -------------------------------------------------------------------
    
    /** Get sub result of the last match */
    public final ParseResult subOf(int I) {
        return this.subResultAt(I);
    }
    
    /** Get sub result of the last match */
    public final ParseResult subOf(String pEName) {
        PREntry E = this.lastEntryOf(pEName);
        return ((E == null) || !E.hasSubResult()) ? null : E.subResult();
    }
    
    /** Get subs result of the last match */
    public final ParseResult[] subsOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new ParseResult[0];
        ParseResult[] PRs = new ParseResult[Is.length];
        for (int i = PRs.length; --i >= 0;)
            PRs[i] = this.subOf(Is[i]);
        return PRs;
    }
    
    // Entry Type -----------------------------------------------------------------------
    
    /** Get Type of the last match */
    public final PType typeOf(int I, PTypeProvider TProvider) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.type();
        if (PT != null)
            return PT;
        String TName = PRE.typeName();
        if (TName == null)
            return null;
        if (TProvider == null)
            return null;
        return TProvider.getType(TName);
    }
    
    /** Get Type of the last match */
    public final PType typeOf(String pEName, PTypeProvider TProvider) {
        return this.typeOf(this.lastIndexOf(pEName), TProvider);
    }
    
    /** Get subs result of the last match */
    public final PType[] typesOf(String pEName, PTypeProvider TProvider) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new PType[0];
        PType[] PTs = new PType[Is.length];
        for (int i = PTs.length; --i >= 0;)
            PTs[i] = this.typeOf(Is[i], TProvider);
        return PTs;
    }
    
    // Entry Type's Name ----------------------------------------------------------------
    
    /** Get Type name of the last match */
    public final String typeNameOf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.type();
        if (PT != null)
            return PT.name();
        return PRE.typeName();
    }
    
    /** Get Type name of the last match */
    public final String typeNameOf(String pEName) {
        return this.typeNameOf(this.lastIndexOf(pEName));
    }
    
    /** Get subs result of the last match */
    public final String[] typeNamesOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] PTNs = new String[Is.length];
        for (int i = PTNs.length; --i >= 0;)
            PTNs[i] = this.typeNameOf(Is[i]);
        return PTNs;
    }
    
    // Entry Type-reference Parameter ---------------------------------------------------
    
    /** Get Type name of the last match */
    public final String typeParamOf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.type();
        if (PT != null)
            return null;
        return PRE.parameter();
    }
    
    /** Get Type name of the last match */
    public final String typeParamOf(String pEName) {
        return this.typeParamOf(this.lastIndexOf(pEName));
    }
    
    /** Get subs result of the last match */
    public final String[] typeParamsOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] PTNs = new String[Is.length];
        for (int i = PTNs.length; --i >= 0;)
            PTNs[i] = this.typeParamOf(Is[i]);
        return PTNs;
    }
    
    // Compiled value -------------------------------------------------------------------
    
    /** Get compile value of the last match */
    public final Object valueOf(int I, PTypeProvider TProvider, CompilationContext CContext) {
        PType PT = this.typeOf(I, TProvider);
        if (PT == null) {
            String TName = this.typeNameOf(I);
            if (TName == null) {
                // The entry has no type, so just return the text value of it
                return this.textOf(I);
            }
            PT = TProvider.getType(TName);
            if (PT == null)
                throw new RuntimeException("Unknown type `" + TName + "`.");
        }
        String Param = this.typeParamOf(I);
        return PT.compile(this, I, Param, CContext, TProvider);
    }
    
    /** Get compile value of the last match */
    public final Object valueOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        return this.valueOf(this.lastIndexOf(pEName), TProvider, CContext);
    }
    
    /** Get subs result of the last match */
    public final Object[] valuesOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        Object[] Vs = new Object[Is.length];
        for (int i = 0; i < Vs.length; i++)
            Vs[i] = this.valueOf(Is[i], TProvider, CContext);
        return Vs;
    }
    
    // Compiled value as Text -----------------------------------------------------------
    
    /** Get compile value as text of the last match */
    public final String valueAsTextOf(int I, PTypeProvider TProvider, CompilationContext CContext) {
        PType PT = this.typeOf(I, TProvider);
        if (PT == null) {
            String TName = this.typeNameOf(I); 
            if (TName == null) {
                // The entry has no type, so compile each sub and concatenate them
                ParseResult Sub = this.subOf(I);
                int         SCount;
                if ((Sub == null) || ((SCount = Sub.entryCount()) == 0))
                    return this.textOf(I);
                
                // Compile each sub of the sub entry and concatenate them
                StringBuilder SB = new StringBuilder();
                for (int i = 0; i < SCount; i++)
                    SB.append(Sub.valueAsTextOf(i, TProvider, CContext));
                return SB.toString();
            }
            PT = TProvider.getType(TName);
            if (PT == null)
                throw new RuntimeException("Unknown type `" + TName + "`.");
        }
        String Param  = this.typeParamOf(I);
        Object Result = PT.compile(this, I, Param, CContext, TProvider);
        return (Result == null) ? "" : Result.toString();
    }
    
    /** Get compile value as text of the last match */
    public final String valueAsTextOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        return this.valueAsTextOf(this.lastIndexOf(pEName), TProvider, CContext);
    }
    
    /** Get compile value as text of the all match */
    public final String[] valueAsTextsOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] Vs = new String[Is.length];
        for (int i = 0; i < Vs.length; i++)
            Vs[i] = this.valueAsTextOf(Is[i], TProvider, CContext);
        return Vs;
    }
    
    // Entry Location in Col,Row --------------------------------------------------------
    
    /** Get locationCR of the entry at the index */
    public final int[] locationCROf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        return this.getLocationAsColRow(I);
    }
    
    /** Get locationRC of the last entry named pEName */
    public final int[] locationCROf(String pEName) {
        return this.locationCROf(this.lastIndexOf(pEName));
    }
    
    /** Get locationRC of the all entries named pEName */
    public final int[][] locationCRsOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new int[0][];
        int[][] LRCs = new int[Is.length][];
        for (int i = LRCs.length; --i >= 0;)
            LRCs[i] = this.locationCROf(Is[i]);
        return LRCs;
    }
    
    // Entry Location with Cursor pointer -----------------------------------------------
    
    /** Get location of the entry at the index */
    public final String locationOf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return null;
        return this.getLocationAsString(I);
    }
    
    /** Get location of the last entry named pEName */
    public final String locationOf(String pEName) {
        return this.locationOf(this.lastIndexOf(pEName));
    }
    
    /** Get location of the all entries named pEName */
    public final String[] locationsOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] Ls = new String[Is.length];
        for (int i = Ls.length; --i >= 0;)
            Ls[i] = this.locationOf(Is[i]);
        return Ls;
    }
    
    // Entry Position -------------------------------------------------------------------
    
    /** Get start position of the entry at the index */
    public final int posOf(int I) {
        var PRE = this.entryAt(I);
        if (PRE == null)
            return -1;
        return this.startPositionAt(I);
    }
    
    /** Get start position of the last entry named pEName */
    public final int posOf(String pEName) {
        return this.posOf(this.lastIndexOf(pEName));
    }
    
    /** Get start positions of the all entries named pEName */
    public final int[] possOf(String pEName) {
        int[] Is = this.indexesOf(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new int[0];
        int[] Ls = new int[Is.length];
        for (int i = Ls.length; --i >= 0;)
            Ls[i] = this.posOf(Is[i]);
        return Ls;
    }
    
    // Modifying the results -------------------------------------------------------------------------------------------
    
    /** Appends the result with an entry */
    public final ParseResult append(PREntry entry) {
        if (entry == null) {
            throw new NullPointerException();
        }
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entry);
        return this;
    }
    
    /** Reset the entry since the index */
    public final void reset(int index) {
        if (entries == null) {
            return;
        }
        for (int i = entries.size(); --i >= index;) {
            entries.remove(i);
        }
    }
    
    /** Merge a temporary result with this result */
    public final void mergeWith(PRTemp temp) {
        if ((temp == null) || (temp.entryCount() == 0)) {
            return;
        }
        
        var tempEntries = temp.entries().collect(toList());
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.addAll(tempEntries);
        
        for (int i = entries.size(); --i > 0;) {
            var entry = entries.get(i);
            if (entry.hasSubResult()) {
                var subResult = entry.subResult();
                if(subResult instanceof PRNode) {
                    var subNode = (PRNode)subResult;
                    subNode.parent(this);
                }
            }
        }
    }
    
    /** Collapse the result so entry with $ and [] will be combine */
    public final void collapse(PTypeProvider pProvider) {
        /* */
        if (this.entries == null)
            return;
        if (this.entries.size() == 0)
            return;
        
        PREntry   LatestPRP       = this.entries.get(this.entries.size() - 1);
        boolean IsLPRPHasNoName = (LatestPRP.name() == null) && (LatestPRP.type() == null)
                && (LatestPRP.typeRef() == null);
        
        // Basic collapse of the no name and no type
        for (int i = (this.entries.size() - 1); --i >= 0;) {
            PREntry ThisPRP = this.entries.get(i);
            
            boolean IsThisPRPNoName = ((ThisPRP.name() == null) && (ThisPRP.type() == null)
                    && (ThisPRP.typeRef() == null));
            // If both has no name, collapse
            if (IsThisPRPNoName && IsLPRPHasNoName)
                this.entries.remove(i);
            
            IsLPRPHasNoName = IsThisPRPNoName;
            LatestPRP       = ThisPRP;
        }
        
        // Collapse sub entry that does not have name or type
        // Remove the entry without sub/type then replace it with the one with out a sub
        for (int i = this.entries.size(); --i >= 0;) {
            var ThisPRP = this.entries.get(i);
            
            if (ThisPRP.hasSubResult() && !ThisPRP.subResult().hasNames() && !ThisPRP.subResult().hasTypes()) {
                if (ThisPRP instanceof PREntryWithSub) {
                    this.entries.remove(i);
                    this.entries.add(i, newEntry(ThisPRP.endPosition()));
                } else
                    if (ThisPRP instanceof PREntryWithParserEntryAndSub) {
                        this.entries.remove(i);
                        this.entries.add(i, PREntry.newEntry(ThisPRP.endPosition(), ThisPRP.parserEntry()));
                    }
            }
            
            if (!ThisPRP.hasSubResult())
                continue;
            ThisPRP.subResult().collapse(pProvider);
        }
        
        // Collapse entry that its sub does not contain any named or typed entry
        for (int i = this.entries.size(); --i >= 0;) {
            var ThisPRP = this.entries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            if (ThisPRP.subResult().hasNames())
                continue;
            if (ThisPRP.subResult().hasTypes())
                continue;
            this.entries.remove(i);
            this.entries.add(i, newEntry(ThisPRP.endPosition(), ThisPRP.parserEntry()));
        }
        
        // Collapse the same type and same name that end with '[]'
        LatestPRP = this.entries.get(this.entries.size() - 1);
        PType    LatestType = LatestPRP.type();
        PTypeRef LatestTRef = LatestPRP.typeRef();
        String   LatestName = LatestPRP.name();
        
        for (int i = (this.entries.size() - 1); --i >= 0;) {
            var ThisPRP  = this.entries.get(i);
            var ThisType = ThisPRP.type();
            var ThisTRef = ThisPRP.typeRef();
            var ThisName = ThisPRP.name();
            
            if (!LatestPRP.hasSubResult() && !ThisPRP.hasSubResult() && Objects.equals(LatestType, ThisType)
                    && Objects.equals(LatestTRef, ThisTRef) && Objects.equals(LatestName, ThisName)
                    && (((ThisName != null) && ThisName.endsWith("[]"))
                            || ((ThisType != null) && ThisType.name().endsWith("[]"))
                            || ((ThisTRef != null) && ThisTRef.name().endsWith("[]")))) {
                this.entries.remove(i);
            }
            LatestPRP  = ThisPRP;
            LatestType = ThisType;
            LatestTRef = ThisTRef;
            LatestName = ThisName;
        }
        
        // Process Second Stage Entry
        for (int i = this.entries.size(); --i >= 0;) {
            var ThisPRP = this.entries.get(i);
            
            RPEntry RPEntry = ThisPRP.parserEntry();
            if (RPEntry == null)
                continue;
            
            RegParser RP = RPEntry.secondStage();
            if (RP == null)
                continue;
            
            this.parseEntry(i, RP, pProvider);
        }
        
        if (RegParser.DebugMode) {
            RegParser.DebugPrintStream.println("Before Flating:------------------------------------------------------");
            RegParser.DebugPrintStream.println(this.toString());
        }
        
        // Collapse auto skip name that end with '*'
        for (int i = 0; i < this.entries.size(); i++) {
            var ThisPRP = this.entries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            String N = ThisPRP.name();
            String T = ThisPRP.typeName();
            if (((N == null) || !N.contains("*")) && ((T == null) || !T.contains("*")))
                continue;
            if (RegParser.DebugMode)
                RegParser.DebugPrintStream
                        .printf("Flating '%s':'%s' START:----------------------------------------------\n", N, T);
            this.flatEntry(i);
            if (RegParser.DebugMode)
                RegParser.DebugPrintStream
                        .printf("Flating '%s':'%s' END:------------------------------------------------\n", N, T);
            i--;
        }
        
        // Collapse auto skip name that end with '+', has sub and only one entry
        for (int i = 0; i < this.entries.size(); i++) {
            var ThisPRP = this.entries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            if (ThisPRP.subResult().entryCount() != 1)
                continue;
            String N = ThisPRP.name();
            String T = ThisPRP.typeName();
            if (((N == null) || !N.contains("+")) && ((T == null) || !T.contains("+")))
                continue;
            if (RegParser.DebugMode)
                RegParser.DebugPrintStream
                        .printf("Flating '%s':'%s' START:----------------------------------------------\n", N, T);
            this.flatEntry(i);
            if (RegParser.DebugMode)
                RegParser.DebugPrintStream
                        .printf("Flating '%s':'%s' END:------------------------------------------------\n", N, T);
            i--;
        }
        
        if (RegParser.DebugMode) {
            RegParser.DebugPrintStream.println("End Flating:---------------------------------------------------------");
        }
    }
    
    // Second Stage related ---------------------------------------------------------------------------------------------
    
    /** Parse the result entry - Only when the entry has no sub */
    public final boolean parseEntry(int pEntryIndex, RegParser pParser) {
        return parseEntry(pEntryIndex, pParser, null);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public final boolean parseEntry(int pEntryIndex, RegParser pParser, PTypeProvider pProvider) {
        if (pParser == null)
            return false;
        PREntry Entry = this.entryAt(pEntryIndex);
        if ((Entry == null) || Entry.hasSubResult())
            return false;
        String      Text = this.originalString().substring(0, this.endPositionAt(pEntryIndex));
        ParseResult PR   = pParser.parse(Text, this.startPositionAt(pEntryIndex), pProvider);
        if (PR == null)
            return false;
        
        if (PR.entryCount() == 0)
            return true;
        if (PR.endPosition() == Text.length()) {
            // If the end position is equal, there is no need to keep the old one.
            this.entries.remove(pEntryIndex);
        }
        for (int i = PR.entryCount(); --i >= 0;)
            this.entries.add(pEntryIndex, PR.entries.get(i));
        
        return true;
    }
    
    // Flat entry -----------------------------------------------------------------------
    
    /** Appends entry of the sub (entry of the node) to the current parse result and erase the entry */
    public final boolean flatEntry(int pEntryIndex) {
        PREntry Entry = this.entryAt(pEntryIndex);
        if ((Entry == null) || !Entry.hasSubResult())
            return false;
        ParseResult PR = Entry.subResult();
        
        // Remove the main one
        this.entries.remove(pEntryIndex);
        
        // Replace with the sub of the main one
        for (int i = PR.entryCount(); --i >= 0;) {
            this.entries.add(pEntryIndex, PR.entries.get(i));
        }
        
        return true;
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public final void flatEntry(int[] pEntryIndexes) {
        if (pEntryIndexes == null)
            return;
        for (int i = pEntryIndexes.length; --i >= 0;)
            this.flatEntry(pEntryIndexes[i]);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public final void flatEntry(String pName) {
        if (pName == null)
            return;
        int[] Is = this.indexesOf(pName);
        this.flatEntry(Is);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public final boolean flatLastEntryOf(String pName) {
        if (pName == null)
            return false;
        int I = this.lastIndexOf(pName);
        if (I < 0)
            return false;
        return this.flatEntry(I);
    }
    
    // Utilities ---------------------------------------------------------------------------------------------------
    
    /** Get Row (line number starts from 0) and Column of the index number */
    public final int[] getLocationAsColRow(int pEntryIndex) {
        int Pos = this.startPositionAt(pEntryIndex);
        if (Pos == -1)
            return null;
        return Helper.getLocationAsColRow(this.originalText(), Pos);
    }
    
    /** Returns the string representation of the starting of the entry index */
    public final String getLocationAsString(int pEntryIndex) {
        int Pos = this.startPositionAt(pEntryIndex);
        if (Pos == -1)
            return null;
        return Helper.getLocationAsString(this.originalText(), Pos);
    }
    
    // Object ------------------------------------------------------------------------------------
    
    /** Returns the detail string representation of the parse result */
    @Override
    public final String toString() {
        return this.toString(0, 0);
    }
    
    /** An internal service for toDetail() */
    public final String toString(int pIndent, int pTab) {
        return this.toString(pIndent, pTab, this.depth() - 1);
    }
    
    /** An internal service for toDetail() */
    final String toString(int indent, int tab, int depth) {
        int count   = entries.size();
        int last    = count - 1;
        var tabs    = indents(indent) + tabs(tab);
        var width   = Math.max(2, textWidth(count));
        var buffer  = new StringBuffer();
        var orgText = originalText();
        
        int startPosition = startPosition();
        
        buffer.append("\n");
        for (int i = 0; i < count; i++) {
            var entry = entries.get(i);
            int endPosition = entry.endPosition();
            
            try {
                var parserEntry = entry.parserEntry();
                var entryName   = nameOf(parserEntry, "<NoName>");
                var typeName    = typeNameOf(entry, "<NoType>");
                var subResult   = entry.subResult();
                var text        = orgText.subSequence(startPosition, endPosition);
                text = (text == null) ? "null" : "\"" + Util.escapeText(this.textAt(i)) + "\"";
                
                buffer.append(tabs);
                buffer.append(zeros(width - textWidth(i)) + i);
                buffer.append(dashes(depth));
                
                buffer.append(" => [").append(spaces(5 - textWidth(endPosition)) + endPosition);
                buffer.append("] = ") .append(entryName + spaces(16 - entryName.length()));
                buffer.append(":")    .append(typeName  + spaces(16 - typeName.length()));
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
