package net.nawaman.regparser.newway;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNullElse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.ParsingException;
import net.nawaman.regparser.Quantifier;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.RootParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

public class NewRegParserResolver {
	
	private final Session                 rootSession;
	private final CharSequence            originalText;
	private final AtomicReference<RPText> finalResult = new AtomicReference<RPText>(null);
	
	private RPText longestText = null;
	
	// NOTE: Optimize the data here.
	private List<Snapshot> snapshots = new ArrayList<Snapshot>();
	
	private Session session;
	private RPText  text;
	private int     offset;
	private int     repeat;
	
	public NewRegParserResolver(String originalText, RegParser parser, ParserTypeProvider typeProvider) {
		this.originalText = originalText;
		this.rootSession  = new Session(parser, typeProvider);
		
		this.session = this.rootSession;
		this.text    = new RPRootText(originalText);
		this.offset  = 0;
		this.repeat  = 0;
	}
	
	synchronized private RPText doParse() {
		while (true) {
			Session: while (session.isInProgress()) {
				if (session.checker instanceof RegParser) {
					session = new Session(session, (RegParser)session.checker, session.typeProvider);
				}
				
				var entry      = session.entry();
				var quantifier = entry.quantifier();
				var lowerBound = entry.quantifier().lowerBound();
				var upperBound = upperBound();
				
				while (true) {
					int length = matchedLength();
					if (length == -1)
						break;
					
					advance(entry, length);
					repeat++;
					
					if (repeat >= upperBound)
						break;
					
					var greediness = quantifier.greediness();
					if (!greediness.isPossessive() && (repeat >= lowerBound)) {
						saveSnapshot(quantifier);
						
						if (greediness.isMinimum())
							break;
					}
				}
				
				if (repeat < lowerBound) {
					saveLongestText(lowerBound);
					
					// Attempt to recover from the snapshot
					var snapshot = (Snapshot)null;
					while ((snapshot = lastSnapshot()) != null) {
						if (snapshot.isFallBack()) {
							fallback(snapshot);
							continue Session;
						}
					}
					
					return incompleteResult(lowerBound);
				}
				
				nextEntry();
			}
			if (session.parent != null) {
				session = session.parent;
				nextEntry();
			} else {
				break;
			}
			
		}
		
		return completeResult();
	}
	
	/**
	 * Parses the original text and returns the result.
	 * 
	 * @return  the parse result.
	 */
	public RPText parse() {
		return finalResult.updateAndGet(oldResult -> {
			if (oldResult != null) {
				return oldResult;
			}
			
			var newResult = doParse();
			return newResult;
		});
	}
	
	/**
	 * Parses the original text and ensure that the match cover the full length and returns the result.
	 * 
	 * @return  the parse result.
	 */
	public RPText match() {
		var result = parse();
		if (!(result instanceof RPEndText))
			return result;
		
		var endResult = (RPEndText)result;
		int endOffset = endResult.endOffset;
		if (endOffset == originalText.length())
			return result;
		
		return newIncompletedText(endResult);
	}
	
	private int upperBound() {
		var entry      = session.entry();
		var quantifier = entry.quantifier();
		var upperBound = quantifier.upperBound();
		return (upperBound >= 0) ? upperBound : Integer.MAX_VALUE;
	}
	
	private int matchedLength() {
		int     length    = text.match(offset, session.checker, session.typeProvider);
		boolean isMatched = (length != -1) && ((session.entry.type() == null) || validateResult(length));
		return isMatched ? length : -1;
	}
	
	private void advance(RegParserEntry entry, int length) {
		text = (repeat == 0)
			 ? new RPNodeText(             text, offset, entry, session.sessionIndex)
			 : new RPNextText((RPMatchText)text, length);
		offset += length;
	}
	
	private int longestOffset() {
		if (longestText == null)
			return Integer.MIN_VALUE;
		
		if (longestText instanceof RPMatchText)
			return ((RPMatchText)longestText).offset();
		
		if (longestText instanceof RPUnmatchedText)
			return ((RPUnmatchedText)longestText).endOffset;
		
		return Integer.MIN_VALUE;
	}
	
	private void saveSnapshot(Quantifier quantifier) {
		var snapshot = new Snapshot(session, text, offset, repeat, quantifier);
		snapshots.add(snapshot);
	}
	
	private void saveLongestText(int lowerBound) {
		if ((text instanceof RPMatchText) && (offset > longestOffset())) {
			longestText = newUnmatchedText(lowerBound);
		}
	}
	
	private RPText newUnmatchedText(int lowerBound) {
		var found   = repeat;
		var pattern = session.checker;
		return new RPUnmatchedText(text, offset, () -> {
			var msg = ((found == 0) && (lowerBound == 1))
					? format("Expect but not found: ", pattern)
					: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
			return format("%s\"%s\"", msg, pattern);
		});
	}
	
	private RPText newIncompletedText(RPEndText endText) {
		return new RPIncompletedText(endText);
	}
	
	private Snapshot lastSnapshot() {
		return snapshots.isEmpty()
				? null
				: snapshots.remove(snapshots.size() - 1);
	}
	
	private void fallback(Snapshot snapshot) {
		session = snapshot.session;
		text    = snapshot.text;
		offset  = snapshot.offset;
		repeat  = snapshot.repeat;
		
		var indexes     = snapshot.entryIndexes;
		var eachSession = session;
		int index       = indexes.length;
		while (eachSession != null) {
			index--;
			eachSession.entryIndex(indexes[index]);
			eachSession = eachSession.parent;
		}
		
		if (snapshot.quantifier.isMaximum()) {
			nextEntry();
		}
	}
	
