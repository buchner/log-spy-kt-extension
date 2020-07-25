package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

class SpiedEventMatcher {
    companion object {

        @JvmStatic
        fun message(matcher: Matcher<String>): Matcher<SpiedEvent> {
            return property(SpiedEvent::message, ClutterFreeNotNullMatcher(matcher))
        }

        @JvmStatic
        fun level(matcher: Matcher<Level>): Matcher<SpiedEvent> {
            return property(SpiedEvent::level, matcher)
        }

        @JvmStatic
        fun exception(matcher: Matcher<ThrowableSnapshot>): Matcher<SpiedEvent> {
            return property(SpiedEvent::exception, ClutterFreeNotNullMatcher(matcher))
        }

        @JvmStatic
        fun mdc(matcher: Matcher<Map<String, String>>): Matcher<SpiedEvent> {
            return property(SpiedEvent::mdc, matcher)
        }
    }
}