package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import org.junit.Test;

import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.utils.Util;

public class TestTypeWithValidator {
	
	@Test
	public void testTypeWithValidation() {
		
		@SuppressWarnings("serial")
		var int0To4 = new ParserType() {
			@Override
			public String name() {
				return "$int(0-4)?";
			}
			
			Checker checker = newRegParser(Digit.bound(1, 1, Maximum));
			
			@Override
			public Checker checker(ParseResult hostResult, String param, ParserTypeProvider provider) {
				return this.checker;
			}
			
			@Override
			public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, ParserTypeProvider provider) {
				var text  = thisResult.text();
				int value = Integer.parseInt(text);
				return (value >= 0) && (value <= 4);
			}
			
			@Override
			public final Boolean isDeterministic() {
				return false;
			}
		};
		
		@SuppressWarnings("serial")
		var int5To9 = new ParserType() {
			@Override
			public String name() {
				return "$int(5-9)?";
			}
			
			private final Checker checker = newRegParser(Digit.bound(1, 1, Maximum));
			
			@Override
			public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
				return this.checker;
			}
			
			@Override
			public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, ParserTypeProvider provider) {
				var text  = thisResult.text();
				int value = Integer.parseInt(text);
				return (value >= 5) && (value <= 9);
			}
			
			@Override
			public final Boolean isDeterministic() {
				return false;
			}
		};
		var regParser = newRegParser(
							either(newRegParser("#Value_Low",  int0To4))
							.or   (newRegParser("#Value_High", int5To9))
							.build()
							.zeroOrMore().maximum());
		
		var result = regParser.parse("3895482565");
		validate("[3,4,2]",         Util.toString(result.textsOf("#Value_Low")));
		validate("[8,9,5,8,5,6,5]", Util.toString(result.textsOf("#Value_High")));
	}
	
}
