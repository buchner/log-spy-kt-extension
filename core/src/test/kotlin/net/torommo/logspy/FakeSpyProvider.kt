package net.torommo.logspy

import net.torommo.logspy.SpyProvider.DisposableLogSpy
import kotlin.reflect.KClass

class FakeSpyProvider : SpyProvider {
    private val providersByClass = mutableMapOf<KClass<out Any>, DisposableLogSpy>()
    private val providersByLiteral = mutableMapOf<String, DisposableLogSpy>()

    fun register(name: KClass<out Any>, spy: DisposableLogSpy) {
        providersByClass.put(name, spy)
    }

    fun register(name: String, spy: DisposableLogSpy) {
        providersByLiteral.put(name, spy)
    }

    override fun resolve(name: KClass<out Any>): DisposableLogSpy {
        return providersByClass.getOrDefault(name, FakeLogSpy())
    }

    override fun resolve(name: String): DisposableLogSpy {
        return providersByLiteral.getOrDefault(name, FakeLogSpy())
    }
}