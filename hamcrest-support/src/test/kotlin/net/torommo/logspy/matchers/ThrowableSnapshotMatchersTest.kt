package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import io.kotest.matchers.string.shouldContain
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.cause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.message
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

            "has a readable mismatch description" - {
                message(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(throwable()) shouldBe "message mismatch"
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

            "has a readable mismatch description" - {
                cause(NeverMatchMatcher(mismatchDescription = "mismatch"))
                    .mismatchDescriptionFor(throwable()) shouldBe "cause mismatch"
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