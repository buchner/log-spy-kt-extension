package net.torommo.logspy

import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

class FaultySpyProvider : SpyProvider {
    override fun createFor(name: KClass<out Any>): SpyProvider.DisposableLogSpy {
        throw UnsupportedOperationException("Unsupported for testing purposes.")
    }

    override fun createFor(name: String): SpyProvider.DisposableLogSpy {
        throw UnsupportedOperationException("Unsupported for testing purposes.")
    }
}
