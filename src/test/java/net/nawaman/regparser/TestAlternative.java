package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

public class TestAlternative {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var checker = new CheckerAlternative(new WordChecker("555"), new WordChecker("222"));
        validate( 3, checker.getStartLengthOf("555222",   0, null));
        validate(-1, checker.getStartLengthOf("555222",   2, null));
        validate( 3, checker.getStartLengthOf("222A555",  0, null));
        validate( 3, checker.getStartLengthOf("555A222",  0, null));
        validate(-1, checker.getStartLengthOf("00555222", 0, null));
        validate(-1, checker.getStartLengthOf("5505222",  0, null));
    }
    
}
