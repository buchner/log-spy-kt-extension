package net.torommo.logspy

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import net.torommo.logspy.LogstashStdoutSpyProvider.Singletons.stream

/**
 * Creates [SpyProvider.DisposableLogSpy] instances that are capable of spying log events on the
 * standard output that have the Logstash format.
 */
class LogstashStdoutSpyProvider : SpyProvider {
    private object Singletons {
        val stream: MultiplexOutputStream = MultiplexOutputStream()

        init {
            val outInterceptor = InterceptablePrintStream(System.out, stream)
            System.setOut(outInterceptor)
        }
    }

    override fun createFor(name: KClass<out Any>): SpyProvider.DisposableLogSpy {
        return createFor(name.qualifiedName!!)
    }

    override fun createFor(name: String): SpyProvider.DisposableLogSpy {
        return StdoutLogSpy.create(name)
    }

    private class StdoutLogSpy private constructor(val loggerName: String) :
        SpyProvider.DisposableLogSpy {

        private val content = ByteArrayOutputStream();

        init {
            stream.add(content)
        }

        companion object {
            fun create(loggerName: String) : StdoutLogSpy {
                return StdoutLogSpy(loggerName)
            }
        }

        override fun events(): List<SpiedEvent> {
            return JsonEventParser(loggerName, content()).events()
        }

        override fun close() {
            stream.remove(content)
            content.close()
        }

        private fun content(): String {
            return content.toString(StandardCharsets.UTF_8.name())
        }
    }

    /**
     * A stream that writes the output that it receives to all registered streams.
     *
     * Streams can be registered by [add] and unregistered by [remove].
     *
     * Instances are thread-safe in the sense that all operations on this instance happen
     * atomically.
     */
    private class MultiplexOutputStream : OutputStream() {
        val lock = ReentrantReadWriteLock()
        val streams = mutableSetOf<OutputStream>()

        /** Registers a new [stream]. */
        fun add(stream: OutputStream) {
            lock.write { streams.add(stream) }
        }

        /** Unregisters a [stream]. */
        fun remove(stream: OutputStream) {
            lock.write { streams.remove(stream) }
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            lock.read { multiplex { write(b, off, len) } }
        }

        override fun write(b: Int) {
            lock.read { multiplex { write(b) } }
        }

        override fun flush() {
            lock.read { multiplex { flush() } }
        }

        override fun close() {
            lock.write {
                multiplex { close() }
                streams.clear()
            }
        }

        private fun multiplex(action: OutputStream.() -> Unit) {
            streams.forEach { action(it) }
        }
    }
}