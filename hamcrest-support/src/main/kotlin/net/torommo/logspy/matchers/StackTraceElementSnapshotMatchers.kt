package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

class StackTraceElementSnapshotMatchers {
    companion object {

        @JvmStatic
        fun declaringClass(matcher: Matcher<String>): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::declaringClass, matcher)
        }

        @JvmStatic
        fun methodName(matcher: Matcher<String>): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::methodName, matcher)
        }
    }
}