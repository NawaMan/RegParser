package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

public class TestName {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void test1() {
        var parser 
                = newRegParser(
                    RPEntry._new("#Name",
                        RegParser.newRegParser(
                            PredefinedCharClasses.Alphabet, 
                            PredefinedCharClasses.AlphabetAndDigit,
                            Quantifier.ZeroOrMore
                        )
                    ),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore, 
                    new CharSingle('['),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    RPEntry._new("#Index", PredefinedCharClasses.Digit, Quantifier.OneOrMore),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    new CharSingle(']'),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    RPEntry._new(
                        RegParser.newRegParser(
                            new CharSingle('='),
                            PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                            RPEntry._new("#Value", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore))
                        ),
                        Quantifier.ZeroOrOne
                    ),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore, new CharSingle(';')
                );
        
        var result = parser.parse("Var[55] = 70;");
        
        assertThat("[#Name, #Value, #Index]", result.getAllNames());
        
        assertThat("Var", result.textOf("#Name"));
        assertThat("5",   result.textOf("#Index"));
        assertThat("70",  result.textOf("#Value"));
        
        result = parser.parse("Var[55];");
        
        assertThat("[#Name, #Index]", result.getAllNames());
        
        assertThat("Var", result.textOf("#Name"));
        assertThat("5",   result.textOf("#Index"));
        assertThat(null,  result.textOf("#Value"));
    }
    
    @Test
    public void test2() {
        var parser 
                = RegParser.newRegParser(
                    new CharSingle('{'), 
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    RPEntry._new("#Value", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    RPEntry._new(
                        RegParser.newRegParser(
                            new CharSingle(','), 
                            PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                            RPEntry._new("#Value", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore))),
                        Quantifier.ZeroOrMore),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    new CharSingle(','),         Quantifier.ZeroOrOne,
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    new CharSingle('}'),
                    PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
                    new CharSingle(';'));
        var result = parser.parse("{ 5, 7, 454, 5 };");
        
        assertThat("[#Value]",            result.getAllNames());
        assertThat("[5,7,454,5]",         Util.toString(result.textsOf("#Value")));
        assertThat("[[5],[7],[454],[5]]", Util.toString(result.getAllOfStrMatchesByName("#Value")));
    }
    
}
