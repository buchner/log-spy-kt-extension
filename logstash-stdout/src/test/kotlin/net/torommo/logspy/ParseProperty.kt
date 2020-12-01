package net.torommo.logspy

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.streams.asSequence
import kotlin.text.CharCategory.DECIMAL_DIGIT_NUMBER
import kotlin.text.CharCategory.LETTER_NUMBER
import kotlin.text.CharCategory.LOWERCASE_LETTER
import kotlin.text.CharCategory.MODIFIER_LETTER
import kotlin.text.CharCategory.OTHER_LETTER
import kotlin.text.CharCategory.TITLECASE_LETTER
import kotlin.text.CharCategory.UPPERCASE_LETTER
import net.torommo.logspy.ExceptionCreationAction.ADD_SUPPRESSED_TO_ROOT_EXCEPTION
import net.torommo.logspy.ExceptionCreationAction.RANDOM_ACTION_ON_RANDOM_SUPPRESSED_IN_ROOT_EXCEPTION
import net.torommo.logspy.ExceptionCreationAction.SET_NEW_ROOT_EXCEPTION
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ParseProperty :
    StringSpec(
        {
            "logger name parsability" {
                checkAll(
                    arbitrary(PositionIndenpendentStringShrinker(1)) { rs ->
                        arbJavaIdentifier.merge(arbKotlinIdentifier).single(rs)
                    }
                ) { loggerName ->
                    val logger = LoggerFactory.getLogger(loggerName)
                    LogstashStdoutSpyProvider().createFor(loggerName)
                        .use {
                            logger.error("Test")

                            assertDoesNotThrow(it::events);
                        }
                }
            }

            "log level parsability" {
                checkAll(arbLevel) { logAction ->
                    val logger = LoggerFactory.getLogger("test")
                    LogstashStdoutSpyProvider().createFor("test")
                        .use {
                            logAction(logger, "Test")

                            assertDoesNotThrow(it::events);
                        }
                }
            }

            "log message parsability" {
                checkAll(
                    arbitrary(PositionIndenpendentStringShrinker()) { rs -> arbMessage.single(rs) }
                ) { message ->
                    val logger = LoggerFactory.getLogger("test")
                    LogstashStdoutSpyProvider().createFor("test")
                        .use {
                            logger.error(message)

                            assertDoesNotThrow(it::events);
                        }
                }
            }

            "exception message parsability" {
                val logger = LoggerFactory.getLogger("test")
                checkAll(
                    arbitrary(PositionIndenpendentStringShrinker()) { rs -> arbMessage.single(rs) }
                ) { message ->
                    LogstashStdoutSpyProvider().createFor("test")
                        .use {
                            val exception = RuntimeException(message)
                            exception.stackTrace = emptyArray()
                            logger.error("test", exception)

                            assertDoesNotThrow(it::events);
                        }
                }
            }

            "stack trace parsability" {
                val logger = LoggerFactory.getLogger("test")
                checkAll(
                    arbitrary(ArrayShrinker()) { rs ->
                        arbKotlinStackTraceElements.merge(arbJavaStackTraceElements).single(rs)
                    }
                ) { element: Array<StackTraceElement> ->
                    LogstashStdoutSpyProvider().createFor("test")
                        .use {
                            val exception = RuntimeException("test message")
                            exception.stackTrace = element
                            logger.error("test", exception)

                            assertDoesNotThrow(it::events);
                        }
                }
            }

            "exception tree parsability" {
                val logger = LoggerFactory.getLogger("test")
                checkAll(arbExceptionTree) { exception: Throwable ->
                    LogstashStdoutSpyProvider().createFor("test")
                        .use {
                            logger.error("test", exception)

                            assertDoesNotThrow(it::events);
                        }
                }
            }
        }
    )

fun Arb.Companion.printableAscii(): Arb<Codepoint> =
    arbitrary(listOf(Codepoint('a'.toInt()))) { rs ->
        val printableChars = (' '.toInt()..'~'.toInt()).asSequence()
        val codepoints = printableChars.map { Codepoint(it) }.toList()
        val ints = Arb.int(codepoints.indices)
        codepoints[ints.sample(rs).value]
    }

fun Arb.Companion.printableMultilinesIndentedAscii(): Arb<Codepoint> =
    arbitrary(listOf(Codepoint('a'.toInt()))) { rs ->
        val indentings = sequenceOf(0xB)
        val endOfLines = sequenceOf(0xA, 0xD)
        val printableChars = (' '.toInt()..'~'.toInt()).asSequence()
        val codepoints = (indentings + endOfLines + printableChars).map { Codepoint(it) }.toList()
        val ints = Arb.int(codepoints.indices)
        codepoints[ints.sample(rs).value]
    }

fun Arb.Companion.unicode(): Arb<Codepoint> =
    arbitrary(listOf(Codepoint('a'.toInt()))) { rs ->
        val ints = Arb.int(Character.MIN_CODE_POINT..Character.MAX_CODE_POINT)
        Codepoint(ints.sample(rs).value)
    }

