package net.nawaman.regparser;

import static java.util.stream.Collectors.joining;
import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.types.SimpleParserType;

public class TestPattern {
    
    @Test
    public void testExact() {
        var parser = compileRegParser("Shape");
        
        validate("Shape",
                parser);
        
        validate("\n"
                + "00 => [    5] = <NoName>        :<NoType>         = \"Shape\"",
                parser.parse("Shape"));
        
        validate(null, parser.parse("Sharp"));
    }
    
    @Test
    public void testExact_backtick() {
        var parser = compileRegParser("`shape and shade`");
        
        validate("shape\\ and\\ shade",
                parser);
        
        validate("\n"
                + "00 => [   15] = <NoName>        :<NoType>         = \"shape and shade\"",
                parser.parse("shape and shade"));
        
        validate(null, parser.parse("Sharp and Shade"));
    }
    
    @Test
    public void testExact_backtick_caseSensitive() {
        var parser = compileRegParser("$`shape and shade`");
        
        validate("shape\\ and\\ shade",
                parser);
        
        validate("\n"
                + "00 => [   15] = <NoName>        :<NoType>         = \"shape and shade\"",
                parser.parse("shape and shade"));
        
        validate(null, parser.parse("Sharp and Shade"));
    }
    
    @Test
    public void testExact_backtick_caseInsensitive() {
        var parser = compileRegParser("#`shape and shade`");
        
        validate("(!textCI(\"shape and shade\")!)\n"
                + "  - (!textCI(\"shape and shade\")!)",
                parser);
        
        validate("\n"
                + "00 => [   15] = <NoName>        :textCI           = \"shape and shade\"",
                parser.parse("shape and shade"));
        
        validate("\n"
                + "00 => [   15] = <NoName>        :textCI           = \"Sharp and Shade\"",
                parser.parse("Sharp and Shade"));
    }
    
    @Test
    public void testTextCI() {// "!textCI(`Te\\\"st`)!"
        var parser = compileRegParser("!textCI(`shape`)!");
        
        validate("(!textCI(\"shape\")!)",
                parser);
        
        validate("\n"
                + "00 => [    5] = <NoName>        :textCI           = \"Shape\"",
                parser.parse("Shape"));
        
        validate("\n"
                + "00 => [    5] = <NoName>        :textCI           = \"shape\"",
                parser.parse("shape"));
        
        validate("\n"
                + "00 => [    5] = <NoName>        :textCI           = \"SHAPE\"",
                parser.parse("SHAPE"));
    }
    
    @Test
    public void testTextCI_escape() {
        var parser = compileRegParser("!textCI(`this is a \"test\".`)!");
        
        validate("(!textCI(\"this is a \\\"test\\\".\")!)",
                parser);
        
        validate("\n"
                + "00 => [   17] = <NoName>        :textCI           = \"This is a \\\"test\\\".\"",
                parser.parse("This is a \"test\"."));
    }
    
    @Test
    public void testOptional() {
        var parser = compileRegParser("Colou?r");
        
        validate("Colo\n"
                + "u?\n"
                + "r",
                parser);
        
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
        
        validate("Ho\n"
                + "Ho\n"
                + "Ho\n"
                + "Ho",
                parser);
        
        
        validate(null, parser.parse("Ho Ho Ho Ho"));
        
        validate("\n"
                + "00 => [    8] = <NoName>        :<NoType>         = \"HoHoHoHo\"",
                parser.parse("HoHoHoHo"));
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
        
        
        validate("\n"
                + "00 => [   11] = <NoName>        :<NoType>         = \"Ho Ho\\tHo\\nHo\"",
                parser.parse("Ho Ho\tHo\nHo"));
    }
    
    @Test
    public void testParseVsMatch() {
        var parser = compileRegParser("Shape");
        
        validate("Shape",
                parser);
        
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
        
        validate("Roa\n"
                + "r*",
                parser);
        
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
        
        validate("Roa\n"
                + "r+",
                parser);
        
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"Roar\"",
                parser.parse("Roar"));
        validate("\n"
                + "00 => [   11] = <NoName>        :<NoType>         = \"Roarrrrrrrr\"",
                parser.parse("Roarrrrrrrr"));
        
