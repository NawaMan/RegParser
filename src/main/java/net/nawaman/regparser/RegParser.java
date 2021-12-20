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

import static java.util.Objects.requireNonNullElse;
import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;
import static net.nawaman.regparser.utils.Util.prependArray;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Stream;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFixeds;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;
import net.nawaman.regparser.utils.Util;

/**
 * The regular parser
 * 
 * @author Nawapunth Manusitthipol (https://github.com/NawaMan)
 */
public class RegParser implements Checker, Serializable {
	
	private static final long serialVersionUID = 5789453645656854655L;
	
	/** Returns the empty array of RegParsers */
	public static final RegParser[] EmptyRegParserArray = new RegParser[0];
	public static boolean           DebugMode           = false;
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
	
	private boolean isOptimized = false;
	
	RegParser(RegParserEntry[] entries) {
		this.entries = requireNonNullElse(entries, RegParserEntry.EmptyRegParserEntryArray);
		this.isOptimized = false;
	}
	
	RegParser(boolean isOptimized, RegParserEntry[] entries) {
		this.entries     = requireNonNullElse(entries, RegParserEntry.EmptyRegParserEntryArray);
		this.isOptimized = true;;
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
	
	// Internal --------------------------------------------------------------------------------------------------------
	
	/** Parse an entry at the index pIndex possessively */
	protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, ParseResult pResult,
	        ParserTypeProvider pProvider, int pTabs) {
		String        FN  = this.entries[pIndex].name();
		ParserTypeRef FTR = this.entries[pIndex].typeRef();
		ParserType    FT  = this.entries[pIndex].type();
		Checker       FP  = this.entries[pIndex].checker();
		
		return this.parseEach_P(pText, pOffset, pIndex, FN, FT, FTR, FP, pResult, pProvider, pTabs);
	}
	
