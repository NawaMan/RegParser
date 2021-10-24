package net.nawaman.regparser;

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

// Char Classes --------------------------------------------------------------------------------------------------------


public class Test_09_RegParserCompiler_1 {
    
    static public void Assert(Object pValue, Object pCorrectValue) {
        if (!Util.equal(pValue, pCorrectValue)) {
            System.out.println(
                    "It's " + Util.toString(pValue) + "\n but it should be " + Util.toString(pCorrectValue) + ".");
            
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
    
    static public void AssertNotNull(Object pValue) {
        if (pValue == null)
            throw new AssertionError();
    }
    
    static public void AssertNull(Object pValue) {
        if (pValue != null)
            throw new AssertionError();
    }
    
    static public void main(String... Args) {
        boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
        
        RegParser   RP = null;
        ParseResult PR = null;
        /* */
        PTypeProvider.Extensible MyRPTP = new PTypeProvider.Extensible();
        
        MyRPTP.addRPType(new PTTextCI());
        
        // Add the type
        MyRPTP.addRPType(new PTIdentifier());
        MyRPTP.addRPType(new PTStrLiteral());
        MyRPTP.addRPType(new RPTComment());
        MyRPTP.addRPType(new RPTType());
        MyRPTP.addRPType(new RPTQuantifier());
        MyRPTP.addRPType(new RPTRegParserItem());
        MyRPTP.addRPType(new RPTEscape());
        MyRPTP.addRPType(new RPTEscapeOct());
        MyRPTP.addRPType(new RPTEscapeHex());
        MyRPTP.addRPType(new RPTEscapeUnicode());
        MyRPTP.addRPType(new RPTRange());
        MyRPTP.addRPType(new RPTCharSetItem());
        MyRPTP.addRPType(new RPTRegParser());
        
        // Type --------------------------------------------------------------------------------------------------------
        
        System.out.println("Test# 9 - RegParserCompiler 1 ----------------------------------");
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(RPTType.Name));
        if (!IsQuiet)
            System.out.println(RP);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.parse("!int!", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.toString(),
                    "\n" + "00 - => [    5] = <NoName>        :Type             = \"!int!\"\n"
                            + ". 00 => [    1] = <NoName>        :<NoType>         = \"!\"\n"
                            + ". 01 => [    4] = #TypeName       :$Identifier      = \"int\"\n"
                            + ". 02 => [    5] = <NoName>        :<NoType>         = \"!\"");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.parse("!int", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        Assert(PR, null);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.parse("!int()!", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.toString(),
                    "\n" + "00 - => [    7] = <NoName>        :Type             = \"!int()!\"\n"
                            + ". 00 => [    1] = <NoName>        :<NoType>         = \"!\"\n"
                            + ". 01 => [    4] = #TypeName       :$Identifier      = \"int\"\n"
                            + ". 02 => [    6] = #Param          :<NoType>         = \"()\"\n"
                            + ". 03 => [    7] = <NoName>        :<NoType>         = \"!\"");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.parse("!int(`A string here`)!", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "!int(`A string here`)!");
            Assert(PR.getSubTextOf(0, 0), "!");
            Assert(PR.getSubTextOf(0, 1), "int");
            Assert(PR.getSubTextOf(0, 2), "(`A string here`)");
            Assert(PR.getSubTextOf(0, 2, 0), "(");
            Assert(PR.getSubTextOf(0, 2, 1), "`A string here`");
            Assert(PR.getSubTextOf(0, 2, 2), ")");
            Assert(PR.getSubTextOf(0, 3), "!");
        } else
            throw new AssertionError("PR should not be null.");
        
        // Item --------------------------------------------------------------------------------------------------------
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(RPTRegParserItem.Name), Quantifier.OneOrMore,
                PredefinedCharClasses.Any, Quantifier.ZeroOrMore);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match(".\\s\\D[:WhiteSpace:]a\\p{Blank}d", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), ".");
            Assert(PR.getSubTextOf(0, 0), ".");
            Assert(PR.getSubTextOf(1), "\\s");
            Assert(PR.getSubTextOf(1, 0), "\\s");
            Assert(PR.getSubTextOf(1, 0, 0), "\\");
            Assert(PR.getSubTextOf(1, 0, 1), "s");
            Assert(PR.getSubTextOf(2), "\\D");
            Assert(PR.getSubTextOf(2, 0), "\\D");
            Assert(PR.getSubTextOf(2, 0, 0), "\\");
            Assert(PR.getSubTextOf(2, 0, 1), "D");
            Assert(PR.getSubTextOf(3), "[:WhiteSpace:]");
            Assert(PR.getSubTextOf(3, 0), "[:WhiteSpace:]");
            Assert(PR.getSubTextOf(3, 0, 0), "[:");
            Assert(PR.getSubTextOf(3, 0, 1), "WhiteSpace");
            Assert(PR.getSubTextOf(3, 0, 2), ":]");
            Assert(PR.getSubTextOf(4), "a");
            Assert(PR.getSubTextOf(5), "\\p{Blank}");
            Assert(PR.getSubTextOf(5, 0), "\\p{Blank}");
            Assert(PR.getSubTextOf(5, 0, 0), "\\p{");
            Assert(PR.getSubTextOf(5, 0, 1), "Blank");
            Assert(PR.getSubTextOf(5, 0, 2), "}");
            Assert(PR.getSubTextOf(6), "d");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match(".\\s\\D\"\\'[:WhiteSpace:]\\.\\\\\\p{Blank}", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), ".");
            Assert(PR.getSubTextOf(0, 0), ".");
            Assert(PR.getSubTextOf(1), "\\s");
            Assert(PR.getSubTextOf(1, 0), "\\s");
            Assert(PR.getSubTextOf(1, 0, 0), "\\");
            Assert(PR.getSubTextOf(1, 0, 1), "s");
            Assert(PR.getSubTextOf(2), "\\D");
            Assert(PR.getSubTextOf(2, 0), "\\D");
            Assert(PR.getSubTextOf(2, 0, 0), "\\");
            Assert(PR.getSubTextOf(2, 0, 1), "D");
            Assert(PR.getSubTextOf(3), "\"");
            Assert(PR.getSubTextOf(4), "\\\'");
            Assert(PR.getSubTextOf(5), "[:WhiteSpace:]");
            Assert(PR.getSubTextOf(5, 0), "[:WhiteSpace:]");
            Assert(PR.getSubTextOf(5, 0, 0), "[:");
            Assert(PR.getSubTextOf(5, 0, 1), "WhiteSpace");
            Assert(PR.getSubTextOf(5, 0, 2), ":]");
            Assert(PR.getSubTextOf(6), "\\.");
            Assert(PR.getSubTextOf(7), "\\\\");
            Assert(PR.getSubTextOf(8), "\\p{Blank}");
            Assert(PR.getSubTextOf(8, 0), "\\p{Blank}");
            Assert(PR.getSubTextOf(8, 0, 0), "\\p{");
            Assert(PR.getSubTextOf(8, 0, 1), "Blank");
            Assert(PR.getSubTextOf(8, 0, 2), "}");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match(".\\s\\045\\x45\\u0045[a-g]s\\p{Blank}", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), ".");
            Assert(PR.getSubTextOf(0, 0), ".");
            Assert(PR.getSubTextOf(1), "\\s");
            Assert(PR.getSubTextOf(1, 0), "\\s");
            Assert(PR.getSubTextOf(1, 0, 0), "\\");
            Assert(PR.getSubTextOf(1, 0, 1), "s");
            Assert(PR.getSubTextOf(2), "\\045");
            Assert(PR.getSubTextOf(3), "\\x45");
            Assert(PR.getSubTextOf(4), "\\u0045");
            Assert(PR.getSubTextOf(5), "[a-g]");
            Assert(PR.getSubTextOf(5, 0), "[a-g]");
            Assert(PR.getSubTextOf(5, 0, 0), "[");
            Assert(PR.getSubTextOf(5, 0, 1), "a-g");
            Assert(PR.getSubTextOf(5, 0, 2), "]");
            Assert(PR.getSubTextOf(6), "s");
            Assert(PR.getSubTextOf(7), "\\p{Blank}");
            Assert(PR.getSubTextOf(7, 0), "\\p{Blank}");
            Assert(PR.getSubTextOf(7, 0, 0), "\\p{");
            Assert(PR.getSubTextOf(7, 0, 1), "Blank");
            Assert(PR.getSubTextOf(7, 0, 2), "}");
        } else
            throw new AssertionError("PR should not be null.");
        
        // Qunatifier --------------------------------------------------------------------------------------------------
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(RPTQuantifier.Name), Quantifier.OneOrMore,
                PredefinedCharClasses.Any, Quantifier.ZeroOrMore);
        if (!IsQuiet)
            System.out.println(RP);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("?+*{54}{5,}*{,7}+{12,65}{ 1 }{ 4 , }{ , 8}{2,6}*", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "?+");
            Assert(PR.getSubTextOf(0, 0), "?");
            Assert(PR.getSubTextOf(0, 1), "+");
            Assert(PR.getSubTextOf(1), "*");
            Assert(PR.getSubTextOf(2), "{54}");
            Assert(PR.getSubTextOf(2, 0), "{54}");
            Assert(PR.getSubTextOf(2, 0, 0), "{");
            Assert(PR.getSubTextOf(2, 0, 1), "54");
            Assert(PR.getSubTextOf(2, 0, 2), "}");
            Assert(PR.getSubTextOf(3), "{5,}*");
            Assert(PR.getSubTextOf(3, 0), "{5,}");
            Assert(PR.getSubTextOf(3, 0, 0), "{");
            Assert(PR.getSubTextOf(3, 0, 1), "5");
            Assert(PR.getSubTextOf(3, 0, 2), ",}");
            Assert(PR.getSubTextOf(3, 1), "*");
            Assert(PR.getSubTextOf(4), "{,7}+");
            Assert(PR.getSubTextOf(4, 0), "{,7}");
            Assert(PR.getSubTextOf(4, 0, 0), "{,");
            Assert(PR.getSubTextOf(4, 0, 1), "7");
            Assert(PR.getSubTextOf(4, 0, 2), "}");
            Assert(PR.getSubTextOf(4, 1), "+");
            Assert(PR.getSubTextOf(5), "{12,65}");
            Assert(PR.getSubTextOf(5, 0), "{12,65}");
            Assert(PR.getSubTextOf(5, 0, 0), "{");
            Assert(PR.getSubTextOf(5, 0, 1), "12");
            Assert(PR.getSubTextOf(5, 0, 2), ",");
            Assert(PR.getSubTextOf(5, 0, 3), "65");
            Assert(PR.getSubTextOf(5, 0, 4), "}");
            Assert(PR.getSubTextOf(6, 0), "{ 1 }");
            Assert(PR.getSubTextOf(6, 0, 0), "{ ");
            Assert(PR.getSubTextOf(6, 0, 1), "1");
            Assert(PR.getSubTextOf(6, 0, 2), " }");
            Assert(PR.getSubTextOf(7), "{ 4 , }");
            Assert(PR.getSubTextOf(7, 0), "{ 4 , }");
            Assert(PR.getSubTextOf(7, 0, 0), "{ ");
            Assert(PR.getSubTextOf(7, 0, 1), "4");
            Assert(PR.getSubTextOf(7, 0, 2), " , }");
            Assert(PR.getSubTextOf(8), "{ , 8}");
            Assert(PR.getSubTextOf(8, 0), "{ , 8}");
            Assert(PR.getSubTextOf(8, 0, 0), "{ , ");
            Assert(PR.getSubTextOf(8, 0, 1), "8");
            Assert(PR.getSubTextOf(8, 0, 2), "}");
            Assert(PR.getSubTextOf(9), "{2,6}*");
            Assert(PR.getSubTextOf(9, 0), "{2,6}");
            Assert(PR.getSubTextOf(9, 0, 0), "{");
            Assert(PR.getSubTextOf(9, 0, 1), "2");
            Assert(PR.getSubTextOf(9, 0, 2), ",");
            Assert(PR.getSubTextOf(9, 0, 3), "6");
            Assert(PR.getSubTextOf(9, 0, 4), "}");
            Assert(PR.getSubTextOf(9, 1), "*");
        } else
            throw new AssertionError("PR should not be null.");
        
        // CharSet -----------------------------------------------------------------------------------------------------
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(RPTCharSetItem.Name), Quantifier.OneOrMore,
                PredefinedCharClasses.Any, Quantifier.ZeroOrMore);
        if (!IsQuiet)
            System.out.println(RP);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("[a-bg-h.gfj\\s\\u0035-c\\s[:Any:][:Digit:]g\\jp{ASCII}]", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "[a-bg-h.gfj\\s\\u0035-c\\s[:Any:][:Digit:]g\\jp{ASCII}]");
            Assert(PR.getSubTextOf(0, 0), "[");
            Assert(PR.getSubTextOf(0, 1), "a-b");
            Assert(PR.getSubTextOf(0, 2), "g-h");
            Assert(PR.getSubTextOf(0, 3), ".");
            Assert(PR.getSubTextOf(0, 4), "g");
            Assert(PR.getSubTextOf(0, 5), "f");
            Assert(PR.getSubTextOf(0, 6), "j");
            Assert(PR.getSubTextOf(0, 7), "\\s");
            Assert(PR.getSubTextOf(0, 7, 0), "\\");
            Assert(PR.getSubTextOf(0, 7, 1), "s");
            Assert(PR.getSubTextOf(0, 8), "\\u0035-c");
            Assert(PR.getSubTextOf(0, 8, 0), "\\u0035");
            Assert(PR.getSubTextOf(0, 8, 1), "-");
            Assert(PR.getSubTextOf(0, 8, 2), "c");
            Assert(PR.getSubTextOf(0, 9), "\\s");
            Assert(PR.getSubTextOf(0, 9, 0), "\\");
            Assert(PR.getSubTextOf(0, 9, 1), "s");
            Assert(PR.getSubTextOf(0, 10), "[:Any:]");
            Assert(PR.getSubTextOf(0, 10, 0), "[:");
            Assert(PR.getSubTextOf(0, 10, 1), "Any");
            Assert(PR.getSubTextOf(0, 10, 2), ":]");
            Assert(PR.getSubTextOf(0, 11), "[:Digit:]");
            Assert(PR.getSubTextOf(0, 11, 0), "[:");
            Assert(PR.getSubTextOf(0, 11, 1), "Digit");
            Assert(PR.getSubTextOf(0, 11, 2), ":]");
            Assert(PR.getSubTextOf(0, 12), "g");
            Assert(PR.getSubTextOf(0, 13), "\\jp{ASCII}");
            Assert(PR.getSubTextOf(0, 13, 0), "\\jp{");
            Assert(PR.getSubTextOf(0, 13, 1), "ASCII");
            Assert(PR.getSubTextOf(0, 13, 2), "}");
            Assert(PR.getSubTextOf(0, 14), "]");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("[a[g-h]b]", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "[a[g-h]b]");
            Assert(PR.getSubTextOf(0, 0), "[");
            Assert(PR.getSubTextOf(0, 1), "a");
            Assert(PR.getSubTextOf(0, 2), "[g-h]");
            Assert(PR.getSubTextOf(0, 2, 0), "[");
            Assert(PR.getSubTextOf(0, 2, 1), "g-h");
            Assert(PR.getSubTextOf(0, 2, 2), "]");
            Assert(PR.getSubTextOf(0, 3), "b");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("[a[g-h]b]&&[d-h]&&[\\s]", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "[a[g-h]b]&&[d-h]&&[\\s]");
            Assert(PR.getSubTextOf(0, 0), "[");
            Assert(PR.getSubTextOf(0, 1), "a");
            Assert(PR.getSubTextOf(0, 2), "[g-h]");
            Assert(PR.getSubTextOf(0, 2, 0), "[");
            Assert(PR.getSubTextOf(0, 2, 1), "g-h");
            Assert(PR.getSubTextOf(0, 2, 2), "]");
            Assert(PR.getSubTextOf(0, 3), "b");
            Assert(PR.getSubTextOf(0, 4), "]");
            Assert(PR.getSubTextOf(0, 5), "&&");
            Assert(PR.getSubTextOf(0, 6), "[d-h]");
            Assert(PR.getSubTextOf(0, 6, 0), "[");
            Assert(PR.getSubTextOf(0, 6, 1), "d-h");
            Assert(PR.getSubTextOf(0, 6, 2), "]");
            Assert(PR.getSubTextOf(0, 7), "&&");
            Assert(PR.getSubTextOf(0, 8), "[\\s]");
            Assert(PR.getSubTextOf(0, 8, 0), "[");
            Assert(PR.getSubTextOf(0, 8, 1), "\\s");
            Assert(PR.getSubTextOf(0, 8, 1, 0), "\\");
            Assert(PR.getSubTextOf(0, 8, 1, 1), "s");
            Assert(PR.getSubTextOf(0, 8, 2), "]");
        } else
            throw new AssertionError("PR should not be null.");
        
        
        // More RegParser ----------------------------------------------------------------------------------------------
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(RPTRegParser.Name), Quantifier.OneOrMore);
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("ab(c.d)o(i|o)o\\)e[h-g]u\\(f", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "ab(c.d)o(i|o)o\\)e[h-g]u\\(f");
            Assert(PR.getSubTextOf(0, 0), "ab");
            Assert(PR.getSubTextOf(0, 1), "(c.d)");
            Assert(PR.getSubTextOf(0, 1, 0), "(c.d)");
            Assert(PR.getSubTextOf(0, 1, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 1), "c.d");
            Assert(PR.getSubTextOf(0, 1, 0, 1, 0), "c");
            Assert(PR.getSubTextOf(0, 1, 0, 1, 1), ".");
            Assert(PR.getSubTextOf(0, 1, 0, 1, 2), "d");
            Assert(PR.getSubTextOf(0, 1, 0, 2), ")");
            Assert(PR.getSubTextOf(0, 2), "o");
            Assert(PR.getSubTextOf(0, 3), "(i|o)");
            Assert(PR.getSubTextOf(0, 3, 0), "(i|o)");
            Assert(PR.getSubTextOf(0, 3, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 3, 0, 1), "i");
            Assert(PR.getSubTextOf(0, 3, 0, 2), "|");
            Assert(PR.getSubTextOf(0, 3, 0, 3), "o");
            Assert(PR.getSubTextOf(0, 3, 0, 4), ")");
            Assert(PR.getSubTextOf(0, 4), "o");
            Assert(PR.getSubTextOf(0, 5), "\\)");
            Assert(PR.getSubTextOf(0, 6), "e");
            Assert(PR.getSubTextOf(0, 7), "[h-g]");
            Assert(PR.getSubTextOf(0, 7, 0), "[h-g]");
            Assert(PR.getSubTextOf(0, 7, 0, 0), "[");
            Assert(PR.getSubTextOf(0, 7, 0, 1), "h-g");
            Assert(PR.getSubTextOf(0, 7, 0, 2), "]");
            Assert(PR.getSubTextOf(0, 8), "u");
            Assert(PR.getSubTextOf(0, 9), "\\(");
            Assert(PR.getSubTextOf(0, 10), "f");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("Col(o|ou)?r", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "Col(o|ou)?r");
            Assert(PR.getSubTextOf(0, 0), "Col");
            Assert(PR.getSubTextOf(0, 1), "(o|ou)?");
            Assert(PR.getSubTextOf(0, 1, 0), "(o|ou)");
            Assert(PR.getSubTextOf(0, 1, 0, 0), "(o|ou)");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 1), "o");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 2), "|");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 3), "ou");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4), ")");
            Assert(PR.getSubTextOf(0, 1, 1), "?");
            Assert(PR.getSubTextOf(0, 2), "r");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("Col(^o|ou|au||[aeiou])?r[^0-9]?s", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "Col(^o|ou|au||[aeiou])?r[^0-9]?s");
            Assert(PR.getSubTextOf(0, 0), "Col");
            Assert(PR.getSubTextOf(0, 1), "(^o|ou|au||[aeiou])?");
            Assert(PR.getSubTextOf(0, 1, 0), "(^o|ou|au||[aeiou])");
            Assert(PR.getSubTextOf(0, 1, 0, 0), "(^o|ou|au||[aeiou])");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 1), "^");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 2), "o");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 3), "|");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4), "ou");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 5), "|");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 6), "au");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 7), "||");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 8), "[aeiou]");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 9), ")");
            Assert(PR.getSubTextOf(0, 1, 1), "?");
            Assert(PR.getSubTextOf(0, 2), "r");
            Assert(PR.getSubTextOf(0, 3), "[^0-9]?");
            Assert(PR.getSubTextOf(0, 3, 0), "[^0-9]");
            Assert(PR.getSubTextOf(0, 3, 0, 0), "[^0-9]");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 0), "[");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 1), "^");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 2), "0-9");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 2, 0), "0");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 2, 1), "-");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 2, 2), "9");
            Assert(PR.getSubTextOf(0, 3, 0, 0, 3), "]");
            Assert(PR.getSubTextOf(0, 3, 1), "?");
            Assert(PR.getSubTextOf(0, 4), "s");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("var\\ (#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\\ =\\ (#Value:~[0-9]*~);", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "var\\ (#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)\\ =\\ (#Value:~[0-9]*~);");
            Assert(PR.getSubTextOf(0, 0), "var");
            Assert(PR.getSubTextOf(0, 1), "\\ ");
            Assert(PR.getSubTextOf(0, 1, 0), "\\ ");
            Assert(PR.getSubTextOf(0, 2), "(#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)");
            Assert(PR.getSubTextOf(0, 2, 0), "(#Name:~[a-zA-Z_][a-zA-Z_0-9]*~)");
            Assert(PR.getSubTextOf(0, 2, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 2, 0, 1), "#");
            Assert(PR.getSubTextOf(0, 2, 0, 2), "Name");
            Assert(PR.getSubTextOf(0, 2, 0, 3), ":");
            Assert(PR.getSubTextOf(0, 2, 0, 4), "~");
            Assert(PR.getSubTextOf(0, 2, 0, 5), "[a-zA-Z_][a-zA-Z_0-9]*");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0), "[a-zA-Z_]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0), "[a-zA-Z_]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0, 0), "[");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0, 1), "a-z");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0, 2), "A-Z");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0, 3), "_");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 0, 0, 4), "]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1), "[a-zA-Z_0-9]*");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0), "[a-zA-Z_0-9]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0), "[a-zA-Z_0-9]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 0), "[");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 1), "a-z");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 2), "A-Z");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 3), "_");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 4), "0-9");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 0, 0, 5), "]");
            Assert(PR.getSubTextOf(0, 2, 0, 5, 1, 1), "*");
            Assert(PR.getSubTextOf(0, 2, 0, 6), "~)");
            Assert(PR.getSubTextOf(0, 3), "\\ ");
            Assert(PR.getSubTextOf(0, 3, 0), "\\ ");
            Assert(PR.getSubTextOf(0, 4), "=");
            Assert(PR.getSubTextOf(0, 5), "\\ ");
            Assert(PR.getSubTextOf(0, 5, 0), "\\ ");
            Assert(PR.getSubTextOf(0, 6), "(#Value:~[0-9]*~)");
            Assert(PR.getSubTextOf(0, 6, 0), "(#Value:~[0-9]*~)");
            Assert(PR.getSubTextOf(0, 6, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 6, 0, 1), "#");
            Assert(PR.getSubTextOf(0, 6, 0, 2), "Value");
            Assert(PR.getSubTextOf(0, 6, 0, 3), ":");
            Assert(PR.getSubTextOf(0, 6, 0, 4), "~");
            Assert(PR.getSubTextOf(0, 6, 0, 5), "[0-9]*");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0), "[0-9]*");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 0), "[0-9]");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 0, 0), "[0-9]");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 0, 0, 0), "[");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 0, 0, 1), "0-9");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 0, 0, 2), "]");
            Assert(PR.getSubTextOf(0, 6, 0, 5, 0, 1), "*");
            Assert(PR.getSubTextOf(0, 6, 0, 6), "~)");
            Assert(PR.getSubTextOf(0, 7), ";");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("a(#Value:!byte()!)?a", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getSubTextOf(0), "a(#Value:!byte()!)?a");
            Assert(PR.getSubTextOf(0, 0), "a");
            Assert(PR.getSubTextOf(0, 1), "(#Value:!byte()!)?");
            Assert(PR.getSubTextOf(0, 1, 0), "(#Value:!byte()!)");
            Assert(PR.getSubTextOf(0, 1, 0), "(#Value:!byte()!)");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 1), "#");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 2), "Value");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 3), ":");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4), "!byte()!");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4, 0), "!");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4, 1), "byte");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4, 2), "()");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 4, 3), "!");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 5), ")");
            Assert(PR.getSubTextOf(0, 1, 1), "?");
            Assert(PR.getSubTextOf(0, 2), "a");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        String Text = "a(a)s)?a";
        PR = RP.parse(Text, MyRPTP);
        if (!IsQuiet)
            System.out.println(((PR == null) || (PR.getEndPosition() != Text.length())) ? "Not Match" : "Match");
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getEndPosition(), 5);
            
            Assert(PR.getSubTextOf(0), "a(a)s");
            Assert(PR.getSubTextOf(0, 0), "a");
            Assert(PR.getSubTextOf(0, 1), "(a)");
            Assert(PR.getSubTextOf(0, 1, 0), "(a)");
            Assert(PR.getSubTextOf(0, 1, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 1), "a");
            Assert(PR.getSubTextOf(0, 1, 0, 2), ")");
            Assert(PR.getSubTextOf(0, 2), "s");
        } else
            throw new AssertionError("PR should not be null.");
        
        if (!IsQuiet)
            System.out.println();
        Text = "a(a(s)?a";
        PR   = RP.parse(Text, MyRPTP);
        if (!IsQuiet)
            System.out.println(((PR == null) || (PR.getEndPosition() != Text.length())) ? "Not Match" : "Match");
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (PR != null) {
            Assert(PR.getEndPosition(), 8);
            
            Assert(PR.getSubTextOf(0), "a(a(s)?a");
            Assert(PR.getSubTextOf(0, 0), "a");
            Assert(PR.getSubTextOf(0, 1), "(a(s)?");
            Assert(PR.getSubTextOf(0, 1, 0), "(a(s)");
            Assert(PR.getSubTextOf(0, 1, 0, 0), "(a(s)");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 0), "(");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 1), "a(s");
            Assert(PR.getSubTextOf(0, 1, 0, 0, 2), ")");
            Assert(PR.getSubTextOf(0, 1, 1), "?");
            Assert(PR.getSubTextOf(0, 2), "a");
        } else
            throw new AssertionError("PR should not be null.");
        
        PR = RP.match("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])", MyRPTP);
        AssertNotNull(PR);
        
        PR = RP.match("[0-9]{1,3}", MyRPTP);
        AssertNotNull(PR);
        
        PR = RP.match("Set(Value)?", MyRPTP);
        AssertNotNull(PR);
        
        PR = RP.match("<(#Term:~[A-Z][A-Z0-9]*~)[^>]*>.*?</(#Term;)>", MyRPTP);
        AssertNull(PR);
        
        PR = RP.match("<(#Term:~[A-Z][A-Z0-9]*~)[^>]*>.**</(#Term;)>", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("a(#Value:!$byte[]()!)?a", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("a[abcdefg]a", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("< (#Term:~[A-Z][A-Z0-9]*~) [^>]* >.**</(#Term ; )>", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        
        if (!IsQuiet)
            System.out.println();
        PR = RP.match("ABC(*DEF*)/*G(*H*)I*/JKL", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        
        RegParser NewRP = RegParser.newRegParser("a/**/");
        System.out.println(NewRP);
        PR = NewRP.parse("a");
        System.out.println((PR == null) ? "null" : PR.toString());
        
        /* End */
        System.out.println();
        System.out.println("All Success.");
    }
    
}
