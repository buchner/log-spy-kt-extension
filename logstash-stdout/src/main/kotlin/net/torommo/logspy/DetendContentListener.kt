package net.torommo.logspy

import kotlin.streams.asSequence

/**
 * Prepares a stack trace for parsing by adding sequences to make the parsing easier.
 *
 * The stack trace format is hard to parse because it uses indentations to form blocks. This
 * listeners prepares the stack trace by replacing the indentations with opening and closing block
 * sequences. The following block sequences are used.
 *  - `\n\t\t` to open a new block and
 *  - `\t\t\n` for to close a block. In addition the sequence `\n\t\n` is used to mark the end of an
 *    entry. This makes it easier to tell the end of entries apart from new lines in text e.g. an
 *    error message. Because tabs and newlines are used in the special sequences any other
 *    occurrence of them will be escaped. The following escape sequences are used.
 *  - `\n\n\n` to escape one occurrence of `\n` and
 *  - `\t\t\t` to escape one occurrence of `\t`.
 */
class DetendContentListener : DetendBaseListener() {
    private val newlineCodepoint = '\n'.toInt()
    private val tabCodepoint = '\t'.toInt()
    private val indentSequence = sequenceOf('\n', '\t', '\t').map { it.toInt() }
    private val dedentSequence = sequenceOf('\t', '\t', '\n').map { it.toInt() }
    private val endEntrySequence = sequenceOf('\n', '\t', '\n').map { it.toInt() }
    private val newlineEscapeSequence = sequenceOf('\n', '\n', '\n').map { it.toInt() }
    private val tabEscapeSequence = sequenceOf('\t', '\t', '\t').map { it.toInt() }
    private val buffer = StringBuilder()
    private var currentDepth = 0

    override fun exitEntry(ctx: DetendParser.EntryContext?) {
        if (ctx != null) {
            if (!isFirstEntry()) {
                writeEndEntry()
            }
            val nextDepth =
                when {
                    ctx.INDENT() != null -> indentDepth(ctx.text().text)
                    ctx.RIGHTCAUSEDBY() != null -> 0
                    ctx.RIGHTWRAPPEDBY() != null -> 0
                    else -> currentDepth
                }
            when {
                nextDepth > currentDepth -> writeIndent()
                nextDepth < currentDepth -> writeDetend(currentDepth - nextDepth)
                else -> {}
            }
            writeRaw(
                when {
                    // Without newline prefix
                    ctx.RIGHTCAUSEDBY() != null -> ctx.RIGHTCAUSEDBY().text.drop(1)
                    ctx.RIGHTWRAPPEDBY() != null -> ctx.RIGHTWRAPPEDBY().text.drop(1)
                    else -> ""
                }.asSequence()
                    .map { it.toInt() }
            )
            ctx.text()
                .text
                .codePoints()
                .asSequence()
                .dropWhile { it == tabCodepoint }
                .forEach { writeEscaped(it) }
            currentDepth = nextDepth
        }
    }

    override fun exitStart(ctx: DetendParser.StartContext?) {
        if (!isFirstEntry()) {
            writeEndEntry()
        }
        writeDetend(currentDepth)
        currentDepth = 0
    }

    private fun indentDepth(text: String) =
        text.codePoints().asSequence().takeWhile { it == tabCodepoint }.count() + 1

    private fun isFirstEntry() = buffer.isEmpty()

    private fun writeIndent() = writeRaw(indentSequence)

    private fun writeDetend(times: Int) = repeat(times) { writeRaw(dedentSequence) }

    private fun writeEndEntry() = writeRaw(endEntrySequence)

    private fun writeEscaped(codepoint: Int) =
        writeRaw(
            when (codepoint) {
                newlineCodepoint -> newlineEscapeSequence
                tabCodepoint -> tabEscapeSequence
                else -> sequenceOf(codepoint)
            }
        )

    private fun writeRaw(codepoints: Sequence<Int>) =
        codepoints.forEach { buffer.appendCodePoint(it) }

    fun createDetendedContent() = buffer.toString()
}
