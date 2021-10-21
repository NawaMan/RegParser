package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;


public class TestType {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    private PTypeProvider.Simple defaultTypeProvider;
    
    @SuppressWarnings("serial")
    static class RTByte extends PType {
        
        static public final RTByte Instance = new RTByte();
        
        @Override
        public String getName() {
            return "$byte?";
        }
        
        Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1, 3));
        
        @Override
        public Checker getChecker(ParseResult hostResult, String param, PTypeProvider provider) {
            return this.Checker;
        }
        
        @Override
        public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, PTypeProvider provider) {
            var text  = thisResult.getText();
            int value = Integer.parseInt(text);
            return (value >= 0) && (value <= 255);
        }
        
    }
    
    @Before
    public void setup() {
        defaultTypeProvider = new PTypeProvider.Extensible(RTByte.Instance);
    }
    
    @Test
    public void testBasicType() {
        var regParser = RegParser.newRegParser(defaultTypeProvider, RPEntry._new("#Value", defaultTypeProvider.getType("$byte?")));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testBasicTypeRef() {
        @SuppressWarnings("serial")
        var refToByte = new PTypeRef() {
            @Override
            public String getName() {
                return "$byte?";
            }
        };
        var regParser = RegParser.newRegParser(defaultTypeProvider, RPEntry._new("#Value", refToByte));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testDefineType() {
        
        @SuppressWarnings("serial")
        var byteType = new PType() {
            @Override
            public String getName() {
                return "$byte?";
            }
            
            Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1, 3));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
            
            @Override
            public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, PTypeProvider provider) {
                var text  = thisResult.getText();
                int value = Integer.parseInt(text);
                return (value >= 0) && (value <= 255);
            }
        };
        
        var regParser = RegParser.newRegParser(RPEntry._new("#Value", byteType));
        var result    = regParser.parse("192");
        validate("192", result.textOf("#Value"));
    }
    
    @Test
    public void testTypeWithValidation() {
        
        @SuppressWarnings("serial")
        var int0To4 = new PType() {
            @Override
            public String getName() {
                return "$int(0-4)?";
            }
            
            Checker Checker = RegParser.newRegParser(
                                PredefinedCharClasses.Digit,
                                new Quantifier(1, 1, Greediness.Maximum)
                            );
            
            @Override
            public Checker getChecker(ParseResult hostResult, String param, PTypeProvider provider) {
                return this.Checker;
            }
            
            @Override
            public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, PTypeProvider provider) {
                var text  = thisResult.getText();
                int value = Integer.parseInt(text);
                return (value >= 0) && (value <= 4);
            }
        };
        
        @SuppressWarnings("serial")
        var int5To9 = new PType() {
            @Override
            public String getName() {
                return "$int(5-9)?";
            }
            
            Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit,
                    new Quantifier(1, 1, Greediness.Maximum));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
            
            @Override
            public boolean doValidate(ParseResult hostResult, ParseResult thisResult, String param, PTypeProvider provider) {
                var text  = thisResult.getText();
                int value = Integer.parseInt(text);
                return (value >= 5) && (value <= 9);
            }
        };
        var regParser = RegParser.newRegParser(
                            new CheckerAlternative(
                                    RegParser.newRegParser("#Value_Low",  int0To4),
                                    RegParser.newRegParser("#Value_High", int5To9)
                            ),
                            Quantifier.ZeroOrMore_Maximum
                        );
        
        var result = regParser.parse("3895482565");
        validate("[3,4,2]",         Util.toString(result.textsOf("#Value_Low")));
        validate("[8,9,5,8,5,6,5]", Util.toString(result.textsOf("#Value_High")));
    }
    
    @Test
    public void testRecursive() {
        @SuppressWarnings("serial")
        var blockType = new PType() {
            @Override
            public String getName() {
                return "block";
            }
            
            Checker Checker = RegParser.newRegParser(    // <([^<]|!block!)*+>
                    new CharSingle('<'),
                    new CheckerAlternative(
                            RegParser.newRegParser("#Other",
                                    RegParser.newRegParser(
                                            new CharNot(new CharSet("<>")), Quantifier.OneOrMore)
                                    ),
                            RegParser.newRegParser("#SubBlock", new PTypeRef.Simple("block"))
                    ),
                    Quantifier.ZeroOrMore_Minimum, new CharSingle('>'));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
        };
        
        var typeProvider = new PTypeProvider.Simple(blockType);
        validate("!block!", typeProvider.getType("block"));
        
        var regParser = RegParser.newRegParser(typeProvider, RPEntry._new("#Block", blockType));
        validate("(#Block:!block!)", regParser);
        
        var parseResult = regParser.parse("<123456>");
        validate("\n"
                + "00 - => [    8] = #Block          :block            = \"<123456>\"\n"
                + ". 00 => [    1] = <NoName>        :<NoType>         = \"<\"\n"
                + ". 01 => [    7] = #Other          :<NoType>         = \"123456\"\n"
                + ". 02 => [    8] = <NoName>        :<NoType>         = \">\"",
                parseResult);
        validate("<123456>", parseResult.getText());
        
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
        validate("< < - >  < < : > ; > >", parseResult.getText());
    }
    
    @Test
    public void testBackReference() {
        // Add the type
        defaultTypeProvider.addRPType(PTBackRef.BackRef_Instance);
        defaultTypeProvider.addRPType(PTBackRefCI.BackRefCI_Instance);
        
        var regParser = RegParser.newRegParser(defaultTypeProvider,
                    RPEntry._new("#X", new PTypeRef.Simple("$byte?")), 
                    new CharSingle('x'),
                    RegParser.newRegParser(defaultTypeProvider, new PTypeRef.Simple(PTBackRef.BackRef_Instance.getName(), "#X"))
                );
        validate("(#X:!$byte?!)[x]((!$BackRef?(\"#X\")!))", regParser);
        
        var parseResult = regParser.parse("56x56");
        validate("\n"
                + "00 => [    2] = #X              :$byte?           = \"56\"\n"
                + "01 => [    3] = <NoName>        :<NoType>         = \"x\"\n"
                + "02 => [    5] = <NoName>        :$BackRef?        = \"56\"",
                parseResult);
        validate("56x56", parseResult.getText());
        
        validate(null, regParser.parse("56x78"));
    }
    
    @Test
    public void testBackReference_xml() {
        // Add the type
        var typeProvider = new PTypeProvider.Simple(PTBackRef.BackRef_Instance);
        
        var parser = RegParser.newRegParser(typeProvider, new CharSingle('<'),
                RPEntry._new("Begin",
                        RegParser.newRegParser(typeProvider, new CharUnion(new CharRange('a', 'z'), new CharRange('A', 'Z')),
                                Quantifier.OneOrMore)),
                PredefinedCharClasses.Blank, Quantifier.ZeroOrMore, new CharSingle('>'), PredefinedCharClasses.Any,
                Quantifier.ZeroOrMore_Minimum,
                RegParser.newRegParser(typeProvider, "#End",
                        RegParser.newRegParser(typeProvider, new WordChecker("</"),
                                RPEntry._new("#EndTag",
                                        new PTypeRef.Simple(PTBackRef.BackRef_Instance.getName(), "Begin")),
                                new CharSingle('>'))));
        
        validate(
                "[<](Begin:~[[a-z][A-Z]]+~)[\\ \\t]*[>].**((#End:~</(#EndTag:!$BackRef?(\"Begin\")!)[>]~))",
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
        var identifierType = new PType() {
            @Override
            public String getName() {
                return "Identifier";
            }
            
            Checker Checker = RegParser.newRegParser(new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_')),
                    new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_'), PredefinedCharClasses.Digit),
                    Quantifier.ZeroOrMore);
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
        };
        
        @SuppressWarnings("serial")
        var stringLiteralType = new PType() {
            @Override
            public String getName() {
                return "StringValue";
            }
            
            Checker Checker = RegParser
                    .newRegParser(
                            new CheckerAlternative(
                                    RegParser.newRegParser(new CharSingle('\"'),
                                            new CheckerAlternative(new CharNot(new CharSingle('\"')),
                                                    new WordChecker("\\\"")),
                                            Quantifier.ZeroOrMore_Minimum, new CharSingle('\"')),
                                    RegParser.newRegParser(new CharSingle('\''),
                                            new CheckerAlternative(new CharNot(new CharSingle('\'')),
                                                    new WordChecker("\\\'")),
                                            Quantifier.ZeroOrMore_Minimum, new CharSingle('\''))));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
        };
        
        @SuppressWarnings("serial")
        var attributeType = new PType() {
            @Override
            public String getName() {
                return "Attribute";
            }
            
            Checker Checker = RegParser.newRegParser("#AttrName", new PTypeRef.Simple("Identifier"),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore, new CharSingle('='),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore, "#AttrValue",
                    new PTypeRef.Simple("StringValue"));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
        };
        
        @SuppressWarnings("serial")
        var tagType = new PType() {
            @Override
            public String getName() {
                return "Tag";
            }
            
            Checker Checker = RegParser.newRegParser(new CharSingle('<'),
                    RPEntry._new("$Begin", RegParser.newRegParser(
                            new CharUnion(new CharRange('a', 'z'), new CharRange('A', 'Z')), Quantifier.OneOrMore)),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    RegParser.newRegParser(RegParser.newRegParser(PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                            "$Attr", new PTypeRef.Simple("Attribute"), PredefinedCharClasses.Blank,
                            Quantifier.ZeroOrMore), Quantifier.ZeroOrMore),
                    new CheckerAlternative(
                            RegParser.newRegParser(new CharSingle('>'),
                                    new CheckerAlternative(
                                            RegParser.newRegParser("#Other",
                                                    RegParser.newRegParser(new CharNot(new CharSet("<>")),
                                                            Quantifier.OneOrMore)),
                                            RegParser.newRegParser("#SubBlock", new PTypeRef.Simple("Tag"))),
                                    Quantifier.ZeroOrMore_Minimum,
                                    RegParser.newRegParser("#End", RegParser.newRegParser(new WordChecker("</"),
                                            RPEntry._new("#EndTag",
                                                    new PTypeRef.Simple(PTBackRefCI.BackRefCI_Instance.getName(),
                                                            "$Begin")),
                                            new CharSingle('>')))),
                            RegParser.newRegParser(new WordChecker("/>"))));
            
            @Override
            public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
                return this.Checker;
            }
        };
        
        // Add the type
        var typeProvider = new PTypeProvider.Extensible();
        typeProvider.addRPType(tagType);
        typeProvider.addRPType(identifierType);
        typeProvider.addRPType(stringLiteralType);
        typeProvider.addRPType(attributeType);
        
        var parser = RegParser.newRegParser(RPEntry._new("#Block", tagType));
        validate("(#Block:!Tag!)", parser);
        
        var result = parser.parse("<tag attr1='value1' attr2='value2'> <p>Something <b> Some more <br /> thing </b> </p> </Tag>",
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
}
