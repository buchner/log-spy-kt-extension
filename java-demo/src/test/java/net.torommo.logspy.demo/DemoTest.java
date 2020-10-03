package net.torommo.logspy.demo;

import net.torommo.logspy.ByLiteral;
import net.torommo.logspy.ByType;
import net.torommo.logspy.LogSpy;
import net.torommo.logspy.LogSpyExtension;
import net.torommo.logspy.java.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Function;

import static net.torommo.logspy.java.LogSpyJavaExtensions.spyForLogger;
import static net.torommo.logspy.java.LogSpyJavaExtensions.spyOn;
import static net.torommo.logspy.matchers.IterableMatchers.containing;
import static net.torommo.logspy.matchers.LogSpyMatcher.infos;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(LogSpyExtension.class)
class DemoTest {

  @Test
  void logByTypeWithoutJunitExtension() {
    LogSpy spy = spyOn(TestObject.class, () -> new TestObject().doSomething());

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }

  @Test
  void logByNameWithoutJunitExtension() {
    LogSpy spy =
        spyOn(
            "net.torommo.logspy.demo.TestObject", () -> new TestObject().doSomething());

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }

  @Test
  void logByTypeDelayed() {
    Function<ThrowingRunnable, LogSpy> configuration = spyForLogger(TestObject.class);

    LogSpy spy = configuration.apply(() -> new TestObject().doSomething());

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }

  @Test
  void logByNameDelayed() {
    Function<ThrowingRunnable, LogSpy> configuration = spyForLogger("net.torommo.logspy.demo.TestObject");

    LogSpy spy = configuration.apply(() -> new TestObject().doSomething());

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }

  @Test
  void logByTypeWithJunitExtension(@ByType(TestObject.class) LogSpy spy) {
    new TestObject().doSomething();

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }

  @Test
  void logByNameWithJunitExtension(@ByLiteral("net.torommo.logspy.demo.TestObject") LogSpy spy) {
    new TestObject().doSomething();

    assertThat(spy, infos(containing(containsString("Something was done."))));
  }
}
