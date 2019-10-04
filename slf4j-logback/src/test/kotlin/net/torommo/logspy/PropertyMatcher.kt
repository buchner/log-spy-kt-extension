package net.torommo.logspy

import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher

class PropertyMatcher<V, U>(
    val extractor: (V) -> U,
    propertyHost: String?,
    propertyName: String,
    matcher: Matcher<U>
) : FeatureMatcher<V, U>(matcher, "a $propertyHost with $propertyName property", propertyName) {
    companion object {
        inline fun <reified V, U> property(
            noinline extractor: (V) -> U,
            name: String,
            matcher: Matcher<U>
        ): PropertyMatcher<V, U> {
            return PropertyMatcher(extractor, V::class.simpleName, name, matcher)
        }
    }

    override fun featureValueOf(actual: V): U {
        return extractor(actual)
    }
}