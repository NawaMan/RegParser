package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.Greediness.Minimum;
import static net.nawaman.regparser.Greediness.Possessive;
import static net.nawaman.regparser.PredefinedCharClasses.Java_Any;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Maximum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Minimum;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Possessive;
import static net.nawaman.regparser.RPEntry.newParserEntry;
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
		                newParserEntry(Java_Any, ZeroOrMore_Possessive),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierZeroOrMore_Maximum() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, ZeroOrMore_Maximum),
		                newParserEntry(new WordChecker("end")));
		validate(28, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierZeroOrMore_Minimum() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, ZeroOrMore_Minimum),
		                newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierPossessive() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 5, Possessive)),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierPossessive_longer() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 17, Possessive)),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierPossessive_evenLonger() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 21, Possessive)),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMaximum() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 5, Maximum)),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMaximum_longer() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 17, Maximum)),
		                newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMaximum_evenLonger() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 31, Maximum)),
		                newParserEntry(new WordChecker("end")));
		validate(28, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMinimum() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 5, Minimum)),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMinimum_longer() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 17, Minimum)),
		                newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMinimum_evenLonger() {
		var parser = newRegParser(
		                newParserEntry(Java_Any, new Quantifier(1, 31, Minimum)),
		                newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierZeroOrMore_Possessive_withAlternative() {
		var parser = newRegParser(
		                newParserEntry(
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
		                     ), Quantifier.ZeroOrMore_Possessive),
		                newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierZeroOrMore_Maximum_withAlternative() {
		var parser = newRegParser(
		                newParserEntry(
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
		                ), ZeroOrMore_Maximum),
		                newParserEntry(new WordChecker("end")));
		validate(28, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierZeroOrMore_Minimum_withAlternative() {
		var parser = newRegParser(
		                    newParserEntry(new CheckerAlternative(
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
		                    newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry() {
		var parser = newRegParser(
		                    newParserEntry(
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
		                    newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_longer() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierZeroOrMore_Possessive_withAlternative_andEntry_evenLonger() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMaximum_withAlternative_andEntry() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMaximum_withAlternative_andEntry_longer() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMaximum_withAlternative_andEntry_evenLonger() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(28, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMinimum_withAlternative_andEntry() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(null, parser.parse("1234567end123456789012345end9012"));
	}
	
	@Test
	public void testQuantifierMinimum_withAlternative_andEntry_longer() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
	@Test
	public void testQuantifierMinimum_withAlternative_andEntry_evenLonger() {
		var parser = newRegParser(
		                        newParserEntry(
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
		                        newParserEntry(new WordChecker("end")));
		validate(10, parser.parse("1234567end123456789012345end9012").endPosition());
	}
	
}
