package net.nawaman.regparser;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

public class TestUtils {
    
    static public void validate(int expected, int actual) {
        if (actual == expected) {
            return;
        }
        assertEquals(expected, actual);
    }
    
    static public void validate(String expected, Object actual) {
        if (Objects.equals(actual, expected)) {
            return;
        }
        
        var actualString = Util.toString(actual);
        if (Objects.equals(actual, expected)) {
            return;
        }
        
        var expectedRegEx = "^\\Q" + expected + "\\E$";
        if (actualString.matches(expectedRegEx)) {
            return;
        }
        assertEquals(expected, actualString);
    }
    
}
