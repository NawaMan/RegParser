package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Maximum;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Maximum;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.result.ParseResult;


public class TestSelfContain {
	
	@ClassRule
	public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
	
	@SuppressWarnings("serial")
	private ParserType Int0To24 = new ParserType() {
		
		private final Checker checker
				= newRegParser()
				.entry(Digit, new Quantifier(1, 2, Maximum))
				.build();
		
		@Override
		public String name() {
			return "int0To24~";
		}
		
		@Override
		public Checker checker(ParseResult postResult, String param, ParserTypeProvider typeProvider) {
			return this.checker;
		}
		
		@Override
		public boolean doValidate(
						ParseResult   hostResult,
						ParseResult   thisResult,
						String        param,
						ParserTypeProvider typeProvider) {
			var text  = thisResult.text().toString();
			int value = Integer.parseInt(text);
			return (value >= 0) && (value <= 24);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return false;
		}
	};
	
	@SuppressWarnings("serial")
	private ParserType Int25To50 = new ParserType() {
		
		private final Checker checker
				= newRegParser()
				.entry(Digit, new Quantifier(1, 2, Maximum))
				.build();
		
		@Override
		public String name() {
			return "$int25To50~";
		}
		
		@Override
		public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
			return this.checker;
		}
		
		@Override
		public boolean doValidate(
						ParseResult   hostResult,
						ParseResult   thisResult,
						String        param,
						ParserTypeProvider typeProvider) {
			var text  = thisResult.text().toString();
			int value = Integer.parseInt(text);
			return (value >= 25) && (value <= 50);
		}
		
		@Override
		public final Boolean isDeterministic() {
			return false;
		}
	};
	
	private final RegParser regParser 
	        = newRegParser()
	        .entry(
	            either(newRegParser("#ValueLow",  Int0To24))
	            .or   (newRegParser("#ValueHigh", Int25To50)),
	            ZeroOrMore_Maximum
	        )
	        .build();
	
	@Test
	public void testValidate() {
		validate("((#ValueLow:!int0To24~!)|(#ValueHigh:!$int25To50~!))*+", regParser);
		var PR = regParser.parse("02154054657654521125432135765354362344");
		validate("\n"
		        + "00 => [    2] = #ValueLow       :int0To24~        = \"02\"\n"
		        + "01 => [    4] = #ValueLow       :int0To24~        = \"15\"\n"
		        + "02 => [    6] = #ValueHigh      :$int25To50~      = \"40\"\n"
		        + "03 => [    7] = #ValueLow       :int0To24~        = \"5\"\n"
		        + "04 => [    9] = #ValueHigh      :$int25To50~      = \"46\"\n"
		        + "05 => [   10] = #ValueLow       :int0To24~        = \"5\"\n"
		        + "06 => [   11] = #ValueLow       :int0To24~        = \"7\"\n"
		        + "07 => [   12] = #ValueLow       :int0To24~        = \"6\"\n"
		        + "08 => [   13] = #ValueLow       :int0To24~        = \"5\"\n"
		        + "09 => [   15] = #ValueHigh      :$int25To50~      = \"45\"\n"
		        + "10 => [   17] = #ValueLow       :int0To24~        = \"21\"\n"
		        + "11 => [   19] = #ValueLow       :int0To24~        = \"12\"\n"
		        + "12 => [   20] = #ValueLow       :int0To24~        = \"5\"\n"
		        + "13 => [   22] = #ValueHigh      :$int25To50~      = \"43\"\n"
		        + "14 => [   24] = #ValueLow       :int0To24~        = \"21\"\n"
		        + "15 => [   26] = #ValueHigh      :$int25To50~      = \"35\"\n"
		        + "16 => [   27] = #ValueLow       :int0To24~        = \"7\"\n"
		        + "17 => [   28] = #ValueLow       :int0To24~        = \"6\"\n"
		        + "18 => [   29] = #ValueLow       :int0To24~        = \"5\"\n"
		        + "19 => [   31] = #ValueHigh      :$int25To50~      = \"35\"\n"
		        + "20 => [   33] = #ValueHigh      :$int25To50~      = \"43\"\n"
		        + "21 => [   34] = #ValueLow       :int0To24~        = \"6\"\n"
		        + "22 => [   36] = #ValueLow       :int0To24~        = \"23\"\n"
		        + "23 => [   38] = #ValueHigh      :$int25To50~      = \"44\"",
		        PR);
		
		validate("[02, 15, 5, 5, 7, 6, 5, 21, 12, 5, 21, 7, 6, 5, 6, 23]", PR.textsOf("#ValueLow"));
		validate("[40, 46, 45, 43, 35, 35, 43, 44]",                       PR.textsOf("#ValueHigh"));
	}

}
