package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;
import org.junit.ClassRule;
import org.junit.Test;

public class TestParseResult {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testChagnableResult() {
        var parser = newRegParser("var[:WhiteSpace:]+($VarName:~[a-zA-Z_]*~)[:WhiteSpace:]*[:=:][:WhiteSpace:]($InvalidExpr:~[^[:;:]]*~)[:;:]");
        var result = parser.parse("var I = 15+5*2+5a;");
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 => [   17] = $InvalidExpr    :<NoType>         = \"15+5*2+5a\"\n"
                + "04 => [   18] = <NoName>        :<NoType>         = \";\"",
                result);
        
        // Further parse entry #3.
        var exprParser = newRegParser("(#ValidExpr:~($Operand:~[0-9]+~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)");
        result.parseEntry(3, exprParser);
        
        validate("\n"
                + "00 - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 - => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 - => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 - => [   16] = #ValidExpr      :<NoType>         = \"15+5*2+5\"\n"
                + ". 00 => [   10] = $Operand        :<NoType>         = \"15\"\n"
                + ". 01 => [   11] = $Operator       :<NoType>         = \"+\"\n"
                + ". 02 => [   12] = $Operand        :<NoType>         = \"5\"\n"
                + ". 03 => [   13] = $Operator       :<NoType>         = \"*\"\n"
                + ". 04 => [   14] = $Operand        :<NoType>         = \"2\"\n"
                + ". 05 => [   15] = $Operator       :<NoType>         = \"+\"\n"
                + ". 06 => [   16] = $Operand        :<NoType>         = \"5\"\n"
                + "04 - => [   17] = $InvalidExpr    :<NoType>         = \"a\"\n"
                + "05 - => [   18] = <NoName>        :<NoType>         = \";\"",
                result);
        
        // Flatten entry #3
        result.flatEntry(3);
        validate("\n"
                + "00 => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 => [   10] = $Operand        :<NoType>         = \"15\"\n"
                + "04 => [   11] = $Operator       :<NoType>         = \"+\"\n"
                + "05 => [   12] = $Operand        :<NoType>         = \"5\"\n"
                + "06 => [   13] = $Operator       :<NoType>         = \"*\"\n"
                + "07 => [   14] = $Operand        :<NoType>         = \"2\"\n"
                + "08 => [   15] = $Operator       :<NoType>         = \"+\"\n"
                + "09 => [   16] = $Operand        :<NoType>         = \"5\"\n"
                + "10 => [   17] = $InvalidExpr    :<NoType>         = \"a\"\n"
                + "11 => [   18] = <NoName>        :<NoType>         = \";\"", result.toString());
    }
    
    @Test
    public void testSubAndRoot() {
        var parser = newRegParser("var[:WhiteSpace:]+($VarName:~[a-zA-Z_][a-zA-Z0-9_]*~)[:WhiteSpace:]*[:=:][:WhiteSpace:]"
                            + "(#Expr+:~(#Operand:~($BeforeDot:~[0-9]+~)[:.:]($AfterDot:~[0-9]+~)~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)"
                            + "[:;:]");
        var result = parser.parse("var I = 15.0*5;");
        validate("\n"
                + "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 - - => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 - - => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 - - => [   14] = #Expr+          :<NoType>         = \"15.0*5\"\n"
                + ". 00 - => [   12] = #Operand        :<NoType>         = \"15.0\"\n"
                + ". . 00 => [   10] = $BeforeDot      :<NoType>         = \"15\"\n"
                + ". . 01 => [   11] = <NoName>        :<NoType>         = \".\"\n"
                + ". . 02 => [   12] = $AfterDot       :<NoType>         = \"0\"\n"
                + ". 01 - => [   13] = $Operator       :<NoType>         = \"*\"\n"
                + ". 02 - => [   14] = $Operand        :<NoType>         = \"5\"\n"
                + "04 - - => [   15] = <NoName>        :<NoType>         = \";\"",
                result);
        validate("\n"
                + "00 => [   10] = $BeforeDot      :<NoType>         = \"15\"\n"
                + "01 => [   11] = <NoName>        :<NoType>         = \".\"\n"
                + "02 => [   12] = $AfterDot       :<NoType>         = \"0\"",
                result.getSubOf(3, 0));
        validate("\n"
                + "00 - - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 - - => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 - - => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 - - => [   14] = #Expr+          :<NoType>         = \"15.0*5\"\n"
                + ". 00 - => [   12] = #Operand        :<NoType>         = \"15.0\"\n"
                + ". . 00 => [   10] = $BeforeDot      :<NoType>         = \"15\"\n"
                + ". . 01 => [   11] = <NoName>        :<NoType>         = \".\"\n"
                + ". . 02 => [   12] = $AfterDot       :<NoType>         = \"0\"\n"
                + ". 01 - => [   13] = $Operator       :<NoType>         = \"*\"\n"
                + ". 02 - => [   14] = $Operand        :<NoType>         = \"5\"\n"
                + "04 - - => [   15] = <NoName>        :<NoType>         = \";\"",
                result.getSubOf(3, 0).getRoot());
    }
    
    @Test
    public void testTwoStage() {
        // The second stage claims some of the spaces.
        var parser = newRegParser("(#InvalidValue:~[^0-9]*~:~(#ValidValue:~ABC~)~)");
        var result = parser.parse("ABCD");
        // ABCD is claimed in the first stage, ABC is claimed for the second step leaving D as still with the first step
        validate("\n"
                + "00 => [    3] = #ValidValue     :<NoType>         = \"ABC\"\n"
                + "01 => [    4] = #InvalidValue   :<NoType>         = \"D\"",
                result);
    }
    
    @Test
    public void testTwoStage_longer() {
        var parser = newRegParser(
                        "var[:WhiteSpace:]+"
                      + "($VarName:~[a-zA-Z_][a-zA-Z0-9_]*~)[:WhiteSpace:]*"
                      + "[:=:][:WhiteSpace:]"
                      + "($InvalidExpr:~[^[:;:]]*~:~(#ValidExpr:~($Operand:~[0-9]+~)(($Operator:~[[:+:][:*:]]~)($Operand:~[0-9]+~))*~)~)"
                      + "[:;:]");
        var result = parser.parse("var I = 15+5*2+5a;");
        // `15+5*2+5a` is claimed as `$InvalidExpr` but only `15+5*2+5` is claimed as a ValidExpr
        // ... leaving `a` is an invalid.
        validate("\n"
                + "00 - => [    4] = <NoName>        :<NoType>         = \"var \"\n"
                + "01 - => [    5] = $VarName        :<NoType>         = \"I\"\n"
                + "02 - => [    8] = <NoName>        :<NoType>         = \" = \"\n"
                + "03 - => [   16] = #ValidExpr      :<NoType>         = \"15+5*2+5\"\n"
                + ". 00 => [   10] = $Operand        :<NoType>         = \"15\"\n"
                + ". 01 => [   11] = $Operator       :<NoType>         = \"+\"\n"
                + ". 02 => [   12] = $Operand        :<NoType>         = \"5\"\n"
                + ". 03 => [   13] = $Operator       :<NoType>         = \"*\"\n"
                + ". 04 => [   14] = $Operand        :<NoType>         = \"2\"\n"
                + ". 05 => [   15] = $Operator       :<NoType>         = \"+\"\n"
                + ". 06 => [   16] = $Operand        :<NoType>         = \"5\"\n"
                + "04 - => [   17] = $InvalidExpr    :<NoType>         = \"a\"\n"
                + "05 - => [   18] = <NoName>        :<NoType>         = \";\"", result);
    }
    
}
