package net.torommo.logspy.matchers

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers.Companion.declaringClass
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers.Companion.methodName

internal class StackTraceElementMatchersTest : FreeSpec() {
    init {
        "declaring class matcher" - {
            "has a readable description" - {
                declaringClass(AlwaysMatchMatcher(description = "test")).description() shouldContain
                    "declaringClass test"
            }

            "matches when matcher matchers" - {
                declaringClass(AlwaysMatchMatcher()).matches(stackTrace()) shouldBe true
            }

            "does not match when matcher does not match" - {
                declaringClass(NeverMatchMatcher()).matches(stackTrace()) shouldBe false
            }

            "does not match null stack trace" - {
                declaringClass(AlwaysMatchMatcher()).matches(null)
            }

            "has a readable mismatch description" - {
                declaringClass(NeverMatchMatcher("different"))
                    .mismatchDescriptionFor(stackTrace()) shouldBe "declaringClass different"
            }
        }

        "method name matcher" - {
            "has a readable description" - {
                methodName(AlwaysMatchMatcher(description = "test")).description() shouldContain
                    "methodName test"
            }

            "matches when matcher matches" - {
                methodName(AlwaysMatchMatcher()).matches(stackTrace()) shouldBe true
            }

            "does not match when matcher does not match" - {
                methodName(NeverMatchMatcher()).matches(stackTrace()) shouldBe false
            }

            "does not match null stack trace" - {
                methodName(AlwaysMatchMatcher()).matches(null) shouldBe false
            }

            "has a readable mismatch description" - {
                methodName(NeverMatchMatcher(mismatchDescription = "different"))
                    .mismatchDescriptionFor(stackTrace()) shouldBe "methodName different"
            }
        }
    }
}