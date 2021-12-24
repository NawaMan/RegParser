package net.nawaman.regparser;

import static java.lang.String.format;
import static net.nawaman.regparser.RegParser.DebugMode;
import static net.nawaman.regparser.RegParser.DebugPrintStream;
import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;
import static net.nawaman.regparser.utils.Util.escapeText;

import java.util.HashMap;
import java.util.Map;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFixeds;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

class RegParserSolver {

	/** Cache for Tabs */
	private static Map<Integer, String> tabs = new HashMap<Integer, String>();
	
	/** Parse an entry at the index index possessively */
	private static ParseResult parseEach(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                tabCount) {
		var entry   = entries[index];
		var name    = entry.name();
		var typeRef = entry.typeRef();
		var type    = entry.type();
		var checker = entry.checker();
		var result  = parseEach(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, tabCount);
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
			int                tabCount) {
		boolean isTyped       = (type != null) || (typeRef != null);
		boolean isRegParser   = (!isTyped && (checker instanceof RegParser));
		boolean isAlternative = (!isTyped && !isRegParser && (checker instanceof CheckerAlternative));
		
		if (isAlternative)
			return parseAlternative(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, tabCount);
		
		boolean isGroup   = (!isTyped && !isRegParser && !isAlternative && (checker instanceof CheckerFixeds));
		boolean isChecker =  !isTyped && !isRegParser && !isAlternative && !isAlternative && !isGroup;
		
		if (isChecker)
			return parserChecker(entries, text, offset, index, name, checker, parseResult, typeProvider);
			
		if (isRegParser)
			return parseRegParser(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, tabCount);
			
		if (isTyped)
			return parseType(entries, text, offset, index, name, type, typeRef, checker, parseResult, typeProvider, tabCount);
			
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
		System.err.println(" = indentation: " + tabCount);
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
			int                tabCount) {
		// find the longest match
		
		boolean isTyped  = (type != null) || (typeRef != null);
		boolean isNamed  = (name != null);
		boolean isAsNode = ((name == null) && isTyped) || ((name != null) && !name.startsWith("$"));
		
		var maxEndRef    = new int[] { Integer.MIN_VALUE };
		var maxResultRef = new ParseResult[] { null };
		
		final var alternatives = (CheckerAlternative)checker;
		alternatives
		.forEachInReverse(alternative -> {
			var tryResult  = isAsNode
			               ? newResult(offset, parseResult)
			               : newResult(parseResult);
			var eachResult = parseEach(entries, text, offset, index, null, null, null, alternative, tryResult, typeProvider, tabCount);
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
			var eachResult     = parseEach(entries, text, offset, index, null, null, null, defaultChecker, tryResult, typeProvider, tabCount);
			if (eachResult == null)
				return null;
			
			maxResult = tryResult;
		}
		
		if (isAsNode) {
			var parserEntry = entries[index];
			int endPosition = maxResult.endPosition();
			var resultEntry = newEntry(endPosition, parserEntry, maxResult);
			parseResult.append(resultEntry);
			
		} else if (isTyped || isNamed) {
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
			
		int entryCount  = parseResult.rawEntryCount();
		int foundLength = checker.startLengthOf(text, offset, typeProvider, parseResult);
		if (foundLength == -1) {
			// Recover what may have been added in the fail attempt
			parseResult.reset(entryCount);
			return null;
		}
		
		var resultEntry
				= isNamed
				? newEntry(offset + foundLength, entries[index])
				: newEntry(offset + foundLength);
		parseResult.append(resultEntry);
		
		return parseResult;
	}
	
	private static ParseResult parseRegParser(
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
			int indentation) {
		// parse it and record within the current result
		
		boolean isTyped  = (type != null) || (typeRef != null);
		boolean isNamed  = (name != null);
		boolean isAsNode = ((name == null) && isTyped) || ((name != null) && !name.startsWith("$"));
		
		if (isNamed) {
			var tryResult = newResult(offset, parseResult);
			var thisResult = ((RegParser) checker).parse(text, offset, 0, 0, tryResult, typeProvider, null, null, indentation + 1);
			if (thisResult == null)
				return null;
			
			// Merge the result
			int endPosition = tryResult.endPosition();
			var parserEntry = entries[index];
			var resultEntry = (ParseResultEntry)null;
			if (isAsNode) {
				resultEntry = newEntry(endPosition, parserEntry, tryResult);
				
			} else if (isTyped || isNamed) {
				resultEntry = newEntry(endPosition, parserEntry);
				
			} else {
				resultEntry = newEntry(endPosition);
			}
			
			parseResult.append(resultEntry);
			return parseResult;
			
		}
		
		int entryCount = parseResult.rawEntryCount();
		var thisResult = ((RegParser)checker).parse(text, offset, 0, 0, parseResult, typeProvider, null, null, indentation);
		if (thisResult == null) {
			// Recover what may have been added in the fail attempt
			parseResult.reset(entryCount);
			return null;
		}
		
		return parseResult;
	}
	
	private static ParseResult parseType(
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
			int                tabCount) {
		// RegParser with a type or a type ref
		// parse it then validate and record separately from the current result
		
		boolean isTyped  = (type != null) || (typeRef != null);
		boolean isNamed  = (name != null);
		boolean isAsNode = ((name == null) && isTyped) || ((name != null) && !name.startsWith("$"));
		
		var parameter = (String)null;
		
		if (typeRef != null) {
			parameter = typeRef.parameter();
			// Get type from the ref
			if (typeProvider != null) {
				// Get from the given provider
				type = typeProvider.type(typeRef.name());
			}
			if (type == null) {
				// Get from the default
				type = ParserTypeProvider.Simple.defaultProvider().type(typeRef.name());
				if (type == null)
					throw new ParsingException("RegParser type named '" + typeRef.name() + "' is not found.");
			}
		}
		
		// Extract a type
		if (type != null) {
			checker = type.checker(parseResult, parameter, typeProvider);
			if (checker == null)
				throw new ParsingException("RegParser type named '" + typeRef + "' has no checker.");
		}
		
		// If type is a text, FP is not a node
		if (type.isText()) {
			isAsNode = false;
		}
		
		var tryResult = parseCheckerInType(entries, text, offset, index, type, checker, parseResult, typeProvider, tabCount, isAsNode, parameter);
		
		if (tryResult == null)
			return null;
		
		var thisResult  = tryResult;
		
		if ((type != null) && type.hasValidation() && type.isSelfContain()) {
			var duplicatedResult = thisResult.duplicate();
			duplicatedResult.collapse(typeProvider);
			// In case of type, do validation
			if (!type.validate(parseResult, duplicatedResult, parameter, typeProvider))
				return null;
		}
		
		// Append the result
		int endPosition = tryResult.endPosition();
		var newEntry = isAsNode             ? newEntry(endPosition, entries[index], thisResult)
		             : (isTyped || isNamed) ? newEntry(endPosition, entries[index])
		             :                        newEntry(endPosition);
		parseResult.append(newEntry);
		return parseResult;
	}
	
	private static ParseResult parseCheckerInType(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			ParserType         type,
			Checker            checker,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                indentation,
			boolean            isAsNode,
			String             parameter) {
		if (checker instanceof RegParser) {
			// The type contain a RegParser
			var tryResult    = (ParseResult)newResult(offset, parseResult);
			var tryType      = ((type != null) && type.isSelfContain()) ? null : type;
			var tryParameter = ((type != null) && type.isSelfContain()) ? null : parameter;
			var eachResult   = ((RegParser) checker).parse(text, offset, 0, 0, tryResult, typeProvider, tryType, tryParameter, indentation + 1);
			
			if (eachResult == null)
				return null;
			
			return eachResult;
		}
		
		if (checker instanceof CheckerAlternative) {
			// The type contain a alternative checker
			var maxEndRef    = new int[] { Integer.MIN_VALUE };
			var maxResultRef = new ParseResult[] { null };
			
			var alternatives = (CheckerAlternative)checker;
			var asNode = isAsNode;
			alternatives
			.forEachInReverse(alternative -> {
				var tryResult = asNode
				              ? newResult(offset, parseResult)
				              : newResult(parseResult);
				var eachResult = parseEach(entries, text, offset, index, null, null, null, alternative, tryResult, typeProvider, indentation + 1);
				if (eachResult == null)
					return;
				
				// Find the longest length
				if (maxEndRef[0] >= tryResult.endPosition())
					return;
				
				maxResultRef[0] = tryResult;
				maxEndRef[0]    = tryResult.endPosition();
			});
			
			var maxResult = maxResultRef[0];
			if ((maxResult == null) && alternatives.hasDefault()) {
				var tryResult = isAsNode
				              ? newResult(offset, parseResult)
				              : newResult(parseResult);
				var defaultChecker = alternatives.defaultChecker();
				var eachResult     = parseEach(entries, text, offset, index, null, null, null, defaultChecker, tryResult, typeProvider, indentation + 1);
				if (eachResult != null) {
					maxResult = tryResult;
				}
			}
			
			return maxResult;
		}
		
		if (checker instanceof CheckerFixeds) {
			var fixedChecker = ((CheckerFixeds) checker);
			// Check if there enough space for it
			if (text.length() < (offset + fixedChecker.neededLength()))
				return null;
			
			int endPosition = offset;
			var thisResult  = newResult(offset, parseResult);
			for (int i = 0; i < fixedChecker.entryCount(); i++) {
				var entry  = fixedChecker.entry(i);
				int length = entry.length();
				endPosition = (length != -1) ? endPosition + length : text.length();
				thisResult.append(newEntry(endPosition, entry.entry()));
			}
			return thisResult;
		}
		
		// The type contain a checker
		int length = checker.startLengthOf(text, offset, typeProvider, parseResult);
		if (length == -1) {
			// Recover what may have been added in the fail attempt
			int entryCount = parseResult.rawEntryCount();
			parseResult.reset(entryCount);
			return null;
		}
		
		int endPosition = offset + length;
		var entry       = entries[index];
		var resultEntry = newEntry(endPosition, entry);
		
		var thisResult = newResult(offset, parseResult);
		thisResult.append(resultEntry);
		return thisResult;
	}
	
	private static ParseResult parseGroup(
			CharSequence text,
			int          offset,
			Checker      checker,
			ParseResult  parseResult) {
		var checkerFixeds = ((CheckerFixeds) checker);
		// There is not enough space for it
		if (text.length() < (offset + checkerFixeds.neededLength()))
			return null;
		
		int endPosition = offset;
		for (int i = 0; i < checkerFixeds.entryCount(); i++) {
			int length = checkerFixeds.entry(i).length();
			if (length != -1) {
				endPosition += length;
			} else {
				endPosition = text.length();
			}
			
			var parserEntry = checkerFixeds.entry(i).entry();
			var resultEntry = newEntry(endPosition, parserEntry);
			parseResult.append(resultEntry);
		}
		
		// Return the result
		return parseResult;
	}
	
	static enum PossessiveReturn {
		Unmatched,
		MatchedEntry,
		MatchedOneTime,
		MatchedCompletely;
	}
	
	/** Returns the length of the match if the text is start with a match or -1 if not */
	static ParseResult parse(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			ParserType         type,
			String             parameter,
			int                tabCount) {
		
		if (parseResult == null)
			parseResult = newResult(offset, text);
		
		if (text == null)
			return parseResult;
			
		// NOTE: If Zero, The first must not match and the later must match
		// NOTE: If Possessive, Try to match first until not match or limit then match
		// the later (the later must match)
		// If alternative, each first match must be longest
		// NOTE: If Maximum, Try to match the first then the later and the last, pick
		// the longest match
		// NOTE: If Minimum, Check first match until reaching the lower bound, the try
		// the full length until match.
		
		int entryCount     = entries.length;
		int lastEntryIndex = entryCount - 1;
		int textLength     = text.length();
		var indentation    = DebugMode ? indentation(tabCount) : "";
		
		MainLoop: while (true) {
			
			// Check if there is no more entry to check.
			if (index > lastEntryIndex)
				break MainLoop;
			
			// Advance or stop
			offset = parseResult.endPosition();
			if (offset < 0)
				return null;
			
			
			// Found the end of the text but there is more parse entry,
			//   this only work if the rest of the entry are optional.
			if (offset >= textLength) {
				var skipToIndex = findSkipToIndex(entries, index, times, entryCount);
				
				// There are entry that cannot be skipped.
				if (skipToIndex == null)
					return null;
				
				// There are more to parse but they all optional
				if (skipToIndex == -1)
					break MainLoop;
				
				index = skipToIndex;
				times = 0;
			}
			
			if (RegParser.DebugMode) {
				mainLoopDebug(entries, text, offset, index, times, parseResult, type, parameter, tabCount, textLength, indentation);
			}
			
			var quantifier = entries[index].quantifier();
			if (quantifier.isPossessive()) {
				var possessiveReturn = parsePossessive(entries, text, offset, index, times, parseResult, typeProvider, tabCount, textLength, quantifier);
				if (possessiveReturn == PossessiveReturn.Unmatched) {
					return null;
				} else if (possessiveReturn == PossessiveReturn.MatchedEntry) {
					index++;
					times = 0;
					continue;
				} else if (possessiveReturn == PossessiveReturn.MatchedOneTime) {
					times++;
					continue;
				} else if (possessiveReturn == PossessiveReturn.MatchedCompletely) {
					break MainLoop;
				}
			}
			
			if (quantifier.isMaximum()) {
				var possessiveReturn = parseMaximum(entries, text, offset, index, times, parseResult, typeProvider, type, parameter, tabCount, textLength, quantifier);
				if (possessiveReturn == PossessiveReturn.Unmatched) {
					return null;
				} else if (possessiveReturn == PossessiveReturn.MatchedEntry) {
					index++;
					times = 0;
					continue;
				} else if (possessiveReturn == PossessiveReturn.MatchedOneTime) {
					times++;
					continue;
				} else if (possessiveReturn == PossessiveReturn.MatchedCompletely) {
					break MainLoop;
				}
				
			}
			if (quantifier.isMinimum()) {
				
				// Check if it has reach the minimum
				if (times >= quantifier.lowerBound()) {
					// Try the last part
					int REC = parseResult.rawEntryCount();
					// Parse the last part. If match, return
					if (parse(entries, text, offset, index + 1, 0, parseResult, typeProvider, type, parameter,
					        tabCount) != null) {
						// Found the match.
						break MainLoop;
					}
					
					// Else continue, the next loop
					
					// Recover what may have been added in the fail attempt
					parseResult.reset(REC);
				}
				
				// Check it reach the maximum
				if ((quantifier.hasUpperBound()) && (times >= quantifier.upperBound()))
					return null; // Yes
					
				ParserType    FT  = entries[index].type();
				ParserTypeRef FTR = entries[index].typeRef();
				Checker       FP  = entries[index].checker();
				
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
						var TryResult = newResult(parseResult);
						if (parseEach(entries, text, offset, index, null, null, null, checkers[c], TryResult,
						        typeProvider, tabCount) != null) {
							// Match
							// Try the later part, if not match, continue other alternatives
							if (parse(entries, text, TryResult.endPosition(), index, times + 1, TryResult, typeProvider,
							        type, parameter, tabCount) == null)
								continue;
								
							// Match, so record as max
							// Find the longer length
							if (MinLength <= TryResult.endPosition())
								continue;
							MinResult = TryResult;
							MinLength = TryResult.endPosition();
							
							if ((MinLength + offset) >= offset)
								break;
						}
					}
					
					if (MinResult != null) {
						// Merge the best result if found.
						parseResult.mergeWith(MinResult);
						break MainLoop;
					}
					
					if (CA.hasDefault()) {
						int REC = parseResult.rawEntryCount();
						if (parseEach(entries, text, offset, index, null, null, null, CA.defaultChecker(), parseResult,
						        typeProvider, tabCount) != null) {
							// Found the match.
							break MainLoop;
						}
						// Recover what may have been added in the fail attempt
						parseResult.reset(REC);
						// Not found, return as not found
						return null;
					}
					return null;
					
				} else {
					int REC = parseResult.rawEntryCount();
					// Try the first part
					if (parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount) != null) {
						times++;
						continue;
						
					}
					// Recover what may have been added in the fail attempt
					parseResult.reset(REC);
					// Not found, return as not found
					return null;
				}
			}
			
			return null;
		}
		
