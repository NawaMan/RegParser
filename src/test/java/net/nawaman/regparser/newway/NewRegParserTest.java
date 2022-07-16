package net.nawaman.regparser.newway;

import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.RegParser;

public class NewRegParserTest {
	
	private ParserTypeProvider typeProvider = null;
	
	@Test
	public void testDefaultGreedinessBasic() {
		var parser = compileRegParser("a{1,4}b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				parse("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				parse("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				parse("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4",
				parse("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testDefaultGreedinessRelaxTail() {
		var parser = compileRegParser("a{1,4}[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: [ab]{,2}\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: [ab]{,2}\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		validate("RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				match("b", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aaaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaaab", parser, typeProvider));
	}
	
	@Test
	public void testDefaultGreedinessZero() {
		var parser = compileRegParser("a{0}b");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: b\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0",
				match("ab", parser, typeProvider));
	}
	
	@Test
	public void testDefaultGreedinessZeroRelaxTail() {
		var parser = compileRegParser("a{0}[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("aab", parser, typeProvider));
	}
	
	@Test
	public void testDefaultGreedinessSub() {
		var parser = compileRegParser("((a|z){1,4})b");
		
		validate("(a|z){1,4}\n"
				+ "  - (a|z){1,4}\n"
				+ "b",
				parser);
		
		// Pass
		validate("RPUnmatchedText: Expect but not found: \"(a|z){1,4}\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				match("b", parser, typeProvider));
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				match("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 3, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"(a|z){1,4}\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 3, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 4",
				match("aaaaab", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"(a|z){1,4}\"\n"
				+ "RPRootText [original=cdef]\n"
				+ "offset: 0",
				parse("cdef", parser, typeProvider));
	}
	
	@Test
	public void testDefaultGreedinessSub_zero() {
		var parser = compileRegParser("((a|z){1,4}){,2}b");
		
		validate("(a|z){1,4}{,2}\n"
				+ "  - (a|z){1,4}\n"
				+ "b",
				parser);
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: b\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 3, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		validate("RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 1, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 2, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 3, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 4, level: 1, checker: (a|z){1,4}\n"
				+ "offset: 5, level: 0, checker: b\n"
				+ "offset: 6",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessBasic() {
		var parser = compileRegParser("a{1,4}!b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				parse("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				parse("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: a{1,4}!\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				parse("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [4] but found [5]: \"a\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: a{1,4}!\n"
				+ "offset: 4, level: 0, checker: a{1,4}!\n"
				+ "offset: 5",
				parse("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessRelaxTail() {
		var parser = compileRegParser("a{1,4}![ab]{,2}");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: [ab]{,2}\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: [ab]{,2}\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: a{1,4}!\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [4] but found [5]: \"a\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}!\n"
				+ "offset: 1, level: 0, checker: a{1,4}!\n"
				+ "offset: 2, level: 0, checker: a{1,4}!\n"
				+ "offset: 3, level: 0, checker: a{1,4}!\n"
				+ "offset: 4, level: 0, checker: a{1,4}!\n"
				+ "offset: 5",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessZero() {
		var parser = compileRegParser("a{0}!b");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: b\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [0] but found [1]: \"a\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a^\n"
				+ "offset: 1",
				match("ab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessZeroRelaxTail() {
		var parser = compileRegParser("a{0}![ab]{,2}");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [0] but found [1]: \"a\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a^\n"
				+ "offset: 1",
				match("ab", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [0] but found [2]: \"a\"\n"
				+ "RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a^\n"
				+ "offset: 1, level: 0, checker: a^\n"
				+ "offset: 2",
				match("aab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessSub() {
		var parser = compileRegParser("((a|z){2,4}!)b");
		
		// Pass
		validate("RPUnmatchedText: Expect atleast [2] but only found [1]: \"(a|z)\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 2, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				match("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 2, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 3, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"(a|z){2,4}!\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect at most [4] but found [5]: \"(a|z)\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 2, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 3, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 4, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 5",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testObsessiveGreedinessSub_multiple() {
		var parser = compileRegParser("((a|z){2,4}!){,2}b");
		
		validate("RPUnmatchedText: Expect at most [4] but found [5]: \"(a|z)\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 1, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 2, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 3, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 4, level: 1, checker: (a|z){2,4}!\n"
				+ "offset: 5",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMinimumGreedinessBasic() {
		var parser = compileRegParser("a{1,4}*b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				parse("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				parse("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: a{1,4}*\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				parse("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: a{1,4}*\n"
				+ "offset: 4",
				parse("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMinimumGreedinessRelaxTail() {
		var parser = compileRegParser("a{1,4}*[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2, level: 0, checker: [ab]{,2}\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: [ab]{,2}\n"
				+ "offset: 3, level: 0, checker: [ab]{,2}\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: [ab]{,2}\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		validate("RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: a{1,4}*\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				match("b", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aaaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}*\n"
				+ "offset: 1, level: 0, checker: a{1,4}*\n"
				+ "offset: 2, level: 0, checker: a{1,4}*\n"
				+ "offset: 3, level: 0, checker: a{1,4}*\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMinimumGreedinessZero() {
		var parser = compileRegParser("a{0}*b");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: b\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0",
				match("ab", parser, typeProvider));
	}
	
	@Test
	public void testMinimumGreedinessZeroRelaxTail() {
		var parser = compileRegParser("a{0}*[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("aab", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMinimumGreedinessSub() {
		var parser = compileRegParser("((a|z){1,4}*)b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				match("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMaximumGreedinessBasic() {
		var parser = compileRegParser("a{1,4}+b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				parse("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				parse("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: a{1,4}+\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				parse("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: a{1,4}+\n"
				+ "offset: 4",
				parse("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMaximumGreedinessRelaxTail() {
		var parser = compileRegParser("a{1,4}+[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: [ab]{,2}\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: [ab]{,2}\n"
				+ "offset: 4",
				parse("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: a{1,4}+\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		validate("RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: a{1,4}+\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				match("b", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aaaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}+\n"
				+ "offset: 1, level: 0, checker: a{1,4}+\n"
				+ "offset: 2, level: 0, checker: a{1,4}+\n"
				+ "offset: 3, level: 0, checker: a{1,4}+\n"
				+ "offset: 4, level: 0, checker: [ab]{,2}\n"
				+ "offset: 5, level: 0, checker: [ab]{,2}\n"
				+ "offset: 6",
				match("aaaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMaximumGreedinessZero() {
		var parser = compileRegParser("a{0}+b");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: b\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=ab]\n"
				+ "offset: 0",
				match("ab", parser, typeProvider));
	}
	
	@Test
	public void testMaximumGreedinessZeroRelaxTail() {
		var parser = compileRegParser("a{0}+[ab]{,2}");
		
		// Pass
		validate("RPRootText [original=b]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1",
				match("b", parser, typeProvider));
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: [ab]{,2}\n"
				+ "offset: 1, level: 0, checker: [ab]{,2}\n"
				+ "offset: 2",
				match("aab", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMaximumGreedinessSub() {
		var parser = compileRegParser("((a|z){1,4}+)b");
		
		// Pass
		validate("RPRootText [original=ab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: b\n"
				+ "offset: 2",
				match("ab", parser, typeProvider));
		validate("RPRootText [original=aab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: b\n"
				+ "offset: 3",
				match("aab", parser, typeProvider));
		validate("RPRootText [original=aaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: b\n"
				+ "offset: 4",
				match("aaab", parser, typeProvider));
		validate("RPRootText [original=aaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4, level: 0, checker: b\n"
				+ "offset: 5",
				match("aaaab", parser, typeProvider));
		
		// Fail
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=b]\n"
				+ "offset: 0",
				parse("b", parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"b\"\n"
				+ "RPRootText [original=aaaaab]\n"
				+ "offset: 0, level: 0, checker: a{1,4}\n"
				+ "offset: 1, level: 0, checker: a{1,4}\n"
				+ "offset: 2, level: 0, checker: a{1,4}\n"
				+ "offset: 3, level: 0, checker: a{1,4}\n"
				+ "offset: 4",
				match("aaaaab", parser, typeProvider));
	}
	
	@Test
	public void testMinimumThenMaximum() {
		var parser = compileRegParser("A[:-:]{1,}*[:-:]{1,}+Z");
		
		validate("A\n"
				+ "[\\-]+*\n"
				+ "[\\-]++\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A--Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]+*\n"
				+ "offset: 2, level: 0, checker: [\\-]++\n"
				+ "offset: 3, level: 0, checker: Z\n"
				+ "offset: 4",
				parse("A--Z", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A---Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]+*\n"
				+ "offset: 2, level: 0, checker: [\\-]++\n"
				+ "offset: 3, level: 0, checker: [\\-]++\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A---Z", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A----Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]+*\n"
				+ "offset: 2, level: 0, checker: [\\-]++\n"
				+ "offset: 3, level: 0, checker: [\\-]++\n"
				+ "offset: 4, level: 0, checker: [\\-]++\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A----Z", parser, typeProvider));
		System.out.println();
	}
	
	@Test
	public void testMaximumThenMinimum() {
		var parser = compileRegParser("A[:-:]{1,}+[:-:]{1,}*Z");
		
		validate("A\n"
				+ "[\\-]++\n"
				+ "[\\-]+*\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A--Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]++\n"
				+ "offset: 2, level: 0, checker: [\\-]+*\n"
				+ "offset: 3, level: 0, checker: Z\n"
				+ "offset: 4",
				parse("A--Z", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A---Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]++\n"
				+ "offset: 2, level: 0, checker: [\\-]++\n"
				+ "offset: 3, level: 0, checker: [\\-]+*\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A---Z", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A----Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [\\-]++\n"
				+ "offset: 2, level: 0, checker: [\\-]++\n"
				+ "offset: 3, level: 0, checker: [\\-]++\n"
				+ "offset: 4, level: 0, checker: [\\-]+*\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A----Z", parser, typeProvider));
		System.out.println();
	}
	
	@Test
	public void testMaximumThenMaximum() {
		var parser = compileRegParser("A.++a++Z");
		
		validate("A\n"
				+ ".++\n"
				+ "a++\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A-aZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .++\n"
				+ "offset: 2, level: 0, checker: a++\n"
				+ "offset: 3, level: 0, checker: Z\n"
				+ "offset: 4",
				parse("A-aZ", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A--aaZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .++\n"
				+ "offset: 2, level: 0, checker: .++\n"
				+ "offset: 3, level: 0, checker: .++\n"
				+ "offset: 4, level: 0, checker: a++\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A--aaZ", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A---aaZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .++\n"
				+ "offset: 2, level: 0, checker: .++\n"
				+ "offset: 3, level: 0, checker: .++\n"
				+ "offset: 4, level: 0, checker: .++\n"
				+ "offset: 5, level: 0, checker: a++\n"
				+ "offset: 6, level: 0, checker: Z\n"
				+ "offset: 7",
				parse("A---aaZ", parser, typeProvider));
		System.out.println();
	}
	
	@Test
	public void testMinimumThenMinimum() {
		var parser = compileRegParser("A.+*a+*Z");
		
		validate("A\n"
				+ ".+*\n"
				+ "a+*\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A-aZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .+*\n"
				+ "offset: 2, level: 0, checker: a+*\n"
				+ "offset: 3, level: 0, checker: Z\n"
				+ "offset: 4",
				parse("A-aZ", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A--aaZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .+*\n"
				+ "offset: 2, level: 0, checker: .+*\n"
				+ "offset: 3, level: 0, checker: a+*\n"
				+ "offset: 4, level: 0, checker: a+*\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A--aaZ", parser, typeProvider));
		System.out.println();
		
		validate("RPRootText [original=A---aaZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .+*\n"
				+ "offset: 2, level: 0, checker: .+*\n"
				+ "offset: 3, level: 0, checker: .+*\n"
				+ "offset: 4, level: 0, checker: a+*\n"
				+ "offset: 5, level: 0, checker: a+*\n"
				+ "offset: 6, level: 0, checker: Z\n"
				+ "offset: 7",
				parse("A---aaZ", parser, typeProvider));
		System.out.println();
	}
	
	@Test
	public void testSimple() {
		var parser = compileRegParser("Colou?r");
		
		parse("Color", parser, typeProvider);
		System.out.println();
		
		parse("Colour", parser, typeProvider);
		System.out.println();
	}
	
	@Test
	public void testExact() {
		var parser = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, level: 0, checker: Shape\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
	}
	
	@Test
	public void testExact_notMatch() {
		var parser = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Shape\"\n"
				+ "RPRootText [original=Sharp]\n"
				+ "offset: 0",
				parse("Sharp", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick() {
		var parser = compileRegParser("`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, level: 0, checker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_notMatch() {
		var parser = compileRegParser("`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"shape\\ and\\ shade\"\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive() {
		var parser = compileRegParser("$`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, level: 0, checker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive_notMatch() {
		var parser = compileRegParser("$`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"shape\\ and\\ shade\"\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testExact_backtick_caseInsensitive() {
		var parser = compileRegParser("#`shape and shade`");
		
		validate("(!textCI(\"shape and shade\")!)\n"
				+ "  - (!textCI(\"shape and shade\")!)",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, level: 2, checker: .{15}\n"
				+ "offset: 1, level: 2, checker: .{15}\n"
				+ "offset: 2, level: 2, checker: .{15}\n"
				+ "offset: 3, level: 2, checker: .{15}\n"
				+ "offset: 4, level: 2, checker: .{15}\n"
				+ "offset: 5, level: 2, checker: .{15}\n"
				+ "offset: 6, level: 2, checker: .{15}\n"
				+ "offset: 7, level: 2, checker: .{15}\n"
				+ "offset: 8, level: 2, checker: .{15}\n"
				+ "offset: 9, level: 2, checker: .{15}\n"
				+ "offset: 10, level: 2, checker: .{15}\n"
				+ "offset: 11, level: 2, checker: .{15}\n"
				+ "offset: 12, level: 2, checker: .{15}\n"
				+ "offset: 13, level: 2, checker: .{15}\n"
				+ "offset: 14, level: 2, checker: .{15}\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testExact_backtick_caseInsensitive_notMatch() {
		var parser = compileRegParser("#`shape and shade`");
		
		validate("(!textCI(\"shape and shade\")!)\n"
				+ "  - (!textCI(\"shape and shade\")!)",
				parser);
		
		validate("RPRootText [original=Shape and Shade]\n"
				+ "offset: 0, level: 2, checker: .{15}\n"
				+ "offset: 1, level: 2, checker: .{15}\n"
				+ "offset: 2, level: 2, checker: .{15}\n"
				+ "offset: 3, level: 2, checker: .{15}\n"
				+ "offset: 4, level: 2, checker: .{15}\n"
				+ "offset: 5, level: 2, checker: .{15}\n"
				+ "offset: 6, level: 2, checker: .{15}\n"
				+ "offset: 7, level: 2, checker: .{15}\n"
				+ "offset: 8, level: 2, checker: .{15}\n"
				+ "offset: 9, level: 2, checker: .{15}\n"
				+ "offset: 10, level: 2, checker: .{15}\n"
				+ "offset: 11, level: 2, checker: .{15}\n"
				+ "offset: 12, level: 2, checker: .{15}\n"
				+ "offset: 13, level: 2, checker: .{15}\n"
				+ "offset: 14, level: 2, checker: .{15}\n"
				+ "offset: 15",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testTextCI_withUpperCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, level: 1, checker: .{5}\n"
				+ "offset: 1, level: 1, checker: .{5}\n"
				+ "offset: 2, level: 1, checker: .{5}\n"
				+ "offset: 3, level: 1, checker: .{5}\n"
				+ "offset: 4, level: 1, checker: .{5}\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testTextCI_allLowerCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=shape]\n"
				+ "offset: 0, level: 1, checker: .{5}\n"
				+ "offset: 1, level: 1, checker: .{5}\n"
				+ "offset: 2, level: 1, checker: .{5}\n"
				+ "offset: 3, level: 1, checker: .{5}\n"
				+ "offset: 4, level: 1, checker: .{5}\n"
				+ "offset: 5",
				parse("shape", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testTextCI_allUpperCase() {// "!textCI(`Te\\\"st`)!"
		var parser = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=SHAPE]\n"
				+ "offset: 0, level: 1, checker: .{5}\n"
				+ "offset: 1, level: 1, checker: .{5}\n"
				+ "offset: 2, level: 1, checker: .{5}\n"
				+ "offset: 3, level: 1, checker: .{5}\n"
				+ "offset: 4, level: 1, checker: .{5}\n"
				+ "offset: 5",
				parse("SHAPE", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testTextCI_escape() {
		var parser = compileRegParser("!textCI(`this is a \"test\".`)!");
		
		validate("(!textCI(\"this is a \\\"test\\\".\")!)",
				parser);
		
		validate("RPRootText [original=This is a \"test\".]\n"
				+ "offset: 0, level: 1, checker: .{17}\n"
				+ "offset: 1, level: 1, checker: .{17}\n"
				+ "offset: 2, level: 1, checker: .{17}\n"
				+ "offset: 3, level: 1, checker: .{17}\n"
				+ "offset: 4, level: 1, checker: .{17}\n"
				+ "offset: 5, level: 1, checker: .{17}\n"
				+ "offset: 6, level: 1, checker: .{17}\n"
				+ "offset: 7, level: 1, checker: .{17}\n"
				+ "offset: 8, level: 1, checker: .{17}\n"
				+ "offset: 9, level: 1, checker: .{17}\n"
				+ "offset: 10, level: 1, checker: .{17}\n"
				+ "offset: 11, level: 1, checker: .{17}\n"
				+ "offset: 12, level: 1, checker: .{17}\n"
				+ "offset: 13, level: 1, checker: .{17}\n"
				+ "offset: 14, level: 1, checker: .{17}\n"
				+ "offset: 15, level: 1, checker: .{17}\n"
				+ "offset: 16, level: 1, checker: .{17}\n"
				+ "offset: 17",
				parse("This is a \"test\".", parser, typeProvider));
	}
	
	@Test
	public void testOptional_wihtout() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPRootText [original=Color]\n"
				+ "offset: 0, level: 0, checker: Colo\n"
				+ "offset: 4, level: 0, checker: r\n"
				+ "offset: 5",
				parse("Color", parser, typeProvider));
	}
	
	@Test
	public void testOptional_with() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPRootText [original=Colour]\n"
				+ "offset: 0, level: 0, checker: Colo\n"
				+ "offset: 4, level: 0, checker: u?\n"
				+ "offset: 5, level: 0, checker: r\n"
				+ "offset: 6",
				parse("Colour", parser, typeProvider));
	}
	
	@Test
	public void testOptional_unmatched() {
		var parser = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Colo\"\n"
				+ "RPRootText [original=Clr]\n"
				+ "offset: 0",
				parse("Clr", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_2() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_3() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A0ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_notEnough_4() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_justEnough_1() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_justEnough_2() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6, level: 0, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_possessive_tooMany() {
		var parser = compileRegParser("A[0-9Z]{2,4}Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_notEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_justEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, level: 0, checker: Z+\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_enough_tailGetMore() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A01ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, level: 0, checker: Z+\n"
				+ "offset: 4, level: 0, checker: Z+\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_stretch1() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, level: 0, checker: Z+\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_stretchToMax() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6, level: 0, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_minimum_cannotStretchEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}*Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}*\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}*\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_notEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=AZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3",
				parse("AZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_justEnough() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, level: 0, checker: Z+\n"
				+ "offset: 4",
				parse("A0ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_enough1() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A01ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, level: 0, checker: Z+\n"
				+ "offset: 5",
				parse("A01ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_enough2_max() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A012ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6",
				parse("A012ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_max() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPRootText [original=A0123ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5, level: 0, checker: Z+\n"
				+ "offset: 6, level: 0, checker: Z+\n"
				+ "offset: 7",
				parse("A0123ZZ", parser, typeProvider));
	}
	
	@Test
	public void testBound_maximum_tooManyMatch() {
		var parser = compileRegParser("A[0-9Z]{2,4}+Z+");
		
		validate("A\n"
				+ "[[0-9][Z]]{2,4}+\n"
				+ "Z+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A01234ZZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 2, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 3, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 4, level: 0, checker: [[0-9][Z]]{2,4}+\n"
				+ "offset: 5",
				parse("A01234ZZ", parser, typeProvider));
	}
	
	@Test
	public void testWhitespaces_matched() {
		var parser = compileRegParser("Ho Ho\tHo\nHo");
		
		validate("Ho\n"
				+ "Ho\n"
				+ "Ho\n"
				+ "Ho",
				parser);
		
		validate("RPRootText [original=HoHoHoHo]\n"
				+ "offset: 0, level: 0, checker: Ho\n"
				+ "offset: 2, level: 0, checker: Ho\n"
				+ "offset: 4, level: 0, checker: Ho\n"
				+ "offset: 6, level: 0, checker: Ho\n"
				+ "offset: 8",
				parse("HoHoHoHo", parser, typeProvider));
	}
	
	@Test
	public void testWhitespaces_notMatch() {
		var parser = compileRegParser("Ho Ho\tHo\nHo");
		
		validate("Ho\n"
				+ "Ho\n"
				+ "Ho\n"
				+ "Ho",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Ho\"\n"
				+ "RPRootText [original=Ho Ho Ho Ho]\n"
				+ "offset: 0, level: 0, checker: Ho\n"
				+ "offset: 2",
				parse("Ho Ho Ho Ho", parser, typeProvider));
	}
	
	@Test
	public void testUseWhiltespaces() {
		var parser = compileRegParser("Ho[: :]Ho[:Tab:]Ho[:NewLine:]Ho");
		
		validate("Ho\n"
				+ "[\\ ]\n"
				+ "Ho\n"
				+ "[\\t]\n"
				+ "Ho\n"
				+ "[\\n]\n"
				+ "Ho",
				parser);
		
		
		validate("RPRootText [original=Ho Ho	Ho\n"
				+ "Ho]\n"
				+ "offset: 0, level: 0, checker: Ho\n"
				+ "offset: 2, level: 0, checker: [\\ ]\n"
				+ "offset: 3, level: 0, checker: Ho\n"
				+ "offset: 5, level: 0, checker: [\\t]\n"
				+ "offset: 6, level: 0, checker: Ho\n"
				+ "offset: 8, level: 0, checker: [\\n]\n"
				+ "offset: 9, level: 0, checker: Ho\n"
				+ "offset: 11",
				parse("Ho Ho\tHo\nHo", parser, typeProvider));
	}
	
	@Test
	public void testParseVsMatch() {
		var parser = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		// When the text match completely, `parse(...)` and `match(...)` will be the same.
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, level: 0, checker: Shape\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, level: 0, checker: Shape\n"
				+ "offset: 5",
				match("Shape", parser, typeProvider));
		
		// "parse(...)" allow tail left over. -- Notice `n` tail at the end of the text.
		// "parse(...)" returns a result but the result will leave out the tail `n`.
		validate("RPRootText [original=Shapen]\n"
				+ "offset: 0, level: 0, checker: Shape\n"
				+ "offset: 5",
				parse("Shapen", parser, typeProvider));
		
		// "match(...)" does not allow tail left over.
		// So it return `null` for not match.
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=Shapen]\n"
				+ "offset: 0, level: 0, checker: Shape\n"
				+ "offset: 5",
				match("Shapen", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roar]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3, level: 0, checker: r*\n"
				+ "offset: 4",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_extra() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roarrrrrrrr]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3, level: 0, checker: r*\n"
				+ "offset: 4, level: 0, checker: r*\n"
				+ "offset: 5, level: 0, checker: r*\n"
				+ "offset: 6, level: 0, checker: r*\n"
				+ "offset: 7, level: 0, checker: r*\n"
				+ "offset: 8, level: 0, checker: r*\n"
				+ "offset: 9, level: 0, checker: r*\n"
				+ "offset: 10, level: 0, checker: r*\n"
				+ "offset: 11",
				parse("Roarrrrrrrr", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_missing() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPRootText [original=Roa]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3",
				parse("Roa", parser, typeProvider));
	}
	
	@Test
	public void testZeroOrMore_unmatched() {
		var parser = compileRegParser("Roar*");
		
		validate("Roa\n"
				+ "r*",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Roa\"\n"
				+ "RPRootText [original=Row]\n"
				+ "offset: 0",
				parse("Row", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_justOne() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPRootText [original=Roar]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3, level: 0, checker: r+\n"
				+ "offset: 4",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_extraR() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPRootText [original=Roarrrrrrrr]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3, level: 0, checker: r+\n"
				+ "offset: 4, level: 0, checker: r+\n"
				+ "offset: 5, level: 0, checker: r+\n"
				+ "offset: 6, level: 0, checker: r+\n"
				+ "offset: 7, level: 0, checker: r+\n"
				+ "offset: 8, level: 0, checker: r+\n"
				+ "offset: 9, level: 0, checker: r+\n"
				+ "offset: 10, level: 0, checker: r+\n"
				+ "offset: 11",
				parse("Roarrrrrrrr", parser, typeProvider));
	}
	
	@Test
	public void testOneOrMore_unmatched() {
		var parser = compileRegParser("Roar+");
		
		validate("Roa\n"
				+ "r+",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"r\"\n"
				+ "RPRootText [original=Road]\n"
				+ "offset: 0, level: 0, checker: Roa\n"
				+ "offset: 3",
				parse("Road", parser, typeProvider));
	}
	
	@Test
	public void testZero_matched() {
		var parser = compileRegParser("Ro^ar");
		
		validate("R\n"
				+ "o^\n"
				+ "ar",
				parser);
		
		validate("RPRootText [original=Rar]\n"
				+ "offset: 0, level: 0, checker: R\n"
				+ "offset: 1, level: 0, checker: ar\n"
				+ "offset: 3",
				parse("Rar", parser, typeProvider));
	}
	
	@Test
	public void testZero_unmatched() {
		var parser = compileRegParser("Ro^ar");
		
		validate("R\n"
				+ "o^\n"
				+ "ar",
				parser);
		
		validate("RPUnmatchedText: Expect at most [0] but found [1]: \"o\"\n"
				+ "RPRootText [original=Roar]\n"
				+ "offset: 0, level: 0, checker: R\n"
				+ "offset: 1, level: 0, checker: o^\n"
				+ "offset: 2",
				parse("Roar", parser, typeProvider));
	}
	
	@Test
	public void testZero_greedyException() {
		try {
			compileRegParser("Ro^+ar");
			fail("Expect an exception.");
		} catch (Exception exception) {
			// Do nothing.
			validate("net.nawaman.regparser.compiler.MalFormedRegParserException: Zero quantifier cannot have maximum greediness: \n"
					+ "	-|Ro^+ar\n"
					+ "	-|   ^\n"
					+ "",
					exception);
		}
		
		try {
			compileRegParser("Ro^*ar");
			fail("Expect an exception.");
		} catch (Exception exception) {
			// Do nothing.
			validate("net.nawaman.regparser.compiler.MalFormedRegParserException: Zero quantifier cannot have minimum greediness: \n"
					+ "	-|Ro^*ar\n"
					+ "	-|   ^\n"
					+ "",
					exception);
		}
	}
	
	@Test
	public void testAny() {
		var parser = compileRegParser("A.Z");
		
		validate("A\n"
				+ ".\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A2Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .\n"
				+ "offset: 2, level: 0, checker: Z\n"
				+ "offset: 3",
				parse("A2Z", parser, typeProvider));
	}
	
	@Test
	public void testAny_unmatched() {
		var parser = compileRegParser("A.Z");
		
		validate("A\n"
				+ ".\n"
				+ "Z",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .\n"
				+ "offset: 2",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testAny_minimum() {
		var parser = compileRegParser("A.**Z");
		
		validate("A\n"
				+ ".**\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .**\n"
				+ "offset: 2, level: 0, checker: .**\n"
				+ "offset: 3, level: 0, checker: .**\n"
				+ "offset: 4, level: 0, checker: .**\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testAny_end() {
		var parser = compileRegParser("A.*");
		
		validate("A\n"
				+ ".*",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .*\n"
				+ "offset: 2, level: 0, checker: .*\n"
				+ "offset: 3, level: 0, checker: .*\n"
				+ "offset: 4, level: 0, checker: .*\n"
				+ "offset: 5, level: 0, checker: .*\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testCharClass_single() {
		var parser = compileRegParser("A[0-9]+Z");
		
		validate("A\n"
				+ "[0-9]+\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A2Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [0-9]+\n"
				+ "offset: 2, level: 0, checker: Z\n"
				+ "offset: 3",
				parse("A2Z", parser, typeProvider));
	}
	
	@Test
	public void testCharClass_multiple() {
		var parser = compileRegParser("A[0-9]+Z");
		
		validate("A\n"
				+ "[0-9]+\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [0-9]+\n"
				+ "offset: 2, level: 0, checker: [0-9]+\n"
				+ "offset: 3, level: 0, checker: [0-9]+\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Test
	public void testCharClass_unmatched() {
		var parser = compileRegParser("A[0-9]+Z");
		
		validate("A\n"
				+ "[0-9]+\n"
				+ "Z",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"[0-9]\"\n"
				+ "RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testCharClass_negative() {
		// We use `++` as one-or-more-maximum, because just one-or-more will be possessive and it will include Z
		//   and no place for `Z`.
		var parser = compileRegParser("A[^0-9]++Z");
		
		validate("A\n"
				+ "[^[0-9]]++\n"
				+ "Z",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"[^[0-9]]\"\n"
				+ "RPRootText [original=A2Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1",
				parse("A2Z", parser, typeProvider));
	}
	
	@Test
	public void testCharClass_negative_unmatched() {
		var parser = compileRegParser("A[^0-9]++Z");
		
		validate("A\n"
				+ "[^[0-9]]++\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: [^[0-9]]++\n"
				+ "offset: 2, level: 0, checker: [^[0-9]]++\n"
				+ "offset: 3, level: 0, checker: [^[0-9]]++\n"
				+ "offset: 4, level: 0, checker: [^[0-9]]++\n"
				+ "offset: 5, level: 0, checker: Z\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_true() {
		var parser = compileRegParser("(true|false)");
		
		validate("RPRootText [original=true]\n"
				+ "offset: 0, level: 0, checker: (true|false)\n"
				+ "offset: 4",
				parse("true", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_false() {
		var parser = compileRegParser("(true|false)");
		
		validate("RPRootText [original=false]\n"
				+ "offset: 0, level: 0, checker: (true|false)\n"
				+ "offset: 5",
				parse("false", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_notMatched() {
		var parser = compileRegParser("(true|false)");
		
		validate("RPUnmatchedText: Expect but not found: \"(true|false)\"\n"
				+ "RPRootText [original=maybe]\n"
				+ "offset: 0",
				parse("maybe", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_negative_true() {
		var parser = compileRegParser("(^true|false)");
		
		validate("RPUnmatchedText: Expect but not found: \"(^(true|false))\"\n"
				+ "RPRootText [original=true]\n"
				+ "offset: 0",
				parse("true", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_negative_false() {
		var parser = compileRegParser("(^true|false)");
		
		validate("RPUnmatchedText: Expect but not found: \"(^(true|false))\"\n"
				+ "RPRootText [original=false]\n"
				+ "offset: 0",
				parse("false", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_negative_unsure_matchedOne() {
		var parser = compileRegParser("(^true|false)");
		
		// Notice only one character match
		validate("RPRootText [original=unsure]\n"
				+ "offset: 0, level: 0, checker: (^(true|false))\n"
				+ "offset: 1",
				parse("unsure", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_negative_notMatched_wholeWord() {
		var parser = compileRegParser("(^true|false)+");
		
		//-- This might be more practically useful.
		validate("RPUnmatchedText: Expect but not found: \"(^(true|false))\"\n"
				+ "RPRootText [original=true]\n"
				+ "offset: 0",
				parse("true",  parser, typeProvider));
		validate("RPUnmatchedText: Expect but not found: \"(^(true|false))\"\n"
				+ "RPRootText [original=false]\n"
				+ "offset: 0",
				parse("false", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_negative_notMatch_oneCharactor() {
		var parser = compileRegParser("(^true|false)");
		
		// Notice only one character match
		validate("RPRootText [original=unsure]\n"
				+ "offset: 0, level: 0, checker: (^(true|false))\n"
				+ "offset: 1",
				parse("unsure", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_length_shortToLong() {
		var parser = compileRegParser("(AA|AAA|AAAA)");
		
		validate("(AA|AAA|AAAA)",
				parser);
		
		validate("RPRootText [original=AA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA|AAAA)\n"
				+ "offset: 2",
				match("AA", parser, typeProvider));
		
		validate("RPRootText [original=AAA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA|AAAA)\n"
				+ "offset: 3",
				match("AAA", parser, typeProvider));
		
		validate("RPRootText [original=AAAA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA|AAAA)\n"
				+ "offset: 4",
				match("AAAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_length_longToShort() {
		// Alternative will try to match longest ... the order of the choice make no different.
		
		var parser = compileRegParser("(AAAA|AAA|AA)");
		
		validate("(AAAA|AAA|AA)",
				parser);
		
		validate("RPRootText [original=AA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA|AA)\n"
				+ "offset: 2",
				match("AA", parser, typeProvider));
		
		validate("RPRootText [original=AAA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA|AA)\n"
				+ "offset: 3",
				match("AAA", parser, typeProvider));
		
		validate("RPRootText [original=AAAA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA|AA)\n"
				+ "offset: 4",
				match("AAAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default() {
		var parser = compileRegParser("(AA|AAA||AAAA)");
		
		validate("(AA|AAA||AAAA)",
				parser);
		
		validate("RPRootText [original=AA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA||AAAA)\n"
				+ "offset: 2",
				match("AA", parser, typeProvider));
		
		validate("RPRootText [original=AAA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA||AAAA)\n"
				+ "offset: 3",
				match("AAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default_matchedBeforeDefault() {
		var parser = compileRegParser("(AA|AAA||AAAA)");
		
		validate("(AA|AAA||AAAA)",
				parser);
		
		// Why?
		// AA and AAA can match ... so they do and the tail is left which result in an unmatched text.
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=AAAA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA||AAAA)\n"
				+ "offset: 3",
				match("AAAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default_someLeft() {
		var parser = compileRegParser("(AA|AAA||AAAA)");
		
		validate("(AA|AAA||AAAA)",
				parser);
		
		
		// So to confirm, try with `parse` and confirm that only `AAA` match (so with tail `A`).
		validate("RPIncompletedText: Match found but do not covering the whole text: \n"
				+ "RPRootText [original=AAAA]\n"
				+ "offset: 0, level: 0, checker: (AA|AAA||AAAA)\n"
				+ "offset: 3",
				match("AAAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default_matchedDefault() {
		var parser = compileRegParser("(AAAA|AAA||AA)");
		
		validate("(AAAA|AAA||AA)",
				parser);
		
		// Why?
		// AAAA and AAA cannot match, the parser try AA and found a match.
		validate("RPRootText [original=AA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA||AA)\n"
				+ "offset: 2",
				match("AA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default_defaultMatch_butNonDefaultMatchFault() {
		var parser = compileRegParser("(AAAA|AAA||AA)");
		
		validate("(AAAA|AAA||AA)",
				parser);
		
		validate("RPRootText [original=AAA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA||AA)\n"
				+ "offset: 3",
				match("AAA", parser, typeProvider));
	}
	
	@Test
	public void testAlternatives_default_defaultMatch_butNonDefaultMatchFault2() {
		var parser = compileRegParser("(AAAA|AAA||AA)");
		
		validate("(AAAA|AAA||AA)",
				parser);
		
		validate("RPRootText [original=AAAA]\n"
				+ "offset: 0, level: 0, checker: (AAAA|AAA||AA)\n"
				+ "offset: 4",
				match("AAAA", parser, typeProvider));
	}
	
	@Test
	public void testComment_slashStar_matched() {
		// We comment out the `not `.
		var parser = compileRegParser("Orange[: :]is[: :]/*not[: :]*/a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPRootText [original=Orange is a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10, level: 0, checker: a\n"
				+ "offset: 11, level: 0, checker: [\\ ]\n"
				+ "offset: 12, level: 0, checker: color\n"
				+ "offset: 17, level: 0, checker: .\n"
				+ "offset: 18",
				parse("Orange is a color.", parser, typeProvider));
	}
	
	@Test
	public void testComment_slashStar_unmatched() {
		// We comment out the `not `.
		var parser = compileRegParser("Orange[: :]is[: :]/*not[: :]*/a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=Orange is not a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10",
				parse("Orange is not a color.", parser, typeProvider));
	}
	
	@Test
	public void testComment_backetStar_matched() {
		var parser = compileRegParser("Orange[: :]is[: :](*not[: :]*)a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPRootText [original=Orange is a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10, level: 0, checker: a\n"
				+ "offset: 11, level: 0, checker: [\\ ]\n"
				+ "offset: 12, level: 0, checker: color\n"
				+ "offset: 17, level: 0, checker: .\n"
				+ "offset: 18",
				parse("Orange is a color.", parser, typeProvider));
	}
	
	@Test
	public void testComment_backetStar_unmatched() {
		var parser = compileRegParser("Orange[: :]is[: :](*not[: :]*)a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=Orange is not a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10",
				parse("Orange is not a color.", parser, typeProvider));
	}
	
	@Test
	public void testComment_lineComment_matched() {
		var parser = compileRegParser(
						"Orange[: :]is[: :]//not[: :]\n"
						+ "a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPRootText [original=Orange is a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10, level: 0, checker: a\n"
				+ "offset: 11, level: 0, checker: [\\ ]\n"
				+ "offset: 12, level: 0, checker: color\n"
				+ "offset: 17, level: 0, checker: .\n"
				+ "offset: 18",
				parse("Orange is a color.", parser, typeProvider));
	}
	
	@Test
	public void testComment_lineComment_unmatched() {
		var parser = compileRegParser(
						"Orange[: :]is[: :]//not[: :]\n"
						+ "a[: :]color.");
		
		validate("Orange\n"
				+ "[\\ ]\n"
				+ "is\n"
				+ "[\\ ]\n"
				+ "a\n"
				+ "[\\ ]\n"
				+ "color\n"
				+ ".",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"a\"\n"
				+ "RPRootText [original=Orange is not a color.]\n"
				+ "offset: 0, level: 0, checker: Orange\n"
				+ "offset: 6, level: 0, checker: [\\ ]\n"
				+ "offset: 7, level: 0, checker: is\n"
				+ "offset: 9, level: 0, checker: [\\ ]\n"
				+ "offset: 10",
				parse("Orange is not a color.", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testPossessive_named_outside_any() {
		var parser = compileRegParser("A($Middle:~.~)*Z");
		
		validate("A\n"
				+ "($Middle:~.~)*\n"
				+ "Z",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: ($Middle:~.~)*\n"
				+ "offset: 2, level: 0, checker: ($Middle:~.~)*\n"
				+ "offset: 3, level: 0, checker: ($Middle:~.~)*\n"
				+ "offset: 4, level: 0, checker: ($Middle:~.~)*\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testPossessive_named_inside_any() {
		var parser = compileRegParser("A($Middle:~.*~)Z");
		
		validate("A\n"
				+ "($Middle:~.*~)\n"
				+ "  - .*\n"
				+ "Z",
				parser);
		
		validate("RPUnmatchedText: Expect but not found: \"Z\"\n"
				+ "RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: .*\n"
				+ "offset: 2, level: 1, checker: .*\n"
				+ "offset: 3, level: 1, checker: .*\n"
				+ "offset: 4, level: 1, checker: .*\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testPossessive_named_outside() {
		var parser = compileRegParser("A($Middle:~[0-9]~)*Z");
		
		validate("A\n"
				+ "($Middle:~[0-9]~)*\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: ($Middle:~[0-9]~)*\n"
				+ "offset: 2, level: 0, checker: ($Middle:~[0-9]~)*\n"
				+ "offset: 3, level: 0, checker: ($Middle:~[0-9]~)*\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testPossessive_named_inside() {
		var parser = compileRegParser("A($Middle:~[0-9]*~)Z");
		
		validate("A\n"
				+ "($Middle:~[0-9]*~)\n"
				+ "  - [0-9]*\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: [0-9]*\n"
				+ "offset: 2, level: 1, checker: [0-9]*\n"
				+ "offset: 3, level: 1, checker: [0-9]*\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMinimum_named_outside() {
		var parser = compileRegParser("A($Middle:~.~)**Z");
		
		validate("A\n"
				+ "($Middle:~.~)**\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: ($Middle:~.~)**\n"
				+ "offset: 2, level: 0, checker: ($Middle:~.~)**\n"
				+ "offset: 3, level: 0, checker: ($Middle:~.~)**\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMinimum_named_inside() {
		var parser = compileRegParser("A($Middle:~.**~)Z");
		
		validate("A\n"
				+ "($Middle:~.**~)\n"
				+ "  - .**\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: .**\n"
				+ "offset: 2, level: 1, checker: .**\n"
				+ "offset: 3, level: 1, checker: .**\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMaximum_named_outside() {
		var parser = compileRegParser("A($Middle:~.~)*+Z");
		
		validate("A\n"
				+ "($Middle:~.~)*+\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: ($Middle:~.~)*+\n"
				+ "offset: 2, level: 0, checker: ($Middle:~.~)*+\n"
				+ "offset: 3, level: 0, checker: ($Middle:~.~)*+\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testMaximum_named_inside() {
		var parser = compileRegParser("A($Middle:~.*+~)Z");
		
		validate("A\n"
				+ "($Middle:~.*+~)\n"
				+ "  - .*+\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=A123Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: .*+\n"
				+ "offset: 2, level: 1, checker: .*+\n"
				+ "offset: 3, level: 1, checker: .*+\n"
				+ "offset: 4, level: 0, checker: Z\n"
				+ "offset: 5",
				parse("A123Z", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testNestedGroup() {
		var parser = compileRegParser("A(#L1:~a(#L2:~0(#L3:~[A-Z]+~)9~)z~)Z");
		
		validate("A\n"
				+ "(#L1:~a(#L2:~0(#L3:~[A-Z]+~)9~)z~)\n"
				+ "  - a\n"
				+ "  - (#L2:~0(#L3:~[A-Z]+~)9~)\n"
				+ "  -   - 0\n"
				+ "  -   - (#L3:~[A-Z]+~)\n"
				+ "  -   -   - [A-Z]+\n"
				+ "  -   - 9\n"
				+ "  - z\n"
				+ "Z",
				parser);
		
		validate("RPRootText [original=Aa0AAA9zZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: a\n"
				+ "offset: 2, level: 2, checker: 0\n"
				+ "offset: 3, level: 3, checker: [A-Z]+\n"
				+ "offset: 4, level: 3, checker: [A-Z]+\n"
				+ "offset: 5, level: 3, checker: [A-Z]+\n"
				+ "offset: 6, level: 2, checker: 9\n"
				+ "offset: 7, level: 1, checker: z\n"
				+ "offset: 8, level: 0, checker: Z\n"
				+ "offset: 9",
				parse("Aa0AAA9zZ", parser, typeProvider));
	}
	
	@Ignore
	@Test
	public void testNestedGroup_multiple() {
		var parser = compileRegParser("[:~:](#L1:~[:<:](#L2:~[:(:](#L3:~[A-Z]+~)[:):]~){3}[:>:]~){2}[:~:]");
		
		validate("[\\~]\n"
				+ "(#L1:~[<](#L2:~[\\(](#L3:~[A-Z]+~)[\\)]~){3}[>]~){2}\n"
				+ "  - [<]\n"
				+ "  - (#L2:~[\\(](#L3:~[A-Z]+~)[\\)]~){3}\n"
				+ "  -   - [\\(]\n"
				+ "  -   - (#L3:~[A-Z]+~)\n"
				+ "  -   -   - [A-Z]+\n"
				+ "  -   - [\\)]\n"
				+ "  - [>]\n"
				+ "[\\~]",
				parser);
		
		validate("RPRootText [original=Aa0AAA9zZ]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 1, checker: a\n"
				+ "offset: 2, level: 2, checker: 0\n"
				+ "offset: 3, level: 3, checker: [A-Z]+\n"
				+ "offset: 4, level: 3, checker: [A-Z]+\n"
				+ "offset: 5, level: 3, checker: [A-Z]+\n"
				+ "offset: 6, level: 2, checker: 9\n"
				+ "offset: 7, level: 1, checker: z\n"
				+ "offset: 8, level: 0, checker: Z\n"
				+ "offset: 9",
				parse("~<(AA)(BB)(CC)><(DD)(EE)(FF)>~", parser, typeProvider));
	}
	
	@Test
	public void testMinimumThenObsessive() {
		var parser = compileRegParser("A.**Z^");
		
		validate("A\n"
				+ ".**\n"
				+ "Z^",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .**\n"
				+ "offset: 2, level: 0, checker: .**\n"
				+ "offset: 3, level: 0, checker: .**\n"
				+ "offset: 4, level: 0, checker: .**\n"
				+ "offset: 5, level: 0, checker: .**\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	@Test
	public void testMaximumThenObsessive() {
		var parser = compileRegParser("A.*+Z^");
		
		validate("A\n"
				+ ".*+\n"
				+ "Z^",
				parser);
		
		validate("RPRootText [original=A-to-Z]\n"
				+ "offset: 0, level: 0, checker: A\n"
				+ "offset: 1, level: 0, checker: .*+\n"
				+ "offset: 2, level: 0, checker: .*+\n"
				+ "offset: 3, level: 0, checker: .*+\n"
				+ "offset: 4, level: 0, checker: .*+\n"
				+ "offset: 5, level: 0, checker: .*+\n"
				+ "offset: 6",
				parse("A-to-Z", parser, typeProvider));
	}
	
	//==================================================================================================================
	
	static RPText parse(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
		try {
			return new NewRegParserResolver(orgText, parser, typeProvider).parse();
		} finally {
			System.out.println();
		}
	}
	static RPText match(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
		try {
			return new NewRegParserResolver(orgText, parser, typeProvider).match();
		} finally {
			System.out.println();
		}
	}
	
}
