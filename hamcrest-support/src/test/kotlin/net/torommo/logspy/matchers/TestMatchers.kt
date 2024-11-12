package net.torommo.logspy.matchers

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

internal class AlwaysMatchMatcher<T>(private val description: String = "can be anything") :
    BaseMatcher<T>() {
    override fun describeTo(description: Description?) {
        description?.appendText(this.description)
    }

    override fun matches(actual: Any?): Boolean {
        return true
    }
}

internal class NeverMatchMatcher<T>(
    private val mismatchDescription: String = "test mismatch description",
    private val description: String = "will never match",
) : BaseMatcher<T>() {
    override fun describeTo(description: Description?) {
        description?.appendText(this.description)
    }

    override fun describeMismatch(
        item: Any?,
        description: Description?,
    ) {
        description?.appendText(mismatchDescription)
    }

    override fun matches(actual: Any?): Boolean {
        return false
    }
}
