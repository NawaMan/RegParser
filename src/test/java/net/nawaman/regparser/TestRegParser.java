package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Possessive;
import static net.nawaman.regparser.Quantifier.One;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.Zero;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;

public class TestRegParser {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testQuantifierZero() {
        var parser = newRegParser(
                        new WordChecker("One"), Zero,
                        new WordChecker("Two"));
        validate(null, parser.parse("OneTwo"));
        validate(   3, parser.parse("TwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierOne() {
        var parser = newRegParser(
                        new WordChecker("One"),
                        new WordChecker("Two"));
        validate(   6, parser.parse("OneTwo").endPosition());
        validate(null, parser.parse("TwoOne"));
    }
    
    @Test
    public void testQuantifierZeroOrOne() {
        var parser = newRegParser(
                        new WordChecker("One"), ZeroOrOne,
                        new WordChecker("Two"));
        validate(6, parser.parse("OneTwo").endPosition());
        validate(3, parser.parse("TwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierZeroOrOne_withPrefix() {
        var parser = newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), ZeroOrOne,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(   6, parser.parse("OneOne").endPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore() {
        var parser = newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), OneOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(  12, parser.parse("OneTwoTwoOne").endPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore() {
        var parser = newRegParser(
                        new WordChecker("One"), 
                        new WordChecker("Two"), ZeroOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(  12, parser.parse("OneTwoTwoOne").endPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore_withRange() {
        var parser = newRegParser(
                        new WordChecker("One"), 
                        new CharRange('0', '9'), OneOrMore,
                        new WordChecker("One"));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("One123One").endPosition());
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Test
    public void testQuantifierMixed_One_andZeroOrMore() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("One"), One),
                        RPEntry._new(new CharRange('0', '9'), ZeroOrMore),
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("One123One").endPosition());
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Test
    public void testQuantifierRange() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("One"), One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(0, 5)),
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("One123One").endPosition());
        validate(null, parser.parse("One123456One"));
        validate(null, parser.parse("One123456789One"));
    }
    
    @Test
    public void testQuantifierRange_open() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("One"), One),
                        RPEntry._new(new CharRange('0', '9'), new Quantifier(5, -1)), 
                        RPEntry._new(new WordChecker("One")));
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(null, parser.parse("One123One"));
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Ignore("Does not seems to work.")
    @Test
    public void testQuantifierPossessive() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(
                                new CheckerAlternative(
                                        new WordChecker("o"),
                                        new WordChecker("ou")
                                ),
                                new Quantifier(2, Possessive)
                        ), 
                        RPEntry._new(new WordChecker("r")));
        
        validate(5, parser.parse("Color").endPosition());
        validate(6, parser.parse("Colour").endPosition());
    }
    
    @Test
    public void testAlternative() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou"))),
                        RPEntry._new(new WordChecker("ur")));
        
        validate(null, parser.parse("Colour"));
        validate(   7, parser.parse("Colouur").endPosition());
    }
    
    @Test
    public void testAlternative_withQualifer() {
        var parser = newRegParser(
                        RPEntry._new(new WordChecker("Col")),
                        RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou")), OneOrMore),
                        RPEntry._new(new WordChecker("r")));
        
        validate(null, parser.parse("Colur"));
        validate(   6, parser.parse("Colour").endPosition());
        validate(   7, parser.parse("Coloour").endPosition());
        validate(   8, parser.parse("Colouour").endPosition());
        validate(  10, parser.parse("Colooouour").endPosition());
        validate(  12, parser.parse("Colouoouoour").endPosition());
    }
    
}
