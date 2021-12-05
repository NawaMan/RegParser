package net.nawaman.regparser;

import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.RPCompiler_ParserTypes.RPTCharSetItem;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParser;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParserItem;
import net.nawaman.regparser.compiler.RPCommentParserType;
import net.nawaman.regparser.compiler.RPEscapeHexParserType;
import net.nawaman.regparser.compiler.RPEscapeOctParserType;
import net.nawaman.regparser.compiler.RPEscapeParserType;
import net.nawaman.regparser.compiler.RPEscapeUnicodeParserType;
import net.nawaman.regparser.compiler.RPQuantifierParserType;
import net.nawaman.regparser.compiler.RPRangeParserType;
import net.nawaman.regparser.compiler.RPTypeParserType;
import net.nawaman.regparser.types.IdentifierParserType;
import net.nawaman.regparser.types.StringLiteralParserType;
import net.nawaman.regparser.types.TextCaseInsensitiveParserType;

public class TestRegParserCompiler1 {
	
	@ClassRule
	public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
	
	private ParserTypeProvider.Extensible typeProvider;
	private RegParser                parser;
	
	@Before
	public void setup() {
		typeProvider = new ParserTypeProvider.Extensible();
		typeProvider.addType(TextCaseInsensitiveParserType.instance);
		typeProvider.addType(IdentifierParserType.instance);
		typeProvider.addType(StringLiteralParserType.instance);
		typeProvider.addType(RPCommentParserType.instance);
		typeProvider.addType(RPTypeParserType.instance);
		typeProvider.addType(RPQuantifierParserType.instance);
		typeProvider.addType(new RPTRegParserItem());
		typeProvider.addType(RPEscapeParserType.instance);
		typeProvider.addType(RPEscapeOctParserType.instance);
		typeProvider.addType(RPEscapeHexParserType.instance);
		typeProvider.addType(RPEscapeUnicodeParserType.instance);
		typeProvider.addType(RPRangeParserType.instance);
		typeProvider.addType(new RPTCharSetItem());
		typeProvider.addType(new RPTRegParser());
		
		parser = newRegParser(typeProvider, RPTypeParserType.typeRef);
	}
	
	@Test
	public void testParseType() {
		validate("(!Type!)", parser);
		
		var result = parser.parse("!int!", typeProvider);
		validate("\n" 
		        + "00 - => [    5] = <NoName>        :Type             = \"!int!\"\n"
		        + ". 00 => [    1] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". 01 => [    4] = #TypeName       :$Identifier      = \"int\"\n"
		        + ". 02 => [    5] = <NoName>        :<NoType>         = \"!\"",
		        result);
		
		result = parser.parse("!int", typeProvider);
		validate(null, result);
		
		result = parser.parse("!int()!", typeProvider);
		validate("\n" 
		        + "00 - => [    7] = <NoName>        :Type             = \"!int()!\"\n"
		        + ". 00 => [    1] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". 01 => [    4] = #TypeName       :$Identifier      = \"int\"\n"
		        + ". 02 => [    6] = #Param          :<NoType>         = \"()\"\n"
		        + ". 03 => [    7] = <NoName>        :<NoType>         = \"!\"",
		        result);
		
		result = parser.parse("!int(`A string here`)!", typeProvider);
		validate("\n"
		        + "00 - - => [   22] = <NoName>        :Type             = \"!int(`A string here`)!\"\n"
		        + ". 00 - => [    1] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". 01 - => [    4] = #TypeName       :$Identifier      = \"int\"\n"
		        + ". 02 - => [   21] = #Param          :<NoType>         = \"(`A string here`)\"\n"
		        + ". . 00 => [    5] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . 01 => [   20] = #ParamValue     :$StringLiteral   = \"`A string here`\"\n"
		        + ". . 02 => [   21] = <NoName>        :<NoType>         = \")\"\n"
		        + ". 03 - => [   22] = <NoName>        :<NoType>         = \"!\"",
		        result);
	}
	
	@Test
	public void testParseCharClass() {
		var parser = newRegParser(typeProvider, new ParserTypeRef.Simple(RPTRegParserItem.Name), OneOrMore, Any, ZeroOrMore);
		
		var result = parser.match(".\\s\\D[:WhiteSpace:]a\\p{Blank}d", typeProvider);
		validate("\n"
		        + "00 - - => [    1] = <NoName>        :RegParserItem[]  = \".\"\n"
		        + ". 00 - => [    1] = #Any            :<NoType>         = \".\"\n"
		        + "01 - - => [    3] = <NoName>        :RegParserItem[]  = \"\\\\s\"\n"
		        + ". 00 - => [    3] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . 00 => [    2] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 => [    3] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + "02 - - => [    5] = <NoName>        :RegParserItem[]  = \"\\\\D\"\n"
		        + ". 00 - => [    5] = #CharClass      :<NoType>         = \"\\\\D\"\n"
		        + ". . 00 => [    4] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 => [    5] = $NonDigit       :<NoType>         = \"D\"\n"
		        + "03 - - => [   19] = <NoName>        :RegParserItem[]  = \"[:WhiteSpace:]\"\n"
		        + ". 00 - => [   19] = #CharClass      :<NoType>         = \"[:WhiteSpace:]\"\n"
		        + ". . 00 => [    7] = <NoName>        :<NoType>         = \"[:\"\n"
		        + ". . 01 => [   17] = $WhiteSpace     :<NoType>         = \"WhiteSpace\"\n"
		        + ". . 02 => [   19] = <NoName>        :<NoType>         = \":]\"\n"
		        + "04 - - => [   20] = <NoName>        :RegParserItem[]  = \"a\"\n"
		        + "05 - - => [   29] = <NoName>        :RegParserItem[]  = \"\\\\p{Blank}\"\n"
		        + ". 00 - => [   29] = #CharClass      :<NoType>         = \"\\\\p{Blank}\"\n"
		        + ". . 00 => [   23] = <NoName>        :<NoType>         = \"\\\\p{\"\n"
		        + ". . 01 => [   28] = $Blank          :<NoType>         = \"Blank\"\n"
		        + ". . 02 => [   29] = <NoName>        :<NoType>         = \"}\"\n"
		        + "06 - - => [   30] = <NoName>        :RegParserItem[]  = \"d\"",
		        result);
		
		result = parser.match(".\\s\\D\"\\'[:WhiteSpace:]\\.\\\\\\p{Blank}", typeProvider);
		validate("\n"
		        + "00 - - => [    1] = <NoName>        :RegParserItem[]  = \".\"\n"
		        + ". 00 - => [    1] = #Any            :<NoType>         = \".\"\n"
		        + "01 - - => [    3] = <NoName>        :RegParserItem[]  = \"\\\\s\"\n"
		        + ". 00 - => [    3] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . 00 => [    2] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 => [    3] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + "02 - - => [    5] = <NoName>        :RegParserItem[]  = \"\\\\D\"\n"
		        + ". 00 - => [    5] = #CharClass      :<NoType>         = \"\\\\D\"\n"
		        + ". . 00 => [    4] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 => [    5] = $NonDigit       :<NoType>         = \"D\"\n"
		        + "03 - - => [    6] = <NoName>        :RegParserItem[]  = \"\\\"\"\n"
		        + "04 - - => [    8] = <NoName>        :RegParserItem[]  = \"\\\\\\'\"\n"
		        + ". 00 - => [    8] = <NoName>        :Escape           = \"\\\\\\'\"\n"
		        + "05 - - => [   22] = <NoName>        :RegParserItem[]  = \"[:WhiteSpace:]\"\n"
		        + ". 00 - => [   22] = #CharClass      :<NoType>         = \"[:WhiteSpace:]\"\n"
		        + ". . 00 => [   10] = <NoName>        :<NoType>         = \"[:\"\n"
		        + ". . 01 => [   20] = $WhiteSpace     :<NoType>         = \"WhiteSpace\"\n"
		        + ". . 02 => [   22] = <NoName>        :<NoType>         = \":]\"\n"
		        + "06 - - => [   24] = <NoName>        :RegParserItem[]  = \"\\\\.\"\n"
		        + ". 00 - => [   24] = <NoName>        :Escape           = \"\\\\.\"\n"
		        + "07 - - => [   26] = <NoName>        :RegParserItem[]  = \"\\\\\\\\\"\n"
		        + ". 00 - => [   26] = <NoName>        :Escape           = \"\\\\\\\\\"\n"
		        + "08 - - => [   35] = <NoName>        :RegParserItem[]  = \"\\\\p{Blank}\"\n"
		        + ". 00 - => [   35] = #CharClass      :<NoType>         = \"\\\\p{Blank}\"\n"
		        + ". . 00 => [   29] = <NoName>        :<NoType>         = \"\\\\p{\"\n"
		        + ". . 01 => [   34] = $Blank          :<NoType>         = \"Blank\"\n"
		        + ". . 02 => [   35] = <NoName>        :<NoType>         = \"}\"",
		        result);
		
		result = parser.match(".\\s\\045\\x45\\u0045[a-g]s\\p{Blank}", typeProvider);
		validate("\n"
		        + "00 - - - => [    1] = <NoName>        :RegParserItem[]  = \".\"\n"
		        + ". 00 - - => [    1] = #Any            :<NoType>         = \".\"\n"
		        + "01 - - - => [    3] = <NoName>        :RegParserItem[]  = \"\\\\s\"\n"
		        + ". 00 - - => [    3] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . 00 - => [    2] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 - => [    3] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + "02 - - - => [    7] = <NoName>        :RegParserItem[]  = \"\\\\045\"\n"
		        + ". 00 - - => [    7] = <NoName>        :EscapeOct        = \"\\\\045\"\n"
		        + "03 - - - => [   11] = <NoName>        :RegParserItem[]  = \"\\\\x45\"\n"
		        + ". 00 - - => [   11] = <NoName>        :EscapeHex        = \"\\\\x45\"\n"
		        + "04 - - - => [   17] = <NoName>        :RegParserItem[]  = \"\\\\u0045\"\n"
		        + ". 00 - - => [   17] = <NoName>        :EscapeUnicode    = \"\\\\u0045\"\n"
		        + "05 - - - => [   22] = <NoName>        :RegParserItem[]  = \"[a-g]\"\n"
		        + ". 00 - - => [   22] = <NoName>        :CharSetItem      = \"[a-g]\"\n"
		        + ". . 00 - => [   18] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . 01 - => [   21] = #Range          :Range            = \"a-g\"\n"
		        + ". . . 00 => [   19] = #Start          :<NoType>         = \"a\"\n"
		        + ". . . 01 => [   20] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . 02 => [   21] = #End            :<NoType>         = \"g\"\n"
		        + ". . 02 - => [   22] = <NoName>        :<NoType>         = \"]\"\n"
		        + "06 - - - => [   23] = <NoName>        :RegParserItem[]  = \"s\"\n"
		        + "07 - - - => [   32] = <NoName>        :RegParserItem[]  = \"\\\\p{Blank}\"\n"
		        + ". 00 - - => [   32] = #CharClass      :<NoType>         = \"\\\\p{Blank}\"\n"
		        + ". . 00 - => [   26] = <NoName>        :<NoType>         = \"\\\\p{\"\n"
		        + ". . 01 - => [   31] = $Blank          :<NoType>         = \"Blank\"\n"
		        + ". . 02 - => [   32] = <NoName>        :<NoType>         = \"}\"",
		        result);
	}
	
