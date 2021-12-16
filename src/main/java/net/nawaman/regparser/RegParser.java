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
package net.nawaman.regparser;

import static net.nawaman.regparser.RegParserEntry.newParserEntry;
import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFixeds;
import net.nawaman.regparser.compiler.RPCharSetItemParserType;
import net.nawaman.regparser.compiler.RPCommentParserType;
import net.nawaman.regparser.compiler.RPEscapeHexParserType;
import net.nawaman.regparser.compiler.RPEscapeOctParserType;
import net.nawaman.regparser.compiler.RPEscapeParserType;
import net.nawaman.regparser.compiler.RPEscapeUnicodeParserType;
import net.nawaman.regparser.compiler.RPQuantifierParserType;
import net.nawaman.regparser.compiler.RPRangeParserType;
import net.nawaman.regparser.compiler.RPRegParserItemParserType;
import net.nawaman.regparser.compiler.RPRegParserParserType;
import net.nawaman.regparser.compiler.RPTypeParserType;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;
import net.nawaman.regparser.types.IdentifierParserType;
import net.nawaman.regparser.types.StringLiteralParserType;
import net.nawaman.regparser.types.TextCaseInsensitiveParserType;
import net.nawaman.regparser.utils.Util;

/**
 * The regular parser
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RegParser implements Checker, Serializable {
    
    static private final long serialVersionUID = 5789453645656854655L;
    
    /** Returns the empty array of RegParsers */
    static public final RegParser[] EmptyRegParserArray = new RegParser[0];
    static public final String      RegParserTypeExt    = "rpt";
    static public boolean           DebugMode           = false;
    static public PrintStream       DebugPrintStream    = null;
    
    static ParserTypeProvider.Extensible RPTProvider       = null;
    static String                        RegParserCompiler = "RegParserCompiler." + RegParserTypeExt;
    
    public RegParser(RegParserEntry[] entries) {
        this.Entries = entries;
    }
    
    public static RegParserBuilder newRegParser() {
    	return new RegParserBuilder();
    }
    
    public static RegParser newRegParser(String name, AsChecker checker) {
    	return new RegParserBuilder()
    			.entry(name, checker)
    			.build();
    }
    
    public static RegParser newRegParser(String name, ParserType parserType) {
    	return new RegParserBuilder()
    			.entry(name, parserType)
    			.build();
    }
    
    public static RegParser newRegParser(ParserType parserType) {
    	return new RegParserBuilder()
    			.entry(parserType)
    			.build();
    }
    
    public static RegParser newRegParser(ParserTypeRef parserTypeRef) {
    	return new RegParserBuilder()
    			.entry(parserTypeRef)
    			.build();
    }
    
    public static RegParser newRegParser(ParserTypeRef parserTypeRef, Quantifier quantifier) {
    	return new RegParserBuilder()
    			.entry(parserTypeRef, quantifier)
    			.build();
    }
    
    public static RegParser newRegParser(String name, ParserType parserType, Quantifier quantifier) {
    	return new RegParserBuilder()
    			.entry(name, parserType, quantifier)
    			.build();
    }
    
    public static RegParser newRegParser(String name, ParserTypeRef typeRef) {
    	return new RegParserBuilder()
    			.entry(name, typeRef)
    			.build();
    }
    
    public static RegParser newRegParser(String name, ParserTypeRef typeRef, Quantifier quantifier) {
    	return new RegParserBuilder()
    			.entry(name, typeRef, quantifier)
    			.build();
    }
    
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(AsRegParserEntry... pEntries) {
        return RegParser.newRegParser(null, pEntries);
    }
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(ParserTypeProvider pTProvider, AsRegParserEntry... pEntries) {
        if (pEntries == null)
            throw new NullPointerException();
        
        var entryArray 
				= Stream.of(pEntries)
				.filter (Objects::nonNull)
				.map    (AsRegParserEntry::asRegParserEntry)
				.toArray(RegParserEntry[]::new);
        return (pTProvider == null)
                ? new RegParser(entryArray)
                : new RegParser.WithDefaultTypeProvider(entryArray, pTProvider);
    }
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(List<RegParserEntry> pEntries) {
        return RegParser.newRegParser(null, pEntries);
    }
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(ParserTypeProvider pTProvider, List<RegParserEntry> pEntries) {
        if (pEntries == null)
            throw new NullPointerException();
        
        var entryArray = pEntries.stream().filter(Objects::nonNull).toArray(RegParserEntry[]::new);
        return (pTProvider == null)
                ? new RegParser(entryArray)
                : new RegParser.WithDefaultTypeProvider(entryArray, pTProvider);
    }
    
    /** Creates a new RegParser from a series of construction entries */
    public static RegParser newRegParser(String name, AsChecker checker, Quantifier quantifier) {
        return newRegParser(newParserEntry(name, checker, quantifier));
    }
    
    /** Creates a new RegParser from a series of construction entries */
    public static RegParser newRegParser(ParserTypeProvider pTProvider, String name, AsChecker checker, Quantifier quantifier) {
        return newRegParser(pTProvider, newParserEntry(name, checker, quantifier));
    }
    
    /** Creates a new RegParser from a series of construction entries */
    public static RegParser newRegParser(AsChecker checker, Quantifier quantifier) {
        return newRegParser(newParserEntry(checker, quantifier));
    }
    
    /** Creates a new RegParser from a series of construction entries */
    public static RegParser newRegParser(ParserTypeProvider pTProvider, AsChecker checker, Quantifier quantifier) {
        return newRegParser(pTProvider, newParserEntry(checker, quantifier));
    }
    
    /**
     * Create a new RegParser from a series of objects representing RegParser Entry
     * 
     * The entries must be in the following sequence: [Name], Checker, [Quantifier].
     * Name and Quantifier can be absent. If the name is absent, that entry has no name. If the quantifier is absent, 
     * the entry has the quantifier of one. 
     **/
    static public RegParser newRegParser(ParserTypeProvider pTProvider, Object... pParams) {
        if (pParams == null)
            throw new NullPointerException();
        Vector<RegParserEntry> RPEs = new Vector<RegParserEntry>();
        
        boolean    IsNew = false;
        String     N     = null;
        Checker    C     = null;
        ParserTypeRef   TR    = null;
        ParserType      T     = null;
        Quantifier Q     = null;
        
        boolean IsSkipped = false;
        for (int i = 0; i < pParams.length; i++) {
            Object O = pParams[i];
            if ((O instanceof AsRegParserEntry) && !(O instanceof Checker)) {
            	O = ((AsRegParserEntry)O).asRegParserEntry();
            }
            if (O instanceof RegParserEntry) {
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
                    else if (T != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
                    else if (TR != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
                    else
                        throw new IllegalArgumentException(
                                "Invalid parameters (" + Arrays.toString(pParams) + ").");
                    IsNew = false;
                    N     = null;
                    C     = null;
                    T     = null;
                    TR    = null;
                    Q     = null;
                }
                
                RPEs.add((RegParserEntry) O);
                IsSkipped = false;
                
            } else if ((O instanceof String) && !IsSkipped) {
                if (!((String) O).startsWith("$") && !((String) O).startsWith("#"))
                    throw new IllegalArgumentException(
                            "Name of RegParser entry must start with '$' or '#' (" + ((String) O) + ").");
                
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
                    else if (T != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
                    else if (TR != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
                    else
                        throw new IllegalArgumentException(
                                "Invalid parameters (" + Arrays.toString(pParams) + ").");
                    IsNew = false;
                    N     = null;
                    C     = null;
                    T     = null;
                    TR    = null;
                    Q     = null;
                }
                
                N         = (String) O;
                IsSkipped = false;
                
            } else if (O instanceof Checker) {
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
                    else if (T != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
                    else if (TR != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
                    else
                        throw new IllegalArgumentException(
                                "Invalid parameters (" + Arrays.toString(pParams) + ").");
                    IsNew = false;
                    N     = null;
                    C     = null;
                    T     = null;
                    TR    = null;
                    Q     = null;
                }
                
                IsNew     = true;
                C         = (Checker) O;
                IsSkipped = false;
                
            } else if (O instanceof ParserTypeRef) {
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
                    else if (T != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
                    else if (TR != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
                    else
                        throw new IllegalArgumentException(
                                "Invalid parameters (" + Arrays.toString(pParams) + ").");
                    IsNew = false;
                    N     = null;
                    C     = null;
                    T     = null;
                    TR    = null;
                    Q     = null;
                }
                
                IsNew     = true;
                TR        = (ParserTypeRef) O;
                IsSkipped = false;
                
            } else if (O instanceof ParserType) {
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
                    else if (T != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
                    else if (TR != null)
                        RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
                    else
                        throw new IllegalArgumentException(
                                "Invalid parameters (" + Arrays.toString(pParams) + ").");
                    IsNew = false;
                    N     = null;
                    C     = null;
                    T     = null;
                    TR    = null;
                    Q     = null;
                }
                
                IsNew     = true;
                T         = (ParserType) O;
                IsSkipped = false;
                
            } else if ((O instanceof Quantifier)
                    || ((O == null) && IsNew && ((C != null) || (T != null)))) {
                if (!IsNew || ((C == null) && (T == null) && (TR == null)) || (Q != null))
                    throw new IllegalArgumentException(
                            "Invalid parameters (" + Arrays.toString(pParams) + ").");
                
                Q         = (Quantifier) O;
                IsSkipped = false;
                
            } else {
                if ((O == null) && !IsNew && !IsSkipped)
                    IsSkipped = true;    // Skip for Name
                else
                    throw new IllegalArgumentException(
                            "Invalid parameters (" + Arrays.toString(pParams) + ").");
            }
        }
        if (IsNew || (N != null)) {
            if (C != null)
                RPEs.add(RegParserEntry.newParserEntry(N, C, Q));
            else if (T != null)
                RPEs.add(RegParserEntry.newParserEntry(N, T, Q));
            else if (TR != null)
                RPEs.add(RegParserEntry.newParserEntry(N, TR, Q));
            else
                throw new IllegalArgumentException("Invalid parameters (" + Arrays.toString(pParams) + ").");
        }
        
        RegParser RP = (pTProvider == null)
                ? new RegParser(RPEs.toArray(RegParserEntry.EmptyRegParserEntryArray))
                : new RegParser.WithDefaultTypeProvider(RPEs.toArray(RegParserEntry.EmptyRegParserEntryArray), pTProvider);
        return RP;
    }
    
    /** Compiles a new RegParser from a RegParser code */
    static public RegParser newRegParser(String pText) {
        return RegParser.newRegParser(null, pText);
    }
    
    /** Compiles a new RegParser from a RegParser code */
    static public RegParser newRegParser(ParserTypeProvider pTProvider, String pText) {
        boolean IsToSave = true;
        
        if (RPTProvider == null) {
            // Try to load from Resource
            try {
                ParserTypeProvider PT = (ParserTypeProvider.Extensible) (Util
                        .loadObjectsFromStream(ClassLoader.getSystemResourceAsStream(RegParserCompiler))[0]);
                RPTProvider = (ParserTypeProvider.Extensible) PT;
                IsToSave    = false;
            } catch (Exception E) {
            }
            
            // Try to load from local file
            if (RPTProvider == null) {
                try {
                    ParserTypeProvider PT = (ParserTypeProvider.Extensible) Util.loadObjectsFromFile(RegParserCompiler)[0];
                    RPTProvider = (ParserTypeProvider.Extensible) PT;
                    IsToSave    = false;
                } catch (Exception E) {
                }
            }
            
            // Try to create one
            if (RPTProvider == null) {
                RPTProvider = new ParserTypeProvider.Extensible();
                RPTProvider.addType(TextCaseInsensitiveParserType.instance);
                // Add the type
                RPTProvider.addType(IdentifierParserType.instance);
                RPTProvider.addType(StringLiteralParserType.instance);
                RPTProvider.addType(RPCommentParserType.instance);
                RPTProvider.addType(RPTypeParserType.instance);
                RPTProvider.addType(RPQuantifierParserType.instance);
                RPTProvider.addType(RPRegParserItemParserType.instance);
                RPTProvider.addType(RPEscapeParserType.instance);
                RPTProvider.addType(RPEscapeOctParserType.instance);
                RPTProvider.addType(RPEscapeHexParserType.instance);
                RPTProvider.addType(RPEscapeUnicodeParserType.instance);
                RPTProvider.addType(RPRangeParserType.instance);
                RPTProvider.addType(RPCharSetItemParserType.instance);
                RPTProvider.addType(RPRegParserParserType.instance);
            }
        } else
            IsToSave = false;
        
        ParserType     RPT = RPTProvider.type(RPRegParserParserType.name);
        RegParser RP  = (RegParser) (RPT.compile(pText, null, null, RPTProvider));
        
        // If have type provider
        if ((RP != null) && (pTProvider != null)) {
            RegParserEntry[] Es = RP.Entries;
            RP         = new RegParser.WithDefaultTypeProvider(Es, pTProvider);
        }
        
        if (IsToSave) {
            // Try to get checker of every all type in the provider so that when it is saved
            Set<String> Ns = RPTProvider.typeNames();
            for (String N : Ns) {
                ParserType RPType = RPTProvider.type(N);
                RPType.checker(null, null, RPTProvider);
            }
            
            // Save for later use
            try {
                Util.saveObjectsToFile(RegParserCompiler, new Serializable[] { RPTProvider });
            } catch (Exception E) {
            }
        }
        
        return RP;
    }
    
    // Data ------------------------------------------------------------------------------------------------------------
    
    private final RegParserEntry[] Entries;
    
    public Stream<RegParserEntry> entries() {
        // TODO - Rethink this.
        return (Entries == null) ? null : Stream.of(Entries);
    }
    
    /** Returns the number of RegParser entry this RegParser composes of */
    public int getEntryCount() {
        return (this.Entries == null) ? 0 : this.Entries.length;
    }
    
    public RegParserEntry getEntryAt(int pIndex) {
        if ((pIndex < 0) || (pIndex >= this.getEntryCount()))
            return null;
        return this.Entries[pIndex];
    }
    
    // Default TypePackage ---------------------------------------------------------------------------------------------
    
    ParserTypeProvider getDefaultTypeProvider() {
        return null;
    }
    
    /** TypeProvider with Default Type */
    static public class WithDefaultTypeProvider extends RegParser {
        
        private static final long serialVersionUID = -7904907723214116873L;
        
        static public RegParser attachDefaultTypeProvider(RegParser pRegParser, ParserTypeProvider pTProvider) {
            if ((pRegParser == null) || (pTProvider == null))
                return pRegParser;
            
            ParserTypeProvider PTProvider = pRegParser.getDefaultTypeProvider();
            if ((pRegParser instanceof WithDefaultTypeProvider) && (PTProvider instanceof ParserTypeProvider.Library))
                ((ParserTypeProvider.Library) PTProvider).addProvider(pTProvider);
            else {
                var parserEntries = ((RegParser)(pRegParser)).entries();
                var entries = (parserEntries == null) ? null : parserEntries.toArray(RegParserEntry[]::new);
                    var TProvider = ((PTProvider != null) && (PTProvider != pTProvider))
                            ? null
                            : new ParserTypeProvider.Library(pTProvider, PTProvider);
                pRegParser = new WithDefaultTypeProvider(entries, TProvider);
            }
            return pRegParser;
        }
        
        WithDefaultTypeProvider(RegParserEntry[] Entries, ParserTypeProvider pTProvider) {
            super(Entries);
            this.TProvider = pTProvider;
        }
        
        ParserTypeProvider TProvider = null;
        
        @Override
        ParserTypeProvider getDefaultTypeProvider() {
            return this.TProvider;
        }
        
        // Parse
        
        /**{@inheritDoc}*/
        @Override
        /** Returns the length of the match if the text is start with a match or -1 if not */
        protected ParseResult parse(CharSequence pText, int pOffset, int pIndex, int pTimes, ParseResult pResult,
                ParserTypeProvider pProvider, ParserType pRPType, String pRPTParam, int pTabs) {
            ParserTypeProvider TP = ParserTypeProvider.Library.either(pProvider, this.TProvider);
            return super.parse(pText, pOffset, pIndex, pTimes, pResult, TP, pRPType, pRPTParam, pTabs);
        }
        
    }
    
    
    // Public services -------------------------------------------------------------------------------------------------
    
    // Parse - as far as it can go.
    
    /** Returns the the match if the text is start with a match or -1 if not */
    public ParseResult parse(CharSequence pText) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, null, null, null, 0);
        if (PR != null)
            PR.collapse(null);
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    public ParseResult parse(CharSequence pText, int pOffset) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, null, null, null, 0);
        if (PR != null)
            PR.collapse(null);
        return PR;
    }
    
    /** Returns the the match if the text is start with a match or -1 if not */
    public ParseResult parse(CharSequence pText, ParserTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, pProvider, null, null, 0);
        if (PR != null)
            PR.collapse(pProvider);
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    public ParseResult parse(CharSequence pText, int pOffset, ParserTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, pProvider, null, null, 0);
        if (PR != null)
            PR.collapse(pProvider);
        return PR;
    }
    
    // Match - to the end or fail.
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    public ParseResult match(CharSequence pText) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, null, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.endPosition() != pText.length())
                return null;
            PR.collapse(null);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    public ParseResult match(CharSequence pText, int pOffset, int pEndPosition) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, null, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.endPosition() != pEndPosition)
                return null;
            PR.collapse(null);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    public ParseResult match(CharSequence pText, ParserTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, pProvider, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.endPosition() != pText.length())
                return null;
            PR.collapse(pProvider);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    public ParseResult match(CharSequence pText, int pOffset, int pEndPosition, ParserTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, pProvider, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.endPosition() != pEndPosition)
                return null;
            PR.collapse(pProvider);
        }
        return PR;
    }
    
    // Internal --------------------------------------------------------------------------------------------------------
    
    /** Parse an entry at the index pIndex possessively */
    protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, ParseResult pResult,
            ParserTypeProvider pProvider, int pTabs) {
        String   FN  = this.Entries[pIndex].name();
        ParserTypeRef FTR = this.Entries[pIndex].typeRef();
        ParserType    FT  = this.Entries[pIndex].type();
        Checker  FP  = this.Entries[pIndex].checker();
        
        return this.parseEach_P(pText, pOffset, pIndex, FN, FT, FTR, FP, pResult, pProvider, pTabs);
    }
    
    /** Parse an entry possessively */
    protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, String FN, ParserType FT, ParserTypeRef FTR,
            Checker FP, ParseResult pResult, ParserTypeProvider pProvider, int pTabs) {
        try {
    	
    	
    	
        boolean IsFPType          = (FT != null) || (FTR != null);
        boolean IsFPName          = (FN != null);
        boolean IsFPAsNode        = ((FN == null) && IsFPType) || ((FN != null) && !FN.startsWith("$"));
        boolean IsFPRegParser     = (!IsFPType && (FP instanceof RegParser));
        boolean IsFPAlternative   = (!IsFPType && !IsFPRegParser && (FP instanceof CheckerAlternative));
        boolean IsFPGroup         = (!IsFPType && !IsFPRegParser && !IsFPAlternative && (FP instanceof CheckerFixeds));
        boolean IsFPNormalChecker = !IsFPType && !IsFPRegParser && !IsFPAlternative && !IsFPAlternative && !IsFPGroup;
        
        // NOTE: If FPType or named, parse it then validate and record separately from the current result
        // NOTE: If FPRegParser, parse it and record within the current result
        // NOTE: If FPNormalChecker, getLengthOfStartOf then recored it with in the current result
        // NOTE: If Alternative, find the longest match
        
        if (IsFPAlternative) {
            int         MaxEnd    = Integer.MIN_VALUE;
            ParseResult MaxResult = null;
            
            CheckerAlternative CA = (CheckerAlternative) FP;
            var checkers = CA.checkers().toArray(Checker[]::new);
            for (int c = checkers.length; --c >= 0;) {
                var TryResult 
                        = IsFPAsNode
                        ? newResult(pOffset, pResult)
                        : newResult(pResult);
                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, checkers[c], TryResult, pProvider,
                        pTabs) == null)
                    continue;
                
                // Find the longest length
                if (MaxEnd >= TryResult.endPosition())
                    continue;
                MaxResult = TryResult;
                MaxEnd    = TryResult.endPosition();
            }
            if (MaxResult == null) {
                if (!CA.hasDefault())
                    return null;
                
                var TryResult
                        = IsFPAsNode
                        ? newResult(pOffset, pResult)
                        : newResult(pResult);
                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), TryResult, pProvider,
                        pTabs) == null)
                    return null;
                
                MaxResult = TryResult;
                MaxEnd    = TryResult.endPosition();
            }
            if (IsFPAsNode) {
                pResult.append(newEntry(MaxResult.endPosition(), this.Entries[pIndex], MaxResult));
            } else {
                if (IsFPType || IsFPName)
                    pResult.append(newEntry(MaxResult.endPosition(), this.Entries[pIndex]));
                else
                    pResult.mergeWith((TemporaryParseResult) MaxResult);
            }
            return pResult;
            
        } else
            if (IsFPNormalChecker) {
                if (pOffset >= pText.length())
                    return null; //pResult;
                    
                int REC      = pResult.rawEntryCount();
                int LengthFP = FP.startLengthOf(pText, pOffset, pProvider, pResult);
                if (LengthFP == -1) {
                    // Recover what may have been added in the fail attempt
                    pResult.reset(REC);
                    return null;
                }
                if (IsFPName)
                    pResult.append(newEntry(pOffset + LengthFP, this.Entries[pIndex]));
                else
                    pResult.append(newEntry(pOffset + LengthFP));
                
                return pResult;
                
            } else
                if (IsFPRegParser) {    // RegParser
                    if (IsFPName) {
                        ParseResult TryResult = newResult(pOffset, pResult);
                        if ((((RegParser) FP).parse(pText, pOffset, 0, 0, TryResult, pProvider, null, null,
                                pTabs + 1)) == null)
                            return null;
                        
                        // Merge the result
                        if (IsFPAsNode) {
                            pResult.append(newEntry(TryResult.endPosition(), this.Entries[pIndex], TryResult));
                        } else {
                            if (IsFPType || IsFPName)
                                pResult.append(newEntry(TryResult.endPosition(), this.Entries[pIndex]));
                            else
                                pResult.append(newEntry(TryResult.endPosition()));
                        }
                        return pResult;
                        
                    } else {
                        int REC = pResult.rawEntryCount();
                        if ((((RegParser) FP).parse(pText, pOffset, 0, 0, pResult, pProvider, null, null,
                                pTabs)) == null) {
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            return null;
                        }
                        return pResult;
                        
                    }
                    
                } else
                    if (IsFPType) {    // RegParser with a type or a type ref
                        
                        int         EndPosition = -1;
                        ParseResult ThisResult  = null;
                        
                        String Param = null;
                        
                        if (FTR != null) {
                            Param = FTR.parameter();
                            // Get type from the ref
                            if (pProvider != null) {
                                // Get from the given provider
                                FT = pProvider.type(FTR.name());
                            }
                            if (FT == null) {
                                // Get from the default
                                FT = ParserTypeProvider.Simple.defaultProvider().type(FTR.name());
                                if (FT == null) {
                                    throw new ParsingException(
                                            "RegParser type named '" + FTR.name() + "' is not found.");
                                }
                            }
                        }
                        
                        // Extract a type
                        if (FT != null) {
                            FP = FT.checker(pResult, Param, pProvider);
                            if (FP == null)
                                throw new ParsingException("RegParser type named '" + FTR + "' has no checker.");
                        }
                        
                        // If type is a text, FP is not a node
                        if (FT.isText())
                            IsFPAsNode = false;
                        
                        if (FP instanceof RegParser) {
                            // The type contain a RegParser
                            ParseResult TryResult = newResult(pOffset, pResult);
                            TryResult = ((RegParser) FP).parse(pText, pOffset, 0, 0, TryResult, pProvider,
                                    ((FT != null) && FT.isSelfContain()) ? null : FT,
                                    ((FT != null) && FT.isSelfContain()) ? null : Param, pTabs + 1);
                            if (TryResult == null)
                                return null;
                            
                            EndPosition = TryResult.endPosition();
                            ThisResult  = TryResult;
                            
                        } else
                            if (FP instanceof CheckerAlternative) {
                                // The type contain a alternative checker
                                int         MaxEnd    = Integer.MIN_VALUE;
                                ParseResult MaxResult = null;
                                
                                CheckerAlternative CA = (CheckerAlternative) FP;
                                var checkers = CA.checkers().toArray(Checker[]::new);
                                for (int c = checkers.length; --c >= 0;) {
                                    var TryResult
                                            = IsFPAsNode
                                            ? newResult(pOffset, pResult)
                                            : newResult(pResult);
                                    if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, checkers[c],
                                            TryResult, pProvider, pTabs + 1) == null)
                                        continue;
                                    
                                    // Find the longest length
                                    if (MaxEnd >= TryResult.endPosition())
                                        continue;
                                    MaxResult = TryResult;
                                    MaxEnd    = TryResult.endPosition();
                                }
                                if (MaxResult == null) {
                                    if (!CA.hasDefault())
                                        return null;
                                    
                                    var TryResult
                                            = IsFPAsNode
                                            ? newResult(pOffset, pResult)
                                            : newResult(pResult);
                                    if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(),
                                            TryResult, pProvider, pTabs + 1) == null)
                                        return null;
                                    
                                    MaxResult = TryResult;
                                    MaxEnd    = TryResult.endPosition();
                                }
                                EndPosition = MaxEnd;
                                ThisResult  = MaxResult;
                            } else
                                if (FP instanceof CheckerFixeds) {
                                    CheckerFixeds CG = ((CheckerFixeds) FP);
                                    // Check if there enough space for it
                                    if (pText.length() >= (pOffset + CG.neededLength())) {
                                        EndPosition = pOffset;
                                        ThisResult  = newResult(pOffset, pResult);
                                        for (int i = 0; i < CG.entryCount(); i++) {
                                            int l = CG.entry(i).length();
                                            if (l != -1)
                                                EndPosition += l;
                                            else
                                                EndPosition = pText.length();
                                            ThisResult.append(newEntry(EndPosition, CG.entry(i).entry()));
                                        }
                                    }
                                } else {
                                    // The type contain a checker
                                    int REC      = pResult.rawEntryCount();
                                    int LengthFP = FP.startLengthOf(pText, pOffset, pProvider, pResult);
                                    if (LengthFP == -1) {
                                        // Recover what may have been added in the fail attempt
                                        pResult.reset(REC);
                                        return null;
                                    }
                                    
                                    EndPosition = pOffset + LengthFP;
                                    ThisResult  = newResult(pOffset, pResult);
                                    ThisResult.append(newEntry(pOffset + LengthFP, this.Entries[pIndex]));
                                    
                                }
                            
                        if ((FT != null) && FT.hasValidation() && FT.isSelfContain()) {
                            ParseResult PR = ThisResult.duplicate();
                            PR.collapse(pProvider);
                            // In case of type, do validation
                            if (!FT.validate(pResult, PR, Param, pProvider))
                                return null;
                        }
                        
                        // Append the result
                        if (IsFPAsNode) {
                            pResult.append(newEntry(EndPosition, this.Entries[pIndex], ThisResult));
                        } else {
                            if (IsFPType || IsFPName)
                                pResult.append(newEntry(EndPosition, this.Entries[pIndex]));
                            else
                                pResult.append(newEntry(EndPosition));
                        }
                        
                        // Return the result
                        return pResult;
                    } else
                        if (IsFPGroup) {
                            CheckerFixeds CG = ((CheckerFixeds) FP);
                            // There is not enough space for it
                            if (pText.length() < (pOffset + CG.neededLength()))
                                return null;
                            
                            int EndPosition = pOffset;
                            for (int i = 0; i < CG.entryCount(); i++) {
                                int l = CG.entry(i).length();
                                if (l != -1)
                                    EndPosition += l;
                                else
                                    EndPosition = pText.length();
                                pResult.append(newEntry(EndPosition, CG.entry(i).entry()));
                            }
                            
                            // Return the result
                            return pResult;
                        }
                    
        return null;
        
        } catch (NullPointerException e) {
        	e.printStackTrace();
        	System.out.println("pText: "     + pText);
        	System.out.println("pOffset: "   + pOffset);
        	System.out.println("pIndex: "    + pIndex);
        	System.out.println("FN: "        + FN);
        	System.out.println("FT: "        + FT);
        	System.out.println("FTR: "       + FTR);
        	System.out.println("FP: "        + FP);
        	System.out.println("pResult: "   + pResult);
        	System.out.println("pProvider: " + pProvider);
        	System.out.println("pTabs: "     + pTabs);
        	throw e;
		}
    }
    
    /** Cache for Tabs */
    static Vector<String> Tabs = new Vector<String>();
    
    /** Returns the length of the match if the text is start with a match or -1 if not */
    protected ParseResult parse(CharSequence pText, int pOffset, int pIndex, int pTimes, ParseResult pResult,
            ParserTypeProvider pProvider, ParserType pRPType, String pRPTParam, int pTabs) {
        
        // If the entry has a name, ask it to parse with a new parse result 
        if (pResult == null)
            pResult = ParseResult.newResult(pOffset, pText);
        if (pText == null)
            return pResult;
        
        // NOTE: If Zero, The first must not match and the later must match
        // NOTE: If Possessive, Try to match first until not match or limit then match the later (the later must match)
        //                      If alternative, each first match must be longest
        // NOTE: If Maximum, Try to match the first then the later and the last, pick the longest match
        // NOTE: If Minimum, Check first match until reaching the lower bound, the try the full length until match.
        
        int LastEntryIndex = this.Entries.length - 1;
        int EntryLength    = this.Entries.length;
        int TextLength     = pText.length();
        
        String StrTabs = "";
        if (DebugMode) {
            while (pTabs >= Tabs.size()) {
                if (Tabs.size() == 0)
                    Tabs.add("");
                if (StrTabs == "")
                    StrTabs = Tabs.get(Tabs.size() - 1);
                StrTabs += "  ";
                Tabs.add(StrTabs);
            }
            
            if (pTabs <= Tabs.size())
                StrTabs = Tabs.get(pTabs);
            else {
                for (int i = pTabs; --i >= 0;)
                    StrTabs += "  ";
                Tabs.add(StrTabs);
            }
        }
        
        MainLoop: while (true) {
            
            if (pIndex > LastEntryIndex)
                break MainLoop;
            
            pOffset = pResult.endPosition();
            if (pOffset < 0)
                return null;
            
            Quantifier FPQ = this.Entries[pIndex].quantifier();
            if (FPQ == null)
                FPQ = Quantifier.One;
            
            // Premature ending by hitting the end of the text
            if (pOffset >= TextLength) {
                boolean ToTry = false;
                // If the current multiple matchings end prematurely
                if (pTimes < FPQ.lowerBound()) {
                    RegParserEntry RPE = this.Entries[pIndex];
                    Checker C   = RPE.checker();
                    // If this is a normal checker, return null
                    if (!((C instanceof RegParser) || (C instanceof CheckerAlternative)
                            || ((C == null) && ((RPE.type() != null) || (RPE.typeRef() != null)))))
                        return null;
                    
                    ToTry = true;
                }
                
                if (!ToTry) {
                    // Check the rest of the Entry
                    TryLoop: for (int i = (pIndex + 1); i < EntryLength; i++) {
                        RegParserEntry RPE = this.Entries[i];
                        
                        Checker    C = RPE.checker();
                        Quantifier Q = RPE.quantifier();
                        // If the rest of the entry is not optional 
                        if ((Q == null) || (Q.lowerBound() != 0)) {
                            // Inside a RegParser or Alternative with Q.LowerBound == 1, there may be a checker with {0}
                            // Inside.
                            if (((Q == null) || (Q.lowerBound() == 1)) && ((C instanceof RegParser)
                                    || (C instanceof CheckerAlternative)
                                    || ((C == null) && ((RPE.type() != null) || (RPE.typeRef() != null))))) {
                                ToTry = true;
                                
                                // Try the entry i
                                pIndex = i;
                                pTimes = 0;
                                
                                FPQ = this.Entries[pIndex].quantifier();
                                if (FPQ == null)
                                    FPQ = Quantifier.One;
                                
                                break TryLoop;
                            }
                            
                            return null;
                        }
                    }
                }
                
                // There are more to parse but they all optional
                if (!ToTry)
                    break MainLoop;
            }
            
            // TOHIDE
            /* */
            if (RegParser.DebugMode) {
                if (DebugPrintStream == null)
                    DebugPrintStream = System.out;
                DebugPrintStream.println(StrTabs + pResult.toString(pTabs, 0));
                DebugPrintStream.println(
                        StrTabs + "----------------------------------------------------------------------------------");
                String T = null;
                if (TextLength >= 50)
                    T = pText.subSequence(pOffset, ((pOffset + 50) >= TextLength) ? TextLength : (pOffset + 50))
                            .toString() + "...";
                else
                    T = pText.subSequence(pOffset, TextLength).toString();
                DebugPrintStream.println(StrTabs + "`" + Util.escapeText(T) + "` ~ "
                        + ((pRPType != null) ? pRPType + "(" + pRPTParam + ") ~ " : "") + this.Entries[pIndex]
                        + ((pTimes != 0) ? "(" + pTimes + ")" : ""));
            }
            /* */
            
            if (FPQ.isPossessive()) {
                
                if (FPQ.isOne_Possessive()) {    // Match one
                    int REC = pResult.rawEntryCount();
                    if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) == null) {
                        // Recover what may have been added in the fail attempt
                        pResult.reset(REC);
                        return null;
                    }
                    
                    // To the next entry, so restart the repeat
                    pIndex++;
                    pTimes = 0;
                    continue;
                    
                } else
                    if (FPQ.isZero()) {        // Match Zero
                        int REC = pResult.rawEntryCount();
                        if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            return null;
                        }
                        // Append an empty entry when found zero (if named or typed)
                        if ((this.Entries[pIndex].name() != null) || (this.Entries[pIndex].type() != null)
                                || (this.Entries[pIndex].typeRef() != null))
                            pResult.append(ParseResultEntry.newEntry(pOffset, this.Entries[pIndex]));
                        
                        // To the next entry, so change the entry index and restart the repeat
                        pIndex++;
                        pTimes = 0;
                        continue;
                        
                    } else {
                        
                        // Is it any
                        RegParserEntry RPE = this.Entries[pIndex];
                        if (RPE.checker() == PredefinedCharClasses.Any) {
                            if ((RPE.name() == null) && (RPE.typeRef() == null) && (RPE.type() == null)) {
                                // Is this limited - Match till the limit
                                int LB = RPE.quantifier().lowerBound();
                                if (pOffset + LB <= TextLength) {    // There is enough space for the minimum (the lower bound)
                                    int UB = RPE.quantifier().upperBound();
                                    if (UB != -1) {    // With limit
                                        if (pOffset + UB <= TextLength) { // Can it contain the maximum
                                            // Take the minimum
                                            pResult.append(newEntry(pOffset + UB));
                                        } else {    // Take what it can
                                            pResult.append(newEntry(TextLength));
                                        }
                                    } else {    // Is no limit - Match till the end
                                        pResult.append(newEntry(TextLength));
                                    }
                                    // To the next entry, so change the entry index and restart the repeat
                                    pIndex++;
                                    pTimes = 0;
                                    continue;
                                } else {    // Need more, return as not match
                                    // Recover what may have been added in the fail attempt
                                    return null;
                                }
                            }
                        }
                        
                        int REC = pResult.rawEntryCount();
                        
                        // Check if it reaches the maximum
                        if ((FPQ.hasNoUpperBound()) || (pTimes < FPQ.upperBound())) {    // Not yet
                            int FREC = pResult.rawEntryCount();
                            // Try the first part
                            if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {    // Match
                                // Only the one that advances the parsing
                                if ((FREC != pResult.rawEntryCount()) && (pOffset != pResult.endPosition())) {
                                    pTimes++;
                                    continue;
                                }
                            }
                            // Recover what may have been added in the fail attempt
                            pResult.reset(FREC);
                            
                        }
                        // Check if it fail to reach the minimum, return as not found
                        if (pTimes < FPQ.lowerBound()) {
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            return null;
                        }
                        
                        // To the next entry, so change the entry index and restart the repeat
                        pIndex++;
                        pTimes = 0;
                        continue;
                        
                    }
                
            } else
                if (FPQ.isMaximum()) {
                    
                    // Check if it reaches the maximum
                    if ((FPQ.hasNoUpperBound()) || (pTimes < FPQ.upperBound())) {    // Not yet
                        
                        ParserType    FT  = this.Entries[pIndex].type();
                        ParserTypeRef FTR = this.Entries[pIndex].typeRef();
                        Checker  FP  = this.Entries[pIndex].checker();
                        
                        boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
                                && (FP instanceof CheckerAlternative);
                        
                        if (IsFPAlternative) {
                            
                            int             MaxLength = Integer.MIN_VALUE;
                            TemporaryParseResult MaxResult = null;
                            
                            // Not yet
                            CheckerAlternative CA = (CheckerAlternative) FP;
                            var checkers = CA.checkers().toArray(Checker[]::new);
                            for (int c = checkers.length; --c >= 0;) {
                                Checker C = checkers[c];
                                // Try the first part
                                TemporaryParseResult TryResult = newResult(pResult);
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, C, TryResult, pProvider,
                                        pTabs) != null) {
                                    // Match
                                    // Try the later part, if not match, continue other alternatives
                                    if (this.parse(pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult,
                                            pProvider, pRPType, pRPTParam, pTabs) == null)
                                        continue;
                                    
                                    // Match, so record as max
                                    // Find the longer length
                                    if (MaxLength >= TryResult.endPosition())
                                        continue;
                                    MaxResult = TryResult;
                                    MaxLength = TryResult.endPosition();
                                    
                                    if ((MaxLength + pOffset) >= TextLength)
                                        break;
                                }
                            }
                            
                            if (MaxResult != null) {
                                // Merge the result if found.
                                pResult.mergeWith(MaxResult);
                                break MainLoop;
                            }
                            
                            if (CA.hasDefault()) {
                                int REC = pResult.rawEntryCount();
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), pResult,
                                        pProvider, pTabs) != null) {
                                    // Found the match.
                                    break MainLoop;
                                }
                                // Recover what may have been added in the fail attempt
                                pResult.reset(REC);
                            }
                            
                        } else {
                            int REC = pResult.rawEntryCount();
                            // Try the first part
                            if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
                                // Try the first part again. If match, return 
                                if (this.parse(pText, pResult.endPosition(), pIndex, pTimes + 1, pResult, pProvider,
                                        pRPType, pRPTParam, pTabs) != null) {
                                    // Found the match.
                                    break MainLoop;
                                }
                            }
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            // If not found any, try to parse the last part
                        }
                    }
                    // Check if it fail to reach the minimum, return as not found
                    if (pTimes < FPQ.lowerBound())
                        return null;
                    
                    // To the next entry, so change the entry index and restart the repeat
                    pIndex++;
                    pTimes = 0;
                    continue;
                    
                } else
                    if (FPQ.isMinimum()) {
                        
                        // Check if it has reach the minimum
                        if (pTimes >= FPQ.lowerBound()) {
                            // Try the last part
                            int REC = pResult.rawEntryCount();
                            // Parse the last part. If match, return
                            if (this.parse(pText, pOffset, pIndex + 1, 0, pResult, pProvider, pRPType, pRPTParam,
                                    pTabs) != null) {
                                // Found the match.
                                break MainLoop;
                            }
                            
                            // Else continue, the next loop
                            
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                        }
                        
                        // Check it reach the maximum
                        if ((FPQ.hasUpperBound()) && (pTimes >= FPQ.upperBound()))
                            return null; // Yes
                            
                        ParserType    FT  = this.Entries[pIndex].type();
                        ParserTypeRef FTR = this.Entries[pIndex].typeRef();
                        Checker  FP  = this.Entries[pIndex].checker();
                        
                        boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
                                && (FP instanceof CheckerAlternative);
                        
                        if (IsFPAlternative) {
                            
                            int             MinLength = Integer.MAX_VALUE;
                            TemporaryParseResult MinResult = null;
                            
                            // Not yet
                            CheckerAlternative CA = (CheckerAlternative) FP;
                            var checkers = CA.checkers().toArray(Checker[]::new);
                            for (int c = checkers.length; --c >= 0;) {
                                // Try the first part
                                var TryResult = newResult(pResult);
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, checkers[c],
                                        TryResult, pProvider, pTabs) != null) {
                                    // Match
                                    // Try the later part, if not match, continue other alternatives
                                    if (this.parse(pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult,
                                            pProvider, pRPType, pRPTParam, pTabs) == null)
                                        continue;
                                    
                                    // Match, so record as max
                                    // Find the longer length
                                    if (MinLength <= TryResult.endPosition())
                                        continue;
                                    MinResult = TryResult;
                                    MinLength = TryResult.endPosition();
                                    
                                    if ((MinLength + pOffset) >= pOffset)
                                        break;
                                }
                            }
                            
                            if (MinResult != null) {
                                // Merge the best result if found.
                                pResult.mergeWith(MinResult);
                                break MainLoop;
                            }
                            
                            if (CA.hasDefault()) {
                                int REC = pResult.rawEntryCount();
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), pResult,
                                        pProvider, pTabs) != null) {
                                    // Found the match.
                                    break MainLoop;
                                }
                                // Recover what may have been added in the fail attempt
                                pResult.reset(REC);
                                // Not found, return as not found
                                return null;
                            }
                            return null;
                            
                        } else {
                            int REC = pResult.rawEntryCount();
                            // Try the first part
                            if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
                                pTimes++;
                                continue;
                                
                            }
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            // Not found, return as not found
                            return null;
                        }
                    }
                
            return null;
        }
        
        // Validate the value here
        if ((pRPType != null) && pRPType.hasValidation() && !pRPType.isSelfContain()) {
            ParseResult PR = pResult.duplicate();
            PR.collapse(pProvider);
            ParseResult Host = (PR instanceof ParseResultNode) ? ((ParseResultNode) PR).parent() : null;
            if (!pRPType.validate(Host, PR, pRPTParam, pProvider))
                return null;
        }
        
        // TOHIDE
        /* */
        if (RegParser.DebugMode) {
            if (DebugPrintStream == null)
                DebugPrintStream = System.out;
            DebugPrintStream.println(StrTabs + pResult.toString(pTabs, 0));
            DebugPrintStream.println(
                    StrTabs + "----------------------------------------------------------------------------------");
        }
        /* */
        return pResult;
    }
    
    /** Return the optimized version of this Checker */
    public Checker optimize() {
        if ((this.Entries.length == 1) && (this.Entries[0].name() == null) && (this.Entries[0].typeRef() == null)
                && ((this.Entries[0].quantifier() == null) || this.Entries[0].quantifier().isOne_Possessive())) {
            return this.Entries[0].checker();
        }
        
		var     newEntries = new RegParserEntry[this.Entries.length];
		boolean isChanged  = false;
		
		for (int i = 0; i < this.Entries.length; i++) {
			var entry      = this.Entries[i];
			var checker    = entry.checker();
			var name       = entry.name();
			var typeRef    = entry.typeRef();
			var type       = entry.type();
			var quantifier = entry.quantifier();
			if (typeRef != null) {
				newEntries[i] = RegParserEntry.newParserEntry(name, typeRef, quantifier);
			} else if (type != null) {
				newEntries[i] = RegParserEntry.newParserEntry(name, type, quantifier);
			} else {
				if (checker instanceof RegParser) {
					var newParser = ((RegParser)checker).optimize();
					if (newParser != checker) {
						newEntries[i] = RegParserEntry.newParserEntry(name, newParser, quantifier);
						isChanged     = true;
					} else {
						newEntries[i] = RegParserEntry.newParserEntry(name, checker, quantifier);
					}
				} else if (checker != null) {
					newEntries[i] = RegParserEntry.newParserEntry(name, checker, quantifier);
				} else {
					throw new NullPointerException("`checker` is null.");
				}
			}
		}
		
		return isChanged ? newRegParser((RegParserEntry[])newEntries) : this;
	}
    
    // To Satisfy Checker ----------------------------------------------------------------------------------------------
    
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    public int startLengthOf(CharSequence S, int pOffset, ParserTypeProvider pProvider) {
        return this.startLengthOf(S, pOffset, pProvider, null);
    }
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int startLengthOf(CharSequence S, int pOffset, ParserTypeProvider pProvider, ParseResult pResult) {
        if (pResult == null) {
            ParseResult PR = this.parse(S.toString(), pOffset, pProvider);
            if (PR == null)
                return -1;
            return PR.endPosition() - pOffset;
        }
        
        int REC = pResult.rawEntryCount();
        if ((this.parse(S, pOffset, 0, 0, pResult, pProvider, null, null, 0)) == null) {
            // Recover what may have been added in the fail attempt
            pResult.reset(REC);
            return -1;
        }
        return pResult.endPosition() - pOffset;
    }
    
    @Override
    public String toString() {
        StringBuffer SB = new StringBuffer();
        //SB.append("(");
        for (int i = 0; i < this.Entries.length; i++) {
            SB.append(this.Entries[i].toString());
        }
        //SB.append(")");
        return SB.toString();
    }
    
    @Override
    public boolean equals(Object O) {
        if (O == this)
            return true;
        if (!(O instanceof RegParser))
            return false;
        return Arrays.equals(this.Entries, ((RegParser) O).Entries);
        
    }
}
