package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.TestUtils.validate;
import static net.nawaman.regparser.checkers.CheckerAlternative.either;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.TimeRecordRule;

public class TestAlternative {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var checker = either(new WordChecker("555")).or(new WordChecker("222")).build();
        validate( 3, checker.startLengthOf("555222",   0, null));
        validate(-1, checker.startLengthOf("555222",   2, null));
        validate( 3, checker.startLengthOf("222A555",  0, null));
        validate( 3, checker.startLengthOf("555A222",  0, null));
        validate(-1, checker.startLengthOf("00555222", 0, null));
        validate(-1, checker.startLengthOf("5505222",  0, null));
    }
    
}
