package net.nawaman.regparser.newway;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.ArrayList;

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
	
	private static class Session {
		final ParserTypeProvider typeProvider;
		final RegParser          parser;
		final int                entryCount;
		
		int            entryIndex = 0;
		ParseResult    hostResult = null;
		RegParserEntry entry      = null;
		Checker        checker    = null;
		Quantifier     quantifier = null;
		ParserType     type       = null;
		String         parameter  = null;
		
		public Session(RegParser parser, ParserTypeProvider typeProvider) {
			this.parser       = parser;
			this.typeProvider = typeProvider;
			this.entryCount   = parser.getEntryCount();
			entryIndex = 0;
			update();
		}
		
		public RegParserEntry entry() {
			return entry;
		}
		
		public boolean isInProgress() {
			return entryIndex < entryCount;
		}
		
		public void nextEntry() {
			entryIndex++;
			
			if (entryIndex < entryCount) {
				update();
			}
		}
		
		public void entryIndex(int entryIndex) {
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
					type        = type(type, typeRef);
					parameter   = typeRef.parameter();
				}
				
				checker = type.checker(hostResult, parameter, typeProvider);
			}
		}
		
		private ParserType type(ParserType type, ParserTypeRef typeRef) {
			var typeName = typeRef.name();
			
			// Get type from the ref
			if (typeProvider != null) {
				// Get from the given provider
				type = typeProvider.type(typeName);
			}
			if (type == null) {
				// Get from the default
				type = ParserTypeProvider.Simple.defaultProvider().type(typeName);
				if (type == null)
					throw new ParsingException("RegParser type named '" + typeName + "' is not found.");
			}
			return type;
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
		final int        sessIdx;
		final int        entryIndex;
		final RPText     text;
		final int        offset;
		final int        repeat;
		final Quantifier quantifier;
		
		public Snapshot(int sessIdx, int entryIndex, RPText text, int offset, int repeat, Quantifier quantifier) {
			this.sessIdx    = sessIdx;
			this.entryIndex = entryIndex;
			this.text       = text;
			this.offset     = offset;
			this.repeat     = repeat;
			this.quantifier = quantifier;
		}
		
		boolean isFallBackMinimum() {
			int upperBound = quantifier.upperBound();
			upperBound = (upperBound == -1) ? Integer.MAX_VALUE : upperBound;
			
			return quantifier.isMinimum() && (repeat < upperBound);
		}
		
		boolean isFallBackMaximum() {
			int lowerBound = quantifier.lowerBound();
			return quantifier.isMaximum() && (repeat >= lowerBound);
		}
		
		@Override
		public String toString() {
			return "Snapshot ["
					+ "sessIdx="    + sessIdx    + ", "
					+ "entryIndex=" + entryIndex + ", "
					+ "offset="     + offset     + ", "
					+ "repeat="     + repeat     + ", "
					+ "quantifier=" + quantifier + ", "
					+ "text="       + text
					+ "]";
		}
		
	}
	
	static RPText parse(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
		RPText longestText = null;
		
		var sessions = new ArrayList<Session>();
		
		// NOTE: Optimize the data here.
		var snapshots = new ArrayList<Snapshot>();
		
		int     sessIdx = 0;
		Session session = new Session(parser, typeProvider);
		sessions.add(session);
		
		RPText text   = new RPRootText(orgText);
		int    offset = 0;
		int    repeat = 0;
		
		Session: while (session.isInProgress()) {
			var entry      = session.entry();
			var quantifier = entry.quantifier();
			var lowerBound = quantifier.lowerBound();
			var upperBound = quantifier.upperBound();
			var greediness = quantifier.greediness();
			
			upperBound = (upperBound >= 0) ? upperBound : Integer.MAX_VALUE;
			
			Repeat: while (true) {
				int length = match(text, offset, session);
				if (length == -1)
					break Repeat;
				
				text = new RPNodeText(text, offset, entry);
				offset += length;
				repeat++;
				
				if (repeat >= upperBound)
					break Repeat;
				
				if (!greediness.isPossessive() && (repeat >= lowerBound)) {
					var snapshot = new Snapshot(sessIdx, session.entryIndex, text, offset, repeat, quantifier);
					snapshots.add(snapshot);
					if (greediness.isMinimum()) {
						break Repeat;
					}
				}
			}
			
			if (repeat < lowerBound) {
				if ((text instanceof RPNodeText) && (offset > longestOffset(longestText))) {
					longestText = newLongestText(session, text, offset, repeat, lowerBound);
				}
				
				// Unable to go forward any more
				var snapshot = lastSnapshots(snapshots);
				while (snapshot != null) {
					boolean isFallBackMinimum = snapshot.isFallBackMinimum();
					boolean isFallBackMaximum = snapshot.isFallBackMaximum();
					if (isFallBackMinimum || isFallBackMaximum) {
						sessIdx = snapshot.sessIdx;
						session = sessions.get(sessIdx);
						text    = snapshot.text;
						offset  = snapshot.offset;
						repeat  = snapshot.repeat;
						session.entryIndex(snapshot.entryIndex);
						
						if (isFallBackMaximum) {
							session.nextEntry();
							repeat = 0;
						}
						
						continue Session;
					}
					
					snapshot = lastSnapshots(snapshots);
				}
				
				if (snapshot == null) {
					if (longestText != null)
						return longestText;
					
					var found   = repeat;
					var pattern = session.checker;
					return new RPImcompleteText(text, offset, () -> {
						var msg = ((found == 0) && (lowerBound == 1))
								? format("Expect but not found: ", pattern)
								: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
						return format("%s\"%s\"", msg, pattern);
					});
				}
				
				sessIdx = snapshot.sessIdx;
				session = sessions.get(sessIdx);
				text    = snapshot.text;
				offset  = snapshot.offset;
				repeat  = snapshot.repeat;
				session.entryIndex(session.entryIndex);
			}
			
			session.nextEntry();
			repeat = 0;
		}
		text = new RPEndText(text, offset);
		
		System.out.println(text.root().matches);
		System.out.println(text);
		System.out.println();
		return text;
	}

	private static Snapshot lastSnapshots(ArrayList<Snapshot> snapshots) {
		return snapshots.isEmpty() ? null : snapshots.remove(snapshots.size() - 1);
	}

	private static int longestOffset(RPText longestText) {
		if (longestText == null)
			return Integer.MIN_VALUE;
		
		if (longestText instanceof RPNodeText)
			return ((RPNodeText)longestText).offset;
		
		if (longestText instanceof RPImcompleteText)
			return ((RPImcompleteText)longestText).endOffset;
		
		return Integer.MIN_VALUE;
	}

	private static RPText newLongestText(Session session, RPText text, int offset, int repeat, int lowerBound) {
		RPText longestText;
		var found   = repeat;
		var pattern = session.checker;
		
		longestText = new RPImcompleteText(text, offset, () -> {
			var msg = ((found == 0) && (lowerBound == 1))
					? format("Expect but not found: ", pattern)
					: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
			return format("%s\"%s\"", msg, pattern);
		});
		return longestText;
	}
	
	private static int match(RPText text, int offset, Session session) {
		int length = text.match(offset, session.checker, session.typeProvider);
		
		System.out.println(session.entryIndex + ": " + session.entry + " = " + length);
		boolean isMatched
				=  (length != -1)
				&& ((session.entry.type() == null) || validateResult(text, offset, length, session));
		return isMatched ? length : -1;
	}
	
	private static boolean validateResult(
			RPText  text,
			int     offset,
			int     length,
			Session session) {
		var hostResult   = (ParseResult)null;
		var rootResult   = new RootParseResult(offset, text);
		var resultEntry  = ParseResultEntry.newEntry(offset + length);
		var thisResult   = new ParseResultNode(offset, rootResult, asList(resultEntry));
		var type         = session.type;
		var parameter    = session.parameter;
		var typeProvider = session.typeProvider;
		return type.validate(hostResult, thisResult, parameter, typeProvider);
	}
}
