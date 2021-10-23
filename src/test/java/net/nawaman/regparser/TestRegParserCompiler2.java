package net.nawaman.regparser;

import static net.nawaman.regparser.RPCompiler_ParserTypes.getCharClass;
import static net.nawaman.regparser.RegParser.newRegParser;

import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.RPCompiler_ParserTypes.RPTCharSetItem;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTComment;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscape;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeHex;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeOct;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTEscapeUnicode;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTQuantifier;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRange;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParser;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTRegParserItem;
import net.nawaman.regparser.RPCompiler_ParserTypes.RPTType;
import net.nawaman.regparser.parsers.PTIdentifier;
import net.nawaman.regparser.parsers.PTStrLiteral;

public class TestRegParserCompiler2 {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    private PTypeProvider.Extensible typeProvider;
    private Random                   random = new Random();
    
    static void validate(CharChecker expectedCharChecker, Object actual) {
        validate(expectedCharChecker.toString(), actual);
    }
    static void validate(Greediness expectedGreediness, Object actual) {
        validate(expectedGreediness.toString(), actual);
    }
    static void validate(int expectedInt, int actualInt) {
        TestUtils.validate(expectedInt, actualInt);
    }
    static void validate(String expected, Object actual) {
        TestUtils.validate(expected, actual);
    }
    static void validateNull(Object actual) {
        TestUtils.validate("Null", actual);
    }
    
    static public void AssertNotNull(Object pValue) {
        if (pValue == null)
            throw new AssertionError();
    }
    
    static public void AssertNull(Object pValue) {
        if (pValue != null)
            throw new AssertionError();
    }
    
    @Before
    public void setup() {
        var typeProvider = new PTypeProvider.Extensible();
        
        typeProvider.addRPType(new PTTextCI());
        typeProvider.addRPType(new PTIdentifier());
        typeProvider.addRPType(new PTStrLiteral());
        typeProvider.addRPType(new RPTComment());
        typeProvider.addRPType(new RPTType());
        typeProvider.addRPType(new RPTQuantifier());
        typeProvider.addRPType(new RPTRegParserItem());
        typeProvider.addRPType(new RPTEscape());
        typeProvider.addRPType(new RPTEscapeOct());
        typeProvider.addRPType(new RPTEscapeHex());
        typeProvider.addRPType(new RPTEscapeUnicode());
        typeProvider.addRPType(new RPTRange());
        typeProvider.addRPType(new RPTCharSetItem());
        typeProvider.addRPType(new RPTRegParser());
        
        this.typeProvider = typeProvider;
    }
    
