package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

public class TestCharChecker {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testSingleChar() {
        var charSingle = new CharSingle('5');
        validate( 1, charSingle.getStartLengthOf("555222",   0, null));
        validate( 1, charSingle.getStartLengthOf("555222",   2, null));
        validate(-1, charSingle.getStartLengthOf("00555222", 0, null));
        validate(-1, charSingle.getStartLengthOf("5505222",  2, null));
    }
    
    @Test
    public void testRangeChar() {
        var charRange = new CharRange('0', '9');
        validate( 1, charRange.getStartLengthOf("543ABC",   0, null));
        validate( 1, charRange.getStartLengthOf("567ABC",   2, null));
        validate(-1, charRange.getStartLengthOf("AB123ABC", 0, null));
        validate(-1, charRange.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testClassChar() {
        var classChar = PredefinedCharClasses.Java_Digit;
        validate( 1, classChar.getStartLengthOf("543ABC",   0, null));
        validate( 1, classChar.getStartLengthOf("567ABC",   2, null));
        validate(-1, classChar.getStartLengthOf("AB123ABC", 0, null));
        validate(-1, classChar.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testNotChar() {
        var charNot = new CharNot(new CharRange('A', 'Z'));
        validate( 1, charNot.getStartLengthOf("543ABC",   0, null));
        validate( 1, charNot.getStartLengthOf("567ABC",   2, null));
        validate(-1, charNot.getStartLengthOf("AB123ABC", 0, null));
        validate(-1, charNot.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharIntersect() {
        var charIntersect = new CharIntersect(new CharRange('0', '9'), new CharRange('0', 'Z'));
        validate( 1, charIntersect.getStartLengthOf("543ABC",   0, null));
        validate( 1, charIntersect.getStartLengthOf("567ABC",   2, null));
        validate(-1, charIntersect.getStartLengthOf("AB123ABC", 0, null));
        validate(-1, charIntersect.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharUnion() {
        var charUnion = new CharUnion(new CharRange('0', '6'), new CharRange('5', '9'));
        validate( 1, charUnion.getStartLengthOf("543ABC",   0, null));
        validate( 1, charUnion.getStartLengthOf("567ABC",   2, null));
        validate(-1, charUnion.getStartLengthOf("AB123ABC", 0, null));
        validate(-1, charUnion.getStartLengthOf("01A3ABC",  2, null));
    }
    
}
