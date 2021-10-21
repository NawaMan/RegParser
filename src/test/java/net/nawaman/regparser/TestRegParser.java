package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

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
        validate(null, parser.parse("OneTwo"));
        validate(   3, parser.parse("TwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOne() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"),
                        new WordChecker("Two"));
        validate(   6, parser.parse("OneTwo").getEndPosition());
        validate(null, parser.parse("TwoOne"));
    }
    
    @Test
    public void testQuantifierZeroOrOne() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), Quantifier.ZeroOrOne,
                        new WordChecker("Two"));
        validate(6, parser.parse("OneTwo").getEndPosition());
        validate(3, parser.parse("TwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrOne_withPrefix() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.ZeroOrOne,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   9, parser.parse("OneTwoOne").getEndPosition());
        validate(   6, parser.parse("OneOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.OneOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("OneTwoOne").getEndPosition());
        validate(  12, parser.parse("OneTwoTwoOne").getEndPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), Quantifier.ZeroOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   6, parser.parse("OneOne").getEndPosition());
        validate(   9, parser.parse("OneTwoOne").getEndPosition());
        validate(  12, parser.parse("OneTwoTwoOne").getEndPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").getEndPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore_withRange() {
        var parser = RegParser.newRegParser(
                        new WordChecker("One"), 
                        new CharRange('0', '9'), Quantifier.OneOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("One123One").getEndPosition());
        validate(  12, parser.parse("One123456One").getEndPosition());
        validate(  15, parser.parse("One123456789One").getEndPosition());
    }
    
    @Test
    public void testQuantifierMixed_One_andZeroOrMore() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), Quantifier.ZeroOrMore),
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").getEndPosition());
        validate(   9, parser.parse("One123One").getEndPosition());
        validate(  12, parser.parse("One123456One").getEndPosition());
        validate(  15, parser.parse("One123456789One").getEndPosition());
    }
    
    @Test
    public void testQuantifierRange() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(0, 5)),
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").getEndPosition());
        validate(   9, parser.parse("One123One").getEndPosition());
        validate(null, parser.parse("One123456One"));
        validate(null, parser.parse("One123456789One"));
    }
    
    @Test
    public void testQuantifierRange_open() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("One"), Quantifier.One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(5, -1)), 
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(null, parser.parse("One123One"));
        validate(  12, parser.parse("One123456One").getEndPosition());
        validate(  15, parser.parse("One123456789One").getEndPosition());
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
        
        validate(5, parser.parse("Color").getEndPosition());
        validate(6, parser.parse("Colour").getEndPosition());
    }
    
    @Test
    public void testAlternative() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou"))),
                        RPEntry._new(new WordChecker("ur")));
        
        validate(null, parser.parse("Colour"));
        validate(   7, parser.parse("Colouur").getEndPosition());
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
        
        validate(null, parser.parse("Colur"));
        validate(   6, parser.parse("Colour").getEndPosition());
        validate(   7, parser.parse("Coloour").getEndPosition());
        validate(   8, parser.parse("Colouour").getEndPosition());
        validate(  10, parser.parse("Colooouour").getEndPosition());
        validate(  12, parser.parse("Colouoouoour").getEndPosition());
    }
    
}
