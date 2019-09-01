package net.torommo.logspy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.core.AppenderBase
import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpyProvider.DisposableLogSpy
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

/**
 * Resolves slf4j loggers that use Logback as backend.
 */
class Slf4jLogbackSpyProvider : SpyProvider {

    override fun resolve(name: KClass<out Any>): DisposableLogSpy {
        return LogbackSpy(name)
    }

    override fun resolve(name: String): DisposableLogSpy {
        return LogbackSpy(name)
    }

    private class LogbackSpy private constructor(provider: () -> Logger) : DisposableLogSpy {
        private val appender = TrackingAppender<ILoggingEvent>()
        private val logger: Logger = provider()

        constructor(name: KClass<out Any>) : this({LoggerFactory.getLogger(name.java) as Logger})

        constructor(name: String) : this({ LoggerFactory.getLogger(name) as Logger})

        init {
            logger.level = Level.ALL
            appender.start()
            logger.addAppender(appender)
        }

        override fun events(): List<SpiedEvent> {
            return appender.events()
                    .map { toSpiedEvent(it) }
        }

        private fun toSpiedEvent(event: ILoggingEvent): SpiedEvent {
            return SpiedEvent(
                    event.formattedMessage,
                    toSpiedLevel(event.level),
                    (event.throwableProxy as ThrowableProxy?)?.throwable,
                    event.mdcPropertyMap.toMap()
            )
        }

        private fun toSpiedLevel(level: Level): SpiedEvent.Level {
            return when (level) {
                Level.TRACE -> TRACE
                Level.DEBUG -> DEBUG
                Level.INFO -> INFO
                Level.WARN -> WARN
                Level.ERROR -> ERROR
                else -> throw IllegalArgumentException("Unsupported level $level")
            }
        }

        override fun close() {
            logger.detachAppender(appender)
            appender.stop()
        }
    }

    private class TrackingAppender<T>(
    ) : AppenderBase<T>() {

        private val lock = ReentrantLock()
        private val events = mutableListOf<T>()

        override fun append(eventObject: T) {
            lock.withLock { events.add(eventObject) }
        }

        fun events() : List<T> {
            lock.withLock { return events.toList() }
        }
    }
}