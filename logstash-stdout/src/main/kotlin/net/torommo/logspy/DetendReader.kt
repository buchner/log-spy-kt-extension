package net.torommo.logspy

import java.io.Reader
import java.util.*
import kotlin.math.min

/**
 * A reader that prepares the indent based stack trace output to make it easier parsable.
 *
 * Java uses indentation by tabs to indicate the depth within suppressed exceptions. Such a format
 * is hard to parse. To make parsing easier this reader replaces the open sequence and adds a closing sequence to each
 * indented block. A block is opened by a new line and a tab. The closing sequence consists of a tab and new line.
 *
 * To distinguish opening from closing sequences all tabs and new lines are escaped by duplicating them.
 */
class DetendReader(val delegate: Reader) : Reader() {
    private val NEWLINE = '\n'.toInt()
    private val TAB = '\t'.toInt()
    private val END = -1
    private val buffer = ArrayDeque<Int>()
    private var currentDepth = 0

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        fillBuffer(len)
        return if (buffer.isEmpty()) {
            END
        } else {
            writeToBufferAndReturnNumberOfWrittenCharacters(cbuf, off, len)
        }
    }

    private fun writeToBufferAndReturnNumberOfWrittenCharacters(
        cbuf: CharArray,
        off: Int,
        len: Int
    ): Int {
        val charactersToWrite = min(len, buffer.size)
        for (charNr in 0 until charactersToWrite) {
            cbuf[off + charNr] = buffer.pop().toChar()
        }
        return charactersToWrite
    }

    private fun fillBuffer(requestedLength: Int) {
        while (buffer.size < requestedLength) {
            val current = delegate.read()
            if (current == END) {
                return
            } else {
                convertAndWrite(current)
            }
        }
    }

    private fun convertAndWrite(value: Int) {
        when (value) {
            NEWLINE -> {
                writeEscapedNewline()
                val tabsAndNext = readTabsUntilNextNonTab()
                val tabsCount = tabsAndNext.count() - 1
                when {
                    tabsCount < currentDepth -> {
                        writeClose(currentDepth - tabsCount)
                    }
                    tabsCount > currentDepth -> {
                        writeOpen(tabsCount - currentDepth)
                    }
                    tabsCount == currentDepth -> {
                        // In the same block
                    }
                }
                currentDepth = tabsCount
                convertAndWrite(tabsAndNext.last())
            }
            TAB -> {
                writeEscapedTab()
            }
            END -> {
                writeClose(currentDepth)
                currentDepth = 0
                return
            }
            else -> buffer.addLast(value)
        }
    }

    private fun writeEscapedNewline() {
        buffer.addLast(NEWLINE)
        buffer.addLast(NEWLINE)
    }

    private fun writeEscapedTab() {
        buffer.addLast(TAB)
        buffer.addLast(TAB)
    }

    private fun writeOpen(times: Int) {
        repeat(times) {
            buffer.addLast(NEWLINE)
            buffer.addLast(TAB)
        }
    }

    private fun writeClose(times: Int) {
        repeat(times) {
            buffer.addLast(TAB)
            buffer.addLast(NEWLINE)
        }
    }

    private fun readTabsUntilNextNonTab(): Sequence<Int> {
        val result = mutableListOf<Int>()
        do {
            result.add(delegate.read())
        } while (result.last() == TAB)

        return result.asSequence()
    }

    override fun close() {
        delegate.close()
    }
}