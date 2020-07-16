package net.torommo.logspy

import kotlin.reflect.KClass

/**
 * Provides log spies during runtime.
 *
 * Created spies must only see events from after their creation.
 *
 * Implementations must be thread-safe.
 */
interface SpyProvider {
    /** Creates a spy for a provided type. */
    fun createFor(name: KClass<out Any>): DisposableLogSpy

    /** Creates a spy for a provided literal. */
    fun createFor(name: String): DisposableLogSpy

    /** A spy that can release its resources if required. */
    interface DisposableLogSpy : LogSpy, AutoCloseable {
        override fun close() {
            // Nothing to do
        }
    }
}