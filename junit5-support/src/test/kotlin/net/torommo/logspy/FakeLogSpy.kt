package net.torommo.logspy

import net.torommo.logspy.SpyProvider.DisposableLogSpy

class FakeLogSpy : DisposableLogSpy {
    private var closed = false
    private var events = mutableListOf<SpiedEvent>()

    fun add(event: SpiedEvent) {
        events.add(event)
    }

    override fun events(): List<SpiedEvent> {
        return events
    }

    override fun close() {
        closed = true
    }

    fun isClosed(): Boolean {
        return closed
    }
}