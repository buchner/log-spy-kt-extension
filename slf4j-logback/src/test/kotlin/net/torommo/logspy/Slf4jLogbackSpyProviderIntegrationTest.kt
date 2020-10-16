package net.torommo.logspy

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import net.torommo.logspy.testing.spyProviderIntegrationTest

class Slf4jLogbackSpyProviderIntegrationTest :
    FreeSpec(
        {
            isolationMode = IsolationMode.InstancePerTest
            include(spyProviderIntegrationTest("Logback"))
        }
    )