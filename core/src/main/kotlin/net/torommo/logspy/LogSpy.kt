package net.torommo.logspy

import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot

/**
 * The recorded log events.
 */
interface LogSpy {
    /**
     * All log events with the severity error.
     */
    fun errors(): List<String> {
        return messagesWith(ERROR)
    }

    /**
     * All log events with the severity warning.
     */
    fun warnings(): List<String> {
        return messagesWith(WARN)
    }

    /**
     * All log events with the severity info.
     */
    fun infos(): List<String> {
        return messagesWith(INFO)
    }

    /**
     * All log events with the severity debug.
     */
    fun debugs(): List<String> {
        return messagesWith(DEBUG)
    }

    /**
     * All log events with the severity trace.
     */
    fun traces(): List<String> {
        return messagesWith(TRACE)
    }

    private fun messagesWith(level: SpiedEvent.Level): List<String> {
        return events()
            .filter { it.level == level }
            .mapNotNull { it.message }
            .toList();
    }

    /**
     * All logged exceptions.
     */
    fun exceptions(): List<ThrowableSnapshot> {
        return events()
            .mapNotNull { it.exception }
            .toList()
    }

    /**
     * All recorded log events.
     */
    fun events(): List<SpiedEvent>
}
