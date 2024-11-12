package net.torommo.logspy

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import net.torommo.logspy.testing.spyProviderIntegrationTest
import org.slf4j.LoggerFactory

class StdoutLogSpyExtensionIntegrationTest :
    FreeSpec(
        {
            isolationMode = IsolationMode.InstancePerTest
            switchConfigurationTo("logback-default.xml")
            include(spyProviderIntegrationTest("Logstash encoder default configuration"))
            switchConfigurationTo("logback-inverse-stacktrace.xml")
            include(spyProviderIntegrationTest("Logstash encoder inverse stacktrace configuration"))
        },
    )

private fun switchConfigurationTo(configuration: String) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val configurator = JoranConfigurator()
    configurator.context = context
    context.reset()
    configurator.doConfigure(Thread.currentThread().contextClassLoader.getResource(configuration))
}
