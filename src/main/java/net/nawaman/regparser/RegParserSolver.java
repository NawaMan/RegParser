package net.nawaman.regparser;

import static java.lang.String.format;
import static net.nawaman.regparser.RegParser.DebugPrintStream;
import static net.nawaman.regparser.RegParser.isDebugMode;
import static net.nawaman.regparser.RegParserEachSolver.parseEach;
import static net.nawaman.regparser.RegParserSolver.ParseLoopResult.MatchedCompletely;
import static net.nawaman.regparser.RegParserSolver.ParseLoopResult.MatchedEntry;
import static net.nawaman.regparser.RegParserSolver.ParseLoopResult.MatchedOneTime;
import static net.nawaman.regparser.RegParserSolver.ParseLoopResult.Unmatched;
import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;
import static net.nawaman.regparser.utils.Util.escapeText;

import java.util.HashMap;
import java.util.Map;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

class RegParserSolver {
	
	static enum ParseLoopResult {
		Unmatched,
		MatchedEntry,
		MatchedOneTime,
		MatchedCompletely;
	}
	
	/** Starting the parse process. @return the result or null. */
	static ParseResult startParse(
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
		
		return parse(entries, text, offset, index, times, parseResult, typeProvider, type, parameter, tabCount);
	}
	
	/** Perform the parse. @return the result or null. */
	private static ParseResult parse(
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
		int entryCount     = entries.length;
		int lastEntryIndex = entryCount - 1;
		int textLength     = text.length();
		var indentation    = isDebugMode ? indentation(tabCount) : "";
		
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
			
			if (isDebugMode) {
				debugMainLoop(entries, text, offset, index, times, parseResult, type, parameter, tabCount, textLength, indentation);
			}
			
			var quantifier = entries[index].quantifier();
			var loopResult = (ParseLoopResult)null;
			if (quantifier.isPossessive()) {
				loopResult = parsePossessive(entries, text, offset, index, times, parseResult, typeProvider, tabCount, textLength, quantifier);
			} else if (quantifier.isMaximum()) {
				loopResult = parseMaximum(entries, text, offset, index, times, parseResult, typeProvider, type, parameter, tabCount, textLength, quantifier);
			} else if (quantifier.isMinimum()) {
				loopResult = parseMinimum(entries, text, offset, index, times, parseResult, typeProvider, type, parameter, tabCount, quantifier);
			}
			
			if (loopResult == ParseLoopResult.MatchedEntry) {
				index++;
				times = 0;
				continue;
			} else if (loopResult == ParseLoopResult.MatchedOneTime) {
				times++;
				continue;
			} else if (loopResult == ParseLoopResult.MatchedCompletely) {
				break MainLoop;
			}
			
			// ParseLoopResult.Unmatched or unrecognized result.
			return null;
		}
		
		// Validate the value here
		if ((type != null) && type.hasValidation() && !type.isSelfContain()) {
			var duplicateParseResult = parseResult.duplicate();
			duplicateParseResult.collapse(typeProvider);
			
			var host = (duplicateParseResult instanceof ParseResultNode)
			         ? ((ParseResultNode) duplicateParseResult).parent()
			         : null;
			var isValidated = type.validate(host, duplicateParseResult, parameter, typeProvider);
			if (!isValidated)
				return null;
		}
		
		if (isDebugMode) {
			debugParseFinish(parseResult, tabCount, indentation);
		}
		
