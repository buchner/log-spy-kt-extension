package net.torommo.logspy

import net.torommo.logspy.SpyProvider.DisposableLogSpy
import kotlin.reflect.KClass

class FakeSpyProvider : SpyProvider {
    private val byTypeInstances = mutableListOf<Pair<KClass<out Any>, FakeLogSpy>>()
    private val byLiteralInstances = mutableListOf<Pair<String, FakeLogSpy>>()

    override fun createFor(name: KClass<out Any>): DisposableLogSpy {
        val instance = FakeLogSpy()
        byTypeInstances.add(Pair(name, instance))
        return instance
    }

    override fun createFor(name: String): DisposableLogSpy {
        val instance = FakeLogSpy()
        byLiteralInstances.add(Pair(name, instance))
        return instance
    }

    fun addEvent(name: KClass<out Any>, event: SpiedEvent) {
        allInstancesFor(name).forEach { it.add(event) }
    }

    fun addEvent(name: String, event: SpiedEvent) {
        byLiteralInstances.filter { it.first == name }.forEach { it.second.add(event) }
    }

    fun allInstancesFor(name: KClass<out Any>): List<FakeLogSpy> {
        return byTypeInstances.filter { it.first == name }.map { it.second }
    }

    fun allInstancesFor(name: String): List<FakeLogSpy> {
        return byLiteralInstances.filter { it.first == name }.map { it.second }
    }
}
