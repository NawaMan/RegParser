package net.nawaman.regparser;

public class Test_04_RegParser {
    
    static public void Assert(Object pValue, Object pCorrectValue) {
        if (!Util.equal(pValue, pCorrectValue)) {
            System.out.println(
                    "It's " + Util.toString(pValue) + " but it should be " + Util.toString(pCorrectValue) + ".");
            
            if ((pValue instanceof String) && (pCorrectValue instanceof String)) {
                String S1 = (String) pValue;
                String S2 = (String) pCorrectValue;
                System.out.println(S1.length() + " : " + S2.length());
            }
            
            throw new AssertionError();
        }
    }
    
    static public void main(String... Arvs) {
        boolean IsQuiet = ((Arvs != null) && (Arvs.length > 0) && (Tests.ToQuiet.equals(Arvs[0])));
        
        System.out.println("RegParser ---------------------------------------------------------");
        RegParser RP = null;
        
        System.out.println();
        System.out.println("Test 1 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), Quantifier.Zero, new WordChecker("Two"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo"), null);
        Assert(RP.parse("TwoOne").getEndPosition(), 3);
        if (!IsQuiet)
            System.out.println(RP.parse("TwoOne"));
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 2 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), new WordChecker("Two"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo").getEndPosition(), 6);
        Assert(RP.parse("TwoOne"), null);
        if (!IsQuiet)
            System.out.println(RP.parse("OneTwo"));
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 3 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), Quantifier.ZeroOrOne, new WordChecker("Two"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo").getEndPosition(), 6);
        Assert(RP.parse("TwoOne").getEndPosition(), 3);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 4 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), new WordChecker("Two"), Quantifier.ZeroOrOne,
                new WordChecker("One"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo"), null);
        Assert(RP.parse("TwoOne"), null);
        Assert(RP.parse("OneTwoOne").getEndPosition(), 9);
        Assert(RP.parse("OneOne").getEndPosition(), 6);
        
        System.out.println("Test 5 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), new WordChecker("Two"), Quantifier.OneOrMore,
                new WordChecker("One"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo"), null);
        Assert(RP.parse("TwoOne"), null);
        Assert(RP.parse("OneOne"), null);
        Assert(RP.parse("OneTwoOne").getEndPosition(), 9);
        Assert(RP.parse("OneTwoTwoOne").getEndPosition(), 12);
        Assert(RP.parse("OneTwoTwoTwoOne").getEndPosition(), 15);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 6 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), new WordChecker("Two"), Quantifier.ZeroOrMore,
                new WordChecker("One"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("OneTwo"), null);
        Assert(RP.parse("TwoOne"), null);
        Assert(RP.parse("OneOne").getEndPosition(), 6);
        Assert(RP.parse("OneTwoOne").getEndPosition(), 9);
        Assert(RP.parse("OneTwoTwoOne").getEndPosition(), 12);
        Assert(RP.parse("OneTwoTwoTwoOne").getEndPosition(), 15);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 7 ----------------------------------");
        RP = RegParser.newRegParser(new WordChecker("One"), new CharRange('0', '9'), Quantifier.OneOrMore,
                new WordChecker("One"));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("One123"), null);
        Assert(RP.parse("123One"), null);
        Assert(RP.parse("OneOne"), null);
        Assert(RP.parse("One123One").getEndPosition(), 9);
        Assert(RP.parse("One123456One").getEndPosition(), 12);
        Assert(RP.parse("One123456789One").getEndPosition(), 15);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 8 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("One"), Quantifier.One),
                RPEntry._new(new CharRange('0', '9'), Quantifier.ZeroOrMore), RPEntry._new(new WordChecker("One")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("One123"), null);
        Assert(RP.parse("123One"), null);
        Assert(RP.parse("OneOne").getEndPosition(), 6);
        Assert(RP.parse("One123One").getEndPosition(), 9);
        Assert(RP.parse("One123456One").getEndPosition(), 12);
        Assert(RP.parse("One123456789One").getEndPosition(), 15);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 9 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("One"), Quantifier.One),
                RPEntry._new(new CharRange('0', '9'), new Quantifier(0, 5)), RPEntry._new(new WordChecker("One")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("One123"), null);
        Assert(RP.parse("123One"), null);
        Assert(RP.parse("OneOne").getEndPosition(), 6);
        Assert(RP.parse("One123One").getEndPosition(), 9);
        Assert(RP.parse("One123456One"), null);
        Assert(RP.parse("One123456789One"), null);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 10 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("One"), Quantifier.One),
                RPEntry._new(new CharRange('0', '9'), new Quantifier(5, -1)), RPEntry._new(new WordChecker("One")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("One123"), null);
        Assert(RP.parse("123One"), null);
        Assert(RP.parse("OneOne"), null);
        Assert(RP.parse("One123One"), null);
        Assert(RP.parse("One123456One").getEndPosition(), 12);
        Assert(RP.parse("One123456789One").getEndPosition(), 15);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 11 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("Col")),
                RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou"))//,
                                                                                                                                                                  //new Quantifier(2, Greediness.Possessive)
                ), RPEntry._new(new WordChecker("r")));
                
        if (!IsQuiet)
            System.out.println(RP.toString());
        if (!IsQuiet)
            System.out.println(RP.parse("Color"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colour"));
        
        Assert(RP.parse("Color").getEndPosition(), 5);
        Assert(RP.parse("Colour").getEndPosition(), 6);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 12 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("Col")),
                RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou"))),
                RPEntry._new(new WordChecker("ur")));
        
        if (!IsQuiet)
            System.out.println(RP.toString());
        if (!IsQuiet)
            System.out.println(RP.parse("Colour"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colouur"));
        
        Assert(RP.parse("Colour"), null);
        Assert(RP.parse("Colouur").getEndPosition(), 7);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 13 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(new WordChecker("Col")),
                RPEntry._new(new CheckerAlternative(new WordChecker("o"), new WordChecker("ou")), Quantifier.OneOrMore),
                RPEntry._new(new WordChecker("r")));
        
        if (!IsQuiet)
            System.out.println(RP.parse("Colur"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colour"));
        if (!IsQuiet)
            System.out.println(RP.parse("Coloour"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colouour"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colooouour"));
        if (!IsQuiet)
            System.out.println(RP.parse("Colouoouoour"));
        
        System.out.println();
        System.out.println("All Success.");
    }
    
}
