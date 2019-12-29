package net.torommo.logspy.matchers

import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

internal class PropertyMatcher<V, U>(
    val extractor: (V) -> U,
    propertyHost: String?,
    propertyName: String,
    matcher: Matcher<U>
) : FeatureMatcher<V, U>(matcher, "a $propertyHost with $propertyName property", propertyName) {
    companion object {
        inline fun <reified V, U> property(
            property: KProperty1<V, U>,
            matcher: Matcher<U>
        ): PropertyMatcher<V, U> {
            return PropertyMatcher(property, V::class.simpleName, property.name, matcher)
        }

        inline fun <reified V, U> property(
            property: KFunction1<V, U>,
            matcher: Matcher<U>
        ): PropertyMatcher<V, U> {
            return PropertyMatcher(property, V::class.simpleName, property.name, matcher)
        }
    }

    override fun featureValueOf(actual: V): U {
        return extractor(actual)
    }
}