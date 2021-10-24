package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CharIntersect;
import net.nawaman.regparser.checkers.CharNot;
import net.nawaman.regparser.checkers.CharRange;
import net.nawaman.regparser.checkers.CharSingle;
import net.nawaman.regparser.checkers.CharUnion;

public class TestCharChecker {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testSingleChar() {
        var charSingle = new CharSingle('5');
        validate( 1, charSingle.startLengthOf("555222",   0, null));
        validate( 1, charSingle.startLengthOf("555222",   2, null));
        validate(-1, charSingle.startLengthOf("00555222", 0, null));
        validate(-1, charSingle.startLengthOf("5505222",  2, null));
    }
    
    @Test
    public void testRangeChar() {
        var charRange = new CharRange('0', '9');
        validate( 1, charRange.startLengthOf("543ABC",   0, null));
        validate( 1, charRange.startLengthOf("567ABC",   2, null));
        validate(-1, charRange.startLengthOf("AB123ABC", 0, null));
        validate(-1, charRange.startLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testClassChar() {
        var classChar = PredefinedCharClasses.Java_Digit;
        validate( 1, classChar.startLengthOf("543ABC",   0, null));
        validate( 1, classChar.startLengthOf("567ABC",   2, null));
        validate(-1, classChar.startLengthOf("AB123ABC", 0, null));
        validate(-1, classChar.startLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testNotChar() {
        var charNot = new CharNot(new CharRange('A', 'Z'));
        validate( 1, charNot.startLengthOf("543ABC",   0, null));
        validate( 1, charNot.startLengthOf("567ABC",   2, null));
        validate(-1, charNot.startLengthOf("AB123ABC", 0, null));
        validate(-1, charNot.startLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharIntersect() {
        var charIntersect = new CharIntersect(new CharRange('0', '9'), new CharRange('0', 'Z'));
        validate( 1, charIntersect.startLengthOf("543ABC",   0, null));
        validate( 1, charIntersect.startLengthOf("567ABC",   2, null));
        validate(-1, charIntersect.startLengthOf("AB123ABC", 0, null));
        validate(-1, charIntersect.startLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharUnion() {
        var charUnion = new CharUnion(new CharRange('0', '6'), new CharRange('5', '9'));
        validate( 1, charUnion.startLengthOf("543ABC",   0, null));
        validate( 1, charUnion.startLengthOf("567ABC",   2, null));
        validate(-1, charUnion.startLengthOf("AB123ABC", 0, null));
        validate(-1, charUnion.startLengthOf("01A3ABC",  2, null));
    }
    
}