    @Test
    public void testEscapeChars() {
        var typeName = RPTEscape.Name;
        var parser   = newRegParser(new PTypeRef.Simple(typeName));
        validate("(!Escape!)", parser);
        
        for (char c : RPCompiler_ParserTypes.Escapable.toCharArray()) {
            var result = parser.match("\\" + c, typeProvider);
            validate("" + c, typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        }
    }
    
    @Test
    public void testEscapeOct() {
        var typeName = RPTEscapeOct.Name;
        var parser   = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!EscapeOct!)", parser);
        
        char c  = 'n';
        var  result = parser.match("\\0" + ((c / (8 * 8)) % 8) + "" + ((c / 8) % 8) + "" + (c % 8), typeProvider);
        //System.out.println((PR == null)?"null":PR.toDetail());
        validate("n", typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        for (int i = 32; i < 126; i++) {
            result = parser.match("\\0" + ((i / (8 * 8)) % 8) + "" + ((i / 8) % 8) + "" + (i % 8), typeProvider);
            validate("" + ((char)i), typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        }
    }
    
    @Test
    public void testEscapeHex() {
        var typeName = RPTEscapeHex.Name;
        
        var parser = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!EscapeHex!)", parser);
        
        char c  = 'm';
        var result = parser.match("\\x" + RPTEscapeHex.HEX.charAt((c / 16) % 16)
                                   + "" + RPTEscapeHex.HEX.charAt(c % 16), typeProvider);
        //System.out.println((PR == null)?"null":PR.toDetail());
        validate("" + c, typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        c  = (char) (random.nextInt(126 - 32) + 32);
        result = parser.match("\\x" + RPTEscapeHex.HEX.charAt((c / 16) % 16)
                               + "" + RPTEscapeHex.HEX.charAt(c % 16), typeProvider);
        //System.out.println((PR == null)?"null":PR.toDetail());
        validate("" + c, typeProvider.getType(typeName).compile(result, null, null, typeProvider));
    }
    
    @Test
    public void testEscapeUnicode() {
        var typeName = RPTEscapeUnicode.Name;
        
        var parser = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!EscapeUnicode!)", parser);
        
        char c  = 'm';
        var result = parser.match("\\u" + RPTEscapeHex.HEX.charAt((c / (16 * 16 * 16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt((c / (16 * 16)) % 16) + "" + RPTEscapeHex.HEX.charAt((c / (16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt(c % 16), typeProvider);
        validate("\n"
                + "00 => [    6] = <NoName>        :EscapeUnicode    = \"\\\\u006D\"",
                result);
        validate("" + c, typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        c  = (char) (random.nextInt('ฮ' - 'ก') + 'ก');
        result = parser.match("\\u" + RPTEscapeHex.HEX.charAt((c / (16 * 16 * 16)) % 16) + ""
                                    + RPTEscapeHex.HEX.charAt((c / (16 * 16)) % 16) + "" 
                                    + RPTEscapeHex.HEX.charAt((c / (16)) % 16) + ""
                                    + RPTEscapeHex.HEX.charAt(c % 16), typeProvider);
        validate("" + c, typeProvider.getType(typeName).compile(result, null, null, typeProvider));
    }
    
    @Test
    public void testPredefinedCharClasses() {
        var parser = RegParser.newRegParser(RPCompiler_ParserTypes.PredefinedCheckers);
        
        var result = parser.match("\\jd", typeProvider);
        validate("\n"
                + "00 - => [    3] = #CharClass      :<NoType>         = \"\\\\jd\"\n"
                + ". 00 => [    2] = <NoName>        :<NoType>         = \"\\\\j\"\n"
                + ". 01 => [    3] = $JDigit         :<NoType>         = \"d\"",
                result);
        validate("[:Digit:]", getCharClass(result, 0));
        
        validate(PredefinedCharClasses.Any,                 getCharClass(parser.match(".",   typeProvider)));
        validate(PredefinedCharClasses.Digit,               getCharClass(parser.match("\\d", typeProvider)));
        validate(PredefinedCharClasses.NonDigit,            getCharClass(parser.match("\\D", typeProvider)));
        validate(PredefinedCharClasses.WhiteSpace,          getCharClass(parser.match("\\s", typeProvider)));
        validate(PredefinedCharClasses.NonWhiteSpace,       getCharClass(parser.match("\\S", typeProvider)));
        validate(PredefinedCharClasses.Blank,               getCharClass(parser.match("\\b", typeProvider)));
        validate(PredefinedCharClasses.NonBlank,            getCharClass(parser.match("\\B", typeProvider)));
        validate(PredefinedCharClasses.Word,                getCharClass(parser.match("\\w", typeProvider)));
        validate(PredefinedCharClasses.NonWord,             getCharClass(parser.match("\\W", typeProvider)));
        validate(PredefinedCharClasses.OctalDigit,          getCharClass(parser.match("\\o", typeProvider)));
        validate(PredefinedCharClasses.NonOctalDigit,       getCharClass(parser.match("\\O", typeProvider)));
        validate(PredefinedCharClasses.HexadecimalDigit,    getCharClass(parser.match("\\x", typeProvider)));
        validate(PredefinedCharClasses.NonHexadecimalDigit, getCharClass(parser.match("\\X", typeProvider)));
        
        validate(PredefinedCharClasses.LowerCaseAlphabet, getCharClass(parser.match("\\p{Lower}",  typeProvider)));
        validate(PredefinedCharClasses.UpperCaseAlphabet, getCharClass(parser.match("\\p{Upper}",  typeProvider)));
        validate(PredefinedCharClasses.ASCII,             getCharClass(parser.match("\\p{ASCII}",  typeProvider)));
        validate(PredefinedCharClasses.Alphabet,          getCharClass(parser.match("\\p{Alpha}",  typeProvider)));
        validate(PredefinedCharClasses.AlphabetAndDigit,  getCharClass(parser.match("\\p{Alnum}",  typeProvider)));
        validate(PredefinedCharClasses.Punctuation,       getCharClass(parser.match("\\p{Punct}",  typeProvider)));
        validate(PredefinedCharClasses.Visible,           getCharClass(parser.match("\\p{Graph}",  typeProvider)));
        validate(PredefinedCharClasses.Printable,         getCharClass(parser.match("\\p{Print}",  typeProvider)));
        validate(PredefinedCharClasses.Blank,             getCharClass(parser.match("\\p{Blank}",  typeProvider)));
        validate(PredefinedCharClasses.OctalDigit,        getCharClass(parser.match("\\p{ODigit}", typeProvider)));
        validate(PredefinedCharClasses.HexadecimalDigit,  getCharClass(parser.match("\\p{XDigit}", typeProvider)));
        
        validate(PredefinedCharClasses.Java_Digit,         getCharClass(parser.match("\\jd", typeProvider)));
        validate(PredefinedCharClasses.Java_NonDigit,      getCharClass(parser.match("\\jD", typeProvider)));
        validate(PredefinedCharClasses.Java_WhiteSpace,    getCharClass(parser.match("\\js", typeProvider)));
        validate(PredefinedCharClasses.Java_NonWhiteSpace, getCharClass(parser.match("\\jS", typeProvider)));
        validate(PredefinedCharClasses.Java_Word,          getCharClass(parser.match("\\jw", typeProvider)));
        validate(PredefinedCharClasses.Java_NonWord,       getCharClass(parser.match("\\jW", typeProvider)));
        
        validate(PredefinedCharClasses.Java_LowerCaseAlphabet, getCharClass(parser.match("\\jp{Lower}",   typeProvider)));
        validate(PredefinedCharClasses.Java_UpperCaseAlphabet, getCharClass(parser.match("\\jp{Upper}",   typeProvider)));
        validate(PredefinedCharClasses.Java_ASCII,             getCharClass(parser.match("\\jp{ASCII}",   typeProvider)));
        validate(PredefinedCharClasses.Java_Alphabet,          getCharClass(parser.match("\\jp{Alpha}",   typeProvider)));
        validate(PredefinedCharClasses.Java_AlphabetAndDigit,  getCharClass(parser.match("\\jp{Alnum}",   typeProvider)));
        validate(PredefinedCharClasses.Java_Punctuation,       getCharClass(parser.match("\\jp{Punct}",   typeProvider)));
        validate(PredefinedCharClasses.Java_Visible,           getCharClass(parser.match("\\jp{Graph}",   typeProvider)));
        validate(PredefinedCharClasses.Java_Printable,         getCharClass(parser.match("\\jp{Print}",   typeProvider)));
        validate(PredefinedCharClasses.Java_Blank,             getCharClass(parser.match("\\jp{Blank}",   typeProvider)));
        validate(PredefinedCharClasses.Java_ControlCharacter,  getCharClass(parser.match("\\jp{Cntrl}",   typeProvider)));
        validate(PredefinedCharClasses.Java_HexadecimalDigit,  getCharClass(parser.match("\\jp{XDigit}",  typeProvider)));
        validate(PredefinedCharClasses.Java_Greek,             getCharClass(parser.match("\\jp{InGreek}", typeProvider)));
        validate(PredefinedCharClasses.Java_CurrencySimbol,    getCharClass(parser.match("\\jp{Sc}",      typeProvider)));
        
        validate(PredefinedCharClasses.Any,                 getCharClass(parser.match("[:Any:]",           typeProvider)));
        validate(PredefinedCharClasses.Digit,               getCharClass(parser.match("[:Digit:]",         typeProvider)));
        validate(PredefinedCharClasses.NonDigit,            getCharClass(parser.match("[:NonDigit:]",      typeProvider)));
        validate(PredefinedCharClasses.WhiteSpace,          getCharClass(parser.match("[:WhiteSpace:]",    typeProvider)));
        validate(PredefinedCharClasses.NonWhiteSpace,       getCharClass(parser.match("[:NonWhiteSpace:]", typeProvider)));
        validate(PredefinedCharClasses.Word,                getCharClass(parser.match("[:Word:]",          typeProvider)));
        validate(PredefinedCharClasses.NonWord,             getCharClass(parser.match("[:NonWord:]",       typeProvider)));
        validate(PredefinedCharClasses.Blank,               getCharClass(parser.match("[:Blank:]",         typeProvider)));
        validate(PredefinedCharClasses.NonBlank,            getCharClass(parser.match("[:NonBlank:]",      typeProvider)));
        validate(PredefinedCharClasses.OctalDigit,          getCharClass(parser.match("[:OctDigit:]",      typeProvider)));
        validate(PredefinedCharClasses.NonOctalDigit,       getCharClass(parser.match("[:NonOctDigit:]",   typeProvider)));
        validate(PredefinedCharClasses.HexadecimalDigit,    getCharClass(parser.match("[:HexDigit:]",      typeProvider)));
        validate(PredefinedCharClasses.NonHexadecimalDigit, getCharClass(parser.match("[:NonHexDigit:]",   typeProvider)));
        
        validate(PredefinedCharClasses.LowerCaseAlphabet, getCharClass(parser.match("[:LowerCaseAlphabet:]", typeProvider)));
        validate(PredefinedCharClasses.UpperCaseAlphabet, getCharClass(parser.match("[:UpperCaseAlphabet:]", typeProvider)));
        validate(PredefinedCharClasses.ASCII,             getCharClass(parser.match("[:ASCII:]",             typeProvider)));
        validate(PredefinedCharClasses.Alphabet,          getCharClass(parser.match("[:Alphabet:]",          typeProvider)));
        validate(PredefinedCharClasses.AlphabetAndDigit,  getCharClass(parser.match("[:AlphabetAndDigit:]",  typeProvider)));
        validate(PredefinedCharClasses.Punctuation,       getCharClass(parser.match("[:Punctuation:]",       typeProvider)));
        validate(PredefinedCharClasses.Visible,           getCharClass(parser.match("[:Visible:]",           typeProvider)));
        validate(PredefinedCharClasses.Printable,         getCharClass(parser.match("[:Printable:]",         typeProvider)));
        
        validate(PredefinedCharClasses.Java_Any,           getCharClass(parser.match("[:JAny:]",           typeProvider)));
        validate(PredefinedCharClasses.Java_Digit,         getCharClass(parser.match("[:JDigit:]",         typeProvider)));
        validate(PredefinedCharClasses.Java_NonDigit,      getCharClass(parser.match("[:JNonDigit:]",      typeProvider)));
        validate(PredefinedCharClasses.Java_WhiteSpace,    getCharClass(parser.match("[:JWhiteSpace:]",    typeProvider)));
        validate(PredefinedCharClasses.Java_NonWhiteSpace, getCharClass(parser.match("[:JNonWhiteSpace:]", typeProvider)));
        validate(PredefinedCharClasses.Java_Word,          getCharClass(parser.match("[:JWord:]",          typeProvider)));
        validate(PredefinedCharClasses.Java_NonWord,       getCharClass(parser.match("[:JNonWord:]",       typeProvider)));
        
        validate(PredefinedCharClasses.Java_LowerCaseAlphabet, getCharClass(parser.match("[:JLowerCaseAlphabet:]", typeProvider)));
        validate(PredefinedCharClasses.Java_UpperCaseAlphabet, getCharClass(parser.match("[:JUpperCaseAlphabet:]", typeProvider)));
        validate(PredefinedCharClasses.Java_ASCII,             getCharClass(parser.match("[:JASCII:]",             typeProvider)));
        validate(PredefinedCharClasses.Java_Alphabet,          getCharClass(parser.match("[:JAlphabet:]",          typeProvider)));
        validate(PredefinedCharClasses.Java_AlphabetAndDigit,  getCharClass(parser.match("[:JAlphabetAndDigit:]",  typeProvider)));
        validate(PredefinedCharClasses.Java_Punctuation,       getCharClass(parser.match("[:JPunctuation:]",       typeProvider)));
        validate(PredefinedCharClasses.Java_Visible,           getCharClass(parser.match("[:JVisible:]",           typeProvider)));
        validate(PredefinedCharClasses.Java_Printable,         getCharClass(parser.match("[:JPrintable:]",         typeProvider)));
        validate(PredefinedCharClasses.Java_Blank,             getCharClass(parser.match("[:JBlank:]",             typeProvider)));
        validate(PredefinedCharClasses.Java_ControlCharacter,  getCharClass(parser.match("[:JControlCharacter:]",  typeProvider)));
        validate(PredefinedCharClasses.Java_HexadecimalDigit,  getCharClass(parser.match("[:JHexadecimalDigit:]",  typeProvider)));
        validate(PredefinedCharClasses.Java_Greek,             getCharClass(parser.match("[:JGreek:]",             typeProvider)));
        validate(PredefinedCharClasses.Java_CurrencySimbol,    getCharClass(parser.match("[:JCurrencySimbol:]",    typeProvider)));
    }
    
    @Test
    public void testTextType() {
        var typeName = RPTType.Name;
        var type  = typeProvider.getType(typeName);
        
        var parser = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!Type!)", parser);
        
        var result = parser.match("!Text!", typeProvider);
        validate("\n"
                + "00 - => [    6] = <NoName>        :Type             = \"!Text!\"\n"
                + ". 00 => [    1] = <NoName>        :<NoType>         = \"!\"\n"
                + ". 01 => [    5] = #TypeName       :$Identifier      = \"Text\"\n"
                + ". 02 => [    6] = <NoName>        :<NoType>         = \"!\"",
                result);
        validate("!Text!",
                typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        validate("!text!",                  type.compile("!text!",                null, typeProvider));
        validate("!$text!",                 type.compile("!$text!",               null, typeProvider));
        validate("!text~!",                 type.compile("!text~!",               null, typeProvider));
        validate("!text[]!",                type.compile("!text[]!",              null, typeProvider));
        validate("!text!",                  type.compile("!text()!",              null, typeProvider));
        validate("!text(\"a\")!",           type.compile("!text('a')!",           null, typeProvider));
        validate("!text(\"a\\\"\")!",       type.compile("!text('a\\\"')!",       null, typeProvider));
        validate("!text(\"a\\\'\")!",       type.compile("!text(`a'`)!",          null, typeProvider));
        validate("!$text~!",                type.compile("!$text~!",              null, typeProvider));
        validate("!$text[]!",               type.compile("!$text[]!",             null, typeProvider));
        validate("!$text!",                 type.compile("!$text()!",             null, typeProvider));
        validate("!$text(\"a\")!",          type.compile("!$text('a')!",          null, typeProvider));
        validate("!text~[]!",               type.compile("!text~[]!",             null, typeProvider));
        validate("!text~!",                 type.compile("!text~()!",             null, typeProvider));
        validate("!text~(\"a\")!",          type.compile("!text~('a')!",          null, typeProvider));
        validate("!text~[]!",               type.compile("!text~[]!",             null, typeProvider));
        validate("!text[]!",                type.compile("!text[]()!",            null, typeProvider));
        validate("!text[](\"a\")!",         type.compile("!text[]('a')!",         null, typeProvider));
        validate("!$text~[](\"Na\\'wa\")!", type.compile("!$text~[]('Na\\'wa')!", null, typeProvider));
    }
    
    @Test
    public void testQualifier() {
        var typeName = RPTQuantifier.Name;
        var type  = typeProvider.getType(typeName);
        
        var parser = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!Quantifier!)", parser);
        
        var result = parser.match("{5,10}*", typeProvider);
        validate("\n"
                + "00 - - => [    7] = <NoName>        :Quantifier       = \"{5,10}*\"\n"
                + ". 00 - => [    6] = #Quantifier     :<NoType>         = \"{5,10}\"\n"
                + ". . 00 => [    1] = <NoName>        :<NoType>         = \"{\"\n"
                + ". . 01 => [    2] = #LowerBound     :<NoType>         = \"5\"\n"
                + ". . 02 => [    3] = <NoName>        :<NoType>         = \",\"\n"
                + ". . 03 => [    5] = #UpperBound     :<NoType>         = \"10\"\n"
                + ". . 04 => [    6] = <NoName>        :<NoType>         = \"}\"\n"
                + ". 01 - => [    7] = #Greediness     :<NoType>         = \"*\"",
                result);
        validate("{5,10}*",
                typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        validate("?",      type.compile("?",          null, typeProvider));
        validate("*",      type.compile("*",          null, typeProvider));
        validate("+",      type.compile("+",          null, typeProvider));
        validate("?+",     type.compile("?+",         null, typeProvider));
        validate("*+",     type.compile("*+",         null, typeProvider));
        validate("++",     type.compile("++",         null, typeProvider));
        validate("?*",     type.compile("?*",         null, typeProvider));
        validate("**",     type.compile("**",         null, typeProvider));
        validate("+*",     type.compile("+*",         null, typeProvider));
        validate("{5}",    type.compile("{5}",        null, typeProvider));
        validate("{5,}",   type.compile("{5,}",       null, typeProvider));
        validate("{,5}",   type.compile("{,5}",       null, typeProvider));
        validate("{2,5}",  type.compile("{2,5}",      null, typeProvider));
        validate("{2,5}",  type.compile("{ 2 , 5 }",  null, typeProvider));
        validate("{2,5}*", type.compile("{ 2 , 5 }*", null, typeProvider));
        
        validate( 0,                  ((Quantifier) type.compile("?",     null, typeProvider)).getLowerBound());
        validate( 1,                  ((Quantifier) type.compile("?",     null, typeProvider)).getUpperBound());
        validate( 0,                  ((Quantifier) type.compile("*",     null, typeProvider)).getLowerBound());
        validate(-1,                  ((Quantifier) type.compile("*",     null, typeProvider)).getUpperBound());
        validate( 1,                  ((Quantifier) type.compile("+",     null, typeProvider)).getLowerBound());
        validate(-1,                  ((Quantifier) type.compile("+",     null, typeProvider)).getUpperBound());
        validate( 5,                  ((Quantifier) type.compile("{5}",   null, typeProvider)).getLowerBound());
        validate( 5,                  ((Quantifier) type.compile("{5}",   null, typeProvider)).getUpperBound());
        validate( 5,                  ((Quantifier) type.compile("{5,}",  null, typeProvider)).getLowerBound());
        validate(-1,                  ((Quantifier) type.compile("{5,}",  null, typeProvider)).getUpperBound());
        validate( 0,                  ((Quantifier) type.compile("{,5}",  null, typeProvider)).getLowerBound());
        validate( 5,                  ((Quantifier) type.compile("{,5}",  null, typeProvider)).getUpperBound());
        validate( 2,                  ((Quantifier) type.compile("{2,5}", null, typeProvider)).getLowerBound());
        validate( 5,                  ((Quantifier) type.compile("{2,5}", null, typeProvider)).getUpperBound());
        validate( Greediness.Maximum, ((Quantifier) type.compile("?+",    null, typeProvider)).getGreediness());
        validate( Greediness.Minimum, ((Quantifier) type.compile("?*",    null, typeProvider)).getGreediness());
    }
    
    @Test
    public void testRange() {
        var typeName = RPTRange.Name;
        var type     = typeProvider.getType(typeName);
        var parser   = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!Range!)", parser);
        
        var result = parser.match("b-z", typeProvider);
        validate("\n"
                + "00 - => [    3] = <NoName>        :Range            = \"b-z\"\n"
                + ". 00 => [    1] = #Start          :<NoType>         = \"b\"\n"
                + ". 01 => [    2] = <NoName>        :<NoType>         = \"-\"\n"
                + ". 02 => [    3] = #End            :<NoType>         = \"z\"",
                result);
        validate("[b-z]", typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        
        validate("[a]",       type.compile("a",       null, typeProvider));
        validate("[a-d]",     type.compile("a-d",     null, typeProvider));
        validate("[\\n-d]",   type.compile("\\\n-d",  null, typeProvider));
        validate("[\\(-\\)]", type.compile("\\(-\\)", null, typeProvider));
        validate("[\\ -\\)]", type.compile("\\ -\\)", null, typeProvider));
    }
    
    @Test
    public void testCharSet() {
        var typeName = RPTCharSetItem.Name;
        var type     = typeProvider.getType(typeName);
        var text     = "[^a-zasdfg[A-Z][0-9].\\s\\jp{Blank}]&&[[:JDigit:]]";
        
        var parser = RegParser.newRegParser(new PTypeRef.Simple(typeName));
        validate("(!CharSetItem!)", parser);
        
        var result = parser.match(text, typeProvider);
        validate("\n"
                + "00 - - - => [   48] = <NoName>        :CharSetItem      = \"[^a-zasdfg[A-Z][0-9].\\\\s\\\\jp{Blank}]&&[[:JDigit:]]\"\n"
                + ". 00 - - => [    1] = <NoName>        :<NoType>         = \"[\"\n"
                + ". 01 - - => [    2] = #NOT            :<NoType>         = \"^\"\n"
                + ". 02 - - => [    5] = #Range          :Range            = \"a-z\"\n"
                + ". . 00 - => [    3] = #Start          :<NoType>         = \"a\"\n"
                + ". . 01 - => [    4] = <NoName>        :<NoType>         = \"-\"\n"
                + ". . 02 - => [    5] = #End            :<NoType>         = \"z\"\n"
                + ". 03 - - => [    6] = #Range          :Range            = \"a\"\n"
                + ". . 00 - => [    6] = #Start          :<NoType>         = \"a\"\n"
                + ". 04 - - => [    7] = #Range          :Range            = \"s\"\n"
                + ". . 00 - => [    7] = #Start          :<NoType>         = \"s\"\n"
                + ". 05 - - => [    8] = #Range          :Range            = \"d\"\n"
                + ". . 00 - => [    8] = #Start          :<NoType>         = \"d\"\n"
                + ". 06 - - => [    9] = #Range          :Range            = \"f\"\n"
                + ". . 00 - => [    9] = #Start          :<NoType>         = \"f\"\n"
                + ". 07 - - => [   10] = #Range          :Range            = \"g\"\n"
                + ". . 00 - => [   10] = #Start          :<NoType>         = \"g\"\n"
                + ". 08 - - => [   15] = <NoName>        :CharSetItem      = \"[A-Z]\"\n"
                + ". . 00 - => [   11] = <NoName>        :<NoType>         = \"[\"\n"
                + ". . 01 - => [   14] = #Range          :Range            = \"A-Z\"\n"
                + ". . . 00 => [   12] = #Start          :<NoType>         = \"A\"\n"
                + ". . . 01 => [   13] = <NoName>        :<NoType>         = \"-\"\n"
                + ". . . 02 => [   14] = #End            :<NoType>         = \"Z\"\n"
                + ". . 02 - => [   15] = <NoName>        :<NoType>         = \"]\"\n"
                + ". 09 - - => [   20] = <NoName>        :CharSetItem      = \"[0-9]\"\n"
                + ". . 00 - => [   16] = <NoName>        :<NoType>         = \"[\"\n"
                + ". . 01 - => [   19] = #Range          :Range            = \"0-9\"\n"
                + ". . . 00 => [   17] = #Start          :<NoType>         = \"0\"\n"
                + ". . . 01 => [   18] = <NoName>        :<NoType>         = \"-\"\n"
                + ". . . 02 => [   19] = #End            :<NoType>         = \"9\"\n"
                + ". . 02 - => [   20] = <NoName>        :<NoType>         = \"]\"\n"
                + ". 10 - - => [   21] = #Any            :<NoType>         = \".\"\n"
                + ". 11 - - => [   23] = #CharClass      :<NoType>         = \"\\\\s\"\n"
                + ". . 00 - => [   22] = <NoName>        :<NoType>         = \"\\\\\"\n"
                + ". . 01 - => [   23] = $WhiteSpace     :<NoType>         = \"s\"\n"
                + ". 12 - - => [   33] = #CharClass      :<NoType>         = \"\\\\jp{Blank}\"\n"
                + ". . 00 - => [   27] = <NoName>        :<NoType>         = \"\\\\jp{\"\n"
                + ". . 01 - => [   32] = $JBlank         :<NoType>         = \"Blank\"\n"
                + ". . 02 - => [   33] = <NoName>        :<NoType>         = \"}\"\n"
                + ". 13 - - => [   34] = <NoName>        :<NoType>         = \"]\"\n"
                + ". 14 - - => [   36] = #Intersect      :<NoType>         = \"&&\"\n"
                + ". 15 - - => [   48] = #Set            :<NoType>         = \"[[:JDigit:]]\"\n"
                + ". . 00 - => [   37] = <NoName>        :<NoType>         = \"[\"\n"
                + ". . 01 - => [   47] = #CharClass      :<NoType>         = \"[:JDigit:]\"\n"
                + ". . . 00 => [   39] = <NoName>        :<NoType>         = \"[:\"\n"
                + ". . . 01 => [   45] = $JDigit         :<NoType>         = \"JDigit\"\n"
                + ". . . 02 => [   47] = <NoName>        :<NoType>         = \":]\"\n"
                + ". . 02 - => [   48] = <NoName>        :<NoType>         = \"]\"",
                result);
        validate("[[^[[a-z][asdfg][A-Z][0-9].[\\ \\t\\n\\r\\" + ((char)11) + "\\f][:SpaceOrTab:]]]&&[:Digit:]]",
                 typeProvider.getType(typeName).compile(result, null, null, typeProvider));
        validate("[[^[[a-z][asdfg][A-Z][0-9].[\\ \\t\\n\\r\\" + ((char)11) + "\\f][:SpaceOrTab:]]]&&[:Digit:]]",
                 type.compile(text, null, typeProvider));
    }
    
    @Test
    public void testRegParser() {
        {
            var parser = RegParser.newRegParser("abc");
            validate("abc",  parser);
            validate(3,      parser.match("abc", typeProvider).getEndPosition());
            validate("null", parser.match("def", typeProvider));
        }
        {
            var parser = RegParser.newRegParser("(a|b|c)");
            validate("(a|b|c)", parser);
            validate(1,      parser.match("a", typeProvider).getEndPosition());
            validate(1,      parser.match("b", typeProvider).getEndPosition());
            validate(1,      parser.match("c", typeProvider).getEndPosition());
            validate("null", parser.match("d", typeProvider));
        }
        {
            var parser = RegParser.newRegParser("Col(o|ou)?r");
            validate("Col(o|ou)?r", parser);
            validate(4,      parser.match("Colr",   typeProvider).getEndPosition());
            validate(5,      parser.match("Color",  typeProvider).getEndPosition());
            validate(6,      parser.match("Colour", typeProvider).getEndPosition());
            validate("null", parser.match("Shape",  typeProvider));
        }
        {
            typeProvider.addRPType(new PTIdentifier());
            var parser = RegParser.newRegParser("(var|int)\\b+!$Identifier!\\b*=\\b*[0-9]+\\b*;");
            validate(12, parser.match("var V1 = 45;", typeProvider).getEndPosition());
            validate(10, parser.match("var V1=45;",   typeProvider).getEndPosition());
            validate(12, parser.match("var V1 = 5 ;", typeProvider).getEndPosition());
            validate(11, parser.match("int V1 = 5;",  typeProvider).getEndPosition());
        }
        
        typeProvider.addRPType(PTBackRef.BackRef_Instance);
        typeProvider.addRPType(PTBackRefCI.BackRefCI_Instance);
        
        {
            var parser = RegParser.newRegParser("(#X:~[:AlphabetAndDigit:]+~)\\-(#X;)\\-(#X;)");
            validate(5, parser.match("5-5-5", typeProvider).getEndPosition());
        }
        
        {
            var parser = RegParser.newRegParser("($X:~[:AlphabetAndDigit:]+~)\\-($X';)\\-($X';)");
            validate(5, parser.match("A-A-A", typeProvider).getEndPosition());
            validate(5, parser.match("a-A-a", typeProvider).getEndPosition());
        }
        
        {
            var parser = RegParser.newRegParser(new PTypeRef.Simple(RPTRegParser.Name));
            validate("(!RegParser!)", parser);
            
            var result = parser.parse("one'Two'three", typeProvider);
            validate("\n"
                    + "00 - - => [   13] = <NoName>        :RegParser        = \"one\\'Two\\'three\"\n"
                    + ". 00 - => [    3] = #Item[]         :RegParserItem[]  = \"one\"\n"
                    + ". 01 - => [    8] = #Item[]         :RegParserItem[]  = \"\\'Two\\'\"\n"
                    + ". . 00 => [    8] = $TextCI         :<NoType>         = \"\\'Two\\'\"\n"
                    + ". 02 - => [   13] = #Item[]         :RegParserItem[]  = \"three\"",
                    result);
        }
        
        {
            var parser = RegParser.newRegParser("one'Two'three");
            validate("one(!textCI(\"Two\")!)three", parser);
            validate(11, parser.match("oneTwothree", typeProvider).getEndPosition());
            validate(11, parser.match("oneTWOthree", typeProvider).getEndPosition());
        }
        
        {
            var parser = RegParser.newRegParser("'var'\\b+(#Name:!$Identifier!)\\b*=\\b*(#Value:~[0-9]*~)\\b*;");
            validate("(!textCI(\"var\")!)[\\ \\t]+(#Name:!$Identifier!)[\\ \\t]*=[\\ \\t]*(#Value:~[0-9]*~)[\\ \\t]*;",
                    parser);
            
            var result = parser.match("var V1 = 50;", typeProvider);
            validate("\n"
                    + "00 => [    3] = <NoName>        :textCI           = \"var\"\n"
                    + "01 => [    4] = <NoName>        :<NoType>         = \" \"\n"
                    + "02 => [    6] = #Name           :$Identifier      = \"V1\"\n"
                    + "03 => [    9] = <NoName>        :<NoType>         = \" = \"\n"
                    + "04 => [   11] = #Value          :<NoType>         = \"50\"\n"
                    + "05 => [   12] = <NoName>        :<NoType>         = \";\"",
                    result);
            validate("V1", result.textOf("#Name"));
            validate("50", result.textOf("#Value"));
            
            result = parser.match("Var V2 = 750;", typeProvider);
            validate("\n"
                    + "00 => [    3] = <NoName>        :textCI           = \"Var\"\n"
                    + "01 => [    4] = <NoName>        :<NoType>         = \" \"\n"
                    + "02 => [    6] = #Name           :$Identifier      = \"V2\"\n"
                    + "03 => [    9] = <NoName>        :<NoType>         = \" = \"\n"
                    + "04 => [   12] = #Value          :<NoType>         = \"750\"\n"
                    + "05 => [   13] = <NoName>        :<NoType>         = \";\"",
                    result);
            validate("V2", result.textOf("#Name"));
            validate("750", result.textOf("#Value"));
        }
    }
    
}
