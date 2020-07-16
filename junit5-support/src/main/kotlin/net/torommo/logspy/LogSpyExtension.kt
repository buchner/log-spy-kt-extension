package net.torommo.logspy

import kotlin.reflect.KClass
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver

/** Junit 5 extension that allows to record and analyse log events. */
class LogSpyExtension(
    private val spyProviderResolver: () -> SpyProvider = { ServiceLoaderWrapper.load()!! }
) : ParameterResolver {
    private val namespace = Namespace.create("net.torommo.logspy")

    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Boolean {
        return if (parameterContext?.parameter?.type == LogSpy::class.java) {
            if ((parameterContext.isAnnotated(ByType::class.java))
                .xor(parameterContext.isAnnotated(ByLiteral::class.java))
            ) {
                true
            } else {
                if (parameterContext.isAnnotated(ByType::class.java)) {
                    throw ParameterResolutionException(
                        "Spy may only annotation with one of: ${ByType::class.java.name}, " +
                            "${ByLiteral::class.java.name}."
                    )
                } else {
                    false
                }
            }
        } else {
            false
        }
    }

    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): LogSpy {
        val result = resolveByClass(parameterContext!!) ?: resolveByLiteral(parameterContext)!!
        extensionContext!!.getStore(namespace)
            .put(
                parameterContext.declaringExecutable.name,
                CloseableResourceToAutoCloseableWrapper(result)
            )
        return result
    }

    private fun resolveByClass(parameterContext: ParameterContext): SpyProvider.DisposableLogSpy? {
        return parameterContext.findAnnotation(ByType::class.java)
            .map { annotation -> annotation.value }
            .map { name -> resolveGuarded(name) }
            .orElse(null)
    }

    private fun resolveGuarded(name: KClass<out Any>): SpyProvider.DisposableLogSpy {
        try {
            return spyProviderResolver().createFor(name)
        } catch (exception: RuntimeException) {
            throw ParameterResolutionException(
                "Could not resolve spy; provider threw an exception.",
                exception
            )
        }
    }

    private fun resolveByLiteral(parameterContext: ParameterContext):
        SpyProvider.DisposableLogSpy? {
            return parameterContext.findAnnotation(ByLiteral::class.java)
                .map { annotation -> annotation.value }
                .map { name -> resolveGuarded(name) }
                .orElse(null)
        }

    private fun resolveGuarded(name: String): SpyProvider.DisposableLogSpy {
        try {
            return spyProviderResolver().createFor(name)
        } catch (exception: RuntimeException) {
            throw ParameterResolutionException(
                "Could not resolve spy; provider threw an exception.",
                exception
            )
        }
    }

    private class CloseableResourceToAutoCloseableWrapper(val delegate: AutoCloseable) :
        CloseableResource {

        override fun close() {
            delegate.close()
        }
    }
}
