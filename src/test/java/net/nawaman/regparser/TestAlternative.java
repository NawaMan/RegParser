package net.nawaman.regparser;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.WordChecker;

public class TestAlternative {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var checker = new CheckerAlternative(new WordChecker("555"), new WordChecker("222"));
        validate( 3, checker.startLengthOf("555222",   0, null));
        validate(-1, checker.startLengthOf("555222",   2, null));
        validate( 3, checker.startLengthOf("222A555",  0, null));
        validate( 3, checker.startLengthOf("555A222",  0, null));
        validate(-1, checker.startLengthOf("00555222", 0, null));
        validate(-1, checker.startLengthOf("5505222",  0, null));
    }
    
}
