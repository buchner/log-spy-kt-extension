package net.torommo.logspy

import net.torommo.logspy.LogSpyMatcher.Companion.debugsContains
import net.torommo.logspy.LogSpyMatcher.Companion.errorsContains
import net.torommo.logspy.LogSpyMatcher.Companion.eventsContains
import net.torommo.logspy.LogSpyMatcher.Companion.exceptionsContains
import net.torommo.logspy.LogSpyMatcher.Companion.infosContains
import net.torommo.logspy.LogSpyMatcher.Companion.tracesContains
import net.torommo.logspy.LogSpyMatcher.Companion.warningsContains
import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEventMatcher.Companion.exceptionIs
import net.torommo.logspy.SpiedEventMatcher.Companion.levelIs
import net.torommo.logspy.SpiedEventMatcher.Companion.mdcIs
import net.torommo.logspy.SpiedEventMatcher.Companion.messageIs
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@ExtendWith(LogSpyExtension::class)
internal class LogSpyExtensionIntegrationTest {

    @Nested
    inner class `Spy by type` {
        private val logger = LoggerFactory.getLogger(TestClass::class.java)

        @Test
        internal fun `captures messages from all levels`(@ByType(TestClass::class) spy : LogSpy) {
            logger.error("error")
            logger.warn("warn")
            logger.info("info")
            logger.debug("debug")
            logger.trace("trace")

            assertThat(spy, allOf(
                    eventsContains(
                            allOf(messageIs("error"), levelIs(ERROR)),
                            allOf(messageIs("warn"), levelIs(WARN)),
                            allOf(messageIs("info"), levelIs(INFO)),
                            allOf(messageIs("debug"), levelIs(DEBUG)),
                            allOf(messageIs("trace"), levelIs(TRACE))
                    ),
                    errorsContains(`is`("error")),
                    warningsContains(`is`("warn")),
                    infosContains(`is`("info")),
                    debugsContains(`is`("debug")),
                    tracesContains(`is`("trace"))
            ))
        }

        @Test
        internal fun `renders messages`(@ByType(TestClass::class) spy : LogSpy) {
            logger.info("{} is {} test", "this", "a")

            assertThat(spy, eventsContains(messageIs("this is a test")))
        }

        @Test
        internal fun `captures exceptions from all levels`(@ByType(TestClass::class) spy : LogSpy) {
            val errorLevelException = RuntimeException("Error exception")
            logger.error("error caught", errorLevelException)
            val warnLevelException = RuntimeException("Warn exception")
            logger.warn("error caught", warnLevelException)
            val infoLevelException = RuntimeException("Info exception")
            logger.info("error caught", infoLevelException)
            val debugLevelException = RuntimeException("Debug exception")
            logger.debug("error caught", debugLevelException)
            val traceLevelException = RuntimeException("Trace exception")
            logger.trace("error caught", traceLevelException)

            assertThat(spy, allOf(
                    eventsContains(
                            allOf(exceptionIs(errorLevelException), levelIs(ERROR)),
                            allOf(exceptionIs(warnLevelException), levelIs(WARN)),
                            allOf(exceptionIs(infoLevelException), levelIs(INFO)),
                            allOf(exceptionIs(debugLevelException), levelIs(DEBUG)),
                            allOf(exceptionIs(traceLevelException), levelIs(TRACE))
                    ),
                    exceptionsContains(
                            `is`(errorLevelException),
                            `is`(warnLevelException),
                            `is`(infoLevelException),
                            `is`(debugLevelException),
                            `is`(traceLevelException)
                    )
            ))
        }

        @Test
        internal fun `captures mdc from all levels`(@ByType(TestClass::class) spy: LogSpy) {
            withMdc("errorKey", "error") {
                logger.error("error")
            }
            withMdc("warnKey", "warn") {
                logger.warn("error")
            }
            withMdc("infoKey", "info") {
                logger.info("info")
            }
            withMdc("debugKey", "debug") {
                logger.debug("debug")
            }
            withMdc("traceKey", "trace") {
                logger.debug("trace")
            }

            assertThat(spy, eventsContains(
                    mdcIs(mapOf("errorKey" to "error")),
                    mdcIs(mapOf("warnKey" to "warn")),
                    mdcIs(mapOf("infoKey" to "info")),
                    mdcIs(mapOf("debugKey" to "debug")),
                    mdcIs(mapOf("traceKey" to "trace"))
            ))
        }

        @Nested
        inner class `Constructor spy parameter`(@ByType(TestClass::class) val spy : LogSpy) {
            private val logger = LoggerFactory.getLogger(TestClass::class.java)

            @BeforeEach
            internal fun setup() {
                logger.info("setup")
            }

            @Test
            internal fun `records logs from constructor until test`() {
                logger.info("test")

                assertThat(spy, eventsContains(
                        messageIs("setup"),
                        messageIs("test")
                ))
            }

            @Test
            internal fun `isolates method from constructor spy`(@ByType(TestClass::class) spy : LogSpy) {
                logger.info("test")

                assertAll({
                    assertThat(spy, eventsContains(messageIs("test")))
                    assertThat(this.spy, eventsContains(
                            messageIs("setup"),
                            messageIs("test")
                    ))
                })
            }
        }

        @Nested
        inner class `Multiple spy parameters` {
            private val loggerA = LoggerFactory.getLogger(TestClassA::class.java)
            private val loggerB = LoggerFactory.getLogger(TestClassB::class.java)

            @Test
            internal fun `are isolated when different name`(@ByType(TestClassA::class) spyA : LogSpy,
                                                            @ByType(TestClassB::class) spyB : LogSpy) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertAll({
                    assertThat(spyA, eventsContains(messageIs("info a")))
                    assertThat(spyB, eventsContains(messageIs("info b")))
                })
            }

            @Test
            internal fun `have same content when same name`(@ByType(TestClassA::class) firstSpy : LogSpy,
                                                            @ByType(TestClassA::class) secondSpy : LogSpy) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertThat(firstSpy.events(), `is`(secondSpy.events()))
            }
        }

        @Nested
        inner class `In combination with parameterized tests` {
            private val logger = LoggerFactory.getLogger(TestClass::class.java)

            @ParameterizedTest
            @ValueSource(strings = ["1", "2", "3"])
            internal fun `is possible`(parameter : String, @ByType(TestClass::class) spy : LogSpy) { // Must be second because junit parameters claim the first argument
                logger.info(parameter)

                assertThat(spy, eventsContains(messageIs(parameter)))
            }
        }
    }

    @Nested
    inner class `Spy by literal` {
        private val logger = LoggerFactory.getLogger("TEST_LOGGER")

        @Test
        internal fun `captures messages from all levels`(@ByLiteral("TEST_LOGGER") spy : LogSpy) {
            logger.error("error")
            logger.warn("warn")
            logger.info("info")
            logger.debug("debug")
            logger.trace("trace")

            assertThat(spy, allOf(
                    eventsContains(
                            allOf(messageIs("error"), levelIs(ERROR)),
                            allOf(messageIs("warn"), levelIs(WARN)),
                            allOf(messageIs("info"), levelIs(INFO)),
                            allOf(messageIs("debug"), levelIs(DEBUG)),
                            allOf(messageIs("trace"), levelIs(TRACE))
                    ),
                    errorsContains(`is`("error")),
                    warningsContains(`is`("warn")),
                    infosContains(`is`("info")),
                    debugsContains(`is`("debug")),
                    tracesContains(`is`("trace"))
            ))
        }

        @Test
        internal fun `captures exceptions from all levels`(@ByLiteral("TEST_LOGGER") spy : LogSpy) {
            val errorLevelException = RuntimeException("Error exception")
            logger.error("error caught", errorLevelException)
            val warnLevelException = RuntimeException("Warn exception")
            logger.warn("error caught", warnLevelException)
            val infoLevelException = RuntimeException("Info exception")
            logger.info("error caught", infoLevelException)
            val debugLevelException = RuntimeException("Debug exception")
            logger.debug("error caught", debugLevelException)
            val traceLevelException = RuntimeException("Trace exception")
            logger.trace("error caught", traceLevelException)

            assertThat(spy, allOf(
                    eventsContains(
                            allOf(exceptionIs(errorLevelException), levelIs(ERROR)),
                            allOf(exceptionIs(warnLevelException), levelIs(WARN)),
                            allOf(exceptionIs(infoLevelException), levelIs(INFO)),
                            allOf(exceptionIs(debugLevelException), levelIs(DEBUG)),
                            allOf(exceptionIs(traceLevelException), levelIs(TRACE))
                    ),
                    exceptionsContains(
                            `is`(errorLevelException),
                            `is`(warnLevelException),
                            `is`(infoLevelException),
                            `is`(debugLevelException),
                            `is`(traceLevelException)
                    )
            ))
        }

        @Test
        internal fun `captures mdc from all levels`(@ByLiteral("TEST_LOGGER") spy: LogSpy) {
            withMdc("errorKey", "error") {
                logger.error("error")
            }
            withMdc("warnKey", "warn") {
                logger.warn("error")
            }
            withMdc("infoKey", "info") {
                logger.info("info")
            }
            withMdc("debugKey", "debug") {
                logger.debug("debug")
            }
            withMdc("traceKey", "trace") {
                logger.debug("trace")
            }

            assertThat(spy, eventsContains(
                    mdcIs(mapOf("errorKey" to "error")),
                    mdcIs(mapOf("warnKey" to "warn")),
                    mdcIs(mapOf("infoKey" to "info")),
                    mdcIs(mapOf("debugKey" to "debug")),
                    mdcIs(mapOf("traceKey" to "trace"))
            ))
        }

        @Nested
        inner class `Constructor spy parameter`(@ByLiteral("TEST_LOGGER") val spy : LogSpy) {
            private val logger = LoggerFactory.getLogger("TEST_LOGGER")

            @BeforeEach
            internal fun setup() {
                logger.info("setup")
            }

            @Test
            internal fun `records logs from constructor until test`() {
                logger.info("test")

                assertThat(spy, eventsContains(
                        messageIs("setup"),
                        messageIs("test")
                ))
            }

            @Test
            internal fun `isolates method from constructor spy`(@ByLiteral("TEST_LOGGER") spy : LogSpy) {
                logger.info("test");

                assertAll({
                    assertThat(spy, eventsContains(messageIs("test")))
                    assertThat(this.spy, eventsContains(messageIs("setup"), messageIs("test")))
                })
            }
        }

        @Nested
        inner class `Multiple spy parameters` {
            private val loggerA = LoggerFactory.getLogger("TEST_LOGGER_A")
            private val loggerB = LoggerFactory.getLogger("TEST_LOGGER_B")

            @Test
            internal fun `are isolated when different name`(@ByLiteral("TEST_LOGGER_A") spyA : LogSpy,
                                                            @ByLiteral("TEST_LOGGER_B") spyB : LogSpy) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertAll({
                    assertThat(spyA, eventsContains(messageIs("info a")))
                    assertThat(spyB, eventsContains(messageIs("info b")))
                })
            }

            @Test
            internal fun `have same content when same name`(@ByLiteral("TEST_LOGGER_A") firstSpy : LogSpy,
                                                            @ByLiteral("TEST_LOGGER_A") secondSpy : LogSpy) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertThat(firstSpy.events(), `is`(secondSpy.events()))
            }
        }

        @Nested
        inner class `In combination with parameterized tests` {
            private val logger = LoggerFactory.getLogger("TEST_LOGGER")

            @ParameterizedTest
            @ValueSource(strings = ["1", "2", "3"])
            internal fun `is possible`(parameter : String, @ByLiteral("TEST_LOGGER") spy : LogSpy) { // Must be second because junit parameters claim the first argument
                logger.info(parameter)

                assertThat(spy, eventsContains(messageIs(parameter)))
            }
        }
    }

    @Nested
    inner class `Multiple spy parameters` {
        private val loggerA = LoggerFactory.getLogger(TestClassA::class.java)
        private val loggerB = LoggerFactory.getLogger("net.torommo.logspy.TestClassB")

        @Test
        internal fun `are isolated when different name`(@ByType(TestClassA::class) spyA : LogSpy,
                                                        @ByLiteral("net.torommo.logspy.TestClassB") spyB : LogSpy) {
            loggerA.info("info a")
            loggerB.info("info b")

            assertAll({
                assertThat(spyA, eventsContains(messageIs("info a")))
                assertThat(spyB, eventsContains(messageIs("info b")))
            })
        }

        @Test
        internal fun `have same content when same name`(@ByType(TestClassA::class) firstSpy : LogSpy,
                                                        @ByLiteral("net.torommo.logspy.TestClassA") secondSpy : LogSpy) {
            loggerA.info("info a")
            loggerB.info("info b")

            assertThat(firstSpy.events(), `is`(secondSpy.events()))
        }
    }

    private inline fun <T> withMdc(key: String, value: String, action: () -> T): T {
        MDC.put(key, value)
        try {
            return action()
        } finally {
            MDC.remove(key)
        }
    }
}

internal class TestClass

internal class TestClassA

internal class TestClassB