@file:JvmName("LogSpyJavaExtensions")
package net.torommo.logspy.java

import java.util.function.Function
import net.torommo.logspy.LogSpy
import net.torommo.logspy.ServiceLoaderWrapper
import net.torommo.logspy.SpyProvider
import net.torommo.logspy.snapshot

/** Creates a configuration for a spy to collect events for a logger with a given name [clazz]. */
public fun <T : Any> spyForLogger(clazz: Class<T>): Function<ThrowingRunnable, LogSpy> {
    return Function { spyOn(clazz, it) }
}

/** Creates a configuration for a spy to collect events for a logger with a given [name]. */
public fun spyForLogger(name: String): Function<ThrowingRunnable, LogSpy> {
    return Function { spyOn(name, it) }
}

/**
 * Creates a spy that contains the log events for a logger with a given name [clazz] that are
 * created during the execution of a [block].
 */
public fun <T: Any> spyOn(clazz: Class<T>, block: ThrowingRunnable): LogSpy {
    ServiceLoaderWrapper.load<SpyProvider>()!!.createFor(clazz.kotlin)
        .use {
            block.run()
            return it.snapshot()
        }
}

/**
 * Creates a spy that contains the log events for a logger with a given [name] that are created
 * during the execution of a [block].
 */
public fun spyOn(name : String, block: ThrowingRunnable): LogSpy {
    return spyOn(name, block::run)
}

/** A runnable that can throw a checked exception. */
public fun interface ThrowingRunnable {
    @Throws(Throwable::class)
    public fun run()
}