package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

public class TestGreediness {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testQuantifierZeroOrMore_Possessive() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Possessive),
                        RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Maximum() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Maximum),
                        RPEntry._new(new WordChecker("end")));
        assertThat(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Minimum() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Minimum),
                        RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierPossessive() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Possessive)),
                        RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierPossessive_longer() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Possessive)),
                        RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierPossessive_evenLonger() {
        var parser = RegParser.newRegParser(
                        RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 21, Greediness.Possessive)),
                        RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_longer() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMaximum_evenLonger() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 31, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMinimum_longer() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_evenLonger() {
        var parser = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 31, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative() {
        var parser = RegParser.newRegParser(
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
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Maximum_withAlternative() {
        var parser = RegParser.newRegParser(
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
                                        ), Quantifier.ZeroOrMore_Maximum),
                            RPEntry._new(new WordChecker("end")));
        assertThat(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Minimum_withAlternative() {
        var parser = RegParser.newRegParser(
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
                                        ), Quantifier.ZeroOrMore_Minimum),
                            RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry() {
        var parser = RegParser.newRegParser(
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
                                new Quantifier(1, 5, Greediness.Possessive)), 
                            RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_longer() {
        var parser = RegParser.newRegParser(
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
                                        new Quantifier(1, 17, Greediness.Possessive)
                                    ), 
                                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_evenLonger() {
        var parser = RegParser.newRegParser(
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
                                    new Quantifier(1, 21, Greediness.Possessive)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry() {
        var parser = RegParser.newRegParser(
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
                                        new Quantifier(1, 5, Greediness.Maximum)),
                                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry_longer() {
        var parser = RegParser.newRegParser(
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
                                        ), new Quantifier(1, 17, Greediness.Maximum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMaximum_withAlternative_andEntry_evenLonger() {
        var parser = RegParser.newRegParser(
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
                                        ), new Quantifier(1, 31, Greediness.Maximum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(28, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry() {
        var parser = RegParser.newRegParser(
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
                                        ), new Quantifier(1, 5, Greediness.Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(null, parser.parse("1234567end123456789012345end9012"));
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry_longer() {
        var parser = RegParser.newRegParser(
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
                                        ), new Quantifier(1, 17, Greediness.Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
    
    @Test
    public void testQuantifierMinimum_withAlternative_andEntry_evenLonger() {
        var parser = RegParser.newRegParser(
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
                                        ), new Quantifier(1, 31, Greediness.Minimum)
                                ),
                                RPEntry._new(new WordChecker("end")));
        assertThat(10, parser.parse("1234567end123456789012345end9012").getEndPosition());
    }
}
