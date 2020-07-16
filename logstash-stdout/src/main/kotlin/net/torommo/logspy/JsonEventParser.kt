package net.torommo.logspy

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

class JsonEventParser(val loggerName: String, val source: String) {
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
