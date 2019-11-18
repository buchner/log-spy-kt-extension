package net.torommo.logspy.demo;

import net.torommo.logspy.ByLiteral;
import net.torommo.logspy.ByType;
import net.torommo.logspy.LogSpy;
import net.torommo.logspy.LogSpyExtension;
import net.torommo.logspy.matchers.LogSpyMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.torommo.logspy.matchers.LogSpyMatcher.infosContains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(LogSpyExtension.class)
class DemoTest {
    @Test
    void logByType(@ByType(TestObject.class) LogSpy spy) {
        new TestObject().doSomething();

        assertThat(spy, infosContains(containsString("Something was done.")));
    }

    @Test
    void logByName(@ByLiteral("net.torommo.logspy.demo.TestObject") LogSpy spy) {
        new TestObject().doSomething();

        assertThat(spy, infosContains(containsString("Something was done.")));
    }
}
