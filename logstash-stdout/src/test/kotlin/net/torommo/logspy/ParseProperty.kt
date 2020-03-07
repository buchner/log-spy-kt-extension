package net.torommo.logspy

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.checkAll
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
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
        checkAll(Arb.codepointCentricString(1, 255)) { loggerName ->
            val logger = LoggerFactory.getLogger(loggerName)
            LogstashStdoutSpyProvider().resolve(loggerName).use {
                logger.error("Test")

                assertDoesNotThrow(it::events);
            }
        }
    }

    "log message parsability" {
        checkAll(Arb.codepointCentricString(0, 255)) { message ->
            val logger = LoggerFactory.getLogger("test")
            LogstashStdoutSpyProvider().resolve("test").use {
                logger.error(message)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "exception message parsability" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(Arb.codepointCentricString(0, 255)) { message ->
            LogstashStdoutSpyProvider().resolve("test").use {
                val exception = RuntimeException(message)
                exception.stackTrace = emptyArray()
                logger.error("test", exception)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "cause parsablility" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(arbFlatException) { cause: Exception ->
            LogstashStdoutSpyProvider().resolve("test").use {
                val rootException = RuntimeException("test", cause)
                rootException.stackTrace = emptyArray()
                logger.error("test", rootException)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "stack trace parsability" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(arbKotlinStackTraceElements.merge(arbJavaStackTraceElements)) { element: Array<StackTraceElement> ->
            LogstashStdoutSpyProvider().resolve("test").use {
                val exception = RuntimeException("test message")
                exception.stackTrace = element
                logger.error("test", exception)

                assertDoesNotThrow(it::events);
            }
        }
    }

    "suppressed parsability" {
        val logger = LoggerFactory.getLogger("test")
        checkAll(arbExceptions) { suppressed: Array<Exception> ->
            LogstashStdoutSpyProvider().resolve("test").use {
                val exception = RuntimeException("test message")
                exception.stackTrace = emptyArray()
                suppressed.forEach { suppressedEntry -> exception.addSuppressed(suppressedEntry) }
                logger.error("test", exception)

                assertDoesNotThrow(it::events);
            }
        }
    }
})

val arbJavaIdentifier = Arb.codepointCentricString(minSize = 1)
    .filter { Character.isJavaIdentifierStart(it.codePoints().asSequence().first()) }
    .filter { it.codePoints().asSequence().all { codePoint -> Character.isJavaIdentifierPart(codePoint) } }

val arbJavaFileName = arbJavaIdentifier
    .map { "${it}.java" }

val arbJavaStackTraceElement = Arb.bindWithShrinks(
        arbJavaIdentifier,
        arbJavaIdentifier,
        arbJavaFileName,
        Arb.int(-65536, 65535)
    ) { className, methodName, fileName, lineNumber -> StackTraceElement(className, methodName, fileName, lineNumber) }

val arbJavaStackTraceElements = Arb.array(0, 7, arbJavaStackTraceElement)

val arbKotlinIdentifier = Arb.codepointCentricString(minSize = 1)
    .filter { isUnescapedIdentifier(it) || isEscapedIdentifier(it) }

val arbKotlinFileName = arbKotlinIdentifier
    .map { "${it}.kt" };

val arbKotlinStackTraceElement = Arb.bindWithShrinks(
        arbKotlinIdentifier,
        arbKotlinIdentifier,
        arbKotlinFileName,
        Arb.int(-65536, 65535)
    ) { className, methodName, fileName, lineNumber -> StackTraceElement(className, methodName, fileName, lineNumber) }

val arbKotlinStackTraceElements = Arb.array(0, 7, arbKotlinStackTraceElement)

val arbFlatException = Arb.bindWithShrinks(
    Arb.codepointCentricString(0, 255),
    arbKotlinStackTraceElements.merge(arbJavaStackTraceElements)
) { message, stackTrace ->
    val result: Exception = RuntimeException(message)
    result.stackTrace = stackTrace
    result
}

val arbExceptions = Arb.array(1, 5, arbFlatException)

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

