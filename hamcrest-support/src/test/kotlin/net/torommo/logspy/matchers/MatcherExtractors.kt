package net.torommo.logspy.matchers

import org.hamcrest.Matcher
import org.hamcrest.StringDescription

internal fun <T> Matcher<T>.description(): String {
    val description = StringDescription()
    this.describeTo(description)
    return description.toString()
}

internal fun <T> Matcher<T>.mismatchDescriptionFor(value: T?): String {
    val description = StringDescription()
    this.describeMismatch(value, description)
    return description.toString()
}