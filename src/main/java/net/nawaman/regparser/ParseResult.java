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

package net.nawaman.regparser;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.nawaman.regparser.types.PTError;

/**
 * The result of the parsing
 *
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
abstract public class ParseResult implements Serializable {
    
    private static final long serialVersionUID = 4543543556454654354L;
    
    /** Constructor */
    ParseResult() {
        this(null);
    }
    
    /** Constructor */
    ParseResult(List<Entry> resultEntries) {
        this.resultEntries 
                = (resultEntries == null)
                ? new ArrayList<>()
                : resultEntries.stream().collect(toList());
    }
    
    // Text ----------------------------------------------------------------------------------------
    
    /** Get the original text as a CharSequence */
    public abstract CharSequence originalText();
    
    /** Get the original text as a string */
    public final String originalString() {
        return this.originalText().toString();
    }
    
    /** Get the match text */
    public String text() {
        var orgText = this.originalText();
        // Ensure proper range
        int start = this.startPosition();
        int end   = this.endPosition();
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
        return orgText.subSequence(start, end).toString();
    }
    
    // Parent --------------------------------------------------------------------------------------
    
    protected ParseResult parent() {
        return null;
    }
    
    public ParseResult root() {
        ParseResult result = this;
        ParseResult parent = null;
        while ((parent = result.parent()) != null) {
            result = parent;
        }
        return result;
    }
    
    // Position ------------------------------------------------------------------------------------
    
    private List<Entry> resultEntries;
    
    public Stream<Entry> entries() {
        return (resultEntries == null)
                ? Stream.empty()
                : resultEntries.stream();
    }
    
    public List<Entry> entryList() {
        var stream = (resultEntries == null)
                    ? Stream.<Entry>empty()
                    : resultEntries.stream();
        return stream.collect(toList());
    }
    
    /** Returns the start position of the match */
    abstract public int startPosition();
    
    /** Returns the end position that this parse result match */
    public int endPosition() {
        int lastIndex = entryCount() - 1;
        return endPositionOf(lastIndex);
    }
    
    // Append and reset ----------------------------------------------------------------------------
    
    /** Appends the result with an entry */
    ParseResult append(Entry entry) {
        if (entry == null) {
            throw new NullPointerException();
        }
        if (resultEntries == null) {
            resultEntries = new ArrayList<>();
        }
        resultEntries.add(entry);
        return this;
    }
    
    /** Reset the entry since the index */
    void reset(int index) {
        if (resultEntries == null) {
            return;
        }
        for (int i = resultEntries.size(); --i >= index;) {
            resultEntries.remove(i);
        }
    }
    
    // Result Entries ------------------------------------------------------------------------------
    
    /** Merge a temporary result with this result */
    void mergeWith(Temp temp) {
        if ((temp == null) || (temp.entryCount() == 0)) {
            return;
        }
        
        var tempEntries = temp.entries().collect(toList());
        if (resultEntries == null) {
            resultEntries = new ArrayList<>();
        }
        resultEntries.addAll(tempEntries);
        
        for (int i = resultEntries.size(); --i > 0;) {
            var entry = resultEntries.get(i);
            if (entry.hasSubResult()) {
                var subResult = entry.subResult();
                if(subResult instanceof Node) {
                    var subNode = (Node)subResult;
                    subNode.parent(this);
                }
            }
        }
    }
    
    /** Returns the number of the result entry - may be overriden by sub classes. */
    public int entryCount() {
        return resultEntrySize();
    }
    
    /** Returns the entry list size */
    final int resultEntrySize() {
        return (resultEntries == null) ? 0 : resultEntries.size();
    }
    
    /** Returns a result entry at the index. or null if the index of out of bound. */
    public Entry resultEntryAt(int index) {
        if ((index < 0) || index >= entryCount()) {
            return null;
        }
        return resultEntries.get(index);
    }
    
    /** Returns the result entry at the nested indexes */
    public ParseResult.Entry entryAt(int... indexes) {
        if ((indexes == null) || (indexes.length == 0)) {
            return null;
        }
        
        ParseResult result = this;
        for (int i = 0; i < (indexes.length - 1); i++) {
            int index = indexes[i];
            result = result.resultEntryAt(index).subResult();
        }
        int lastIndex = indexes[indexes.length - 1];
        return result.resultEntryAt(lastIndex);
    }
    
    /** Returns the sub result at the indexes */
    public ParseResult subResultAt(int... indexes) {
        if ((indexes == null) || (indexes.length == 0)) {
            return null;
        }
        
        var result = this;
        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            result = result.resultEntryAt(index).subResult();
        }
        return result;
    }
    
    /** Returns the name of the sub entry at the indexes */
    public String nameAt(int... indexes) {
        var resultEntry = entryAt(indexes);
        if (resultEntry == null) {
            return null;
        }
        var parserEntry = resultEntry.parserEntry();
        if (parserEntry == null) {
            return null;
        }
        return parserEntry.name();
    }
    
    /** Returns the type name of the sub entry at the indexes */
    public String typeNameAt(int... indexes) {
        var resultEntry = entryAt(indexes);
        if (resultEntry == null) {
            return null;
        }
        var parserEntry = resultEntry.parserEntry();
        if (parserEntry == null) {
            return null;
        }
        var typeRef = parserEntry.typeRef();
        if (typeRef != null) {
            return typeRef.name();
        }
        var type = parserEntry.type();
        if (type != null) {
            return type.name();
        }
        return null;
    }
    
    /** Returns the type reference parameters of the sub entry at the index */
    public String parameterAt(int... indexes) {
        var result = entryAt(indexes);
        if (result == null) {
            return null;
        }
        var parseEntry = result.parserEntry();
        if (parseEntry == null) {
            return null;
        }
        var typeRef = parseEntry.typeRef();
        if (typeRef != null) {
            return typeRef.parameter();
        }
        return null;
    }
    
    /** Returns the start position of an entry at the index */
    public int getStartPositionOf(int pIndex) {
        if (pIndex == 0)
            return this.startPosition();
        if (pIndex < 0)
            return -1;
        if (pIndex >= this.entryCount())
            return -1;
        return this.resultEntryAt(pIndex - 1).getEndPosition();
    }
    
    /** Returns the end position of an entry at the index */
    public int endPositionOf(int pIndex) {
        if (pIndex == -1)
            return this.startPosition();
        if (pIndex < 0)
            return -1;
        if (pIndex >= this.entryCount())
            return -1;
        return this.resultEntryAt(pIndex).getEndPosition();
    }
    
    /** Returns the text of an entry at the index */
    public String getTextOf(int pIndex) {
        CharSequence Text = this.originalText();
        if (Text == null)
            return null;
        int S = this.getStartPositionOf(pIndex);
        int E = this.endPositionOf(pIndex);
        if ((S < 0) || (E < 0) || (S > Text.length()) || (E > Text.length()))
            return null;
        if (S == E)
            return "";
        return Text.subSequence(S, E).toString();
    }
    
    /** Returns the start position of a sub entry at the index */
    public String getSubTextOf(int... Index) {
        ParseResult PR = this;
        if (Index != null) {
            for (int i = 0; i < (Index.length - 1); i++)
                PR = PR.resultEntryAt(Index[i]).subResult();
        }
        return ((Index == null) || (Index.length == 0)) ? null : PR.getTextOf(Index[Index.length - 1]);
    }
    
    /** Duplicate this result - to be used when verifying */
    abstract ParseResult getDuplicate();
    
    private boolean CheckEquals(Object O1, Object O2) {
        if (O1 == O2)
            return true;
        if (O1 == null)
            return false;
        return O1.equals(O2);
    }
    
    /** Collapse the result so entry with $ and [] will be combine */
    void collapse(PTypeProvider pProvider) {
        /* */
        if (this.resultEntries == null)
            return;
        if (this.resultEntries.size() == 0)
            return;
        
        Entry   LatestPRP       = this.resultEntries.get(this.resultEntries.size() - 1);
        boolean IsLPRPHasNoName = (LatestPRP.getName() == null) && (LatestPRP.getType() == null)
                && (LatestPRP.getTypeRef() == null);
        
        // Basic collapse of the no name and no type
        for (int i = (this.resultEntries.size() - 1); --i >= 0;) {
            Entry ThisPRP = this.resultEntries.get(i);
            
            boolean IsThisPRPNoName = ((ThisPRP.getName() == null) && (ThisPRP.getType() == null)
                    && (ThisPRP.getTypeRef() == null));
            // If both has no name, collapse
            if (IsThisPRPNoName && IsLPRPHasNoName)
                this.resultEntries.remove(i);
            
            IsLPRPHasNoName = IsThisPRPNoName;
            LatestPRP       = ThisPRP;
        }
        
        // Collapse sub entry that does not have name or type
        // Remove the entry without sub/type then replace it with the one with out a sub
        for (int i = this.resultEntries.size(); --i >= 0;) {
            ParseResult.Entry ThisPRP = this.resultEntries.get(i);
            
            if (ThisPRP.hasSubResult() && !ThisPRP.subResult().hasNames() && !ThisPRP.subResult().hasTypes()) {
                if (ThisPRP instanceof ParseResult.Entry_WithSub) {
                    this.resultEntries.remove(i);
                    this.resultEntries.add(i, new ParseResult.Entry(ThisPRP.getEndPosition()));
                } else
                    if (ThisPRP instanceof ParseResult.Entry_WithRPEntry_WithSub) {
                        this.resultEntries.remove(i);
                        this.resultEntries.add(i, new ParseResult.Entry_WithRPEntry(ThisPRP.getEndPosition(), ThisPRP.parserEntry()));
                    }
            }
            
            if (!ThisPRP.hasSubResult())
                continue;
            ThisPRP.subResult().collapse(pProvider);
        }
        
        // Collapse entry that its sub does not contain any named or typed entry
        for (int i = this.resultEntries.size(); --i >= 0;) {
            ParseResult.Entry ThisPRP = this.resultEntries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            if (ThisPRP.subResult().hasNames())
                continue;
            if (ThisPRP.subResult().hasTypes())
                continue;
            this.resultEntries.remove(i);
            this.resultEntries.add(i, ParseResult.newEntry(ThisPRP.getEndPosition(), ThisPRP.parserEntry()));
        }
        
        // Collapse the same type and same name that end with '[]'
        LatestPRP = this.resultEntries.get(this.resultEntries.size() - 1);
        PType    LatestType = LatestPRP.getType();
        PTypeRef LatestTRef = LatestPRP.getTypeRef();
        String   LatestName = LatestPRP.getName();
        
        for (int i = (this.resultEntries.size() - 1); --i >= 0;) {
            ParseResult.Entry ThisPRP  = this.resultEntries.get(i);
            PType             ThisType = ThisPRP.getType();
            PTypeRef          ThisTRef = ThisPRP.getTypeRef();
            String            ThisName = ThisPRP.getName();
            
            if (!LatestPRP.hasSubResult() && !ThisPRP.hasSubResult() && CheckEquals(LatestType, ThisType)
                    && CheckEquals(LatestTRef, ThisTRef) && CheckEquals(LatestName, ThisName)
                    && (((ThisName != null) && ThisName.endsWith("[]"))
                            || ((ThisType != null) && ThisType.name().endsWith("[]"))
                            || ((ThisTRef != null) && ThisTRef.name().endsWith("[]")))) {
                this.resultEntries.remove(i);
            }
            LatestPRP  = ThisPRP;
            LatestType = ThisType;
            LatestTRef = ThisTRef;
            LatestName = ThisName;
        }
        
        // Process Second Stage Entry
        for (int i = this.resultEntries.size(); --i >= 0;) {
            ParseResult.Entry ThisPRP = this.resultEntries.get(i);
            
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
        for (int i = 0; i < this.resultEntries.size(); i++) {
            ParseResult.Entry ThisPRP = this.resultEntries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            String N = ThisPRP.getName();
            String T = ThisPRP.getTypeName();
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
        for (int i = 0; i < this.resultEntries.size(); i++) {
            ParseResult.Entry ThisPRP = this.resultEntries.get(i);
            if (!ThisPRP.hasSubResult())
                continue;
            if (ThisPRP.subResult().entryCount() != 1)
                continue;
            String N = ThisPRP.getName();
            String T = ThisPRP.getTypeName();
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
    public boolean parseEntry(int pEntryIndex, RegParser pParser) {
        return parseEntry(pEntryIndex, pParser, null);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public boolean parseEntry(int pEntryIndex, RegParser pParser, PTypeProvider pProvider) {
        if (pParser == null)
            return false;
        Entry Entry = this.resultEntryAt(pEntryIndex);
        if ((Entry == null) || Entry.hasSubResult())
            return false;
        String      Text = this.originalString().substring(0, this.endPositionOf(pEntryIndex));
        ParseResult PR   = pParser.parse(Text, this.getStartPositionOf(pEntryIndex), pProvider);
        if (PR == null)
            return false;
        
        if (PR.entryCount() == 0)
            return true;
        if (PR.endPosition() == Text.length()) {
            // If the end position is equal, there is no need to keep the old one.
            this.resultEntries.remove(pEntryIndex);
        }
        for (int i = PR.entryCount(); --i >= 0;)
            this.resultEntries.add(pEntryIndex, PR.resultEntries.get(i));
        
        return true;
    }
    
    // Flat entry -----------------------------------------------------------------------
    
    /** Appends entry of the sub (entry of the node) to the current parse result and erase the entry */
    public boolean flatEntry(int pEntryIndex) {
        Entry Entry = this.resultEntryAt(pEntryIndex);
        if ((Entry == null) || !Entry.hasSubResult())
            return false;
        ParseResult PR = Entry.subResult();
        
        // Remove the main one
        this.resultEntries.remove(pEntryIndex);
        
        // Replace with the sub of the main one
        for (int i = PR.entryCount(); --i >= 0;) {
            this.resultEntries.add(pEntryIndex, PR.resultEntries.get(i));
        }
        
        return true;
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public void flatEntry(int[] pEntryIndexes) {
        if (pEntryIndexes == null)
            return;
        for (int i = pEntryIndexes.length; --i >= 0;)
            this.flatEntry(pEntryIndexes[i]);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public void flatEntry(String pName) {
        if (pName == null)
            return;
        int[] Is = this.getAllIndexOfEntryName(pName);
        this.flatEntry(Is);
    }
    
    /** Parse the result entry - Only when the entry has no sub */
    public boolean flatLastEntryOf(String pName) {
        if (pName == null)
            return false;
        int I = this.getLastIndexOfEntryName(pName);
        if (I < 0)
            return false;
        return this.flatEntry(I);
    }
    
    // Get Element by name -----------------------------------------------------------------------
    
    /** Checks if the result has entries with name */
    boolean hasNames() {
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && (E.parserEntry().name() != null))
                return true;
            if (E.hasSubResult() && E.subResult().hasNames())
                return true;
        }
        return false;
    }
    
    /** Checks if the result has entries with name */
    boolean hasTypes() {
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry()) {
                if ((E.parserEntry().type() != null) || (E.parserEntry().typeRef() != null))
                    return true;
            }
            if (E.hasSubResult() && E.subResult().hasNames())
                return true;
        }
        return false;
    }
    
    /** Returns names of all result entries */
    public HashSet<String> getAllNames() {
        HashSet<String> Ns = new HashSet<String>();
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry()) {
                String N = E.parserEntry().name();
                if (N == null)
                    continue;
                Ns.add(E.parserEntry().name());
            }
        }
        return (Ns.size() == 0) ? null : Ns;
    }
    
    /** Returns names of all result entries that starts with the given prefix */
    public HashSet<String> getAllNames(String pPrefix) {
        HashSet<String> Ns = new HashSet<String>();
        if ((pPrefix == null) || (pPrefix.length() == 0))
            return Ns;
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry()) {
                String N = E.parserEntry().name();
                if (N == null)
                    continue;
                Ns.add(E.parserEntry().name());
            }
        }
        for (String N : Ns.toArray(Util.EMPTY_STRING_ARRAY)) {
            if (!N.startsWith(pPrefix))
                Ns.remove(N);
        }
        return (Ns.size() == 0) ? null : Ns;
    }
    
    /** Returns the index of the last entry that has the same name with the given name */
    public int getLastIndexOfEntryName(String pName) {
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                return i;
        }
        return -1;
    }
    
    /** Returns the all indexes of the result entries that has the same name with the given name */
    public int[] getAllIndexOfEntryName(String pName) {
        Vector<Integer> Is = new Vector<Integer>();
        for (int i = 0; i < this.entryCount(); i++) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Is.add(i);
        }
        if (Is.size() == 0)
            return null;
        int[] RIs = new int[Is.size()];
        for (int i = Is.size(); --i >= 0;)
            RIs[i] = Is.get(i);
        return RIs;
    }
    
    /** Returns the text of the the last match */
    String getLastStrMatchByName(String pName) {
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                return this.getTextOf(i);
        }
        return null;
    }
    
    /** Returns the last group of continuous match */
    String[] getLastStrMatchesByName(String pName) {
        Vector<String> Ms = new Vector<String>();
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Ms.add(this.getTextOf(i));
            
            else
                if (Ms.size() > 0)
                    break;
        }
        
        if (Ms.size() == 0)
            return null;
        
        String[] MSs = new String[Ms.size()];
        for (int i = MSs.length; --i >= 0;)
            MSs[i] = Ms.get(MSs.length - i - 1);
        return MSs;
    }
    
    /** Returns the all the match */
    String[] getAllStrMatchesByName(String pName) {
        Vector<String> Ms = new Vector<String>();
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Ms.add(this.getTextOf(i));
        }
        
        if (Ms.size() == 0)
            return null;
        
        String[] MSs = new String[Ms.size()];
        for (int i = MSs.length; --i >= 0;)
            MSs[i] = Ms.get(MSs.length - i - 1);
        return MSs;
    }
    
    /** Returns the all the match */
    public int[][] getAllOfIndexMatchesByName(String pName) {
        Vector<int[]>   AMs = new Vector<int[]>();
        Vector<Integer> Ms  = new Vector<Integer>();
        for (int i = 0; i < this.entryCount(); i++) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Ms.add(i);
            
            else
                if (Ms.size() > 0) {
                    if (Ms.size() == 0)
                        continue;
                    
                    int[] MSs = new int[Ms.size()];
                    for (int m = MSs.length; --m >= 0;)
                        MSs[m] = Ms.get(MSs.length - m - 1);
                    AMs.add(MSs);
                    
                    Ms.clear();
                }
        }
        
        if (AMs.size() == 0)
            return null;
        
        int[][] AMSs = new int[AMs.size()][];
        for (int i = AMSs.length; --i >= 0;)
            AMSs[i] = AMs.get(AMSs.length - i - 1);
        return AMSs;
    }
    
    /** Returns the all the match */
    public String[][] getAllOfStrMatchesByName(String pName) {
        Vector<String[]> AMs = new Vector<String[]>();
        Vector<String>   Ms  = new Vector<String>();
        for (int i = 0; i < this.entryCount(); i++) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Ms.add(this.getTextOf(i));
            
            else
                if (Ms.size() > 0) {
                    if (Ms.size() == 0)
                        continue;
                    
                    String[] MSs = new String[Ms.size()];
                    for (int m = MSs.length; --m >= 0;)
                        MSs[m] = Ms.get(MSs.length - m - 1);
                    AMs.add(MSs);
                    
                    Ms.clear();
                }
        }
        
        if (AMs.size() == 0)
            return null;
        
        String[][] AMSs = new String[AMs.size()][];
        for (int i = AMSs.length; --i >= 0;)
            AMSs[i] = AMs.get(i);
        return AMSs;
    }
    
    /** Returns the last match */
    public Entry getLastMatchByName(String pName) {
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                return E;
        }
        return null;
    }
    
    /** Returns the last group of continuous match */
    public Entry[] getLastMatchesByName(String pName) {
        Vector<Entry> Es = new Vector<Entry>();
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Es.add(E);
            
            else
                if (Es.size() > 0)
                    break;
        }
        
        if (Es.size() == 0)
            return null;
        
        Entry[] ESs = new Entry[Es.size()];
        for (int i = ESs.length; --i >= 0;)
            ESs[i] = Es.get(ESs.length - i - 1);
        return ESs;
    }
    
    /** Returns the all the match */
    public Entry[] getAllMatchesByName(String pName) {
        Vector<Entry> Es = new Vector<Entry>();
        for (int i = this.entryCount(); --i >= 0;) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Es.add(E);
        }
        
        if (Es.size() == 0)
            return null;
        
        Entry[] ESs = new Entry[Es.size()];
        for (int i = ESs.length; --i >= 0;)
            ESs[i] = Es.get(ESs.length - i - 1);
        return ESs;
    }
    
    /** Returns the all the match */
    public Entry[][] getAllOfMatchesByName(String pName) {
        Vector<Entry[]> AEs = new Vector<Entry[]>();
        Vector<Entry>   Es  = new Vector<Entry>();
        for (int i = 0; i < this.entryCount(); i++) {
            Entry E = this.resultEntryAt(i);
            if (E.hasRPEntry() && pName.equals(E.parserEntry().name()))
                Es.add(E);
            
            else
                if (Es.size() > 0) {
                    if (Es.size() == 0)
                        continue;
                    
                    Entry[] ESs = new Entry[Es.size()];
                    for (int e = ESs.length; --e >= 0;)
                        ESs[e] = Es.get(ESs.length - e - 1);
                    AEs.add(ESs);
                    
                    Es.clear();
                }
        }
        
        if (AEs.size() == 0)
            return null;
        
        Entry[][] AESs = new Entry[AEs.size()][];
        for (int i = AESs.length; --i >= 0;)
            AESs[i] = AEs.get(AESs.length - i - 1);
        return AESs;
    }
    
    /** Returns how deep the parse result is nested */
    public int getDepth() {
        int D = 0;
        for (int i = this.entryCount(); --i >= 0;) {
            ParseResult PR = this.subResultAt(i);
            if (PR == null)
                continue;
            int d = PR.getDepth();
            if (D < d)
                D = d;
        }
        return D + 1;
    }
    
    // Easy to use utilities methods ------------------------------------------------------------------------
    // These method allow a short method name but with the assumption that the accessing name 
    //     refers to the last match only.
    // Some of these method are multiple access counterpart (added for completion) and the name
    //     access refers to all match
    
    // Message ------------------------------------------------------------------------------------
    
    static public final String WarningPrefix    = "$WARNING_";
    static public final String ErrorPrefix      = "$ERROR_";
    static public final String FatalErrorPrefix = "$FATAL_ERROR_";
    
    final public String getMessage(String pName) {
        StringBuffer SB = new StringBuffer();
        for (int i = 0; i < pName.length(); i++) {
            char C = pName.charAt(i);
            if (C == '_') {
                if (pName.startsWith("___")) {
                    SB.append("_");
                    i += 2;
                } else
                    SB.append(" ");
            } else
                SB.append(C);
        }
        String S = SB.toString();
        return S.endsWith("[]") ? S.substring(0, S.length() - 2) : S;
    }
    
    /** Detect and report all error and warning - returns if there is at least one error or warning */
    final public boolean hasNoError(PTypeProvider TProvider) {
        boolean HaveErrorOrWanrning = false;
        int     Count               = this.entryCount();
        for (int i = 0; i < Count; i++) {
            Entry E = this.resultEntryAt(i);
            if (E == null)
                continue;
            
            PType Type = E.getType();
            if (Type == null) {
                String TName = E.getTypeName();
                if (TName != null) {
                    Type = (TProvider == null) ? null : TProvider.getType(TName);
                    if (Type == null) {
                        Type = PTypeProvider.Extensible.getDefault().getType(TName);
                        if (Type == null)
                            throw new RuntimeException("Unknown type `" + TName + "`.");
                    }
                }
            }
            // Found an error type, so there is error
            if (Type instanceof PTError)
                return false;
            
            String Name = E.getName();
            if (Name != null) {
                if (Name.startsWith(WarningPrefix) || Name.startsWith(ErrorPrefix) || Name.startsWith(FatalErrorPrefix))
                    return false;
            }
            
            ParseResult Sub = this.subResultAt(i);
            if ((Sub != null) && !Sub.hasNoError(TProvider))
                return false;
        }
        return !HaveErrorOrWanrning;
    }
    
    /** Detect and report all error and warning - returns if there is at least one error or warning */
    final public boolean ensureNoError(PTypeProvider TProvider, CompilationContext CContext) {
        boolean HaveErrorOrWanrning = false;
        int     Count               = this.entryCount();
        for (int i = 0; i < Count; i++) {
            Entry E = this.resultEntryAt(i);
            if (E == null)
                continue;
            
            PType Type = E.getType();
            if (Type == null) {
                String TName = E.getTypeName();
                if (TName != null) {
                    Type = (TProvider == null) ? null : TProvider.getType(TName);
                    if (Type == null) {
                        Type = PTypeProvider.Extensible.getDefault().getType(TName);
                        if (Type == null)
                            throw new RuntimeException("Unknown type `" + TName + "`.");
                    }
                }
            }
            if (Type instanceof PTError) {
                // The type is an error type - let it solve it own business
                Type.compile(this, i, E.getTypeParam(), CContext, TProvider);
                HaveErrorOrWanrning = true;
                continue;
            }
            
            String Name = E.getName();
            if (Name != null) {
                boolean IsW = false;
                boolean IsE = false;
                boolean IsF = false;
                if ((IsW = Name.startsWith(WarningPrefix)) || (IsE = Name.startsWith(ErrorPrefix))
                        || (IsF = Name.startsWith(FatalErrorPrefix))) {
                    HaveErrorOrWanrning = true;
                    
                    int Kind_Length = 0;
                    if (IsW)
                        Kind_Length = WarningPrefix.length();
                    else
                        if (IsE)
                            Kind_Length = ErrorPrefix.length();
                        else
                            if (IsF)
                                Kind_Length = FatalErrorPrefix.length();
                            
                    String Msg = null;
                    Msg = (TProvider == null) ? null : TProvider.getErrorMessage(Name.substring(1));
                    // NOTE: 1 is to eliminate $ prefix >-----------------------------------^
                    Msg = (Msg != null) ? Msg : this.getMessage(Name.substring(Kind_Length, Name.length()));
                    CContext.reportError(Msg, null, this.getStartPositionOf(i));
                    if (IsF)
                        throw new RuntimeException("FATAL ERROR! The compilation cannot be continued: " + Msg);
                    
                }
            }
            
            ParseResult Sub = this.subResultAt(i);
            if ((Sub != null) && !Sub.ensureNoError(TProvider, CContext))
                return false;
        }
        return !HaveErrorOrWanrning;
    }
    
    // Entry --------------------------------------------------------------------------------------
    
    /** Returns the number of result entry of this result */
    final public int count() {
        return this.entryCount();
    }
    
    /** Returns the number of result entry at the position */
    final public ParseResult.Entry get(int I) {
        return this.resultEntryAt(I);
    }
    
    // Text value -----------------------------------------------------------------------
    
    /** Get text result of the last match */
    final public String textOf(int I) {
        return this.getTextOf(I);
    }
    
    /** Get text result of the last match */
    final public String textOf(String pEName) {
        return this.getTextOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get texts result of the last match */
    final public String[] textsOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new String[0];
        String[] PRENs = new String[Is.length];
        for (int i = PRENs.length; --i >= 0;)
            PRENs[i] = this.getTextOf(Is[i]);
        return PRENs;
    }
    
    // Entry Name -----------------------------------------------------------------------
    
    /** Get text result of the last match */
    final public String nameOf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        return PRE.getName();
    }
    
    /** Get text result of the last match */
    final public String nameOf(String pEName) {
        return this.nameOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get texts result of the last match */
    final public String[] namesOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public ParseResult subOf(int I) {
        return this.subResultAt(I);
    }
    
    /** Get sub result of the last match */
    final public ParseResult subOf(String pEName) {
        Entry E = this.getLastMatchByName(pEName);
        return ((E == null) || !E.hasSubResult()) ? null : E.subResult();
    }
    
    /** Get subs result of the last match */
    final public ParseResult[] subsOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public PType typeOf(int I, PTypeProvider TProvider) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.getType();
        if (PT != null)
            return PT;
        String TName = PRE.getTypeName();
        if (TName == null)
            return null;
        if (TProvider == null)
            return null;
        return TProvider.getType(TName);
    }
    
    /** Get Type of the last match */
    final public PType typeOf(String pEName, PTypeProvider TProvider) {
        return this.typeOf(this.getLastIndexOfEntryName(pEName), TProvider);
    }
    
    /** Get subs result of the last match */
    final public PType[] typesOf(String pEName, PTypeProvider TProvider) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public String typeNameOf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.getType();
        if (PT != null)
            return PT.name();
        return PRE.getTypeName();
    }
    
    /** Get Type name of the last match */
    final public String typeNameOf(String pEName) {
        return this.typeNameOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get subs result of the last match */
    final public String[] typeNamesOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public String typeParamOf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        PType PT = PRE.getType();
        if (PT != null)
            return null;
        return PRE.getTypeParam();
    }
    
    /** Get Type name of the last match */
    final public String typeParamOf(String pEName) {
        return this.typeParamOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get subs result of the last match */
    final public String[] typeParamsOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public Object valueOf(int I, PTypeProvider TProvider, CompilationContext CContext) {
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
    final public Object valueOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        return this.valueOf(this.getLastIndexOfEntryName(pEName), TProvider, CContext);
    }
    
    /** Get subs result of the last match */
    final public Object[] valuesOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public String valueAsTextOf(int I, PTypeProvider TProvider, CompilationContext CContext) {
        PType PT = this.typeOf(I, TProvider);
        if (PT == null) {
            String TName = this.typeNameOf(I);
            if (TName == null) {
                // The entry has no type, so compile each sub and concatenate them
                ParseResult Sub = this.subOf(I);
                int         SCount;
                if ((Sub == null) || ((SCount = Sub.count()) == 0))
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
    final public String valueAsTextOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        return this.valueAsTextOf(this.getLastIndexOfEntryName(pEName), TProvider, CContext);
    }
    
    /** Get compile value as text of the all match */
    final public String[] valueAsTextsOf(String pEName, PTypeProvider TProvider, CompilationContext CContext) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public int[] locationCROf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        return this.getLocationAsColRow(I);
    }
    
    /** Get locationRC of the last entry named pEName */
    final public int[] locationCROf(String pEName) {
        return this.locationCROf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get locationRC of the all entries named pEName */
    final public int[][] locationCRsOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public String locationOf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return null;
        return this.getLocationAsString(I);
    }
    
    /** Get location of the last entry named pEName */
    final public String locationOf(String pEName) {
        return this.locationOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get location of the all entries named pEName */
    final public String[] locationsOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
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
    final public int posOf(int I) {
        ParseResult.Entry PRE = this.resultEntryAt(I);
        if (PRE == null)
            return -1;
        return this.getStartPositionOf(I);
    }
    
    /** Get start position of the last entry named pEName */
    final public int posOf(String pEName) {
        return this.posOf(this.getLastIndexOfEntryName(pEName));
    }
    
    /** Get start positions of the all entries named pEName */
    final public int[] possOf(String pEName) {
        int[] Is = this.getAllIndexOfEntryName(pEName);
        if (Is == null)
            return null;
        if (Is.length == 0)
            return new int[0];
        int[] Ls = new int[Is.length];
        for (int i = Ls.length; --i >= 0;)
            Ls[i] = this.posOf(Is[i]);
        return Ls;
    }
    
    // Utilities ---------------------------------------------------------------------------------------------------
    
    /** Get Row (line number starts from 0) and Column of the index number */
    public int[] getLocationAsColRow(int pEntryIndex) {
        int Pos = this.getStartPositionOf(pEntryIndex);
        if (Pos == -1)
            return null;
        return getLocationAsColRow(this.originalText(), Pos);
    }
    
    /** Returns the string representation of the starting of the entry index */
    public String getLocationAsString(int pEntryIndex) {
        int Pos = this.getStartPositionOf(pEntryIndex);
        if (Pos == -1)
            return null;
        return getLocationAsString(this.originalText(), Pos);
    }
    
    /** Returns the string representation of the starting of the given text at the position (character number) */
    static public int[] getLocationAsColRow(CharSequence pOrgText, int pPosition) {
        if (pOrgText == null)
            return null;
        if (pPosition >= pOrgText.length())
            pPosition = pOrgText.length() - 1;
        if (pPosition <= 0)
            return new int[] { 0, 0 };
        int PrevLine   = 0;
        int LineCount  = 0;
        int TextLength = pOrgText.length();
        
        int[] CR = null;
        for (int i = 0; i < TextLength; i++) {
            if (pOrgText.charAt(i) == '\r') {
                if ((TextLength > i) && (pOrgText.charAt(i + 1) == '\n'))
                    i++;
            } else
                if (pOrgText.charAt(i) == '\n') {
                    if (pPosition <= i) {
                        // Found it
                        if (PrevLine != 0)
                            PrevLine++;
                        CR = new int[] { pPosition - PrevLine, LineCount };
                        break;
                    } else
                        PrevLine = i;
                    LineCount++;
                } else
                    continue;
        }
        
        // Not found yet, so it is the last line
        if (CR == null) {
            if (PrevLine != 0)
                PrevLine++;
            CR = new int[] { pPosition - PrevLine, LineCount };
        }
        
        return CR;
    }
    
    /** Returns the string representation of the starting of the given text at the position (character number) */
    static public String getLocationAsString(CharSequence pOrgText, int pPosition) {
        if (pOrgText == null)
            return null;
        if ((pPosition < 0) || (pPosition > pOrgText.length()))
            return null;
        
        boolean IsShift = false;
        if ((pPosition == pOrgText.length()) || (pOrgText.charAt(pPosition) == '\n')) {
            // Shift to avoid point at NewLine
            IsShift = true;
            pPosition--;
        }
        
        String OrgText       = pOrgText.toString();
        int    ThisLineBegin = OrgText.lastIndexOf("\n", pPosition);
        int    ThisLineEnd   = OrgText.indexOf("\n", pPosition);
        if (ThisLineEnd == -1)
            ThisLineEnd = OrgText.length();
        
        String ThisLine = OrgText.substring((ThisLineBegin + 1 <= ThisLineEnd) ? (ThisLineBegin + 1) : ThisLineEnd,
                ThisLineEnd);
        ThisLineBegin++;
        
        StringBuffer SB = new StringBuffer();
        
        // Print (Row, Col)
        int[] RC = getLocationAsColRow(pOrgText, pPosition);
        if (RC != null)
            SB.append("(").append(RC[1]).append(",").append(RC[0]).append(")\n");
        
        if (ThisLineBegin > 1) {
            int PrevLineBegin = OrgText.lastIndexOf("\n", ThisLineBegin - 2);
            if (PrevLineBegin == -1)
                PrevLineBegin = 0;
            String PrevLine = OrgText.substring(PrevLineBegin, ThisLineBegin - 1);
            SB.append("\n");
            SB.append("\t-|");
            for (int i = 0; i < PrevLine.length(); i++) {
                if (PrevLine.charAt(i) == '\t')
                    SB.append("    ");
                else
                    if (PrevLine.charAt(i) == '\n')
                        continue;
                    else
                        SB.append(PrevLine.charAt(i));
            }
        }
        
        SB.append("\n");
        SB.append("\t-|");
        SB.append((ThisLine.endsWith("\n")) ? ThisLine.substring(0, ThisLine.length() - 1) : ThisLine);
        
        pPosition -= ThisLineBegin;
        SB.append("\n");
        SB.append("\t-|");
        for (int i = 0; i < pPosition; i++) {
            if (ThisLine.charAt(i) != '\t')
                SB.append(' ');
            else
                SB.append("    ");
        }
        if (IsShift)
            SB.append(' ');
        SB.append('^');
        if (IsShift)
            SB.append("-- At the end of the line");
        SB.append('\n');
        return SB.toString();
    }
    
    // Object ------------------------------------------------------------------------------------
    
    /** Returns the detail string representation of the parse result */
    @Override
    public String toString() {
        return this.toString(0, 0);
    }
    
    /** An internal service for toDetail() */
    String toString(int pIndent, int pTab) {
        return this.toString(pIndent, pTab, this.getDepth() - 1);
    }
    
    /** An internal service for toDetail() */
    String toString(int pIndent, int pTab, int pDepth) {
        StringBuffer SB = new StringBuffer();
        SB.append("\n");
        int    Count = this.entryCount();
        int    Last  = Count - 1;
        String Tabs  = "";
        for (int i = pIndent; --i >= 0;)
            Tabs += "  ";
        for (int i = pTab; --i >= 0;)
            Tabs += ". ";
        
        for (int i = 0; i < Count; i++) {
            SB.append(Tabs);
            String Index = "" + i;
            while (Index.length() < 2)
                Index = "0" + Index;
            SB.append(Index);
            
            String Indents = "";
            for (int j = pDepth; --j >= 0;)
                Indents += " -";
            SB.append(Indents);
            SB.append(" => [");
            
            String End = "" + this.endPositionOf(i);
            while (End.length() < 5)
                End = " " + End;
            SB.append(End);
            SB.append("] = ");
            
            String Text = null;
            // Get the text
            if (this.getTextOf(i) == null)
                Text = "null ";
            else
                Text = "\"" + Util.escapeText(this.getTextOf(i)) + "\"";
            
            Entry RPT = this.resultEntryAt(i);
            if (RPT == null) {
                SB.append(Text);
                continue;
            }    // No Entry
            
            String PName = null;
            if ((RPT.parserEntry() != null) && (RPT.parserEntry().name() != null))
                PName = RPT.parserEntry().name();
            else
                PName = "<NoName>";
            while (PName.length() < 16)
                PName = PName + " ";
            SB.append(PName);
            
            SB.append(":");
            String TName = null;
            if (RPT.getType() != null)
                TName = RPT.getType().name();
            else
                if (RPT.getTypeRef() != null)
                    TName = RPT.getTypeRef().name();
                else
                    TName = "<NoType>";
            while (TName.length() < 16)
                TName = TName + " ";
            SB.append(TName);
            
            SB.append(" = ");
            SB.append(Text);
            
            if (RPT.hasSubResult()) {
                SB.append(RPT.subResult().toString(pIndent, pTab + 1, pDepth - 1));
            }
            if (i != Last)
                SB.append("\n");
        }
        return SB.toString();
    }
    
    // sub classes ---------------------------------------------------------------------------------
    
    static abstract protected class Normal extends ParseResult {
        
        static private final long serialVersionUID = 4121353565468546546L;
        
        protected Normal(int pStartPosition) {
            this(pStartPosition, null);
        }
        protected Normal(int pStartPosition, List<Entry> resultEntries) {
            super(resultEntries);
            this.StartPosition = pStartPosition;
        }
        
        int StartPosition = 0;
        
        @Override
        public int startPosition() {
            return this.StartPosition;
        }
    }
    
    static protected class Temp extends ParseResult {
        
        static private final long serialVersionUID = 3255656565625655652L;
        
        protected Temp(ParseResult pFirst) {
            this(pFirst, null);
        }
        
        protected Temp(ParseResult pFirst, List<Entry> resultEntries) {
            super(resultEntries);
            this.First = pFirst;
        }
        
        ParseResult First = null;
        
        @Override
        public int entryCount() {
            int         Count = super.entryCount();
            ParseResult F     = this.First;
            while (F instanceof Temp) {
                Count += ((F.resultEntries == null) ? 0 : F.resultEntries.size());
                F      = ((Temp) F).First;
            }
            return F.entryCount() + Count;
        }
        
        @Override
        public Entry resultEntryAt(int pIndex) {
            if ((pIndex < 0) || pIndex >= this.entryCount())
                return null;
            if (pIndex < this.First.entryCount()) {
                Temp T = this;
                while (pIndex < T.First.entryCount()) {
                    if (!(T.First instanceof Temp))
                        return T.First.resultEntryAt(pIndex);
                    T = (Temp) T.First;
                }
                return T.resultEntryAt(pIndex);
            }
            return this.entries().skip(pIndex - this.First.entryCount()).findFirst().orElse(null);
        }
        
        @Override
        public int startPosition() {
            return this.First.startPosition();
        }
        
        @Override
        public CharSequence originalText() {
            return this.First.originalText();
        }
        
        @Override
        ParseResult getDuplicate() {
            // This was initially implement using recursive but it was too slow.
            // The optimization is done by going to the root or the first 'First' part that is not a Temp and then all
            //     all entries from then down to the current Temp Result.
            if (!(this.First instanceof Temp)) {
                var resultEntries = this.entries().collect(Collectors.toList());
                Temp T = new Temp(this.First.getDuplicate(), resultEntries);
                return T;
            } else {
                Vector<Temp> Firsts = new Vector<Temp>();
                Firsts.add(this);
                Temp F = (Temp) this.First;
                Firsts.add(F);
                while (F.First instanceof Temp) {
                    F = (Temp) F.First;
                    Firsts.add(F);
                }
                
                var resultEntries = new ArrayList<Entry>();
                for (int i = Firsts.size(); --i >= 0;) {
                    if (Firsts.get(i).entries().count() == 0) {
                        continue;
                    }
                    resultEntries.addAll(Firsts.get(i).entryList());
                }
                Temp T = new Temp(F.First.getDuplicate(), resultEntries);
                return T;
            }
        }
    }
    
    /** Root Result */
    static protected class Root extends Normal {
        
        static private final long serialVersionUID = 2543546515135214354L;
        
        protected Root(int pStartPosition, CharSequence pOrgText) {
            this(pStartPosition, pOrgText, null);
        }
        
        protected Root(int pStartPosition, CharSequence pOrgText, List<Entry> resultEntries) {
            super(pStartPosition, resultEntries);
            this.OrgText = pOrgText;
        }
        
        CharSequence OrgText = null;
        
        @Override
        public CharSequence originalText() {
            return this.OrgText;
        }
        
        @Override
        ParseResult getDuplicate() {
            Root R = new Root(this.startPosition(), this.OrgText, this.entryList());
            return R;
        }
    }
    
    /** Node Result - For sub result*/
    static protected class Node extends Normal {
        
        static private final long serialVersionUID = 2545684654651635454L;
        
        protected Node(int pStartPosition, ParseResult pParseResult) {
            this(pStartPosition, pParseResult, null);
        }
        
        protected Node(int pStartPosition, ParseResult pParseResult, List<Entry> resultEntries) {
            super(pStartPosition, resultEntries);
            this.parent = pParseResult;
            for (int i = 0; i < this.parent.entryCount(); i++) {
                if (pStartPosition == this.parent.endPositionOf(i)) {
                    this.Index = i;
                    break;
                }
            }
        }
        
        ParseResult parent = null;
        int         Index  = 0;
        
        void parent(ParseResult parent) {
            this.parent = parent;
        }
        
        @Override
        protected ParseResult parent() {
            return this.parent;
        }
        
        @Override
        public CharSequence originalText() {
            return this.parent.originalText();
        }
        
        @Override
        ParseResult getDuplicate() {
            // Duplication of Node cannot be optimize the same way with Temp (by avoiding recursive) because Node hold
            //     structure that is important for verification and compilation.
            Node N = new Node(this.startPosition(), this.parent.getDuplicate(), this.entryList());
            N.Index = this.Index;
            return N;
        }
        
        // Get Element by name -----------------------------------------------------------------------
        
        @Override
        public HashSet<String> getAllNames() {
            HashSet<String> Ns = super.getAllNames();
            if (this.parent != null) {
                if (Ns == null)
                    return this.parent.getAllNames();
                HashSet<String> PNs = this.parent.getAllNames();
                if (PNs != null)
                    Ns.addAll(this.parent.getAllNames());
            }
            return Ns;
        }
        
        @Override
        public HashSet<String> getAllNames(String pPrefix) {
            HashSet<String> Ns = super.getAllNames(pPrefix);
            if (this.parent != null) {
                if (Ns == null)
                    return this.parent.getAllNames(pPrefix);
                HashSet<String> PNs = this.parent.getAllNames(pPrefix);
                if (PNs != null)
                    Ns.addAll(this.parent.getAllNames(pPrefix));
            }
            return Ns;
        }
        
        /**{@inheritDoc}*/
        @Override
        String getLastStrMatchByName(String pName) {
            String N = super.getLastStrMatchByName(pName);
            if (N != null)
                return N;
            if (this.parent != null)
                return this.parent.getLastStrMatchByName(pName);
            return null;
        }
        
        /**{@inheritDoc}*/
        @Override
        String[] getLastStrMatchesByName(String pName) {
            String[] S_Ms = super.getLastStrMatchesByName(pName);
            String[] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)];
            if (S_Ms != null)
                System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
            return Ms;
        }
        
        /**{@inheritDoc}*/
        @Override
        String[] getAllStrMatchesByName(String pName) {
            String[] S_Ms = super.getAllStrMatchesByName(pName);
            String[] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)];
            if (S_Ms != null)
                System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
            return Ms;
        }
        
        /** Returns the all the match */
        @Override
        public String[][] getAllOfStrMatchesByName(String pName) {
            String[][] S_Ms = super.getAllOfStrMatchesByName(pName);
            String[][] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)][];
            if (S_Ms != null) {
                int O = 0;
                for (int i = S_Ms.length; --i >= 0;)
                    Ms[O + i] = S_Ms[i].clone();
            }
            return Ms;
        }
        
        /** Returns the last match */
        @Override
        public Entry getLastMatchByName(String pName) {
            Entry E = super.getLastMatchByName(pName);
            if (E != null)
                return E;
            return null;
        }
        
        /** Returns the last group of continuous match */
        @Override
        public Entry[] getLastMatchesByName(String pName) {
            Entry[] S_Ms = super.getLastMatchesByName(pName);
            Entry[] Ms   = new Entry[((S_Ms == null) ? 0 : S_Ms.length)];
            if (S_Ms != null)
                System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
            return Ms;
        }
        
        /** Returns the all the match */
        @Override
        public Entry[] getAllMatchesByName(String pName) {
            Entry[] S_Ms = super.getAllMatchesByName(pName);
            Entry[] Ms   = new Entry[((S_Ms == null) ? 0 : S_Ms.length)];
            if (S_Ms != null)
                System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
            return Ms;
        }
        
        /** Returns the all the match */
        @Override
        public Entry[][] getAllOfMatchesByName(String pName) {
            Entry[][] P_Ms = (this.parent == null) ? null : this.parent.getAllOfMatchesByName(pName);
            Entry[][] S_Ms = super.getAllOfMatchesByName(pName);
            Entry[][] Ms   = new Entry[((S_Ms == null) ? 0 : S_Ms.length) + ((P_Ms == null) ? 0 : P_Ms.length)][];
            if (P_Ms != null)
                for (int i = P_Ms.length; --i >= 0;)
                    Ms[i] = P_Ms[i].clone();
            if (S_Ms != null) {
                int O = (P_Ms != null) ? P_Ms.length : 0;
                for (int i = S_Ms.length; --i >= 0;)
                    Ms[O + i] = S_Ms[i].clone();
            }
            return Ms;
        }
    }
    
    // Child classes -------------------------------------------------------------------------------
    
    static protected Entry newEntry(int pEndPosition) {
        return new Entry(pEndPosition);
    }
    
    static protected Entry newEntry(int pEndPosition, RPEntry pEntry) {
        if (pEntry == null)
            return new Entry(pEndPosition);
        else
            return new Entry_WithRPEntry(pEndPosition, pEntry);
    }
    
    static protected Entry newEntry(int pEndPosition, ParseResult pSubResult) {
        if (pSubResult == null)
            return new Entry(pEndPosition);
        else
            return new Entry_WithSub(pEndPosition, pSubResult);
    }
    
    static protected Entry newEntry(int pEndPosition, RPEntry pEntry, ParseResult pSubResult) {
        if (pEntry == null) {
            if (pSubResult == null)
                return new Entry(pEndPosition);
            else
                return new Entry_WithSub(pEndPosition, pSubResult);
        } else {
            if (pSubResult == null)
                return new Entry_WithRPEntry(pEndPosition, pEntry);
            else
                return new Entry_WithRPEntry_WithSub(pEndPosition, pEntry, pSubResult);
        }
    }
    
    /** The entry of the parse result */
    static public class Entry implements Serializable {
        
        static private final long serialVersionUID = 3256524452125552551L;
        
        protected Entry(int pEndPosition) {
            this.EndPosition = pEndPosition;
        }
        
        int EndPosition;
        
        public int getEndPosition() {
            return this.EndPosition;
        }
        
        public String getName() {
            return this.hasRPEntry() ? this.parserEntry().name() : null;
        }
        
        public PTypeRef getTypeRef() {
            return this.hasRPEntry() ? this.parserEntry().typeRef() : null;
        }
        
        public PType getType() {
            return this.hasRPEntry() ? this.parserEntry().type() : null;
        }
        
        public String getTypeName() {
            PTypeRef TR = this.getTypeRef();
            if (TR != null)
                return TR.name();
            PType T = this.getType();
            if (T != null)
                return T.name();
            return null;
        }
        
        public String getTypeParam() {
            PTypeRef TR = this.getTypeRef();
            if (TR != null)
                return TR.parameter();
            return null;
        }
        
        public boolean hasRPEntry() {
            return false;
        }
        
        public RPEntry parserEntry() {
            return null;
        }
        
        public boolean hasSubResult() {
            return false;
        }
        
        public ParseResult subResult() {
            return null;
        }
        
        @Override
        public String toString() {
            StringBuffer SB = new StringBuffer();
            SB.append("Entry").append(" { ");
            SB.append("End = ").append(this.EndPosition).append("; ");
            if (this.hasRPEntry())
                SB.append("RPEntry = ").append(this.parserEntry()).append("; ");
            if (this.hasSubResult())
                SB.append("Sub = ").append(this.subResult());
            SB.append("}");
            return SB.toString();
        }
    }
    
    static protected class Entry_WithSub extends Entry {
        
        static private final long serialVersionUID = 3256954552565455451L;
        
        protected Entry_WithSub(int pEndPosition, ParseResult pSubResult) {
            super(pEndPosition);
            this.SubResult = pSubResult;
        }
        
        ParseResult SubResult;
        
        @Override
        public boolean hasSubResult() {
            return (this.SubResult != null);
        }
        
        @Override
        public ParseResult subResult() {
            return this.SubResult;
        }
    }
    
    static protected class Entry_WithRPEntry extends Entry {
        
        static private final long serialVersionUID = 2254558458854566555L;
        
        protected Entry_WithRPEntry(int pEndPosition, RPEntry pEntry) {
            super(pEndPosition);
            this.RPEntry = pEntry;
        }
        
        RPEntry RPEntry;
        
        @Override
        public boolean hasRPEntry() {
            return (this.RPEntry != null);
        }
        
        @Override
        public RPEntry parserEntry() {
            return this.RPEntry;
        }
    }
    
    static protected class Entry_WithRPEntry_WithSub extends Entry_WithRPEntry {
        
        static private final long serialVersionUID = 2548545452415545545L;
        
        protected Entry_WithRPEntry_WithSub(int pEndPosition, RPEntry pEntry, ParseResult pSubResult) {
            super(pEndPosition, pEntry);
            this.SubResult = pSubResult;
        }
        
        ParseResult SubResult;
        
        @Override
        public boolean hasSubResult() {
            return (this.SubResult != null);
        }
        
        @Override
        public ParseResult subResult() {
            return this.SubResult;
        }
    }
}