	private boolean validateResult(int length) {
		// TODO - Temporary way to do the validation
		var hostResult   = (ParseResult)null;
		var rootResult   = new RootParseResult(offset, text);
		var resultEntry  = ParseResultEntry.newEntry(offset + length);
		var thisResult   = new ParseResultNode(offset, rootResult, asList(resultEntry));
		
		var type         = session.type;
		var parameter    = session.parameter;
		var typeProvider = session.typeProvider;
		return type.validate(hostResult, thisResult, parameter, typeProvider);
	}
	
	private RPText incompleteResult(int lowerBound) {
		return (longestText != null)
				? longestText
				: newUnmatchedText(lowerBound);
	}
	
	private void nextEntry() {
		session.nextEntry();
		repeat = 0;
	}
	
	private RPEndText completeResult() {
		return new RPEndText(text, offset);
	}
	
	private static class Session {
		final Session parent;
		final int     sessionIndex;
		
		final ParserTypeProvider typeProvider;
		final RegParser          parser;
		final int                entryCount;
		
		private int            entryIndex = 0;
		private ParseResult    hostResult = null;
		private RegParserEntry entry      = null;
		private Checker        checker    = null;
		private Quantifier     quantifier = null;
		private ParserType     type       = null;
		private String         parameter  = null;
		
		Session(RegParser parser, ParserTypeProvider typeProvider) {
			this(null, parser, typeProvider);
		}
		
		Session(Session parent, RegParser parser, ParserTypeProvider typeProvider) {
			this.parent       = parent;
			this.sessionIndex = (parent == null) ? 0 : parent.sessionIndex + 1;
			
			this.parser       = parser;
			this.typeProvider = requireNonNullElse(typeProvider, ParserTypeProvider.Simple.defaultProvider());
			this.entryCount   = parser.getEntryCount();
			this.entryIndex   = 0;
			update();
		}
		
		RegParserEntry entry() {
			return entry;
		}
		
		boolean isInProgress() {
			return entryIndex < entryCount;
		}
		
		void nextEntry() {
			entryIndex++;
			
			if (entryIndex < entryCount) {
				update();
			}
		}
		
		void entryIndex(int entryIndex) {
			this.entryIndex = entryIndex;
			
			if (entryIndex < entryCount) {
				update();
			}
		}
		
		private void update() {
			checker    = null;
			quantifier = null;
			type       = null;
			parameter  = null;
			
			entry = parser.getEntryAt(entryIndex);
			if (entry == null)
				return;
			
			checker    = entry.checker();
			quantifier = entry.quantifier();
			if (checker == null) {
				type = entry.type();
				if (type == null) {
					var typeRef = entry.typeRef();
					type        = type(typeRef);
					parameter   = typeRef.parameter();
				}
				
				checker = type.checker(hostResult, parameter, typeProvider);
			}
		}
		
		private ParserType type(ParserTypeRef typeRef) {
			var typeName = typeRef.name();
			
			type = typeProvider.type(typeName);
			if (type != null)
				return type;
				
			throw new ParsingException("RegParser type named '" + typeName + "' is not found.");
		}
		
		@Override
		public String toString() {
			return "Session ["
					+ "typeProvider=" + typeProvider + ", "
					+ "parser="       + parser + ", "
					+ "entryCount="   + entryCount + ", "
					+ "entryIndex="   + entryIndex + ", "
					+ "entry="        + entry + ", "
					+ "hostResult="   + hostResult + ", "
					+ "checker="      + checker + ", "
					+ "quantifier="   + quantifier + ", "
					+ "type="         + type + ", "
					+ "parameter="    + parameter
					+ "]";
		}
		
	}
	
	private static class Snapshot {
		final Session    session;
		final int[]      entryIndexes;
		final RPText     text;
		final int        offset;
		final int        repeat;
		final Quantifier quantifier;
		
		public Snapshot(Session session, RPText text, int offset, int repeat, Quantifier quantifier) {
			this.session      = session;
			this.text         = text;
			this.offset       = offset;
			this.repeat       = repeat;
			this.quantifier   = quantifier;
			
			this.entryIndexes = new int[session.sessionIndex + 1];
			var eachSession = session;
			while (eachSession != null) {
				this.entryIndexes[eachSession.sessionIndex] = eachSession.entryIndex;
				eachSession = eachSession.parent;
			}
		}
		
		boolean isFallBack() {
			int upperBound = upperBound();
			int lowerBound = quantifier.lowerBound();
			
			return quantifier.isMinimum() && (repeat <  upperBound)
				|| quantifier.isMaximum() && (repeat >= lowerBound);
		}
		
		private int upperBound() {
			int upperBound = quantifier.upperBound();
			return (upperBound == -1)
					? Integer.MAX_VALUE
					: upperBound;
		}
		
		@Override
		public String toString() {
			return "Snapshot ["
					+ "session="      + session                       + ", "
					+ "entryIndexes=" + Arrays.toString(entryIndexes) + ", "
					+ "offset="       + offset                        + ", "
					+ "repeat="       + repeat                        + ", "
					+ "quantifier="   + quantifier                    + ", "
					+ "text="         + text
					+ "]";
		}
		
	}
	
}
