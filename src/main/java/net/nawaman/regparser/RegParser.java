/*----------------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2008-2019 Nawapunth Manusitthipol. Implements with and for Sun Java 1.6 JDK.
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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import net.nawaman.regparser.RPCompiler_ParserTypes.RPTCharSetItem;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTComment;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscape;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeHex;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeOct;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeUnicode;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTQuantifier;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRange;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParser;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParserItem;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTType;
import net.nawaman.regparser.parsers.PTIdentifier;
import net.nawaman.regparser.parsers.PTStrLiteral;

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
    
    static PTypeProvider.Extensible RPTProvider       = null;
    static String                   RegParserCompiler = "RegParserCompiler." + RegParserTypeExt;
    
    RegParser() {
    }
    
    static public class ConstructionEntry {
        
        public ConstructionEntry(Checker pChecker) {
            this(null, (Object) pChecker, null, null);
        }
        
        public ConstructionEntry(Checker pChecker, Checker pSecondStage) {
            this(null, (Object) pChecker, null, pSecondStage);
        }
        
        public ConstructionEntry(Checker pChecker, Quantifier pQuantifier) {
            this(null, (Object) pChecker, pQuantifier, null);
        }
        
        public ConstructionEntry(Checker pChecker, Quantifier pQuantifier, Checker pSecondStage) {
            this(null, (Object) pChecker, pQuantifier, pSecondStage);
        }
        
        public ConstructionEntry(String pName, Checker pChecker) {
            this(pName, (Object) pChecker, null, null);
        }
        
        public ConstructionEntry(String pName, Checker pChecker, Checker pSecondStage) {
            this(pName, (Object) pChecker, null, pSecondStage);
        }
        
        public ConstructionEntry(String pName, Checker pChecker, Quantifier pQuantifier) {
            this(pName, (Object) pChecker, pQuantifier, null);
        }
        
        public ConstructionEntry(String pName, Checker pChecker, Quantifier pQuantifier, Checker pSecondStage) {
            this(pName, (Object) pChecker, pQuantifier, pSecondStage);
        }
        
        public ConstructionEntry(PType pPType) {
            this(null, (Object) pPType, null, null);
        }
        
        public ConstructionEntry(PType pPType, Checker pSecondStage) {
            this(null, (Object) pPType, null, pSecondStage);
        }
        
        public ConstructionEntry(PType pPType, Quantifier pQuantifier) {
            this(null, (Object) pPType, pQuantifier, null);
        }
        
        public ConstructionEntry(PType pPType, Quantifier pQuantifier, Checker pSecondStage) {
            this(null, (Object) pPType, pQuantifier, pSecondStage);
        }
        
        public ConstructionEntry(String pName, PType pPType) {
            this(pName, (Object) pPType, null, null);
        }
        
        public ConstructionEntry(String pName, PType pPType, Checker pSecondStage) {
            this(pName, (Object) pPType, null, pSecondStage);
        }
        
        public ConstructionEntry(String pName, PType pPType, Quantifier pQuantifier) {
            this(pName, (Object) pPType, pQuantifier, null);
        }
        
        public ConstructionEntry(String pName, PType pPType, Quantifier pQuantifier, Checker pSecondStage) {
            this(pName, (Object) pPType, pQuantifier, pSecondStage);
        }
        
        public ConstructionEntry(PTypeRef pPTypeRef) {
            this(null, (Object) pPTypeRef, null, null);
        }
        
        public ConstructionEntry(PTypeRef pPTypeRef, Checker pSecondStage) {
            this(null, (Object) pPTypeRef, null, pSecondStage);
        }
        
        public ConstructionEntry(PTypeRef pPTypeRef, Quantifier pQuantifier) {
            this(null, (Object) pPTypeRef, pQuantifier, null);
        }
        
        public ConstructionEntry(PTypeRef pPTypeRef, Quantifier pQuantifier, Checker pSecondStage) {
            this(null, (Object) pPTypeRef, pQuantifier, pSecondStage);
        }
        
        public ConstructionEntry(String pName, PTypeRef pPTypeRef) {
            this(pName, (Object) pPTypeRef, null, null);
        }
        
        public ConstructionEntry(String pName, PTypeRef pPTypeRef, Checker pSecondStage) {
            this(pName, (Object) pPTypeRef, null, pSecondStage);
        }
        
        public ConstructionEntry(String pName, PTypeRef pPTypeRef, Quantifier pQuantifier) {
            this(pName, (Object) pPTypeRef, pQuantifier, null);
        }
        
        public ConstructionEntry(String pName, PTypeRef pPTypeRef, Quantifier pQuantifier, Checker pSecondStage) {
            this(pName, (Object) pPTypeRef, pQuantifier, pSecondStage);
        }
        
        ConstructionEntry(String pName, Object pItem, Quantifier pQuantifier, Checker pSecondStage) {
            this.Name        = pName;
            this.Item        = pItem;
            this.Quantifier  = pQuantifier;
            this.SecondStage = pSecondStage;
        }
        
        String     Name;
        Object     Item;
        Quantifier Quantifier;
        Checker    SecondStage = null;
        
        public String getName() {
            return this.Name;
        }
        
        public Checker getChecker() {
            return (this.Item instanceof Checker) ? (Checker) this.Item : null;
        }
        
        public PType getType() {
            return (this.Item instanceof PType) ? (PType) this.Item : null;
        }
        
        public PTypeRef getTypeRef() {
            return (this.Item instanceof PTypeRef) ? (PTypeRef) this.Item : null;
        }
        
        public boolean isChecker() {
            return (this.Item instanceof Checker);
        }
        
        public boolean isType() {
            return (this.Item instanceof PType);
        }
        
        public boolean isTypeRef() {
            return (this.Item instanceof PTypeRef);
        }
        
        public Quantifier getQuantifier() {
            return this.Quantifier;
        }
        
        public boolean hasSecondStage() {
            return (this.SecondStage != null);
        }
        
        public Checker getSecondStage() {
            return this.SecondStage;
        }
        
        /**{@inheritDoc}*/
        @Override
        public String toString() {
            String Format = (this.Name == null) ? "%s%s" : "(" + this.Name + ":%s%s)";
            return String.format(Format, this.Item, (this.Quantifier == null) ? "" : this.Quantifier);
        }
    }
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(ConstructionEntry... pEntries) {
        return RegParser.newRegParser(null, pEntries);
    }
    
    /** Creates a new RegParser from a series of construction entries */
    static public RegParser newRegParser(PTypeProvider pTProvider, ConstructionEntry... pEntries) {
        if (pEntries == null)
            throw new NullPointerException();
        
        Vector<RPEntry> RPEs = new Vector<RPEntry>();
        for (int i = 0; i < pEntries.length; i++) {
            ConstructionEntry CE = pEntries[i];
            if (CE == null)
                continue;
            
            if (CE.isChecker())
                RPEs.add(RPEntry._new(CE.getName(), CE.getChecker(), CE.getQuantifier()));
            else
                if (CE.isType())
                    RPEs.add(RPEntry._new(CE.getName(), CE.getType(), CE.getQuantifier()));
                else
                    if (CE.isTypeRef())
                        RPEs.add(RPEntry._new(CE.getName(), CE.getTypeRef(), CE.getQuantifier()));
        }
        
        RegParser RP = (pTProvider == null) ? new RegParser() : new RegParser.WithDefaultTypeProvider(pTProvider);
        RP.Entries = RPEs.toArray(RPEntry.EmptyRPEntryArray);
        
        return RP;
    }
    
    /**
     * Create a new RegParser from a series of objects representing RegParser Entry
     * 
     * The entries must be in the following sequence:
     *         [Name], Checker, [Quantifier]
     *     OR  [Name], Type,    [Quantifier]
     *     OR  [Name], TypeRef, [Quantifier]
     * .
     * Name and Quantifier can be absent. If the name is absent, that entry has no name. If the quantifier is absent, 
     * the entry has the quantifier of one. 
     **/
    static public RegParser newRegParser(Object... pParams) {
        return RegParser.newRegParser(null, pParams);
    }
    
    
    /**
     * Create a new RegParser from a series of objects representing RegParser Entry
     * 
     * The entries must be in the following sequence: [Name], Checker, [Quantifier].
     * Name and Quantifier can be absent. If the name is absent, that entry has no name. If the quantifier is absent, 
     * the entry has the quantifier of one. 
     **/
    static public RegParser newRegParser(PTypeProvider pTProvider, Object... pParams) {
        if (pParams == null)
            throw new NullPointerException();
        Vector<RPEntry> RPEs = new Vector<RPEntry>();
        
        boolean    IsNew = false;
        String     N     = null;
        Checker    C     = null;
        PTypeRef   TR    = null;
        PType      T     = null;
        Quantifier Q     = null;
        
        boolean IsSkipped = false;
        for (int i = 0; i < pParams.length; i++) {
            Object O = pParams[i];
            if (O instanceof RPEntry) {
                if (IsNew) {
                    if (C != null)
                        RPEs.add(RPEntry._new(N, C, Q));
                    else
                        if (T != null)
                            RPEs.add(RPEntry._new(N, T, Q));
                        else
                            if (TR != null)
                                RPEs.add(RPEntry._new(N, TR, Q));
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
                
                RPEs.add((RPEntry) O);
                IsSkipped = false;
                
            } else
                if ((O instanceof String) && !IsSkipped) {
                    if (!((String) O).startsWith("$") && !((String) O).startsWith("#"))
                        throw new IllegalArgumentException(
                                "Name of RegParser entry must start with '$' or '#' (" + ((String) O) + ").");
                    
                    if (IsNew) {
                        if (C != null)
                            RPEs.add(RPEntry._new(N, C, Q));
                        else
                            if (T != null)
                                RPEs.add(RPEntry._new(N, T, Q));
                            else
                                if (TR != null)
                                    RPEs.add(RPEntry._new(N, TR, Q));
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
                    
                } else
                    if (O instanceof Checker) {
                        if (IsNew) {
                            if (C != null)
                                RPEs.add(RPEntry._new(N, C, Q));
                            else
                                if (T != null)
                                    RPEs.add(RPEntry._new(N, T, Q));
                                else
                                    if (TR != null)
                                        RPEs.add(RPEntry._new(N, TR, Q));
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
                        
                    } else
                        if (O instanceof PTypeRef) {
                            if (IsNew) {
                                if (C != null)
                                    RPEs.add(RPEntry._new(N, C, Q));
                                else
                                    if (T != null)
                                        RPEs.add(RPEntry._new(N, T, Q));
                                    else
                                        if (TR != null)
                                            RPEs.add(RPEntry._new(N, TR, Q));
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
                            TR        = (PTypeRef) O;
                            IsSkipped = false;
                            
                        } else
                            if (O instanceof PType) {
                                if (IsNew) {
                                    if (C != null)
                                        RPEs.add(RPEntry._new(N, C, Q));
                                    else
                                        if (T != null)
                                            RPEs.add(RPEntry._new(N, T, Q));
                                        else
                                            if (TR != null)
                                                RPEs.add(RPEntry._new(N, TR, Q));
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
                                T         = (PType) O;
                                IsSkipped = false;
                                
                            } else
                                if ((O instanceof Quantifier)
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
                RPEs.add(RPEntry._new(N, C, Q));
            else
                if (T != null)
                    RPEs.add(RPEntry._new(N, T, Q));
                else
                    if (TR != null)
                        RPEs.add(RPEntry._new(N, TR, Q));
                    else
                        throw new IllegalArgumentException("Invalid parameters (" + Arrays.toString(pParams) + ").");
        }
        
        RegParser RP = (pTProvider == null) ? new RegParser() : new RegParser.WithDefaultTypeProvider(pTProvider);
        RP.Entries = RPEs.toArray(RPEntry.EmptyRPEntryArray);
        
        return RP;
    }
    
    /** Compiles a new RegParser from a RegParser code */
    static public RegParser newRegParser(String pText) {
        return RegParser.newRegParser(null, pText);
    }
    
    /** Compiles a new RegParser from a RegParser code */
    static public RegParser newRegParser(PTypeProvider pTProvider, String pText) {
        boolean IsToSave = true;
        
        if (RPTProvider == null) {
            // Try to load from Resource
            try {
                PTypeProvider PT = (PTypeProvider.Extensible) (Util
                        .loadObjectsFromStream(ClassLoader.getSystemResourceAsStream(RegParserCompiler))[0]);
                RPTProvider = (PTypeProvider.Extensible) PT;
                IsToSave    = false;
            } catch (Exception E) {
            }
            
            // Try to load from local file
            if (RPTProvider == null) {
                try {
                    PTypeProvider PT = (PTypeProvider.Extensible) Util.loadObjectsFromFile(RegParserCompiler)[0];
                    RPTProvider = (PTypeProvider.Extensible) PT;
                    IsToSave    = false;
                } catch (Exception E) {
                }
            }
            
            // Try to create one
            if (RPTProvider == null) {
                RPTProvider = new PTypeProvider.Extensible();
                RPTProvider.addType(new PTTextCI());
                // Add the type
                RPTProvider.addType(new PTIdentifier());
                RPTProvider.addType(new PTStrLiteral());
                RPTProvider.addType(new RPTComment());
                RPTProvider.addType(new RPTType());
                RPTProvider.addType(new RPTQuantifier());
                RPTProvider.addType(new RPTRegParserItem());
                RPTProvider.addType(new RPTEscape());
                RPTProvider.addType(new RPTEscapeOct());
                RPTProvider.addType(new RPTEscapeHex());
                RPTProvider.addType(new RPTEscapeUnicode());
                RPTProvider.addType(new RPTRange());
                RPTProvider.addType(new RPTCharSetItem());
                RPTProvider.addType(new RPTRegParser());
            }
        } else
            IsToSave = false;
        
        PType     RPT = RPTProvider.getType(RPTRegParser.Name);
        RegParser RP  = (RegParser) (RPT.compile(pText, null, null, RPTProvider));
        
        // If have type provider
        if ((RP != null) && (pTProvider != null)) {
            RPEntry[] Es = RP.Entries;
            RP         = new RegParser.WithDefaultTypeProvider(pTProvider);
            RP.Entries = Es;
        }
        
        if (IsToSave) {
            // Try to get checker of every all type in the provider so that when it is saved
            Set<String> Ns = RPTProvider.getAllTypeNames();
            for (String N : Ns) {
                PType RPType = RPTProvider.getType(N);
                RPType.getChecker(null, null, RPTProvider);
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
    
    RPEntry[] Entries = null;
    
    /** Returns the number of RegParser entry this RegParser composes of */
    public int getEntryCount() {
        return (this.Entries == null) ? 0 : this.Entries.length;
    }
    
    public RPEntry getEntryAt(int pIndex) {
        if ((pIndex < 0) || (pIndex >= this.getEntryCount()))
            return null;
        return this.Entries[pIndex];
    }
    
    // Default TypePackage ---------------------------------------------------------------------------------------------
    
    PTypeProvider getDefaultTypeProvider() {
        return null;
    }
    
    /** TypeProvider with Default Type */
    @SuppressWarnings("serial")
    static public class WithDefaultTypeProvider extends RegParser {
        
        static public RegParser attachDefaultTypeProvider(RegParser pRegParser, PTypeProvider pTProvider) {
            if ((pRegParser == null) || (pTProvider == null))
                return pRegParser;
            
            PTypeProvider PTProvider = pRegParser.getDefaultTypeProvider();
            if ((pRegParser instanceof WithDefaultTypeProvider) && (PTProvider instanceof PTypeProvider.Library))
                ((PTypeProvider.Library) PTProvider).addProvider(pTProvider);
            else {
                WithDefaultTypeProvider RPWDTP = new WithDefaultTypeProvider(PTProvider);
                RPWDTP.Entries = pRegParser.Entries;
                if ((PTProvider != null) && (PTProvider != pTProvider))
                    RPWDTP.TProvider = new PTypeProvider.Library(pTProvider, PTProvider);
                pRegParser = RPWDTP;
            }
            return pRegParser;
        }
        
        WithDefaultTypeProvider(PTypeProvider pTProvider) {
            this.TProvider = pTProvider;
        }
        
        PTypeProvider TProvider = null;
        
        @Override
        PTypeProvider getDefaultTypeProvider() {
            return this.TProvider;
        }
        
        // Parse
        
        /**{@inheritDoc}*/
        @Override
        /** Returns the length of the match if the text is start with a match or -1 if not */
        protected ParseResult parse(CharSequence pText, int pOffset, int pIndex, int pTimes, ParseResult pResult,
                PTypeProvider pProvider, PType pRPType, String pRPTParam, int pTabs) {
            PTypeProvider TP = PTypeProvider.Library.getEither(pProvider, this.TProvider);
            return super.parse(pText, pOffset, pIndex, pTimes, pResult, TP, pRPType, pRPTParam, pTabs);
        }
        
    }
    
    
    // Public services -------------------------------------------------------------------------------------------------
    
    // Parse
    
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
    public ParseResult parse(CharSequence pText, PTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, pProvider, null, null, 0);
        if (PR != null)
            PR.collapse(pProvider);
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from pOffset on) or -1 if not */
    public ParseResult parse(CharSequence pText, int pOffset, PTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, pProvider, null, null, 0);
        if (PR != null)
            PR.collapse(pProvider);
        return PR;
    }
    
    // Match
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    public ParseResult match(CharSequence pText) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, null, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.getEndPosition() != pText.length())
                return null;
            PR.collapse(null);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    public ParseResult match(CharSequence pText, int pOffset, int pEndPosition) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, null, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.getEndPosition() != pEndPosition)
                return null;
            PR.collapse(null);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the end) or -1 if not */
    public ParseResult match(CharSequence pText, PTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, 0, 0, 0, null, pProvider, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.getEndPosition() != pText.length())
                return null;
            PR.collapse(pProvider);
        }
        return PR;
    }
    
    /** Returns the match if the text is start with a match (from start to the pEndPosition) or -1 if not */
    public ParseResult match(CharSequence pText, int pOffset, int pEndPosition, PTypeProvider pProvider) {
        ParseResult PR = this.parse(pText, pOffset, 0, 0, null, pProvider, null, null, 0);
        if ((PR != null) && (pText != null)) {
            if (PR.getEndPosition() != pEndPosition)
                return null;
            PR.collapse(pProvider);
        }
        return PR;
    }
    
    // Internal --------------------------------------------------------------------------------------------------------
    
    /** Parse an entry at the index pIndex possessively */
    protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, ParseResult pResult,
            PTypeProvider pProvider, int pTabs) {
        String   FN  = this.Entries[pIndex].getName();
        PTypeRef FTR = this.Entries[pIndex].getTypeRef();
        PType    FT  = this.Entries[pIndex].getType();
        Checker  FP  = this.Entries[pIndex].getChecker();
        
        return this.parseEach_P(pText, pOffset, pIndex, FN, FT, FTR, FP, pResult, pProvider, pTabs);
    }
    
    /** Parse an entry possessively */
    protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, String FN, PType FT, PTypeRef FTR,
            Checker FP, ParseResult pResult, PTypeProvider pProvider, int pTabs) {
        
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
            for (int c = CA.Checkers.length; --c >= 0;) {
                ParseResult TryResult = IsFPAsNode ? new ParseResult.Node(pOffset, pResult)
                        : new ParseResult.Temp(pResult);
                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.Checkers[c], TryResult, pProvider,
                        pTabs) == null)
                    continue;
                
                // Find the longest length
                if (MaxEnd >= TryResult.getEndPosition())
                    continue;
                MaxResult = TryResult;
                MaxEnd    = TryResult.getEndPosition();
            }
            if (MaxResult == null) {
                if (!CA.hasDefault())
                    return null;
                
                ParseResult TryResult = IsFPAsNode ? new ParseResult.Node(pOffset, pResult)
                        : new ParseResult.Temp(pResult);
                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.getDefault(), TryResult, pProvider,
                        pTabs) == null)
                    return null;
                
                MaxResult = TryResult;
                MaxEnd    = TryResult.getEndPosition();
            }
            if (IsFPAsNode) {
                pResult.append(ParseResult.newEntry(MaxResult.getEndPosition(), this.Entries[pIndex], MaxResult));
            } else {
                if (IsFPType || IsFPName)
                    pResult.append(ParseResult.newEntry(MaxResult.getEndPosition(), this.Entries[pIndex]));
                else
                    pResult.mergeWith((ParseResult.Temp) MaxResult);
            }
            return pResult;
            
        } else
            if (IsFPNormalChecker) {
                if (pOffset >= pText.length())
                    return null; //pResult;
                    
                int REC      = pResult.getEntryListSize();
                int LengthFP = FP.getStartLengthOf(pText, pOffset, pProvider, pResult);
                if (LengthFP == -1) {
                    // Recover what may have been added in the fail attempt
                    pResult.reset(REC);
                    return null;
                }
                if (IsFPName)
                    pResult.append(ParseResult.newEntry(pOffset + LengthFP, this.Entries[pIndex]));
                else
                    pResult.append(ParseResult.newEntry(pOffset + LengthFP));
                
                return pResult;
                
            } else
                if (IsFPRegParser) {    // RegParser
                    if (IsFPName) {
                        ParseResult TryResult = new ParseResult.Node(pOffset, pResult);
                        if ((((RegParser) FP).parse(pText, pOffset, 0, 0, TryResult, pProvider, null, null,
                                pTabs + 1)) == null)
                            return null;
                        
                        // Merge the result
                        if (IsFPAsNode) {
                            pResult.append(
                                    ParseResult.newEntry(TryResult.getEndPosition(), this.Entries[pIndex], TryResult));
                        } else {
                            if (IsFPType || IsFPName)
                                pResult.append(ParseResult.newEntry(TryResult.getEndPosition(), this.Entries[pIndex]));
                            else
                                pResult.append(ParseResult.newEntry(TryResult.getEndPosition()));
                        }
                        return pResult;
                        
                    } else {
                        int REC = pResult.getEntryListSize();
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
                            Param = FTR.getParam();
                            // Get type from the ref
                            if (pProvider != null) {
                                // Get from the given provider
                                FT = pProvider.getType(FTR.getName());
                            }
                            if (FT == null) {
                                // Get from the default
                                FT = PTypeProvider.Simple.getDefault().getType(FTR.getName());
                                if (FT == null) {
                                    throw new RPParsingException(
                                            "RegParser type named '" + FTR.getName() + "' is not found.");
                                }
                            }
                        }
                        
                        // Extract a type
                        if (FT != null) {
                            FP = FT.getChecker(pResult, Param, pProvider);
                            if (FP == null)
                                throw new RPParsingException("RegParser type named '" + FTR + "' has no checker.");
                        }
                        
                        // If type is a text, FP is not a node
                        if (FT.isText())
                            IsFPAsNode = false;
                        
                        if (FP instanceof RegParser) {
                            // The type contain a RegParser
                            ParseResult TryResult = new ParseResult.Node(pOffset, pResult);
                            TryResult = ((RegParser) FP).parse(pText, pOffset, 0, 0, TryResult, pProvider,
                                    ((FT != null) && FT.isSelfContain()) ? null : FT,
                                    ((FT != null) && FT.isSelfContain()) ? null : Param, pTabs + 1);
                            if (TryResult == null)
                                return null;
                            
                            EndPosition = TryResult.getEndPosition();
                            ThisResult  = TryResult;
                            
                        } else
                            if (FP instanceof CheckerAlternative) {
                                // The type contain a alternative checker
                                int         MaxEnd    = Integer.MIN_VALUE;
                                ParseResult MaxResult = null;
                                
                                CheckerAlternative CA = (CheckerAlternative) FP;
                                for (int c = CA.Checkers.length; --c >= 0;) {
                                    ParseResult TryResult = IsFPAsNode ? new ParseResult.Node(pOffset, pResult)
                                            : new ParseResult.Temp(pResult);
                                    if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.Checkers[c],
                                            TryResult, pProvider, pTabs + 1) == null)
                                        continue;
                                    
                                    // Find the longest length
                                    if (MaxEnd >= TryResult.getEndPosition())
                                        continue;
                                    MaxResult = TryResult;
                                    MaxEnd    = TryResult.getEndPosition();
                                }
                                if (MaxResult == null) {
                                    if (!CA.hasDefault())
                                        return null;
                                    
                                    ParseResult TryResult = IsFPAsNode ? new ParseResult.Node(pOffset, pResult)
                                            : new ParseResult.Temp(pResult);
                                    if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.getDefault(),
                                            TryResult, pProvider, pTabs + 1) == null)
                                        return null;
                                    
                                    MaxResult = TryResult;
                                    MaxEnd    = TryResult.getEndPosition();
                                }
                                EndPosition = MaxEnd;
                                ThisResult  = MaxResult;
                            } else
                                if (FP instanceof CheckerFixeds) {
                                    CheckerFixeds CG = ((CheckerFixeds) FP);
                                    // Check if there enough space for it
                                    if (pText.length() >= (pOffset + CG.getNeededLength())) {
                                        EndPosition = pOffset;
                                        ThisResult  = new ParseResult.Node(pOffset, pResult);
                                        for (int i = 0; i < CG.getEntryCount(); i++) {
                                            int l = CG.getEntry(i).getLength();
                                            if (l != -1)
                                                EndPosition += l;
                                            else
                                                EndPosition = pText.length();
                                            ThisResult.append(
                                                    ParseResult.newEntry(EndPosition, CG.getEntry(i).getRPEntry()));
                                        }
                                    }
                                } else {
                                    // The type contain a checker
                                    int REC      = pResult.getEntryListSize();
                                    int LengthFP = FP.getStartLengthOf(pText, pOffset, pProvider, pResult);
                                    if (LengthFP == -1) {
                                        // Recover what may have been added in the fail attempt
                                        pResult.reset(REC);
                                        return null;
                                    }
                                    
                                    EndPosition = pOffset + LengthFP;
                                    ThisResult  = new ParseResult.Node(pOffset, pResult);
                                    ThisResult.append(ParseResult.newEntry(pOffset + LengthFP, this.Entries[pIndex]));
                                    
                                }
                            
                        if ((FT != null) && FT.hasValidation() && FT.isSelfContain()) {
                            ParseResult PR = ThisResult.getDuplicate();
                            PR.collapse(pProvider);
                            // In case of type, do validation
                            if (!FT.validate(pResult, PR, Param, pProvider))
                                return null;
                        }
                        
                        // Append the result
                        if (IsFPAsNode) {
                            pResult.append(ParseResult.newEntry(EndPosition, this.Entries[pIndex], ThisResult));
                        } else {
                            if (IsFPType || IsFPName)
                                pResult.append(ParseResult.newEntry(EndPosition, this.Entries[pIndex]));
                            else
                                pResult.append(ParseResult.newEntry(EndPosition));
                        }
                        
                        // Return the result
                        return pResult;
                    } else
                        if (IsFPGroup) {
                            CheckerFixeds CG = ((CheckerFixeds) FP);
                            // There is not enough space for it
                            if (pText.length() < (pOffset + CG.getNeededLength()))
                                return null;
                            
                            int EndPosition = pOffset;
                            for (int i = 0; i < CG.getEntryCount(); i++) {
                                int l = CG.getEntry(i).getLength();
                                if (l != -1)
                                    EndPosition += l;
                                else
                                    EndPosition = pText.length();
                                pResult.append(ParseResult.newEntry(EndPosition, CG.getEntry(i).getRPEntry()));
                            }
                            
                            // Return the result
                            return pResult;
                        }
                    
        return null;
    }
    
    /** Cache for Tabs */
    static Vector<String> Tabs = new Vector<String>();
    
    /** Returns the length of the match if the text is start with a match or -1 if not */
    protected ParseResult parse(CharSequence pText, int pOffset, int pIndex, int pTimes, ParseResult pResult,
            PTypeProvider pProvider, PType pRPType, String pRPTParam, int pTabs) {
        
        // If the entry has a name, ask it to parse with a new parse result 
        if (pResult == null)
            pResult = new ParseResult.Root(pOffset, pText);
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
            
            pOffset = pResult.getEndPosition();
            if (pOffset < 0)
                return null;
            
            Quantifier FPQ = this.Entries[pIndex].getQuantifier();
            if (FPQ == null)
                FPQ = Quantifier.One;
            
            // Premature ending by hitting the end of the text
            if (pOffset >= TextLength) {
                boolean ToTry = false;
                // If the current multiple matchings end prematurely
                if (pTimes < FPQ.LBound) {
                    RPEntry RPE = this.Entries[pIndex];
                    Checker C   = RPE.getChecker();
                    // If this is a normal checker, return null
                    if (!((C instanceof RegParser) || (C instanceof CheckerAlternative)
                            || ((C == null) && ((RPE.getType() != null) || (RPE.getTypeRef() != null)))))
                        return null;
                    
                    ToTry = true;
                }
                
                if (!ToTry) {
                    // Check the rest of the Entry
                    TryLoop: for (int i = (pIndex + 1); i < EntryLength; i++) {
                        RPEntry RPE = this.Entries[i];
                        
                        Checker    C = RPE.getChecker();
                        Quantifier Q = RPE.getQuantifier();
                        // If the rest of the entry is not optional 
                        if ((Q == null) || (Q.LBound != 0)) {
                            // Inside a RegParser or Alternative with Q.LowerBound == 1, there may be a checker with {0}
                            // Inside.
                            if (((Q == null) || (Q.LBound == 1)) && ((C instanceof RegParser)
                                    || (C instanceof CheckerAlternative)
                                    || ((C == null) && ((RPE.getType() != null) || (RPE.getTypeRef() != null))))) {
                                ToTry = true;
                                
                                // Try the entry i
                                pIndex = i;
                                pTimes = 0;
                                
                                FPQ = this.Entries[pIndex].getQuantifier();
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
                    int REC = pResult.getEntryListSize();
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
                        int REC = pResult.getEntryListSize();
                        if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
                            // Recover what may have been added in the fail attempt
                            pResult.reset(REC);
                            return null;
                        }
                        // Append an empty entry when found zero (if named or typed)
                        if ((this.Entries[pIndex].getName() != null) || (this.Entries[pIndex].getType() != null)
                                || (this.Entries[pIndex].getTypeRef() != null))
                            pResult.append(new ParseResult.Entry_WithRPEntry(pOffset, this.Entries[pIndex]));
                        
                        // To the next entry, so change the entry index and restart the repeat
                        pIndex++;
                        pTimes = 0;
                        continue;
                        
                    } else {
                        
                        // Is it any
                        RPEntry RPE = this.Entries[pIndex];
                        if (RPE.getChecker() == PredefinedCharClasses.Any) {
                            if ((RPE.getName() == null) && (RPE.getTypeRef() == null) && (RPE.getType() == null)) {
                                // Is this limited - Match till the limit
                                int LB = RPE.getQuantifier().getLowerBound();
                                if (pOffset + LB <= TextLength) {    // There is enough space for the minimum (the lower bound)
                                    int UB = RPE.getQuantifier().getUpperBound();
                                    if (UB != -1) {    // With limit
                                        if (pOffset + UB <= TextLength) { // Can it contain the maximum
                                            // Take the minimum
                                            pResult.append(new ParseResult.Entry(pOffset + UB));
                                        } else {    // Take what it can
                                            pResult.append(new ParseResult.Entry(TextLength));
                                        }
                                    } else {    // Is no limit - Match till the end
                                        pResult.append(new ParseResult.Entry(TextLength));
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
                        
                        int REC = pResult.getEntryListSize();
                        
                        // Check if it reaches the maximum
                        if ((FPQ.UBound == -1) || (pTimes < FPQ.UBound)) {    // Not yet
                            int FREC = pResult.getEntryListSize();
                            // Try the first part
                            if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {    // Match
                                // Only the one that advances the parsing
                                if ((FREC != pResult.getEntryListSize()) && (pOffset != pResult.getEndPosition())) {
                                    pTimes++;
                                    continue;
                                }
                            }
                            // Recover what may have been added in the fail attempt
                            pResult.reset(FREC);
                            
                        }
                        // Check if it fail to reach the minimum, return as not found
                        if (pTimes < FPQ.LBound) {
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
                    if ((FPQ.UBound == -1) || (pTimes < FPQ.UBound)) {    // Not yet
                        
                        PType    FT  = this.Entries[pIndex].getType();
                        PTypeRef FTR = this.Entries[pIndex].getTypeRef();
                        Checker  FP  = this.Entries[pIndex].getChecker();
                        
                        boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
                                && (FP instanceof CheckerAlternative);
                        
                        if (IsFPAlternative) {
                            
                            int              MaxLength = Integer.MIN_VALUE;
                            ParseResult.Temp MaxResult = null;
                            
                            // Not yet
                            CheckerAlternative CA = (CheckerAlternative) FP;
                            for (int c = CA.Checkers.length; --c >= 0;) {
                                Checker C = CA.Checkers[c];
                                // Try the first part
                                ParseResult.Temp TryResult = new ParseResult.Temp(pResult);
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, C, TryResult, pProvider,
                                        pTabs) != null) {
                                    // Match
                                    // Try the later part, if not match, continue other alternatives
                                    if (this.parse(pText, TryResult.getEndPosition(), pIndex, pTimes + 1, TryResult,
                                            pProvider, pRPType, pRPTParam, pTabs) == null)
                                        continue;
                                    
                                    // Match, so record as max
                                    // Find the longer length
                                    if (MaxLength >= TryResult.getEndPosition())
                                        continue;
                                    MaxResult = TryResult;
                                    MaxLength = TryResult.getEndPosition();
                                    
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
                                int REC = pResult.getEntryListSize();
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.getDefault(), pResult,
                                        pProvider, pTabs) != null) {
                                    // Found the match.
                                    break MainLoop;
                                }
                                // Recover what may have been added in the fail attempt
                                pResult.reset(REC);
                            }
                            
                        } else {
                            int REC = pResult.getEntryListSize();
                            // Try the first part
                            if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
                                // Try the first part again. If match, return 
                                if (this.parse(pText, pResult.getEndPosition(), pIndex, pTimes + 1, pResult, pProvider,
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
                    if (pTimes < FPQ.LBound)
                        return null;
                    
                    // To the next entry, so change the entry index and restart the repeat
                    pIndex++;
                    pTimes = 0;
                    continue;
                    
                } else
                    if (FPQ.isMinimum()) {
                        
                        // Check if it has reach the minimum
                        if (pTimes >= FPQ.LBound) {
                            // Try the last part
                            int REC = pResult.getEntryListSize();
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
                        if ((FPQ.UBound != -1) && (pTimes >= FPQ.UBound))
                            return null; // Yes
                            
                        PType    FT  = this.Entries[pIndex].getType();
                        PTypeRef FTR = this.Entries[pIndex].getTypeRef();
                        Checker  FP  = this.Entries[pIndex].getChecker();
                        
                        boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
                                && (FP instanceof CheckerAlternative);
                        
                        if (IsFPAlternative) {
                            
                            int              MinLength = Integer.MAX_VALUE;
                            ParseResult.Temp MinResult = null;
                            
                            // Not yet
                            CheckerAlternative CA = (CheckerAlternative) FP;
                            for (int c = CA.Checkers.length; --c >= 0;) {
                                // Try the first part
                                ParseResult.Temp TryResult = new ParseResult.Temp(pResult);
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.Checkers[c],
                                        TryResult, pProvider, pTabs) != null) {
                                    // Match
                                    // Try the later part, if not match, continue other alternatives
                                    if (this.parse(pText, TryResult.getEndPosition(), pIndex, pTimes + 1, TryResult,
                                            pProvider, pRPType, pRPTParam, pTabs) == null)
                                        continue;
                                    
                                    // Match, so record as max
                                    // Find the longer length
                                    if (MinLength <= TryResult.getEndPosition())
                                        continue;
                                    MinResult = TryResult;
                                    MinLength = TryResult.getEndPosition();
                                    
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
                                int REC = pResult.getEntryListSize();
                                if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.getDefault(), pResult,
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
                            int REC = pResult.getEntryListSize();
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
            ParseResult PR = pResult.getDuplicate();
            PR.collapse(pProvider);
            ParseResult Host = (PR instanceof ParseResult.Node) ? ((ParseResult.Node) PR).Parent : null;
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
    public Checker getOptimized() {
        if ((this.Entries.length == 1) && (this.Entries[0].getName() == null) && (this.Entries[0].getTypeRef() == null)
                && ((this.Entries[0].getQuantifier() == null) || this.Entries[0].getQuantifier().isOne_Possessive())) {
            return this.Entries[0].getChecker();
        }
        
        RPEntry[] RPEs      = new RPEntry[this.Entries.length];
        boolean   IsChanged = false;
        
        for (int i = 0; i < this.Entries.length; i++) {
            RPEntry RPE = this.Entries[i];
            if ((RPE.getTypeRef() == null) && (RPE.getChecker() instanceof RegParser)) {
                Checker New = ((RegParser) RPE.getChecker()).getOptimized();
                if (New != RPE.getChecker()) {
                    RPEs[i]   = RPEntry._new(RPE.getName(), New, RPE.getQuantifier());
                    IsChanged = true;
                } else {
                    RPEs[i] = RPEntry._new(RPE.getName(), RPE.getChecker(), RPE.getQuantifier());
                }
            } else
                RPEs[i] = RPEntry._new(RPE.getName(), RPE.getChecker(), RPE.getQuantifier());
        }
        
        if (IsChanged)
            return RegParser.newRegParser((Object[]) RPEs);
        return this;
    }
    
    // To Satisfy Checker ----------------------------------------------------------------------------------------------
    
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
        return this.getStartLengthOf(S, pOffset, pProvider, null);
    }
    
    /**
     * Returns the length of the match if the string S starts with this checker.<br />
     * @param    S is the string to be parse
     * @param    pOffset the starting point of the checking
     * @param   pResult the parse result of the current parsing. This is only available when this checker is called from a RegParser
     * @return    the length of the match or -1 if the string S does not start with this checker
     */
    @Override
    public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
        if (pResult == null) {
            ParseResult PR = this.parse(S.toString(), pOffset, pProvider);
            if (PR == null)
                return -1;
            return PR.getEndPosition() - pOffset;
        }
        
        int REC = pResult.getEntryListSize();
        if ((this.parse(S, pOffset, 0, 0, pResult, pProvider, null, null, 0)) == null) {
            // Recover what may have been added in the fail attempt
            pResult.reset(REC);
            return -1;
        }
        return pResult.getEndPosition() - pOffset;
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
