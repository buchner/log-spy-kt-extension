package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.shouldContain
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exception
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.level
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdc
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.message

internal class SpiedEventMatchersTest : FreeSpec() {
    init {
        "level matcher" - {
            "has a readable description" - {
                level(AlwaysMatchMatcher(description = "test")).description() should
                    contain("level test")
            }

            "matches when matcher matches" - {
                level(AlwaysMatchMatcher()).matches(spiedEvent()) shouldBe true
            }

            "does not match when matcher does not match" - {
                level(NeverMatchMatcher()).matches(spiedEvent()) shouldBe false
            }

            "does not match null event" { level(AlwaysMatchMatcher()).matches(null) shouldBe false }

            "has a readable mismatch description" - {
                level(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(spiedEvent()) should contain("level different")
            }
        }

        "message matcher" - {
            "has a readable description" - {
                message(AlwaysMatchMatcher(description = "test")).description() should
                    contain("message test")
            }

            "matches when provided matcher matches" - {
                message(AlwaysMatchMatcher()).matches(spiedEvent()) shouldBe true
            }

            "does not match when provided does not match" - {
                message(NeverMatchMatcher()).matches(spiedEvent()) shouldBe false
            }

            "has a readable mismatch description" - {
                message(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(spiedEvent()) shouldBe "message mismatch"
            }
        }

        "exception matcher" - {
            "has a readable description " - {
                exception(AlwaysMatchMatcher(description = "test")).description() should
                    contain("exception test")
            }

            "matches when provided matcher matches" - {
                exception(AlwaysMatchMatcher()).matches(spiedEvent()) shouldBe true
            }

            "does not match when provided matcher does not match" - {
                exception(NeverMatchMatcher()).matches(spiedEvent()) shouldBe false
            }

            "has a readable mismatch description" - {
                exception(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(spiedEvent()) shouldBe "exception mismatch"
            }
        }

        "mdc matcher" - {
            "has a readable description" - {
                mdc(AlwaysMatchMatcher(description = "test")).description() shouldContain "mdc test"
            }

            "matches when provided matcher matches" - {
                mdc(AlwaysMatchMatcher()).matches(spiedEvent()) shouldBe true
            }

            "does not match when provided matcher does not match" - {
                mdc(NeverMatchMatcher()).matches(spiedEvent()) shouldBe false
            }

            "does not match null event" - { mdc(AlwaysMatchMatcher()).matches(null) shouldBe false }

            "has a readable mismatch description" - {
                mdc(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(spiedEvent()) shouldBe "mdc different"
            }
        }
    }
}
