package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

public class Test_01_Char {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = new TimeRecordRule();
    
    @Test
    public void testSingleChar() {
        var charSingle = new CharSingle('5');
        assertThat( 1, charSingle.getStartLengthOf("555222",   0, null));
        assertThat( 1, charSingle.getStartLengthOf("555222",   2, null));
        assertThat(-1, charSingle.getStartLengthOf("00555222", 0, null));
        assertThat(-1, charSingle.getStartLengthOf("5505222",  2, null));
    }
    
    @Test
    public void testRangeChar() {
        var charRange = new CharRange('0', '9');
        assertThat( 1, charRange.getStartLengthOf("543ABC",   0, null));
        assertThat( 1, charRange.getStartLengthOf("567ABC",   2, null));
        assertThat(-1, charRange.getStartLengthOf("AB123ABC", 0, null));
        assertThat(-1, charRange.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testClassChar() {
        var classChar = PredefinedCharClasses.Java_Digit;
        assertThat( 1, classChar.getStartLengthOf("543ABC",   0, null));
        assertThat( 1, classChar.getStartLengthOf("567ABC",   2, null));
        assertThat(-1, classChar.getStartLengthOf("AB123ABC", 0, null));
        assertThat(-1, classChar.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testNotChar() {
        var charNot = new CharNot(new CharRange('A', 'Z'));
        assertThat( 1, charNot.getStartLengthOf("543ABC",   0, null));
        assertThat( 1, charNot.getStartLengthOf("567ABC",   2, null));
        assertThat(-1, charNot.getStartLengthOf("AB123ABC", 0, null));
        assertThat(-1, charNot.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharIntersect() {
        var charIntersect = new CharIntersect(new CharRange('0', '9'), new CharRange('0', 'Z'));
        assertThat( 1, charIntersect.getStartLengthOf("543ABC",   0, null));
        assertThat( 1, charIntersect.getStartLengthOf("567ABC",   2, null));
        assertThat(-1, charIntersect.getStartLengthOf("AB123ABC", 0, null));
        assertThat(-1, charIntersect.getStartLengthOf("01A3ABC",  2, null));
    }
    
    @Test
    public void testCharUnion() {
        var charUnion = new CharUnion(new CharRange('0', '6'), new CharRange('5', '9'));
        assertThat( 1, charUnion.getStartLengthOf("543ABC",   0, null));
        assertThat( 1, charUnion.getStartLengthOf("567ABC",   2, null));
        assertThat(-1, charUnion.getStartLengthOf("AB123ABC", 0, null));
        assertThat(-1, charUnion.getStartLengthOf("01A3ABC",  2, null));
    }
    
}
