package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

public class TestRegParser {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testQuantifierZero() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), Quantifier.Zero,
                        new WordChecker("Two"));
        assertThat(null, parser.parse("OneTwo"));
        assertThat(   3, parser.parse("TwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOne() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"),
                        new WordChecker("Two"));
        assertThat(   6, parser.parse("OneTwo").getEndPosition());
        assertThat(null, parser.parse("TwoOne"));
    }
    
    @Test
    public void testQuantifierZeroOrOne() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), Quantifier.ZeroOrOne,
                        new WordChecker("Two"));
        assertThat(6, parser.parse("OneTwo").getEndPosition());
        assertThat(3, parser.parse("TwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrOne_withPrefix() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.ZeroOrOne,
                        new WordChecker("One"));
        assertThat(null, parser.parse("OneTwo"));
        assertThat(null, parser.parse("TwoOne"));
        assertThat(   9, parser.parse("OneTwoOne").getEndPosition());
        assertThat(   6, parser.parse("OneOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.OneOrMore,
                        new WordChecker("One"));
        assertThat(null, parser.parse("OneTwo"));
        assertThat(null, parser.parse("TwoOne"));
        assertThat(null, parser.parse("OneOne"));
        assertThat(   9, parser.parse("OneTwoOne").getEndPosition());
        assertThat(  12, parser.parse("OneTwoTwoOne").getEndPosition());
        assertThat(  15, parser.parse("OneTwoTwoTwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.ZeroOrMore,
                        new WordChecker("One"));
        assertThat(null, parser.parse("OneTwo"));
        assertThat(null, parser.parse("TwoOne"));
        assertThat(   6, parser.parse("OneOne").getEndPosition());
        assertThat(   9, parser.parse("OneTwoOne").getEndPosition());
        assertThat(  12, parser.parse("OneTwoTwoOne").getEndPosition());
        assertThat(  15, parser.parse("OneTwoTwoTwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore_withRange() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new CharRange('0', '9'), Quantifier.OneOrMore,
                        new WordChecker("One"));
        assertThat(null, parser.parse("One123"));
        assertThat(null, parser.parse("123One"));
        assertThat(null, parser.parse("OneOne"));
        assertThat(   9, parser.parse("One123One").getEndPosition());
        assertThat(  12, parser.parse("One123456One").getEndPosition());
        assertThat(  15, parser.parse("One123456789One").getEndPosition());
    }
    
    @Test
    public void testQuantifierMixed_One_andZeroOrMore() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), Quantifier.ZeroOrMore),
                        RPEntry._new(new WordChecker("One")));
        assertThat(null, parser.parse("One123"));
        assertThat(null, parser.parse("123One"));
        assertThat(   6, parser.parse("OneOne").getEndPosition());
        assertThat(   9, parser.parse("One123One").getEndPosition());
        assertThat(  12, parser.parse("One123456One").getEndPosition());
        assertThat(  15, parser.parse("One123456789One").getEndPosition());
    }
    
    @Test
    public void testQuantifierRange() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(0, 5)),
                        RPEntry._new(new WordChecker("One")));
        assertThat(null, parser.parse("One123"));
        assertThat(null, parser.parse("123One"));
        assertThat(   6, parser.parse("OneOne").getEndPosition());
        assertThat(   9, parser.parse("One123One").getEndPosition());
        assertThat(null, parser.parse("One123456One"));
        assertThat(null, parser.parse("One123456789One"));
    }
    
    @Test
    public void testQuantifierRange_open() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(5, -1)), 
                        RPEntry._new(new WordChecker("One")));
        assertThat(null, parser.parse("One123"));
        assertThat(null, parser.parse("123One"));
        assertThat(null, parser.parse("OneOne"));
        assertThat(null, parser.parse("One123One"));
        assertThat(  12, parser.parse("One123456One").getEndPosition());
        assertThat(  15, parser.parse("One123456789One").getEndPosition());
    }
    
    @Ignore("Does not seems to work.")
    @Test
    public void testQuantifierPossessive() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(
                                new CheckerAlternative(
                                        new WordChecker("o"),
                                        new WordChecker("ou")
                                ),
                                new Quantifier(2, Greediness.Possessive)
                        ), 
                        RPEntry._new(new WordChecker("r")));
        
        assertThat(5, parser.parse("Color").getEndPosition());
        assertThat(6, parser.parse("Colour").getEndPosition());
    }
    
    @Test
    public void testAlternative() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou"))),
                        RPEntry._new(new WordChecker("ur")));
        
        assertThat(null, parser.parse("Colour"));
        assertThat(   7, parser.parse("Colouur").getEndPosition());
    }
    
    @Test
    public void testAlternative_withQualifer() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(new CheckerAlternative(
                                            new WordChecker("o"), 
                                            new WordChecker("ou")), 
                                        Quantifier.OneOrMore),
                        RPEntry._new(new WordChecker("r")));
        
        assertThat(null, parser.parse("Colur"));
        assertThat(   6, parser.parse("Colour").getEndPosition());
        assertThat(   7, parser.parse("Coloour").getEndPosition());
        assertThat(   8, parser.parse("Colouour").getEndPosition());
        assertThat(  10, parser.parse("Colooouour").getEndPosition());
        assertThat(  12, parser.parse("Colouoouoour").getEndPosition());
    }
    
}
