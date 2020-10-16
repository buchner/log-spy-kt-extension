package net.torommo.logspy

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

/**
 * Parser that converts a json input with the Logstash format for a logger into [SpiedEvent]s.
 *
 * The parsed events can be retrieved by calling [events].
 *
 * @param loggerName The name of the logger
 * @param source The json input
 */
internal class JsonEventParser(private val loggerName: String, private val source: String) {
    fun events(): List<SpiedEvent> {
        val listener = SpiedEventListener(loggerName)
        val walker = ParseTreeWalker()
        walker.walk(listener, parser(source).stdout())
        return listener.events
    }

    private fun parser(literal: String): LogstashStdoutParser {
        val lexer = LogstashStdoutLexer(CharStreams.fromString(literal))
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener(""))
        val tokens = CommonTokenStream(lexer)
        val result = LogstashStdoutParser(tokens)
        result.removeErrorListeners()
        result.addErrorListener(ThrowingErrorListener(literal))
        return result
    }
}