		return parseResult;
	}
	
	/** Some cases allow use to short cut to some point in the index. */
	private static Integer findSkipToIndex(RegParserEntry[] entries, int index, int times, int entryCount) {
		var entry      = entries[index];
		var quantifier = entry.quantifier();
		// If the current multiple matchings end prematurely
		if (times < quantifier.lowerBound()) {
			return isSimpleChecker(entry) ? null : index;
		}
		
		// Check the rest of the Entry
		for (int i = (index + 1); i < entryCount; i++) {
			var tryEntry      = entries[i];
			var tryQuantifier = tryEntry.quantifier();
			// If the rest of the entry is not optional
			if (tryQuantifier.lowerBound() == 0)
				continue;
			
			boolean isOne           = (tryQuantifier.lowerBound() == 1);
			boolean isSimpleChecker = isSimpleChecker(tryEntry);
			return (isOne && !isSimpleChecker) ? i : null;
		}
		return -1;
	}
	
	private static boolean isSimpleChecker(RegParserEntry entry) {
		var checker = entry.checker();
		return !(checker instanceof RegParser)
		    && !(checker instanceof CheckerAlternative)
		    && !((checker == null) && ((entry.type() != null) || (entry.typeRef() != null)));
	}
	
	private static ParseLoopResult parsePossessive(
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
		// NOTE: If Possessive, Try to match first until not match or limit then match
		// the later (the later must match)
		// If alternative, each first match must be longest
		
		if (quantifier.isOne_Possessive()) { // Match one
			int currentEntryIndex = parseResult.rawEntryCount();
			var eachResult        = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
			if (eachResult == null) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(currentEntryIndex);
				return Unmatched;
			}
			
			// To the next entry, so restart the repeat
			return MatchedEntry;
		}
		
		if (quantifier.isZero()) { // Match Zero
			int currentEntryIndex = parseResult.rawEntryCount();
			var eachResult        = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
			if (eachResult != null) {
				// Recover what may have been added in the fail attempt
				parseResult.reset(currentEntryIndex);
				return Unmatched;
			}
			
			// Append an empty entry when found zero (if named or typed)
			var entry = entries[index];
			if ((entry.name()    != null)
			 || (entry.type()    != null)
			 || (entry.typeRef() != null)) {
				parseResult.append(ParseResultEntry.newEntry(offset, entries[index]));
			}
			
			// To the next entry, so change the entry index and restart the repeat
			return MatchedEntry;
			
		}
		
		// Is it any
		var entry = entries[index];
		if (entry.checker() == PredefinedCharClasses.Any) {
			if ((entry.name()    == null)
			 && (entry.type()    == null)
			 && (entry.typeRef() == null)) {
				// Is this limited - Match till the limit
				int lowerBound = entry.quantifier().lowerBound();
				if (offset + lowerBound <= textLength) { // There is enough space for the minimum (the lower bound)
					int     upperBound    = entry.quantifier().upperBound();
					boolean hasUpperBound = upperBound != -1;
					boolean enoughSpace   = hasUpperBound && (offset + upperBound <= textLength);
					int     entryLength   = enoughSpace ? (offset + upperBound) : textLength;
					var     resultEntry   = newEntry(entryLength);
					parseResult.append(resultEntry);
					
					// To the next entry, so change the entry index and restart the repeat
					return MatchedEntry;
				}
				
				// Need more, return as not match
				return Unmatched;
			}
		}
		
		int savedIndex = parseResult.rawEntryCount();
		
		// Check if it reaches the maximum
		if ((quantifier.hasNoUpperBound()) || (times < quantifier.upperBound())) { // Not yet
			int prevEntryIndex = parseResult.rawEntryCount();
			// Try the first part
			if (parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount) != null) { // Match
				// Only the one that advances the parsing
				int currEntryCount = parseResult.rawEntryCount();
				if ((prevEntryIndex != currEntryCount) && (offset != parseResult.endPosition())) {
					return MatchedOneTime;
				}
			}
			
			// Recover what may have been added in the fail attempt
			parseResult.reset(prevEntryIndex);
		}
		
		// Check if it fail to reach the minimum, return as not found
		if (times < quantifier.lowerBound()) {
			// Recover what may have been added in the fail attempt
			parseResult.reset(savedIndex);
			return Unmatched;
		}
		
		// To the next entry, so change the entry index and restart the repeat
		return MatchedEntry;
	}
	
	private static ParseLoopResult parseMaximum(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			ParserType         type,
			String             parameter,
			int                tabCount,
			int                textLength,
			Quantifier         quantifier) {
		// NOTE: If Maximum, Try to match the first then the later and the last, pick
		// the longest match
		
		// Check if it reaches the maximum
		if ((quantifier.hasNoUpperBound()) || (times < quantifier.upperBound())) { // Not yet
			
			var thieEntry   = entries[index];
			var thisType    = thieEntry.type();
			var thisTypeReg = thieEntry.typeRef();
			var thisChecker = thieEntry.checker();
			
			boolean isAlternative
					= ((thisType == null) && (thisTypeReg == null))
					&& !(thisChecker instanceof RegParser)
					&&  (thisChecker instanceof CheckerAlternative);
			
			if (isAlternative) {
				int maxLength = Integer.MIN_VALUE;
				var maxResult = (TemporaryParseResult)null;
				
				// Not yet
				var alternativeCheckers = (CheckerAlternative) thisChecker;
				var checkers            = alternativeCheckers.checkers().toArray(Checker[]::new);
				for (int c = checkers.length; --c >= 0;) {
					var checker = checkers[c];
					// Try the first part
					var tryResult = newResult(parseResult);
					var eachResult = parseEach(entries, text, offset, index, null, null, null, checker, tryResult, typeProvider, tabCount);
					if (eachResult != null) {
						// Match
						// Try the later part, if not match, continue other alternatives
						int tryEndPosition = tryResult.endPosition();
						var laterResult = parse(entries, text, tryEndPosition, index, times + 1, tryResult, typeProvider, type, parameter, tabCount);
						if (laterResult == null)
							continue;
							
						// Match, so record as max
						// Find the longer length
						if (maxLength >= tryEndPosition)
							continue;
						
						maxResult = tryResult;
						maxLength = tryEndPosition;
						
						if ((maxLength + offset) >= textLength)
							break;
					}
				}
				
				if (maxResult != null) {
					// Merge the result if found.
					parseResult.mergeWith(maxResult);
					return MatchedCompletely;
				}
				
				if (alternativeCheckers.hasDefault()) {
					int savedIndex = parseResult.rawEntryCount();
					var defaultChecker = alternativeCheckers.defaultChecker();
					var defaultResult = parseEach(entries, text, offset, index, null, null, null, defaultChecker, parseResult, typeProvider, tabCount);
					if (defaultResult != null) {
						// Found the match.
						return MatchedCompletely;
					}
					
					// Recover what may have been added in the fail attempt
					parseResult.reset(savedIndex);
				}
				
			} else {
				int savedIndex = parseResult.rawEntryCount();
				// Try the first part
				var firstResult = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
				if (firstResult != null) {
					// Try the first part again. If match, return
					var secondResult = parse(entries, text, parseResult.endPosition(), index, times + 1, parseResult, typeProvider, type, parameter, tabCount);
					if (secondResult != null) {
						// Found the match.
						return MatchedCompletely;
					}
				}
				// Recover what may have been added in the fail attempt
				parseResult.reset(savedIndex);
				// If not found any, try to parse the last part
			}
		}
		
		// Check if it fail to reach the minimum, return as not found
		boolean reachMinimum = (times >= quantifier.lowerBound());
		return reachMinimum ? MatchedEntry : Unmatched;
	}
	
	private static ParseLoopResult parseMinimum(
			RegParserEntry[]   entries,
			CharSequence       text,
			int                offset,
			int                index,
			int                times,
			ParseResult        parseResult,
			ParserTypeProvider typeProvider,
			ParserType         type,
			String             parameter,
			int                tabCount,
			Quantifier         quantifier) {
		// NOTE: If Minimum, Check first match until reaching the lower bound, the try
		// the full length until match.
		
		// Check if it has reach the minimum
		if (times >= quantifier.lowerBound()) {
			// Try the last part
			int savedIndex = parseResult.rawEntryCount();
			// Parse the last part. If match, return
			var lastResult = parse(entries, text, offset, index + 1, 0, parseResult, typeProvider, type, parameter, tabCount);
			if (lastResult != null)
				return MatchedCompletely;
			
			// Else continue, the next loop
			
			// Recover what may have been added in the fail attempt
			parseResult.reset(savedIndex);
		}
		
		// Check it reach the maximum
		if ((quantifier.hasUpperBound()) && (times >= quantifier.upperBound()))
			return Unmatched;
		
		var entry        = entries[index];
		var entryType    = entry.type();
		var entryTypeRef = entry.typeRef();
		var entryChecker = entry.checker();
		
		boolean isAlternative
				= ((entryType == null) && (entryTypeRef == null))
				&& !(entryChecker instanceof RegParser)
				&&  (entryChecker instanceof CheckerAlternative);
		
		if (isAlternative) {
			
			int minLength = Integer.MAX_VALUE;
			var minResult = (TemporaryParseResult)null;
			
			// Not yet
			var alternatives = (CheckerAlternative) entryChecker;
			var checkers     = alternatives.checkers().toArray(Checker[]::new);
			for (int c = checkers.length; --c >= 0;) {
				// Try the first part
				var newResult = newResult(parseResult);
				var checker = checkers[c];
				var tryResult = parseEach(entries, text, offset, index, null, null, null, checker, newResult, typeProvider, tabCount);
				if (tryResult != null) {
					// Match
					// Try the later part, if not match, continue other alternatives
					var nextResult = parse(entries, text, newResult.endPosition(), index, times + 1, newResult, typeProvider, type, parameter, tabCount);
					if (nextResult == null)
						continue;
						
					// Match, so record as max
					// Find the longer length
					if (minLength <= newResult.endPosition())
						continue;
					
					minResult = newResult;
					minLength = newResult.endPosition();
					
					if ((minLength + offset) >= offset)
						break;
				}
			}
			
			if (minResult != null) {
				// Merge the best result if found.
				parseResult.mergeWith(minResult);
				return MatchedCompletely;
			}
			
			if (alternatives.hasDefault()) {
				int savedIndex     = parseResult.rawEntryCount();
				var defaultChecker = alternatives.defaultChecker();
				var defaultResult  = parseEach(entries, text, offset, index, null, null, null, defaultChecker, parseResult, typeProvider, tabCount);
				if (defaultResult != null) {
					// Found the match.
					return MatchedCompletely;
				}
				
				// Recover what may have been added in the fail attempt
				parseResult.reset(savedIndex);
				// Not found, return as not found
			}
			return Unmatched;
		}
		int savedIndex = parseResult.rawEntryCount();
		// Try the first part
		var firstResult = parseEach(entries, text, offset, index, parseResult, typeProvider, tabCount);
		if (firstResult != null)
			return MatchedOneTime;
		
		// Recover what may have been added in the fail attempt
		parseResult.reset(savedIndex);
		// Not found, return as not found
		return Unmatched;
	}
	
	//== Debug =========================================================================================================
	
	/** Cache for Tabs */
	private static Map<Integer, String> tabs = new HashMap<Integer, String>();
	
	private static String debugSeparatorSuffix = "----------------------------------------------------------------------------------";
	private static int    debugTextLength      = 50;
	
	private static String indentation(int tabCount) {
		if (tabCount < tabs.size())
			return tabs.get(tabCount);
		
		if (tabCount == 0) {
			tabs.put(tabCount, "");
			return "";
		}
		
		var indentation = "  " + indentation(tabCount - 1);
		tabs.put(tabCount, indentation);
		return indentation;
	}
	
	private static void debugMainLoop(
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
		DebugPrintStream.println(indentation + debugSeparatorSuffix);
		
		int tailPosition = ((textLength >= debugTextLength) || ((offset + debugTextLength) >= textLength) ? textLength : (offset + debugTextLength));
		var suffix       =  (textLength >= debugTextLength)                                               ? "..."      : "";
		var displayText  = text.subSequence(offset, tailPosition).toString() + suffix;
		var escapeText   = escapeText(displayText);
		var entry        = entries[index];
		var typeString   = (type  == null) ? "" : type + "(" + parameter + ")" + " ~ ";
		var timesString  = (times ==    0) ? "" :        "(" + times     + ")";
		var logMessage   = format("%s`%s` ~ %s", indentation, escapeText, typeString + entry + timesString);
		DebugPrintStream.println(logMessage);
	}
	
	private static void debugParseFinish(ParseResult parseResult, int tabCount, String indentation) {
		if (DebugPrintStream == null) {
			DebugPrintStream = System.out;
		}
		
		DebugPrintStream.println(indentation + parseResult.toString(tabCount, 0));
		DebugPrintStream.println(indentation + debugSeparatorSuffix);
	}
	
}
