package net.torommo.logspy.testing

import net.torommo.logspy.ByLiteral
import net.torommo.logspy.ByType
import net.torommo.logspy.LogSpy
import net.torommo.logspy.LogSpyExtension
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.debugsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.errorsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.eventsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.exceptionsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.infosContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.tracesContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.warningsContains
import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exceptionIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exceptionWith
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.levelIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdcIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.messageIs
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.typeIs
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
import java.lang.IllegalArgumentException

@ExtendWith(LogSpyExtension::class)
open class LogSpyExtensionIntegrationTest {

    @Nested
    inner class `Spy by type` {
        private val logger = LoggerFactory.getLogger(TestClass::class.java)

        @Test
        internal fun `captures messages from all levels`(@ByType(TestClass::class) spy: LogSpy) {
            logger.error("error")
            logger.warn("warn")
            logger.info("info")
            logger.debug("debug")
            logger.trace("trace")

            assertThat(
                spy, allOf(
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
                )
            )
        }

        @Test
        internal fun `renders messages`(@ByType(TestClass::class) spy: LogSpy) {
            logger.info("{} is {} test", "this", "a")

            assertThat(spy, eventsContains(messageIs("this is a test")))
        }

        @Test
        internal fun `captures exceptions from all levels`(@ByType(TestClass::class) spy: LogSpy) {
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

            assertThat(
                spy, allOf(
                    eventsContains(
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Error exception")
                                )
                            ),
                            levelIs(ERROR)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Warn exception")
                                )
                            ),
                            levelIs(WARN)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Info exception")
                                )
                            ),
                            levelIs(INFO)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Debug exception")
                                )
                            ),
                            levelIs(DEBUG)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Trace exception")
                                )
                            ),
                            levelIs(TRACE)
                        )
                    ),
                    exceptionsContains(
                        ThrowableSnapshotMatchers.messageIs("Error exception"),
                        ThrowableSnapshotMatchers.messageIs("Warn exception"),
                        ThrowableSnapshotMatchers.messageIs( "Info exception"),
                        ThrowableSnapshotMatchers.messageIs( "Debug exception"),
                        ThrowableSnapshotMatchers.messageIs( "Trace exception")
                    )
                )
            )
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

            assertThat(
                spy, eventsContains(
                    mdcIs(mapOf("errorKey" to "error")),
                    mdcIs(mapOf("warnKey" to "warn")),
                    mdcIs(mapOf("infoKey" to "info")),
                    mdcIs(mapOf("debugKey" to "debug")),
                    mdcIs(mapOf("traceKey" to "trace"))
                )
            )
        }

        @Nested
        inner class `Constructor spy parameter`(@ByType(TestClass::class) val spy: LogSpy) {
            private val logger = LoggerFactory.getLogger(TestClass::class.java)

            @BeforeEach
            internal fun setup() {
                logger.info("setup")
            }

            @Test
            internal fun `records logs from constructor until test`() {
                logger.info("test")

                assertThat(
                    spy, eventsContains(
                        messageIs("setup"),
                        messageIs("test")
                    )
                )
            }

            @Test
            internal fun `isolates method from constructor spy`(@ByType(TestClass::class) spy: LogSpy) {
                logger.info("test")

                assertAll({
                    assertThat(spy, eventsContains(messageIs("test")))
                    assertThat(
                        this.spy, eventsContains(
                            messageIs("setup"),
                            messageIs("test")
                        )
                    )
                })
            }
        }

        @Nested
        inner class `Multiple spy parameters` {
            private val loggerA = LoggerFactory.getLogger(TestClassA::class.java)
            private val loggerB = LoggerFactory.getLogger(TestClassB::class.java)

            @Test
            internal fun `are isolated when different name`(
                @ByType(TestClassA::class) spyA: LogSpy,
                @ByType(TestClassB::class) spyB: LogSpy
            ) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertAll({
                    assertThat(spyA, eventsContains(messageIs("info a")))
                    assertThat(spyB, eventsContains(messageIs("info b")))
                })
            }

            @Test
            internal fun `have same content when same name`(
                @ByType(TestClassA::class) firstSpy: LogSpy,
                @ByType(TestClassA::class) secondSpy: LogSpy
            ) {
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
            internal fun `is possible`(parameter: String, @ByType(TestClass::class) spy: LogSpy) { // Must be second because junit parameters claim the first argument
                logger.info(parameter)

                assertThat(spy, eventsContains(messageIs(parameter)))
            }
        }
    }

    @Nested
    inner class `Spy by literal` {
        private val logger = LoggerFactory.getLogger("TEST_LOGGER")

        @Test
        internal fun `captures messages from all levels`(@ByLiteral("TEST_LOGGER") spy: LogSpy) {
            logger.error("error")
            logger.warn("warn")
            logger.info("info")
            logger.debug("debug")
            logger.trace("trace")

            assertThat(
                spy, allOf(
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
                )
            )
        }

        @Test
        internal fun `captures exceptions from all levels`(@ByLiteral("TEST_LOGGER") spy: LogSpy) {
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

            assertThat(
                spy, allOf(
                    eventsContains(
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Error exception")
                                )
                            ),
                            levelIs(ERROR)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Warn exception")
                                )
                            ),
                            levelIs(WARN)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Info exception")
                                )
                            ),
                            levelIs(INFO)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Debug exception")
                                )
                            ),
                            levelIs(DEBUG)
                        ),
                        allOf(
                            exceptionWith(
                                allOf(
                                    typeIs("java.lang.RuntimeException"),
                                    ThrowableSnapshotMatchers.messageIs("Trace exception")
                                )
                            ),
                            levelIs(TRACE)
                        )
                    ),
                    exceptionsContains(
                        ThrowableSnapshotMatchers.messageIs("Error exception"),
                        ThrowableSnapshotMatchers.messageIs("Warn exception"),
                        ThrowableSnapshotMatchers.messageIs( "Info exception"),
                        ThrowableSnapshotMatchers.messageIs( "Debug exception"),
                        ThrowableSnapshotMatchers.messageIs( "Trace exception")
                    )
                )
            )
        }

        @Test
        internal fun takesSnapshotOfException(@ByLiteral("TEST_LOGGER") spy: LogSpy) {
            val cause = IllegalArgumentException("Cause")
            cause.stackTrace = arrayOf()
            val exception = RuntimeException("Root", cause)
            val suppressed1 = RuntimeException("Suppressed 1")
            suppressed1.stackTrace = arrayOf()
            exception.addSuppressed(suppressed1)
            val suppressed2 = RuntimeException("Suppressed 2")
            suppressed2.stackTrace = arrayOf()
            exception.addSuppressed(suppressed2)
            exception.stackTrace = arrayOf(
                StackTraceElement("OuterClass", "callingMethod", "TestFailingClass.class", 389),
                StackTraceElement("TestFailingClass", "failingMethod", "TestFailingClass.class", 37)
            )

            logger.error("Test exception", exception)

            assertThat(
                spy, exceptionsContains(
                    `is`(
                        ThrowableSnapshot(
                            "java.lang.RuntimeException", "Root",
                            ThrowableSnapshot("java.lang.IllegalArgumentException", "Cause"),
                            listOf(
                                ThrowableSnapshot("java.lang.RuntimeException", "Suppressed 1"),
                                ThrowableSnapshot("java.lang.RuntimeException", "Suppressed 2")
                            ),
                            listOf(
                                StackTraceElementSnapshot("OuterClass", "callingMethod"),
                                StackTraceElementSnapshot("TestFailingClass", "failingMethod")
                            )
                        )
                    )
                )
            )
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

            assertThat(
                spy, eventsContains(
                    mdcIs(mapOf("errorKey" to "error")),
                    mdcIs(mapOf("warnKey" to "warn")),
                    mdcIs(mapOf("infoKey" to "info")),
                    mdcIs(mapOf("debugKey" to "debug")),
                    mdcIs(mapOf("traceKey" to "trace"))
                )
            )
        }

        @Nested
        inner class `Constructor spy parameter`(@ByLiteral("TEST_LOGGER") val spy: LogSpy) {
            private val logger = LoggerFactory.getLogger("TEST_LOGGER")

            @BeforeEach
            internal fun setup() {
                logger.info("setup")
            }

            @Test
            internal fun `records logs from constructor until test`() {
                logger.info("test")

                assertThat(
                    spy, eventsContains(
                        messageIs("setup"),
                        messageIs("test")
                    )
                )
            }

            @Test
            internal fun `isolates method from constructor spy`(@ByLiteral("TEST_LOGGER") spy: LogSpy) {
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
            internal fun `are isolated when different name`(
                @ByLiteral("TEST_LOGGER_A") spyA: LogSpy,
                @ByLiteral("TEST_LOGGER_B") spyB: LogSpy
            ) {
                loggerA.info("info a")
                loggerB.info("info b")

                assertAll({
                    assertThat(spyA, eventsContains(messageIs("info a")))
                    assertThat(spyB, eventsContains(messageIs("info b")))
                })
            }

            @Test
            internal fun `have same content when same name`(
                @ByLiteral("TEST_LOGGER_A") firstSpy: LogSpy,
                @ByLiteral("TEST_LOGGER_A") secondSpy: LogSpy
            ) {
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
            internal fun `is possible`(parameter: String, @ByLiteral("TEST_LOGGER") spy: LogSpy) { // Must be second because junit parameters claim the first argument
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
        internal fun `are isolated when different name`(
            @ByType(TestClassA::class) spyA: LogSpy,
            @ByLiteral("net.torommo.logspy.TestClassB") spyB: LogSpy
        ) {
            loggerA.info("info a")
            loggerB.info("info b")

            assertAll({
                assertThat(spyA, eventsContains(messageIs("info a")))
                assertThat(spyB, eventsContains(messageIs("info b")))
            })
        }

        @Test
        internal fun `have same content when same name`(
            @ByType(TestClassA::class) firstSpy: LogSpy,
            @ByLiteral("net.torommo.logspy.testing.TestClassA") secondSpy: LogSpy
        ) {
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