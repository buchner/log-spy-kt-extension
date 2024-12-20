package net.torommo.logspy

import net.torommo.logspy.ServiceLoaderWrapper.load

/** Creates a configuration for a spy to collect events for a logger with a given name [T]. */
public inline fun <reified T : Any> spyForLogger(): (() -> Unit) -> LogSpy {
    return { spyOn<T>(it) }
}

/**
 * Creates a spy that contains the log events for a logger with a given name [T] that are created
 * during the execution of a [block].
 */
public inline fun <reified T : Any> spyOn(noinline block: () -> Unit): LogSpy {
    load<SpyProvider>()!!.createFor(T::class)
        .use {
            block()
            return it.snapshot()
        }
}

/** Creates a configuration for a spy to collect events for a logger with a given [name]. */
public fun spyForLogger(name: String): (() -> Unit) -> LogSpy {
    return { spyOn(name, it) }
}

/**
 * Creates a spy that contains the log events for a logger with a given [name] that are created
 * during the execution of a [block].
 */
public fun spyOn(
    name: String,
    block: () -> Unit,
): LogSpy {
    load<SpyProvider>()!!.createFor(name)
        .use {
            block()
            return it.snapshot()
        }
}

/** Creates a spy with a immutable copy of the current state of this spy. */
public fun LogSpy.snapshot(): LogSpy {
    return SnapshotLogSpy(this.events().asSequence().toList())
}

private class SnapshotLogSpy(private val events: List<SpiedEvent>) : LogSpy {
    override fun events(): List<SpiedEvent> {
        return events
    }
}
