package net.nawaman.regparser;

import static net.nawaman.regparser.PredefinedCharClasses.Alphabet;
import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.PredefinedCharClasses.Blank;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrMore_Minimum;
import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.RegParserEntry.newParserEntry;
import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CharSet;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;


public class TestType {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    private ParserTypeProvider.Simple defaultTypeProvider;
    
    @SuppressWarnings("serial")
    static class RTByte extends ParserType {
        
        static public final RTByte Instance = new RTByte();
        
        private final Checker checker = newRegParser(Digit.bound(1, 3));
        
        @Override
        public String name() {
            return "$byte?";
        }
        
        @Override
        public Checker checker(ParseResult hostResult, String param, ParserTypeProvider provider) {
            return this.checker;
        }
        
        @Override
        public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, ParserTypeProvider provider) {
            var text  = thisResult.text();
            int value = Integer.parseInt(text);
            return (value >= 0) && (value <= 255);
        }
        
        @Override
        public final Boolean isDeterministic() {
            return true;
        }
        
    }
    
    @Before
    public void setup() {
        defaultTypeProvider = new ParserTypeProvider.Extensible(RTByte.Instance);
    }
    
    @Test
    public void testBasicType() {
        var regParser = newRegParser(defaultTypeProvider, newParserEntry("#Value", defaultTypeProvider.type("$byte?")));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testBasicTypeRef() {
        @SuppressWarnings("serial")
        var refToByte = new ParserTypeRef() {
            @Override
            public String name() {
                return "$byte?";
            }
            @Override
            public final Boolean isDeterministic() {
                return true;
            }
        };
        var regParser = newRegParser(defaultTypeProvider, newParserEntry("#Value", refToByte));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testDefineType() {
        
        @SuppressWarnings("serial")
        var byteType = new ParserType() {
            
            private final Checker checker = newRegParser(Digit.bound(1, 3));
            
            @Override
            public String name() {
                return "$byte?";
            }
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return true;
            }
            
            @Override
            public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, ParserTypeProvider provider) {
                var text  = thisResult.text();
                int value = Integer.parseInt(text);
                return (value >= 0) && (value <= 255);
            }
        };
        
        var regParser = newRegParser(RegParserEntry.newParserEntry("#Value", byteType));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testRecursive() {
        @SuppressWarnings("serial")
        var blockType = new ParserType() {
            @Override
            public String name() {
                return "block";
            }
            
            private final Checker checker
                    = newRegParser()    // <([^<]|!block!)*+>
                    .entry(CharSingle.of('<'))
                    .entry(either(newRegParser("#Other",    newRegParser(new CharNot(new CharSet("<>")).oneOrMore())))
                            .or  (newRegParser("#SubBlock", ParserTypeRef.of("block"))),
                            ZeroOrMore_Minimum)
                    .entry(CharSingle.of('>'))
                    .build();
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return true;
            }
        };
        
        var typeProvider = new ParserTypeProvider.Simple(blockType);
        validate("!block!", typeProvider.type("block"));
        
        var regParser = newRegParser(typeProvider, RegParserEntry.newParserEntry("#Block", blockType));
        validate("(#Block:!block!)", regParser);
        
        var parseResult = regParser.parse("<123456>");
        validate("\n"
                + "00 - => [    8] = #Block          :block            = \"<123456>\"\n"
                + ". 00 => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + ". 01 => [    7] = #Other          :<NoType>         = \"123456\"\n"
                + ". 02 => [    8] = <NoName>        :<NoType>         = \">\"",
                parseResult);
        validate("<123456>", parseResult.text());
        
        parseResult = regParser.parse("< < - >  < < : > ; > >");
        validate("\n"
                + "00 - - - => [   22] = #Block          :block            = \"< < - >  < < : > ; > >\"\n"
                + ". 00 - - => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + ". 01 - - => [    2] = #Other          :<NoType>         = \" \"\n"
                + ". 02 - - => [    7] = #SubBlock       :block            = \"< - >\"\n"
                + ". . 00 - => [    3] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . 01 - => [    6] = #Other          :<NoType>         = \" - \"\n"
                + ". . 02 - => [    7] = <NoName>        :<NoType>         = \">\"\n"
                + ". 03 - - => [    9] = #Other          :<NoType>         = \"  \"\n"
                + ". 04 - - => [   20] = #SubBlock       :block            = \"< < : > ; >\"\n"
                + ". . 00 - => [   10] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . 01 - => [   11] = #Other          :<NoType>         = \" \"\n"
                + ". . 02 - => [   16] = #SubBlock       :block            = \"< : >\"\n"
                + ". . . 00 => [   12] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . . 01 => [   15] = #Other          :<NoType>         = \" : \"\n"
                + ". . . 02 => [   16] = <NoName>        :<NoType>         = \">\"\n"
                + ". . 03 - => [   19] = #Other          :<NoType>         = \" ; \"\n"
                + ". . 04 - => [   20] = <NoName>        :<NoType>         = \">\"\n"
                + ". 05 - - => [   21] = #Other          :<NoType>         = \" \"\n"
                + ". 06 - - => [   22] = <NoName>        :<NoType>         = \">\"",
                parseResult);
        validate("< < - >  < < : > ; > >", parseResult.text());
    }
    
    @Test
    public void testBackReference() {
        // Add the type
        defaultTypeProvider.addType(ParserTypeBackRef.BackRef_Instance);
        defaultTypeProvider.addType(ParserTypeBackRefCaseInsensitive.BackRefCI_Instance);
        
        var regParser = newRegParser(defaultTypeProvider,
                            RegParserEntry.newParserEntry("#X", new ParserTypeRef.Simple("$byte?")), 
                            new CharSingle('x'),
                            newRegParser(defaultTypeProvider, new ParserTypeRef.Simple(ParserTypeBackRef.BackRef_Instance.name(), "#X"))
                );
        validate("(#X:!$byte?!)\n"
                + "[x]\n"
                + "(!$BackRef?(\"#X\")!)\n"
                + "  - (!$BackRef?(\"#X\")!)", regParser);
        
        var parseResult = regParser.parse("56x56");
        validate("\n"
                + "00 => [    2] = #X              :$byte?           = \"56\"\n"
                + "01 => [    3] = <NoName>        :<NoType>         = \"x\"\n"
                + "02 => [    5] = <NoName>        :$BackRef?        = \"56\"",
                parseResult);
        validate("56x56", parseResult.text());
        
        validate(null, regParser.parse("56x78"));
    }
    
    @Test
    public void testBackReference_xml() {
        // Add the type
        var typeProvider = new ParserTypeProvider.Simple(ParserTypeBackRef.BackRef_Instance);
        
        var parser
                = newRegParser()
                .typeProvider(typeProvider)
                .entry(new CharSingle('<'))
                .entry("Begin", newRegParser()
                    .typeProvider(typeProvider)
                    .entry(new CharUnion(new CharRange('a', 'z'), new CharRange('A', 'Z')).oneOrMore()))
                .entry(Blank, ZeroOrMore)
                .entry(new CharSingle('>'))
                .entry(Any, ZeroOrMore_Minimum)
                .entry(newRegParser()
                    .typeProvider(typeProvider)
                    .entry("#End", newRegParser()
                        .typeProvider(typeProvider)
                        .entry(new WordChecker("</"))
                        .entry("#EndTag", new ParserTypeRef.Simple(ParserTypeBackRef.BackRef_Instance.name(), "Begin"))
                        .entry(new CharSingle('>'))))
                .build();
        
        validate(
                "[<]\n"
                + "(Begin:~[[a-z][A-Z]]+~)\n"
                + "  - [[a-z][A-Z]]+\n"
                + "[\\ \\t]*\n"
                + "[>]\n"
                + ".**\n"
                + "(#End:~</(#EndTag:!$BackRef?(\"Begin\")!)[>]~)\n"
                + "  - (#End:~</(#EndTag:!$BackRef?(\"Begin\")!)[>]~)\n"
                + "  -   - </\n"
                + "  -   - (#EndTag:!$BackRef?(\"Begin\")!)\n"
                + "  -   - [>]",
                parser);
        
        var result = parser.parse("<tag> <br /> </tag>");
        validate("\n"
                + "00 - => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + "01 - => [    4] = Begin           :<NoType>         = \"tag\"\n"
                + "02 - => [   13] = <NoName>        :<NoType>         = \"> <br /> \"\n"
                + "03 - => [   19] = #End            :<NoType>         = \"</tag>\"\n"
                + ". 00 => [   15] = <NoName>        :<NoType>         = \"</\"\n"
                + ". 01 => [   18] = #EndTag         :$BackRef?        = \"tag\"\n"
                + ". 02 => [   19] = <NoName>        :<NoType>         = \">\"",
                result);
        
        validate("\n"
                + "00 - => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + "01 - => [    4] = Begin           :<NoType>         = \"tag\"\n"
                + "02 - => [   13] = <NoName>        :<NoType>         = \"> <br /> \"\n"
                + "03 - => [   19] = #End            :<NoType>         = \"</tag>\"\n"
                + ". 00 => [   15] = <NoName>        :<NoType>         = \"</\"\n"
                + ". 01 => [   18] = #EndTag         :$BackRef?        = \"tag\"\n"
                + ". 02 => [   19] = <NoName>        :<NoType>         = \">\"",
                result.toString());
    }
    
    @Test
    public void testExtensibleTypeProvider() {
        
        @SuppressWarnings("serial")
        var identifierType = new ParserType() {
            
            private final Checker checker
                    = newRegParser()
                    .entry(new CharUnion(Alphabet, new CharSingle('_')))
                    .entry(new CharUnion(Alphabet, new CharSingle('_'), Digit), ZeroOrMore)
                    .build();
            
            @Override
            public String name() {
                return "Identifier";
            }
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return true;
            }
        };
        
        @SuppressWarnings("serial")
        var stringLiteralType = new ParserType() {
            
            final private Checker checker
                    = either(newRegParser()
                        .entry(new CharSingle('\"'))
                        .entry(either(new CharNot(new CharSingle('\"')))
                                .or  (new WordChecker("\\\"")), ZeroOrMore_Minimum)
                        .entry(new CharSingle('\"')))
                    .or(newRegParser()
                        .entry(new CharSingle('\''))
                        .entry(either(new CharNot(CharSingle.of('\'')))
                                .or  (new WordChecker("\\\'")),
                                ZeroOrMore_Minimum)
                        .entry(new CharSingle('\'')))
                    .build();
            
            @Override
            public String name() {
                return "StringValue";
            }
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return checker.isDeterministic();
            }
        };
        
        @SuppressWarnings("serial")
        var attributeType = new ParserType() {
            @Override
            public String name() {
                return "Attribute";
            }
            
            private final Checker checker
                    = newRegParser()
                    .entry("#AttrName", ParserTypeRef.of("Identifier"))
                    .entry(Blank, ZeroOrMore)
                    .entry(new CharSingle('='))
                    .entry(Blank, ZeroOrMore)
                    .entry("#AttrValue", ParserTypeRef.of("StringValue"))
                    .build();
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return true;
            }
        };
        
        @SuppressWarnings("serial")
        var tagType = new ParserType() {
            @Override
            public String name() {
                return "Tag";
            }
            
            private final Checker checker
                    = newRegParser()
                    .entry(new CharSingle('<'))
                    .entry("$Begin", newRegParser(new CharUnion(new CharRange('a', 'z'), new CharRange('A', 'Z')).oneOrMore()))
                    .entry(Blank, ZeroOrMore)
                    .entry(
                        newRegParser()
                        .entry(Blank, ZeroOrMore)
                        .entry("$Attr", ParserTypeRef.of("Attribute"))
                        .entry(Blank, ZeroOrMore)
                        , ZeroOrMore
                    )
                    .entry(
                        either(
                            newRegParser()
                            .entry(new CharSingle('>'))
                            .entry(
                                either(newRegParser("#Other", newRegParser(new CharNot(new CharSet("<>")).oneOrMore())))
                                .or   (newRegParser("#SubBlock", ParserTypeRef.of("Tag"))),
                                ZeroOrMore_Minimum
                            )
                            .entry(
                                "#End",
                                newRegParser()
                                .entry(new WordChecker("</"))
                                .entry("#EndTag", ParserTypeBackRefCaseInsensitive.of("$Begin"))
                                .entry(new CharSingle('>'))
                            )
                        )
                        .or(new WordChecker("/>"))
                    )
                    .build();
            
            @Override
            public Checker checker(ParseResult hostResult, String param, ParserTypeProvider typeProvider) {
                return this.checker;
            }
            
            @Override
            public final Boolean isDeterministic() {
                return checker.isDeterministic();
            }
        };
        
        // Add the type
        var typeProvider = new ParserTypeProvider.Extensible();
        typeProvider.addType(tagType);
        typeProvider.addType(identifierType);
        typeProvider.addType(stringLiteralType);
        typeProvider.addType(attributeType);
        
        var parser = RegParser.newRegParser(RegParserEntry.newParserEntry("#Block", tagType));
        validate("(#Block:!Tag!)", parser);
        
        var result = parser.parse(
                "<tag attr1='value1' attr2='value2'> <p>Something <b> Some more <br /> thing </b> </p> </Tag>",
                typeProvider);
        validate("\n"
                + "00 - - - - => [   92] = #Block          :Tag              = \"<tag attr1=\\'value1\\' attr2=\\'value2\\'> <p>Something <b> Some more <br /> thing </b> </p> </Tag>\"\n"
                + ". 00 - - - => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + ". 01 - - - => [    4] = $Begin          :<NoType>         = \"tag\"\n"
                + ". 02 - - - => [    5] = <NoName>        :<NoType>         = \" \"\n"
                + ". 03 - - - => [   19] = $Attr           :Attribute        = \"attr1=\\'value1\\'\"\n"
                + ". 04 - - - => [   20] = <NoName>        :<NoType>         = \" \"\n"
                + ". 05 - - - => [   34] = $Attr           :Attribute        = \"attr2=\\'value2\\'\"\n"
                + ". 06 - - - => [   35] = <NoName>        :<NoType>         = \">\"\n"
                + ". 07 - - - => [   36] = #Other          :<NoType>         = \" \"\n"
                + ". 08 - - - => [   85] = #SubBlock       :Tag              = \"<p>Something <b> Some more <br /> thing </b> </p>\"\n"
                + ". . 00 - - => [   37] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . 01 - - => [   38] = $Begin          :<NoType>         = \"p\"\n"
                + ". . 02 - - => [   39] = <NoName>        :<NoType>         = \">\"\n"
                + ". . 03 - - => [   49] = #Other          :<NoType>         = \"Something \"\n"
                + ". . 04 - - => [   80] = #SubBlock       :Tag              = \"<b> Some more <br /> thing </b>\"\n"
                + ". . . 00 - => [   50] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . . 01 - => [   51] = $Begin          :<NoType>         = \"b\"\n"
                + ". . . 02 - => [   52] = <NoName>        :<NoType>         = \">\"\n"
                + ". . . 03 - => [   63] = #Other          :<NoType>         = \" Some more \"\n"
                + ". . . 04 - => [   69] = #SubBlock       :Tag              = \"<br />\"\n"
                + ". . . . 00 => [   64] = <NoName>        :<NoType>         = \"<\"\n"
                + ". . . . 01 => [   66] = $Begin          :<NoType>         = \"br\"\n"
                + ". . . . 02 => [   69] = <NoName>        :<NoType>         = \" />\"\n"
                + ". . . 05 - => [   76] = #Other          :<NoType>         = \" thing \"\n"
                + ". . . 06 - => [   80] = #End            :<NoType>         = \"</b>\"\n"
                + ". . . . 00 => [   78] = <NoName>        :<NoType>         = \"</\"\n"
                + ". . . . 01 => [   79] = #EndTag         :$BackRefCI?      = \"b\"\n"
                + ". . . . 02 => [   80] = <NoName>        :<NoType>         = \">\"\n"
                + ". . 05 - - => [   81] = #Other          :<NoType>         = \" \"\n"
                + ". . 06 - - => [   85] = #End            :<NoType>         = \"</p>\"\n"
                + ". . . 00 - => [   83] = <NoName>        :<NoType>         = \"</\"\n"
                + ". . . 01 - => [   84] = #EndTag         :$BackRefCI?      = \"p\"\n"
                + ". . . 02 - => [   85] = <NoName>        :<NoType>         = \">\"\n"
                + ". 09 - - - => [   86] = #Other          :<NoType>         = \" \"\n"
                + ". 10 - - - => [   92] = #End            :<NoType>         = \"</Tag>\"\n"
                + ". . 00 - - => [   88] = <NoName>        :<NoType>         = \"</\"\n"
                + ". . 01 - - => [   91] = #EndTag         :$BackRefCI?      = \"Tag\"\n"
                + ". . 02 - - => [   92] = <NoName>        :<NoType>         = \">\"",
                result);
    }
    
    @Test
    public void testTextCaseInsensitive() {
        var parser = compile("!textCI(`Te\\\"st`)!");
        var result = parser.parse("te\"st");
        validate("\n"
               + "00 => [    5] = <NoName>        :textCI           = \"te\\\"st\"", result);
    }
    
}
