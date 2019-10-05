package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.notNullValue

class ThrowableSnapshotMatchers {
    companion object {
        fun typeIs(value: String): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::type, "type", `is`(value))
        }

        fun messageIs(value: String?): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::message, "message", `is`(value))
        }

        fun causeThat(matcher: Matcher<ThrowableSnapshot>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, "cause", allOf(notNullValue(), matcher))
        }

        fun suppressedContains(
            matcher: Matcher<ThrowableSnapshot>,
            vararg others: Matcher<ThrowableSnapshot>
        ): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::suppressed, "suppressed", contains(matcher, *others))
        }

        fun stackContains(
            matcher: Matcher<StackTraceElementSnapshot>,
            vararg others: Matcher<StackTraceElementSnapshot>
        ): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::stackTrace, "stackTrace", contains(matcher, *others))
        }
    }
}