val arbLevel =
    Arb.element(
        listOf<(Logger, String) -> Unit>(
            Logger::error,
            Logger::warn,
            Logger::info,
            Logger::debug,
            Logger::trace
        )
    )

val arbMessage =
    Arb.string(0, 1024, Arb.printableMultilinesIndentedAscii())
        .merge(Arb.string(0, 1024, Arb.unicode()))

val arbJavaIdentifier =
    Arb.string(minSize = 1, codepoints = Arb.printableAscii())
        .merge(Arb.string(minSize = 1, codepoints = Arb.unicode()))
        .filter { Character.isJavaIdentifierStart(it.codePoints().asSequence().first()) }
        .filter {
            it.codePoints()
                .asSequence()
                .all { codePoint -> Character.isJavaIdentifierPart(codePoint) }
        }

val arbJavaFileName = arbJavaIdentifier.map { "${it}.java" }

val arbJavaStackTraceElement =
    Arb.bind(arbJavaIdentifier, arbJavaIdentifier, arbJavaFileName, Arb.int(-65536, 65535))
        { className, methodName, fileName, lineNumber ->
            StackTraceElement(className, methodName, fileName, lineNumber)
        }

val arbJavaStackTraceElements = Arb.array(arbJavaStackTraceElement, 0..7)

val arbKotlinIdentifier =
    Arb.string(minSize = 1, codepoints = Arb.printableAscii())
        .merge(Arb.string(minSize = 1, codepoints = Arb.unicode()))
        .filter { isUnescapedIdentifier(it) || isEscapedIdentifier(it) }

val arbKotlinFileName = arbKotlinIdentifier.map { "${it}.kt" };

val arbKotlinStackTraceElement =
    Arb.bind(arbKotlinIdentifier, arbKotlinIdentifier, arbKotlinFileName, Arb.int(-65536, 65535))
        { className, methodName, fileName, lineNumber ->
            StackTraceElement(className, methodName, fileName, lineNumber)
        }

val arbKotlinStackTraceElements = Arb.array(arbKotlinStackTraceElement, 0..7)

val arbExceptionTree: Arb<Throwable> =
    arbitrary { rs ->
        val throwableGenerator = arbFlatException.generate(rs).iterator()
        val result = throwableGenerator.next().value
        repeat(rs.random.nextInt(1..7)) {
            val exceptionToPlace = throwableGenerator.next().value
            addExceptionToRandomPlace(rs.random, result, exceptionToPlace)
        }
        result
    }

private fun addExceptionToRandomPlace(random: Random, aggregator: Throwable, addition: Throwable) {
    when (ExceptionCreationAction.values()[random.nextInt(ExceptionCreationAction.values().size)]) {
        SET_NEW_ROOT_EXCEPTION -> {
            rootCauseOf(aggregator).initCause(addition)
        }
        ADD_SUPPRESSED_TO_ROOT_EXCEPTION -> {
            rootCauseOf(aggregator).addSuppressed(addition)
        }
        RANDOM_ACTION_ON_RANDOM_SUPPRESSED_IN_ROOT_EXCEPTION -> {
            val rootCause = rootCauseOf(aggregator)
            if (rootCause.suppressed.isNotEmpty()) {
                addExceptionToRandomPlace(
                    random,
                    rootCause.suppressed[random.nextInt(rootCause.suppressed.size)],
                    addition
                )
            }
        }
    }
}

private tailrec fun rootCauseOf(candidate: Throwable): Throwable {
    return if (candidate.cause == null) {
        candidate
    } else {
        rootCauseOf(candidate.cause!!)
    }
}

private enum class ExceptionCreationAction {
    SET_NEW_ROOT_EXCEPTION,
    ADD_SUPPRESSED_TO_ROOT_EXCEPTION,
    RANDOM_ACTION_ON_RANDOM_SUPPRESSED_IN_ROOT_EXCEPTION
}

val arbFlatException =
    Arb.bind(
        Arb.string(0, 255, Arb.printableMultilinesIndentedAscii()),
        arbKotlinStackTraceElements.merge(arbJavaStackTraceElements)
    ) { message, stackTrace ->
        val result: Exception = RuntimeException(message)
        result.stackTrace = stackTrace
        result
    }

private fun isUnescapedIdentifier(string: String): Boolean {
    return isLetterOrUnderscore(string.first()) &&
        string.toCharArray().all { char -> isLetterOrUnderscore(char) || isUnicodeDigit(char) }
}

private fun isLetterOrUnderscore(char: Char): Boolean {
    return LOWERCASE_LETTER.contains(char) || UPPERCASE_LETTER.contains(char) ||
        TITLECASE_LETTER.contains(char) || LETTER_NUMBER.contains(char) ||
        MODIFIER_LETTER.contains(char) || OTHER_LETTER.contains(char) || char == '_'
}

private fun isUnicodeDigit(char: Char): Boolean {
    return DECIMAL_DIGIT_NUMBER.contains(char)
}

private fun isEscapedIdentifier(string: String): Boolean {
    return string.all { it != '\r' && it != '\n' && it != '`' }
}
