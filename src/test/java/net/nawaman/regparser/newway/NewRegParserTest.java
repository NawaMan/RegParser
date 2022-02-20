package net.nawaman.regparser.newway;

import static java.util.Arrays.asList;
import static net.nawaman.regparser.RegParser.compileRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import java.util.Arrays;

import org.junit.Test;

import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
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
		
		validate("RPRootText [original=Shape]\n"
				+ "offset: 0, asChecker: Shape\n"
				+ "offset: 5",
				parse("Shape", parser, typeProvider));
		
		validate("RPRootText [original=Sharp]\n"
				+ "offset: 0",
				parse("Sharp", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("`shape and shade`");
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, asChecker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
		
		validate("RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseSensitive() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("$`shape and shade`");
		
		validate("RPRootText [original=shape and shade]\n"
				+ "offset: 0, asChecker: shape\\ and\\ shade\n"
				+ "offset: 15",
				parse("shape and shade", parser, typeProvider));
		
		validate("RPRootText [original=Shape and Shade]\n"
				+ "offset: 0",
				parse("Shape and Shade", parser, typeProvider));
	}
	
	@Test
	public void testExact_backtick_caseInsensitive() {
		var typeProvider = (ParserTypeProvider)null;
		var parser       = compileRegParser("#`shape and shade`");
		
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
		ParseResult thisResult = null;
		
		int offset = 0;
		while (session.isInProgress()) {
			try {
				var entry     = session.entry();
//				var qualifier = entry.quantifier();
				var checker   = entry.checker();
				var type      = (ParserType)null;
				var parameter = (String)null;
				if (checker == null) {
					type = entry.type();
					if (type == null) {
						var typeRef  = entry.typeRef();
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
						
						parameter = typeRef.parameter();
					}
					
					checker = type.checker(hostResult, parameter, typeProvider);
				}
				
				int length = text.match(offset, checker, typeProvider);
				
				System.out.println(session.entryIndex + ": " + entry + " = " + length);
				if (length == -1) {
					System.out.println("length: " + length);
					continue;	// For now.
				}
				
				if (type != null) {
					// TODO - Temporary - We will need to get rid of the old result ... or generate it.
					var rootResult = new RootParseResult(offset, text);
					var prEntry    = ParseResultEntry.newEntry(offset + length);
					thisResult     = new ParseResultNode(offset, rootResult, asList(prEntry));
					if (!type.validate(hostResult, thisResult, parameter, typeProvider)) {
						System.out.println("validate: " + false);
						continue;	// For now.
					}
				}
				
				text = new RPNodeText(text, offset, entry);
				offset += length;
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
}
