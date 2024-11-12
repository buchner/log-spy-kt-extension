package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

/** Matchers for properties of [SpiedEvent]. */
public class SpiedEventMatcher {
    public companion object {
        @JvmStatic
        public fun message(matcher: Matcher<String?>): Matcher<SpiedEvent> {
            return property(SpiedEvent::message, matcher)
        }

        @JvmStatic
        public fun level(matcher: Matcher<Level>): Matcher<SpiedEvent> {
            return property(SpiedEvent::level, matcher)
        }

        @JvmStatic
        public fun exception(matcher: Matcher<ThrowableSnapshot?>): Matcher<SpiedEvent> {
            return property(SpiedEvent::exception, matcher)
        }

        @JvmStatic
        public fun mdc(matcher: Matcher<Map<String, String>>): Matcher<SpiedEvent> {
            return property(SpiedEvent::mdc, matcher)
        }
    }
}
