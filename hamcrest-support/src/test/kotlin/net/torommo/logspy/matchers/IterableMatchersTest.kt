package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import net.torommo.logspy.matchers.IterableMatchers.Companion.containing
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactly
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactlyInOrder
import org.hamcrest.Matchers.`is`

internal class IterableMatchersTest : FreeSpec() {
    init {
        "containing matcher" - {
            "has a readable description" - {
                containing<String>(
                    AlwaysMatchMatcher(description = "a"),
                    AlwaysMatchMatcher(description = "b")
                ).description() shouldBe "contains a, b"
            }

            "matches when all matcher match" - {
                forAll(
                    row(arrayOf(`is`("a"), `is`("b")), listOf("a", "b")),
                    row(arrayOf(`is`("a"), `is`("b")), listOf("b", "a")),
                    row(arrayOf(`is`("a"), `is`("b")), listOf("a", "b", "c")),
                    row(arrayOf(`is`("a"), `is`("b")), listOf("c", "b", "a"))
                ) { matcher, input ->
                    containing(matcher[0], *matcher.copyOfRange(1, matcher.size))
                        .matches(input) shouldBe true
                }
            }

            "does not match when iterable contains less elements" - {
                containing(`is`("a"), `is`("a")).matches(listOf("a")) shouldBe false
            }

            "does not match when not all matchers match" - {
                containing(`is`("a"), `is`("a")).matches(listOf("a", "b")) shouldBe false
            }

            "has a readable mismatch description when iterable contains less elements" - {
                containing(`is`("a"), `is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "a")) shouldBe
                    """contained 2 elements instead of 3: "a", "a""""
            }

            "has a readable mismatch description when not all matchers were matched" - {
                containing<String>(
                    AlwaysMatchMatcher(),
                    NeverMatchMatcher(description = "a"),
                    NeverMatchMatcher(description = "b")
                ).mismatchDescriptionFor(listOf("1", "2", "3")) shouldBe
                    """a, b were not in: "1", "2", "3""""
            }

            "has a readable mismatch description when too less matches" - {
                containing(`is`("a"), `is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "b", "b")) shouldBe
                    """is "a", is "a" were not in: "a", "b", "b""""
            }
        }

        "containing exactly matcher" - {
            "has a readable description" - {
                containingExactly<String>(
                    AlwaysMatchMatcher(description = "a"),
                    AlwaysMatchMatcher(description = "b")
                ).description() shouldBe "contains exactly a, b"
            }

            "matches when all matcher match" - {
                forAll(
                    row(arrayOf(`is`("a"), `is`("b")), listOf("a", "b")),
                    row(arrayOf(`is`("a"), `is`("b")), listOf("b", "a"))
                ) { matcher, input ->
                    containingExactly(matcher[0], *matcher.copyOfRange(1, matcher.size))
                        .matches(input) shouldBe true
                }
            }

            "does not match when iterable contains less elements" - {
                containingExactly(`is`("a"), `is`("a")).matches(listOf("a")) shouldBe false
            }

            "does not match when not all matchers match" - {
                containingExactly(`is`("a"), `is`("a")).matches(listOf("a", "b")) shouldBe false
            }

            "does not match when when iterable contains more elements" - {
                containingExactly(`is`("a")).matches(listOf("a", "a")) shouldBe false
            }

            "has a readable mismatch description when iterable contains less elements" - {
                containingExactly(`is`("a"), `is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "a")) shouldBe
                    """contained 2 elements instead of 3: "a", "a""""
            }

            "has a readable mismatch description when not all matchers were matched" - {
                containingExactly<String>(
                    AlwaysMatchMatcher(),
                    NeverMatchMatcher(description = "a"),
                    NeverMatchMatcher(description = "b")
                ).mismatchDescriptionFor(listOf("1", "2", "3")) shouldBe
                    """a, b were not in: "1", "2", "3""""
            }

            "has a readable mismatch description when iterable contains more elements" - {
                containingExactly(`is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "a", "a")) shouldBe
                    """contained 3 elements instead of 2: "a", "a", "a""""
            }
        }

        "containing exactly in order matcher" - {
            "has a readable description" - {
                containingExactlyInOrder<String>(
                    AlwaysMatchMatcher(description = "a"),
                    AlwaysMatchMatcher(description = "b")
                ).description() shouldBe "contains exactly in order a, b"
            }

            "matches when all matcher match" - {
                containingExactlyInOrder(`is`("a"), `is`("b")).matches(listOf("a", "b"))
            }

            "does not match when iterable contains less elements" - {
                containingExactlyInOrder(`is`("a"), `is`("a")).matches(listOf("a")) shouldBe false
            }

            "does not match when not all matchers match" - {
                containingExactlyInOrder(`is`("a"), `is`("a")).matches(listOf("a", "b")) shouldBe
                    false
            }

            "does not match when matchers to not match in order" - {
                containingExactlyInOrder(`is`("a"), `is`("b")).matches(listOf("b", "a")) shouldBe
                    false
            }

            "does not match when when iterable contains more elements" - {
                containingExactlyInOrder(`is`("a")).matches(listOf("a", "a")) shouldBe false
            }

            "has a readable mismatch description when iterable contains less elements" - {
                containingExactlyInOrder(`is`("a"), `is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "a")) shouldBe
                    """contained 2 elements instead of 3: "a", "a""""
            }

            "has a readable mismatch description when not all matchers were matched" - {
                containingExactlyInOrder<String>(
                    AlwaysMatchMatcher(),
                    NeverMatchMatcher(description = "a"),
                    NeverMatchMatcher(description = "b")
                ).mismatchDescriptionFor(listOf("1", "2", "3")) shouldBe
                    """a, b were not in the same order in: "1", "2", "3""""
            }

            "has a readable mismatch description when not matchers were not matched in order" - {
                containingExactlyInOrder<String>(`is`("a"), `is`("b"))
                    .mismatchDescriptionFor(listOf("b", "a")) shouldBe
                    """is "a", is "b" were not in the same order in: "b", "a""""
            }

            "has a readable mismatch description when iterable contains more elements" - {
                containingExactlyInOrder(`is`("a"), `is`("a"))
                    .mismatchDescriptionFor(listOf("a", "a", "a")) shouldBe
                    """contained 3 elements instead of 2: "a", "a", "a""""
            }
        }
    }
}