	@Test
	public void testParseQunatifier() {
		var parser = newRegParser(RPQuantifierParserType.typeRef, OneOrMore, Any, ZeroOrMore);
		validate("(!Quantifier!)+.*", parser);
		
		var result = parser.match("?+*{54}{5,}*{,7}+{12,65}{ 1 }{ 4 , }{ , 8}{2,6}*", typeProvider);
		validate("\n"
		        + "00 - - => [    2] = <NoName>        :Quantifier       = \"?+\"\n"
		        + ". 00 - => [    1] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 01 - => [    2] = #Greediness     :<NoType>         = \"+\"\n"
		        + "01 - - => [    3] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". 00 - => [    3] = #Quantifier     :<NoType>         = \"*\"\n"
		        + "02 - - => [    7] = <NoName>        :Quantifier       = \"{54}\"\n"
		        + ". 00 - => [    7] = #Quantifier     :<NoType>         = \"{54}\"\n"
		        + ". . 00 => [    4] = <NoName>        :<NoType>         = \"{\"\n"
		        + ". . 01 => [    6] = #BothBound      :<NoType>         = \"54\"\n"
		        + ". . 02 => [    7] = <NoName>        :<NoType>         = \"}\"\n"
		        + "03 - - => [   12] = <NoName>        :Quantifier       = \"{5,}*\"\n"
		        + ". 00 - => [   11] = #Quantifier     :<NoType>         = \"{5,}\"\n"
		        + ". . 00 => [    8] = <NoName>        :<NoType>         = \"{\"\n"
		        + ". . 01 => [    9] = #LowerBound     :<NoType>         = \"5\"\n"
		        + ". . 02 => [   11] = <NoName>        :<NoType>         = \",}\"\n"
		        + ". 01 - => [   12] = #Greediness     :<NoType>         = \"*\"\n"
		        + "04 - - => [   17] = <NoName>        :Quantifier       = \"{,7}+\"\n"
		        + ". 00 - => [   16] = #Quantifier     :<NoType>         = \"{,7}\"\n"
		        + ". . 00 => [   14] = <NoName>        :<NoType>         = \"{,\"\n"
		        + ". . 01 => [   15] = #UpperBound     :<NoType>         = \"7\"\n"
		        + ". . 02 => [   16] = <NoName>        :<NoType>         = \"}\"\n"
		        + ". 01 - => [   17] = #Greediness     :<NoType>         = \"+\"\n"
		        + "05 - - => [   24] = <NoName>        :Quantifier       = \"{12,65}\"\n"
		        + ". 00 - => [   24] = #Quantifier     :<NoType>         = \"{12,65}\"\n"
		        + ". . 00 => [   18] = <NoName>        :<NoType>         = \"{\"\n"
		        + ". . 01 => [   20] = #LowerBound     :<NoType>         = \"12\"\n"
		        + ". . 02 => [   21] = <NoName>        :<NoType>         = \",\"\n"
		        + ". . 03 => [   23] = #UpperBound     :<NoType>         = \"65\"\n"
		        + ". . 04 => [   24] = <NoName>        :<NoType>         = \"}\"\n"
		        + "06 - - => [   29] = <NoName>        :Quantifier       = \"{ 1 }\"\n"
		        + ". 00 - => [   29] = #Quantifier     :<NoType>         = \"{ 1 }\"\n"
		        + ". . 00 => [   26] = <NoName>        :<NoType>         = \"{ \"\n"
		        + ". . 01 => [   27] = #BothBound      :<NoType>         = \"1\"\n"
		        + ". . 02 => [   29] = <NoName>        :<NoType>         = \" }\"\n"
		        + "07 - - => [   36] = <NoName>        :Quantifier       = \"{ 4 , }\"\n"
		        + ". 00 - => [   36] = #Quantifier     :<NoType>         = \"{ 4 , }\"\n"
		        + ". . 00 => [   31] = <NoName>        :<NoType>         = \"{ \"\n"
		        + ". . 01 => [   32] = #LowerBound     :<NoType>         = \"4\"\n"
		        + ". . 02 => [   36] = <NoName>        :<NoType>         = \" , }\"\n"
		        + "08 - - => [   42] = <NoName>        :Quantifier       = \"{ , 8}\"\n"
		        + ". 00 - => [   42] = #Quantifier     :<NoType>         = \"{ , 8}\"\n"
		        + ". . 00 => [   40] = <NoName>        :<NoType>         = \"{ , \"\n"
		        + ". . 01 => [   41] = #UpperBound     :<NoType>         = \"8\"\n"
		        + ". . 02 => [   42] = <NoName>        :<NoType>         = \"}\"\n"
		        + "09 - - => [   48] = <NoName>        :Quantifier       = \"{2,6}*\"\n"
		        + ". 00 - => [   47] = #Quantifier     :<NoType>         = \"{2,6}\"\n"
		        + ". . 00 => [   43] = <NoName>        :<NoType>         = \"{\"\n"
		        + ". . 01 => [   44] = #LowerBound     :<NoType>         = \"2\"\n"
		        + ". . 02 => [   45] = <NoName>        :<NoType>         = \",\"\n"
		        + ". . 03 => [   46] = #UpperBound     :<NoType>         = \"6\"\n"
		        + ". . 04 => [   47] = <NoName>        :<NoType>         = \"}\"\n"
		        + ". 01 - => [   48] = #Greediness     :<NoType>         = \"*\"",
		        result);
	}
	
