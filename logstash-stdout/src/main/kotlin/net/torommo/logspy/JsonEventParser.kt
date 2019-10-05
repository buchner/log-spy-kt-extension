package net.torommo.logspy

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.torommo.logspy.SpiedEvent.Level
import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot

class JsonEventParser(val loggerName: String, val source: String) {
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

    fun events(): List<SpiedEvent> {
        return source.splitToSequence("\n")
            .filterNot { it.isBlank() }
            .map { JsonParser.parseString(it).asJsonObject }
            .filter { it["logger_name"]?.asString == loggerName}
            .map(this::toSpiedEvent)
            .toList()
    }

    private fun toSpiedEvent(json: JsonObject): SpiedEvent {
        val message = json["message"].asString
        val level = toLevel(json["level"].asString)
        val exception = json["stack_trace"]?.asString?.let { toException(it) }
        val mdc = json.keySet()
            .filterNot { STANDARD_FIELD_NAMES.contains(it) }
            .filter { json[it].isJsonPrimitive }
            .map { it to json[it].asString }
            .toMap()

        return SpiedEvent(message, level, exception, mdc)
    }

    private fun toLevel(literal: String): Level {
        return when (literal) {
            "TRACE" -> TRACE
            "DEBUG" -> DEBUG
            "INFO" -> INFO
            "WARN" -> WARN
            "ERROR" -> ERROR
            else -> throw IllegalArgumentException(literal)
        }
    }

    private fun toException(literal: String): ThrowableSnapshot {
        return toException(literal.splitToSequence("\n"))
    }

    private fun toException(sequence: Sequence<String>): ThrowableSnapshot {
        val header = sequence.first().splitToSequence(": ")
        val type = header.first()
        val message = if (header.last() == "null") null else header.last()

        return ThrowableSnapshot(type, message, toCause(sequence), toSuppressed(sequence), toStack(sequence))
    }

    private fun toCause(sequence: Sequence<String>, depth: Int = 0): ThrowableSnapshot? {
        val sequenceFromStartTag = sequence.dropWhile { !it.startsWith(indented(depth, "Caused by: ")) }
            .asSequence()
        if (sequenceFromStartTag.count() == 0) {
            return null;
        }
        val header = sequenceFromStartTag.first().splitToSequence(": ")
        val type = header.drop(1).first()
        val message = if (header.last() == "null") null else header.last()

        val remainingCauses = sequenceFromStartTag.drop(1)
            .dropWhile { !it.startsWith(indented(depth, "Caused by: ")) }
            .asSequence()

        return ThrowableSnapshot(
            type,
            message,
            toCause(remainingCauses),
            toSuppressed(sequenceFromStartTag, depth),
            toStack(sequenceFromStartTag, depth)
        )
    }

    private fun toSuppressed(sequence: Sequence<String>, depth: Int = 0): List<ThrowableSnapshot> {
        val sequenceFromStartTag = sequence.dropWhile { !it.startsWith(indented(depth + 1, "Suppressed: ")) }
            .asSequence()
        if (sequenceFromStartTag.count() == 0) {
            return emptyList();
        }
        val header = sequenceFromStartTag.first().splitToSequence(": ")
        val type = header.drop(1).first()
        val message = if (header.last() == "null") null else header.last()
        val remainingSuppressed = sequenceFromStartTag.drop(1)
            .dropWhile { !it.startsWith(indented(depth + 1, "Suppressed: ")) }
            .asSequence()
        val result = mutableListOf(
            ThrowableSnapshot(
                type,
                message,
                toCause(sequenceFromStartTag, depth + 1),
                toSuppressed(sequenceFromStartTag, depth + 1),
                toStack(sequenceFromStartTag, depth + 1)
            )
        )
        result.addAll(toSuppressed(remainingSuppressed, depth))
        return result.asSequence().toList()
    }

    private fun toStack(sequence: Sequence<String>, depth: Int = 0): List<StackTraceElementSnapshot> {
        val sequenceFromStartTag = sequence.dropWhile { !it.startsWith(indented(depth + 1, "at ")) }
            .asSequence()
        return sequenceFromStartTag.takeWhile { it.startsWith(indented(depth + 1, "at ")) }
            .map { toStack(it, depth) }
            .toList()
    }

    private fun toStack(string: String, depth: Int = 0): StackTraceElementSnapshot {
        val nameParts = string.asSequence()
            .drop(indented(depth + 1, "at ").length)
            .takeWhile { it != '(' }
            .joinToString("")
            .splitToSequence(".")

        return StackTraceElementSnapshot(
            nameParts.take(nameParts.count() - 1).joinToString("."),
            nameParts.last()
        )
    }

    private fun indented(depth: Int, text: String): String {
        return "${"\t".repeat(depth)}$text"
    }
}
