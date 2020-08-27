package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

class ThrowableSnapshotMatchers {
    companion object {

        @JvmStatic
        fun type(value: Matcher<String>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::type, value)
        }

        @JvmStatic
        fun message(matcher: Matcher<String?>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::message, matcher)
        }

        @JvmStatic
        fun cause(matcher: Matcher<ThrowableSnapshot?>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, matcher)
        }

        @JvmStatic
        @SafeVarargs
        fun suppressed(matcher: Matcher<Iterable<ThrowableSnapshot>>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::suppressed, matcher)
        }

        @JvmStatic
        @SafeVarargs
        fun stack(matcher: Matcher<Iterable<StackTraceElementSnapshot>>):
            Matcher<ThrowableSnapshot> {
                return property(ThrowableSnapshot::stackTrace, matcher)
            }
    }
}