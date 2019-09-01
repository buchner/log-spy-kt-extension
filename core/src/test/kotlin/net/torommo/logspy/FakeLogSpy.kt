package net.torommo.logspy

import net.torommo.logspy.SpyProvider.DisposableLogSpy

class FakeLogSpy : DisposableLogSpy {
    private var closed = false

    override fun events(): List<SpiedEvent> {
        return listOf()
    }

    override fun close() {
        closed = true
    }

    fun isClosed() : Boolean {
        return closed
    }
}