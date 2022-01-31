package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

import net.nawaman.regparser.types.SimpleParserType;

public class TestPattern {
	
	@Test
	public void testExact() {
		var parser = compileRegParser("Shape");
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
				parser.parse("Shape"));
		
		validate(null, parser.parse("Sharp"));
	}
	
	@Test
	public void testOptional() {
		var parser = compileRegParser("Colou?r");
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"Color\"",
				parser.parse("Color"));
		validate("\n"
				+ "00 => [    6] = <NoName>        :<NoType>         = \"Colour\"",
				parser.parse("Colour"));
		
		validate(null, parser.parse("Clr"));
	}
	
	@Test
	public void testWhitespaces() {
		var parser = compileRegParser("Ho Ho\tHo\nHo");
		
		validate(null, parser.parse("Ho Ho Ho Ho"));
		
		validate("\n"
				+ "00 => [    8] = <NoName>        :<NoType>         = \"HoHoHoHo\"",
				parser.parse("HoHoHoHo"));
	}
	
	@Test
	public void testUseWhiltespaces() {
		var parser = compileRegParser("Ho[: :]Ho[:Tab:]Ho[:NewLine:]Ho");
		
		validate("\n"
				+ "00 => [   11] = <NoName>        :<NoType>         = \"Ho Ho\\tHo\\nHo\"",
				parser.parse("Ho Ho\tHo\nHo"));
	}
	
	@Test
	public void testParseVsMatch() {
		var parser = compileRegParser("Shape");
		
		// When the text match completely, `parse(...)` and `match(...)` will be the same.
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
				parser.parse("Shape"));
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
				parser.match("Shape"));
		
		// "parse(...)" allow tail left over. -- Notice `n` tail at the end of the text.
		// "parse(...)" returns a result but the result will leave out the tail `n`.
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
				parser.parse("Shapen"));
		
		// "match(...)" does not allow tail left over.
		// So it return `null` for not match.
		validate(null, parser.match("Shapen"));
	}
	
	@Test
	public void testZeroOrMore() {
		var parser = compileRegParser("Roar*");
		validate("\n"
				+ "00 => [    4] = <NoName>        :<NoType>         = \"Roar\"",
				parser.parse("Roar"));
		validate("\n"
				+ "00 => [   11] = <NoName>        :<NoType>         = \"Roarrrrrrrr\"",
				parser.parse("Roarrrrrrrr"));
		validate("\n"
				+ "00 => [    3] = <NoName>        :<NoType>         = \"Roa\"",
				parser.parse("Roa"));
		
		validate(null, parser.parse("Row"));
	}
	
	@Test
	public void testOneOrMore() {
		var parser = compileRegParser("Roar+");
		validate("\n"
				+ "00 => [    4] = <NoName>        :<NoType>         = \"Roar\"",
				parser.parse("Roar"));
		validate("\n"
				+ "00 => [   11] = <NoName>        :<NoType>         = \"Roarrrrrrrr\"",
				parser.parse("Roarrrrrrrr"));
		
		validate(null, parser.parse("Road"));
	}
	
	@Test
	public void testAny() {
		var parser = compileRegParser("A.Z");
		validate("\n"
				+ "00 => [    3] = <NoName>        :<NoType>         = \"A2Z\"",
				parser.parse("A2Z"));
		
		validate(null, parser.parse("A-to-Z"));
	}
	
	@Test
	public void testCharClass() {
		var parser = compileRegParser("A[0-9]+Z");
		validate("\n"
				+ "00 => [    3] = <NoName>        :<NoType>         = \"A2Z\"",
				parser.parse("A2Z"));
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"A123Z\"",
				parser.parse("A123Z"));
		
		validate(null, parser.parse("A-to-Z"));
	}
	
	@Test
	public void testCharClass_negative() {
		// We use `++` as one-or-more-maximum, because just one-or-more will be possessive and it will include Z
		//   and no place for `Z`.
		var parser = compileRegParser("A[^0-9]++Z");
		
		validate(null, parser.parse("A2Z"));
		validate("\n"
				+ "00 => [    6] = <NoName>        :<NoType>         = \"A-to-Z\"",
				parser.parse("A-to-Z"));
	}
	
	@Test
	public void testAlternatives() {
		var parser = compileRegParser("(true|false)");
		validate("\n"
				+ "00 => [    4] = <NoName>        :<NoType>         = \"true\"",
				parser.parse("true"));
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"false\"",
				parser.parse("false"));
		
		validate(null, parser.parse("maybe"));
	}
	
	@Test
	public void testAlternatives_negative() {
		var parser = compileRegParser("(^true|false)");
		validate(null, parser.parse("true"));
		validate(null, parser.parse("false"));
		
		// Notice only one character match
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"u\"",
				parser.parse("unsure"));
		
		//-- This might be more practically useful.
		
		parser = compileRegParser("(^true|false)+");
		validate(null, parser.parse("true"));
		validate(null, parser.parse("false"));
		
		// Notice only one character match
		validate("\n"
				+ "00 => [    6] = <NoName>        :<NoType>         = \"unsure\"",
				parser.parse("unsure"));
	}
	
	// TODO - Alternative with default.
	
	@Test
	public void testComment_slashStar() {
		// We comment out the work `not `.
		var parser = compileRegParser("Orange[: :]is[: :]/*not[: :]*/a[: :]color.");
		validate("\n"
				+ "00 => [   18] = <NoName>        :<NoType>         = \"Orange is a color.\"",
				parser.parse("Orange is a color."));
		
		validate(null, parser.parse("Orange is not a color."));
	}
	
	@Test
	public void testComment_backetStar() {
		var parser = compileRegParser("Orange[: :]is[: :](*not[: :]*)a[: :]color.");
		validate("\n"
				+ "00 => [   18] = <NoName>        :<NoType>         = \"Orange is a color.\"",
				parser.parse("Orange is a color."));
		
		validate(null, parser.parse("Orange is not a color."));
	}
	
	@Test
	public void testComment_lineComment() {
		var parser = compileRegParser(
						"Orange[: :]is[: :]//not[: :]\n"
						+ "a[: :]color.");
		validate("\n"
				+ "00 => [   18] = <NoName>        :<NoType>         = \"Orange is a color.\"",
				parser.parse("Orange is a color."));
		
		validate(null, parser.parse("Orange is not a color."));
	}
	
	@Test
	public void testPossessive() {
		var parser = compileRegParser("A.*Z");
		var result = parser.parse("A123Z");
		validate(null, result);
	}
	
	@Test
	public void testMinimum() {
		var parser = compileRegParser("A.**Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"A123Z\"",
				result);
	}
	
	@Test
	public void testMaximum() {
		var parser = compileRegParser("A.*+Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    5] = <NoName>        :<NoType>         = \"A123Z\"",
				result);
	}

	@Test
	public void testPossessive_named() {
		var parser = compileRegParser("A($Middle:~.~)*Z");
		var result = parser.parse("A123Z");
		validate(null, result);
	}
	
	@Test
	public void testMinimum_named() {
		var parser = compileRegParser("A($Middle:~.~)**Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    2] = $Middle         :<NoType>         = \"1\"\n"
				+ "02 => [    3] = $Middle         :<NoType>         = \"2\"\n"
				+ "03 => [    4] = $Middle         :<NoType>         = \"3\"\n"
				+ "04 => [    5] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testMaximum_named() {
		var parser = compileRegParser("A($Middle:~.~)*+Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    2] = $Middle         :<NoType>         = \"1\"\n"
				+ "02 => [    3] = $Middle         :<NoType>         = \"2\"\n"
				+ "03 => [    4] = $Middle         :<NoType>         = \"3\"\n"
				+ "04 => [    5] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testPossessive_named_collapsed() {
		var parser = compileRegParser("A($Middle:~.~)*Z");
		var result = parser.parse("A123Z");
		validate(null, result);
	}
	
	@Test
	public void testMinimum_named_collapsed() {
		var parser = compileRegParser("A($Middle[]:~.~)**Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    4] = $Middle[]       :<NoType>         = \"123\"\n"
				+ "02 => [    5] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testMaximum_named_collapsed() {
		var parser = compileRegParser("A($Middle[]:~.~)*+Z");
		var result = parser.parse("A123Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    4] = $Middle[]       :<NoType>         = \"123\"\n"
				+ "02 => [    5] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testSub_named() {
		var parser = compileRegParser("A($Digit:~1($Two:~2($Two:~3~)4~)5~)Z");
		var result = parser.parse("A12345Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    6] = $Digit          :<NoType>         = \"12345\"\n"
				+ "02 => [    7] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testSub_grouped() {
		var parser = compileRegParser("A(#Digit:~1(#Two:~2(#Two:~3~)4~)5~)Z");
		var result = parser.parse("A12345Z");
		validate("\n"
				+ "00 - - => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 - - => [    6] = #Digit          :<NoType>         = \"12345\"\n"
				+ ". 00 - => [    2] = <NoName>        :<NoType>         = \"1\"\n"
				+ ". 01 - => [    5] = #Two            :<NoType>         = \"234\"\n"
				+ ". . 00 => [    3] = <NoName>        :<NoType>         = \"2\"\n"
				+ ". . 01 => [    4] = #Two            :<NoType>         = \"3\"\n"
				+ ". . 02 => [    5] = <NoName>        :<NoType>         = \"4\"\n"
				+ ". 02 - => [    6] = <NoName>        :<NoType>         = \"5\"\n"
				+ "02 - - => [    7] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	@Test
	public void testType() {
		var intType      = new SimpleParserType("int", RegParser.compile("[0-9]+"));
		var typeProvider = new ParserTypeProvider.Simple(intType);
		var parser       = compileRegParser(typeProvider, "A!int!Z");
		var result       = parser.parse("A12345Z");
		validate("\n"
				+ "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
				+ "01 => [    6] = <NoName>        :int              = \"12345\"\n"
				+ "02 => [    7] = <NoName>        :<NoType>         = \"Z\"",
				result);
	}
	
	// Test error
	
	private ParserType charEscape 
			= new SimpleParserType("charEscape", compile(
					  "[:\\:]"
					+ "("
					+ "		0("
					+ "			[0-3][0-7][0-7]"
					+ "			|| ("
					+ "				[0-7][0-7]"
					+ "				||"
					+ "				[0-7]"
					+ "			)"
					+ "		)"
					+ "		|"
					+ "		[xX][0-9a-fA-F]{2}"
					+ "		|"
					+ "		[uU][0-9a-fA-F]{4}"
					+ "		|"
					+ "		[[:\\:][:\":][:':]tnrbf]"
					+ "		||"
					+ "		(#ERROR_Invalid_Escape_Character:~.~)"
					+ "	)"));
	
	private ParserType stringLiteral
			= new SimpleParserType("stringLiteral", compile(
					  "[:\":]"
					+ "(($Chars[]:~[^[:\":][:NewLine:]]~)|(#EscapeChr:!charEscape!))*\n"
					+ "([:\":] || ($ERROR_Missing_the_closing_quatation_mark:~.{0}~))"));
	
	private ParserType identifier
			= new SimpleParserType("identifier", compile("[a-zA-Z][a-zA-Z0-9]*"));
	
	private ParserTypeProvider typeProvider 
			= new ParserTypeProvider.Simple(charEscape, stringLiteral, identifier);
	
	private RegParser assignStringParser
			= compileRegParser(typeProvider,
					"var[: :]($VarName:~str~)[: :]=[: :](#Value:~!stringLiteral!~)(;||($ERROR_Missing_semicolon:~.{0}~))");
	
	@Test
	public void testCharEscape() {
		validate("\n"
				+ "00 => [    4] = <NoName>        :charEscape       = \"\\\\064\"",
				charEscape.parse("\\064"));
	}
	
	@Test
	public void testCharEscape_ERROR() {
		validate("\n"
				+ "00 - => [    2] = <NoName>        :charEscape       = \"\\\\s\"\n"
				+ ". 00 => [    1] = <NoName>        :<NoType>         = \"\\\\\"\n"
				+ ". 01 => [    2] = #ERROR_Invalid_Escape_Character:<NoType>         = \"s\"",
				charEscape.parse("\\s"));
	}
	
	@Test
	public void testStringLiteral() {
		validate("\n"
				+ "00 - => [   19] = <NoName>        :stringLiteral    = \"\\\"This is a string.\\\"\"\n"
				+ ". 00 => [    1] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". 01 => [   18] = $Chars[]        :<NoType>         = \"This is a string.\"\n"
				+ ". 02 => [   19] = <NoName>        :<NoType>         = \"\\\"\"",
				compileRegParser(typeProvider, "!stringLiteral!")
				.parse("\"This is a string.\""));
	}
	
	@Test
	public void testStringLiteral_withEscape() {
		validate("\n"
				+ "00 - => [   19] = <NoName>        :stringLiteral    = \"\\\"This\\\\\\'s a string.\\\"\"\n"
				+ ". 00 => [    1] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". 01 => [    5] = $Chars[]        :<NoType>         = \"This\"\n"
				+ ". 02 => [    7] = #EscapeChr      :charEscape       = \"\\\\\\'\"\n"
				+ ". 03 => [   18] = $Chars[]        :<NoType>         = \"s a string.\"\n"
				+ ". 04 => [   19] = <NoName>        :<NoType>         = \"\\\"\"",
				compileRegParser(typeProvider, "!stringLiteral!")
				.parse("\"This\\'s a string.\""));
	}
	
	@Test
	public void testStringLiteral_noClosing() {
		validate("\n"
				+ "00 - => [    5] = <NoName>        :stringLiteral    = \"\\\"This\"\n"
				+ ". 00 => [    1] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". 01 => [    5] = $Chars[]        :<NoType>         = \"This\"\n"
				+ ". 02 => [    5] = $ERROR_Missing_the_closing_quatation_mark:<NoType>         = \"\"",
				compileRegParser(typeProvider, "!stringLiteral!")
				.parse("\"This"));
	}
	
	@Test
	public void testComplexType_matchBasic() {
		validate("\n"
				+ "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
				+ "01 - - => [    7] = $VarName        :<NoType>         = \"str\"\n"
				+ "02 - - => [   10] = <NoName>        :<NoType>         = \" = \"\n"
				+ "03 - - => [   27] = #Value          :<NoType>         = \"\\\"This is a text.\\\"\"\n"
				+ ". 00 - => [   27] = <NoName>        :stringLiteral    = \"\\\"This is a text.\\\"\"\n"
				+ ". . 00 => [   11] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". . 01 => [   26] = $Chars[]        :<NoType>         = \"This is a text.\"\n"
				+ ". . 02 => [   27] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ "04 - - => [   28] = <NoName>        :<NoType>         = \";\"",
				assignStringParser.parse("var str = \"This is a text.\";"));
	}
	
	@Test
	public void testComplexType_matchWithEscape() {
		validate("\n"
				+ "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
				+ "01 - - => [    7] = $VarName        :<NoType>         = \"str\"\n"
				+ "02 - - => [   10] = <NoName>        :<NoType>         = \" = \"\n"
				+ "03 - - => [   27] = #Value          :<NoType>         = \"\\\"This\\\\\\'s a text.\\\"\"\n"
				+ ". 00 - => [   27] = <NoName>        :stringLiteral    = \"\\\"This\\\\\\'s a text.\\\"\"\n"
				+ ". . 00 => [   11] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". . 01 => [   15] = $Chars[]        :<NoType>         = \"This\"\n"
				+ ". . 02 => [   17] = #EscapeChr      :charEscape       = \"\\\\\\'\"\n"
				+ ". . 03 => [   26] = $Chars[]        :<NoType>         = \"s a text.\"\n"
				+ ". . 04 => [   27] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ "04 - - => [   28] = <NoName>        :<NoType>         = \";\"",
				assignStringParser.parse("var str = \"This\\\'s a text.\";"));
	}
	
	@Test
	public void testComplexType_unmatchInvalidEscape() {
		validate("\n"
				+ "00 - - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
				+ "01 - - - => [    7] = $VarName        :<NoType>         = \"str\"\n"
				+ "02 - - - => [   10] = <NoName>        :<NoType>         = \" = \"\n"
				+ "03 - - - => [   27] = #Value          :<NoType>         = \"\\\"This\\\\ s a text.\\\"\"\n"
				+ ". 00 - - => [   27] = <NoName>        :stringLiteral    = \"\\\"This\\\\ s a text.\\\"\"\n"
				+ ". . 00 - => [   11] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". . 01 - => [   15] = $Chars[]        :<NoType>         = \"This\"\n"
				+ ". . 02 - => [   17] = #EscapeChr      :charEscape       = \"\\\\ \"\n"
				+ ". . . 00 => [   16] = <NoName>        :<NoType>         = \"\\\\\"\n"
				+ ". . . 01 => [   17] = #ERROR_Invalid_Escape_Character:<NoType>         = \" \"\n"
				+ ". . 03 - => [   26] = $Chars[]        :<NoType>         = \"s a text.\"\n"
				+ ". . 04 - => [   27] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ "04 - - - => [   28] = <NoName>        :<NoType>         = \";\"",
				assignStringParser.parse("var str = \"This\\ s a text.\";"));
	}
	
	@Test
	public void testComplexType_unmatchMissingClosingQuote() {
		validate("\n"
				+ "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
				+ "01 - - => [    7] = $VarName        :<NoType>         = \"str\"\n"
				+ "02 - - => [   10] = <NoName>        :<NoType>         = \" = \"\n"
				+ "03 - - => [   15] = #Value          :<NoType>         = \"\\\"Text\"\n"
				+ ". 00 - => [   15] = <NoName>        :stringLiteral    = \"\\\"Text\"\n"
				+ ". . 00 => [   11] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". . 01 => [   15] = $Chars[]        :<NoType>         = \"Text\"\n"
				+ ". . 02 => [   15] = $ERROR_Missing_the_closing_quatation_mark:<NoType>         = \"\"\n"
				+ "04 - - => [   15] = $ERROR_Missing_semicolon:<NoType>         = \"\"",
				assignStringParser.parse("var str = \"Text"));
	}
	
	@Test
	public void testComplexType_unmatchMissingSemicolon() {
		validate("\n"
				+ "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
				+ "01 - - => [    7] = $VarName        :<NoType>         = \"str\"\n"
				+ "02 - - => [   10] = <NoName>        :<NoType>         = \" = \"\n"
				+ "03 - - => [   16] = #Value          :<NoType>         = \"\\\"Text\\\"\"\n"
				+ ". 00 - => [   16] = <NoName>        :stringLiteral    = \"\\\"Text\\\"\"\n"
				+ ". . 00 => [   11] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ ". . 01 => [   15] = $Chars[]        :<NoType>         = \"Text\"\n"
				+ ". . 02 => [   16] = <NoName>        :<NoType>         = \"\\\"\"\n"
				+ "04 - - => [   16] = $ERROR_Missing_semicolon:<NoType>         = \"\"",
				assignStringParser.parse("var str = \"Text\""));
	}
	
	@Test
	public void testRecursive() {
		var lisp         = new SimpleParserType("lisp", compile("($Open:~[:(:]~)(($Content[]:~[^\\(]~)|(#Sub:~!lisp!~))**($Close:~[:):]~)"));
		var typeProvider = new ParserTypeProvider.Simple(lisp);
		var parser       = compileRegParser(typeProvider, "!lisp!");
		validate("\n"
				+ "00 - => [    6] = <NoName>        :lisp             = \"(abcd)\"\n"
				+ ". 00 => [    1] = $Open           :<NoType>         = \"(\"\n"
				+ ". 01 => [    5] = $Content[]      :<NoType>         = \"abcd\"\n"
				+ ". 02 => [    6] = $Close          :<NoType>         = \")\"",
				parser.parse("(abcd)"));
		
		validate("\n"
				+ "00 - - - => [    8] = <NoName>        :lisp             = \"(a(bc)d)\"\n"
				+ ". 00 - - => [    1] = $Open           :<NoType>         = \"(\"\n"
				+ ". 01 - - => [    2] = $Content[]      :<NoType>         = \"a\"\n"
				+ ". 02 - - => [    6] = #Sub            :<NoType>         = \"(bc)\"\n"
				+ ". . 00 - => [    6] = <NoName>        :lisp             = \"(bc)\"\n"
				+ ". . . 00 => [    3] = $Open           :<NoType>         = \"(\"\n"
				+ ". . . 01 => [    5] = $Content[]      :<NoType>         = \"bc\"\n"
				+ ". . . 02 => [    6] = $Close          :<NoType>         = \")\"\n"
				+ ". 03 - - => [    7] = $Content[]      :<NoType>         = \"d\"\n"
				+ ". 04 - - => [    8] = $Close          :<NoType>         = \")\"",
				parser.parse("(a(bc)d)"));
		
		validate("\n"
				+ "00 - - - - - => [   52] = <NoName>        :lisp             = \"(if x (list 1 2 (concat \\\"fo\\\" \\\"o\\\")) (list 3 4 \\\"bar\\\"))\"\n"
				+ ". 00 - - - - => [    1] = $Open           :<NoType>         = \"(\"\n"
				+ ". 01 - - - - => [    6] = $Content[]      :<NoType>         = \"if x \"\n"
				+ ". 02 - - - - => [   34] = #Sub            :<NoType>         = \"(list 1 2 (concat \\\"fo\\\" \\\"o\\\"))\"\n"
				+ ". . 00 - - - => [   34] = <NoName>        :lisp             = \"(list 1 2 (concat \\\"fo\\\" \\\"o\\\"))\"\n"
				+ ". . . 00 - - => [    7] = $Open           :<NoType>         = \"(\"\n"
				+ ". . . 01 - - => [   16] = $Content[]      :<NoType>         = \"list 1 2 \"\n"
				+ ". . . 02 - - => [   33] = #Sub            :<NoType>         = \"(concat \\\"fo\\\" \\\"o\\\")\"\n"
				+ ". . . . 00 - => [   33] = <NoName>        :lisp             = \"(concat \\\"fo\\\" \\\"o\\\")\"\n"
				+ ". . . . . 00 => [   17] = $Open           :<NoType>         = \"(\"\n"
				+ ". . . . . 01 => [   32] = $Content[]      :<NoType>         = \"concat \\\"fo\\\" \\\"o\\\"\"\n"
				+ ". . . . . 02 => [   33] = $Close          :<NoType>         = \")\"\n"
				+ ". . . 03 - - => [   34] = $Close          :<NoType>         = \")\"\n"
				+ ". 03 - - - - => [   35] = $Content[]      :<NoType>         = \" \"\n"
				+ ". 04 - - - - => [   51] = #Sub            :<NoType>         = \"(list 3 4 \\\"bar\\\")\"\n"
				+ ". . 00 - - - => [   51] = <NoName>        :lisp             = \"(list 3 4 \\\"bar\\\")\"\n"
				+ ". . . 00 - - => [   36] = $Open           :<NoType>         = \"(\"\n"
				+ ". . . 01 - - => [   50] = $Content[]      :<NoType>         = \"list 3 4 \\\"bar\\\"\"\n"
				+ ". . . 02 - - => [   51] = $Close          :<NoType>         = \")\"\n"
				+ ". 05 - - - - => [   52] = $Close          :<NoType>         = \")\"",
				parser.parse("(if x (list 1 2 (concat \"fo\" \"o\")) (list 3 4 \"bar\"))"));
	}
	
}