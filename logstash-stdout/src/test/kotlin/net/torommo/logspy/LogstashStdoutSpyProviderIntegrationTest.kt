package net.torommo.logspy

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

internal class LogstashStdoutSpyProviderIntegrationTest : FreeSpec() {
    init {
        "does not deregister interceptor" - {
            val spy = LogstashStdoutSpyProvider().createFor(TestClass::class)
            val stdout = System.out

            spy.close()

            System.out shouldBeSameInstanceAs stdout
        }

        "reuses interceptor" - {
            val provider = LogstashStdoutSpyProvider()
            provider.createFor(TestClassA::class)
                .use {
                    val stdout = System.out
                    provider.createFor(TestClassB::class)
                        .use { System.out shouldBeSameInstanceAs stdout }
                }
        }
    }
}
