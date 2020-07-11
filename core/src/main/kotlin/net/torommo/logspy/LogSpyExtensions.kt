package net.torommo.logspy

import net.torommo.logspy.ServiceLoaderWrapper.load

/**
 * Creates a configuration for a spy to collect events for a logger with a given name [T].
 */
inline fun <reified T: Any> spyForLogger(): (() -> Unit) -> LogSpy {
    return { spyForLogger<T>(it) }
}

/**
 * Creates a spy that contains the log events for a logger with a given name [T] that
 * are created during the execution of a [block].
 */
inline fun <reified T: Any> spyForLogger(noinline block: () -> Unit): LogSpy {
    load<SpyProvider>()!!.createFor(T::class).use {
        block()
        return it.snapshot()
    }
}

/**
 * Creates a configuration for a spy to collect events for a logger with a given [name].
 */
fun spyForLogger(name: String): (() -> Unit) -> LogSpy {
    return { spyForLogger(name, it) }
}

/**
 * Creates a spy that contains the log events for a logger with a given [name] that
 * are created during the execution of a [block].
 */
fun spyForLogger(name : String, block: () -> Unit): LogSpy {
    load<SpyProvider>()!!.createFor(name).use {
        block()
        return it.snapshot()
    }
}

/**
 * Creates a spy with a immutable copy of the current state of this spy.
 */
fun LogSpy.snapshot() : LogSpy {
    return SnapshotLogSpy(this.events().asSequence().toList())
}

private class SnapshotLogSpy(private val events: List<SpiedEvent>) : LogSpy {
    override fun events(): List<SpiedEvent> {
        return events
    }
}
