package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

/** Matchers for properties of [StackTraceElement]. */
public class StackTraceElementSnapshotMatchers {
    public companion object {

        @JvmStatic
        public fun declaringClass(matcher: Matcher<String>): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::declaringClass, matcher)
        }

        @JvmStatic
        public fun methodName(matcher: Matcher<String>): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::methodName, matcher)
        }
    }
}