        validate(null, parser.parse("Road"));
    }
    
    @Test
    public void testZero() {
        var parser = compileRegParser("Ro^ar");
        
        validate("R\n"
                + "o{0}\n"
                + "ar",
                parser);
        
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"Rar\"",
                parser.parse("Rar"));
        
        validate(null, parser.parse("Roar"));
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
        
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"A2Z\"",
                parser.parse("A2Z"));
        
        validate(null, parser.parse("A-to-Z"));
    }
    
    @Test
    public void testCharClass() {
        var parser = compileRegParser("A[0-9]+Z");
        
        validate("A\n"
                + "[0-9]+\n"
                + "Z",
                parser);
        
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
        
        validate("A\n"
                + "[^[0-9]]++\n"
                + "Z",
                parser);
        
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
        
        validate("(^(true|false))",
                parser);
        
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
    
    @Test
    public void testAlternatives_length() {
        var parser1 = compileRegParser("(AA|AAA|AAAA)");
        
        validate("(AA|AAA|AAAA)",
                parser1);
        
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"AA\"",
                parser1.match("AA"));
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"AAA\"",
                parser1.match("AAA"));
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"AAAA\"",
                parser1.match("AAAA"));
        
        // Alternative will try to match longest ... the order of the choice make no different.
        
        var parser2 = compileRegParser("(AAAA|AAA|AA)");
        
        validate("(AAAA|AAA|AA)",
                parser2);
        
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"AA\"",
                parser2.match("AA"));
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"AAA\"",
                parser2.match("AAA"));
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"AAAA\"",
                parser2.match("AAAA"));
    }
    
    @Test
    public void testAlternatives_default() {
        var parser1 = compileRegParser("(AA|AAA||AAAA)");
        
        validate("(AA|AAA||AAAA)",
                parser1);
        
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"AA\"",
                parser1.match("AA"));
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"AAA\"",
                parser1.match("AAA"));
        
        // Why?
        // AA and AAA can match ... so they do and the tail is left which result in an unmatched text.
        validate(null, parser1.match("AAAA"));
        
        // So to confirm, try with `parse` and confirm that only `AAA` match (so with tail `A`).
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"AAA\"",
                parser1.parse("AAAA"));
        
        var parser2 = compileRegParser("(AAAA|AAA||AA)");
        
        validate("(AAAA|AAA||AA)",
                parser2);
        
        // Why?
        // AAAA and AAA cannot match, the parser try AA and found a match.
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"AA\"",
                parser2.match("AA"));
        
        validate("\n"
                + "00 => [    3] = <NoName>        :<NoType>         = \"AAA\"",
                parser2.match("AAA"));
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"AAAA\"",
                parser2.match("AAAA"));
    }
    
    @Test
    public void testComment_slashStar() {
        // We comment out the `not `.
        var parser = compileRegParser("Orange[: :]is[: :]/*not[: :]*/a[: :]color.");
        
        validate("Orange\n"
                + "[\\ ]\n"
                + "is\n"
                + "[\\ ]\n"
                + "a\n"
                + "[\\ ]\n"
                + "color\n"
                + ".",
                parser);
        
        validate("\n"
                + "00 => [   18] = <NoName>        :<NoType>         = \"Orange is a color.\"",
                parser.parse("Orange is a color."));
        
        validate(null, parser.parse("Orange is not a color."));
    }
    
    @Test
    public void testComment_backetStar() {
        var parser = compileRegParser("Orange[: :]is[: :](*not[: :]*)a[: :]color.");
        
        validate("Orange\n"
                + "[\\ ]\n"
                + "is\n"
                + "[\\ ]\n"
                + "a\n"
                + "[\\ ]\n"
                + "color\n"
                + ".",
                parser);
        
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
        
        validate("Orange\n"
                + "[\\ ]\n"
                + "is\n"
                + "[\\ ]\n"
                + "a\n"
                + "[\\ ]\n"
                + "color\n"
                + ".",
                parser);
        
        validate("\n"
                + "00 => [   18] = <NoName>        :<NoType>         = \"Orange is a color.\"",
                parser.parse("Orange is a color."));
        
        validate(null, parser.parse("Orange is not a color."));
    }
    
    @Test
    public void testPossessive() {
        var parser = compileRegParser("A.*Z");
        
        validate("A\n"
                + ".*\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate(null, result);
    }
    
    @Test
    public void testMinimum() {
        var parser = compileRegParser("A.**Z");
        
        validate("A\n"
                + ".**\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate("\n"
                + "00 => [    5] = <NoName>        :<NoType>         = \"A123Z\"",
                result);
    }
    
    @Test
    public void testMaximum() {
        var parser = compileRegParser("A.*+Z");
        
        validate("A\n"
                + ".*+\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate("\n"
                + "00 => [    5] = <NoName>        :<NoType>         = \"A123Z\"",
                result);
    }

    @Test
    public void testPossessive_named() {
        var parser = compileRegParser("A($Middle:~.~)*Z");
        
        validate("A\n"
                + "($Middle:~.~)*\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate(null, result);
    }
    
    @Test
    public void testMinimum_named() {
        var parser = compileRegParser("A($Middle:~.~)**Z");
        
        validate("A\n"
                + "($Middle:~.~)**\n"
                + "Z",
                parser);
        
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
        
        validate("A\n"
                + "($Middle:~.~)*+\n"
                + "Z",
                parser);
        
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
    public void testMinimum_named_combine() {
        var parser = compileRegParser("A($Middle[]:~.~)**Z");
        
        validate("A\n"
                + "($Middle[]:~.~)**\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    4] = $Middle[]       :<NoType>         = \"123\"\n"
                + "02 => [    5] = <NoName>        :<NoType>         = \"Z\"",
                result);
    }
    
    @Test
    public void testMaximum_named_combine() {
        var parser = compileRegParser("A($Middle[]:~.~)*+Z");
        
        validate("A\n"
                + "($Middle[]:~.~)*+\n"
                + "Z",
                parser);
        
        var result = parser.parse("A123Z");
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    4] = $Middle[]       :<NoType>         = \"123\"\n"
                + "02 => [    5] = <NoName>        :<NoType>         = \"Z\"",
                result);
    }
    
    @Test
    public void testNamedGroupFlat() {
        // Non-flatten --- as a reference
        validate("\n"
                + "00 - => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 - => [    4] = #Middle         :<NoType>         = \"123\"\n"
                + ". 00 => [    2] = #Sub            :<NoType>         = \"1\"\n"
                + ". 01 => [    3] = #Sub            :<NoType>         = \"2\"\n"
                + ". 02 => [    4] = #Sub            :<NoType>         = \"3\"\n"
                + "02 - => [    5] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle:~(#Sub:~[0-9]~)*~)Z")
                .parse("A123Z"));
        
        // Flatten
        // That is the outer group (Middle in this case) is replaced with its sub entry.
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    2] = #Sub            :<NoType>         = \"1\"\n"
                + "02 => [    3] = #Sub            :<NoType>         = \"2\"\n"
                + "03 => [    4] = #Sub            :<NoType>         = \"3\"\n"
                + "04 => [    5] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle*:~(#Sub:~[0-9]~)*~)Z")
                .parse("A123Z"));
        
        // Flatten
        // That is the outer group (Middle in this case) is replaced with its sub entry.
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    2] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle*:~(#Sub:~[0-9]~)*~)Z")
                .parse("AZ"));
    }
    
    @Test
    public void testNamedGroupFlat_whenOne() {
        // Non-flatten --- as a reference
        validate("\n"
                + "00 - => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 - => [    4] = #Middle         :<NoType>         = \"123\"\n"
                + ". 00 => [    2] = #Sub            :<NoType>         = \"1\"\n"
                + ". 01 => [    3] = #Sub            :<NoType>         = \"2\"\n"
                + ". 02 => [    4] = #Sub            :<NoType>         = \"3\"\n"
                + "02 - => [    5] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle:~(#Sub:~[0-9]~)*~)Z")
                .parse("A123Z"));
        
        // Flatten - as there is only sub entry
        // That is the outer group (Middle in this case) is removed  with its sub entry if it has only one entry.
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    2] = #Sub            :<NoType>         = \"1\"\n"
                + "02 => [    3] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle+:~(#Sub:~[0-9]~)*~)Z")
                .parse("A1Z"));
        
        // Not-Flatten as there are more than one sub elements.
        validate("\n"
                + "00 - => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 - => [    4] = #Middle+        :<NoType>         = \"123\"\n"
                + ". 00 => [    2] = #Sub            :<NoType>         = \"1\"\n"
                + ". 01 => [    3] = #Sub            :<NoType>         = \"2\"\n"
                + ". 02 => [    4] = #Sub            :<NoType>         = \"3\"\n"
                + "02 - => [    5] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle+:~(#Sub:~[0-9]~)*~)Z")
                .parse("A123Z"));
        
        // No sub e.
        validate("\n"
                + "00 => [    1] = <NoName>        :<NoType>         = \"A\"\n"
                + "01 => [    2] = <NoName>        :<NoType>         = \"Z\"",
                compileRegParser("A(#Middle+:~(#Sub:~[0-9]~)*~)Z")
                .parse("AZ"));
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
        
        validate("A\n"
                + "(#Digit:~1(#Two:~2(#Two:~3~)4~)5~)\n"
                + "  - 1\n"
                + "  - (#Two:~2(#Two:~3~)4~)\n"
                + "  -   - 2\n"
                + "  -   - (#Two:~3~)\n"
                + "  -   - 4\n"
                + "  - 5\n"
                + "Z",
                parser);
        
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
        
        validate("A\n"
                + "(!int!)\n"
                + "Z",
                parser);
        
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
        var lisp = new SimpleParserType("lisp", compile(
                  "($Open:~[:(:]~)"
                + "(($Content[]:~[^\\(]~)|(#Sub:~!lisp!~))**"
                + "($Close:~[:):]~)"));
        var typeProvider = new ParserTypeProvider.Simple(lisp);
        var parser       = compileRegParser(typeProvider, "!lisp!");
        
        validate("(!lisp!)",
                parser);
        
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
    
    @Test
    public void testBackRef() {
        var typeProvider = new ParserTypeProvider.Extensible();
        typeProvider.addType(ParserTypeBackRef.BackRef_Instance);
        
        // Case sensitive
        validate("\n"
                + "00 => [    1] = $X              :<NoType>         = \"a\"\n"
                + "01 => [    2] = <NoName>        :<NoType>         = \"x\"\n"
                + "02 => [    3] = <NoName>        :$BackRef?        = \"a\"",
                compileRegParser(typeProvider, "($X:~.~)[x]($X;)")
                .parse("axa"));
        
        validate("($X:~.~)\n"
                + "[x]\n"
                + "(!$BackRef?(\"$X\")!)",
                compileRegParser(typeProvider, "($X:~.~)[x]($X;)")
                .entries()
                .map(String::valueOf).collect(joining("\n")));
        
        // Case insensitive
        validate("\n"
                + "00 => [    1] = $X              :<NoType>         = \"a\"\n"
                + "01 => [    2] = <NoName>        :<NoType>         = \"x\"\n"
                + "02 => [    3] = <NoName>        :$BackRefCI?      = \"A\"",
                compileRegParser(typeProvider, "($X:~.~)[x]($X';)")
                .parse("axA"));
        
        validate("($X:~.~)\n"
                + "[x]\n"
                + "(!$BackRefCI?(\"$X\")!)",
                compileRegParser(typeProvider, "($X:~.~)[x]($X';)")
                .entries()
                .map(String::valueOf).collect(joining("\n")));
        
    }
    
    // TestJavaChecker for JavaChecker
    
    // TestTypeWithValidator for type with validator
    
    @Test
    public void testTypeWithCompiler() {
        @SuppressWarnings("serial")
        var int0To99 = new ParserType() {
            @Override
            public String name() {
                return "$int0to99?";
            }
            
            private final Checker checker = newRegParser(Digit.bound(2, 2, Maximum));
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }

            @Override
            protected Object doCompile(
                                ParseResult        thisResult,
                                int                entryIndex,
                                String             parameter,
                                CompilationContext compilationContext,
                                ParserTypeProvider typeProvider) {
                var entryText = thisResult.textOf(entryIndex);
                return Integer.parseInt(entryText);
            }
            
            @Override
            public final Boolean isDeterministic() {
                return false;
            }
            
        };
        
        var typeProvider = new ParserTypeProvider.Simple(int0To99);
        var parser       = compileRegParser(typeProvider, "(#int:!$int0to99?!)+");
        
        validate("(#int:!$int0to99?!)+",
                parser);
        
        var result       = parser.parse("3895482565");
        validate("\n"
                + "00 => [    2] = #int            :$int0to99?       = \"38\"\n"
                + "01 => [    4] = #int            :$int0to99?       = \"95\"\n"
                + "02 => [    6] = #int            :$int0to99?       = \"48\"\n"
                + "03 => [    8] = #int            :$int0to99?       = \"25\"\n"
                + "04 => [   10] = #int            :$int0to99?       = \"65\"",
                result);
        
        var context = new CompilationContext.Simple();
        var values  = result.valuesOf("#int", typeProvider, context);
        assertEquals(38, values[0]);
        assertEquals(95, values[1]);
        assertEquals(48, values[2]);
        assertEquals(25, values[3]);
        assertEquals(65, values[4]);
        assertEquals(Integer.class, values[0].getClass());
        assertEquals(Integer.class, values[1].getClass());
        assertEquals(Integer.class, values[2].getClass());
        assertEquals(Integer.class, values[3].getClass());
        assertEquals(Integer.class, values[4].getClass());
    }
    
}
