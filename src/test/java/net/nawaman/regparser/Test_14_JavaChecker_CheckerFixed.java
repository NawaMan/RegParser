package net.nawaman.regparser;

public class Test_14_JavaChecker_CheckerFixed {
    
    static public void Assert(Object pValue, Object pCorrectValue) {
        if (!Util.equal(pValue, pCorrectValue)) {
            System.out.println(
                    "It's " + Util.toString(pValue) + " but it should be " + Util.toString(pCorrectValue) + ".");
            
            if ((pValue instanceof String) && (pCorrectValue instanceof String)) {
                String S1 = (String) pValue;
                String S2 = (String) pCorrectValue;
                System.out.println(S1.length() + " : " + S2.length());
                
                for (int i = 0; i < S1.length(); i++) {
                    if (S1.charAt(i) != S2.charAt(i))
                        System.out.println("|");
                    System.out.print(S1.charAt(i));
                }
            }
            
            throw new AssertionError();
        }
    }
    
    @SuppressWarnings("serial")
    static public class TestChecker implements Checker {
        static public final TestChecker Instance = new TestChecker();
        
        static public final TestChecker getInstance() {
            return TestChecker.Instance;
        }
        
        public TestChecker() {
        }
        
        public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider) {
            return this.getStartLengthOf(S, pOffset, pProvider, null);
        }
        
        public int getStartLengthOf(CharSequence S, int pOffset, PTypeProvider pProvider, ParseResult pResult) {
            return TestChecker.getLengthOf(S, pOffset, pProvider, pResult);
        }
        
        static public int getLengthOf(CharSequence CS, int Offset, PTypeProvider pTProvider, ParseResult pResult) {
            for (int i = Offset; i < CS.length(); i++) {
                char c = CS.charAt(i);
                if (!((c >= '0') && (c <= '9')))
                    return i - Offset;
            }
            return 0;
        }
        
        public Checker getOptimized() {
            return this;
        }
    }
    
    static public void main(String... Args) {
        
        boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
        
        RegParser                RP  = null;
        ParseResult              PR  = null;
        PTypeProvider.Extensible PTP = new PTypeProvider.Extensible();
        
        RP = RegParser.newRegParser(
                "!javaChecker(`net.nawaman.regparser.Test_14_JavaChecker_CheckerFixed.TestChecker->getLengthOf`)!");
        PR = RP.parse("0123456fdgd");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser
                .newRegParser("!javaChecker(`net.nawaman.regparser.Test_14_JavaChecker_CheckerFixed.TestChecker`)!");
        PR = RP.parse("012345fdgd");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser(
                "!javaChecker(`net.nawaman.regparser.Test_14_JavaChecker_CheckerFixed.TestChecker::Instance`)!");
        PR = RP.parse("01234fdgd");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser(
                "!javaChecker(`net.nawaman.regparser.Test_14_JavaChecker_CheckerFixed.TestChecker::getInstance()`)!");
        PR = RP.parse("0123fdgd");
        if (!IsQuiet)
            System.out.println(PR.toString());
        PR = RP.parse("012fdgd");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        PTP.addType("Number", RegParser.newRegParser("[0-9]*"));
        PTP.addType("TestCG", new CheckerFixeds(new CheckerFixeds.Entry(1), new CheckerFixeds.Entry(4),
                new CheckerFixeds.Entry("G1", 5, PTP.getType("Number").getTypeRef()), new CheckerFixeds.Entry()));
        PR = PTP.getType("TestCG").parse("0123456789ABCDEFG");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("END --------------------------------------------------------------");
    }
    
}
