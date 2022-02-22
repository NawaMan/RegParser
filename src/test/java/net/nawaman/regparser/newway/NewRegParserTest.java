package net.nawaman.regparser.newway;

import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static org.junit.Assert.fail;

import org.junit.Test;

import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.RegParser;

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
	
	@Test
	public void testWhitespaces_match() {
		var parser = compileRegParser("Ho Ho\tHo\nHo");
		
		validate("Ho\n"
				+ "Ho\n"
				+ "Ho\n"
				+ "Ho",
				parser);
		
		validate("RPRootText [original=HoHoHoHo]\n"
				+ "offset: 0, checker: Ho\n"
				+ "offset: 2, checker: Ho\n"
				+ "offset: 4, checker: Ho\n"
				+ "offset: 6, checker: Ho\n"
				+ "offset: 8",
				parse("HoHoHoHo", parser, typeProvider));
	}
	
	@Test
	public void testWhitespaces_notMatch() {
		var parser = compileRegParser("Ho Ho\tHo\nHo");
		
		validate("Ho\n"
				+ "Ho\n"
				+ "Ho\n"
				+ "Ho",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Ho\"\n"
				+ "RPRootText [original=Ho Ho Ho Ho]\n"
				+ "offset: 0, checker: Ho\n"
				+ "offset: 2",
				parse("Ho Ho Ho Ho", parser, typeProvider));
	}
	
	@Test
	public void testUseWhiltespaces() {
		var parser = compileRegParser("Ho[: :]Ho[:Tab:]Ho[:NewLine:]Ho");
		
		validate("Ho\n"
				+ "[\\ ]\n"
				+ "Ho\n"
				+ "[\\t]\n"
				+ "Ho\n"
				+ "[\\n]\n"
				+ "Ho",
				parser);
		
		
		validate("RPRootText [original=Ho Ho	Ho\n"
				+ "Ho]\n"
				+ "offset: 0, checker: Ho\n"
				+ "offset: 2, checker: [\\ ]\n"
				+ "offset: 3, checker: Ho\n"
				+ "offset: 5, checker: [\\t]\n"
				+ "offset: 6, checker: Ho\n"
				+ "offset: 8, checker: [\\n]\n"
				+ "offset: 9, checker: Ho\n"
				+ "offset: 11",
				parse("Ho Ho\tHo\nHo", parser, typeProvider));
	}
	
//	@Test
//	public void testParseVsMatch() {
//		var parser = compileRegParser("Shape");
//		
//		validate("Shape",
//				parser);
//		
//		// When the text match completely, `parse(...)` and `match(...)` will be the same.
//		validate("\n"
//				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
//				parser.parse("Shape"));
//		validate("\n"
//				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
//				parser.match("Shape"));
//		
//		// "parse(...)" allow tail left over. -- Notice `n` tail at the end of the text.
//		// "parse(...)" returns a result but the result will leave out the tail `n`.
//		validate("\n"
//				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
//				parser.parse("Shapen"));
//		
//		// "match(...)" does not allow tail left over.
//		// So it return `null` for not match.
//		validate(null, parser.match("Shapen"));
//	}
	
	@Test
	public void testZeroOrMore() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roar]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3, checker: r*\n"
				+ "offset: 4",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_extra() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roarrrrrrrr]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3, checker: r*\n"
				+ "offset: 4, checker: r*\n"
				+ "offset: 5, checker: r*\n"
				+ "offset: 6, checker: r*\n"
				+ "offset: 7, checker: r*\n"
				+ "offset: 8, checker: r*\n"
				+ "offset: 9, checker: r*\n"
				+ "offset: 10, checker: r*\n"
				+ "offset: 11",
				parse("Roarrrrrrrr", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_missing() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roa]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3",
				parse("Roa", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_unmatch() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Roa\"\n"
				+ "RPRootText [original=Row]\n"
				+ "offset: 0",
				parse("Row", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_justOne() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPRootText [original=Roar]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3, checker: r+\n"
				+ "offset: 4",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_extraR() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPRootText [original=Roarrrrrrrr]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3, checker: r+\n"
				+ "offset: 4, checker: r+\n"
				+ "offset: 5, checker: r+\n"
				+ "offset: 6, checker: r+\n"
				+ "offset: 7, checker: r+\n"
				+ "offset: 8, checker: r+\n"
				+ "offset: 9, checker: r+\n"
				+ "offset: 10, checker: r+\n"
				+ "offset: 11",
				parse("Roarrrrrrrr", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_unmatch() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"r\"\n"
				+ "RPRootText [original=Road]\n"
				+ "offset: 0, checker: Roa\n"
				+ "offset: 3",
				parse("Road", parser, typeProvider));
	}
	
	@Test
	public void testZero_match() {
		var parser = compileRegParser("Ro^ar");
		
		validate("R\n"
				+ "o{0}\n"
				+ "ar",
				parser);
		
		validate("RPRootText [original=Rar]\n"
				+ "offset: 0, checker: R\n"
				+ "offset: 1, checker: ar\n"
				+ "offset: 3",
				parse("Rar", parser, typeProvider));
	}
	
	@Test
	public void testZero_unmatch() {
		var parser = compileRegParser("Ro^ar");
		
		validate("R\n"
				+ "o{0}\n"
				+ "ar",
				parser);
		
		validate("RPRootText [original=Roar]\n"
				+ "offset: 0, checker: R\n"
				+ "offset: 1, checker: o{0}\n"
				+ "offset: 2, checker: ar\n"
				+ "offset: 4",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testZero_greedyException() {
		try {
			compileRegParser("Ro^+ar");
			fail("Expect an exception.");
		} catch (Exception exception) {
			// Do nothing.
			validate("net.nawaman.regparser.compiler.MalFormedRegParserException: Zero quantifier cannot have maximum greediness: \n"
					+ "	-|Ro^+ar\n"
					+ "	-|   ^\n"
					+ "",
					exception);
		}
		
		try {
			compileRegParser("Ro^*ar");
			fail("Expect an exception.");
		} catch (Exception exception) {
			// Do nothing.
			validate("net.nawaman.regparser.compiler.MalFormedRegParserException: Zero quantifier cannot have minimum greediness: \n"
					+ "	-|Ro^*ar\n"
					+ "	-|   ^\n"
					+ "",
					exception);
		}
	}
	
	@Test
	public void testAny() {
		var parser = compileRegParser("A.Z");
		
		validate("A\n"
				+ ".\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A2Z]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: .\n"
				+ "offset: 2, checker: Z\n"
				+ "offset: 3",
				parse("A2Z", parser, typeProvider));
	}
	
	@Test
	public void testAny_unmatch() {
		var parser = compileRegParser("A.Z");
		
		validate("A\n"
				+ ".\n"
				+ "Z",
				parser);
		
		validate("RPImcompleteText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A-to-Z]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: .\n"
				+ "offset: 2",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testAny_minimum() {
		var parser = compileRegParser("A.**Z");
		
		validate("A\n"
				+ ".**\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, checker: A\n"
				+ "offset: 1, checker: .**\n"
				+ "offset: 2, checker: .**\n"
				+ "offset: 3, checker: .**\n"
				+ "offset: 4, checker: .**\n"
				+ "offset: 5, checker: Z\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	static RPText parse(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
		return new NewRegParserResolver().parse(orgText, parser, typeProvider);
	}
	
}
