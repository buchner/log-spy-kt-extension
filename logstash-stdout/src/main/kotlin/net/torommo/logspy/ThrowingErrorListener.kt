package net.torommo.logspy

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * An Antlr error listener that does throw an exception whenever a something could not be parsed.
 */
internal class ThrowingErrorListener(val literal: String) : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?,
    ) {
        throw AssertionError("""Could not parse output: $literal.""", e)
    }
}
