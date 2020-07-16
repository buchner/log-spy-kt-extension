package net.torommo.logspy

import java.io.OutputStream
import java.io.PrintStream
import java.util.*

/**
 * [PrintStream] implementation that forwards all output to another instance and to provided
 * [OutputStream] instance.
 *
 * @param base the other [PrintStream] instance
 * @param interceptor the [OutputStream] instance
 */
class InterceptablePrintStream(private val base: PrintStream, interceptor: OutputStream) :
    PrintStream(NullOutputStream) {

    private val delegate = PrintStream(interceptor)

    override fun print(b: Boolean) {
        forwardToBoth { print(b) }
    }

    override fun print(c: Char) {
        forwardToBoth { print(c) }
    }

    override fun print(i: Int) {
        forwardToBoth { print(i) }
    }

    override fun print(l: Long) {
        forwardToBoth { print(l) }
    }

    override fun print(f: Float) {
        forwardToBoth { print(f) }
    }

    override fun print(d: Double) {
        forwardToBoth { print(d) }
    }

    override fun print(s: CharArray) {
        forwardToBoth { print(s) }
    }

    override fun print(s: String?) {
        forwardToBoth { print(s) }
    }

    override fun print(obj: Any?) {
        forwardToBoth { print(obj) }
    }

    override fun write(b: Int) {
        forwardToBoth { write(b) }
    }

    override fun write(b: ByteArray) {
        forwardToBoth { write(b) }
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        forwardToBoth { write(buf, off, len) }
    }

    override fun println() {
        forwardToBoth { println() }
    }

    override fun println(x: Boolean) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Char) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Int) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Long) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Float) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Double) {
        forwardToBoth { println(x) }
    }

    override fun println(x: CharArray) {
        forwardToBoth { println(x) }
    }

    override fun println(x: String?) {
        forwardToBoth { println(x) }
    }

    override fun println(x: Any?) {
        forwardToBoth { println(x) }
    }

    override fun flush() {
        forwardToBoth { flush() }
    }

    override fun checkError(): Boolean {
        return forwardToBothAndAssertSameResults { checkError() }
    }

    override fun append(csq: CharSequence?): PrintStream {
        forwardToBoth { append(csq) }

        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): PrintStream {
        forwardToBoth { append(csq, start, end) }

        return this
    }

    override fun append(c: Char): PrintStream {
        forwardToBoth { append(c) }

        return this
    }

    override fun format(format: String, vararg args: Any?): PrintStream {
        forwardToBoth { format(format, *args) }

        return this
    }

    override fun format(l: Locale?, format: String, vararg args: Any?): PrintStream {
        forwardToBoth { format(l, format, *args) }

        return this
    }

    override fun printf(format: String, vararg args: Any?): PrintStream {
        forwardToBoth { printf(format, *args) }

        return this
    }

    override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream {
        forwardToBoth { printf(l, format, *args) }

        return this
    }

    override fun close() {
        forwardToBoth { close() }
    }

    private fun forwardToBoth(action: PrintStream.() -> Unit) {
        action(delegate)
        action(base)
    }

    private fun <T> forwardToBothAndAssertSameResults(action: PrintStream.() -> T): T {
        val result1 = action(delegate)
        val result2 = action(base)

        if (result1 != result2) {
            throw AssertionError("Results differed: $result1 != $result2")
        } else {
            return result1
        }
    }

    /** Output stream that throws away everything that it is written to it. */
    private object NullOutputStream : OutputStream() {
        override fun write(b: Int) {
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            // Overwritten for performance optimization
        }
    }
}
