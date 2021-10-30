package net.nawaman.regparser;

import static net.nawaman.regparser.Greediness.Possessive;
import static net.nawaman.regparser.PredefinedCharClasses.Alphabet;
import static net.nawaman.regparser.PredefinedCharClasses.AlphabetAndDigit;
import static net.nawaman.regparser.PredefinedCharClasses.Blank;
import static net.nawaman.regparser.PredefinedCharClasses.Digit;
import static net.nawaman.regparser.Quantifier.OneOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrMore;
import static net.nawaman.regparser.Quantifier.ZeroOrOne;
import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CharSingle;

public class TestName {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void test1() {
        var parser 
                = newRegParser(
                    RPEntry._new("#Name", newRegParser(Alphabet, AlphabetAndDigit, ZeroOrMore)),
                    Blank, ZeroOrMore, 
                    new CharSingle('['), 
                    Blank, ZeroOrMore, 
                    RPEntry._new("#Index", Digit, OneOrMore), 
                    Blank, ZeroOrMore, 
                    new CharSingle(']'),
                    Blank, ZeroOrMore,
                    RPEntry._new(
                        newRegParser(
                            new CharSingle('='),
                            Blank, ZeroOrMore,
                            RPEntry._new("#Value", newRegParser(Digit, OneOrMore))
                        ),
                        ZeroOrOne
                    ),
                    Blank, ZeroOrMore, new CharSingle(';')
                );
        
        var result = parser.parse("Var[55] = 70;");
        
        validate("[#Name, #Value, #Index]", result.names().toArray());
        
        validate("Var", result.textOf("#Name"));
        validate("5",   result.textOf("#Index"));
        validate("70",  result.textOf("#Value"));
        
        result = parser.parse("Var[55];");
        
        validate("[#Name, #Index]", result.names().toArray());
        
        validate("Var", result.textOf("#Name"));
        validate("5",   result.textOf("#Index"));
        validate(null,  result.textOf("#Value"));
    }
    
    @Test
    public void test2() {
        var parser 
                = newRegParser(
                    new CharSingle('{'), 
                    Blank, ZeroOrMore,
                    RPEntry._new("#Value", newRegParser(Digit, OneOrMore)),
                    Blank, ZeroOrMore,
                    RPEntry._new(
                        newRegParser(
                            new CharSingle(','), 
                            Blank, ZeroOrMore,
                            RPEntry._new("#Value", newRegParser(Digit, OneOrMore))
                        ), ZeroOrMore
                    ),
                    Blank,               ZeroOrMore,
                    new CharSingle(','), ZeroOrOne,
                    Blank,               ZeroOrMore,
                    new CharSingle('}'),
                    Blank,               ZeroOrMore,
                    new CharSingle(';'));
        
        var result = parser.parse("{ 5, 7, 454, 5 };");
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"{ \"\n"
                + "01 => [    3] = #Value          :<NoType>         = \"5\"\n"
                + "02 => [    5] = <NoName>        :<NoType>         = \", \"\n"
                + "03 => [    6] = #Value          :<NoType>         = \"7\"\n"
                + "04 => [    8] = <NoName>        :<NoType>         = \", \"\n"
                + "05 => [   11] = #Value          :<NoType>         = \"454\"\n"
                + "06 => [   13] = <NoName>        :<NoType>         = \", \"\n"
                + "07 => [   14] = #Value          :<NoType>         = \"5\"\n"
                + "08 => [   17] = <NoName>        :<NoType>         = \" };\"",
                result);
        
        validate("[#Value]",            result.names().toArray());
        validate("[5,7,454,5]",         Util.toString(result.textsOf("#Value")));
        validate("[[5],[7],[454],[5]]", Util.toString(result.stringMatchesOf("#Value")));
    }
    
    @Test
    public void test3() {
        var oneOrTwo = new Quantifier(1, 2, Possessive);
        var parser 
                = newRegParser(
                    new CharSingle('{'), 
                    Blank, ZeroOrMore,
                    RPEntry._new("#Value", newRegParser(Digit, oneOrTwo)),
                    Blank, ZeroOrMore,
                    RPEntry._new(
                        newRegParser(
                            new CharSingle(','), 
                            Blank, ZeroOrMore,
                            RPEntry._new("#Value", newRegParser(Digit, oneOrTwo), OneOrMore)
                        ), ZeroOrMore
                    ),
                    Blank,               ZeroOrMore,
                    new CharSingle(','), ZeroOrOne,
                    Blank,               ZeroOrMore,
                    new CharSingle('}'),
                    Blank,               ZeroOrMore,
                    new CharSingle(';'));
        
        var result = parser.parse("{ 5, 7, 456, 5 };");
        validate("\n"
                + "00 => [    2] = <NoName>        :<NoType>         = \"{ \"\n"
                + "01 => [    3] = #Value          :<NoType>         = \"5\"\n"
                + "02 => [    5] = <NoName>        :<NoType>         = \", \"\n"
                + "03 => [    6] = #Value          :<NoType>         = \"7\"\n"
                + "04 => [    8] = <NoName>        :<NoType>         = \", \"\n"
                + "05 => [   10] = #Value          :<NoType>         = \"45\"\n"
                + "06 => [   11] = #Value          :<NoType>         = \"6\"\n"
                + "07 => [   13] = <NoName>        :<NoType>         = \", \"\n"
                + "08 => [   14] = #Value          :<NoType>         = \"5\"\n"
                + "09 => [   17] = <NoName>        :<NoType>         = \" };\"",
                result);
        
        validate("[#Value]",              result.names().toArray());
        validate("[5,7,45,6,5]",          Util.toString(result.textsOf("#Value")));
        validate("[[5],[7],[45, 6],[5]]", Util.toString(result.stringMatchesOf("#Value")));
    }
    
}
