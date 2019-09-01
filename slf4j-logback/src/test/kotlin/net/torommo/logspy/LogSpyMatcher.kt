package net.torommo.logspy

import net.torommo.logspy.PropertyMatcher.Companion.property
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

        fun exceptionsContains(matcher: Matcher<Throwable>, vararg others: Matcher<Throwable>): Matcher<LogSpy> {
            return property(LogSpy::exceptions, "exceptions", contains(matcher, *others))
        }

        fun eventsContains(matcher: Matcher<SpiedEvent>, vararg others: Matcher<SpiedEvent>): Matcher<LogSpy> {
            return property(LogSpy::events, "events", contains(matcher, *others))
        }
    }
}