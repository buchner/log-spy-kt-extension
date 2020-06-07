package net.torommo.logspy

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.nextInt
import kotlin.streams.asSequence
import kotlin.text.CharCategory.DECIMAL_DIGIT_NUMBER
import kotlin.text.CharCategory.LETTER_NUMBER
import kotlin.text.CharCategory.LOWERCASE_LETTER
import kotlin.text.CharCategory.MODIFIER_LETTER
import kotlin.text.CharCategory.OTHER_LETTER
import kotlin.text.CharCategory.TITLECASE_LETTER
import kotlin.text.CharCategory.UPPERCASE_LETTER

class ParseProperty : StringSpec({

    "logger name parsability" {
        checkAll(arb(PositionIndenpendentStringShrinker(1)) { rs ->
            arbJavaIdentifier.merge(arbKotlinIdentifier).single(rs)
        }) { loggerName ->
            val logger = LoggerFactory.getLogger(loggerName)
            LogstashStdoutSpyProvider().resolve(loggerName).use {
                logger.error("Test")

                assertDoesNotThrow(it::events);
            }
        }
    }

    "log level parsability" {
        checkAll(arbLevel) { logAction ->
            val logger = LoggerFactory.getLogger("test")
            LogstashStdoutSpyProvider().resolve("test").use {
                logAction(logger, "Test")

                assertDoesNotThrow(it::events);
            }
        }
    }

    "log message parsability" {
        checkAll(arb(PositionIndenpendentStringShrinker()) { rs -> arbMessage.single(rs) }) { message ->
            val logger = LoggerFactory.getLogger("test")
            LogstashStdoutSpyProvider().resolve("test").use {
                logger.error(message)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "exception message parsability" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(arb(PositionIndenpendentStringShrinker()) { rs -> arbMessage.single(rs) }) { message ->
            LogstashStdoutSpyProvider().resolve("test").use {
                val exception = RuntimeException(message)
                exception.stackTrace = emptyArray()
                logger.error("test", exception)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "stack trace parsability" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(arb(ArrayShrinker()) { rs ->
            arbKotlinStackTraceElements.merge(arbJavaStackTraceElements).single()
        }) { element: Array<StackTraceElement> ->
            LogstashStdoutSpyProvider().resolve("test").use {
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
            LogstashStdoutSpyProvider().resolve("test").use {
                logger.error("test", exception)

                assertDoesNotThrow(it::events);
            }
        }
    }
})

fun Arb.Companion.printableAscii(): Arb<Codepoint> = arb(listOf(Codepoint('a'.toInt()))) { rs ->
    val printableChars = (' '.toInt()..'~'.toInt()).asSequence()
    val codepoints = printableChars
        .map { Codepoint(it) }
        .toList()
    val ints = Arb.int(codepoints.indices)
    ints.values(rs).map { codepoints[it.value] }
}

fun Arb.Companion.printableMultilinesIndentedAscii(): Arb<Codepoint> = arb(listOf(Codepoint('a'.toInt()))) { rs ->
    val indentings = sequenceOf(0xB)
    val endOfLines = sequenceOf(0xA, 0xD)
    val printableChars = (' '.toInt()..'~'.toInt()).asSequence()
    val codepoints = (indentings + endOfLines + printableChars)
        .map { Codepoint(it) }
        .toList()
    val ints = Arb.int(codepoints.indices)
    ints.values(rs).map { codepoints[it.value] }
}

fun Arb.Companion.unicode(): Arb<Codepoint> = arb(listOf(Codepoint('a'.toInt()))) { rs ->
    val ints = Arb.int(Character.MIN_CODE_POINT..Character.MAX_CODE_POINT)
    ints.values(rs).map { Codepoint(it.value) }
}

val arbLevel = Arb.element(
    listOf<(Logger, String) -> Unit>(
        Logger::error,
        Logger::warn,
        Logger::info,
        Logger::debug,
        Logger::trace
    )
)

val arbMessage = Arb.string(0, 1024, Arb.printableMultilinesIndentedAscii())
    .merge(Arb.string(0, 1024, Arb.unicode()))

val arbJavaIdentifier = Arb.string(minSize = 1, codepoints = Arb.printableAscii())
    .merge(Arb.string(minSize = 1, codepoints = Arb.unicode()))
    .filter { Character.isJavaIdentifierStart(it.codePoints().asSequence().first()) }
    .filter { it.codePoints().asSequence().all { codePoint -> Character.isJavaIdentifierPart(codePoint) } }

val arbJavaFileName = arbJavaIdentifier
    .map { "${it}.java" }

val arbJavaStackTraceElement = Arb.bind(
        arbJavaIdentifier,
        arbJavaIdentifier,
        arbJavaFileName,
        Arb.int(-65536, 65535)
    ) { className, methodName, fileName, lineNumber -> StackTraceElement(className, methodName, fileName, lineNumber) }

val arbJavaStackTraceElements = Arb.array(arbJavaStackTraceElement, 0..7)

val arbKotlinIdentifier = Arb.string(minSize = 1, codepoints = Arb.printableAscii())
    .merge(Arb.string(minSize = 1, codepoints = Arb.unicode()))
    .filter { isUnescapedIdentifier(it) || isEscapedIdentifier(it) }

val arbKotlinFileName = arbKotlinIdentifier
    .map { "${it}.kt" };

val arbKotlinStackTraceElement = Arb.bind(
        arbKotlinIdentifier,
        arbKotlinIdentifier,
        arbKotlinFileName,
        Arb.int(-65536, 65535)
    ) { className, methodName, fileName, lineNumber -> StackTraceElement(className, methodName, fileName, lineNumber) }

val arbKotlinStackTraceElements = Arb.array(arbKotlinStackTraceElement, 0..7)

val arbExceptionTree: Arb<Throwable> = arb { rs ->
    sequence {
        val throwableGenerator = arbFlatException.generate(rs).iterator()
        while (true) {
            val result = throwableGenerator.next().value
            repeat(rs.random.nextInt(1..7)) {
                val exceptionToPlace = throwableGenerator.next().value
                when (ExceptionCreationAction.values()[rs.random.nextInt(ExceptionCreationAction.values().size)]) {
                    ExceptionCreationAction.ADD_CAUSE -> {
                        var current: Throwable = result
                        while (current.cause != null) {
                            current = current.cause!!
                        }
                        current.initCause(exceptionToPlace)
                    }
                    ExceptionCreationAction.INSERT_SUPPRESSED -> {
                        var current: Throwable = result
                        while (current.cause != null) {
                            current = current.cause!!
                        }
                        current.addSuppressed(exceptionToPlace)
                    }
                    ExceptionCreationAction.ADD_CAUSE_TO_SUPPRESSED -> {
                        var current: Throwable = result
                        while (current.cause != null) {
                            current = current.cause!!
                        }
                        for (suppressed in current.suppressed) {
                            if (suppressed.cause == null) {
                                suppressed.initCause(exceptionToPlace)
                                break
                            }
                        }
                    }
                }
            }
            yield(result)
        }
    }
}

private enum class ExceptionCreationAction {
    ADD_CAUSE,
    INSERT_SUPPRESSED,
    ADD_CAUSE_TO_SUPPRESSED
}

val arbFlatException = Arb.bind(
    Arb.string(0, 255, Arb.printableMultilinesIndentedAscii()),
    arbKotlinStackTraceElements.merge(arbJavaStackTraceElements)
) { message, stackTrace ->
    val result: Exception = RuntimeException(message)
    result.stackTrace = stackTrace
    result
}

private fun isUnescapedIdentifier(string: String): Boolean {
    return isLetterOrUnderscore(string.first()) && string.toCharArray()
        .all { char -> isLetterOrUnderscore(char) || isUnicodeDigit(char) }
}

private fun isLetterOrUnderscore(char: Char): Boolean {
    return LOWERCASE_LETTER.contains(char) || UPPERCASE_LETTER.contains(char) ||
            TITLECASE_LETTER.contains(char) || LETTER_NUMBER.contains(char) ||
            MODIFIER_LETTER.contains(char) || OTHER_LETTER.contains(char) ||
            char == '_'
}

private fun isUnicodeDigit(char: Char): Boolean {
    return DECIMAL_DIGIT_NUMBER.contains(char)
}

private fun isEscapedIdentifier(string: String): Boolean {
    return string.all { it != '\r' && it != '\n' && it != '`' }
}
