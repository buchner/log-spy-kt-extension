package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue

class ThrowableSnapshotMatchers {
    companion object {
        @JvmStatic
        fun typeIs(value: String): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::type, `is`(value))
        }

        @JvmStatic
        fun messageIs(value: String?): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::message, `is`(value))
        }

        @JvmStatic
        fun causeThat(matcher: Matcher<ThrowableSnapshot>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, allOf(notNullValue(), matcher))
        }

        @JvmStatic
        fun noCause(): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, nullValue())
        }

        @JvmStatic
        @SafeVarargs
        fun suppressedContains(
            matcher: Matcher<ThrowableSnapshot>,
            vararg others: Matcher<ThrowableSnapshot>
        ): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::suppressed, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun stackContains(
            matcher: Matcher<StackTraceElementSnapshot>,
            vararg others: Matcher<StackTraceElementSnapshot>
        ): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::stackTrace, contains(matcher, *others))
        }
    }
}