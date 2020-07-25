package net.torommo.logspy.testing

import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactly
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactlyInOrder
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.debugs
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.errors
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.events
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.exceptions
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.infos
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.traces
import net.torommo.logspy.matchers.LogSpyMatcher.Companion.warnings
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exception
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.level
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdc
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.message
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.type
import net.torommo.logspy.spyOn
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
            val spy =
                spyOn<TestClass> {
                    logger.error("error")
                    logger.warn("warn")
                    logger.info("info")
                    logger.debug("debug")
                    logger.trace("trace")
                }

            assertThat(
                spy,
                allOf(
                    events(
                        containingExactlyInOrder(
                            allOf(message(`is`("error")), level(`is`(ERROR))),
                            allOf(message(`is`("warn")), level(`is`(WARN))),
                            allOf(message(`is`("info")), level(`is`(INFO))),
                            allOf(message(`is`("debug")), level(`is`(DEBUG))),
                            allOf(message(`is`("trace")), level(`is`(TRACE)))
                        )
                    ),
                    errors(containingExactly(`is`("error"))),
                    warnings(containingExactly(`is`("warn"))),
                    infos(containingExactly(`is`("info"))),
                    debugs(containingExactly(`is`("debug"))),
                    traces(containingExactly(`is`("trace")))
                )
            )
        }

        @Test
        internal fun `renders messages`() {
            val spy = spyOn<TestClass> { logger.info("{} is {} test", "this", "a") }

            assertThat(spy, events(containingExactly(message(`is`("this is a test")))))
        }

        @Test
        internal fun `captures exceptions from all levels`() {
            val spy =
                spyOn<TestClass> {
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
                spy,
                allOf(
                    events(
                        containingExactlyInOrder(
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Error exception"))
                                    )
                                ),
                                level(`is`(ERROR))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Warn exception"))
                                    )
                                ),
                                level(`is`(WARN))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Info exception"))
                                    )
                                ),
                                level(`is`(INFO))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Debug exception"))
                                    )
                                ),
                                level(`is`(DEBUG))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Trace exception"))
                                    )
                                ),
                                level(`is`(TRACE))
                            )
                        )
                    ),
                    exceptions(
                        containingExactlyInOrder(
                            ThrowableSnapshotMatchers.message(`is`("Error exception")),
                            ThrowableSnapshotMatchers.message(`is`("Warn exception")),
                            ThrowableSnapshotMatchers.message(`is`("Info exception")),
                            ThrowableSnapshotMatchers.message(`is`("Debug exception")),
                            ThrowableSnapshotMatchers.message(`is`("Trace exception"))
                        )
                    )
                )
            )
        }

        @Test
        internal fun `limits record scope of spy`() {
            logger.info("test 1")
            val spy = spyOn<TestClass> { logger.info("test 2") }
            logger.info("test 3")

            assertThat(spy, events(containingExactly(message(`is`("test 2")))))
        }

        @Test
        internal fun `captures mdc from all levels`() {
            val spy =
                spyOn<TestClass> {
                    withMdc("errorKey", "error") { logger.error("error") }
                    withMdc("warnKey", "warn") { logger.warn("error") }
                    withMdc("infoKey", "info") { logger.info("info") }
                    withMdc("debugKey", "debug") { logger.debug("debug") }
                    withMdc("traceKey", "trace") { logger.debug("trace") }
                }

            assertThat(
                spy,
                events(
                    containingExactlyInOrder(
                        mdc(`is`(mapOf("errorKey" to "error"))),
                        mdc(`is`(mapOf("warnKey" to "warn"))),
                        mdc(`is`(mapOf("infoKey" to "info"))),
                        mdc(`is`(mapOf("debugKey" to "debug"))),
                        mdc(`is`(mapOf("traceKey" to "trace")))
                    )
                )
            )
        }
    }

    @Nested
    inner class `Spy by literal` {
        private val logger = LoggerFactory.getLogger("TEST_LOGGER")

        @Test
        internal fun `captures messages from all levels`() {
            val spy =
                spyOn("TEST_LOGGER") {
                    logger.error("error")
                    logger.warn("warn")
                    logger.info("info")
                    logger.debug("debug")
                    logger.trace("trace")
                }

            assertThat(
                spy,
                allOf(
                    events(
                        containingExactlyInOrder(
                            allOf(message(`is`("error")), level(`is`(ERROR))),
                            allOf(message(`is`("warn")), level(`is`(WARN))),
                            allOf(message(`is`("info")), level(`is`(INFO))),
                            allOf(message(`is`("debug")), level(`is`(DEBUG))),
                            allOf(message(`is`("trace")), level(`is`(TRACE)))
                        )
                    ),
                    errors(containingExactly(`is`("error"))),
                    warnings(containingExactly(`is`("warn"))),
                    infos(containingExactly(`is`("info"))),
                    debugs(containingExactly(`is`("debug"))),
                    traces(containingExactly(`is`("trace")))
                )
            )
        }

        @Test
        internal fun `captures exceptions from all levels`() {
            val spy =
                spyOn("TEST_LOGGER") {
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
                spy,
                allOf(
                    events(
                        containingExactlyInOrder(
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Error exception"))
                                    )
                                ),
                                level(`is`(ERROR))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Warn exception"))
                                    )
                                ),
                                level(`is`(WARN))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Info exception"))
                                    )
                                ),
                                level(`is`(INFO))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Debug exception"))
                                    )
                                ),
                                level(`is`(DEBUG))
                            ),
                            allOf(
                                exception(
                                    allOf(
                                        type(`is`("java.lang.RuntimeException")),
                                        ThrowableSnapshotMatchers.message(`is`("Trace exception"))
                                    )
                                ),
                                level(`is`(TRACE))
                            )
                        )
                    ),
                    exceptions(
                        containingExactlyInOrder(
                            ThrowableSnapshotMatchers.message(`is`("Error exception")),
                            ThrowableSnapshotMatchers.message(`is`("Warn exception")),
                            ThrowableSnapshotMatchers.message(`is`("Info exception")),
                            ThrowableSnapshotMatchers.message(`is`("Debug exception")),
                            ThrowableSnapshotMatchers.message(`is`("Trace exception"))
                        )
                    )
                )
            )
        }

        @Test
        internal fun takesSnapshotOfException() {
            val spy =
                spyOn("TEST_LOGGER") {
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
                    exception.stackTrace =
                        arrayOf(
                            StackTraceElement(
                                "OuterClass",
                                "callingMethod",
                                "TestFailingClass.class",
                                389
                            ),
                            StackTraceElement(
                                "TestFailingClass",
                                "failingMethod",
                                "TestFailingClass.class",
                                37
                            )
                        )

                    logger.error("Test exception", exception)
                }

            assertThat(
                spy,
                exceptions(
                    containingExactly(
                        `is`(
                            ThrowableSnapshot(
                                "java.lang.RuntimeException",
                                "Root",
                                ThrowableSnapshot(
                                    "java.lang.IllegalArgumentException",
                                    "Cause",
                                    ThrowableSnapshot(
                                        "java.lang.NullPointerException",
                                        "Cause cause"
                                    )
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
            )
        }

        @Test
        internal fun `captures mdc from all levels`() {
            val spy =
                spyOn("TEST_LOGGER") {
                    withMdc("errorKey", "error") { logger.error("error") }
                    withMdc("warnKey", "warn") { logger.warn("error") }
                    withMdc("infoKey", "info") { logger.info("info") }
                    withMdc("debugKey", "debug") { logger.debug("debug") }
                    withMdc("traceKey", "trace") { logger.debug("trace") }
                }

            assertThat(
                spy,
                events(
                    containingExactlyInOrder(
                        mdc(`is`(mapOf("errorKey" to "error"))),
                        mdc(`is`(mapOf("warnKey" to "warn"))),
                        mdc(`is`(mapOf("infoKey" to "info"))),
                        mdc(`is`(mapOf("debugKey" to "debug"))),
                        mdc(`is`(mapOf("traceKey" to "trace")))
                    )
                )
            )
        }

        @Test
        internal fun `limits record scope of spy`() {
            logger.info("test 1")
            val spy = spyOn("TEST_LOGGER") { logger.info("test 2") }
            logger.info("test 3")

            assertThat(spy, events(containingExactly(message(`is`("test 2")))))
        }
    }

    private inline fun <T> withMdc(key: String, value: String, action: () -> T): T {
        MDC.putCloseable(key, value).use { return action() }
    }
}

internal class TestClass
