package net.torommo.logspy

import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.matchers.SpiedEventMatcher
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exceptionWith
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.levelIs
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdcIs
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers.Companion.declaringClassIs
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.causeThat
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.messageIs
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.stackContains
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.suppressedContains
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.typeIs
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

internal class JsonEventParserTest {

    @Test
    internal fun `parses single event`() {
        val entry = JsonEntryBuilder(message = "Test message")
            .build()

        val events = parseToEvents(entry)

        assertThat(events, contains(SpiedEventMatcher.messageIs("Test message")))
    }

    @Test
    internal fun `ignores event when from not matching logger`() {
        val entry = JsonEntryBuilder(loggerName = "net.torommo.logspy.AnotherName")
            .build()

        val events = parseToEvents(entry, "net.torommo.logspy.DifferentName")

        assertThat(events, empty())
    }

    @ParameterizedTest
    @CsvSource(
        "TRACE, TRACE",
        "DEBUG, DEBUG",
        "INFO, INFO",
        "WARN, WARN",
        "ERROR, ERROR"
    )
    internal fun `maps level`(literal: String, level: Level) {
        val entry = JsonEntryBuilder(level = literal)
            .build()

        val events = parseToEvents(entry)

        assertThat(events, contains(levelIs(level)))
    }

    @Test
    internal fun `maps exception type`() {
        val stackTrace = StackTraceBuilder("java.lang.RuntimeException")
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    typeIs("java.lang.RuntimeException")
                )
            )
        )
    }

    @CsvSource(
        "Test message, Test message",
        ","
    )
    @ParameterizedTest
    internal fun `maps exception message`(literal: String?, expected: String?) {
        val stackTrace = StackTraceBuilder()
            .setMessage(literal)
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    ThrowableSnapshotMatchers.messageIs(expected)
                )
            )
        )
    }

    @Test
    internal fun `maps exception cause`() {
        val causingCause = StackTraceBuilder()
            .setMessage("Causing causing exception")
        val cause = StackTraceBuilder()
            .setCause(causingCause)
            .setMessage("Causing exception")
        val stackTrace = StackTraceBuilder()
            .setCause(cause)
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    causeThat(allOf(
                        messageIs("Causing exception"),
                        causeThat(messageIs("Causing causing exception"))
                    ))
                )
            )
        )
    }

    @Test
    internal fun `maps suppressed exceptions`() {
        val suppressedSuppressed = StackTraceBuilder()
            .setMessage("Suppressed suppressed exception")
        val firstSuppressed = StackTraceBuilder()
            .addSuppressed(suppressedSuppressed)
            .setMessage("First suppressed exception")
        val secondSuppressed = StackTraceBuilder()
            .setMessage("Second suppressed exception")
        val stackTrace = StackTraceBuilder()
            .addSuppressed(firstSuppressed)
            .addSuppressed(secondSuppressed)
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    suppressedContains(
                        allOf(
                            messageIs("First suppressed exception"),
                            suppressedContains(messageIs("Suppressed suppressed exception"))
                        ),
                        messageIs("Second suppressed exception")
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps stack from exception`() {
        val stackTrace = StackTraceBuilder()
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestA1", "testA1"))
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestA2", "testA2"))
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(allOf(
                    stackContains(
                        allOf(
                            declaringClassIs("net.torommo.logspy.TestA1"),
                            StackTraceElementSnapshotMatchers.methodNameIs("testA1")
                        ),
                        allOf(
                            declaringClassIs("net.torommo.logspy.TestA2"),
                            StackTraceElementSnapshotMatchers.methodNameIs("testA2")
                        )
                    )
                ))
            )
        )
    }

    @Test
    internal fun `maps stack from causal chain`() {
        val causingCause = StackTraceBuilder()
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestB1", "testB1"))
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestB2", "testB2"))
        val cause = StackTraceBuilder()
            .setCause(causingCause)
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestA", "testA"))
        val stackTrace = StackTraceBuilder()
            .setCause(cause)
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(allOf(
                    causeThat(allOf(
                        stackContains(
                            allOf(
                                declaringClassIs("net.torommo.logspy.TestA"),
                                StackTraceElementSnapshotMatchers.methodNameIs("testA")
                            )
                        ),
                        causeThat(
                            stackContains(
                                allOf(
                                    declaringClassIs("net.torommo.logspy.TestB1"),
                                    StackTraceElementSnapshotMatchers.methodNameIs("testB1")
                                ),
                                allOf(
                                    declaringClassIs("net.torommo.logspy.TestB2"),
                                    StackTraceElementSnapshotMatchers.methodNameIs("testB2")
                                ))
                        )
                    ))
                ))
            )
        )
    }

    @Test
    internal fun `maps stack from suppressed`() {
        val suppressedSuppressed = StackTraceBuilder()
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestB1", "testB1"))
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestB2", "testB2"))
        val suppressed = StackTraceBuilder()
            .addSuppressed(suppressedSuppressed)
            .addFrame(FilledFrameBuilder("net.torommo.logspy.TestA", "testA"))
        val stackTrace = StackTraceBuilder()
            .addSuppressed(suppressed)
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    suppressedContains(allOf(
                        stackContains(allOf(
                            declaringClassIs("net.torommo.logspy.TestA"),
                            StackTraceElementSnapshotMatchers.methodNameIs("testA")
                        )),
                        suppressedContains(stackContains(
                            allOf(
                                declaringClassIs("net.torommo.logspy.TestB1"),
                                StackTraceElementSnapshotMatchers.methodNameIs("testB1")
                            ),
                            allOf(
                                declaringClassIs("net.torommo.logspy.TestB2"),
                                StackTraceElementSnapshotMatchers.methodNameIs("testB2")
                            )
                        ))
                    ))
                )
            )
        )
    }

    @Test
    internal fun `ignores omitted frames`() {
        val stackTrace = StackTraceBuilder()
            .addFrame(FilledFrameBuilder("net.torommo.logspy.Test"))
            .addFrame(OmittedFrameBuilder())
        val entry = JsonEntryBuilder()
            .setStackTrace(stackTrace)
            .build()

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exceptionWith(stackContains(declaringClassIs("net.torommo.logspy.Test"))))
        )
    }

    @MethodSource("mdcConfigurations")
    @ParameterizedTest
    internal fun `maps mdc`(configuration: JsonEntryBuilder) {
        val entry = configuration
            .addField("test-key-1", "test-value-1")
            .addField("test-key-2", "test-value-2")
            .build()

        val events = parseToEvents(entry)

        assertThat(events, contains(mdcIs(mapOf(
            "test-key-1" to "test-value-1",
            "test-key-2" to "test-value-2"
        ))))
    }

    companion object {
        @JvmStatic
        fun mdcConfigurations(): Iterator<Arguments> {
            return sequenceOf(
                arguments(JsonEntryBuilder()),
                arguments(JsonEntryBuilder().setStackTrace(StackTraceBuilder())),
                arguments(JsonEntryBuilder().addComplexField("test")),
                arguments(JsonEntryBuilder().addMarker("testMarker"))
            ).iterator()
        }
    }

    private fun parseToEvents(
        entry: String,
        loggerName: String = "net.torommo.logspy.LogSpyExtensionIntegrationTest"
    ): List<SpiedEvent> {
        return JsonEventParser(loggerName, entry).events()
    }

    internal class JsonEntryBuilder(
        val loggerName: String = "net.torommo.logspy.LogSpyExtensionIntegrationTest",
        val message: String = "Test message",
        val level: String = "INFO"
    ) {
        private var stackTrace: StackTraceBuilder? = null
        private val simpleAdditionalFields: MutableMap<String, String> = mutableMapOf()
        private val nestedAdditionalFields: MutableSet<String> = mutableSetOf()
        private val markers: MutableSet<String> = mutableSetOf()

        fun setStackTrace(stackTrace: StackTraceBuilder?): JsonEntryBuilder {
            this.stackTrace = stackTrace
            return this
        }

        fun addField(key: String, value: String): JsonEntryBuilder {
            nestedAdditionalFields.remove(key)
            simpleAdditionalFields.put(key, value)
            return this
        }

        fun addComplexField(key: String): JsonEntryBuilder {
            simpleAdditionalFields.remove(key)
            nestedAdditionalFields.add(key)
            return this
        }

        fun addMarker(marker: String): JsonEntryBuilder {
            markers.add(marker)
            return this
        }

        fun build(): String {
            val stackTraceJson = stackTrace?.let { ""","stack_trace": "${it.build()}"""" } ?: ""
            val additionalFieldsJson = if (additionalFieldsAsJson().isEmpty()) {
                ""
            } else ",${additionalFieldsAsJson()}"
            val markersJson = if (markersAsJson().isEmpty()) { "" } else { ",${markersAsJson()}" }
            return """{"@timestamp":"2019-10-31T20:31:17.234+01:00","@version":"1","message":"${message}","logger_name":"${loggerName}","thread_name":"main","level":"${level}","level_value":20000${stackTraceJson}${additionalFieldsJson}${markersJson}}"""
        }

        fun additionalFieldsAsJson(): String {
            var result = simpleAdditionalFields.map { """"${it.key}": "${it.value}"""" }
                .joinToString(",")
            if (result.isNotEmpty() && nestedAdditionalFields.isNotEmpty()) {
                result += ","
            }
            result += nestedAdditionalFields.map { """"${it}": { "value": "test" }""" }
                .joinToString( ",")

            return result
        }

        fun markersAsJson(): String {
            val items = markers.map { """"${it}"""" }
                .joinToString(",")
            if (markers.isNotEmpty()) {
                return """"tags": [${items}]"""
            } else {
                return ""
            }
        }
    }

    internal class StackTraceBuilder(val type: String = "java.lang.RuntimeException") {
        private var message: String? = null
        private var cause: StackTraceBuilder? = null
        private val suppressed: MutableList<StackTraceBuilder> = mutableListOf()
        private val frames: MutableList<FrameBuilder> = mutableListOf()

        fun setMessage(message: String?): StackTraceBuilder {
            this.message = message
            return this
        }

        fun setCause(cause: StackTraceBuilder?): StackTraceBuilder {
            this.cause = cause
            return this
        }

        fun addSuppressed(supressed: StackTraceBuilder): StackTraceBuilder {
            this.suppressed.add(supressed)
            return this;
        }

        fun addFrame(frame: FrameBuilder): StackTraceBuilder {
            this.frames.add(frame)
            return this;
        }

        fun build(): String {
            return build(0)
        }

        private fun build(indent: Int): String {
            val header = "${type}: ${message}\\n"
            val stack = frames.asSequence()
                .map { it.build(indent) }
                .joinToString("")
            val causedBy = cause?.let { "${"\\t".repeat(indent)}Caused by: ${it.build()}" }
            val suppressed = this.suppressed.asSequence()
                .map { "${"\\t".repeat(indent + 1)}Suppressed: ${it.build(indent + 1)}" }
                .joinToString("")

            return "${header}${stack}${causedBy ?: ""}${suppressed}"
        }
    }

    internal interface FrameBuilder {
        fun build(indent: Int): String
    }

    internal class FilledFrameBuilder(
        val declaringClass: String = "net.torommo.logspy.Test",
        val methodName: String = "test"
    ) : FrameBuilder {

        override fun build(indent: Int): String {
            return "${"\\t".repeat(indent + 1)}at ${declaringClass}.${methodName}(Test.java:123)\\n"
        }
    }

    internal class OmittedFrameBuilder : FrameBuilder {

        override fun build(indent: Int): String {
            return "${"\\t".repeat(indent + 1)}... 42 common frames ommited\\n"
        }
    }
}