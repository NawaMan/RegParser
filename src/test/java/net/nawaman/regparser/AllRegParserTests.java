package net.nawaman.regparser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
