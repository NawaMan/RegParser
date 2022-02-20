package net.nawaman.regparser.newway;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.ParsingException;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.result.ParseResult;
import net.nawaman.regparser.result.ParseResultNode;
import net.nawaman.regparser.result.RootParseResult;
import net.nawaman.regparser.result.entry.ParseResultEntry;

public class NewRegParserTest {
	
	@Test
	public void test() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("Colou?r");
		
		parse("Color", parser, typeProvider);
		System.out.println();
		
		parse("Colour", parser, typeProvider);
		System.out.println();
		
		System.out.println("DONE!");
	}
	
	@Test
	public void testExact() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("Shape");
		
		validate("Shape",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, asChecker: Shape\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
		
		validate("RPImcompleteText: Expect atleast [1] but only found [0]\n"
				+ "RPRootText [original=Sharp]\n"
				+ "offset: 0",
				parse("Sharp", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, asChecker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
		
		validate("RPImcompleteText: Expect atleast [1] but only found [0]\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("$`shape and shade`");
		
		validate("shape\\ and\\ shade",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, asChecker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
		
		validate("RPImcompleteText: Expect atleast [1] but only found [0]\n"
				+ "RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseInsensitive() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("#`shape and shade`");
		
		validate("(!textCI(\"shape and shade\")!)\n"
				+ "  - (!textCI(\"shape and shade\")!)",
				parser);
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, asChecker: (!textCI(\"shape and shade\")!)\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
		
		validate("RPRootText [original=Shape and Shade]\n"
				+ "offset: 0, asChecker: (!textCI(\"shape and shade\")!)\n"
				+ "offset: 15",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testTextCI() {// "!textCI(`Te\\\"st`)!"
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("!textCI(`shape`)!");
		
		validate("(!textCI(\"shape\")!)",
				parser);
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, asChecker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
		
		validate("RPRootText [original=shape]\n"
				+ "offset: 0, asChecker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("shape", parser, typeProvider));
		
		validate("RPRootText [original=SHAPE]\n"
				+ "offset: 0, asChecker: (!textCI(\"shape\")!)\n"
				+ "offset: 5",
				parse("SHAPE", parser, typeProvider));
	}
	
	@Test
	public void testTextCI_escape() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("!textCI(`this is a \"test\".`)!");
		
		validate("(!textCI(\"this is a \\\"test\\\".\")!)",
				parser);
		
		validate("RPRootText [original=This is a \"test\".]\n"
				+ "offset: 0, asChecker: (!textCI(\"this is a \\\"test\\\".\")!)\n"
				+ "offset: 17",
				parse("This is a \"test\".", parser, typeProvider));
	}
	
	@Test
	public void testOptional() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("Colou?r");
		
		validate("Colo\n"
				+ "u?\n"
				+ "r",
				parser);
		
		validate("RPRootText [original=Color]\n"
				+ "offset: 0, asChecker: Colo\n"
				+ "offset: 4, asChecker: r\n"
				+ "offset: 5",
				parse("Color", parser, typeProvider));
		
		validate("RPRootText [original=Colour]\n"
				+ "offset: 0, asChecker: Colo\n"
				+ "offset: 4, asChecker: u?\n"
				+ "offset: 5, asChecker: r\n"
				+ "offset: 6",
				parse("Colour", parser, typeProvider));
		
		validate("RPImcompleteText: Expect atleast [1] but only found [0]\n"
				+ "RPRootText [original=Clr]\n"
				+ "offset: 0",
				parse("Clr", parser, typeProvider));
	}
	
	private static class Session {
		final RegParser parser;
		final int       entryCount;
		
		int entryIndex = 0;
		
		public Session(RegParser parser) {
			this.parser     = parser;
			this.entryCount = parser.getEntryCount();
			this.entryIndex = 0;
		}
		
		public RegParserEntry entry() {
			return parser.getEntryAt(this.entryIndex);
		}
		
		public boolean isInProgress() {
			return entryIndex < entryCount;
		}
		
		public void next() {
			entryIndex++;
		}
	}
	
	private static RPText parse(String orgText, RegParser parser, ParserTypeProvider typeProvider) {
		RPText  text    = new RPRootText(orgText);
		Session session = new Session(parser);
		
		ParseResult hostResult = null;
		
		int offset = 0;
		Session: while (session.isInProgress()) {
			try {
				var entry     = session.entry();
				var checker   = entry.checker();
				var type      = (ParserType)null;
				var parameter = (String)null;
				if (checker == null) {
					type = entry.type();
					if (type == null) {
						var typeRef  = entry.typeRef();
						type      = type(typeProvider, type, typeRef);
						parameter = typeRef.parameter();
					}
					
					checker = type.checker(hostResult, parameter, typeProvider);
				}
				
				var qualifier  = entry.quantifier();
				var lowerBound = qualifier.lowerBound();
				var upperBound = qualifier.upperBound();
				var greediness = qualifier.greediness();
				int repeat = 0;
				Repeate: while (true) {
					int length = text.match(offset, checker, typeProvider);
					
					System.out.println(session.entryIndex + ": " + entry + " = " + length);
					boolean isMatched
							=  (length != -1)
							&& ((type == null) || validateResult(typeProvider, text, offset, type, parameter, length));
					if (!isMatched)
						break Repeate;
					
					text = new RPNodeText(text, offset, entry);
					offset += length;
					repeat++;
					if (repeat >= upperBound)
						break Repeate;
				}
				if (repeat < lowerBound) {
					var found  = repeat;
					return new RPImcompleteText(text, offset, () -> {
						return format("Expect atleast [%d] but only found [%d]", lowerBound, found);
					});
				}
				
			} finally {
				session.next();
			}
		}
		text = new RPEndText(text, offset);
		
		System.out.println(text.root().matches);
		System.out.println(text);
		System.out.println();
		return text;
	}

	private static boolean validateResult(
			ParserTypeProvider typeProvider,
			RPText             text,
			int                offset,
	        ParserType         type,
	        String             parameter,
	        int                length) {
		
		var     hostResult  = (ParseResult)null;
		var     rootResult  = new RootParseResult(offset, text);
		var     prEntry     = ParseResultEntry.newEntry(offset + length);
		var     thisResult  = new ParseResultNode(offset, rootResult, asList(prEntry));
		boolean isValidated = type.validate(hostResult, thisResult, parameter, typeProvider);
		return isValidated;
	}

	private static ParserType type(ParserTypeProvider typeProvider, ParserType type, ParserTypeRef typeRef) {
		var typeName = typeRef.name();
		
		// Get type from the ref
		if (typeProvider != null) {
			// Get from the given provider
			type = typeProvider.type(typeName);
		}
		if (type == null) {
			// Get from the default
			type = ParserTypeProvider.Simple.defaultProvider().type(typeName);
			if (type == null)
				throw new ParsingException("RegParser type named '" + typeName + "' is not found.");
		}
		return type;
	}
}
