package net.torommo.logspy.matchers

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

internal class ClutterFreeNotNullMatcher<T>(private val delegate: Matcher<T>) : BaseMatcher<T?>() {
    override fun describeTo(description: Description?) {
        delegate.describeTo(description)
    }

    override fun describeMismatch(item: Any?, description: Description?) {
        if (item == null) {
            description?.appendText("was null")
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