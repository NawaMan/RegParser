package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.Greediness.Minimum;
import static net.nawaman.regparser.Greediness.Possessive;
import static net.nawaman.regparser.PredefinedCharClasses.Java_Any;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Maximum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Minimum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Possessive;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;

public class TestGreediness {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testQuantifierZeroOrMore_Possessive() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, ZeroOrMore_Possessive),
                        RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Maximum() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, ZeroOrMore_Maximum),
                        RPEntry._new(new WordChecker("end")));
        validate(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Minimum() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, ZeroOrMore_Minimum),
                        RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierPossessive() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, new Quantifier(1, 5, Possessive)),
                        RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierPossessive_longer() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, new Quantifier(1, 17, Possessive)),
                        RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierPossessive_evenLonger() {
        var parser = newRegParser(
                        RPEntry._new(Java_Any, new Quantifier(1, 21, Possessive)),
                        RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 5, Maximum)),
                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_longer() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 17, Maximum)),
                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMaximum_evenLonger() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 31, Maximum)),
                RPEntry._new(new WordChecker("end")));
        validate(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 5, Minimum)),
                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMinimum_longer() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 17, Minimum)),
                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_evenLonger() {
        var parser = newRegParser(
                RPEntry._new(Java_Any, new Quantifier(1, 31, Minimum)),
                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative() {
        var parser = newRegParser(
                                RPEntry._new(new CheckerAlternative(
                                                new WordChecker("1"), 
                                                new WordChecker("2"), 
                                                new WordChecker("3"),
                                                new WordChecker("4"), 
                                                new WordChecker("5"), 
                                                new WordChecker("6"), 
                                                new WordChecker("7"),
                                                new WordChecker("8"), 
                                                new WordChecker("9"), 
                                                new WordChecker("0"), 
                                                new WordChecker("e"),
                                                new WordChecker("n"), 
                                                new WordChecker("d")
                                            ), Quantifier.ZeroOrMore_Possessive),
                                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Maximum_withAlternative() {
        var parser = newRegParser(
                            RPEntry._new(new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), ZeroOrMore_Maximum),
                            RPEntry._new(new WordChecker("end")));
        validate(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Minimum_withAlternative() {
        var parser = newRegParser(
                            RPEntry._new(new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), ZeroOrMore_Minimum),
                            RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry() {
        var parser = newRegParser(
                            RPEntry._new(
                                new CheckerAlternative(
                                    new WordChecker("1"), 
                                    new WordChecker("2"), 
                                    new WordChecker("3"),
                                    new WordChecker("4"), 
                                    new WordChecker("5"), 
                                    new WordChecker("6"), 
                                    new WordChecker("7"),
                                    new WordChecker("8"), 
                                    new WordChecker("9"), 
                                    new WordChecker("0"), 
                                    new WordChecker("e"),
                                    new WordChecker("n"), 
                                    new WordChecker("d")
                                ),
                                new Quantifier(1, 5, Possessive)), 
                            RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_longer() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ),
                                        new Quantifier(1, 17, Possessive)
                                    ), 
                                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_evenLonger() {
        var parser = newRegParser(
                                RPEntry._new(
                                    new CheckerAlternative(
                                        new WordChecker("1"), 
                                        new WordChecker("2"), 
                                        new WordChecker("3"),
                                        new WordChecker("4"), 
                                        new WordChecker("5"), 
                                        new WordChecker("6"), 
                                        new WordChecker("7"),
                                        new WordChecker("8"), 
                                        new WordChecker("9"), 
                                        new WordChecker("0"), 
                                        new WordChecker("e"),
                                        new WordChecker("n"), 
                                        new WordChecker("d")
                                    ),
                                    new Quantifier(1, 21, Possessive)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), 
                                        new Quantifier(1, 5, Maximum)),
                                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry_longer() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), new Quantifier(1, 17, Maximum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry_evenLonger() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), new Quantifier(1, 31, Maximum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), new Quantifier(1, 5, Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry_longer() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), new Quantifier(1, 17, Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry_evenLonger() {
        var parser = newRegParser(
                                RPEntry._new(
                                        new CheckerAlternative(
                                            new WordChecker("1"), 
                                            new WordChecker("2"), 
                                            new WordChecker("3"),
                                            new WordChecker("4"), 
                                            new WordChecker("5"), 
                                            new WordChecker("6"), 
                                            new WordChecker("7"),
                                            new WordChecker("8"), 
                                            new WordChecker("9"), 
                                            new WordChecker("0"), 
                                            new WordChecker("e"),
                                            new WordChecker("n"), 
                                            new WordChecker("d")
                                        ), new Quantifier(1, 31, Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        validate(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
}
