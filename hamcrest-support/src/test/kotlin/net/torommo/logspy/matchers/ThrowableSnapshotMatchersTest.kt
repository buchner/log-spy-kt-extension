package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.shouldContain
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.cause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.message
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.noCause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.noMessage
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.stack
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.suppressed
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.type

internal class ThrowableSnapshotMatchersTest : FreeSpec() {
    init {
        "message matcher" - {
            "has a readable description" - {
                message(AlwaysMatchMatcher(description = "test")).description() should
                    contain("message test")
            }

            "matches when provided matcher matches" - {
                message(AlwaysMatchMatcher()).matches(throwable()) shouldBe true
            }

            "does not match when provided does not match" - {
                message(NeverMatchMatcher()).matches(throwable()) shouldBe false
            }

            "does not match null throwable" - {
                message(AlwaysMatchMatcher()).matches(null) shouldBe false
            }

            "does not match null message" - {
                message(AlwaysMatchMatcher()).matches(throwable().copy(message = null)) shouldBe
                    false
            }

            "has a readable mismatch description" - {
                message(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(throwable()) shouldBe "message mismatch"
            }

            "has a readable null message mismatch description" - {
                message(AlwaysMatchMatcher())
                    .mismatchDescriptionFor(throwable().copy(message = null)) shouldBe
                    "message was null"
            }

            "has a readable null throwable mismatch description" - {
                message(AlwaysMatchMatcher()).mismatchDescriptionFor(null) shouldBe "was null"
            }
        }

        "no message matcher" - {
            "has a readable description" - {
                noMessage().description() shouldContain "message null"
            }

            "matches when message is null" - {
                noMessage().matches(throwable().copy(message = null)) shouldBe true
            }

            "does not match when message is not null" - {
                noMessage().matches(throwable()) shouldBe false
            }

            "does not match null throwable" - { noMessage().matches(null) shouldBe false }

            "has a readable mismatch description" - {
                noMessage().mismatchDescriptionFor(throwable()) should contain("message was")
            }

            "has a readable null throwable mismatch description" - {
                noMessage().mismatchDescriptionFor(null) should contain("was null")
            }
        }

        "exact type matcher" - {
            "has a readable description" - {
                type(AlwaysMatchMatcher(description = "test")).description() should
                    contain("type test")
            }

            "matches when matcher matches" - {
                type(AlwaysMatchMatcher()).matches(throwable()) shouldBe true
            }

            "does not match when matcher does not match" - {
                type(NeverMatchMatcher()).matches(throwable()) shouldBe false
            }

            "does not match null throwable" - {
                type(AlwaysMatchMatcher()).matches(null) shouldBe false
            }

            "has a readable mismatch description" - {
                type(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(throwable()) shouldBe "type different"
            }
        }

        "cause matcher" - {
            "has a readable description" - {
                cause(AlwaysMatchMatcher(description = "test")).description() shouldContain
                    "cause test"
            }

            "matches when provided matcher matches" - {
                cause(AlwaysMatchMatcher()).matches(throwable()) shouldBe true
            }

            "does not match when provided matcher does not match" - {
                cause(NeverMatchMatcher()).matches(throwable()) shouldBe false
            }

            "does not match null throwable" - {
                cause(AlwaysMatchMatcher()).matches(null) shouldBe false
            }

            "does not match null cause" - {
                cause(AlwaysMatchMatcher()).matches(throwable().copy(cause = null)) shouldBe false
            }

            "has a readable mismatch description" - {
                cause(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(throwable()) shouldBe "cause mismatch"
            }

            "has a readable null mismatch description" - {
                cause(NeverMatchMatcher())
                    .mismatchDescriptionFor(throwable().copy(cause = null)) shouldBe
                    "cause was null"
            }

            "has a readable null throwable mismatch description" - {
                cause(NeverMatchMatcher()).mismatchDescriptionFor(null) shouldBe "was null"
            }
        }

        "no cause matcher" - {
            "has a readable description" - { noCause().description() shouldContain "cause null" }

            "matches when cause is null" - {
                noCause().matches(throwable().copy(cause = null)) shouldBe true
            }

            "does not match when cause is not null" - {
                noCause().matches(throwable()) shouldBe false
            }

            "does not match null throwable" - { noCause().matches(null) shouldBe false }

            "has a readable mismatch description" - {
                noCause().mismatchDescriptionFor(throwable()) should contain("cause was")
            }

            "has a readable null throwable mismatch description" - {
                noCause().mismatchDescriptionFor(null) should contain("was null")
            }
        }

        "suppressed matcher" - {
            "has a readable description" - {
                suppressed(AlwaysMatchMatcher(description = "test")).description() should
                    contain("suppressed test")
            }

            "matches when matcher matches" - {
                suppressed(AlwaysMatchMatcher()).matches(throwable()) shouldBe true
            }

            "does not match when matcher does not match" - {
                suppressed(NeverMatchMatcher()).matches(throwable()) shouldBe false
            }

            "does not match null throwable" - {
                suppressed(AlwaysMatchMatcher()).matches(null) shouldBe false
            }

            "has a readable mismatch description" - {
                suppressed(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(throwable()) shouldBe "suppressed different"
            }
        }

        "stack matcher" - {
            "has a readable description" - {
                stack(AlwaysMatchMatcher(description = "test")).description() should contain("test")
            }

            "matches when matcher matches" - {
                stack(AlwaysMatchMatcher()).matches(throwable()) shouldBe true
            }

            "does not match when matcher does not match" - {
                stack(NeverMatchMatcher()).matches(throwable()) shouldBe false
            }

            "has a readable mismatch description" - {
                stack(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(throwable()) shouldBe "stackTrace different"
            }
        }
    }
}