package net.nawaman.regparser;

public class Test_05_Greediness {
    
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
    
    static public void main(String... Args) {
        boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
        
        System.out.println("Greediness ---------------------------------------------------------");
        
        RegParser RP = null;
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 1 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Possessive),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 2 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Maximum),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 28);
        
        System.out.println("Test 3 ----------------------------------");
        RP = RegParser.newRegParser(RPEntry._new(PredefinedCharClasses.Java_Any, Quantifier.ZeroOrMore_Minimum),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 4 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Possessive)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 5 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Possessive)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 6 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 21, Greediness.Possessive)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 7 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 8 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        System.out.println("Test 9 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 31, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 28);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 10 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 5, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 11 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 17, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        System.out.println("Test 12 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(PredefinedCharClasses.Java_Any, new Quantifier(1, 31, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 13 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), Quantifier.ZeroOrMore_Possessive),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 14 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), Quantifier.ZeroOrMore_Maximum),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 28);
        
        System.out.println("Test 15 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), Quantifier.ZeroOrMore_Minimum),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 16 ----------------------------------");
        RP = RegParser
                .newRegParser(RPEntry._new(
                        new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                                new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                                new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                                new WordChecker("n"), new WordChecker("d")),
                        new Quantifier(1, 5, Greediness.Possessive)), RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 17 ----------------------------------");
        RP = RegParser
                .newRegParser(RPEntry._new(
                        new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                                new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                                new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                                new WordChecker("n"), new WordChecker("d")),
                        new Quantifier(1, 17, Greediness.Possessive)), RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 18 ----------------------------------");
        RP = RegParser
                .newRegParser(RPEntry._new(
                        new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                                new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                                new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                                new WordChecker("n"), new WordChecker("d")),
                        new Quantifier(1, 21, Greediness.Possessive)), RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 19 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 5, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 20 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 17, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        System.out.println("Test 21 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 31, Greediness.Maximum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 28);
        
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println();
        System.out.println("Test 22 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 5, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012"), null);
        
        System.out.println("Test 23 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 17, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        System.out.println("Test 24 ----------------------------------");
        RP = RegParser.newRegParser(
                RPEntry._new(new CheckerAlternative(new WordChecker("1"), new WordChecker("2"), new WordChecker("3"),
                        new WordChecker("4"), new WordChecker("5"), new WordChecker("6"), new WordChecker("7"),
                        new WordChecker("8"), new WordChecker("9"), new WordChecker("0"), new WordChecker("e"),
                        new WordChecker("n"), new WordChecker("d")), new Quantifier(1, 31, Greediness.Minimum)),
                RPEntry._new(new WordChecker("end")));
        if (!IsQuiet)
            System.out.println(RP.toString());
        Assert(RP.parse("1234567end123456789012345end9012").getEndPosition(), 10);
        
        
        System.out.println();
        System.out.println("All Success.");
    }
}
