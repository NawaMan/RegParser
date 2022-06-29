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
	
	private final AtomicReference<RPText> finalResult = new AtomicReference<RPText>(null);
	
	private RPText longestText = null;
	
	// NOTE: Optimize the data here.
	private List<Snapshot> snapshots = new ArrayList<Snapshot>();
	
	private ParserStack stack;
	private RPText      text;
	private int         offset;
	
	public NewRegParserResolver(String originalText, RegParser parser, ParserTypeProvider typeProvider) {
		this.stack  = new ParserStack(parser, typeProvider);
		this.text   = new RPRootText(originalText);
		this.offset = 0;
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
		
		var endResult  = (RPEndText)result;
		int endOffset  = endResult.endOffset;
		int fullLength = result.root().originalText().length();
		if (endOffset == fullLength)
			return result;
		
		return newIncompletedText(endResult);
	}
	
	synchronized private RPText doParse() {
		// Root loop
		while (true) {
			stackLoop: while (stack.isInProgress()) {
				var upperBound = stack.upperBound();
				var lowerBound = stack.lowerBound();
				var quantifier = stack.quantifier();
				var greediness = quantifier.greediness();
				if (greediness.isDefault()) {
					 
					while (true) {
						if (stack.repeat >= upperBound)
							break;
						
						int length = tryMatchedLength();
						if (length == -1) {
							break;
						}
						
						text   =  newMatchText(length);
						offset += length;
						
						stack.repeat++;
					}
					
					if (stack.repeat < lowerBound) {
						longestText = longestText(lowerBound, upperBound);
						
						// Attempt to recover from the snapshot
						var snapshot = (Snapshot)null;
						while ((snapshot = lastSnapshot()) != null) {
							
							boolean isFallBack
									 = snapshot.quantifier.isMinimum() && (snapshot.repeat <  snapshot.upperBound())
									|| snapshot.quantifier.isMaximum() && (snapshot.repeat >= snapshot.lowerBound());
							if (isFallBack) {
								stack        = snapshot.stack;
								text         = snapshot.text;
								offset       = snapshot.offset;
								stack.repeat = snapshot.repeat;
								stack.isBackTracking = true;
								
								var indexes     = snapshot.indexes;
								var eachSession = stack;
								int index       = indexes.length;
								while (eachSession != null) {
									index--;
									eachSession.entryIndex(indexes[index]);
									eachSession = eachSession.parent;
								}
								if (snapshot.quantifier.isMaximum()) {
									stack.nextEntry();
								}
								continue stackLoop;
							}
						}
						
						return incompleteResult(lowerBound, upperBound);
					}
					
					stack.nextEntry();
					
				} else if (greediness.isObsessive()) {
					
					while (true) {
						int length = tryMatchedLength();
						if (length == -1) {
							break;
						}
						
						text   =  newMatchText(length);
						offset += length;
						
						stack.repeat++;
					}
					
					var repeat = stack.repeat;
					if (repeat < lowerBound || repeat > upperBound) {
						longestText = longestText(lowerBound, upperBound);
						
						// Attempt to recover from the snapshot
						var snapshot = (Snapshot)null;
						while ((snapshot = lastSnapshot()) != null) {
							
							boolean isFallBack
									 = snapshot.quantifier.isMinimum() && (snapshot.repeat <  snapshot.upperBound())
									|| snapshot.quantifier.isMaximum() && (snapshot.repeat >= snapshot.lowerBound());
							if (isFallBack) {
								stack        = snapshot.stack;
								text         = snapshot.text;
								offset       = snapshot.offset;
								stack.repeat = snapshot.repeat;
								stack.isBackTracking = true;
								
								var indexes     = snapshot.indexes;
								var eachSession = stack;
								int index       = indexes.length;
								while (eachSession != null) {
									index--;
									eachSession.entryIndex(indexes[index]);
									eachSession = eachSession.parent;
								}
								if (snapshot.quantifier.isMaximum()) {
									stack.nextEntry();
								}
								continue stackLoop;
							}
						}
						
						
						return incompleteResult(lowerBound, upperBound);
					}
					
					stack.nextEntry();
					
				} else if (greediness.isMinimum()) {
					
					if (!stack.isBackTracking && (stack.repeat == 0) && (lowerBound == 0)) {
						saveSnapshot(stack.quantifier());
						
					} else {
						while (true) {
							
							int length = tryMatchedLength();
							if (length == -1) {
								
								longestText = longestText(lowerBound, upperBound);
								
								// Attempt to recover from the snapshot
								var snapshot = (Snapshot)null;
								while ((snapshot = lastSnapshot()) != null) {
									
									boolean isFallBack
											 = snapshot.quantifier.isMinimum() && (snapshot.repeat <  snapshot.upperBound())
											|| snapshot.quantifier.isMaximum() && (snapshot.repeat >= snapshot.lowerBound());
									if (isFallBack) {
										stack        = snapshot.stack;
										text         = snapshot.text;
										offset       = snapshot.offset;
										stack.repeat = snapshot.repeat;
										stack.isBackTracking = true;
										
										var indexes     = snapshot.indexes;
										var eachSession = stack;
										int index       = indexes.length;
										while (eachSession != null) {
											index--;
											eachSession.entryIndex(indexes[index]);
											eachSession = eachSession.parent;
										}
										if (snapshot.quantifier.isMaximum()) {
											stack.nextEntry();
										}
										continue stackLoop;
									}
								}
								
								return incompleteResult(lowerBound, upperBound);
							}
							
							text   =  newMatchText(length);
							offset += length;
							
							stack.repeat++;
							
							if (stack.repeat >= lowerBound) {
								saveSnapshot(stack.quantifier());
								break;
							}
						}
					}
					
					stack.nextEntry();
					
				} else if (greediness.isMaximum()) {
					
					while (true) {
						
						if (stack.repeat >= lowerBound) {
							saveSnapshot(stack.quantifier());
						}
						
						if (stack.repeat >= upperBound) {
							break;
						}
						
						int length = tryMatchedLength();
						if (length == -1) {
							if (stack.repeat >= lowerBound) {
								break;
							}
							
							longestText = longestText(lowerBound, upperBound);
							
							// Attempt to recover from the snapshot
							var snapshot = (Snapshot)null;
							while ((snapshot = lastSnapshot()) != null) {
								
								boolean isFallBack
										 = snapshot.quantifier.isMinimum() && (snapshot.repeat <  snapshot.upperBound())
										|| snapshot.quantifier.isMaximum() && (snapshot.repeat >= snapshot.lowerBound());
								if (isFallBack) {
									stack        = snapshot.stack;
									text         = snapshot.text;
									offset       = snapshot.offset;
									stack.repeat = snapshot.repeat;
									stack.isBackTracking = true;
									
									var indexes     = snapshot.indexes;
									var eachSession = stack;
									int index       = indexes.length;
									while (eachSession != null) {
										index--;
										eachSession.entryIndex(indexes[index]);
										eachSession = eachSession.parent;
									}
									if (snapshot.quantifier.isMaximum()) {
										stack.nextEntry();
									}
									continue stackLoop;
								}
							}
							
							return incompleteResult(lowerBound, upperBound);
						}
						
						text   =  newMatchText(length);
						offset += length;
						
						stack.repeat++;
					}
					
					stack.nextEntry();
					
				} else {
					throw new IllegalArgumentException("Unknown greediness: "+ greediness);
				}
				
			} 
			
			// At this point, the current stack is done.
			// See if we can move up a stack.
			if (stack.parent != null) {
				stack = stack.parent;
				stack.nextEntry();
			} else {
				var snapshot = lastSnapshot();
				if ((offset != text.length())
				 && (snapshot != null)
				 && snapshot.quantifier.greediness().isMinimum()
				 && (snapshot.repeat < snapshot.upperBound())) {
					stack        = snapshot.stack;
					text         = snapshot.text;
					offset       = snapshot.offset;
					stack.repeat = snapshot.repeat;
					stack.isBackTracking = true;
					
					var indexes     = snapshot.indexes;
					var eachSession = stack;
					int index       = indexes.length;
					while (eachSession != null) {
						index--;
						eachSession.entryIndex(indexes[index]);
						eachSession = eachSession.parent;
					}
					continue;
				}
				
				break;
			}
			
			
			
			
			
			// Stack loop
//			while (stack.isInProgress()) {
//				// Sub-Parser
//				if (stack.checker instanceof RegParser) {
//					stack = new ParserStack(stack, (RegParser)stack.checker);
//					continue;
//				}
//				
//				// Checker Repeat loop
//				while (true) {
//					int length = attemptForwardReturnMatchedLength();
//					if (length == -1)
//						break;
//					
//					advanceMatch(length);
//					
//					boolean mayStop = advanceRepeatCheckMayStop();
//					if (mayStop)
//						break;
//				}
//				
//				// TODO - We should not return the early return if this is not a root stack.
//				//        We should save the longest test, we can say that we cannot move one and
//				//          let the parent stack check the repeat and decide.
//				var result = attemptForwardOrEarlyResult();
//				if (result != null)
//					return result;
//			}
//			
//			// At this point, the current stack is done.
//			// See if we can move up a stack.
//			if (stack.parent != null) {
//				stack = stack.parent;
//				nextEntry();
//			} else {
//				break;
//			}
		}
		
		return completeResult();
	}
