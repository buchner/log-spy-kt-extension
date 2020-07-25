package net.torommo.logspy

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactly
import net.torommo.logspy.matchers.IterableMatchers.Companion.containingExactlyInOrder
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.exception
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.level
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.mdc
import net.torommo.logspy.matchers.SpiedEventMatcher.Companion.message
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers.Companion.declaringClass
import net.torommo.logspy.matchers.StackTraceElementSnapshotMatchers.Companion.methodName
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.cause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.noCause
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.stack
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.suppressed
import net.torommo.logspy.matchers.ThrowableSnapshotMatchers.Companion.type
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

internal class JsonEventParserTest {

    @Test
    internal fun `parses single event`() {
        val entry = content { message = "Test message" }

        val events = parseToEvents(entry)

        assertThat(events, contains(message(`is`("Test message"))))
    }

    @Test
    internal fun `ignores event when from not matching logger`() {
        val entry = content { loggerName = "net.torommo.logspy.AnotherName" }

        val events = parseToEvents(entry, "net.torommo.logspy.DifferentName")

        assertThat(events, empty())
    }

    @MethodSource("incompleteConfigurations")
    @ParameterizedTest
    internal fun `ignores event when incomplete`(payload: String) {
        val events =
            JsonEventParser("net.torommo.logspy.LogSpyExtensionIntegrationTest", payload).events()

        assertThat(events, empty())
    }

    @ParameterizedTest
    @CsvSource("TRACE, TRACE", "DEBUG, DEBUG", "INFO, INFO", "WARN, WARN", "ERROR, ERROR")
    internal fun `maps level`(literal: String, level: Level) {
        val entry = content { this.level = literal }

        val events = parseToEvents(entry)

        assertThat(events, contains(level(`is`(level))))
    }

