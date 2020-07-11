package net.torommo.logspy.testing

import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.debugsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.errorsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.eventsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.exceptionsContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.infosContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.tracesContains
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.warningsContains
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exceptionWith
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.levelIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdcIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.messageIs
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.typeIs
import net.torommo.logspy.spyForLogger
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.slf4j.MDC

abstract class SpyProviderIntegrationTest {

    @Nested
    inner class `Spy by type` {
        private val logger = LoggerFactory.getLogger(TestClass::class.java)

        @Test
        internal fun `captures messages from all levels`() {
            val spy = spyForLogger<TestClass> {
                logger.error("error")
                logger.warn("warn")
                logger.info("info")
                logger.debug("debug")
                logger.trace("trace")
            }

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
        internal fun `renders messages`() {
            val spy = spyForLogger<TestClass> {
                logger.info("{} is {} test", "this", "a")
            }

            assertThat(spy, eventsContains(messageIs("this is a test")))
        }

        @Test
        internal fun `captures exceptions from all levels`() {
            val spy = spyForLogger<TestClass> {
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
            }

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
                        ThrowableSnapshotMatchers.messageIs("Info exception"),
                        ThrowableSnapshotMatchers.messageIs("Debug exception"),
                        ThrowableSnapshotMatchers.messageIs("Trace exception")
                    )
                )
            )
        }

        @Test
        internal fun `limits record scope of spy`() {
            logger.info("test 1")
            val spy = spyForLogger<TestClass> {
                logger.info("test 2")
            }
            logger.info("test 3")

            assertThat(spy, eventsContains(messageIs("test 2")))
        }

        @Test
        internal fun `captures mdc from all levels`() {
            val spy = spyForLogger<TestClass> {
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
    }

    @Nested
    inner class `Spy by literal` {
        private val logger = LoggerFactory.getLogger("TEST_LOGGER")

        @Test
        internal fun `captures messages from all levels`() {
            val spy = spyForLogger("TEST_LOGGER") {
                logger.error("error")
                logger.warn("warn")
                logger.info("info")
                logger.debug("debug")
                logger.trace("trace")
            }

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
        internal fun `captures exceptions from all levels`() {
            val spy = spyForLogger("TEST_LOGGER") {
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
            }

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
                        ThrowableSnapshotMatchers.messageIs("Info exception"),
                        ThrowableSnapshotMatchers.messageIs("Debug exception"),
                        ThrowableSnapshotMatchers.messageIs("Trace exception")
                    )
                )
            )
        }

        @Test
        internal fun takesSnapshotOfException() {
            val spy = spyForLogger("TEST_LOGGER") {
                val causeCause = NullPointerException("Cause cause")
                causeCause.stackTrace = arrayOf()
                val cause = IllegalArgumentException("Cause", causeCause)
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
            }

            assertThat(
                spy, exceptionsContains(
                    `is`(
                        ThrowableSnapshot(
                            "java.lang.RuntimeException", "Root",
                            ThrowableSnapshot(
                                "java.lang.IllegalArgumentException", "Cause",
                                ThrowableSnapshot("java.lang.NullPointerException", "Cause cause")
                            ),
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
        internal fun `captures mdc from all levels`() {
            val spy = spyForLogger("TEST_LOGGER") {
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

        @Test
        internal fun `limits record scope of spy`() {
            logger.info("test 1")
            val spy = spyForLogger("TEST_LOGGER") {
                logger.info("test 2")
            }
            logger.info("test 3")

            assertThat(spy, eventsContains(messageIs("test 2")))
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