		// Validate the value here
		if ((type != null) && type.hasValidation() && !type.isSelfContain()) {
			ParseResult PR = parseResult.duplicate();
			PR.collapse(typeProvider);
			ParseResult Host = (PR instanceof ParseResultNode) ? ((ParseResultNode) PR).parent() : null;
			if (!type.validate(Host, PR, parameter, typeProvider))
				return null;
		}
		
		// TOHIDE
		/* */
		if (RegParser.DebugMode) {
			if (RegParser.DebugPrintStream == null) {
				RegParser.DebugPrintStream = System.out;
			}
			RegParser.DebugPrintStream.println(indentation + parseResult.toString(tabCount, 0));
			RegParser.DebugPrintStream.println(
			        indentation + "----------------------------------------------------------------------------------");
		}
		/* */
		return parseResult;
	}

	private static PossessiveReturn parseMaximum(RegParserEntry[] entries, CharSequence text, int offset, int index,
	        int times, ParseResult parseResult, ParserTypeProvider typeProvider, ParserType type, String parameter,
	        int tabCount, int textLength, Quantifier quantifier) {
		var possessiveReturn = (PossessiveReturn)null;
		
		// Check if it reaches the maximum
		if ((quantifier.hasNoUpperBound()) || (times < quantifier.upperBound())) { // Not yet
			
			ParserType    FT  = entries[index].type();
			ParserTypeRef FTR = entries[index].typeRef();
			Checker       FP  = entries[index].checker();
			
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
					TemporaryParseResult TryResult = newResult(parseResult);
					if (parseEach(entries, text, offset, index, null, null, null, C, TryResult, typeProvider,
					        tabCount) != null) {
						// Match
						// Try the later part, if not match, continue other alternatives
						if (parse(entries, text, TryResult.endPosition(), index, times + 1, TryResult, typeProvider, type, parameter, tabCount) == null)
							continue;
							
						// Match, so record as max
						// Find the longer length
						if (MaxLength >= TryResult.endPosition())
							continue;
						MaxResult = TryResult;
						MaxLength = TryResult.endPosition();
						
						if ((MaxLength + offset) >= textLength)
							break;
					}
				}
				
				if (MaxResult != null) {
					// Merge the result if found.
					parseResult.mergeWith(MaxResult);
					possessiveReturn = PossessiveReturn.MatchedCompletely;
				}
				
				if (possessiveReturn == null) {
					if (CA.hasDefault()) {
						int REC = parseResult.rawEntryCount();
						if (parseEach(entries, text, offset, index, null, null, null, CA.defaultChecker(), parseResult,
						        typeProvider, tabCount) != null) {
							// Found the match.
							possessiveReturn = PossessiveReturn.MatchedCompletely;
						}
						if (possessiveReturn == null) {
							// Recover what may have been added in the fail attempt
							parseResult.reset(REC);
						}
					}
				}
				
			} else {
				int REC = parseResult.rawEntryCount();
				// Try the first part
				if (parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount) != null) {
					// Try the first part again. If match, return
					if (parse(entries, text, parseResult.endPosition(), index, times + 1, parseResult, typeProvider,
					        type, parameter, tabCount) != null) {
						// Found the match.
						possessiveReturn = PossessiveReturn.MatchedCompletely;
					}
				}
				if (possessiveReturn == null) {
					// Recover what may have been added in the fail attempt
					parseResult.reset(REC);
					// If not found any, try to parse the last part
				}
			}
		}
		
		if (possessiveReturn == null) {
			// Check if it fail to reach the minimum, return as not found
			if (times < quantifier.lowerBound())
				possessiveReturn = PossessiveReturn.Unmatched;
			else {
				possessiveReturn = PossessiveReturn.MatchedEntry;
			}
		}
		return possessiveReturn;
	}
	
	private static String indentation(int tabCount) {
		if (tabCount < tabs.size()) {
			return tabs.get(tabCount);
		}
		
		if (tabCount == 0) {
			tabs.put(tabCount, "");
		}
		
		var indentation = "  " + indentation(tabCount - 1);
		tabs.put(tabCount, indentation);
		return indentation;
	}
	
	private static void mainLoopDebug(
			RegParserEntry[] entries,
			CharSequence     text,
			int              offset,
			int              index,
			int              times,
			ParseResult      parseResult,
			ParserType       type,
			String           parameter,
			int              tabCount,
			int              textLength,
			String           indentation) {
		if (DebugPrintStream == null) {
			DebugPrintStream = System.out;
		}
		DebugPrintStream.println(indentation + parseResult.toString(tabCount, 0));
		DebugPrintStream.println(indentation + "----------------------------------------------------------------------------------");
		
		int endPosition = ((textLength >= 50) || ((offset + 50) >= textLength) ? textLength : (offset + 50));
		var suffix      =  (textLength >= 50)                                  ? "..."      : "";
		var displayText = text.subSequence(offset, endPosition).toString() + suffix;
		var escapeText  = escapeText(displayText);
		var entry       = entries[index];
		var typeString  = (type  == null) ? "" : type + "(" + parameter + ")" + " ~ ";
		var timesString = (times ==    0) ? "" :        "(" + times     + ")";
		var logMessage  = format("%s`%s` ~ %s%s%s", indentation, escapeText, typeString + entry + timesString);
		DebugPrintStream.println(logMessage);
	}
	
	private static Integer findSkipToIndex(RegParserEntry[] entries, int index, int times, int entryCount) {
		// If the current multiple matchings end prematurely
		var quantifier = entries[index].quantifier();
		if (times < quantifier.lowerBound()) {
			var entry   = entries[index];
			var checker = entry.checker();
			// If this is a normal checker, return null
			boolean isOptionalChecker = isOptionalChecker(entry, checker);
			return isOptionalChecker ? index : null;
		}
		
		// Check the rest of the Entry
		for (int i = (index + 1); i < entryCount; i++) {
			var tryEntry      = entries[i];
			var tryChecker    = tryEntry.checker();
			var tryQuantifier = tryEntry.quantifier();
			// If the rest of the entry is not optional
			if (tryQuantifier.lowerBound() == 0)
				continue;
			
			// Inside a RegParser or Alternative with Q.LowerBound == 1,
			//   there may be a checker with {0} inside.
			boolean isOne = (tryQuantifier.lowerBound() == 1);
			boolean isOptionalChecker = isOptionalChecker(tryEntry, tryChecker);
			return (isOne && isOptionalChecker) ? i : null;
		}
		return -1;
	}
	
	private static boolean isOptionalChecker(RegParserEntry tryEntry, Checker tryChecker) {
		return (tryChecker instanceof RegParser)
		    || (tryChecker instanceof CheckerAlternative)
		    || ((tryChecker == null) && ((tryEntry.type() != null) || (tryEntry.typeRef() != null)));
	}
	
	private static PossessiveReturn parsePossessive(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			int                tabCount,
			int                textLength,
			Quantifier         quantifier) {
		if (quantifier.isOne_Possessive()) { // Match one
			int currentEntry = parseResult.rawEntryCount();
			var eachResult   = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
			if (eachResult == null) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(currentEntry);
				return PossessiveReturn.Unmatched;
			}
			
			// To the next entry, so restart the repeat
			return PossessiveReturn.MatchedEntry;
		}
		
		if (quantifier.isZero()) { // Match Zero
			int currentEntry = parseResult.rawEntryCount();
			var eachResult   = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
			if (eachResult != null) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(currentEntry);
				return PossessiveReturn.Unmatched;
			}
			
			// Append an empty entry when found zero (if named or typed)
			var entry = entries[index];
			if ((entry.name() != null) || (entry.type() != null) || (entry.typeRef() != null)) {
				parseResult.append(ParseResultEntry.newEntry(offset, entries[index]));
			}
			
			// To the next entry, so change the entry index and restart the repeat
			return PossessiveReturn.MatchedEntry;
			
		}
		
		// Is it any
		var entry = entries[index];
		if (entry.checker() == PredefinedCharClasses.Any) {
			if ((entry.name() == null) && (entry.typeRef() == null) && (entry.type() == null)) {
				// Is this limited - Match till the limit
				int LB = entry.quantifier().lowerBound();
				if (offset + LB <= textLength) { // There is enough space for the minimum (the lower bound)
					int UB = entry.quantifier().upperBound();
					if (UB != -1) { // With limit
						if (offset + UB <= textLength) { // Can it contain the maximum
							// Take the minimum
							parseResult.append(newEntry(offset + UB));
						} else { // Take what it can
							parseResult.append(newEntry(textLength));
						}
					} else { // Is no limit - Match till the end
						parseResult.append(newEntry(textLength));
					}
					
					// To the next entry, so change the entry index and restart the repeat
					return PossessiveReturn.MatchedEntry;
				}
				
				// Need more, return as not match
				return PossessiveReturn.Unmatched;
			}
		}
		
		int REC = parseResult.rawEntryCount();
		
		// Check if it reaches the maximum
		if ((quantifier.hasNoUpperBound()) || (times < quantifier.upperBound())) { // Not yet
			int FREC = parseResult.rawEntryCount();
			// Try the first part
			if (parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount) != null) { // Match
				// Only the one that advances the parsing
				if ((FREC != parseResult.rawEntryCount()) && (offset != parseResult.endPosition())) {
					return PossessiveReturn.MatchedOneTime;
				}
			}
			
			// Recover what may have been added in the fail attempt
			parseResult.reset(FREC);
		}
		
		// Check if it fail to reach the minimum, return as not found
		if (times < quantifier.lowerBound()) {
			// Recover what may have been added in the fail attempt
			parseResult.reset(REC);
			return PossessiveReturn.Unmatched;
		}
		
		// To the next entry, so change the entry index and restart the repeat
		return PossessiveReturn.MatchedEntry;
	}
	
}
