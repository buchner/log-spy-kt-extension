package net.torommo.logspy

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import net.torommo.logspy.testing.SpyProviderIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.slf4j.LoggerFactory

internal class StdoutLogSpyExtensionIntegrationTest {

    @Nested
    inner class `Default configuration` : SpyProviderIntegrationTest() {
        @BeforeEach
        internal fun setUp() {
            switchConfigurationTo("logback-default.xml")
        }
    }

    @Nested
    inner class `Inverse stacktrace configuration` : SpyProviderIntegrationTest() {
        @BeforeEach
        internal fun setUp() {
            switchConfigurationTo("logback-inverse-stacktrace.xml")
        }
    }

    private fun switchConfigurationTo(configuration: String) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        val configurator = JoranConfigurator()
        configurator.context = context
        context.reset()
        configurator.doConfigure(this.javaClass.classLoader.getResource(configuration))
    }
}