package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher
import org.hamcrest.Matchers.nullValue

class ThrowableSnapshotMatchers {
    companion object {

        @JvmStatic
        fun type(value: Matcher<String>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::type, value)
        }

        @JvmStatic
        fun message(matcher: Matcher<String>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::message, ClutterFreeNotNullMatcher(matcher))
        }

        @JvmStatic
        fun noMessage(): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::message, nullValue())
        }

        @JvmStatic
        fun cause(matcher: Matcher<ThrowableSnapshot>): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, ClutterFreeNotNullMatcher(matcher))
        }

        @JvmStatic
        fun noCause(): Matcher<ThrowableSnapshot> {
            return property(ThrowableSnapshot::cause, nullValue())
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