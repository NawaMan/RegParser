package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.TimeRecordRule;

public class TestCheckerFixeds {
	
	@ClassRule
	public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
	
	@Test
	public void testCheckerFixeds() {
		var typeProvider = new ParserTypeProvider.Extensible();
		typeProvider.addType("Number", compile("[0-9]*"));
		typeProvider.addType("TestCG",
		                new CheckerFixeds(
		                new CheckerFixeds.Entry(1),
		                new CheckerFixeds.Entry(4),
		                new CheckerFixeds.Entry("G1", 5, typeProvider.type("Number").typeRef()),
		                new CheckerFixeds.Entry()));
		var result = typeProvider.type("TestCG").parse("0123456789ABCDEFG");
		validate("\n"
		       + "00 - => [   17] = <NoName>        :TestCG           = \"0123456789ABCDEFG\"\n"
		       + ". 00 => [    5] = <NoName>        :<NoType>         = \"01234\"\n"
		       + ". 01 => [   10] = G1              :Number           = \"56789\"\n"
		       + ". 02 => [   17] = <NoName>        :<NoType>         = \"ABCDEFG\"", result);
	}
	
}
