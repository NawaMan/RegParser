package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.newRegParser;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

public class TestJavaChecker {
    
    @SuppressWarnings("serial")
    static public class DigitChecker implements Checker {
        static public final DigitChecker Instance = new DigitChecker();
        
        static public final DigitChecker getInstance() {
            return DigitChecker.Instance;
        }
        
        public DigitChecker() {
        }
        
        public int startLengthOf(CharSequence string, int offset, PTypeProvider typeProvider) {
            return this.startLengthOf(string, offset, typeProvider, null);
        }
        
        public int startLengthOf(CharSequence string, int offset, PTypeProvider typeProvider, ParseResult result) {
            return DigitChecker.getLengthOf(string, offset, typeProvider, result);
        }
        
        // Returns the length of the detected digits.
        static public int getLengthOf(CharSequence string, int offset, PTypeProvider typeProvider, ParseResult result) {
            for (int i = offset; i < string.length(); i++) {
                char c = string.charAt(i);
                if (!((c >= '0') && (c <= '9')))
                    return i - offset;
            }
            return 0;
        }
        
        public Checker getOptimized() {
            return this;
        }
    }
    
    @Test
    public void testJavaChecker() {
        // Point to a static method
        var parser = newRegParser("!javaChecker(`net.nawaman.regparser.TestJavaChecker.DigitChecker->getLengthOf`)!");
        var result = parser.parse("0123456fdgd");
        validate("\n"
                + "00 - => [    7] = <NoName>        :javaChecker      = \"0123456\"\n"
                + ". 00 => [    7] = <NoName>        :javaChecker      = \"0123456\"",
                result);
        
        
        // Point to the class
        parser = newRegParser("!javaChecker(`net.nawaman.regparser.TestJavaChecker.DigitChecker`)!");
        result = parser.parse("012345fdgd");
        validate("\n"
                + "00 - => [    6] = <NoName>        :javaChecker      = \"012345\"\n"
                + ". 00 => [    6] = <NoName>        :javaChecker      = \"012345\"", result);
        
        
        // Point to the static field
        parser = newRegParser("!javaChecker(`net.nawaman.regparser.TestJavaChecker.DigitChecker::Instance`)!");
        result = parser.parse("01234fdgd");
        validate("\n"
                + "00 - => [    5] = <NoName>        :javaChecker      = \"01234\"\n"
                + ". 00 => [    5] = <NoName>        :javaChecker      = \"01234\"",
                result);
        
        
        // Point to the factory method.
        parser = newRegParser("!javaChecker(`net.nawaman.regparser.TestJavaChecker.DigitChecker::getInstance()`)!");
        result = parser.parse("0123fdgd");
        validate("\n"
                + "00 - => [    4] = <NoName>        :javaChecker      = \"0123\"\n"
                + ". 00 => [    4] = <NoName>        :javaChecker      = \"0123\"",
                result);
        
        
        result = parser.parse("012fdgd");
        validate("\n"
                + "00 - => [    3] = <NoName>        :javaChecker      = \"012\"\n"
                + ". 00 => [    3] = <NoName>        :javaChecker      = \"012\"",
                result);
    }
    
}
