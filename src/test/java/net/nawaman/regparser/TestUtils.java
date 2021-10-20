package net.nawaman.regparser;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

public class TestUtils {
    
    static public void assertThat(int expected, int actual) {
        if (actual == expected) {
            return;
        }
        assertEquals(expected, actual);
    }
    
    static public void assertThat(String expected, Object actual) {
        if (Objects.equals(actual, expected)) {
            return;
        }
        
        var actualString = Util.toString(actual);
        assertEquals(expected, actualString);
    }
    
}
