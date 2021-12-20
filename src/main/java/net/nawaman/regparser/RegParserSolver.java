package net.nawaman.regparser;

import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;

import java.util.ArrayList;
import java.util.List;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFixeds;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;
import net.nawaman.regparser.utils.Util;

class RegParserSolver {

	/** Cache for Tabs */
	private static List<String> tabs = new ArrayList<String>();
	
	/** Parse an entry at the index index possessively */
	private static ParseResult parseEach(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                indentation) {
		var entry   = entries[index];
		var name    = entry.name();
		var typeRef = entry.typeRef();
		var type    = entry.type();
		var checker = entry.checker();
		var result  = parseEach(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, indentation);
		return result;
	}
	
	/** Parse an entry possessively */
	private static ParseResult parseEach(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			String             name,
			ParserType         type,
			ParserTypeRef      typeRef,
			Checker            checker,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                indentation) {
		boolean isType        = (type != null) || (typeRef != null);
		boolean isRegParser   = (!isType && (checker instanceof RegParser));
		boolean isAlternative = (!isType && !isRegParser && (checker instanceof CheckerAlternative));
		
		if (isAlternative)
			return parseAlternative(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, indentation);
		
		boolean isGroup   = (!isType && !isRegParser && !isAlternative && (checker instanceof CheckerFixeds));
		boolean isChecker =  !isType && !isRegParser && !isAlternative && !isAlternative && !isGroup;
		
		if (isChecker)
			return parserChecker(entries, text, offset, index, name, checker, parseResult, typeProvider);
			
		if (isRegParser)
			return parseRegParser(entries, text, offset, index, name, type, typeRef, checker,
					parseResult, typeProvider, indentation);
			
		if (isType)
			return parseType(entries, text, offset, index, name, type, typeRef, checker, parseResult,
			        typeProvider, indentation);
			
		if (isGroup)
			return parseGroup(text, offset, checker, parseResult);
		
		System.err.println("Parser is in an unexpected state: ");
		System.err.println(" = entries: " + entries);
		System.err.println(" = text: " + text);
		System.err.println(" = offset: " + offset);
		System.err.println(" = index: " + index);
		System.err.println(" = name: " + name);
		System.err.println(" = type: " + type);
		System.err.println(" = typeRef: " + typeRef);
		System.err.println(" = checker: " + checker);
		System.err.println(" = parseResult: " + parseResult);
		System.err.println(" = typeProvider: " + typeProvider);
		System.err.println(" = indentation: " + indentation);
		return null;
	}
	
	private static ParseResult parseAlternative(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			String             name,
			ParserType         type,
			ParserTypeRef      typeRef,
			Checker            checker,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                indentation) {
		// find the longest match
		
		boolean isType   = (type != null) || (typeRef != null);
		boolean isNamed  = (name != null);
		boolean isAsNode = ((name == null) && isType) || ((name != null) && !name.startsWith("$"));
		
		var maxEndRef    = new int[] { Integer.MIN_VALUE };
		var maxResultRef = new ParseResult[] { null };
		
		final var alternatives = (CheckerAlternative)checker;
		alternatives
		.forEachInReverse(alternative -> {
			var tryResult  = isAsNode
			               ? newResult(offset, parseResult)
			               : newResult(parseResult);
			var eachResult = parseEach(entries, text, offset, index, null, null, null, alternative, tryResult, typeProvider, indentation);
			if (eachResult == null)
				return;
			
			// Find the longest length
			if (maxEndRef[0] >= tryResult.endPosition())
				return;
			
			maxResultRef[0] = tryResult;
			maxEndRef[0]    = tryResult.endPosition();
		});
		
		var maxResult = maxResultRef[0];
		if (maxResult == null) {
			if (!alternatives.hasDefault())
				return null;
			
			var tryResult      = isAsNode ? newResult(offset, parseResult) : newResult(parseResult);
			var defaultChecker = alternatives.defaultChecker();
			var eachResult     = parseEach(entries, text, offset, index, null, null, null, defaultChecker, tryResult, typeProvider, indentation);
			if (eachResult == null)
				return null;
			
			maxResult = tryResult;
		}
		
		if (isAsNode) {
			var parserEntry = entries[index];
			int endPosition = maxResult.endPosition();
			var resultEntry = newEntry(endPosition, parserEntry, maxResult);
			parseResult.append(resultEntry);
		} else if (isType || isNamed) {
			var parserEntry = entries[index];
			int endPosition = maxResult.endPosition();
			var resultEntry = newEntry(endPosition, parserEntry);
			parseResult.append(resultEntry);
		} else {
			parseResult.mergeWith((TemporaryParseResult) maxResult);
		}
		return parseResult;
	}
	
