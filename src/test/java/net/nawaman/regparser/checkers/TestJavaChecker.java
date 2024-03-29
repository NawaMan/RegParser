package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.result.ParseResult;

public class TestJavaChecker {
    
    @SuppressWarnings("serial")
    static public class DigitChecker implements Checker {
        static public final DigitChecker instance = new DigitChecker();
        
        static public final DigitChecker getInstance() {
            return DigitChecker.instance;
        }
        
        public DigitChecker() {
        }
        
        public int startLengthOf(CharSequence string, int offset, ParserTypeProvider typeProvider) {
            return this.startLengthOf(string, offset, typeProvider, null);
        }
        
        public int startLengthOf(CharSequence string, int offset, ParserTypeProvider typeProvider, ParseResult result) {
            return DigitChecker.getLengthOf(string, offset, typeProvider, result);
        }
        
        // Returns the length of the detected digits.
        static public int getLengthOf(CharSequence string, int offset, ParserTypeProvider typeProvider, ParseResult result) {
            for (int i = offset; i < string.length(); i++) {
                char c = string.charAt(i);
                if (!((c >= '0') && (c <= '9')))
                    return i - offset;
            }
            return 0;
        }
        
        @Override
        public final Boolean isDeterministic() {
            return true;
        }
        
        public Checker optimize() {
            return this;
        }
        
        private int hashCode = 0;
        
        @Override
        public int hashCode() {
            if (hashCode != 0) {
                return hashCode;
            }
            
            hashCode = System.identityHashCode(this);
            return hashCode;
        }
    }
    
    @Test
    public void testJavaChecker() {
        // Point to a static method
        var parser = compile("!javaChecker(`net.nawaman.regparser.checkers.TestJavaChecker.DigitChecker->getLengthOf`)!");
        var result = parser.parse("0123456fdgd");
        validate("\n"
               + "00 - => [    7] = <NoName>        :javaChecker      = \"0123456\"\n"
               + ". 00 => [    7] = <NoName>        :javaChecker      = \"0123456\"", result);
        
        
        // Point to the class
        parser = compile("!javaChecker(`net.nawaman.regparser.checkers.TestJavaChecker.DigitChecker`)!");
        result = parser.parse("012345fdgd");
        validate("\n"
               + "00 - => [    6] = <NoName>        :javaChecker      = \"012345\"\n"
               + ". 00 => [    6] = <NoName>        :javaChecker      = \"012345\"", result);
        
        
        // Point to the static field
        parser = compile("!javaChecker(`net.nawaman.regparser.checkers.TestJavaChecker.DigitChecker::instance`)!");
        result = parser.parse("01234fdgd");
        validate("\n"
               + "00 - => [    5] = <NoName>        :javaChecker      = \"01234\"\n"
               + ". 00 => [    5] = <NoName>        :javaChecker      = \"01234\"", result);
        
        
        // Point to the factory method.
        parser = compile("!javaChecker(`net.nawaman.regparser.checkers.TestJavaChecker.DigitChecker::getInstance()`)!");
        result = parser.parse("0123fdgd");
        validate("\n"
               + "00 - => [    4] = <NoName>        :javaChecker      = \"0123\"\n"
               + ". 00 => [    4] = <NoName>        :javaChecker      = \"0123\"", result);
        
        
        result = parser.parse("012fdgd");
        validate("\n"
               + "00 - => [    3] = <NoName>        :javaChecker      = \"012\"\n"
               + ". 00 => [    3] = <NoName>        :javaChecker      = \"012\"", result);
    }
    
}
