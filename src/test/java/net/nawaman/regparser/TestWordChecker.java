package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

public class TestWordChecker {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var wordChecker = new WordChecker("555");
        assertThat( 3, wordChecker.getStartLengthOf("555222",   0, null));
        assertThat(-1, wordChecker.getStartLengthOf("555222",   2, null));
        assertThat(-1, wordChecker.getStartLengthOf("00555222", 0, null));
        assertThat(-1, wordChecker.getStartLengthOf("5505222",  0, null));
    }
    
}