//	
	private int tryMatchedLength() {
		int     length    = text.match(offset, stack.checker, stack.typeProvider);
		boolean isMatched = (length != -1) && ((stack.entry.type() == null) || validateResult(length));
		return isMatched ? length : -1;
	}
	
//	private void advanceMatch(int length) {
//		text = newMatchText(length);
//		offset += length;
//	}
	
	private RPMatchText newMatchText(int length) {
		return (stack.repeat == 0)
				 ? new RPNodeText(             text, offset, stack.entry(), stack.stackIndex)
				 : new RPNextText((RPMatchText)text, length);
	}
	
//	private boolean advanceRepeatCheckMayStop() {
//		stack.repeat++;
//		
//		if (stack.repeat >= stack.upperBound())
//			return true;
//		
//		var quantifier = stack.quantifier();
//		var greediness = quantifier.greediness();
//		if (!greediness.isPossessive() && (stack.repeat >= stack.lowerBound())) {
//			saveSnapshot(quantifier);
//			
//			return greediness.isMinimum();
//		}
//		
//		return false;
//	}
	
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
		var snapshot = new Snapshot(stack, text, offset);
		snapshots.add(snapshot);
	}
	
	private RPText longestText(int lowerBound, int upperBound) {
		if ((text instanceof RPMatchText) && (offset > longestOffset())) {
			return newUnmatchedText(lowerBound, upperBound);
		} else {
			return longestText;
		}
	}
	
	private RPText newUnmatchedText(int lowerBound, int upperBound) {
		var pattern = stack.checker;
		if (stack.repeat > upperBound) {
			return new RPUnmatchedText(text, offset, () -> {
				var msg = format("Expect at most [%d] but found [%d]: ", upperBound, stack.repeat);
				return format("%s\"%s\"", msg, pattern);
			});
		}
		
		var found = stack.repeat != 0;
		return new RPUnmatchedText(text, offset, () -> {
			var msg = (!found && (lowerBound == 1))
					? format("Expect but not found: ", pattern)
					: format("Expect atleast [%d] but only found [%d]: ", lowerBound, stack.repeat);
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
	
//	private void fallback(Snapshot snapshot) {
//		stack        = snapshot.stack;
//		text         = snapshot.text;
//		offset       = snapshot.offset;
//		stack.repeat = snapshot.repeat;
//		
//		var indexes     = snapshot.indexes;
//		var eachSession = stack;
//		int index       = indexes.length;
//		while (eachSession != null) {
//			index--;
//			eachSession.entryIndex(indexes[index]);
//			eachSession = eachSession.parent;
//		}
//	}
	
	private boolean validateResult(int length) {
		// TODO - Temporary way to do the validation
		var hostResult   = (ParseResult)null;
		var rootResult   = new RootParseResult(offset, text);
		var resultEntry  = ParseResultEntry.newEntry(offset + length);
		var thisResult   = new ParseResultNode(offset, rootResult, asList(resultEntry));
		
		var type         = stack.type;
		var parameter    = stack.parameter;
		var typeProvider = stack.typeProvider;
		return type.validate(hostResult, thisResult, parameter, typeProvider);
	}
	
	private RPText incompleteResult(int lowerBound, int upperBound) {
		return (longestText != null)
				? longestText
				: newUnmatchedText(lowerBound, upperBound);
	}
	
//	private void nextEntry() {
//		stack.nextEntry();
//	}
//	
//	private RPText attemptForwardOrEarlyResult() {
//		var lowerBound = stack.lowerBound();
//		if (stack.repeat < lowerBound) {
//			saveLongestText(lowerBound);
//			
//			boolean shouldStartOver = false;
//			// Attempt to recover from the snapshot
//			var snapshot = (Snapshot)null;
//			while ((snapshot = lastSnapshot()) != null) {
//				
//				int upperBound = snapshot.upperBound();
//				boolean isFallBack
//						 = snapshot.quantifier.isMinimum() && (snapshot.repeat <  upperBound)
//						|| snapshot.quantifier.isMaximum() && (snapshot.repeat >= lowerBound);
//				if (isFallBack) {
//					fallback(snapshot);
//					return null;
//				}
//			}
//			
//			if (!shouldStartOver)
//				return incompleteResult(lowerBound);
//		}
//		
//		nextEntry();
//		return null;
//	}
	
	private RPEndText completeResult() {
		return new RPEndText(text, offset);
	}
	
	@Override
	public String toString() {
		return "NewRegParserResolver [\n"
				+ "  text="        + text        + ",\n"
				+ "  offset="      + offset      + ",\n"
				+ "  session="     + stack     + ",\n"
				+ "  finalResult=" + finalResult + ",\n"
				+ "  longestText=" + longestText + ",\n"
				+ "  snapshots="   + snapshots   + "\n"
				+ "]";
	}
	
	private static class ParserStack {
		
		final ParserStack        parent;
		final int                stackIndex;
		final ParserTypeProvider typeProvider;
		final RegParser          parser;
		final int                entryCount;
		
		private int repeat = 0;
		
		private int            entryIndex = 0;
		private RegParserEntry entry      = null;
		private Checker        checker    = null;
		private Quantifier     quantifier = null;
		private ParserType     type       = null;
		private String         parameter  = null;
		private ParseResult    hostResult = null;
		
		private boolean isBackTracking = false;
		
		ParserStack(RegParser parser, ParserTypeProvider typeProvider) {
			this.parent       = null;
			this.stackIndex   = (parent == null) ? 0 : parent.stackIndex + 1;
			this.parser       = parser;
			this.typeProvider = requireNonNullElse(typeProvider, ParserTypeProvider.Simple.defaultProvider());
			this.entryCount   = parser.getEntryCount();
			this.entryIndex   = 0;
			update();
		}
		
		ParserStack(ParserStack parent, RegParser parser) {
			this.parent       = parent;
			this.stackIndex   = (parent == null) ? 0 : parent.stackIndex + 1;
			this.parser       = parser;
			this.typeProvider = parent.typeProvider;
			this.entryCount   = parser.getEntryCount();
			this.entryIndex   = 0;
			update();
		}
		
		RegParserEntry entry() {
			return entry;
		}
		
		Quantifier quantifier() {
			return quantifier;
		}
		
		int lowerBound() {
			return quantifier.lowerBound();
		}
		
		int upperBound() {
			var upperBound = quantifier.upperBound();
			return (upperBound >= 0) ? upperBound : Integer.MAX_VALUE;
		}
		
		boolean isInProgress() {
			return entryIndex < entryCount;
		}
		
		void nextEntry() {
			entryIndex++;
			
			if (entryIndex < entryCount) {
				update();
			}
			
			repeat = 0;
			isBackTracking = false;
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
			return "ParserStack ["
					+ "stackIndex="   + stackIndex   + ", "
					+ "parser="       + parser       + ", "
					+ "repeat="       + repeat       + ", "
					+ "entryCount="   + entryCount   + ", "
					+ "entryIndex="   + entryIndex   + ", "
					+ "entry="        + entry        + ", "
					+ "checker="      + checker      + ", "
					+ "quantifier="   + quantifier   + ", "
					+ "type="         + type         + ", "
					+ "parameter="    + parameter    + ", "
					+ "hostResult="   + hostResult   + ", "
					+ "typeProvider=" + typeProvider
					+ "]";
		}
		
	}
	
	private static class Snapshot {
		
		final ParserStack stack;
		final int[]       indexes;
		final RPText      text;
		final int         offset;
		final int         repeat;
		final Quantifier  quantifier;
		
		public Snapshot(ParserStack stack, RPText text, int offset) {
			this.stack      = stack;
			this.text       = text;
			this.offset     = offset;
			this.repeat     = stack.repeat;
			this.quantifier = stack.quantifier();
			
			// Save the current indexes.
			this.indexes = new int[stack.stackIndex + 1];
			var eachSession = stack;
			while (eachSession != null) {
				this.indexes[eachSession.stackIndex] = eachSession.entryIndex;
				eachSession = eachSession.parent;
			}
		}
		
		// Upper bound of the current stack as a number (Integer.MAX_VALUE when no upper bound).
		private int upperBound() {
			int upperBound = quantifier.upperBound();
			return (upperBound == -1)
					? Integer.MAX_VALUE
					: upperBound;
		}
		
		private int lowerBound() {
			return quantifier.lowerBound();
		}
		
		@Override
		public String toString() {
			var indexes = Arrays.toString(this.indexes);
			return "Snapshot ["
					+ "stack="        + stack      + ", "
					+ "stackIndexes=" + indexes    + ", "
					+ "offset="       + offset     + ", "
					+ "repeat="       + repeat     + ", "
					+ "quantifier="   + quantifier + ", "
					+ "text="         + text
					+ "]";
		}
		
	}
	
}
