package net.nawaman.regparser;

public class Test_12_Chagnable_ParseResult {
    
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
    
    static public void main(String... Args) {
        
        boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
        
        RegParser   RP = RegParser.newRegParser("var[:WhiteSpace:]+($VarName:~[a-zA-Z_][a-zA-Z0-9_]*~)"
                + "[:WhiteSpace:]*[:=:][:WhiteSpace:]" + "($InvalidExpr:~[^[:;:]]*~)" + "[:;:]");
        ParseResult PR = RP.parse("var I = 15+5*2+5a;");
        
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        RegParser RPE = RegParser
                .newRegParser("(#ValidExpr:~($Operand:~[0-9]+~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)");
        
        PR.parseEntry(3, RPE, null);
        
        String FirstDetail_Org = PR.toString();
        
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        PR.flatEntry(3);
        
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser("var[:WhiteSpace:]+($VarName:~[a-zA-Z_][a-zA-Z0-9_]*~)"
                + "[:WhiteSpace:]*[:=:][:WhiteSpace:]"
                + "(#Expr+:~(#Operand:~($BeforeDot:~[0-9]+~)[:.:]($AfterDot:~[0-9]+~)~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)"
                + "[:;:]");
        PR = RP.parse("var I = 15.0*5;");
        
        if (!IsQuiet)
            System.out.println(PR.toString());
        if (!IsQuiet)
            System.out.println(PR.getSubOf(3, 0).toString());
        if (!IsQuiet)
            System.out.println(PR.getSubOf(3, 0).getRoot().toString());
        
        Assert(PR.toString(), PR.getSubOf(3, 0).getRoot().toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser("(#InvalidValue:~[^0-9]*~:~(#ValidValue:~ABC~)~)");
        PR = RP.parse("ABCD");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser("var[:WhiteSpace:]+($VarName:~[a-zA-Z_][a-zA-Z0-9_]*~)"
                + "[:WhiteSpace:]*[:=:][:WhiteSpace:]"
                + "($InvalidExpr:~[^[:;:]]*~:~(#ValidExpr:~($Operand:~[0-9]+~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)~)"
                + "[:;:]");
        PR = RP.parse("var I = 15+5*2+5a;");
        String FirstDetail_Res = PR.toString();
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        Assert(FirstDetail_Org, FirstDetail_Res);
        
        System.out.println("------------------------------------------------------------------");
        
        RP = RegParser.newRegParser("!textCI(`Te\\\"st`)!");
        PR = RP.parse("te\"st");
        if (!IsQuiet)
            System.out.println(PR.toString());
        
        System.out.println("END --------------------------------------------------------------");
    }
    
}
