package net.torommo.logspy

import java.util.ServiceLoader
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

/**
 * Thin wrapper around Java's service loader to make it more test friendly and easier to use from
 * Kotlin.
 */
public object ServiceLoaderWrapper {
    private val lock = ReentrantLock()
    private val services: MutableMap<KClass<*>, Any> = mutableMapOf()

    /**
     * Defines a instance to be used for the service [T]
     *
     * A service instance defined by this method has a higher priority than a instance for the same
     * service from the service loader. This method is especially useful for testing where you might
     * not want to have the Java's service loader.
     */
    public inline fun <reified T: Any> predefine(instance: T) {
        predefine(T::class, instance)
    }

    public fun predefine(type: KClass<*>, service: Any) {
        lock.withLock { services[type] = service }
    }

    public inline fun <reified T: Any> load(): T? {
        return load(T::class)
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> load(service: KClass<T>): T? {
        lock.withLock {
            return (services[service]) as T?
                ?: ServiceLoader.load<T>(service.java).iterator().asSequence().firstOrNull()
        }
    }
}