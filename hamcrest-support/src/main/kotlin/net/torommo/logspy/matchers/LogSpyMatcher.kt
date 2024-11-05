package net.torommo.logspy.matchers

import net.torommo.logspy.LogSpy
import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.PropertyMatcher.Companion.property
import org.hamcrest.Matcher

/** Matchers for the [LogSpy]. */
public class LogSpyMatcher {
    public companion object {
        @JvmStatic
        public fun errors(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::errors, matcher)
        }

        @JvmStatic
        public fun warnings(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::warnings, matcher)
        }

        @JvmStatic
        public fun infos(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::infos, matcher)
        }

        @JvmStatic
        public fun debugs(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::debugs, matcher)
        }

        @JvmStatic
        public fun traces(matcher: Matcher<Iterable<String>>): Matcher<LogSpy> {
            return property(LogSpy::traces, matcher)
        }

        @JvmStatic
        public fun exceptions(matcher: Matcher<Iterable<ThrowableSnapshot>>): Matcher<LogSpy> {
            return property(LogSpy::exceptions, matcher)
        }

        @JvmStatic
        public fun events(matcher: Matcher<Iterable<SpiedEvent>>): Matcher<LogSpy> {
            return property(LogSpy::events, matcher)
        }
    }
}
