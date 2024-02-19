package net.nawaman.regparser;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TimeRecordRule implements TestRule {
    
    public static final TimeRecordRule instance = new TimeRecordRule();
    
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                long startTime = System.currentTimeMillis();
                base.evaluate();
                System.out.println(description + ": " + (System.currentTimeMillis() - startTime) + " ms");
            }
        };
    }
    
}