	/** Parse an entry possessively */
	protected ParseResult parseEach_P(CharSequence pText, int pOffset, int pIndex, String FN, ParserType FT,
	        ParserTypeRef FTR, Checker FP, ParseResult pResult, ParserTypeProvider pProvider, int pTabs) {
		try {
			
			boolean IsFPType          = (FT != null) || (FTR != null);
			boolean IsFPName          = (FN != null);
			boolean IsFPAsNode        = ((FN == null) && IsFPType) || ((FN != null) && !FN.startsWith("$"));
			boolean IsFPRegParser     = (!IsFPType && (FP instanceof RegParser));
			boolean IsFPAlternative   = (!IsFPType && !IsFPRegParser && (FP instanceof CheckerAlternative));
			boolean IsFPGroup         = (!IsFPType && !IsFPRegParser && !IsFPAlternative
			        && (FP instanceof CheckerFixeds));
			boolean IsFPNormalChecker = !IsFPType && !IsFPRegParser && !IsFPAlternative && !IsFPAlternative
			        && !IsFPGroup;
			
			// NOTE: If FPType or named, parse it then validate and record separately from
			// the current result
			// NOTE: If FPRegParser, parse it and record within the current result
			// NOTE: If FPNormalChecker, getLengthOfStartOf then recored it with in the
			// current result
			// NOTE: If Alternative, find the longest match
			
			if (IsFPAlternative) {
				int         MaxEnd    = Integer.MIN_VALUE;
				ParseResult MaxResult = null;
				
				CheckerAlternative CA       = (CheckerAlternative) FP;
				var                checkers = CA.checkers().toArray(Checker[]::new);
				for (int c = checkers.length; --c >= 0;) {
					var TryResult = IsFPAsNode ? newResult(pOffset, pResult) : newResult(pResult);
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
					
					var TryResult = IsFPAsNode ? newResult(pOffset, pResult) : newResult(pResult);
					if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), TryResult,
					        pProvider, pTabs) == null)
						return null;
					
					MaxResult = TryResult;
					MaxEnd    = TryResult.endPosition();
				}
				if (IsFPAsNode) {
					pResult.append(newEntry(MaxResult.endPosition(), this.entries[pIndex], MaxResult));
				} else {
					if (IsFPType || IsFPName)
						pResult.append(newEntry(MaxResult.endPosition(), this.entries[pIndex]));
					else
						pResult.mergeWith((TemporaryParseResult) MaxResult);
				}
				return pResult;
				
			} else if (IsFPNormalChecker) {
				if (pOffset >= pText.length())
					return null; // pResult;
					
				int REC      = pResult.rawEntryCount();
				int LengthFP = FP.startLengthOf(pText, pOffset, pProvider, pResult);
				if (LengthFP == -1) {
					// Recover what may have been added in the fail attempt
					pResult.reset(REC);
					return null;
				}
				if (IsFPName)
					pResult.append(newEntry(pOffset + LengthFP, this.entries[pIndex]));
				else
					pResult.append(newEntry(pOffset + LengthFP));
				
				return pResult;
				
			} else if (IsFPRegParser) { // RegParser
				if (IsFPName) {
					ParseResult TryResult = newResult(pOffset, pResult);
					if ((((RegParser) FP).parse(pText, pOffset, 0, 0, TryResult, pProvider, null, null,
					        pTabs + 1)) == null)
						return null;
					
					// Merge the result
					if (IsFPAsNode) {
						pResult.append(newEntry(TryResult.endPosition(), this.entries[pIndex], TryResult));
					} else {
						if (IsFPType || IsFPName)
							pResult.append(newEntry(TryResult.endPosition(), this.entries[pIndex]));
						else
							pResult.append(newEntry(TryResult.endPosition()));
					}
					return pResult;
					
				} else {
					int REC = pResult.rawEntryCount();
					if ((((RegParser) FP).parse(pText, pOffset, 0, 0, pResult, pProvider, null, null, pTabs)) == null) {
						// Recover what may have been added in the fail attempt
						pResult.reset(REC);
						return null;
					}
					return pResult;
					
				}
				
			} else if (IsFPType) { // RegParser with a type or a type ref
				
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
							throw new ParsingException("RegParser type named '" + FTR.name() + "' is not found.");
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
					
				} else if (FP instanceof CheckerAlternative) {
					// The type contain a alternative checker
					int         MaxEnd    = Integer.MIN_VALUE;
					ParseResult MaxResult = null;
					
					CheckerAlternative CA       = (CheckerAlternative) FP;
					var                checkers = CA.checkers().toArray(Checker[]::new);
					for (int c = checkers.length; --c >= 0;) {
						var TryResult = IsFPAsNode ? newResult(pOffset, pResult) : newResult(pResult);
						if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, checkers[c], TryResult,
						        pProvider, pTabs + 1) == null)
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
						
						var TryResult = IsFPAsNode ? newResult(pOffset, pResult) : newResult(pResult);
						if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), TryResult,
						        pProvider, pTabs + 1) == null)
							return null;
						
						MaxResult = TryResult;
						MaxEnd    = TryResult.endPosition();
					}
					EndPosition = MaxEnd;
					ThisResult  = MaxResult;
				} else if (FP instanceof CheckerFixeds) {
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
					ThisResult.append(newEntry(pOffset + LengthFP, this.entries[pIndex]));
					
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
					pResult.append(newEntry(EndPosition, this.entries[pIndex], ThisResult));
				} else {
					if (IsFPType || IsFPName)
						pResult.append(newEntry(EndPosition, this.entries[pIndex]));
					else
						pResult.append(newEntry(EndPosition));
				}
				
				// Return the result
				return pResult;
			} else if (IsFPGroup) {
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
			System.out.println("pText: " + pText);
			System.out.println("pOffset: " + pOffset);
			System.out.println("pIndex: " + pIndex);
			System.out.println("FN: " + FN);
			System.out.println("FT: " + FT);
			System.out.println("FTR: " + FTR);
			System.out.println("FP: " + FP);
			System.out.println("pResult: " + pResult);
			System.out.println("pProvider: " + pProvider);
			System.out.println("pTabs: " + pTabs);
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
		// NOTE: If Possessive, Try to match first until not match or limit then match
		// the later (the later must match)
		// If alternative, each first match must be longest
		// NOTE: If Maximum, Try to match the first then the later and the last, pick
		// the longest match
		// NOTE: If Minimum, Check first match until reaching the lower bound, the try
		// the full length until match.
		
		int LastEntryIndex = this.entries.length - 1;
		int EntryLength    = this.entries.length;
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
			
			Quantifier FPQ = this.entries[pIndex].quantifier();
			if (FPQ == null)
				FPQ = Quantifier.One;
			
			// Premature ending by hitting the end of the text
			if (pOffset >= TextLength) {
				boolean ToTry = false;
				// If the current multiple matchings end prematurely
				if (pTimes < FPQ.lowerBound()) {
					RegParserEntry RPE = this.entries[pIndex];
					Checker        C   = RPE.checker();
					// If this is a normal checker, return null
					if (!((C instanceof RegParser) || (C instanceof CheckerAlternative)
					        || ((C == null) && ((RPE.type() != null) || (RPE.typeRef() != null)))))
						return null;
					
					ToTry = true;
				}
				
				if (!ToTry) {
					// Check the rest of the Entry
					TryLoop: for (int i = (pIndex + 1); i < EntryLength; i++) {
						RegParserEntry RPE = this.entries[i];
						
						Checker    C = RPE.checker();
						Quantifier Q = RPE.quantifier();
						// If the rest of the entry is not optional
						if ((Q == null) || (Q.lowerBound() != 0)) {
							// Inside a RegParser or Alternative with Q.LowerBound == 1, there may be a
							// checker with {0}
							// Inside.
							if (((Q == null) || (Q.lowerBound() == 1))
							        && ((C instanceof RegParser) || (C instanceof CheckerAlternative)
							                || ((C == null) && ((RPE.type() != null) || (RPE.typeRef() != null))))) {
								ToTry = true;
								
								// Try the entry i
								pIndex = i;
								pTimes = 0;
								
								FPQ = this.entries[pIndex].quantifier();
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
				        + ((pRPType != null) ? pRPType + "(" + pRPTParam + ") ~ " : "") + this.entries[pIndex]
				        + ((pTimes != 0) ? "(" + pTimes + ")" : ""));
			}
			/* */
			
			if (FPQ.isPossessive()) {
				
				if (FPQ.isOne_Possessive()) { // Match one
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
					
				} else if (FPQ.isZero()) { // Match Zero
					int REC = pResult.rawEntryCount();
					if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
						// Recover what may have been added in the fail attempt
						pResult.reset(REC);
						return null;
					}
					// Append an empty entry when found zero (if named or typed)
					if ((this.entries[pIndex].name() != null) || (this.entries[pIndex].type() != null)
					        || (this.entries[pIndex].typeRef() != null))
						pResult.append(ParseResultEntry.newEntry(pOffset, this.entries[pIndex]));
					
					// To the next entry, so change the entry index and restart the repeat
					pIndex++;
					pTimes = 0;
					continue;
					
				} else {
					
					// Is it any
					RegParserEntry RPE = this.entries[pIndex];
					if (RPE.checker() == PredefinedCharClasses.Any) {
						if ((RPE.name() == null) && (RPE.typeRef() == null) && (RPE.type() == null)) {
							// Is this limited - Match till the limit
							int LB = RPE.quantifier().lowerBound();
							if (pOffset + LB <= TextLength) { // There is enough space for the minimum (the lower bound)
								int UB = RPE.quantifier().upperBound();
								if (UB != -1) { // With limit
									if (pOffset + UB <= TextLength) { // Can it contain the maximum
										// Take the minimum
										pResult.append(newEntry(pOffset + UB));
									} else { // Take what it can
										pResult.append(newEntry(TextLength));
									}
								} else { // Is no limit - Match till the end
									pResult.append(newEntry(TextLength));
								}
								// To the next entry, so change the entry index and restart the repeat
								pIndex++;
								pTimes = 0;
								continue;
							} else { // Need more, return as not match
								// Recover what may have been added in the fail attempt
								return null;
							}
						}
					}
					
					int REC = pResult.rawEntryCount();
					
					// Check if it reaches the maximum
					if ((FPQ.hasNoUpperBound()) || (pTimes < FPQ.upperBound())) { // Not yet
						int FREC = pResult.rawEntryCount();
						// Try the first part
						if (this.parseEach_P(pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) { // Match
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
				
			} else if (FPQ.isMaximum()) {
				
				// Check if it reaches the maximum
				if ((FPQ.hasNoUpperBound()) || (pTimes < FPQ.upperBound())) { // Not yet
					
					ParserType    FT  = this.entries[pIndex].type();
					ParserTypeRef FTR = this.entries[pIndex].typeRef();
					Checker       FP  = this.entries[pIndex].checker();
					
					boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
					        && (FP instanceof CheckerAlternative);
					
					if (IsFPAlternative) {
						
						int                  MaxLength = Integer.MIN_VALUE;
						TemporaryParseResult MaxResult = null;
						
						// Not yet
						CheckerAlternative CA       = (CheckerAlternative) FP;
						var                checkers = CA.checkers().toArray(Checker[]::new);
						for (int c = checkers.length; --c >= 0;) {
							Checker C = checkers[c];
							// Try the first part
							TemporaryParseResult TryResult = newResult(pResult);
							if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, C, TryResult, pProvider,
							        pTabs) != null) {
								// Match
								// Try the later part, if not match, continue other alternatives
								if (this.parse(pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult, pProvider,
								        pRPType, pRPTParam, pTabs) == null)
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
				
			} else if (FPQ.isMinimum()) {
				
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
					
				ParserType    FT  = this.entries[pIndex].type();
				ParserTypeRef FTR = this.entries[pIndex].typeRef();
				Checker       FP  = this.entries[pIndex].checker();
				
				boolean IsFPAlternative = ((FT == null) && (FTR == null)) && !(FP instanceof RegParser)
				        && (FP instanceof CheckerAlternative);
				
				if (IsFPAlternative) {
					
					int                  MinLength = Integer.MAX_VALUE;
					TemporaryParseResult MinResult = null;
					
					// Not yet
					CheckerAlternative CA       = (CheckerAlternative) FP;
					var                checkers = CA.checkers().toArray(Checker[]::new);
					for (int c = checkers.length; --c >= 0;) {
						// Try the first part
						var TryResult = newResult(pResult);
						if (this.parseEach_P(pText, pOffset, pIndex, null, null, null, checkers[c], TryResult,
						        pProvider, pTabs) != null) {
							// Match
							// Try the later part, if not match, continue other alternatives
							if (this.parse(pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult, pProvider,
							        pRPType, pRPTParam, pTabs) == null)
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
		if (isOptimized)
			return this;
		
		if (entries.length == 1) {
			var entry = entries[0];
			if ((entry.name() == null)
			 && (entry.typeRef() == null)
			 && ((entry.quantifier() == null) || entry.quantifier().isOne_Possessive())) {
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
	
}