	@Test
	public void testParseCharSet() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTCharSetItem.Name), OneOrMore, Any, ZeroOrMore);
		validate("(!CharSetItem!)+.*", parser);
		
		var result = parser.match("[a-bg-h.gfj\\s\\u0035-c\\s[:Any:][:Digit:]g\\jp{ASCII}]", typeProvider);
		validate("\n"
		        + "00 - - - => [   51] = <NoName>        :CharSetItem      = \"[a-bg-h.gfj\\\\s\\\\u0035-c\\\\s[:Any:][:Digit:]g\\\\jp{ASCII}]\"\n"
		        + ". 00 - - => [    1] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". 01 - - => [    4] = #Range          :Range            = \"a-b\"\n"
		        + ". . 00 - => [    2] = #Start          :<NoType>         = \"a\"\n"
		        + ". . 01 - => [    3] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . 02 - => [    4] = #End            :<NoType>         = \"b\"\n"
		        + ". 02 - - => [    7] = #Range          :Range            = \"g-h\"\n"
		        + ". . 00 - => [    5] = #Start          :<NoType>         = \"g\"\n"
		        + ". . 01 - => [    6] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . 02 - => [    7] = #End            :<NoType>         = \"h\"\n"
		        + ". 03 - - => [    8] = #Any            :<NoType>         = \".\"\n"
		        + ". 04 - - => [    9] = #Range          :Range            = \"g\"\n"
		        + ". . 00 - => [    9] = #Start          :<NoType>         = \"g\"\n"
		        + ". 05 - - => [   10] = #Range          :Range            = \"f\"\n"
		        + ". . 00 - => [   10] = #Start          :<NoType>         = \"f\"\n"
		        + ". 06 - - => [   11] = #Range          :Range            = \"j\"\n"
		        + ". . 00 - => [   11] = #Start          :<NoType>         = \"j\"\n"
		        + ". 07 - - => [   13] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . 00 - => [   12] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 - => [   13] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + ". 08 - - => [   21] = #Range          :Range            = \"\\\\u0035-c\"\n"
		        + ". . 00 - => [   19] = #Start          :<NoType>         = \"\\\\u0035\"\n"
		        + ". . . 00 => [   19] = <NoName>        :EscapeUnicode    = \"\\\\u0035\"\n"
		        + ". . 01 - => [   20] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . 02 - => [   21] = #End            :<NoType>         = \"c\"\n"
		        + ". 09 - - => [   23] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . 00 - => [   22] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . 01 - => [   23] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + ". 10 - - => [   30] = #CharClass      :<NoType>         = \"[:Any:]\"\n"
		        + ". . 00 - => [   25] = <NoName>        :<NoType>         = \"[:\"\n"
		        + ". . 01 - => [   28] = $Any            :<NoType>         = \"Any\"\n"
		        + ". . 02 - => [   30] = <NoName>        :<NoType>         = \":]\"\n"
		        + ". 11 - - => [   39] = #CharClass      :<NoType>         = \"[:Digit:]\"\n"
		        + ". . 00 - => [   32] = <NoName>        :<NoType>         = \"[:\"\n"
		        + ". . 01 - => [   37] = $Digit          :<NoType>         = \"Digit\"\n"
		        + ". . 02 - => [   39] = <NoName>        :<NoType>         = \":]\"\n"
		        + ". 12 - - => [   40] = #Range          :Range            = \"g\"\n"
		        + ". . 00 - => [   40] = #Start          :<NoType>         = \"g\"\n"
		        + ". 13 - - => [   50] = #CharClass      :<NoType>         = \"\\\\jp{ASCII}\"\n"
		        + ". . 00 - => [   44] = <NoName>        :<NoType>         = \"\\\\jp{\"\n"
		        + ". . 01 - => [   49] = $JASCII         :<NoType>         = \"ASCII\"\n"
		        + ". . 02 - => [   50] = <NoName>        :<NoType>         = \"}\"\n"
		        + ". 14 - - => [   51] = <NoName>        :<NoType>         = \"]\"",
		        result);
		
		result = parser.match("[a[g-h]b]", typeProvider);
		validate("\n"
		        + "00 - - - => [    9] = <NoName>        :CharSetItem      = \"[a[g-h]b]\"\n"
		        + ". 00 - - => [    1] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". 01 - - => [    2] = #Range          :Range            = \"a\"\n"
		        + ". . 00 - => [    2] = #Start          :<NoType>         = \"a\"\n"
		        + ". 02 - - => [    7] = <NoName>        :CharSetItem      = \"[g-h]\"\n"
		        + ". . 00 - => [    3] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . 01 - => [    6] = #Range          :Range            = \"g-h\"\n"
		        + ". . . 00 => [    4] = #Start          :<NoType>         = \"g\"\n"
		        + ". . . 01 => [    5] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . 02 => [    6] = #End            :<NoType>         = \"h\"\n"
		        + ". . 02 - => [    7] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 03 - - => [    8] = #Range          :Range            = \"b\"\n"
		        + ". . 00 - => [    8] = #Start          :<NoType>         = \"b\"\n"
		        + ". 04 - - => [    9] = <NoName>        :<NoType>         = \"]\"",
		        result);
		
		result = parser.match("[a[g-h]b]&&[d-h]&&[\\s]", typeProvider);
		validate("\n"
		        + "00 - - - => [   22] = <NoName>        :CharSetItem      = \"[a[g-h]b]&&[d-h]&&[\\\\s]\"\n"
		        + ". 00 - - => [    1] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". 01 - - => [    2] = #Range          :Range            = \"a\"\n"
		        + ". . 00 - => [    2] = #Start          :<NoType>         = \"a\"\n"
		        + ". 02 - - => [    7] = <NoName>        :CharSetItem      = \"[g-h]\"\n"
		        + ". . 00 - => [    3] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . 01 - => [    6] = #Range          :Range            = \"g-h\"\n"
		        + ". . . 00 => [    4] = #Start          :<NoType>         = \"g\"\n"
		        + ". . . 01 => [    5] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . 02 => [    6] = #End            :<NoType>         = \"h\"\n"
		        + ". . 02 - => [    7] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 03 - - => [    8] = #Range          :Range            = \"b\"\n"
		        + ". . 00 - => [    8] = #Start          :<NoType>         = \"b\"\n"
		        + ". 04 - - => [    9] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 05 - - => [   11] = #Intersect      :<NoType>         = \"&&\"\n"
		        + ". 06 - - => [   16] = #Set            :<NoType>         = \"[d-h]\"\n"
		        + ". . 00 - => [   12] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . 01 - => [   15] = #Range          :Range            = \"d-h\"\n"
		        + ". . . 00 => [   13] = #Start          :<NoType>         = \"d\"\n"
		        + ". . . 01 => [   14] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . 02 => [   15] = #End            :<NoType>         = \"h\"\n"
		        + ". . 02 - => [   16] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 07 - - => [   18] = #Intersect      :<NoType>         = \"&&\"\n"
		        + ". 08 - - => [   22] = #Set            :<NoType>         = \"[\\\\s]\"\n"
		        + ". . 00 - => [   19] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . 01 - => [   21] = #CharClass      :<NoType>         = \"\\\\s\"\n"
		        + ". . . 00 => [   20] = <NoName>        :<NoType>         = \"\\\\\"\n"
		        + ". . . 01 => [   21] = $WhiteSpace     :<NoType>         = \"s\"\n"
		        + ". . 02 - => [   22] = <NoName>        :<NoType>         = \"]\"", result);
		validate("[a[g-h]b]&&[d-h]&&[\\s]", result.textOf(0));
	}
	
	@Test
	public void testAlternative() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.match("ab(c.d)o(i|o)o\\)e[h-g]u\\(f", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   26] = <NoName>        :RegParser        = \"ab(c.d)o(i|o)o\\\\)e[h-g]u\\\\(f\"\n"
		        + ". 00 - - - - => [    2] = #Item[]         :RegParserItem[]  = \"ab\"\n"
		        + ". 01 - - - - => [    7] = #Item[]         :RegParserItem[]  = \"(c.d)\"\n"
		        + ". . 00 - - - => [    7] = #Group          :<NoType>         = \"(c.d)\"\n"
		        + ". . . 00 - - => [    3] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - => [    6] = <NoName>        :RegParser        = \"c.d\"\n"
		        + ". . . . 00 - => [    4] = #Item[]         :RegParserItem[]  = \"c\"\n"
		        + ". . . . 01 - => [    5] = #Item[]         :RegParserItem[]  = \".\"\n"
		        + ". . . . . 00 => [    5] = #Any            :<NoType>         = \".\"\n"
		        + ". . . . 02 - => [    6] = #Item[]         :RegParserItem[]  = \"d\"\n"
		        + ". . . 02 - - => [    7] = <NoName>        :<NoType>         = \")\"\n"
		        + ". 02 - - - - => [    8] = #Item[]         :RegParserItem[]  = \"o\"\n"
		        + ". 03 - - - - => [   13] = #Item[]         :RegParserItem[]  = \"(i|o)\"\n"
		        + ". . 00 - - - => [   13] = #Group          :<NoType>         = \"(i|o)\"\n"
		        + ". . . 00 - - => [    9] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - => [   10] = <NoName>        :RegParser        = \"i\"\n"
		        + ". . . . 00 - => [   10] = #Item[]         :RegParserItem[]  = \"i\"\n"
		        + ". . . 02 - - => [   11] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . 03 - - => [   12] = <NoName>        :RegParser        = \"o\"\n"
		        + ". . . . 00 - => [   12] = #Item[]         :RegParserItem[]  = \"o\"\n"
		        + ". . . 04 - - => [   13] = <NoName>        :<NoType>         = \")\"\n"
		        + ". 04 - - - - => [   14] = #Item[]         :RegParserItem[]  = \"o\"\n"
		        + ". 05 - - - - => [   16] = #Item[]         :RegParserItem[]  = \"\\\\)\"\n"
		        + ". . 00 - - - => [   16] = <NoName>        :Escape           = \"\\\\)\"\n"
		        + ". 06 - - - - => [   17] = #Item[]         :RegParserItem[]  = \"e\"\n"
		        + ". 07 - - - - => [   22] = #Item[]         :RegParserItem[]  = \"[h-g]\"\n"
		        + ". . 00 - - - => [   22] = <NoName>        :CharSetItem      = \"[h-g]\"\n"
		        + ". . . 00 - - => [   18] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . 01 - - => [   21] = #Range          :Range            = \"h-g\"\n"
		        + ". . . . 00 - => [   19] = #Start          :<NoType>         = \"h\"\n"
		        + ". . . . 01 - => [   20] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . 02 - => [   21] = #End            :<NoType>         = \"g\"\n"
		        + ". . . 02 - - => [   22] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 08 - - - - => [   23] = #Item[]         :RegParserItem[]  = \"u\"\n"
		        + ". 09 - - - - => [   25] = #Item[]         :RegParserItem[]  = \"\\\\(\"\n"
		        + ". . 00 - - - => [   25] = <NoName>        :Escape           = \"\\\\(\"\n"
		        + ". 10 - - - - => [   26] = #Item[]         :RegParserItem[]  = \"f\"",
		        result);
		
		result = parser.match("Col(o|ou)?r", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   11] = <NoName>        :RegParser        = \"Col(o|ou)?r\"\n"
		        + ". 00 - - - - => [    3] = #Item[]         :RegParserItem[]  = \"Col\"\n"
		        + ". 01 - - - - => [   10] = #ItemQuantifier :<NoType>         = \"(o|ou)?\"\n"
		        + ". . 00 - - - => [    9] = <NoName>        :RegParserItem[]  = \"(o|ou)\"\n"
		        + ". . . 00 - - => [    9] = #Group          :<NoType>         = \"(o|ou)\"\n"
		        + ". . . . 00 - => [    4] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 - => [    5] = <NoName>        :RegParser        = \"o\"\n"
		        + ". . . . . 00 => [    5] = #Item[]         :RegParserItem[]  = \"o\"\n"
		        + ". . . . 02 - => [    6] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . . 03 - => [    8] = <NoName>        :RegParser        = \"ou\"\n"
		        + ". . . . . 00 => [    8] = #Item[]         :RegParserItem[]  = \"ou\"\n"
		        + ". . . . 04 - => [    9] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - - => [   10] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - => [   10] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 02 - - - - => [   11] = #Item[]         :RegParserItem[]  = \"r\"",
		        result);
		
		result = parser.match("Col(^o|ou|au||[aeiou])?r[^0-9]?s", typeProvider);
		validate("\n"
		        + "00 - - - - - - - - => [   32] = <NoName>        :RegParser        = \"Col(^o|ou|au||[aeiou])?r[^0-9]?s\"\n"
		        + ". 00 - - - - - - - => [    3] = #Item[]         :RegParserItem[]  = \"Col\"\n"
		        + ". 01 - - - - - - - => [   23] = #ItemQuantifier :<NoType>         = \"(^o|ou|au||[aeiou])?\"\n"
		        + ". . 00 - - - - - - => [   22] = <NoName>        :RegParserItem[]  = \"(^o|ou|au||[aeiou])\"\n"
		        + ". . . 00 - - - - - => [   22] = #Group          :<NoType>         = \"(^o|ou|au||[aeiou])\"\n"
		        + ". . . . 00 - - - - => [    4] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 - - - - => [    5] = #NOT            :<NoType>         = \"^\"\n"
		        + ". . . . 02 - - - - => [    6] = <NoName>        :RegParser        = \"o\"\n"
		        + ". . . . . 00 - - - => [    6] = #Item[]         :RegParserItem[]  = \"o\"\n"
		        + ". . . . 03 - - - - => [    7] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . . 04 - - - - => [    9] = <NoName>        :RegParser        = \"ou\"\n"
		        + ". . . . . 00 - - - => [    9] = #Item[]         :RegParserItem[]  = \"ou\"\n"
		        + ". . . . 05 - - - - => [   10] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . . 06 - - - - => [   12] = <NoName>        :RegParser        = \"au\"\n"
		        + ". . . . . 00 - - - => [   12] = #Item[]         :RegParserItem[]  = \"au\"\n"
		        + ". . . . 07 - - - - => [   14] = #Default        :<NoType>         = \"||\"\n"
		        + ". . . . 08 - - - - => [   21] = <NoName>        :RegParser        = \"[aeiou]\"\n"
		        + ". . . . . 00 - - - => [   21] = #Item[]         :RegParserItem[]  = \"[aeiou]\"\n"
		        + ". . . . . . 00 - - => [   21] = <NoName>        :CharSetItem      = \"[aeiou]\"\n"
		        + ". . . . . . . 00 - => [   15] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . . 01 - => [   16] = #Range          :Range            = \"a\"\n"
		        + ". . . . . . . . 00 => [   16] = #Start          :<NoType>         = \"a\"\n"
		        + ". . . . . . . 02 - => [   17] = #Range          :Range            = \"e\"\n"
		        + ". . . . . . . . 00 => [   17] = #Start          :<NoType>         = \"e\"\n"
		        + ". . . . . . . 03 - => [   18] = #Range          :Range            = \"i\"\n"
		        + ". . . . . . . . 00 => [   18] = #Start          :<NoType>         = \"i\"\n"
		        + ". . . . . . . 04 - => [   19] = #Range          :Range            = \"o\"\n"
		        + ". . . . . . . . 00 => [   19] = #Start          :<NoType>         = \"o\"\n"
		        + ". . . . . . . 05 - => [   20] = #Range          :Range            = \"u\"\n"
		        + ". . . . . . . . 00 => [   20] = #Start          :<NoType>         = \"u\"\n"
		        + ". . . . . . . 06 - => [   21] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 09 - - - - => [   22] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - - - - - => [   23] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - - - - => [   23] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 02 - - - - - - - => [   24] = #Item[]         :RegParserItem[]  = \"r\"\n"
		        + ". 03 - - - - - - - => [   31] = #ItemQuantifier :<NoType>         = \"[^0-9]?\"\n"
		        + ". . 00 - - - - - - => [   30] = <NoName>        :RegParserItem[]  = \"[^0-9]\"\n"
		        + ". . . 00 - - - - - => [   30] = <NoName>        :CharSetItem      = \"[^0-9]\"\n"
		        + ". . . . 00 - - - - => [   25] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . 01 - - - - => [   26] = #NOT            :<NoType>         = \"^\"\n"
		        + ". . . . 02 - - - - => [   29] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . 00 - - - => [   27] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . 01 - - - => [   28] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . 02 - - - => [   29] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . 03 - - - - => [   30] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . 01 - - - - - - => [   31] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - - - - => [   31] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 04 - - - - - - - => [   32] = #Item[]         :RegParserItem[]  = \"s\"",
		        result);
	}
	
	@Test
	public void testNamed() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.match("var\\ (#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\\ =\\ (#Value:~[0-9]*~);", typeProvider);
		validate("\n"
		        + "00 - - - - - - - - => [   60] = <NoName>        :RegParser        = \"var\\\\ (#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\\\\ =\\\\ (#Value:~[0-9]*~);\"\n"
		        + ". 00 - - - - - - - => [    3] = #Item[]         :RegParserItem[]  = \"var\"\n"
		        + ". 01 - - - - - - - => [    5] = #Item[]         :RegParserItem[]  = \"\\\\ \"\n"
		        + ". . 00 - - - - - - => [    5] = <NoName>        :Escape           = \"\\\\ \"\n"
		        + ". 02 - - - - - - - => [   37] = #Item[]         :RegParserItem[]  = \"(#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\"\n"
		        + ". . 00 - - - - - - => [   37] = #Group          :<NoType>         = \"(#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\"\n"
		        + ". . . 00 - - - - - => [    6] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [    7] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [   11] = #Group-Name     :$Identifier      = \"Name\"\n"
		        + ". . . 03 - - - - - => [   12] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . 04 - - - - - => [   13] = <NoName>        :<NoType>         = \"~\"\n"
		        + ". . . 05 - - - - - => [   35] = #GroupRegParser :RegParser        = \"[a-zA-Z_][a-zA-Z_0-9]*\"\n"
		        + ". . . . 00 - - - - => [   22] = #Item[]         :RegParserItem[]  = \"[a-zA-Z_]\"\n"
		        + ". . . . . 00 - - - => [   22] = <NoName>        :CharSetItem      = \"[a-zA-Z_]\"\n"
		        + ". . . . . . 00 - - => [   14] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - - => [   17] = #Range          :Range            = \"a-z\"\n"
		        + ". . . . . . . 00 - => [   15] = #Start          :<NoType>         = \"a\"\n"
		        + ". . . . . . . 01 - => [   16] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 - => [   17] = #End            :<NoType>         = \"z\"\n"
		        + ". . . . . . 02 - - => [   20] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . 00 - => [   18] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . 01 - => [   19] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 - => [   20] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . 03 - - => [   21] = #Range          :Range            = \"_\"\n"
		        + ". . . . . . . 00 - => [   21] = #Start          :<NoType>         = \"_\"\n"
		        + ". . . . . . 04 - - => [   22] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 01 - - - - => [   35] = #ItemQuantifier :<NoType>         = \"[a-zA-Z_0-9]*\"\n"
		        + ". . . . . 00 - - - => [   34] = <NoName>        :RegParserItem[]  = \"[a-zA-Z_0-9]\"\n"
		        + ". . . . . . 00 - - => [   34] = <NoName>        :CharSetItem      = \"[a-zA-Z_0-9]\"\n"
		        + ". . . . . . . 00 - => [   23] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . . 01 - => [   26] = #Range          :Range            = \"a-z\"\n"
		        + ". . . . . . . . 00 => [   24] = #Start          :<NoType>         = \"a\"\n"
		        + ". . . . . . . . 01 => [   25] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   26] = #End            :<NoType>         = \"z\"\n"
		        + ". . . . . . . 02 - => [   29] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . . 00 => [   27] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . . 01 => [   28] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   29] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . . 03 - => [   30] = #Range          :Range            = \"_\"\n"
		        + ". . . . . . . . 00 => [   30] = #Start          :<NoType>         = \"_\"\n"
		        + ". . . . . . . 04 - => [   33] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . . 00 => [   31] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . . 01 => [   32] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   33] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . . 05 - => [   34] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . . 01 - - - => [   35] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . . . . 00 - - => [   35] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 06 - - - - - => [   37] = <NoName>        :<NoType>         = \"~)\"\n"
		        + ". 03 - - - - - - - => [   39] = #Item[]         :RegParserItem[]  = \"\\\\ \"\n"
		        + ". . 00 - - - - - - => [   39] = <NoName>        :Escape           = \"\\\\ \"\n"
		        + ". 04 - - - - - - - => [   40] = #Item[]         :RegParserItem[]  = \"=\"\n"
		        + ". 05 - - - - - - - => [   42] = #Item[]         :RegParserItem[]  = \"\\\\ \"\n"
		        + ". . 00 - - - - - - => [   42] = <NoName>        :Escape           = \"\\\\ \"\n"
		        + ". 06 - - - - - - - => [   59] = #Item[]         :RegParserItem[]  = \"(#Value:~[0-9]*~)\"\n"
		        + ". . 00 - - - - - - => [   59] = #Group          :<NoType>         = \"(#Value:~[0-9]*~)\"\n"
		        + ". . . 00 - - - - - => [   43] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [   44] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [   49] = #Group-Name     :$Identifier      = \"Value\"\n"
		        + ". . . 03 - - - - - => [   50] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . 04 - - - - - => [   51] = <NoName>        :<NoType>         = \"~\"\n"
		        + ". . . 05 - - - - - => [   57] = #GroupRegParser :RegParser        = \"[0-9]*\"\n"
		        + ". . . . 00 - - - - => [   57] = #ItemQuantifier :<NoType>         = \"[0-9]*\"\n"
		        + ". . . . . 00 - - - => [   56] = <NoName>        :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . . 00 - - => [   56] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . . 00 - => [   52] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . . 01 - => [   55] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . . 00 => [   53] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . . 01 => [   54] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   55] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . . 02 - => [   56] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . . 01 - - - => [   57] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . . . . 00 - - => [   57] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 06 - - - - - => [   59] = <NoName>        :<NoType>         = \"~)\"\n"
		        + ". 07 - - - - - - - => [   60] = #Item[]         :RegParserItem[]  = \";\"", result);
		
		result = parser.match("a(#Value:!byte()!)?a", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   20] = <NoName>        :RegParser        = \"a(#Value:!byte()!)?a\"\n"
		        + ". 00 - - - - => [    1] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". 01 - - - - => [   19] = #ItemQuantifier :<NoType>         = \"(#Value:!byte()!)?\"\n"
		        + ". . 00 - - - => [   18] = <NoName>        :RegParserItem[]  = \"(#Value:!byte()!)\"\n"
		        + ". . . 00 - - => [   18] = #Group          :<NoType>         = \"(#Value:!byte()!)\"\n"
		        + ". . . . 00 - => [    2] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 - => [    3] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . . 02 - => [    8] = #Group-Name     :$Identifier      = \"Value\"\n"
		        + ". . . . 03 - => [    9] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . . 04 - => [   17] = #Type           :Type             = \"!byte()!\"\n"
		        + ". . . . . 00 => [   10] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". . . . . 01 => [   14] = #TypeName       :$Identifier      = \"byte\"\n"
		        + ". . . . . 02 => [   16] = #Param          :<NoType>         = \"()\"\n"
		        + ". . . . . 03 => [   17] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". . . . 05 - => [   18] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - - => [   19] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - => [   19] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 02 - - - - => [   20] = #Item[]         :RegParserItem[]  = \"a\"",
		        result);
	}
	
	@Test
	public void testIncomplete() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.parse("a(a)s)?a", typeProvider);
		validate(5, result.endPosition());
		
		validate("\n"
		        + "00 - - - - => [    5] = <NoName>        :RegParser        = \"a(a)s\"\n"
		        + ". 00 - - - => [    1] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". 01 - - - => [    4] = #Item[]         :RegParserItem[]  = \"(a)\"\n"
		        + ". . 00 - - => [    4] = #Group          :<NoType>         = \"(a)\"\n"
		        + ". . . 00 - => [    2] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - => [    3] = <NoName>        :RegParser        = \"a\"\n"
		        + ". . . . 00 => [    3] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". . . 02 - => [    4] = <NoName>        :<NoType>         = \")\"\n"
		        + ". 02 - - - => [    5] = #Item[]         :RegParserItem[]  = \"s\"",
		        result);
	}
	
	@Test
	public void testError() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.parse("a(a(s)?a", typeProvider);
		validate(8, result.endPosition());
		validate("\n"
		        + "00 - - - - => [    8] = <NoName>        :RegParser        = \"a(a(s)?a\"\n"
		        + ". 00 - - - => [    1] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". 01 - - - => [    7] = #ItemQuantifier :<NoType>         = \"(a(s)?\"\n"
		        + ". . 00 - - => [    6] = <NoName>        :RegParserItem[]  = \"(a(s)\"\n"
		        + ". . . 00 - => [    6] = #Group          :<NoType>         = \"(a(s)\"\n"
		        + ". . . . 00 => [    2] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 => [    5] = #Error[]        :<NoType>         = \"a(s\"\n"
		        + ". . . . 02 => [    6] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - => [    7] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - => [    7] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 02 - - - => [    8] = #Item[]         :RegParserItem[]  = \"a\"",
		        result);
	}
	
	@Test
	public void testOthers() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		
		var result = parser.match("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])", typeProvider);
		validate("\n"
		        + "00 - - - - - - - => [   50] = <NoName>        :RegParser        = \"([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\"\n"
		        + ". 00 - - - - - - => [   50] = #Item[]         :RegParserItem[]  = \"([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\"\n"
		        + ". . 00 - - - - - => [   50] = #Group          :<NoType>         = \"([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\"\n"
		        + ". . . 00 - - - - => [    1] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - => [    6] = <NoName>        :RegParser        = \"[0-9]\"\n"
		        + ". . . . 00 - - - => [    6] = #Item[]         :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . 00 - - => [    6] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . 00 - => [    2] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [    5] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . 00 => [    3] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [    4] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [    5] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [    6] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . 02 - - - - => [    7] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . 03 - - - - => [   17] = <NoName>        :RegParser        = \"[1-9][0-9]\"\n"
		        + ". . . . 00 - - - => [   12] = #Item[]         :RegParserItem[]  = \"[1-9]\"\n"
		        + ". . . . . 00 - - => [   12] = <NoName>        :CharSetItem      = \"[1-9]\"\n"
		        + ". . . . . . 00 - => [    8] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   11] = #Range          :Range            = \"1-9\"\n"
		        + ". . . . . . . 00 => [    9] = #Start          :<NoType>         = \"1\"\n"
		        + ". . . . . . . 01 => [   10] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   11] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [   12] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 01 - - - => [   17] = #Item[]         :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . 00 - - => [   17] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . 00 - => [   13] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   16] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . 00 => [   14] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   15] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   16] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [   17] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . 04 - - - - => [   18] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . 05 - - - - => [   29] = <NoName>        :RegParser        = \"1[0-9][0-9]\"\n"
		        + ". . . . 00 - - - => [   19] = #Item[]         :RegParserItem[]  = \"1\"\n"
		        + ". . . . 01 - - - => [   24] = #Item[]         :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . 00 - - => [   24] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . 00 - => [   20] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   23] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . 00 => [   21] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   22] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   23] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [   24] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 02 - - - => [   29] = #Item[]         :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . 00 - - => [   29] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . 00 - => [   25] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   28] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . 00 => [   26] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   27] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   28] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [   29] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . 06 - - - - => [   30] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . 07 - - - - => [   41] = <NoName>        :RegParser        = \"2[0-4][0-9]\"\n"
		        + ". . . . 00 - - - => [   31] = #Item[]         :RegParserItem[]  = \"2\"\n"
		        + ". . . . 01 - - - => [   36] = #Item[]         :RegParserItem[]  = \"[0-4]\"\n"
		        + ". . . . . 00 - - => [   36] = <NoName>        :CharSetItem      = \"[0-4]\"\n"
		        + ". . . . . . 00 - => [   32] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   35] = #Range          :Range            = \"0-4\"\n"
		        + ". . . . . . . 00 => [   33] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   34] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   35] = #End            :<NoType>         = \"4\"\n"
		        + ". . . . . . 02 - => [   36] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 02 - - - => [   41] = #Item[]         :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . . . 00 - - => [   41] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . . . 00 - => [   37] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   40] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . 00 => [   38] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   39] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   40] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . 02 - => [   41] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . 08 - - - - => [   42] = #OR             :<NoType>         = \"|\"\n"
		        + ". . . 09 - - - - => [   49] = <NoName>        :RegParser        = \"25[0-5]\"\n"
		        + ". . . . 00 - - - => [   44] = #Item[]         :RegParserItem[]  = \"25\"\n"
		        + ". . . . 01 - - - => [   49] = #Item[]         :RegParserItem[]  = \"[0-5]\"\n"
		        + ". . . . . 00 - - => [   49] = <NoName>        :CharSetItem      = \"[0-5]\"\n"
		        + ". . . . . . 00 - => [   45] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - => [   48] = #Range          :Range            = \"0-5\"\n"
		        + ". . . . . . . 00 => [   46] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . 01 => [   47] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 => [   48] = #End            :<NoType>         = \"5\"\n"
		        + ". . . . . . 02 - => [   49] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . 10 - - - - => [   50] = <NoName>        :<NoType>         = \")\"",
		        result);
		
		result = parser.match("[0-9]{1,3}", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   10] = <NoName>        :RegParser        = \"[0-9]{1,3}\"\n"
		        + ". 00 - - - - => [   10] = #ItemQuantifier :<NoType>         = \"[0-9]{1,3}\"\n"
		        + ". . 00 - - - => [    5] = <NoName>        :RegParserItem[]  = \"[0-9]\"\n"
		        + ". . . 00 - - => [    5] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
		        + ". . . . 00 - => [    1] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . 01 - => [    4] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . 00 => [    2] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . 01 => [    3] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . 02 => [    4] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . 02 - => [    5] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . 01 - - - => [   10] = <NoName>        :Quantifier       = \"{1,3}\"\n"
		        + ". . . 00 - - => [   10] = #Quantifier     :<NoType>         = \"{1,3}\"\n"
		        + ". . . . 00 - => [    6] = <NoName>        :<NoType>         = \"{\"\n"
		        + ". . . . 01 - => [    7] = #LowerBound     :<NoType>         = \"1\"\n"
		        + ". . . . 02 - => [    8] = <NoName>        :<NoType>         = \",\"\n"
		        + ". . . . 03 - => [    9] = #UpperBound     :<NoType>         = \"3\"\n"
		        + ". . . . 04 - => [   10] = <NoName>        :<NoType>         = \"}\"",
		        result);
		
		result = parser.match("Set(Value)?", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   11] = <NoName>        :RegParser        = \"Set(Value)?\"\n"
		        + ". 00 - - - - => [    3] = #Item[]         :RegParserItem[]  = \"Set\"\n"
		        + ". 01 - - - - => [   11] = #ItemQuantifier :<NoType>         = \"(Value)?\"\n"
		        + ". . 00 - - - => [   10] = <NoName>        :RegParserItem[]  = \"(Value)\"\n"
		        + ". . . 00 - - => [   10] = #Group          :<NoType>         = \"(Value)\"\n"
		        + ". . . . 00 - => [    4] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 - => [    9] = <NoName>        :RegParser        = \"Value\"\n"
		        + ". . . . . 00 => [    9] = #Item[]         :RegParserItem[]  = \"Value\"\n"
		        + ". . . . 02 - => [   10] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - - => [   11] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - => [   11] = #Quantifier     :<NoType>         = \"?\"",
		        result);
		
		result = parser.match("<(#Term:~[A-Z][A-Z0-9]*~)[^>]*>.*?</(#Term;)>", typeProvider);
		validate(null, result);
		
		result = parser.match("<(#Term:~[A-Z][A-Z0-9]*~)[^>]*>.**</(#Term;)>", typeProvider);
		validate("\n"
		        + "00 - - - - - - - - => [   45] = <NoName>        :RegParser        = \"<(#Term:~[A-Z][A-Z0-9]*~)[^>]*>.**</(#Term;)>\"\n"
		        + ". 00 - - - - - - - => [    1] = #Item[]         :RegParserItem[]  = \"<\"\n"
		        + ". 01 - - - - - - - => [   25] = #Item[]         :RegParserItem[]  = \"(#Term:~[A-Z][A-Z0-9]*~)\"\n"
		        + ". . 00 - - - - - - => [   25] = #Group          :<NoType>         = \"(#Term:~[A-Z][A-Z0-9]*~)\"\n"
		        + ". . . 00 - - - - - => [    2] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [    3] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [    7] = #Group-Name     :$Identifier      = \"Term\"\n"
		        + ". . . 03 - - - - - => [    8] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . 04 - - - - - => [    9] = <NoName>        :<NoType>         = \"~\"\n"
		        + ". . . 05 - - - - - => [   23] = #GroupRegParser :RegParser        = \"[A-Z][A-Z0-9]*\"\n"
		        + ". . . . 00 - - - - => [   14] = #Item[]         :RegParserItem[]  = \"[A-Z]\"\n"
		        + ". . . . . 00 - - - => [   14] = <NoName>        :CharSetItem      = \"[A-Z]\"\n"
		        + ". . . . . . 00 - - => [   10] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - - => [   13] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . 00 - => [   11] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . 01 - => [   12] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 - => [   13] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . 02 - - => [   14] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 01 - - - - => [   23] = #ItemQuantifier :<NoType>         = \"[A-Z0-9]*\"\n"
		        + ". . . . . 00 - - - => [   22] = <NoName>        :RegParserItem[]  = \"[A-Z0-9]\"\n"
		        + ". . . . . . 00 - - => [   22] = <NoName>        :CharSetItem      = \"[A-Z0-9]\"\n"
		        + ". . . . . . . 00 - => [   15] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . . 01 - => [   18] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . . 00 => [   16] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . . 01 => [   17] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   18] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . . 02 - => [   21] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . . 00 => [   19] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . . 01 => [   20] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   21] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . . 03 - => [   22] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . . 01 - - - => [   23] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . . . . 00 - - => [   23] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 06 - - - - - => [   25] = <NoName>        :<NoType>         = \"~)\"\n"
		        + ". 02 - - - - - - - => [   30] = #ItemQuantifier :<NoType>         = \"[^>]*\"\n"
		        + ". . 00 - - - - - - => [   29] = <NoName>        :RegParserItem[]  = \"[^>]\"\n"
		        + ". . . 00 - - - - - => [   29] = <NoName>        :CharSetItem      = \"[^>]\"\n"
		        + ". . . . 00 - - - - => [   26] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . 01 - - - - => [   27] = #NOT            :<NoType>         = \"^\"\n"
		        + ". . . . 02 - - - - => [   28] = #Range          :Range            = \">\"\n"
		        + ". . . . . 00 - - - => [   28] = #Start          :<NoType>         = \">\"\n"
		        + ". . . . 03 - - - - => [   29] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . 01 - - - - - - => [   30] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . 00 - - - - - => [   30] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". 03 - - - - - - - => [   31] = #Item[]         :RegParserItem[]  = \">\"\n"
		        + ". 04 - - - - - - - => [   34] = #ItemQuantifier :<NoType>         = \".**\"\n"
		        + ". . 00 - - - - - - => [   32] = <NoName>        :RegParserItem[]  = \".\"\n"
		        + ". . . 00 - - - - - => [   32] = #Any            :<NoType>         = \".\"\n"
		        + ". . 01 - - - - - - => [   34] = <NoName>        :Quantifier       = \"**\"\n"
		        + ". . . 00 - - - - - => [   33] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 01 - - - - - => [   34] = #Greediness     :<NoType>         = \"*\"\n"
		        + ". 05 - - - - - - - => [   36] = #Item[]         :RegParserItem[]  = \"</\"\n"
		        + ". 06 - - - - - - - => [   44] = #Item[]         :RegParserItem[]  = \"(#Term;)\"\n"
		        + ". . 00 - - - - - - => [   44] = #Group          :<NoType>         = \"(#Term;)\"\n"
		        + ". . . 00 - - - - - => [   37] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [   38] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [   42] = #Group-Name     :$Identifier      = \"Term\"\n"
		        + ". . . 03 - - - - - => [   43] = #BackRef        :<NoType>         = \";\"\n"
		        + ". . . 04 - - - - - => [   44] = <NoName>        :<NoType>         = \")\"\n"
		        + ". 07 - - - - - - - => [   45] = #Item[]         :RegParserItem[]  = \">\"",
		        result);
		
		result = parser.match("a(#Value:!$byte[]()!)?a", typeProvider);
		validate("\n"
		        + "00 - - - - - => [   23] = <NoName>        :RegParser        = \"a(#Value:!$byte[]()!)?a\"\n"
		        + ". 00 - - - - => [    1] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". 01 - - - - => [   22] = #ItemQuantifier :<NoType>         = \"(#Value:!$byte[]()!)?\"\n"
		        + ". . 00 - - - => [   21] = <NoName>        :RegParserItem[]  = \"(#Value:!$byte[]()!)\"\n"
		        + ". . . 00 - - => [   21] = #Group          :<NoType>         = \"(#Value:!$byte[]()!)\"\n"
		        + ". . . . 00 - => [    2] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . . 01 - => [    3] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . . 02 - => [    8] = #Group-Name     :$Identifier      = \"Value\"\n"
		        + ". . . . 03 - => [    9] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . . 04 - => [   20] = #Type           :Type             = \"!$byte[]()!\"\n"
		        + ". . . . . 00 => [   10] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". . . . . 01 => [   11] = #AsText         :<NoType>         = \"$\"\n"
		        + ". . . . . 02 => [   15] = #TypeName       :$Identifier      = \"byte\"\n"
		        + ". . . . . 03 => [   17] = #Collective     :<NoType>         = \"[]\"\n"
		        + ". . . . . 04 => [   19] = #Param          :<NoType>         = \"()\"\n"
		        + ". . . . . 05 => [   20] = <NoName>        :<NoType>         = \"!\"\n"
		        + ". . . . 05 - => [   21] = <NoName>        :<NoType>         = \")\"\n"
		        + ". . 01 - - - => [   22] = <NoName>        :Quantifier       = \"?\"\n"
		        + ". . . 00 - - => [   22] = #Quantifier     :<NoType>         = \"?\"\n"
		        + ". 02 - - - - => [   23] = #Item[]         :RegParserItem[]  = \"a\"",
		        result);
		
		result = parser.match("a[abcdefg]a", typeProvider);
		validate("\n"
		        + "00 - - - - => [   11] = <NoName>        :RegParser        = \"a[abcdefg]a\"\n"
		        + ". 00 - - - => [    1] = #Item[]         :RegParserItem[]  = \"a\"\n"
		        + ". 01 - - - => [   10] = #Item[]         :RegParserItem[]  = \"[abcdefg]\"\n"
		        + ". . 00 - - => [   10] = <NoName>        :CharSetItem      = \"[abcdefg]\"\n"
		        + ". . . 00 - => [    2] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . 01 - => [    3] = #Range          :Range            = \"a\"\n"
		        + ". . . . 00 => [    3] = #Start          :<NoType>         = \"a\"\n"
		        + ". . . 02 - => [    4] = #Range          :Range            = \"b\"\n"
		        + ". . . . 00 => [    4] = #Start          :<NoType>         = \"b\"\n"
		        + ". . . 03 - => [    5] = #Range          :Range            = \"c\"\n"
		        + ". . . . 00 => [    5] = #Start          :<NoType>         = \"c\"\n"
		        + ". . . 04 - => [    6] = #Range          :Range            = \"d\"\n"
		        + ". . . . 00 => [    6] = #Start          :<NoType>         = \"d\"\n"
		        + ". . . 05 - => [    7] = #Range          :Range            = \"e\"\n"
		        + ". . . . 00 => [    7] = #Start          :<NoType>         = \"e\"\n"
		        + ". . . 06 - => [    8] = #Range          :Range            = \"f\"\n"
		        + ". . . . 00 => [    8] = #Start          :<NoType>         = \"f\"\n"
		        + ". . . 07 - => [    9] = #Range          :Range            = \"g\"\n"
		        + ". . . . 00 => [    9] = #Start          :<NoType>         = \"g\"\n"
		        + ". . . 08 - => [   10] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". 02 - - - => [   11] = #Item[]         :RegParserItem[]  = \"a\"",
		        result);
		
		result = parser.match("< (#Term:~[A-Z][A-Z0-9]*~) [^>]* >.**</(#Term ; )>", typeProvider);
		validate("\n"
		        + "00 - - - - - - - - => [   50] = <NoName>        :RegParser        = \"< (#Term:~[A-Z][A-Z0-9]*~) [^>]* >.**</(#Term ; )>\"\n"
		        + ". 00 - - - - - - - => [    1] = #Item[]         :RegParserItem[]  = \"<\"\n"
		        + ". 01 - - - - - - - => [    2] = #Ignored[]      :<NoType>         = \" \"\n"
		        + ". 02 - - - - - - - => [   26] = #Item[]         :RegParserItem[]  = \"(#Term:~[A-Z][A-Z0-9]*~)\"\n"
		        + ". . 00 - - - - - - => [   26] = #Group          :<NoType>         = \"(#Term:~[A-Z][A-Z0-9]*~)\"\n"
		        + ". . . 00 - - - - - => [    3] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [    4] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [    8] = #Group-Name     :$Identifier      = \"Term\"\n"
		        + ". . . 03 - - - - - => [    9] = #Defined        :<NoType>         = \":\"\n"
		        + ". . . 04 - - - - - => [   10] = <NoName>        :<NoType>         = \"~\"\n"
		        + ". . . 05 - - - - - => [   24] = #GroupRegParser :RegParser        = \"[A-Z][A-Z0-9]*\"\n"
		        + ". . . . 00 - - - - => [   15] = #Item[]         :RegParserItem[]  = \"[A-Z]\"\n"
		        + ". . . . . 00 - - - => [   15] = <NoName>        :CharSetItem      = \"[A-Z]\"\n"
		        + ". . . . . . 00 - - => [   11] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . 01 - - => [   14] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . 00 - => [   12] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . 01 - => [   13] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . 02 - => [   14] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . 02 - - => [   15] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . 01 - - - - => [   24] = #ItemQuantifier :<NoType>         = \"[A-Z0-9]*\"\n"
		        + ". . . . . 00 - - - => [   23] = <NoName>        :RegParserItem[]  = \"[A-Z0-9]\"\n"
		        + ". . . . . . 00 - - => [   23] = <NoName>        :CharSetItem      = \"[A-Z0-9]\"\n"
		        + ". . . . . . . 00 - => [   16] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . . . . 01 - => [   19] = #Range          :Range            = \"A-Z\"\n"
		        + ". . . . . . . . 00 => [   17] = #Start          :<NoType>         = \"A\"\n"
		        + ". . . . . . . . 01 => [   18] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   19] = #End            :<NoType>         = \"Z\"\n"
		        + ". . . . . . . 02 - => [   22] = #Range          :Range            = \"0-9\"\n"
		        + ". . . . . . . . 00 => [   20] = #Start          :<NoType>         = \"0\"\n"
		        + ". . . . . . . . 01 => [   21] = <NoName>        :<NoType>         = \"-\"\n"
		        + ". . . . . . . . 02 => [   22] = #End            :<NoType>         = \"9\"\n"
		        + ". . . . . . . 03 - => [   23] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . . . . 01 - - - => [   24] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . . . . 00 - - => [   24] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 06 - - - - - => [   26] = <NoName>        :<NoType>         = \"~)\"\n"
		        + ". 03 - - - - - - - => [   27] = #Ignored[]      :<NoType>         = \" \"\n"
		        + ". 04 - - - - - - - => [   32] = #ItemQuantifier :<NoType>         = \"[^>]*\"\n"
		        + ". . 00 - - - - - - => [   31] = <NoName>        :RegParserItem[]  = \"[^>]\"\n"
		        + ". . . 00 - - - - - => [   31] = <NoName>        :CharSetItem      = \"[^>]\"\n"
		        + ". . . . 00 - - - - => [   28] = <NoName>        :<NoType>         = \"[\"\n"
		        + ". . . . 01 - - - - => [   29] = #NOT            :<NoType>         = \"^\"\n"
		        + ". . . . 02 - - - - => [   30] = #Range          :Range            = \">\"\n"
		        + ". . . . . 00 - - - => [   30] = #Start          :<NoType>         = \">\"\n"
		        + ". . . . 03 - - - - => [   31] = <NoName>        :<NoType>         = \"]\"\n"
		        + ". . 01 - - - - - - => [   32] = <NoName>        :Quantifier       = \"*\"\n"
		        + ". . . 00 - - - - - => [   32] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". 05 - - - - - - - => [   33] = #Ignored[]      :<NoType>         = \" \"\n"
		        + ". 06 - - - - - - - => [   34] = #Item[]         :RegParserItem[]  = \">\"\n"
		        + ". 07 - - - - - - - => [   37] = #ItemQuantifier :<NoType>         = \".**\"\n"
		        + ". . 00 - - - - - - => [   35] = <NoName>        :RegParserItem[]  = \".\"\n"
		        + ". . . 00 - - - - - => [   35] = #Any            :<NoType>         = \".\"\n"
		        + ". . 01 - - - - - - => [   37] = <NoName>        :Quantifier       = \"**\"\n"
		        + ". . . 00 - - - - - => [   36] = #Quantifier     :<NoType>         = \"*\"\n"
		        + ". . . 01 - - - - - => [   37] = #Greediness     :<NoType>         = \"*\"\n"
		        + ". 08 - - - - - - - => [   39] = #Item[]         :RegParserItem[]  = \"</\"\n"
		        + ". 09 - - - - - - - => [   49] = #Item[]         :RegParserItem[]  = \"(#Term ; )\"\n"
		        + ". . 00 - - - - - - => [   49] = #Group          :<NoType>         = \"(#Term ; )\"\n"
		        + ". . . 00 - - - - - => [   40] = <NoName>        :<NoType>         = \"(\"\n"
		        + ". . . 01 - - - - - => [   41] = #Name           :<NoType>         = \"#\"\n"
		        + ". . . 02 - - - - - => [   45] = #Group-Name     :$Identifier      = \"Term\"\n"
		        + ". . . 03 - - - - - => [   46] = <NoName>        :<NoType>         = \" \"\n"
		        + ". . . 04 - - - - - => [   47] = #BackRef        :<NoType>         = \";\"\n"
		        + ". . . 05 - - - - - => [   49] = <NoName>        :<NoType>         = \" )\"\n"
		        + ". 10 - - - - - - - => [   50] = #Item[]         :RegParserItem[]  = \">\"",
		        result);
	}
	
	@Test
	public void testComment() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.match("ABC(*DEF*)/*G(*H*)I*/JKL", typeProvider);
		validate("\n"
		        + "00 - => [   24] = <NoName>        :RegParser        = \"ABC(*DEF*)/*G(*H*)I*/JKL\"\n"
		        + ". 00 => [    3] = #Item[]         :RegParserItem[]  = \"ABC\"\n"
		        + ". 01 => [   10] = #Comment        :Comment          = \"(*DEF*)\"\n"
		        + ". 02 => [   21] = #Comment        :Comment          = \"/*G(*H*)I*/\"\n"
		        + ". 03 => [   24] = #Item[]         :RegParserItem[]  = \"JKL\"", result);
	}
	
	@Test
	public void testWhitespace() {
		var parser = newRegParser(new ParserTypeRef.Simple(RPTRegParser.Name), OneOrMore);
		var result = parser.match("ABC   JKL", typeProvider);
		validate("\n"
		        + "00 - => [    9] = <NoName>        :RegParser        = \"ABC   JKL\"\n"
		        + ". 00 => [    3] = #Item[]         :RegParserItem[]  = \"ABC\"\n"
		        + ". 01 => [    6] = #Ignored[]      :<NoType>         = \"   \"\n"
		        + ". 02 => [    9] = #Item[]         :RegParserItem[]  = \"JKL\"",
		        result);
	}

}
