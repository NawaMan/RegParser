package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

public class TestTypeProviderExtensible {
	
	@Test
	public void test() {
		var typeProvider = new ParserTypeProvider.Extensible();
		typeProvider.addType("Number", "[0-9]+");
		typeProvider.addType("Plus", "!Number![:WhiteSpace:]*[:+:][:WhiteSpace:]*!Number!");
		
		var type   = typeProvider.type("Plus");
		var result = type.parse("5 + 10");
		validate("\n"
		       + "00 - => [    6] = <NoName>        :Plus             = \"5 + 10\"\n"
		       + ". 00 => [    1] = <NoName>        :Number           = \"5\"\n"
		       + ". 01 => [    4] = <NoName>        :<NoType>         = \" + \"\n"
		       + ". 02 => [    6] = <NoName>        :Number           = \"10\"", result);
	}
	
}
