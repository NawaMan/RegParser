package net.nawaman.regparser;

public class Test_01_Char {
    
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
        System.out.println("Single char ---------------------------------------------------------");
        Checker CS = new CharSingle('5');
        Assert(CS.getStartLengthOf("555222", 0, null), 1);
        Assert(CS.getStartLengthOf("555222", 2, null), 1);
        Assert(CS.getStartLengthOf("00555222", 0, null), -1);
        Assert(CS.getStartLengthOf("5505222", 2, null), -1);
        
        System.out.println("Range char ---------------------------------------------------------");
        Checker CE = new CharRange('0', '9');
        Assert(CE.getStartLengthOf("543ABC", 0, null), 1);
        Assert(CE.getStartLengthOf("567ABC", 2, null), 1);
        Assert(CE.getStartLengthOf("AB123ABC", 0, null), -1);
        Assert(CE.getStartLengthOf("01A3ABC", 2, null), -1);
        
        System.out.println("Class char ---------------------------------------------------------");
        Checker CC = PredefinedCharClasses.Java_Digit;
        Assert(CC.getStartLengthOf("543ABC", 0, null), 1);
        Assert(CC.getStartLengthOf("567ABC", 2, null), 1);
        Assert(CC.getStartLengthOf("AB123ABC", 0, null), -1);
        Assert(CC.getStartLengthOf("01A3ABC", 2, null), -1);
        
        System.out.println("Not char ---------------------------------------------------------");
        Checker CN = new CharNot(new CharRange('A', 'Z'));
        Assert(CN.getStartLengthOf("543ABC", 0, null), 1);
        Assert(CN.getStartLengthOf("567ABC", 2, null), 1);
        Assert(CN.getStartLengthOf("AB123ABC", 0, null), -1);
        Assert(CN.getStartLengthOf("01A3ABC", 2, null), -1);
        
        System.out.println("Intersect char ---------------------------------------------------------");
        Checker CI = new CharIntersect(new CharRange('0', '9'), new CharRange('0', 'Z'));
        Assert(CI.getStartLengthOf("543ABC", 0, null), 1);
        Assert(CI.getStartLengthOf("567ABC", 2, null), 1);
        Assert(CI.getStartLengthOf("AB123ABC", 0, null), -1);
        Assert(CI.getStartLengthOf("01A3ABC", 2, null), -1);
        
        System.out.println("Union char ---------------------------------------------------------");
        Checker CU = new CharUnion(new CharRange('0', '6'), new CharRange('5', '9'));
        Assert(CU.getStartLengthOf("543ABC", 0, null), 1);
        Assert(CU.getStartLengthOf("567ABC", 2, null), 1);
        Assert(CU.getStartLengthOf("AB123ABC", 0, null), -1);
        Assert(CU.getStartLengthOf("01A3ABC", 2, null), -1);
        
        System.out.println("All Success.");
    }
    
}
