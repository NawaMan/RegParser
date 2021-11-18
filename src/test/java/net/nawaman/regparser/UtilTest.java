package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.Util.indexOf;
import static net.nawaman.regparser.Util.lastIndexOf;

import org.junit.Test;

public class UtilTest {
	
	@Test
	public void testIndexOf() {
		validate("abcdefghi".indexOf("de", 0), indexOf("abcdefghi", "de", 0));
		validate("abcdefghi".indexOf("de", 4), indexOf("abcdefghi", "de", 4));
	}
	
	@Test
	public void testLastIndexOf() {
		validate("abcdefghi".lastIndexOf("de", 10), lastIndexOf("abcdefghi", "de", 10));
		validate("abcdefghi".lastIndexOf("de",  7), lastIndexOf("abcdefghi", "de",  7));
	}
	
}
