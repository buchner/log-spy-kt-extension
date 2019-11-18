package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matcher

class SpiedEventMatcher {
    companion object {
        @JvmStatic
        fun messageIs(value: String): Matcher<SpiedEvent> {
            return property(SpiedEvent::message, "message", `is`(value))
        }

        @JvmStatic
        fun levelIs(value: Level): Matcher<SpiedEvent> {
            return property(SpiedEvent::level, "level", `is`(value))
        }

        @JvmStatic
        fun exceptionIs(value: ThrowableSnapshot): Matcher<SpiedEvent> {
            return exceptionWith(`is`(value))
        }

        @JvmStatic
        fun exceptionWith(matcher: Matcher<ThrowableSnapshot>): Matcher<SpiedEvent> {
            return property(SpiedEvent::exception, "exception", allOf(notNullValue(), matcher))
        }

        @JvmStatic
        fun mdcIs(value: Map<String, String>): Matcher<SpiedEvent> {
            return property(SpiedEvent::mdc, "mdc", `is`(value))
        }
    }
}