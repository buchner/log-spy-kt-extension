package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import net.torommo.logspy.matchers.ClutterFreeOptionalMatchers.Companion.absent
import net.torommo.logspy.matchers.ClutterFreeOptionalMatchers.Companion.present

internal class ClutterFreeOptionalMatchersTest : FreeSpec() {
    init {
        "present matcher" - {
            "has a readable description" - {
                present(AlwaysMatchMatcher<String>(description = "test description"))
                    .description() shouldBe "test description"
            }

            "matches when not null and matcher matches" - {
                present(AlwaysMatchMatcher<String>()).matches("test") shouldBe true
            }

            "does not match when matcher does not match matches" - {
                present(NeverMatchMatcher<String>()).matches("test") shouldBe false
            }

            "does not match when null" - {
                present(AlwaysMatchMatcher<String>()).matches(null) shouldBe false
            }

            "has a readable mismatch description when null" - {
                present(AlwaysMatchMatcher<String>(description = "test description"))
                    .mismatchDescriptionFor(null) shouldBe "test description was null"
            }
        }

        "absent matcher" - {
            "has a readable description" - { absent<Any>().description() shouldBe "absent" }

            "matches when null" - { absent<Any>().matches(null) shouldBe true }

            "does not match when not null" - { absent<String>().matches("test") shouldBe false }

            "has a readable mismatch description when not null" - {
                absent<String>().mismatchDescriptionFor("test") shouldBe """was "test""""
            }
        }
    }
}