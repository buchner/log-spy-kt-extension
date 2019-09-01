package net.torommo.logspy

import kotlin.reflect.KClass

/**
 * Resolves a log spy during runtime.
 *
 * Implementations must be thread-safe.
 */
interface SpyProvider {
    /**
     * Resolves a spy by a provided type.
     */
    fun resolve(name: KClass<out Any>) : DisposableLogSpy

    /**
     * Resolves a spy by a provided literal.
     */
    fun resolve(name: String) : DisposableLogSpy

    /**
     * A spy that can release its resources if required.
     */
    interface DisposableLogSpy : LogSpy, AutoCloseable {
        override fun close() {
            // Nothing to do
        }
    }
}