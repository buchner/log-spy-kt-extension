package net.torommo.logspy.matchers

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

internal class PropertyMatcher<V : Any, U>(
    private val extractor: (V) -> U,
    private val type: KClass<*>,
    private val propertyName: String,
    private val matcher: Matcher<U>
) : BaseMatcher<V>() {
    companion object {
        inline fun <reified V : Any, U> property(property: KProperty1<V, U>, matcher: Matcher<U>):
            PropertyMatcher<V, U> {
                return PropertyMatcher(property, V::class, property.name, matcher)
            }

        inline fun <reified V : Any, U> property(property: KFunction1<V, U>, matcher: Matcher<U>):
            PropertyMatcher<V, U> {
                return PropertyMatcher(property, V::class, property.name, matcher)
            }
    }

    override fun describeTo(description: Description?) {
        description?.appendText("a ${type.simpleName} with $propertyName ")
        description?.appendDescriptionOf(matcher)
    }

    override fun describeMismatch(item: Any?, description: Description?) {
        if (item == null) {
            description?.appendText("was null")
        }
        if (type.isInstance(item) && item != null) {
            description?.appendText("$propertyName ")
            @Suppress("UNCHECKED_CAST")
            matcher.describeMismatch(extractor(item as V), description)
        }
    }

    override fun matches(actual: Any?): Boolean {
        return if (type.isInstance(actual) && actual != null) {
            @Suppress("UNCHECKED_CAST")
            matcher.matches(extractor(actual as V))
        } else {
            false
        }
    }
}