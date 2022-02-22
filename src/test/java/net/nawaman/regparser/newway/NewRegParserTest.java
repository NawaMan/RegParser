package net.nawaman.regparser.newway;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import java.util.ArrayList;

import org.junit.Test;

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

public class NewRegParserTest {
	
	private ParserTypeProvider typeProvider = null;
	
	@Test
	public void test() {
		var parser = compileRegParser("Colou?r");
		
		parse("Color", parser, typeProvider);
		System.out.println();
		
		parse("Colour", parser, typeProvider);
		System.out.println();
		
		System.out.println("DONE!");
	}
	
	@Test
	public void testExact() {
		var parser = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, checker: Shape\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
	}
	
	@Test
	public void testExact_notMatch() {
		var parser = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Shape\"\n"
				+ "RPRootText [original=Sharp]\n"
				+ "offset: 0",
				parse("Sharp", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick() {
		var parser = compileRegParser("`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, checker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_notMatch() {
		var parser = compileRegParser("`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"shape\\ and\\ shade\"\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive() {
		var parser = compileRegParser("$`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, checker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive_notMatch() {
		var parser = compileRegParser("$`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"shape\\ and\\ shade\"\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseInsensitive() {
		var parser = compileRegParser("#`shape and shade`");
		
		validate("(!textCI(\"shape and shade\")!)\n"
				+ "  - (!textCI(\"shape and shade\")!)",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, checker: (!textCI(\"shape and shade\")!)\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseInsensitive_notMatch() {
		var parser = compileRegParser("#`shape and shade`");
		
		validate("(!textCI(\"shape and shade\")!)\n"
				+ "  - (!textCI(\"shape and shade\")!)",
				parser);
		
		validate("RPRootText [original=Shape and Shade]\n"
				+ "offset: 0, checker: (!textCI(\"shape and shade\")!)\n"
				+ "offset: 15",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testTextCI_withUpperCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, checker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
	}
	
	@Test
	public void testTextCI_allLowerCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=shape]\n"
				+ "offset: 0, checker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("shape", parser, typeProvider));
	}
	
	@Test
	public void testTextCI_allUpperCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=SHAPE]\n"
				+ "offset: 0, checker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("SHAPE", parser, typeProvider));
	}
	
	@Test
	public void testTextCI_escape() {
		var parser = compileRegParser("!textCI(`this is a \"test\".`)!");
		
		validate("(!textCI(\"this is a \\\"test\\\".\")!)",
				parser);
		
		validate("RPRootText [original=This is a \"test\".]\n"
				+ "offset: 0, checker: (!textCI(\"this is a \\\"test\\\".\")!)\n"
				+ "offset: 17",
				parse("This is a \"test\".", parser, typeProvider));
	}
	
	@Test
	public void testOptional_wihtout() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPRootText [original=Color]\n"
				+ "offset: 0, checker: Colo\n"
				+ "offset: 4, checker: r\n"
				+ "offset: 5",
				parse("Color", parser, typeProvider));
	}
	
	@Test
	public void testOptional_with() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPRootText [original=Colour]\n"
				+ "offset: 0, checker: Colo\n"
				+ "offset: 4, checker: u?\n"
				+ "offset: 5, checker: r\n"
				+ "offset: 6",
				parse("Colour", parser, typeProvider));
	}
	
	@Test
	public void testOptional_unmatch() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Colo\"\n"
				+ "RPRootText [original=Clr]\n"
				+ "offset: 0",
				parse("Clr", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_2() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_3() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A0ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_4() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_justEnough_1() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_justEnough_2() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_tooMany() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_notEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_justEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, checker: Z+\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_enough_tailGetMore() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A01ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, checker: Z+\n"
				+ "offset: 4, checker: Z+\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_stretch1() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, checker: Z+\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_stretchToMax() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_cannotStretchEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_notEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_justEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, checker: Z+\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_enough1() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A01ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, checker: Z+\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_enough2_max() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_max() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5, checker: Z+\n"
				+ "offset: 6, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_tooManyMatch() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	//=========================================================================
	
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
	
	private static RPText parse(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
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
				if (text instanceof RPNodeText) {
					if (longestText != null) {
						if ((longestText instanceof RPNodeText) && (offset > ((RPNodeText)longestText).offset)) {
							var found   = repeat;
							var pattern = session.checker;
							
							longestText = new RPImcompleteText(text, offset, () -> {
								var msg = ((found == 0) && (lowerBound == 1))
										? format("Expect but not found: ", pattern)
										: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
								return format("%s\"%s\"", msg, pattern);
							});
						}
						if ((longestText instanceof RPImcompleteText) && (offset > ((RPImcompleteText)longestText).endOffset)) {
							var found   = repeat;
							var pattern = session.checker;
							
							longestText = new RPImcompleteText(text, offset, () -> {
								var msg = ((found == 0) && (lowerBound == 1))
										? format("Expect but not found: ", pattern)
										: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
								return format("%s\"%s\"", msg, pattern);
							});
						}
					} else {
						var found   = repeat;
						var pattern = session.checker;
						
						longestText = new RPImcompleteText(text, offset, () -> {
							var msg = ((found == 0) && (lowerBound == 1))
									? format("Expect but not found: ", pattern)
									: format("Expect atleast [%d] but only found [%d]: ", lowerBound, found);
							return format("%s\"%s\"", msg, pattern);
						});
					}
				}
				
				// Unable to go forward any more
				var snapshot = snapshots.isEmpty() ? null : snapshots.remove(snapshots.size() - 1);
				while (snapshot != null) {
					if (snapshot.quantifier.isMinimum() && (snapshot.repeat < snapshot.quantifier.upperBound())) {
						sessIdx = snapshot.sessIdx;
						session = sessions.get(sessIdx);
						text    = snapshot.text;
						offset  = snapshot.offset;
						repeat  = snapshot.repeat;
						session.entryIndex(snapshot.entryIndex);
						continue Session;
					}
					if (snapshot.quantifier.isMaximum() && (snapshot.repeat >= snapshot.quantifier.lowerBound())) {
						sessIdx = snapshot.sessIdx;
						session = sessions.get(sessIdx);
						text    = snapshot.text;
						offset  = snapshot.offset;
						repeat  = snapshot.repeat;
						session.entryIndex(snapshot.entryIndex);
						session.nextEntry();
						repeat = 0;
						continue Session;
					}
					
					snapshot = snapshots.isEmpty() ? null : snapshots.remove(snapshots.size() - 1);
				}
				
				if (snapshot == null) {
					if (longestText != null) {
						return longestText;
					}
					
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
