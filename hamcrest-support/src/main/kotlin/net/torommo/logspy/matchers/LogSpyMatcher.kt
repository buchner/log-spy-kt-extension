package net.torommo.logspy.matchers

import net.torommo.logspy.LogSpy
import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

class LogSpyMatcher {
    companion object {
        @JvmStatic
        fun errors(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::errors, matcher)
        }

        @JvmStatic
        fun warnings(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::warnings, matcher)
        }

        @JvmStatic
        fun infos(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::infos, matcher)
        }

        @JvmStatic
        fun debugs(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::debugs, matcher)
        }

        @JvmStatic
        fun traces(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::traces, matcher)
        }

        @JvmStatic
        fun exceptions(matcher: Matcher<Iterable<ThrowableSnapshot>>): Matcher<LogSpy> {
            return property(LogSpy::exceptions, matcher)
        }

        @JvmStatic
        fun events(matcher: Matcher<Iterable<SpiedEvent>>): Matcher<LogSpy> {
            return property(LogSpy::events, matcher)
        }
    }
}