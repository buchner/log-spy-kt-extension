package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class StackTraceElementSnapshotMatchers {
    companion object {
        @JvmStatic
        fun declaringClassIs(value: String): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::declaringClass, "declaring class", `is`(value))
        }

        @JvmStatic
        fun methodNameIs(value: String): Matcher<StackTraceElementSnapshot> {
            return property(StackTraceElementSnapshot::methodName, "method name", `is`(value))
        }
    }
}