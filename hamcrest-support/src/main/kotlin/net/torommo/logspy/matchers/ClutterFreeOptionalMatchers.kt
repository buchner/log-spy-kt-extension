package net.torommo.logspy.matchers

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class ClutterFreeOptionalMatchers {
    companion object {
        /**
         * Creates a matcher that matches when the value is not `null` and the provided matcher matches.
         */
        @JvmStatic
        fun <T> present(matcher : Matcher<T>) : Matcher<T?> {
            return PresentMatcher(matcher)
        }

        /**
         * Creates a matcher that matches when the value is `null`.
         */
        @JvmStatic
        fun <T> absent() : Matcher<T?> {
            return AbsentMatcher()
        }
    }

    private class PresentMatcher<T>(private val delegate: Matcher<T>) : BaseMatcher<T?>() {
        override fun describeTo(description: Description?) {
            delegate.describeTo(description)
        }

        override fun describeMismatch(item: Any?, description: Description?) {
            if (item == null) {
                description?.appendDescriptionOf(delegate)
                description?.appendText(" was null")
            } else {
                delegate.describeMismatch(item, description)
            }
        }

        override fun matches(actual: Any?): Boolean {
            return if (actual == null) {
                false
            } else {
                delegate.matches(actual)
            }
        }
    }

    private class AbsentMatcher<T> : BaseMatcher<T?>() {
        override fun describeTo(description: Description?) {
            description?.appendText("absent")
        }

        override fun describeMismatch(item: Any?, description: Description?) {
            description?.appendText("was ")
            description?.appendValue(item)
        }

        override fun matches(actual: Any?): Boolean {
            return actual == null
        }
    }
}