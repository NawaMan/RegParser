package net.nawaman.regparser.checkers;

import static net.nawaman.regparser.TestUtils.validate;

import org.junit.ClassRule;
import org.junit.Test;

import net.nawaman.regparser.TimeRecordRule;

public class TestWordChecker {
    
    @ClassRule
    public static TimeRecordRule timeRecordRule = TimeRecordRule.instance;
    
    @Test
    public void testWord() {
        var wordChecker = new WordChecker("555");
        validate( 3, wordChecker.startLengthOf("555222",   0, null));
        validate(-1, wordChecker.startLengthOf("555222",   2, null));
        validate(-1, wordChecker.startLengthOf("00555222", 0, null));
        validate(-1, wordChecker.startLengthOf("5505222",  0, null));
    }
    
}
