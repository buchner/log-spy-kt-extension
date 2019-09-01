package net.torommo.logspy

import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

class FaultySpyProvider : SpyProvider {
    override fun resolve(name: KClass<out Any>): SpyProvider.DisposableLogSpy {
        throw UnsupportedOperationException("Unsupported for testing purposes.")
    }

    override fun resolve(name: String): SpyProvider.DisposableLogSpy {
        throw UnsupportedOperationException("Unsupported for testing purposes.")
    }
}