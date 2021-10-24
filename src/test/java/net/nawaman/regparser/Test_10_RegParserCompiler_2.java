package net.nawaman.regparser;

import java.util.Random;

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

public class Test_10_RegParserCompiler_2 {
    
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
        
        RegParser   RP    = null;
        ParseResult PR    = null;
        Object      O     = null;
        Random      R     = new Random();
        PType       Type  = null;
        String      TName = null;
        String      Text  = null;
        
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
        
        System.out.println("Test# 9 - RegParserCompiler 2 ----------------------------------");
        
        // Escape ------------------------------------------------------------------------------------------------------
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Escape ----------------------------------------------------------------------------------");
        
        TName = RPTEscape.Name;
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        char c = '~';
        PR = RP.match("\\" + c, MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Random ----------------");
        
        c  = RPCompiler_ParserTypes.Escapable.charAt(R.nextInt(RPCompiler_ParserTypes.Escapable.length()));
        PR = RP.match("\\" + c, MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Escape Oct ------------------------------------------------------------------------------");
        
        TName = RPTEscapeOct.Name;
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        c  = 'n';
        PR = RP.match("\\0" + ((c / (8 * 8)) % 8) + "" + ((c / 8) % 8) + "" + (c % 8), MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Random ----------------");
        
        c  = (char) (R.nextInt(126 - 32) + 32);
        PR = RP.match("\\0" + ((c / (8 * 8)) % 8) + "" + ((c / 8) % 8) + "" + (c % 8), MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Escape Hex ------------------------------------------------------------------------------");
        
        TName = RPTEscapeHex.Name;
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        c  = 'm';
        PR = RP.match("\\x" + RPTEscapeHex.HEX.charAt((c / 16) % 16) + "" + RPTEscapeHex.HEX.charAt(c % 16), MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Random ----------------");
        c  = (char) (R.nextInt(126 - 32) + 32);
        PR = RP.match("\\x" + RPTEscapeHex.HEX.charAt((c / 16) % 16) + "" + RPTEscapeHex.HEX.charAt(c % 16), MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Escape Unicode --------------------------------------------------------------------------");
        
        TName = RPTEscapeUnicode.Name;
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        c  = 'm';
        PR = RP.match("\\u" + RPTEscapeHex.HEX.charAt((c / (16 * 16 * 16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt((c / (16 * 16)) % 16) + "" + RPTEscapeHex.HEX.charAt((c / (16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt(c % 16), MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Random ----------------");
        c  = (char) (R.nextInt('ฮ' - 'ก') + 'ก');
        PR = RP.match("\\u" + RPTEscapeHex.HEX.charAt((c / (16 * 16 * 16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt((c / (16 * 16)) % 16) + "" + RPTEscapeHex.HEX.charAt((c / (16)) % 16) + ""
                + RPTEscapeHex.HEX.charAt(c % 16), MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(O, "" + c);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("CharClass -------------------------------------------------------------------------------");
        
        RP = RegParser.newRegParser(RPCompiler_ParserTypes.PredefinedCheckers);
        if (!IsQuiet)
            System.out.println(RP);
        PR = RP.match("\\jd", MyRPTP);
        if (!IsQuiet)
            System.out.println((PR == null) ? "null" : PR.toString());
        O = RPCompiler_ParserTypes.getCharClass(PR, 0);
        if (!IsQuiet)
            System.out.println(O);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match(".", MyRPTP)), PredefinedCharClasses.Any);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\d", MyRPTP)), PredefinedCharClasses.Digit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\D", MyRPTP)), PredefinedCharClasses.NonDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\s", MyRPTP)), PredefinedCharClasses.WhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\S", MyRPTP)), PredefinedCharClasses.NonWhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\b", MyRPTP)), PredefinedCharClasses.Blank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\B", MyRPTP)), PredefinedCharClasses.NonBlank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\w", MyRPTP)), PredefinedCharClasses.Word);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\W", MyRPTP)), PredefinedCharClasses.NonWord);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\o", MyRPTP)), PredefinedCharClasses.OctalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\O", MyRPTP)), PredefinedCharClasses.NonOctalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\x", MyRPTP)), PredefinedCharClasses.HexadecimalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\X", MyRPTP)), PredefinedCharClasses.NonHexadecimalDigit);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Lower}", MyRPTP)),
                PredefinedCharClasses.LowerCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Upper}", MyRPTP)),
                PredefinedCharClasses.UpperCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{ASCII}", MyRPTP)), PredefinedCharClasses.ASCII);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Alpha}", MyRPTP)), PredefinedCharClasses.Alphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Alnum}", MyRPTP)),
                PredefinedCharClasses.AlphabetAndDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Punct}", MyRPTP)), PredefinedCharClasses.Punctuation);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Graph}", MyRPTP)), PredefinedCharClasses.Visible);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Print}", MyRPTP)), PredefinedCharClasses.Printable);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{Blank}", MyRPTP)), PredefinedCharClasses.Blank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{ODigit}", MyRPTP)), PredefinedCharClasses.OctalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\p{XDigit}", MyRPTP)),
                PredefinedCharClasses.HexadecimalDigit);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jd", MyRPTP)), PredefinedCharClasses.Java_Digit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jD", MyRPTP)), PredefinedCharClasses.Java_NonDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\js", MyRPTP)), PredefinedCharClasses.Java_WhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jS", MyRPTP)), PredefinedCharClasses.Java_NonWhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jw", MyRPTP)), PredefinedCharClasses.Java_Word);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jW", MyRPTP)), PredefinedCharClasses.Java_NonWord);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Lower}", MyRPTP)),
                PredefinedCharClasses.Java_LowerCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Upper}", MyRPTP)),
                PredefinedCharClasses.Java_UpperCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{ASCII}", MyRPTP)), PredefinedCharClasses.Java_ASCII);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Alpha}", MyRPTP)),
                PredefinedCharClasses.Java_Alphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Alnum}", MyRPTP)),
                PredefinedCharClasses.Java_AlphabetAndDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Punct}", MyRPTP)),
                PredefinedCharClasses.Java_Punctuation);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Graph}", MyRPTP)),
                PredefinedCharClasses.Java_Visible);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Print}", MyRPTP)),
                PredefinedCharClasses.Java_Printable);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Blank}", MyRPTP)), PredefinedCharClasses.Java_Blank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Cntrl}", MyRPTP)),
                PredefinedCharClasses.Java_ControlCharacter);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{XDigit}", MyRPTP)),
                PredefinedCharClasses.Java_HexadecimalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{InGreek}", MyRPTP)),
                PredefinedCharClasses.Java_Greek);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("\\jp{Sc}", MyRPTP)),
                PredefinedCharClasses.Java_CurrencySimbol);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Any:]", MyRPTP)), PredefinedCharClasses.Any);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Digit:]", MyRPTP)), PredefinedCharClasses.Digit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonDigit:]", MyRPTP)), PredefinedCharClasses.NonDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:WhiteSpace:]", MyRPTP)),
                PredefinedCharClasses.WhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonWhiteSpace:]", MyRPTP)),
                PredefinedCharClasses.NonWhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Word:]", MyRPTP)), PredefinedCharClasses.Word);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonWord:]", MyRPTP)), PredefinedCharClasses.NonWord);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Blank:]", MyRPTP)), PredefinedCharClasses.Blank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonBlank:]", MyRPTP)), PredefinedCharClasses.NonBlank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:OctDigit:]", MyRPTP)), PredefinedCharClasses.OctalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonOctDigit:]", MyRPTP)),
                PredefinedCharClasses.NonOctalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:HexDigit:]", MyRPTP)),
                PredefinedCharClasses.HexadecimalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:NonHexDigit:]", MyRPTP)),
                PredefinedCharClasses.NonHexadecimalDigit);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:LowerCaseAlphabet:]", MyRPTP)),
                PredefinedCharClasses.LowerCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:UpperCaseAlphabet:]", MyRPTP)),
                PredefinedCharClasses.UpperCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:ASCII:]", MyRPTP)), PredefinedCharClasses.ASCII);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Alphabet:]", MyRPTP)), PredefinedCharClasses.Alphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:AlphabetAndDigit:]", MyRPTP)),
                PredefinedCharClasses.AlphabetAndDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Punctuation:]", MyRPTP)),
                PredefinedCharClasses.Punctuation);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Visible:]", MyRPTP)), PredefinedCharClasses.Visible);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:Printable:]", MyRPTP)), PredefinedCharClasses.Printable);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JAny:]", MyRPTP)), PredefinedCharClasses.Java_Any);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JDigit:]", MyRPTP)), PredefinedCharClasses.Java_Digit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JNonDigit:]", MyRPTP)),
                PredefinedCharClasses.Java_NonDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JWhiteSpace:]", MyRPTP)),
                PredefinedCharClasses.Java_WhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JNonWhiteSpace:]", MyRPTP)),
                PredefinedCharClasses.Java_NonWhiteSpace);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JWord:]", MyRPTP)), PredefinedCharClasses.Java_Word);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JNonWord:]", MyRPTP)),
                PredefinedCharClasses.Java_NonWord);
        
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JLowerCaseAlphabet:]", MyRPTP)),
                PredefinedCharClasses.Java_LowerCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JUpperCaseAlphabet:]", MyRPTP)),
                PredefinedCharClasses.Java_UpperCaseAlphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JASCII:]", MyRPTP)), PredefinedCharClasses.Java_ASCII);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JAlphabet:]", MyRPTP)),
                PredefinedCharClasses.Java_Alphabet);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JAlphabetAndDigit:]", MyRPTP)),
                PredefinedCharClasses.Java_AlphabetAndDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JPunctuation:]", MyRPTP)),
                PredefinedCharClasses.Java_Punctuation);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JVisible:]", MyRPTP)),
                PredefinedCharClasses.Java_Visible);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JPrintable:]", MyRPTP)),
                PredefinedCharClasses.Java_Printable);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JBlank:]", MyRPTP)), PredefinedCharClasses.Java_Blank);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JControlCharacter:]", MyRPTP)),
                PredefinedCharClasses.Java_ControlCharacter);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JHexadecimalDigit:]", MyRPTP)),
                PredefinedCharClasses.Java_HexadecimalDigit);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JGreek:]", MyRPTP)), PredefinedCharClasses.Java_Greek);
        Assert(RPCompiler_ParserTypes.getCharClass(RP.match("[:JCurrencySimbol:]", MyRPTP)),
                PredefinedCharClasses.Java_CurrencySimbol);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Type ------------------------------------------------------------------------------------");
        
        TName = RPTType.Name;
        Type  = MyRPTP.getType(TName);
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        PR = RP.match("!Text!", MyRPTP);
        //System.out.println((PR == null)?"null":PR.toDetail());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        Assert(Type.compile("!text!", null, MyRPTP).toString(), "!text!");
        Assert(Type.compile("!$text!", null, MyRPTP).toString(), "!$text!");
        Assert(Type.compile("!text~!", null, MyRPTP).toString(), "!text~!");
        Assert(Type.compile("!text[]!", null, MyRPTP).toString(), "!text[]!");
        Assert(Type.compile("!text()!", null, MyRPTP).toString(), "!text!");
        Assert(Type.compile("!text('a')!", null, MyRPTP).toString(), "!text(\"a\")!");
        Assert(Type.compile("!text('a\\\"')!", null, MyRPTP).toString(), "!text(\"a\\\"\")!");
        Assert(Type.compile("!text(`a'`)!", null, MyRPTP).toString(), "!text(\"a\\\'\")!");
        Assert(Type.compile("!$text~!", null, MyRPTP).toString(), "!$text~!");
        Assert(Type.compile("!$text[]!", null, MyRPTP).toString(), "!$text[]!");
        Assert(Type.compile("!$text()!", null, MyRPTP).toString(), "!$text!");
        Assert(Type.compile("!$text('a')!", null, MyRPTP).toString(), "!$text(\"a\")!");
        Assert(Type.compile("!text~[]!", null, MyRPTP).toString(), "!text~[]!");
        Assert(Type.compile("!text~()!", null, MyRPTP).toString(), "!text~!");
        Assert(Type.compile("!text~('a')!", null, MyRPTP).toString(), "!text~(\"a\")!");
        Assert(Type.compile("!text~[]!", null, MyRPTP).toString(), "!text~[]!");
        Assert(Type.compile("!text[]()!", null, MyRPTP).toString(), "!text[]!");
        Assert(Type.compile("!text[]('a')!", null, MyRPTP).toString(), "!text[](\"a\")!");
        Assert(Type.compile("!$text~[]('Na\\'wa')!", null, MyRPTP).toString(), "!$text~[](\"Na\\'wa\")!");
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Type ------------------------------------------------------------------------------------");
        
        TName = RPTQuantifier.Name;
        Type  = MyRPTP.getType(TName);
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        PR = RP.match("{5,10}*", MyRPTP);
        if (PR == null)
            throw new AssertionError("PR should not be null.");
        if (!IsQuiet)
            System.out.println(PR.toString());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(O);
        
        Assert(Type.compile("?", null, MyRPTP).toString(), "?");
        Assert(Type.compile("*", null, MyRPTP).toString(), "*");
        Assert(Type.compile("+", null, MyRPTP).toString(), "+");
        Assert(Type.compile("?+", null, MyRPTP).toString(), "?+");
        Assert(Type.compile("*+", null, MyRPTP).toString(), "*+");
        Assert(Type.compile("++", null, MyRPTP).toString(), "++");
        Assert(Type.compile("?*", null, MyRPTP).toString(), "?*");
        Assert(Type.compile("**", null, MyRPTP).toString(), "**");
        Assert(Type.compile("+*", null, MyRPTP).toString(), "+*");
        Assert(Type.compile("{5}", null, MyRPTP).toString(), "{5}");
        Assert(Type.compile("{5,}", null, MyRPTP).toString(), "{5,}");
        Assert(Type.compile("{,5}", null, MyRPTP).toString(), "{,5}");
        Assert(Type.compile("{2,5}", null, MyRPTP).toString(), "{2,5}");
        Assert(Type.compile("{ 2 , 5 }", null, MyRPTP).toString(), "{2,5}");
        Assert(Type.compile("{ 2 , 5 }*", null, MyRPTP).toString(), "{2,5}*");
        
        Assert(((Quantifier) Type.compile("?", null, MyRPTP)).getLowerBound(), 0);
        Assert(((Quantifier) Type.compile("?", null, MyRPTP)).getUpperBound(), 1);
        Assert(((Quantifier) Type.compile("*", null, MyRPTP)).getLowerBound(), 0);
        Assert(((Quantifier) Type.compile("*", null, MyRPTP)).getUpperBound(), -1);
        Assert(((Quantifier) Type.compile("+", null, MyRPTP)).getLowerBound(), 1);
        Assert(((Quantifier) Type.compile("+", null, MyRPTP)).getUpperBound(), -1);
        Assert(((Quantifier) Type.compile("{5}", null, MyRPTP)).getLowerBound(), 5);
        Assert(((Quantifier) Type.compile("{5}", null, MyRPTP)).getUpperBound(), 5);
        Assert(((Quantifier) Type.compile("{5,}", null, MyRPTP)).getLowerBound(), 5);
        Assert(((Quantifier) Type.compile("{5,}", null, MyRPTP)).getUpperBound(), -1);
        Assert(((Quantifier) Type.compile("{,5}", null, MyRPTP)).getLowerBound(), 0);
        Assert(((Quantifier) Type.compile("{,5}", null, MyRPTP)).getUpperBound(), 5);
        Assert(((Quantifier) Type.compile("{2,5}", null, MyRPTP)).getLowerBound(), 2);
        Assert(((Quantifier) Type.compile("{2,5}", null, MyRPTP)).getUpperBound(), 5);
        Assert(((Quantifier) Type.compile("?+", null, MyRPTP)).getGreediness(), Greediness.Maximum);
        Assert(((Quantifier) Type.compile("?*", null, MyRPTP)).getGreediness(), Greediness.Minimum);
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("Range -----------------------------------------------------------------------------------");
        
        TName = RPTRange.Name;
        Type  = MyRPTP.getType(TName);
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        PR = RP.match("b-z", MyRPTP);
        AssertNotNull(PR);
        if (!IsQuiet)
            System.out.println(PR.toString());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP);
        if (!IsQuiet)
            System.out.println(O);
        
        Assert(Type.compile("a", null, MyRPTP).toString(), "[a]");
        Assert(Type.compile("a-d", null, MyRPTP).toString(), "[a-d]");
        Assert(Type.compile("\\\n-d", null, MyRPTP).toString(), "[\\n-d]");
        Assert(Type.compile("\\(-\\)", null, MyRPTP).toString(), "[\\(-\\)]");
        Assert(Type.compile("\\ -\\)", null, MyRPTP).toString(), "[\\ -\\)]");
        
        if (!IsQuiet)
            System.out.println();
        System.out.println("CharSet ---------------------------------------------------------------------------------");
        
        TName = RPTCharSetItem.Name;
        Type  = MyRPTP.getType(TName);
        Text  = "[^a-zasdfg[A-Z][0-9].\\s\\jp{Blank}]&&[[:JDigit:]]";
        
        RP = RegParser.newRegParser(new PTypeRef.Simple(TName));
        if (!IsQuiet)
            System.out.println(RP);
        PR = RP.match(Text, MyRPTP);
        AssertNotNull(PR);
        if (!IsQuiet)
            System.out.println(PR.toString());
        O = MyRPTP.getType(TName).compile(PR, null, null, MyRPTP).toString();
        if (!IsQuiet)
            System.out.println(Text);
        if (!IsQuiet)
            System.out.println(O);
        
        Assert(Type.compile(Text, null, MyRPTP).toString(),
                "[[^[[a-z][asdfg][A-Z][0-9].[\\ \\t\\n\\r\\\\f][:SpaceOrTab:]]]&&[:Digit:]]");
        
        /* */
        if (!IsQuiet)
            System.out.println();
        System.out.println("RegParser--------------------------------------------------------------------------------");
        
        TName = RPTRegParser.Name;
        Type  = MyRPTP.getType(TName);
        RegParser RT = null;
        
        Text = "abc";
        RT   = RegParser.newRegParser(Text);
        Assert(RT.toString(), "abc");
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.match("abc", MyRPTP).getEndPosition(), 3);
        Assert(RT.match("def", MyRPTP), null);
        
        Text = "(a|b|c)";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "(a|b|c)");
        Assert(RT.match("a", MyRPTP).getEndPosition(), 1);
        Assert(RT.match("b", MyRPTP).getEndPosition(), 1);
        Assert(RT.match("c", MyRPTP).getEndPosition(), 1);
        Assert(RT.match("d", MyRPTP), null);
        
        Text = "Col(o|ou)?r";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "Col(o|ou)?r");
        Assert(RT.match("Colr", MyRPTP).getEndPosition(), 4);
        Assert(RT.match("Color", MyRPTP).getEndPosition(), 5);
        Assert(RT.match("Colour", MyRPTP).getEndPosition(), 6);
        Assert(RT.match("Shape", MyRPTP), null);
        
        MyRPTP.addRPType(new PTIdentifier());
        Text = "(var|int)\\b+!$Identifier!\\b*=\\b*[0-9]+\\b*;";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "(var|int)[\\ \\t]+(!$Identifier!)[\\ \\t]*=[\\ \\t]*[0-9]+[\\ \\t]*;");
        Assert(RT.match("var V1 = 45;", MyRPTP).getEndPosition(), 12);
        Assert(RT.match("var V1=45;", MyRPTP).getEndPosition(), 10);
        Assert(RT.match("var V1 = 5 ;", MyRPTP).getEndPosition(), 12);
        Assert(RT.match("int V1 = 5;", MyRPTP).getEndPosition(), 11);
        
        Text = "(^byte|short|int||long)";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "(^(byte|short|int||long))");
        
        MyRPTP.addRPType(PTBackRef.BackRef_Instance);
        MyRPTP.addRPType(PTBackRefCI.BackRefCI_Instance);
        Text = "(#X:~[:AlphabetAndDigit:]+~)\\-(#X;)\\-(#X;)";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "(#X:~[[0-9][a-z][A-Z]]+~)[\\-](!$BackRef?(\"#X\")!)[\\-](!$BackRef?(\"#X\")!)");
        Assert(RT.match("5-5-5", MyRPTP).getEndPosition(), 5);
        
        Text = "($X:~[:AlphabetAndDigit:]+~)\\-($X';)\\-($X';)";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.toString(), "($X:~[[0-9][a-z][A-Z]]+~)[\\-](!$BackRefCI?(\"$X\")!)[\\-](!$BackRefCI?(\"$X\")!)");
        Assert(RT.match("A-A-A", MyRPTP).getEndPosition(), 5);
        Assert(RT.match("a-A-a", MyRPTP).getEndPosition(), 5);
        
        RP   = RegParser.newRegParser(new PTypeRef.Simple(RPTRegParser.Name));
        Text = "one'Two'three";
        PR   = RP.parse(Text, MyRPTP);
        AssertNotNull(PR);
        if (!IsQuiet)
            System.out.println(PR.toString());
        RT = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        Assert(RT.match("oneTwothree", MyRPTP).getEndPosition(), 11);
        Assert(RT.match("oneTWOthree", MyRPTP).getEndPosition(), 11);
        
        Text = "'var'\\b+(#Name:!$Identifier!)\\b*=\\b*(#Value:~[0-9]*~)\\b*;";
        RT   = RegParser.newRegParser(Text);
        if (!IsQuiet)
            System.out.println(RT);
        PR = RT.match("var V1 = 50;", MyRPTP);
        AssertNotNull(PR);
        if (!IsQuiet)
            System.out.println(PR);
        if (!IsQuiet)
            System.out.println(PR.textOf("#Name"));
        if (!IsQuiet)
            System.out.println(PR.textOf("#Value"));
        
        if (!IsQuiet)
            System.out.println();
        if (!IsQuiet)
            System.out.println(RT);
        PR = RT.match("Var V2 = 750;", MyRPTP);
        AssertNotNull(PR);
        if (!IsQuiet)
            System.out.println(PR);
        if (!IsQuiet)
            System.out.println(PR.textOf("#Name"));
        if (!IsQuiet)
            System.out.println(PR.textOf("#Value"));
        
        /* End */
        System.out.println();
        System.out.println("All Success.");
    }
    
}
