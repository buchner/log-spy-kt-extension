package net.torommo.logspy

import net.torommo.logspy.LogstashStdoutSpyProvider.Singletons.stream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

class LogstashStdoutSpyProvider : SpyProvider {

    private object Singletons {
        val stream: InterceptablePrintStream

        init {
            stream = InterceptablePrintStream(System.out)
            System.setOut(stream)
        }
    }

    override fun resolve(name: KClass<out Any>): SpyProvider.DisposableLogSpy {
        return resolve(name.qualifiedName!!)
    }

    override fun resolve(name: String): SpyProvider.DisposableLogSpy {
        return StdoutLogSpy.create(name)
    }

    private class StdoutLogSpy private constructor(val loggerName: String) : SpyProvider.DisposableLogSpy {

        companion object {
            fun create(loggerName: String) : StdoutLogSpy {
                val result = StdoutLogSpy(loggerName)
                stream.register(result)

                return result
            }
        }

        init {
            stream // Force initialization before log events are sent to stdout
        }

        override fun events(): List<SpiedEvent> {
            return JsonEventParser(loggerName, stream.content(this)).events()
        }

        override fun close() {
            stream.unregister(this)
        }
    }

    private class InterceptablePrintStream(
        val base: PrintStream,
        val streams: MultiplexOutputStream = MultiplexOutputStream()
    ) : PrintStream(streams) {
        val lock = ReentrantReadWriteLock()
        val registry = ConcurrentHashMap<StdoutLogSpy, ByteArrayOutputStream>()

        fun register(spy: StdoutLogSpy) {
            val delegateStream = ByteArrayOutputStream()
            lock.write {
                streams.add(delegateStream)
                registry.put(spy, delegateStream)
            }
        }

        fun unregister(spy: StdoutLogSpy) {
            lock.write {
                streams.remove(registry.get(spy)!!)
                registry.remove(spy)!!.close()
            }
        }

        override fun print(b: Boolean) {
            lock.read {
                super.print(b)
            }
            base.print(b)
        }

        override fun print(c: Char) {
            lock.read {
                super.print(c)
            }
            base.print(c)
        }

        override fun print(i: Int) {
            lock.read {
                super.print(i)
            }
            base.print(i)
        }

        override fun print(l: Long) {
            lock.read {
                super.print(l)
            }
            base.print(l)
        }

        override fun print(f: Float) {
            lock.read {
                super.print(f)
            }
            base.print(f)
        }

        override fun print(d: Double) {
            lock.read {
                super.print(d)
            }
            base.print(d)
        }

        override fun print(s: CharArray) {
            lock.read {
                super.print(s)
            }
            base.print(s)
        }

        override fun print(s: String?) {
            lock.read {
                super.print(s)
            }
            base.print(s)
        }

        override fun print(obj: Any?) {
            lock.read {
                super.print(obj)
            }
            base.print(obj)
        }

        override fun write(b: Int) {
            lock.read {
                super.write(b)
            }
            base.write(b)
        }

        override fun write(buf: ByteArray, off: Int, len: Int) {
            lock.read {
                super.write(buf, off, len)
            }
            base.write(buf, off, len)
        }

        override fun println() {
            lock.read {
                super.println()
            }
            base.println()
        }

        override fun println(x: Boolean) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Char) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Int) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Long) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Float) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Double) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: CharArray) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: String?) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun println(x: Any?) {
            lock.read {
                super.println(x)
            }
            base.println(x)
        }

        override fun flush() {
            lock.read {
                super.flush()
            }
            base.flush()
        }

        override fun checkError(): Boolean {
            val result1 = lock.read {
                super.checkError()
            }
            val result2 = base.checkError()

            if (result1 != result2) {
                throw AssertionError("Results differed: ${result1} != ${result2}")
            } else {
                return result1
            }
        }

        override fun append(csq: CharSequence?): PrintStream {
            lock.read {
                super.append(csq)
            }
            base.append(csq)

            return this
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): PrintStream {
            lock.read {
                super.append(csq, start, end)
            }
            base.append(csq, start, end)

            return this
        }

        override fun append(c: Char): PrintStream {
            lock.read {
                super.append(c)
            }
            base.append(c)

            return this
        }

        override fun format(format: String, vararg args: Any?): PrintStream {
            lock.read {
                super.format(format, *args)
            }
            base.format(format, args)

            return this
        }

        override fun format(l: Locale?, format: String, vararg args: Any?): PrintStream {
            lock.read {
                super.format(l, format, *args)
            }
            base.format(l, format, args)

            return this
        }

        override fun printf(format: String, vararg args: Any?): PrintStream {
            lock.read {
                super.printf(format, *args)
            }
            base.printf(format, args)

            return this
        }

        override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream {
            lock.read {
                super.printf(l, format, *args)
            }
            base.printf(l, format, args)

            return this
        }

        override fun close() {
            base.close()
            lock.read {
                super.close()
            }
        }

        fun content(spy: StdoutLogSpy): String {
            return lock.read {
                registry.get(spy)!!.toString(StandardCharsets.UTF_8.name())
            }
        }
    }

    private class MultiplexOutputStream : OutputStream() {
        val lock = ReentrantReadWriteLock()
        val streams = mutableSetOf<OutputStream>()

        fun add(stream: OutputStream) {
            lock.write {
                streams.add(stream)
            }
        }

        fun remove(stream: OutputStream) {
            lock.write {
                streams.remove(stream)
            }
        }

        override fun write(b: Int) {
            lock.read {
                for (it in streams) {
                    it.write(b)
                }
            }
        }

        override fun flush() {
            lock.read {
                for (it in streams) {
                    it.flush()
                }
            }
        }

        override fun close() {
            lock.write {
                for (stream in streams) {
                    stream.close()
                }
                streams.clear()
            }
        }
    }
}