package net.torommo.logspy

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.kotest.assertions.failure
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.Row1
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.inspectors.forOne
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot

internal class JsonEventParserTest : FreeSpec() {
    init {
        "parses single event" - {
            val entry = content { message = "Test message" }

            val events = parseToEvents(entry)

            events.forOne { it.message shouldBe "Test message" }
        }

        "ignores event when from not matching logger" - {
            val entry = content { loggerName = "net.torommo.logspy.AnotherName" }

            val events = parseToEvents(entry, "net.torommo.logspy.DifferentName")

            events shouldBe emptyList()
        }

        "ignores event when incomplete" - {
            fun incompleteConfigurations(): Array<Row1<String>> {
                val entry = content { message = "Test message" }
                val string = JsonEntryBuilder().apply(entry).build()
                return (0 until string.length).map { string.substring(0 until it) }
                    .map { row(it) }
                    .toTypedArray()
            }

            forAll(*incompleteConfigurations()) { payload ->
                val events =
                    JsonEventParser("net.torommo.logspy.LogSpyExtensionIntegrationTest", payload)
                        .events()

                events shouldBe emptyList()
            }
        }

        "maps level" - {
            forAll(
                row("TRACE", Level.TRACE),
                row("DEBUG", Level.DEBUG),
                row("INFO", Level.INFO),
                row("WARN", Level.WARN),
                row("ERROR", Level.ERROR)
            ) { literal, level ->
                val entry = content { this.level = literal }

                val events = parseToEvents(entry)

                events.forOne { it.level shouldBe level }
            }
        }

        "maps exception type" - {
            forAll(
                row("net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Modules
                row(
                    "net.torommo.logspy/test@42.314/net.torommo.logspy.Test",
                    "net.torommo.logspy.Test"
                ),
                row("net.torommo.logspy//net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("net.torommo.logspy/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("test@42.314/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Kotlin specific identifiers
                row("net.torommo.logspy.My exception", "net.torommo.logspy.My exception"),
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo logspy.Exception", "net.torommo logspy.Exception"),
                // Uncommon but valid Java identifiers
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo.logspy.Δ", "net.torommo.logspy.Δ"),
                row("net.torommoΔlogspy.Test", "net.torommoΔlogspy.Test")
            ) { value, expectedType ->
                val entry = content { stackTrace { this.type = value } }

                val events = parseToEvents(entry)

                events.forSingleton { it.exception?.type shouldBe expectedType }
            }
        }

        "maps exception message" - {
            forAll(
                row("Test message", "Test message"),
                row("", ""),
                row("\ttest\tmessage", "\ttest\tmessage"),
                row("Test: message", "Test: message") // Mimics the type prefix
            ) { literal, expected ->
                val entry = content { stackTrace { message = literal } }

                val events = parseToEvents(entry)

                events.forSingleton { it.exception?.message shouldBe expected }
            }
        }

        "maps missing exception message" - {
            val entry = content { stackTrace { message = null } }

            val events = parseToEvents(entry)

            events.forSingleton { it.exception?.message shouldBe null }
        }

        "maps multiline message" - {
            val entry = content { stackTrace { message = "test\nmessage\n" } }

            val events = parseToEvents(entry)

            events.forSingleton { it.exception?.message shouldBe "test\nmessage\n" }
        }

        "maps special chars in exception message" - {
            forAll(row("#"), row("`"), row(""""""")) { value ->
                val entry = content { stackTrace { message = value } }

                val events = parseToEvents(entry)

                events.forSingleton { it.exception?.message shouldBe value }
            }
        }

        "mapping favours message over type when ambiguous" - {
            val entry =
                content {
                    stackTrace {
                        type = "java.lang.String: exception"
                        message = "Test message"
                    }
                }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception?.type shouldBe "java.lang.String"
                it.exception?.message shouldBe "exception: Test message"
            }
        }

        "mapping favours message over frames when multi line message is ambiguous" - {
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

            events.forSingleton {
                it.exception?.message shouldBe
                    "test\n\t\n\n\t\tat something.Else\n\t\nat " +
                        "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
            }
        }

        "maps exception cause" - {
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

            events.forSingleton {
                it.exception?.cause?.message shouldBe "Causing exception"
                it.exception?.cause?.cause?.message shouldBe "Causing causing exception"
            }
        }

        "maps type of cause" - {
            forAll(
                row("net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Modules
                row(
                    "net.torommo.logspy/test@42.314/net.torommo.logspy.Test",
                    "net.torommo.logspy.Test"
                ),
                row("net.torommo.logspy//net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("net.torommo.logspy/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("test@42.314/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Kotlin specific identifiers
                row("net.torommo.logspy.My exception", "net.torommo.logspy.My exception"),
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo logspy.Exception", "net.torommo logspy.Exception"),
                // Uncommon but valid Java identifiers
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo.logspy.Δ", "net.torommo.logspy.Δ"),
                row("net.torommoΔlogspy.Test", "net.torommoΔlogspy.Test")
            ) { value, expectedType ->
                val entry = content { stackTrace { cause { type = value } } }

                val events = parseToEvents(entry)

                events.forSingleton { it.exception?.cause?.type shouldBe expectedType }
            }
        }

        "maps type of cause when root cause first" - {
            forAll(
                row("net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Modules
                row(
                    "net.torommo.logspy/test@42.314/net.torommo.logspy.Test",
                    "net.torommo.logspy.Test"
                ),
                row("net.torommo.logspy//net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("net.torommo.logspy/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("test@42.314/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Kotlin specific identifiers
                row("net.torommo.logspy.My exception", "net.torommo.logspy.My exception"),
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo logspy.Exception", "net.torommo logspy.Exception"),
                // Uncommon but valid Java identifiers
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo.logspy.Δ", "net.torommo.logspy.Δ"),
                row("net.torommoΔlogspy.Test", "net.torommoΔlogspy.Test")
            ) { value, expectedType ->
                val entry = content {
                    rootCauseFirstStackTrace {
                        type = value
                        cause {}
                    }
                }

                val events = parseToEvents(entry)

                events.forSingleton { it.exception?.type shouldBe expectedType }
            }
        }

        "mapping favours message from cause over type when ambiguous" - {
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

            events.forSingleton {
                it.exception?.cause?.type shouldBe "java.lang.String"
                it.exception?.cause?.message shouldBe "exception: Test message"
            }
        }

        "mapping favours message from cause over type when ambiguous and root cause first" - {
            val entry =
                content {
                    rootCauseFirstStackTrace {
                        type = "java.lang.String: exception"
                        message = "Test message"
                        cause {}
                    }
                }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception?.type shouldBe "java.lang.String"
                it.exception?.message shouldBe "exception: Test message"
            }
        }

        "mapping favours message from cause over frames when multiline message is ambiguous" - {
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

            events.forSingleton {
                it.exception?.cause?.message shouldBe
                    "Test\n\t\n\n\t\tat something else\n\t\nat " +
                        "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
            }
        }

        "mapping favours message from cause over frames when multiline message is ambiguous and " +
            "root cause first" - {
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

                events.forSingleton {
                    it.exception?.message shouldBe
                        "Test\n\t\n\n\t\tat something else\n\t\nat " +
                            "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
                }
            }

        "maps suppressed exceptions" - {
            val entry =
                content {
                    stackTrace {
                        suppressed {
                            message = "First suppressed exception 1"
                            suppressed { this.message = "Suppressed suppressed exception" }
                        }
                        suppressed { this.message = "Second suppressed exception" }
                    }
                }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forOne { suppressed ->
                        suppressed.message shouldBe "First suppressed exception 1"
                        suppressed.suppressed
                            .forSingleton { suppressedSuppressed ->
                                suppressedSuppressed.message shouldBe
                                    "Suppressed suppressed exception"
                            }
                    }
                it.exception
                    ?.suppressed
                    .forOne { suppressed ->
                        suppressed.message shouldBe "Second suppressed exception"
                    }
            }
        }

        "maps type from suppressed" - {
            forAll(
                row("net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Modules
                row(
                    "net.torommo.logspy/test@42.314/net.torommo.logspy.Test",
                    "net.torommo.logspy.Test"
                ),
                row("net.torommo.logspy//net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("net.torommo.logspy/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("test@42.314/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Kotlin specific identifiers
                row("net.torommo.logspy.My exception", "net.torommo.logspy.My exception"),
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo logspy.Exception", "net.torommo logspy.Exception"),
                // Uncommon but valid Java identifiers
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo.logspy.Δ", "net.torommo.logspy.Δ"),
                row("net.torommoΔlogspy.Test", "net.torommoΔlogspy.Test")
            ) { literal, expected ->
                val entry = content { stackTrace { suppressed { this.type = literal } } }

                val events = parseToEvents(entry)

                events.forSingleton {
                    it.exception
                        ?.suppressed
                        .forSingleton { suppressed -> suppressed.type shouldBe expected }
                }
            }
        }

        "maps message from suppressed exceptions" - {
            forAll(
                row("Test message", "Test message"),
                row("\ttest\tmessage", "\ttest\tmessage"),
                row("Test: message", "Test: message") // Mimics the type prefix
            ) { literal, expected ->
                val entry = content { stackTrace { suppressed { this.message = literal } } }

                val events = parseToEvents(entry)

                events.forSingleton {
                    it.exception
                        ?.suppressed
                        .forSingleton { suppressed -> suppressed.message shouldBe expected }
                }
            }
        }

        "maps missing message from suppressed exceptions" - {
            val entry = content { stackTrace { suppressed { this.message = null } } }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forSingleton { suppressed -> suppressed.message shouldBe null }
            }
        }

        "mapping favours message over type in suppressed when ambiguous" - {
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

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forSingleton { suppressed ->
                        suppressed.type shouldBe "java.lang.String"
                        suppressed.message shouldBe "exception: Test message"
                    }
            }
        }

        "maps multiline message in suppressed" - {
            val entry = content { stackTrace { suppressed { message = "test\nmessage\n" } } }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forSingleton { suppressed -> suppressed.message shouldBe "test\nmessage\n" }
            }
        }

        "mapping favours message over frames in suppressed when multi line message is ambiguous" - {
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

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forSingleton { suppresed ->
                        suppresed.message shouldBe
                            "test\n\t\nat something.Else\n\t\n\n\t\tat " +
                                "net.torommo.logspy.Anything.toDo(Anything.kt:23)\n\t\n\t\t\n"
                    }
            }
        }

        "maps stack from exception" - {
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

            events.forSingleton {
                it.exception?.cause shouldBe null
                it.exception?.stackTrace shouldContainExactly
                    listOf(
                        StackTraceElementSnapshot("net.torommo.logspy.TestA1", "testA1"),
                        StackTraceElementSnapshot("net.torommo.logspy.TestA2", "testA2")
                    )
            }
        }

        "maps type in stack frame from exception" - {
            forAll(
                row("net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Modules
                row(
                    "net.torommo.logspy/test@42.314/net.torommo.logspy.Test",
                    "net.torommo.logspy.Test"
                ),
                row("net.torommo.logspy//net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("net.torommo.logspy/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                row("test@42.314/net.torommo.logspy.Test", "net.torommo.logspy.Test"),
                // Uncommon but valid Java identifiers
                row("net.torommo.logspy.exception", "net.torommo.logspy.exception"),
                row("net.torommo.logspy.Δ", "net.torommo.logspy.Δ"),
                row("net.torommoΔlogspy.Test", "net.torommoΔlogspy.Test")
            ) { value, expected ->
                val entry = content { stackTrace { frame { declaringClass = value } } }

                val events = parseToEvents(entry)

                events.forSingleton {
                    it.exception
                        ?.stackTrace
                        .forSingleton { stackTrace -> stackTrace.declaringClass shouldBe expected }
                }
            }
        }

        "mapping favours method name over type in stack when ambiguous" - {
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

            events.forSingleton {
                it.exception?.stackTrace shouldContainExactly
                    listOf(StackTraceElementSnapshot("net.torommo.logspy", "This is.a test"))
            }
        }

        "maps method name with substring that resembles type but with unusual codepoints for type" -
            {
                forAll(
                    // Empty space
                    row("test Test.testmethod"),
                    // Parentheses
                    row("Te(st"),
                    row("Te)st"),
                    // Mimics ellipsis in combination with the dot separator between type and method
                    row("..test")
                ) { methodName ->
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

                    events.forSingleton {
                        it.exception
                            ?.stackTrace
                            .forSingleton { stackTrace ->
                                stackTrace.methodName shouldBe methodName
                            }
                    }
                }
            }

        "maps method name with substring that resembles location" - {
            forAll(row("Test(Test.kt:10)"), row("(Test.kt:10)"), row("(Test.kt)")) { methodName ->
                val entry = content { stackTrace { frame { this.methodName = methodName } } }

                val events = parseToEvents(entry)

                events.forSingleton {
                    it.exception
                        ?.stackTrace
                        .forSingleton { stackTrace -> stackTrace.methodName shouldBe methodName }
                }
            }
        }

        "maps frame without class, method name, file, and line" - {
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

            events.forSingleton {
                it.exception?.stackTrace shouldContainExactly
                    listOf(StackTraceElementSnapshot("", ""))
            }
        }

        "ignores line number in frame" - {
            forAll(row("42"), row(""), row("2147483647")) { lineNumber ->
                val entry = content { stackTrace { frame { line = lineNumber } } }

                parseToEvents(entry).shouldNotBeEmpty()
            }
        }

        "ignores source in frame" - {
            forAll(row("Test.java"), row("("), row(")"), row(":")) { name ->
                val entry = content { stackTrace { frame { fileName = name } } }

                parseToEvents(entry).shouldNotBeEmpty()
            }
        }

        "maps stack from causal chain" - {
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

            events.forSingleton {
                it.exception?.cause?.stackTrace shouldContainExactly
                    listOf(StackTraceElementSnapshot("net.torommo.logspy.TestA", "testA"))
                it.exception?.cause?.cause?.cause shouldBe null
                it.exception?.cause?.cause?.stackTrace shouldContainExactly
                    listOf(
                        StackTraceElementSnapshot("net.torommo.logspy.TestB1", "testB1"),
                        StackTraceElementSnapshot("net.torommo.logspy.TestB2", "testB2")
                    )
            }
        }

        "maps stack from causal chain when root cause is first" - {
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

            events.forSingleton {
                it.exception?.cause?.stackTrace shouldContainExactly
                    listOf(StackTraceElementSnapshot("net.torommo.logspy.TestA", "testA"))
                it.exception?.cause?.cause?.cause shouldBe null
                it.exception?.cause?.cause?.stackTrace shouldContainExactly
                    listOf(
                        StackTraceElementSnapshot("net.torommo.logspy.TestB1", "testB1"),
                        StackTraceElementSnapshot("net.torommo.logspy.TestB2", "testB2")
                    )
            }
        }

        "maps stack from suppressed" - {
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

            events.forSingleton {
                it.exception
                    ?.suppressed
                    .forSingleton { suppressed ->
                        suppressed.cause shouldBe null
                        suppressed.stackTrace shouldContainExactly
                            listOf(StackTraceElementSnapshot("net.torommo.logspy.TestA", "testA"))
                        suppressed.suppressed
                            .forSingleton { suppressedSuppressed ->
                                suppressedSuppressed.cause shouldBe null
                                suppressedSuppressed.stackTrace shouldContainExactly
                                    listOf(
                                        StackTraceElementSnapshot(
                                            "net.torommo.logspy.TestB1",
                                            "testB1"
                                        ),
                                        StackTraceElementSnapshot(
                                            "net.torommo.logspy.TestB2",
                                            "testB2"
                                        )
                                    )
                            }
                    }
            }
        }

        "ignores omitted frames" - {
            val entry =
                content {
                    stackTrace {
                        frame { declaringClass = "net.torommo.logspy.Test" }
                        omittedFrame {}
                    }
                }

            val events = parseToEvents(entry)

            events.forSingleton {
                it.exception
                    ?.stackTrace
                    .forSingleton { stackTrace ->
                        stackTrace.declaringClass shouldBe "net.torommo.logspy.Test"
                    }
            }
        }

        "maps mdc" - {
            forAll(
                row(content {}),
                row(content { stackTrace {} }),
                row(content { complexField("test") }),
                row(content { marker("testMarker") })
            ) { configuration ->
                val entry =
                    configuration.merge(
                        content {
                            field("test-key-1", "test-value-1")
                            field("test-key-2", "test-value-2")
                        }
                    )

                val events = parseToEvents(entry)

                events.forSingleton {
                    it.mdc shouldContainExactly
                        mapOf("test-key-1" to "test-value-1", "test-key-2" to "test-value-2")
                }
            }
        }

        "ignores lines without json" - {
            forAll(row("garbled\n"), row("\n"), row("""{""""" + "\n")) { payload ->
                val entry1 = content {
                    loggerName = "TestLogger"
                    message = "Test 1"
                }.asSource()
                val entry2 = content {
                    loggerName = "TestLogger"
                    message = "Test 2"
                }.asSource()

                val events = JsonEventParser("TestLogger", "$entry1$payload$entry2").events()

                events.forOne { it.message shouldBe "Test 1" }
                events.forOne { it.message shouldBe "Test 2" }
            }
        }

        "ignores lines with non logstash json" - {
            forAll(
                // logger name missing
                row("""{"level": "INFO"}"""" + "\n"),
                // level missing
                row(""""{"logger_name": "TestLogger"}""" + "\n")
            ) { payload ->
                val entry1 = content {
                    loggerName = "TestLogger"
                    message = "Test 1"
                }.asSource()
                val entry2 = content {
                    loggerName = "TestLogger"
                    message = "Test 2"
                }.asSource()

                val events = JsonEventParser("TestLogger", "$entry1$payload$entry2").events()

                events.forOne { it.message shouldBe "Test 1" }
                events.forOne { it.message shouldBe "Test 2" }
            }
        }
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

infix fun <T : Collection<U>?, U> T.forOne(fn: (U) -> Unit) {
    when {
        this == null -> {
            throw failure("collection was null")
        }
        else -> {
            this.forOne(fn)
        }
    }
}

infix fun <T : Collection<U>?, U> T.forSingleton(fn: (U) -> Unit) {
    when {
        this == null -> {
            throw failure("collection was null")
        }
        this.size > 1 -> {
            throw failure("collection contained more than one element")
        }
        else -> {
            this.forOne(fn)
        }
    }
}