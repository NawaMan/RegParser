package net.nawaman.regparser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.nawaman.regparser.checkers.TestAlternative;
import net.nawaman.regparser.checkers.TestCharChecker;
import net.nawaman.regparser.checkers.TestCheckerFixeds;
import net.nawaman.regparser.checkers.TestJavaChecker;
import net.nawaman.regparser.checkers.TestWordChecker;

@RunWith(Suite.class)
@SuiteClasses({
    TestAlternative.class,
    TestCharChecker.class,
    TestCheckerFixeds.class,
    TestGreediness.class,
    TestJavaChecker.class,
    TestName.class,
    TestParseResult.class,
    TestRegParser.class,
    TestRegParserCompiler1.class,
    TestRegParserCompiler2.class,
    TestSelfContain.class,
    TestSpeed.class,
    TestType.class,
    TestTypeProviderExtensible.class,
    TestUsages.class,
    TestWordChecker.class
})
public class AllRegParserTests {
    
}
