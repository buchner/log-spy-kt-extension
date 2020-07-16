package net.torommo.logspy

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.sameInstance
import org.junit.jupiter.api.Test

internal class LogstashStdoutSpyProviderIntegrationTest {
    @Test
    internal fun `does not deregister interceptor`() {
        val spy = LogstashStdoutSpyProvider().createFor(TestClass::class)
        val stdout = System.out

        spy.close()

        assertThat(System.out, sameInstance(stdout))
    }

    @Test
    internal fun `reuses interceptor`() {
        val provider = LogstashStdoutSpyProvider()
        provider.createFor(TestClassA::class)
            .use {
                val stdout = System.out
                provider.createFor(TestClassB::class)
                    .use { assertThat(System.out, sameInstance(stdout)) }
            }
    }
}
