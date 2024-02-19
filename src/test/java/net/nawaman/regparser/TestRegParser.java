package net.nawaman.regparser;

import static net.nawaman.regparser.Quantifier.One;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.Zero;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.WordChecker;

public class TestRegParser {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testQuantifierZero() {
        var parser = newRegParser()
                .entry(new WordChecker("One"), Zero)
                .entry(new WordChecker("Two"))
                .build();
        validate(null, parser.parse("OneTwo"));
        validate(   3, parser.parse("TwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierOne() {
        var parser = newRegParser()
                .entry(new WordChecker("One"))
                .entry(new WordChecker("Two"))
                .build();
        validate(   6, parser.parse("OneTwo").endPosition());
        validate(null, parser.parse("TwoOne"));
    }
    
    @Test
    public void testQuantifierZeroOrOne() {
        var parser = newRegParser()
                .entry(new WordChecker("One"), ZeroOrOne)
                .entry(new WordChecker("Two"))
                .build();
        validate(6, parser.parse("OneTwo").endPosition());
        validate(3, parser.parse("TwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierZeroOrOne_withPrefix() {
        var parser = newRegParser()
                .entry(new WordChecker("One"))
                .entry(new WordChecker("Two"), ZeroOrOne)
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(   6, parser.parse("OneOne").endPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore() {
        var parser = newRegParser()
                .entry(new WordChecker("One"))
                .entry(new WordChecker("Two").oneOrMore())
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(  12, parser.parse("OneTwoTwoOne").endPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore() {
        var parser = newRegParser()
                .entry(new WordChecker("One"))
                .entry(new WordChecker("Two").zeroOrMore())
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("OneTwo"));
        validate(null, parser.parse("TwoOne"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("OneTwoOne").endPosition());
        validate(  12, parser.parse("OneTwoTwoOne").endPosition());
        validate(  15, parser.parse("OneTwoTwoTwoOne").endPosition());
    }
    
    @Test
    public void testQuantifierOneOrMore_withRange() {
        var parser = newRegParser()
                .entry(new WordChecker("One"))
                .entry(new CharRange('0', '9').oneOrMore())
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(   9, parser.parse("One123One").endPosition());
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Test
    public void testQuantifierMixed_One_andZeroOrMore() {
        var parser
                = newRegParser()
                .entry(new WordChecker("One"), One)
                .entry(new CharRange('0', '9'), ZeroOrMore)
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("One123One").endPosition());
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Test
    public void testQuantifierRange() {
        var parser = newRegParser()
                .entry(new WordChecker("One"), One)
                .entry(new CharRange('0', '9'), new Quantifier(0, 5))
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(   6, parser.parse("OneOne").endPosition());
        validate(   9, parser.parse("One123One").endPosition());
        validate(null, parser.parse("One123456One"));
        validate(null, parser.parse("One123456789One"));
    }
    
    @Test
    public void testQuantifierRange_open() {
        var parser = newRegParser()
                .entry(new WordChecker("One"), One)
                .entry(new CharRange('0', '9'), new Quantifier(5, -1))
                .entry(new WordChecker("One"))
                .build();
        validate(null, parser.parse("One123"));
        validate(null, parser.parse("123One"));
        validate(null, parser.parse("OneOne"));
        validate(null, parser.parse("One123One"));
        validate(  12, parser.parse("One123456One").endPosition());
        validate(  15, parser.parse("One123456789One").endPosition());
    }
    
    @Test
    public void testQuantifierPossessive() {
        var parser = newRegParser()
                .entry(new WordChecker("Col"))
                .entry(
                    either(new WordChecker("o"))
                    .or   (newRegParser(new WordChecker("ou"))))
                .build();
        
        validate(4, parser.parse("Color").endPosition());
        validate(5, parser.parse("Colour").endPosition());
    }
    
    @Test
    public void testAlternative() {
        var parser = newRegParser()
                .entry(new WordChecker("Col"))
                .entry(either(new WordChecker("o")).or(new WordChecker("ou")))
                .entry(new WordChecker("ur"))
                .build();
        
        validate(null, parser.parse("Colour"));
        validate(   7, parser.parse("Colouur").endPosition());
    }
    
    @Test
    public void testAlternative_withQualifer() {
        var parser = newRegParser()
                .entry(new WordChecker("Col"))
                .entry(either(new WordChecker("o")).or(new WordChecker("ou")), OneOrMore)
                .entry(new WordChecker("r"))
                .build();
        
        validate(null, parser.parse("Colur"));
        validate(   6, parser.parse("Colour").endPosition());
        validate(   7, parser.parse("Coloour").endPosition());
        validate(   8, parser.parse("Colouour").endPosition());
        validate(  10, parser.parse("Colooouour").endPosition());
        validate(  12, parser.parse("Colouoouoour").endPosition());
    }

}
