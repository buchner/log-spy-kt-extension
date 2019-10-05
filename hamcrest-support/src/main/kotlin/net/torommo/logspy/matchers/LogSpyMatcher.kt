package net.torommo.logspy.matchers

import net.torommo.logspy.LogSpy
import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains

class LogSpyMatcher {
    companion object {
        fun errorsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::errors, "errors", contains(matcher, *others))
        }

        fun warningsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::warnings, "warnings", contains(matcher, *others))
        }

        fun infosContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::infos, "infos", contains(matcher, *others))
        }

        fun debugsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::debugs, "debugs", contains(matcher, *others))
        }

        fun tracesContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::traces, "traces", contains(matcher, *others))
        }

        fun exceptionsContains(
            matcher: Matcher<ThrowableSnapshot>,
            vararg others: Matcher<ThrowableSnapshot>
        ): Matcher<LogSpy> {
            return property(LogSpy::exceptions, "exceptions", contains(matcher, *others))
        }

        fun eventsContains(matcher: Matcher<SpiedEvent>, vararg others: Matcher<SpiedEvent>): Matcher<LogSpy> {
            return property(LogSpy::events, "events", contains(matcher, *others))
        }
    }
}