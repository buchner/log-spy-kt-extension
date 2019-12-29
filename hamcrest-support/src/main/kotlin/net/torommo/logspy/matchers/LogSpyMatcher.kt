package net.torommo.logspy.matchers

import net.torommo.logspy.LogSpy
import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains

class LogSpyMatcher {
    companion object {
        @JvmStatic
        @SafeVarargs
        fun errorsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::errors, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun warningsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::warnings, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun infosContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::infos, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun debugsContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::debugs, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun tracesContains(matcher: Matcher<String>, vararg others: Matcher<String>): Matcher<LogSpy> {
            return property(LogSpy::traces, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun exceptionsContains(
            matcher: Matcher<ThrowableSnapshot>,
            vararg others: Matcher<ThrowableSnapshot>
        ): Matcher<LogSpy> {
            return property(LogSpy::exceptions, contains(matcher, *others))
        }

        @JvmStatic
        @SafeVarargs
        fun eventsContains(matcher: Matcher<SpiedEvent>, vararg others: Matcher<SpiedEvent>): Matcher<LogSpy> {
            return property(LogSpy::events, contains(matcher, *others))
        }
    }
}