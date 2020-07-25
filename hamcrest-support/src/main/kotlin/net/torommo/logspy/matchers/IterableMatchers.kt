package net.torommo.logspy.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.hamcrest.TypeSafeMatcher

class IterableMatchers {
    companion object {

        /**
         * Creates a matcher that matches when all provided matchers are matched at least once
         * during a single pass over the elements.
         */
        @JvmStatic
        @SafeVarargs
        fun <T> containing(matcher : Matcher<T>, vararg others : Matcher<T>) :
            Matcher<Iterable<T>> {
                return ContainingMatcher(listOf(matcher, *others))
            }

        /**
         * Creates a matcher that matches when all provided matchers are matched exactly once during
         * a single pass over the elements and the number of elements is equal to the number of the
         * provided matchers.
         */
        @JvmStatic
        @SafeVarargs
        fun <T> containingExactly(matcher: Matcher<T>, vararg others : Matcher<T>) :
            Matcher<Iterable<T>> {
                return ContainingExactlyMatcher(listOf(matcher, *others))
            }

        /**
         * Creates a matcher that matches when all provided matchers match in the provided order
         * during a single pass over the elements and the number of element is equal to the number
         * of the provided matchers.
         */
        @JvmStatic
        @SafeVarargs
        fun <T> containingExactlyInOrder(matcher: Matcher<T>, vararg others : Matcher<T>) :
            Matcher<Iterable<T>> {
                return ContainingExactlyInOrderMatcher(listOf(matcher, *others))
            }
    }

    private class ContainingMatcher<T>(private val matchers : List<Matcher<T>>) :
        TypeSafeMatcher<Iterable<T>>() {

        override fun describeTo(description: Description?) {
            description?.appendList("contains ", ", ", "", matchers)
        }

        override fun matchesSafely(item: Iterable<T>?): Boolean {
            val remainingElements = item!!.toMutableList()
            for (matcher in matchers) {
                if (remainingElements.asSequence().none { matcher.matches(it) }) {
                    return false
                } else {
                    remainingElements.asSequence()
                        .find { matcher.matches(it) }
                        .let { remainingElements.remove(it) }
                }
            }
            return true
        }

        override fun describeMismatchSafely(item: Iterable<T>?, mismatchDescription: Description?) {
            if (item!!.count() < matchers.size) {
                mismatchDescription?.appendValueList(
                    "contained ${item.count()} elements instead of ${matchers.size}: ",
                    ", ",
                    "",
                    item
                )
            } else {
                val remainingElements = item.toMutableList()
                val mismatches = mutableListOf<Matcher<T>>()
                for (matcher in matchers) {
                    if (remainingElements.asSequence().none { matcher.matches(it) }) {
                        mismatches.add(matcher)
                    } else {
                        remainingElements.asSequence()
                            .find { matcher.matches(it) }
                            .let { remainingElements.remove(it) }
                    }
                }
                val mismatchesDescription =
                    mismatches.joinToString {
                        val describer = StringDescription()
                        it.describeTo(describer)
                        describer.toString()
                    }
                mismatchDescription?.appendValueList(
                    "${mismatchesDescription} were not in: ",
                    ", ",
                    "",
                    item
                )
            }
        }
    }

    private class ContainingExactlyMatcher<T>(private val matchers : List<Matcher<T>>) :
        TypeSafeMatcher<Iterable<T>>() {

        override fun describeTo(description: Description?) {
            description?.appendList("contains exactly ", ", ", "", matchers)
        }

        override fun matchesSafely(item: Iterable<T>?): Boolean {
            val remainingElements = item!!.toMutableList()
            for (matcher in matchers) {
                if (remainingElements.asSequence().none { matcher.matches(it) }) {
                    return false
                } else {
                    remainingElements.asSequence()
                        .find { matcher.matches(it) }
                        .let { remainingElements.remove(it) }
                }
            }
            return remainingElements.isEmpty()
        }

        override fun describeMismatchSafely(item: Iterable<T>?, mismatchDescription: Description?) {
            if (item!!.count() < matchers.size || item.count() > matchers.size) {
                mismatchDescription?.appendValueList(
                    "contained ${item.count()} elements instead of ${matchers.size}: ",
                    ", ",
                    "",
                    item
                )
            } else {
                val remainingElements = item.toMutableList()
                val mismatches = mutableListOf<Matcher<T>>()
                for (matcher in matchers) {
                    if (remainingElements.asSequence().none { matcher.matches(it) }) {
                        mismatches.add(matcher)
                    } else {
                        remainingElements.asSequence()
                            .find { matcher.matches(it) }
                            .let { remainingElements.remove(it) }
                    }
                }
                val mismatchesDescription =
                    mismatches.joinToString {
                        val describer = StringDescription()
                        it.describeTo(describer)
                        describer.toString()
                    }
                mismatchDescription?.appendValueList(
                    "${mismatchesDescription} were not in: ",
                    ", ",
                    "",
                    item
                )
            }
        }
    }

    private class ContainingExactlyInOrderMatcher<T>(private val matchers : List<Matcher<T>>) :
        TypeSafeMatcher<Iterable<T>>() {

        override fun describeTo(description: Description?) {
            description?.appendList("contains exactly in order ", ", ", "", matchers)
        }

        override fun matchesSafely(item: Iterable<T>?): Boolean {
            return item!!.count() == matchers.count() &&
                item.asSequence()
                    .zip(matchers.asSequence())
                    .all { (element, matcher) -> matcher.matches(element) }
        }

        override fun describeMismatchSafely(item: Iterable<T>?, mismatchDescription: Description?) {
            if (item!!.count() < matchers.size || item.count() > matchers.size) {
                mismatchDescription?.appendValueList(
                    "contained ${item.count()} elements instead of ${matchers.size}: ",
                    ", ",
                    "",
                    item
                )
            } else {
                val mismatches =
                    matchers.asSequence()
                        .zip(item.asSequence())
                        .dropWhile { (matcher, candidate) -> matcher.matches(candidate) }
                        .map { it.first }
                        .toList()
                val mismatchesDescription =
                    mismatches.joinToString {
                        val describer = StringDescription()
                        it.describeTo(describer)
                        describer.toString()
                    }
                mismatchDescription?.appendValueList(
                    "${mismatchesDescription} were not in the same order in: ",
                    ", ",
                    "",
                    item
                )
            }
        }
    }
}