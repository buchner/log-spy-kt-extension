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
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.noCause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.stackContains
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.suppressedContains
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.typeIs
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

internal class JsonEventParserTest {

    @Test
    internal fun `parses single event`() {
        val entry = content {
            message = "Test message"
        }

        val events = parseToEvents(entry)

        assertThat(events, contains(SpiedEventMatcher.messageIs("Test message")))
    }

    @Test
    internal fun `ignores event when from not matching logger`() {
        val entry = content {
            loggerName = "net.torommo.logspy.AnotherName"
        }

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
        val entry = content {
            this.level = literal
        }

        val events = parseToEvents(entry)

        assertThat(events, contains(levelIs(level)))
    }

    @Test
    internal fun `maps exception type`() {
        val entry = content {
            stackTrace {
                type = "java.lang.RuntimeException"
            }
        }

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
        ",",
        "'', ''"
    )
    @ParameterizedTest
    internal fun `maps exception message`(literal: String?, expected: String?) {
        val entry = content {
            stackTrace {
                message = literal
            }
        }

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
        val entry = content {
            stackTrace {
                cause {
                    cause {
                        message = "Causing causing exception"
                    }
                    message = "Causing exception"
                }
            }
        }

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
        val entry = content {
            stackTrace {
                suppressed {
                    message = "First suppressed exception"
                    suppressed {
                        this.message = "Suppressed suppressed exception"
                    }
                }
                suppressed {
                    this.message = "Second suppressed exception"
                }
            }
        }

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
        val entry = content {
            stackTrace {
                frame {
                    declaringClass = "net.torommo.logspy.TestA1"
                    methodName = "testA1"
                }
                frame {
                    declaringClass = "net.torommo.logspy.TestA2"
                    methodName = "testA2"
                }
            }
        }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(allOf(
                    noCause(),
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
        val entry = content {
            stackTrace {
                cause {
                    cause {
                        frame {
                            declaringClass = "net.torommo.logspy.TestB1"
                            methodName = "testB1"
                        }
                        frame {
                            declaringClass = "net.torommo.logspy.TestB2"
                            methodName = "testB2"
                        }
                    }
                    frame {
                        declaringClass = "net.torommo.logspy.TestA"
                        methodName = "testA"
                    }
                }
            }
        }

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
                            allOf(
                                noCause(),
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
                        )
                    ))
                ))
            )
        )
    }

    @Test
    internal fun `maps stack from causal chain when root cause is first`() {
        val entry = content {
            rootCauseFirstStackTrace {
                cause {
                    cause {
                        frame {
                            declaringClass = "net.torommo.logspy.TestB1"
                            methodName = "testB1"
                        }
                        frame {
                            declaringClass = "net.torommo.logspy.TestB2"
                            methodName = "testB2"
                        }
                    }
                    frame {
                        declaringClass = "net.torommo.logspy.TestA"
                        methodName = "testA"
                    }
                }
            }
        }

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
                            allOf(
                                noCause(),
                                stackContains(
                                    allOf(
                                        declaringClassIs("net.torommo.logspy.TestB1"),
                                        StackTraceElementSnapshotMatchers.methodNameIs("testB1")
                                    ),
                                    allOf(
                                        declaringClassIs("net.torommo.logspy.TestB2"),
                                        StackTraceElementSnapshotMatchers.methodNameIs("testB2")
                                    ))
                        ))
                    ))
                ))
            )
        )
    }

    @Test
    internal fun `maps stack from suppressed`() {
        val entry = content {
            stackTrace {
                suppressed {
                    suppressed {
                        frame {
                            declaringClass = "net.torommo.logspy.TestB1"
                            methodName = "testB1"
                        }
                        frame {
                            declaringClass = "net.torommo.logspy.TestB2"
                            methodName = "testB2"
                        }
                    }
                    frame {
                        declaringClass = "net.torommo.logspy.TestA"
                        methodName = "testA"
                    }
                }
            }
        }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exceptionWith(
                    suppressedContains(allOf(
                        noCause(),
                        stackContains(allOf(
                            declaringClassIs("net.torommo.logspy.TestA"),
                            StackTraceElementSnapshotMatchers.methodNameIs("testA")
                        )),
                        suppressedContains(allOf(
                            noCause(),
                            stackContains(
                                allOf(
                                    declaringClassIs("net.torommo.logspy.TestB1"),
                                    StackTraceElementSnapshotMatchers.methodNameIs("testB1")
                                ),
                                allOf(
                                    declaringClassIs("net.torommo.logspy.TestB2"),
                                    StackTraceElementSnapshotMatchers.methodNameIs("testB2")
                                )
                        )))
                    ))
                )
            )
        )
    }

    @Test
    internal fun `ignores omitted frames`() {
        val entry = content {
            stackTrace {
                frame {
                    declaringClass = "net.torommo.logspy.Test"
                }
                omittedFrame { }
            }
        }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exceptionWith(stackContains(declaringClassIs("net.torommo.logspy.Test"))))
        )
    }

    @MethodSource("mdcConfigurations")
    @ParameterizedTest
    internal fun `maps mdc`(configuration: JsonEntryBuilder.() -> Unit) {
        val entry = configuration.merge(
            content {
                field("test-key-1", "test-value-1")
                field("test-key-2", "test-value-2")
            }
        )

        val events = parseToEvents(entry)

        assertThat(events, contains(mdcIs(mapOf(
            "test-key-1" to "test-value-1",
            "test-key-2" to "test-value-2"
        ))))
    }

    @ValueSource(strings = ["garbled", """{""""])
    @ParameterizedTest
    internal fun `throws assertion exception when output is unparsable`(payload: String) {
        assertThrows<AssertionError> { JsonEventParser("TestLogger", payload).events() }
    }

    private fun <T> (T.() -> Unit).merge(block: T.() -> Unit): T.() -> Unit {
        return fun T.() {
            this@merge(this)
            block(this)
        }
    }

    companion object {
        @JvmStatic
        fun mdcConfigurations(): Iterator<Arguments> {
            return sequenceOf(
                arguments(content {}),
                arguments(content { stackTrace {} }),
                arguments(content { complexField("test") }),
                arguments(content { marker("testMarker") })
            ).iterator()
        }

        private fun content(block: JsonEntryBuilder.() -> Unit): JsonEntryBuilder.() -> Unit {
            return block
        }
    }

    private fun content(block: JsonEntryBuilder.() -> Unit): JsonEntryBuilder.() -> Unit {
        return block
    }

    private fun parseToEvents(
        block: JsonEntryBuilder.() -> Unit,
        loggerName: String = "net.torommo.logspy.LogSpyExtensionIntegrationTest"
    ): List<SpiedEvent> {
        return JsonEventParser(loggerName, JsonEntryBuilder().apply(block).build()).events()
    }

    @DslMarker
    annotation class JsonEntryDsl

    @JsonEntryDsl
    internal class JsonEntryBuilder {
        var level: String = "INFO"
        var message: String = "Test message"
        var loggerName: String = "net.torommo.logspy.LogSpyExtensionIntegrationTest"
        private var stackTrace: StackTraceBuilder? = null
        private val simpleAdditionalFields: MutableMap<String, String> = mutableMapOf()
        private val nestedAdditionalFields: MutableSet<String> = mutableSetOf()
        private val markers: MutableSet<String> = mutableSetOf()

        fun stackTrace(block: (StackTraceBuilder.() -> Unit)?) {
            if (block == null) {
                this.stackTrace = null
            } else {
                this.stackTrace = StackTraceBuilder().apply(block)
            }
        }

        fun rootCauseFirstStackTrace(block: (StackTraceBuilder.() -> Unit)?) {
            if (block == null) {
                this.stackTrace = null
            } else {
                this.stackTrace = StackTraceBuilder(rootCauseFirst = true).apply(block)
            }
        }

        fun field(key: String, value: String) {
            nestedAdditionalFields.remove(key)
            simpleAdditionalFields.put(key, value)
        }

        fun complexField(key: String) {
            simpleAdditionalFields.remove(key)
            nestedAdditionalFields.add(key)
        }

        fun marker(marker: String) {
            markers.add(marker)
        }

        fun build(): String {
            val stackTraceJson = stackTrace?.let { ""","stack_trace": "${it.build()}"""" } ?: ""
            val additionalFieldsJson = if (additionalFieldsAsJson().isEmpty()) {
                ""
            } else ",${additionalFieldsAsJson()}"
            val markersJson = if (markersAsJson().isEmpty()) { "" } else { ",${markersAsJson()}" }
            return """{"@timestamp":"2019-10-31T20:31:17.234+01:00","@version":"1","message":"${message}","logger_name":"${loggerName}","thread_name":"main","level":"${level}","level_value":20000${stackTraceJson}${additionalFieldsJson}${markersJson}}"""
        }

        private fun additionalFieldsAsJson(): String {
            var result = simpleAdditionalFields.map { """"${it.key}": "${it.value}"""" }
                .joinToString(",")
            if (result.isNotEmpty() && nestedAdditionalFields.isNotEmpty()) {
                result += ","
            }
            result += nestedAdditionalFields.map { """"${it}": { "value": "test" }""" }
                .joinToString( ",")

            return result
        }

        private fun markersAsJson(): String {
            val items = markers.map { """"${it}"""" }
                .joinToString(",")
            if (markers.isNotEmpty()) {
                return """"tags": [${items}]"""
            } else {
                return ""
            }
        }
    }

    @JsonEntryDsl
    internal class StackTraceBuilder(val rootCauseFirst: Boolean = false) {
        var type: String = "java.lang.RuntimeException"
        var message: String? = null
        private var cause: StackTraceBuilder? = null
        private val suppressed: MutableList<StackTraceBuilder> = mutableListOf()
        private val frames: MutableList<FrameBuilder> = mutableListOf()

        fun cause(block: (StackTraceBuilder.() -> Unit)?) {
            if (block == null) {
                this.cause = null
            } else {
                this.cause = StackTraceBuilder(rootCauseFirst).apply(block)
            }
        }

        fun suppressed(block: StackTraceBuilder.() -> Unit) {
            this.suppressed.add(StackTraceBuilder(rootCauseFirst).apply(block))
        }

        fun frame(block: FilledFrameBuilder.() -> Unit) {
            this.frames.add(FilledFrameBuilder().apply(block))
        }

        fun omittedFrame(block: OmittedFrameBuilder.() -> Unit) {
            this.frames.add(OmittedFrameBuilder().apply(block))
        }

        fun build(): String {
            return build(0)
        }

        private fun build(indent: Int): String {
            return if (rootCauseFirst) {
                buildRootCauseFirst(indent)
            } else {
                buildRootCauseLast(indent, true)
            }
        }

        private fun buildRootCauseFirst(indent: Int): String {
            val prefix = if (cause == null) {
                ""
            } else {
                "Wrapped by: "
            }
            val header = if (message == null) "${type}\\n" else "${type}: ${message}\\n"
            val stack = frames.asSequence()
                .map { it.build(indent) }
                .joinToString("")
            val suppressed = this.suppressed.asSequence()
                .map { "${"\\t".repeat(indent + 1)}Suppressed: ${it.build(indent + 1)}" }
                .joinToString("")

            return "${cause?.buildRootCauseFirst(indent) ?: ""}${"\\t".repeat(indent)}${prefix}${header}${stack}${suppressed}"
        }

        private fun buildRootCauseLast(indent: Int, root: Boolean): String {
            val prefix = if (root) {
                ""
            } else {
                "Caused by: "
            }
            val header = if (message == null) "${type}\\n" else "${type}: ${message}\\n"
            val stack = frames.asSequence()
                .map { it.build(indent) }
                .joinToString("")
            val suppressed = this.suppressed.asSequence()
                .map { "${"\\t".repeat(indent + 1)}Suppressed: ${it.build(indent + 1)}" }
                .joinToString("")

            return "${"\\t".repeat(indent)}${prefix}${header}${stack}${suppressed}${cause?.buildRootCauseLast(indent, false) ?: ""}"
        }
    }

    internal interface FrameBuilder {
        fun build(indent: Int): String
    }

    @JsonEntryDsl
    internal class FilledFrameBuilder : FrameBuilder {

        var declaringClass: String = "net.torommo.logspy.Test"
        var methodName: String = "test"

        override fun build(indent: Int): String {
            return "${"\\t".repeat(indent + 1)}at ${declaringClass}.${methodName}(Test.java:123)\\n"
        }
    }

    @JsonEntryDsl
    internal class OmittedFrameBuilder : FrameBuilder {

        override fun build(indent: Int): String {
            return "${"\\t".repeat(indent + 1)}... 42 common frames ommited\\n"
        }
    }
}