    @CsvSource(
        "net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Modules
        "net.torommo.logspy/test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy//net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Kotlin specific identifiers
        "net.torommo.logspy.My exception, net.torommo.logspy.My exception",
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo logspy.Exception, net.torommo logspy.Exception",
        // Uncommon but valid Java identifiers
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo.logspy.Δ, net.torommo.logspy.Δ",
        "net.torommoΔlogspy.Test, net.torommoΔlogspy.Test"
    )
    @ParameterizedTest
    internal fun `maps exception type`(value: String, expectedType: String) {
        val entry = content { stackTrace { this.type = value } }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(type(`is`(expectedType)))))
    }

    @CsvSource(
        "Test message, Test message",
        ",",
        "'', ''",
        "\ttest\tmessage, \ttest\tmessage",
        "'Test: message', 'Test: message'" // Mimics the type prefix
        )
    @ParameterizedTest
    internal fun `maps exception message`(literal: String?, expected: String?) {
        val entry = content { stackTrace { message = literal } }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(ThrowableSnapshotMatchers.message(`is`(expected)))))
    }

    @Test
    internal fun `maps multiline message`() {
        val entry = content { stackTrace { message = "test\nmessage\n" } }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exception(ThrowableSnapshotMatchers.message(`is`("test\nmessage\n"))))
        )
    }

    @ValueSource(strings = ["#", "`", """""""])
    @ParameterizedTest
    internal fun `maps special chars in exception message`(value: String) {
        val entry = content { stackTrace { message = value } }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(ThrowableSnapshotMatchers.message(`is`(value)))))
    }

    @Test
    internal fun `mapping favours message over type when ambiguous`() {
        val entry =
            content {
                stackTrace {
                    type = "java.lang.String: exception"
                    message = "Test message"
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    allOf(
                        type(`is`("java.lang.String")),
                        ThrowableSnapshotMatchers.message(`is`("exception: Test message"))
                    )
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message over frames when multi line message is ambiguous`() {
        val entry =
            content {
                stackTrace {
                    message = "test\n\tat something.Else"
                    frame {
                        declaringClass = "net.torommo.logspy.Anything"
                        methodName = "toDo"
                        fileName = "Anything.kt"
                        line = "23"
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    ThrowableSnapshotMatchers.message(
                        `is`(
                            "test\n\t\n\n\t\tat something.Else\n\t\nat " +
                                "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps exception cause`() {
        val entry =
            content {
                stackTrace {
                    cause {
                        cause { message = "Causing causing exception" }
                        message = "Causing exception"
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    cause(
                        allOf(
                            ThrowableSnapshotMatchers.message(`is`("Causing exception")),
                            cause(
                                ThrowableSnapshotMatchers.message(`is`("Causing causing exception"))
                            )
                        )
                    )
                )
            )
        )
    }

    @CsvSource(
        "net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Modules
        "net.torommo.logspy/test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy//net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Kotlin specific identifiers
        "net.torommo.logspy.My exception, net.torommo.logspy.My exception",
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo logspy.Exception, net.torommo logspy.Exception",
        // Uncommon but valid Java identifiers
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo.logspy.Δ, net.torommo.logspy.Δ",
        "net.torommoΔlogspy.Test, net.torommoΔlogspy.Test"
    )
    @ParameterizedTest
    internal fun `maps type of cause`(value: String, expectedType: String) {
        val entry = content { stackTrace { cause { type = value } } }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(cause(type(`is`(expectedType))))))
    }

    @CsvSource(
        "net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Modules
        "net.torommo.logspy/test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy//net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Kotlin specific identifiers
        "net.torommo.logspy.My exception, net.torommo.logspy.My exception",
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo logspy.Exception, net.torommo logspy.Exception",
        // Uncommon but valid Java identifiers
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo.logspy.Δ, net.torommo.logspy.Δ",
        "net.torommoΔlogspy.Test, net.torommoΔlogspy.Test"
    )
    @ParameterizedTest
    internal fun `maps type of cause when root cause first`(value: String, expectedType: String) {
        val entry = content {
            rootCauseFirstStackTrace {
                type = value
                cause {}
            }
        }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(type(`is`(expectedType)))))
    }

    @Test
    internal fun `mapping favours message from cause over type when ambiguous`() {
        val entry =
            content {
                stackTrace {
                    cause {
                        type = "java.lang.String: exception"
                        message = "Test message"
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    cause(
                        allOf(
                            type(`is`("java.lang.String")),
                            ThrowableSnapshotMatchers.message(`is`("exception: Test message"))
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message from cause over type when ambiguous and root cause first`() {
        val entry =
            content {
                rootCauseFirstStackTrace {
                    type = "java.lang.String: exception"
                    message = "Test message"
                    cause {}
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    allOf(
                        type(`is`("java.lang.String")),
                        ThrowableSnapshotMatchers.message(`is`("exception: Test message"))
                    )
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message from cause over frames when multiline message is ambiguous`() {
        val entry =
            content {
                stackTrace {
                    cause {
                        message = "Test\n\tat something else"
                        frame {
                            declaringClass = "net.torommo.logspy.Anything"
                            methodName = "toDo"
                            fileName = "Anything.kt"
                            line = "23"
                        }
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    cause(
                        ThrowableSnapshotMatchers.message(
                            `is`(
                                "Test\n\t\n\n\t\tat something else\n\t\nat " +
                                    "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message from cause over frames when multiline message is ambiguous and root cause first`() {
        val entry =
            content {
                rootCauseFirstStackTrace {
                    message = "Test\n\tat something else"
                    frame {
                        declaringClass = "net.torommo.logspy.Anything"
                        methodName = "toDo"
                        fileName = "Anything.kt"
                        line = "23"
                    }
                    cause {}
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    ThrowableSnapshotMatchers.message(
                        `is`(
                            "Test\n\t\n\n\t\tat something else\n\t\nat " +
                                "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps suppressed exceptions`() {
        val entry =
            content {
                stackTrace {
                    suppressed {
                        message = "First suppressed exception"
                        suppressed { this.message = "Suppressed suppressed exception" }
                    }
                    suppressed { this.message = "Second suppressed exception" }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    suppressed(
                        containingExactly(
                            allOf(
                                ThrowableSnapshotMatchers.message(
                                    `is`("First suppressed exception")
                                ),
                                suppressed(
                                    containingExactly(
                                        ThrowableSnapshotMatchers.message(
                                            `is`("Suppressed suppressed exception")
                                        )
                                    )
                                )
                            ),
                            ThrowableSnapshotMatchers.message(`is`("Second suppressed exception"))
                        )
                    )
                )
            )
        )
    }

    @CsvSource(
        "net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Modules
        "net.torommo.logspy/test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy//net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Kotlin specific identifiers
        "net.torommo.logspy.My exception, net.torommo.logspy.My exception",
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo logspy.Exception, net.torommo logspy.Exception",
        // Uncommon but valid Java identifiers
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo.logspy.Δ, net.torommo.logspy.Δ",
        "net.torommoΔlogspy.Test, net.torommoΔlogspy.Test"
    )
    @ParameterizedTest
    internal fun `maps type from suppressed`(literal: String, expected: String) {
        val entry = content { stackTrace { suppressed { this.type = literal } } }

        val events = parseToEvents(entry)

        assertThat(events, contains(exception(suppressed(containingExactly(type(`is`(expected)))))))
    }

    @CsvSource(
        "Test message, Test message",
        ",",
        "'', ''",
        "\ttest\tmessage, \ttest\tmessage",
        "'Test: message', 'Test: message'" // Mimics the type prefix
        )
    @ParameterizedTest
    internal fun `maps message from suppressed exceptions`(literal: String?, expected: String?) {
        val entry = content { stackTrace { suppressed { this.message = literal } } }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    suppressed(containingExactly(ThrowableSnapshotMatchers.message(`is`(expected))))
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message over type in suppressed when ambiguous`() {
        val entry =
            content {
                stackTrace {
                    suppressed {
                        type = "java.lang.String: exception"
                        message = "Test message"
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    suppressed(
                        containingExactly(
                            allOf(
                                type(`is`("java.lang.String")),
                                ThrowableSnapshotMatchers.message(`is`("exception: Test message"))
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps multiline message in suppressed`() {
        val entry = content { stackTrace { suppressed { message = "test\nmessage\n" } } }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    suppressed(
                        containingExactly(
                            ThrowableSnapshotMatchers.message(`is`("test\nmessage\n"))
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `mapping favours message over frames in suppressed when multi line message is ambiguous`() {
        val entry =
            content {
                stackTrace {
                    suppressed {
                        message = "test\n\tat something.Else"
                        frame {
                            declaringClass = "net.torommo.logspy.Anything"
                            methodName = "toDo"
                            fileName = "Anything.kt"
                            line = "23"
                        }
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    suppressed(
                        containingExactly(
                            ThrowableSnapshotMatchers.message(
                                `is`(
                                    "test\n\t\nat something.Else\n\t\n\n\t\tat " +
                                        "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t" +
                                        "\t\n"
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps stack from exception`() {
        val entry =
            content {
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
                exception(
                    allOf(
                        noCause(),
                        stack(
                            containingExactlyInOrder(
                                allOf(
                                    declaringClass(`is`("net.torommo.logspy.TestA1")),
                                    methodName(`is`("testA1"))
                                ),
                                allOf(
                                    declaringClass(`is`("net.torommo.logspy.TestA2")),
                                    methodName(`is`("testA2"))
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @CsvSource(
        "net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Modules
        "net.torommo.logspy/test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy//net.torommo.logspy.Test, net.torommo.logspy.Test",
        "net.torommo.logspy/net.torommo.logspy.Test, net.torommo.logspy.Test",
        "test@42.314/net.torommo.logspy.Test, net.torommo.logspy.Test",
        // Uncommon but valid Java identifiers
        "net.torommo.logspy.exception, net.torommo.logspy.exception",
        "net.torommo.logspy.Δ, net.torommo.logspy.Δ",
        "net.torommoΔlogspy.Test, net.torommoΔlogspy.Test"
    )
    @ParameterizedTest
    internal fun `maps type in stack frame from exception`(value: String, expected: String) {
        val entry = content { stackTrace { frame { declaringClass = value } } }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exception(allOf(stack(containingExactly(declaringClass(`is`(expected)))))))
        )
    }

    @Test
    internal fun `mapping favours method name over type in stack when ambiguous`() {
        val entry =
            content {
                stackTrace {
                    frame {
                        declaringClass = "net.torommo.logspy.This is"
                        this.methodName = "a test"
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    stack(
                        containingExactly(
                            allOf(
                                declaringClass(`is`("net.torommo.logspy")),
                                methodName(`is`("This is.a test"))
                            )
                        )
                    )
                )
            )
        )
    }

    @CsvSource(
        // Empty space
        "test Test.testmethod",
        // Parentheses
        "Te(st",
        "Te)st",
        // Mimics ellipsis in combination with the dot separator between type and method
        "..test"
    )
    @ParameterizedTest
    internal fun `maps method name with substring that resembles type but with unusual codepoints for type`(
        methodName: String
    ) {
        val entry =
            content {
                stackTrace {
                    frame {
                        declaringClass = "Test"
                        this.methodName = methodName
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exception(stack(containingExactly(methodName(`is`(methodName))))))
        )
    }

    @CsvSource("Test(Test.kt:10)", "(Test.kt:10)", "(Test.kt)")
    @ParameterizedTest
    internal fun `maps method name with substring that resembles location`(methodName: String) {
        val entry = content { stackTrace { frame { this.methodName = methodName } } }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(exception(stack(containingExactly(methodName(`is`(methodName))))))
        )
    }

    @Test
    internal fun `maps frame without class, method name, file, and line`() {
        val entry =
            content {
                stackTrace {
                    frame {
                        declaringClass = ""
                        methodName = ""
                        fileName = ""
                        line = ""
                    }
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(
                    stack(containingExactly(allOf(declaringClass(`is`("")), methodName(`is`("")))))
                )
            )
        )
    }

    @CsvSource("42", "''", "2147483647")
    @ParameterizedTest
    internal fun `ignores line number in frame`(lineNumber: String) {
        val entry = content { stackTrace { frame { line = lineNumber } } }

        assertDoesNotThrow { parseToEvents(entry) }
    }

    @CsvSource("Test.java", "(", ")", ":")
    @ParameterizedTest
    internal fun `ignores source in frame`(name: String) {
        val entry = content { stackTrace { frame { fileName = name } } }

        assertDoesNotThrow { parseToEvents(entry) }
    }

    @Test
    internal fun `maps stack from causal chain`() {
        val entry =
            content {
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
                exception(
                    allOf(
                        cause(
                            allOf(
                                stack(
                                    containingExactly(
                                        allOf(
                                            declaringClass(`is`("net.torommo.logspy.TestA")),
                                            methodName(`is`("testA"))
                                        )
                                    )
                                ),
                                cause(
                                    allOf(
                                        noCause(),
                                        stack(
                                            containingExactlyInOrder(
                                                allOf(
                                                    declaringClass(
                                                        `is`("net.torommo.logspy.TestB1")
                                                    ),
                                                    methodName(`is`("testB1"))
                                                ),
                                                allOf(
                                                    declaringClass(
                                                        `is`("net.torommo.logspy.TestB2")
                                                    ),
                                                    methodName(`is`("testB2"))
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps stack from causal chain when root cause is first`() {
        val entry =
            content {
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
                exception(
                    allOf(
                        cause(
                            allOf(
                                stack(
                                    containingExactly(
                                        allOf(
                                            declaringClass(`is`("net.torommo.logspy.TestA")),
                                            methodName(`is`("testA"))
                                        )
                                    )
                                ),
                                cause(
                                    allOf(
                                        noCause(),
                                        stack(
                                            containingExactlyInOrder(
                                                allOf(
                                                    declaringClass(
                                                        `is`("net.torommo.logspy.TestB1")
                                                    ),
                                                    methodName(`is`("testB1"))
                                                ),
                                                allOf(
                                                    declaringClass(
                                                        `is`("net.torommo.logspy.TestB2")
                                                    ),
                                                    methodName(`is`("testB2"))
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `maps stack from suppressed`() {
        val entry =
            content {
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
                exception(
                    suppressed(
                        containingExactly(
                            allOf(
                                noCause(),
                                stack(
                                    containingExactly(
                                        allOf(
                                            declaringClass(`is`("net.torommo.logspy.TestA")),
                                            methodName(`is`("testA"))
                                        )
                                    )
                                ),
                                suppressed(
                                    containingExactly(
                                        allOf(
                                            noCause(),
                                            stack(
                                                containingExactlyInOrder(
                                                    allOf(
                                                        declaringClass(
                                                            `is`("net.torommo.logspy.TestB1")
                                                        ),
                                                        methodName(`is`("testB1"))
                                                    ),
                                                    allOf(
                                                        declaringClass(
                                                            `is`("net.torommo.logspy.TestB2")
                                                        ),
                                                        methodName(`is`("testB2"))
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    internal fun `ignores omitted frames`() {
        val entry =
            content {
                stackTrace {
                    frame { declaringClass = "net.torommo.logspy.Test" }
                    omittedFrame {}
                }
            }

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                exception(stack(containingExactly(declaringClass(`is`("net.torommo.logspy.Test")))))
            )
        )
    }

    @MethodSource("mdcConfigurations")
    @ParameterizedTest
    internal fun `maps mdc`(configuration: JsonEntryBuilder.() -> Unit) {
        val entry =
            configuration.merge(
                content {
                    field("test-key-1", "test-value-1")
                    field("test-key-2", "test-value-2")
                }
            )

        val events = parseToEvents(entry)

        assertThat(
            events,
            contains(
                mdc(`is`(mapOf("test-key-1" to "test-value-1", "test-key-2" to "test-value-2")))
            )
        )
    }

    @ValueSource(strings = ["garbled\n", "\n", """{""""" + "\n"])
    @ParameterizedTest
    internal fun `ignores lines without json`(payload: String) {
        val entry1 = content {
            loggerName = "TestLogger"
            message = "Test 1"
        }.asSource()
        val entry2 = content {
            loggerName = "TestLogger"
            message = "Test 2"
        }.asSource()

        val events = JsonEventParser("TestLogger", "$entry1$payload$entry2").events()

        assertThat(events, contains(message(`is`("Test 1")), message(`is`("Test 2"))))
    }

    @ValueSource(
        strings =
            [
                // logger name missing
                """{"level": "INFO"}"""" + "\n",
                // level missing
                """"{"logger_name": "TestLogger"}""" + "\n"
            ]
    )
    @ParameterizedTest
    internal fun `ignores lines with non logstash json`(payload: String) {
        val entry1 = content {
            loggerName = "TestLogger"
            message = "Test 1"
        }.asSource()
        val entry2 = content {
            loggerName = "TestLogger"
            message = "Test 2"
        }.asSource()

        val events = JsonEventParser("TestLogger", "$entry1$payload$entry2").events()

        assertThat(events, contains(message(`is`("Test 1")), message(`is`("Test 2"))))
    }

    private fun (JsonEntryBuilder.() -> Unit).asSource(): String {
        return JsonEntryBuilder().apply(this).build()
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

        @JvmStatic
        fun incompleteConfigurations(): Iterator<Arguments> {
            val entry = content { message = "Test message" }
            val string = JsonEntryBuilder().apply(entry).build()
            return (0 until string.length).map { string.substring(0 until it) }
                .map { arguments(it) }
                .iterator()
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
        return JsonEventParser(loggerName, block.asSource()).events()
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
            val jsonObject = JsonObject()
            jsonObject.addProperty("@timestamp", "2019-10-31T20:31:17.234+01:00")
            jsonObject.addProperty("@version", "1")
            jsonObject.addProperty("message", message)
            jsonObject.addProperty("logger_name", loggerName)
            jsonObject.addProperty("thread_name", "main")
            jsonObject.addProperty("level", level)
            jsonObject.addProperty("level_value", 20000)
            stackTrace?.let { jsonObject.addProperty("stack_trace", it.build()) }
            addAdditionalFieldsTo(jsonObject)
            addMarkersTo(jsonObject)
            return GsonBuilder().create().toJson(jsonObject) + "\n"
        }

        private fun addAdditionalFieldsTo(target: JsonObject) {
            simpleAdditionalFields.forEach { target.addProperty(it.key, it.value) }
            nestedAdditionalFields.forEach {
                val nestedObject = JsonObject()
                nestedObject.addProperty("value", "test")
                target.add(it, nestedObject)
            }
        }

        private fun addMarkersTo(target: JsonObject) {
            if (!markers.isEmpty()) {
                val tags = JsonArray()
                markers.forEach { tags.add(it) }
                target.add("tags", tags)
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

        fun frameWithUnknownSource(block: UnknownSourceFrameBuilder.() -> Unit) {
            this.frames.add(UnknownSourceFrameBuilder().apply(block))
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
            val header = if (message == null) "${type}\n" else "${type}: ${message}\n"
            val stack = frames.asSequence().map { it.build(indent) }.joinToString("")
            val suppressed =
                this.suppressed
                    .asSequence()
                    .map { "${"\t".repeat(indent + 1)}Suppressed: ${it.build(indent + 1)}" }
                    .joinToString("")

            return "${cause?.buildRootCauseFirst(indent) ?: ""}${"\t".repeat(indent)}${prefix}" +
                "${header}${stack}${suppressed}"
        }

        private fun buildRootCauseLast(indent: Int, root: Boolean): String {
            val prefix = if (root) {
                ""
            } else {
                "Caused by: "
            }
            val header = if (message == null) "${type}\n" else "${type}: ${message}\n"
            val stack = frames.asSequence().map { it.build(indent) }.joinToString("")
            val suppressed =
                this.suppressed
                    .asSequence()
                    .map { "${"\t".repeat(indent + 1)}Suppressed: ${it.build(indent + 1)}" }
                    .joinToString("")

            return "${prefix}${header}${stack}${suppressed}" +
                "${cause?.buildRootCauseLast(indent, false) ?: ""}"
        }
    }

    internal interface FrameBuilder {
        fun build(indent: Int): String
    }

    @JsonEntryDsl
    internal class FilledFrameBuilder : FrameBuilder {
        var declaringClass: String = "net.torommo.logspy.Test"
        var methodName: String = "test"
        var fileName: String = "Test.java"
        var line: String = "123"

        override fun build(indent: Int): String {
            if (line.isEmpty()) {
                return "${"\t".repeat(indent + 1)}at ${declaringClass}.${methodName}(${fileName})\n"
            } else {
                return "${"\t".repeat(indent + 1)}at ${declaringClass}.${methodName}(${fileName}:" +
                    "${line})\n"
            }
        }
    }

    @JsonEntryDsl
    internal class OmittedFrameBuilder : FrameBuilder {
        override fun build(indent: Int): String {
            return "${"\t".repeat(indent + 1)}... 42 common frames ommited\n"
        }
    }

    @JsonEntryDsl
    internal class UnknownSourceFrameBuilder : FrameBuilder {
        var declaringClass: String = "net.torommo.logspy.Test"
        var methodName: String = "test"
        var line: String = "123"

        override fun build(indent: Int): String {
            return FilledFrameBuilder()
                .apply {
                    declaringClass = this@UnknownSourceFrameBuilder.declaringClass
                    methodName = this@UnknownSourceFrameBuilder.methodName
                    line = this@UnknownSourceFrameBuilder.line
                }
                .build(indent)
        }
    }
}