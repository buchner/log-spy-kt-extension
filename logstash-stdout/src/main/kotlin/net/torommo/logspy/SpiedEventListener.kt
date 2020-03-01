package net.torommo.logspy

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.io.StringReader

class SpiedEventListener(val loggerName: String) : LogstashStdoutBaseListener() {
    private val STANDARD_FIELD_NAMES =
        listOf(
            "logger_name",
            "message",
            "level",
            "stack_trace",
            "@timestamp",
            "@version",
            "thread_name",
            "level_value",
            "markers"
        )
    var events = mutableListOf<SpiedEvent>()

    override fun enterStdout(ctx: LogstashStdoutParser.StdoutContext?) {
        events = mutableListOf()
    }

    override fun exitJson(ctx: LogstashStdoutParser.JsonContext?) {
        if (ctx != null) {
            toSpiedEvent(ctx.text)?.let { events.add(it) }
        }
    }

    private fun toSpiedEvent(literal: String): SpiedEvent? {
        val parser = jsonParser(literal)
        if (!parser.isJsonObject) {
            throw AssertionError("Unparsable event.")
        } else {
            var currentLoggerName: String? = null
            var message: String? = null
            var level: SpiedEvent.Level? = null
            var stackTrace: SpiedEvent.ThrowableSnapshot? = null
            val mdc: MutableMap<String, String> = mutableMapOf()
            parser.asJsonObject.entrySet().forEach { (key, value) ->
                when (key) {
                    "logger_name" -> currentLoggerName = value.asString
                    "message" -> message = value.asString
                    "level" -> level = toLevel(value.asString)
                    "stack_trace" -> stackTrace = toException(value.asString)
                    !in STANDARD_FIELD_NAMES -> if (!value.isJsonNull && value.isJsonPrimitive) mdc.put(
                        key,
                        value.asString
                    )
                }
            }
            if (currentLoggerName == loggerName) {
                return SpiedEvent(
                    level = level!!,
                    message = message,
                    exception = stackTrace,
                    mdc = mdc
                )
            } else {
                return null
            }
        }
    }

    private fun jsonParser(literal: String): JsonElement {
        return try {
            JsonParser.parseString(literal)
        } catch (exception: JsonParseException) {
            throw AssertionError("Unparsable event.")
        }
    }

    private fun toLevel(literal: String): SpiedEvent.Level {
        return when (literal) {
            "TRACE" -> SpiedEvent.Level.TRACE
            "DEBUG" -> SpiedEvent.Level.DEBUG
            "INFO" -> SpiedEvent.Level.INFO
            "WARN" -> SpiedEvent.Level.WARN
            "ERROR" -> SpiedEvent.Level.ERROR
            else -> throw IllegalArgumentException(literal)
        }
    }

    private fun toException(literal: String): SpiedEvent.ThrowableSnapshot {
        val listener = ThrowableSnapshotStacktraceListener()
        val walker = ParseTreeWalker()
        walker.walk(listener, stacktraceParser(literal).start())
        return listener.stackTrace!!
    }

    private fun stacktraceParser(literal: String): StacktraceParser {
        val lexer = StacktraceLexer(
            CharStreams.fromReader(
                DetendReader(
                    StringReader(literal)
                )
            )
        )
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener(literal))
        val tokens = CommonTokenStream(lexer)
        val result = StacktraceParser(tokens)
        result.removeErrorListeners()
        result.addErrorListener(ThrowingErrorListener(literal))
        return result
    }
}