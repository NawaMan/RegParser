package net.nawaman.regparser;

import static net.nawaman.regparser.result.ParseResult.newResult;
import static net.nawaman.regparser.result.entry.ParseResultEntry.newEntry;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerFixeds;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.TemporaryParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

class RegParserEachSolver {
	
	/** Parse an entry at the index index possessively */
	static ParseResult parseEach(
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
	static ParseResult parseEach(
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
	
}
