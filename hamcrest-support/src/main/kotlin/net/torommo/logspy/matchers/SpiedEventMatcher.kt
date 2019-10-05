package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import net.torommo.logspy.SpiedEvent.Level
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher

class SpiedEventMatcher {
    companion object {
        fun messageIs(value: String): Matcher<SpiedEvent> {
            return property(SpiedEvent::message, "message", `is`(value))
        }

        fun levelIs(value: Level): Matcher<SpiedEvent> {
            return property(SpiedEvent::level, "level", `is`(value))
        }

        fun exceptionIs(value: Throwable): Matcher<SpiedEvent> {
            return property(SpiedEvent::exception, "exception", `is`(value))
        }

        fun mdcIs(value: Map<String, String>): Matcher<SpiedEvent> {
            return property(SpiedEvent::mdc, "mdc", `is`(value))
        }
    }
}