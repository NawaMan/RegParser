package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

public class TestAlternative {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var checker = new CheckerAlternative(new WordChecker("555"), new WordChecker("222"));
        assertThat( 3, checker.getStartLengthOf("555222",   0, null));
        assertThat(-1, checker.getStartLengthOf("555222",   2, null));
        assertThat( 3, checker.getStartLengthOf("222A555",  0, null));
        assertThat( 3, checker.getStartLengthOf("555A222",  0, null));
        assertThat(-1, checker.getStartLengthOf("00555222", 0, null));
        assertThat(-1, checker.getStartLengthOf("5505222",  0, null));
    }
    
}
