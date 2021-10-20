package net.nawaman.regparser;

public class Test_03_Alternative {
    
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
        System.out.println("Alternative ---------------------------------------------------------");
        Checker CA = new CheckerAlternative(new WordChecker("555"), new WordChecker("222"));
        Assert(CA.getStartLengthOf("555222", 0, null), 3);
        Assert(CA.getStartLengthOf("555222", 2, null), -1);
        Assert(CA.getStartLengthOf("222A555", 0, null), 3);
        Assert(CA.getStartLengthOf("555A222", 0, null), 3);
        Assert(CA.getStartLengthOf("00555222", 0, null), -1);
        Assert(CA.getStartLengthOf("5505222", 0, null), -1);
        
        System.out.println("All Success.");
    }
    
}
