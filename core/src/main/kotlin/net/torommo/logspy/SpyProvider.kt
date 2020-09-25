package net.torommo.logspy

import kotlin.reflect.KClass

/**
 * Provides log spies during runtime.
 *
 * Created spies must only see events from after their creation.
 *
 * Implementations must be thread-safe.
 */
public interface SpyProvider {
    /** Creates a spy for a provided type. */
    public fun createFor(name: KClass<out Any>): DisposableLogSpy

    /** Creates a spy for a provided literal. */
    public fun createFor(name: String): DisposableLogSpy

    /** A spy that can release its resources if required. */
    public interface DisposableLogSpy : LogSpy, AutoCloseable {
        override fun close() {
            // Nothing to do
        }
    }
}