	private static ParseResult parserChecker(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			String             name,
			Checker            checker,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider) {
		// getLengthOfStartOf then recored it with in the current result
		
		boolean isNamed = (name != null);
		
		if (offset >= text.length())
			return null;
			
		int REC      = parseResult.rawEntryCount();
		int LengthFP = checker.startLengthOf(text, offset, typeProvider, parseResult);
		if (LengthFP == -1) {
			// Recover what may have been added in the fail attempt
			parseResult.reset(REC);
			return null;
		}
		if (isNamed)
			parseResult.append(newEntry(offset + LengthFP, entries[index]));
		else
			parseResult.append(newEntry(offset + LengthFP));
		
		return parseResult;
	}
	
	private static ParseResult parseRegParser(final RegParserEntry[] entries, final CharSequence text, final int offset,
	        final int index, String name, ParserType type, ParserTypeRef typeRef,
	        Checker checker, final ParseResult parseResult, final ParserTypeProvider typeProvider, final int indentation) {
		// parse it and record within the current result
		
		final boolean IsFPType          = (type != null) || (typeRef != null);
		final boolean IsFPName          = (name != null);
		final boolean IsFPAsNode        = ((name == null) && IsFPType) || ((name != null) && !name.startsWith("$"));
		
		if (IsFPName) {
			ParseResult TryResult = newResult(offset, parseResult);
			if ((((RegParser) checker).parse(text, offset, 0, 0, TryResult, typeProvider, null, null,
			        indentation + 1)) == null)
				return null;
			
			// Merge the result
			if (IsFPAsNode) {
				parseResult.append(newEntry(TryResult.endPosition(), entries[index], TryResult));
			} else {
				if (IsFPType || IsFPName)
					parseResult.append(newEntry(TryResult.endPosition(), entries[index]));
				else
					parseResult.append(newEntry(TryResult.endPosition()));
			}
			return parseResult;
			
		} else {
			int REC = parseResult.rawEntryCount();
			if ((((RegParser) checker).parse(text, offset, 0, 0, parseResult, typeProvider, null, null, indentation)) == null) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(REC);
				return null;
			}
			return parseResult;
			
		}
	}
	
	private static ParseResult parseType(final RegParserEntry[] entries, final CharSequence text, final int offset,
	        final int index, final String name, ParserType type, final ParserTypeRef typeRef,
	        Checker checker, final ParseResult parseResult, final ParserTypeProvider typeProvider,
	        final int indentation) {
		// RegParser with a type or a type ref
		// parse it then validate and record separately from the current result
		
		boolean IsFPType   = (type != null) || (typeRef != null);
		boolean IsFPName   = (name != null);
		boolean IsFPAsNode = ((name == null) && IsFPType) || ((name != null) && !name.startsWith("$"));
		
		int         EndPosition = -1;
		ParseResult ThisResult  = null;
		
		String Param = null;
		
		if (typeRef != null) {
			Param = typeRef.parameter();
			// Get type from the ref
			if (typeProvider != null) {
				// Get from the given provider
				type = typeProvider.type(typeRef.name());
			}
			if (type == null) {
				// Get from the default
				type = ParserTypeProvider.Simple.defaultProvider().type(typeRef.name());
				if (type == null) {
					throw new ParsingException("RegParser type named '" + typeRef.name() + "' is not found.");
				}
			}
		}
		
		// Extract a type
		if (type != null) {
			checker = type.checker(parseResult, Param, typeProvider);
			if (checker == null)
				throw new ParsingException("RegParser type named '" + typeRef + "' has no checker.");
		}
		
		// If type is a text, FP is not a node
		if (type.isText())
			IsFPAsNode = false;
		
		if (checker instanceof RegParser) {
			// The type contain a RegParser
			ParseResult TryResult = newResult(offset, parseResult);
			TryResult = ((RegParser) checker).parse(text, offset, 0, 0, TryResult, typeProvider,
			        ((type != null) && type.isSelfContain()) ? null : type,
			        ((type != null) && type.isSelfContain()) ? null : Param, indentation + 1);
			if (TryResult == null)
				return null;
			
			EndPosition = TryResult.endPosition();
			ThisResult  = TryResult;
			
		} else if (checker instanceof CheckerAlternative) {
			// The type contain a alternative checker
			int         MaxEnd    = Integer.MIN_VALUE;
			ParseResult MaxResult = null;
			
			CheckerAlternative CA       = (CheckerAlternative) checker;
			var                checkers = CA.checkers().toArray(Checker[]::new);
			for (int c = checkers.length; --c >= 0;) {
				var TryResult = IsFPAsNode ? newResult(offset, parseResult) : newResult(parseResult);
				if (parseEach(entries, text, offset, index, null, null, null, checkers[c], TryResult,
				        typeProvider, indentation + 1) == null)
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
				
				var TryResult = IsFPAsNode ? newResult(offset, parseResult) : newResult(parseResult);
				if (parseEach(entries, text, offset, index, null, null, null, CA.defaultChecker(), TryResult,
				        typeProvider, indentation + 1) == null)
					return null;
				
				MaxResult = TryResult;
				MaxEnd    = TryResult.endPosition();
			}
			EndPosition = MaxEnd;
			ThisResult  = MaxResult;
		} else if (checker instanceof CheckerFixeds) {
			CheckerFixeds CG = ((CheckerFixeds) checker);
			// Check if there enough space for it
			if (text.length() >= (offset + CG.neededLength())) {
				EndPosition = offset;
				ThisResult  = newResult(offset, parseResult);
				for (int i = 0; i < CG.entryCount(); i++) {
					int l = CG.entry(i).length();
					if (l != -1)
						EndPosition += l;
					else
						EndPosition = text.length();
					ThisResult.append(newEntry(EndPosition, CG.entry(i).entry()));
				}
			}
		} else {
			// The type contain a checker
			int REC      = parseResult.rawEntryCount();
			int LengthFP = checker.startLengthOf(text, offset, typeProvider, parseResult);
			if (LengthFP == -1) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(REC);
				return null;
			}
			
			EndPosition = offset + LengthFP;
			ThisResult  = newResult(offset, parseResult);
			ThisResult.append(newEntry(offset + LengthFP, entries[index]));
			
		}
		
		if ((type != null) && type.hasValidation() && type.isSelfContain()) {
			ParseResult PR = ThisResult.duplicate();
			PR.collapse(typeProvider);
			// In case of type, do validation
			if (!type.validate(parseResult, PR, Param, typeProvider))
				return null;
		}
		
		// Append the result
		if (IsFPAsNode) {
			parseResult.append(newEntry(EndPosition, entries[index], ThisResult));
		} else {
			if (IsFPType || IsFPName)
				parseResult.append(newEntry(EndPosition, entries[index]));
			else
				parseResult.append(newEntry(EndPosition));
		}
		
		// Return the result
		return parseResult;
	}
	
	private static ParseResult parseGroup(final CharSequence text, final int offset, Checker checker,
	        final ParseResult parseResult) {
		CheckerFixeds CG = ((CheckerFixeds) checker);
		// There is not enough space for it
		if (text.length() < (offset + CG.neededLength()))
			return null;
		
		int EndPosition = offset;
		for (int i = 0; i < CG.entryCount(); i++) {
			int l = CG.entry(i).length();
			if (l != -1)
				EndPosition += l;
			else
				EndPosition = text.length();
			parseResult.append(newEntry(EndPosition, CG.entry(i).entry()));
		}
		
		// Return the result
		return parseResult;
	}
	
	/** Returns the length of the match if the text is start with a match or -1 if not */
	static ParseResult parse(RegParserEntry[] entries, CharSequence pText, int pOffset, int pIndex, int pTimes, ParseResult pResult,
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
		
		int LastEntryIndex = entries.length - 1;
		int EntryLength    = entries.length;
		int TextLength     = pText.length();
		
		String StrTabs = "";
		if (RegParser.DebugMode) {
			while (pTabs >= tabs.size()) {
				if (tabs.size() == 0)
					tabs.add("");
				if (StrTabs == "")
					StrTabs = tabs.get(tabs.size() - 1);
				StrTabs += "  ";
				tabs.add(StrTabs);
			}
			
			if (pTabs <= tabs.size())
				StrTabs = tabs.get(pTabs);
			else {
				for (int i = pTabs; --i >= 0;)
					StrTabs += "  ";
				tabs.add(StrTabs);
			}
		}
		
		MainLoop: while (true) {
			
			if (pIndex > LastEntryIndex)
				break MainLoop;
			
			pOffset = pResult.endPosition();
			if (pOffset < 0)
				return null;
			
			Quantifier FPQ = entries[pIndex].quantifier();
			if (FPQ == null)
				FPQ = Quantifier.One;
			
			// Premature ending by hitting the end of the text
			if (pOffset >= TextLength) {
				boolean ToTry = false;
				// If the current multiple matchings end prematurely
				if (pTimes < FPQ.lowerBound()) {
					RegParserEntry RPE = entries[pIndex];
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
						RegParserEntry RPE = entries[i];
						
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
								
								FPQ = entries[pIndex].quantifier();
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
				if (RegParser.DebugPrintStream == null) {
					RegParser.DebugPrintStream = System.out;
				}
				RegParser.DebugPrintStream.println(StrTabs + pResult.toString(pTabs, 0));
				RegParser.DebugPrintStream.println(
				        StrTabs + "----------------------------------------------------------------------------------");
				String T = null;
				if (TextLength >= 50)
					T = pText.subSequence(pOffset, ((pOffset + 50) >= TextLength) ? TextLength : (pOffset + 50))
					        .toString() + "...";
				else
					T = pText.subSequence(pOffset, TextLength).toString();
				
				RegParser.DebugPrintStream.println(StrTabs + "`" + Util.escapeText(T) + "` ~ "
				        + ((pRPType != null) ? pRPType + "(" + pRPTParam + ") ~ " : "") + entries[pIndex]
				        + ((pTimes != 0) ? "(" + pTimes + ")" : ""));
			}
			/* */
			
			if (FPQ.isPossessive()) {
				
				if (FPQ.isOne_Possessive()) { // Match one
					int REC = pResult.rawEntryCount();
					if (parseEach(entries, pText, pOffset, pIndex, pResult, pProvider, pTabs) == null) {
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
					if (parseEach(entries, pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
						// Recover what may have been added in the fail attempt
						pResult.reset(REC);
						return null;
					}
					// Append an empty entry when found zero (if named or typed)
					if ((entries[pIndex].name() != null) || (entries[pIndex].type() != null)
					        || (entries[pIndex].typeRef() != null))
						pResult.append(ParseResultEntry.newEntry(pOffset, entries[pIndex]));
					
					// To the next entry, so change the entry index and restart the repeat
					pIndex++;
					pTimes = 0;
					continue;
					
				} else {
					
					// Is it any
					RegParserEntry RPE = entries[pIndex];
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
						if (parseEach(entries, pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) { // Match
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
					
					ParserType    FT  = entries[pIndex].type();
					ParserTypeRef FTR = entries[pIndex].typeRef();
					Checker       FP  = entries[pIndex].checker();
					
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
							if (parseEach(entries, pText, pOffset, pIndex, null, null, null, C, TryResult, pProvider,
							        pTabs) != null) {
								// Match
								// Try the later part, if not match, continue other alternatives
								if (parse(entries, pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult, pProvider,
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
							if (parseEach(entries, pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), pResult,
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
						if (parseEach(entries, pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
							// Try the first part again. If match, return
							if (parse(entries, pText, pResult.endPosition(), pIndex, pTimes + 1, pResult, pProvider,
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
					if (parse(entries, pText, pOffset, pIndex + 1, 0, pResult, pProvider, pRPType, pRPTParam,
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
					
				ParserType    FT  = entries[pIndex].type();
				ParserTypeRef FTR = entries[pIndex].typeRef();
				Checker       FP  = entries[pIndex].checker();
				
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
						if (parseEach(entries, pText, pOffset, pIndex, null, null, null, checkers[c], TryResult,
						        pProvider, pTabs) != null) {
							// Match
							// Try the later part, if not match, continue other alternatives
							if (parse(entries, pText, TryResult.endPosition(), pIndex, pTimes + 1, TryResult, pProvider,
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
						if (parseEach(entries, pText, pOffset, pIndex, null, null, null, CA.defaultChecker(), pResult,
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
					if (parseEach(entries, pText, pOffset, pIndex, pResult, pProvider, pTabs) != null) {
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
			if (RegParser.DebugPrintStream == null) {
				RegParser.DebugPrintStream = System.out;
			}
			RegParser.DebugPrintStream.println(StrTabs + pResult.toString(pTabs, 0));
			RegParser.DebugPrintStream.println(
			        StrTabs + "----------------------------------------------------------------------------------");
		}
		/* */
		return pResult;
	}
	
}
