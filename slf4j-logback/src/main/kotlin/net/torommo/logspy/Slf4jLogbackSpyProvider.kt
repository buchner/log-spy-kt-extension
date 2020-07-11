package net.torommo.logspy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.StackTraceElementProxy
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.core.AppenderBase
import net.torommo.logspy.SpiedEvent.Level.DEBUG
import net.torommo.logspy.SpiedEvent.Level.ERROR
import net.torommo.logspy.SpiedEvent.Level.INFO
import net.torommo.logspy.SpiedEvent.Level.TRACE
import net.torommo.logspy.SpiedEvent.Level.WARN
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import net.torommo.logspy.SpyProvider.DisposableLogSpy
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

/**
 * Resolves slf4j loggers that use Logback as backend.
 */
class Slf4jLogbackSpyProvider : SpyProvider {

    override fun createFor(name: KClass<out Any>): DisposableLogSpy {
        return LogbackSpy(name)
    }

    override fun createFor(name: String): DisposableLogSpy {
        return LogbackSpy(name)
    }

    private class LogbackSpy private constructor(provider: () -> Logger) : DisposableLogSpy {
        private val appender = TrackingAppender<ILoggingEvent>()
        private val logger: Logger = provider()

        constructor(name: KClass<out Any>) : this({ LoggerFactory.getLogger(name.java) as Logger })

        constructor(name: String) : this({ LoggerFactory.getLogger(name) as Logger })

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
                toThrowableSnapshotFromMaybe(event.throwableProxy as ThrowableProxy?),
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

        private fun toThrowableSnapshotFromMaybe(throwable: IThrowableProxy?): ThrowableSnapshot? {
            return throwable?.let {
                toThrowableSnapshot(throwable)
            };
        }


        private fun toThrowableSnapshot(throwable: IThrowableProxy): ThrowableSnapshot {
            return ThrowableSnapshot(
                throwable.className,
                throwable.message,
                toThrowableSnapshotFromMaybe(throwable.cause),
                throwable.suppressed.asSequence().map { toThrowableSnapshot(it) }.toList(),
                throwable.stackTraceElementProxyArray.asSequence().filterNotNull().map { toStackTraceElementSnapshot(it) }.toList()
            );
        }

        private fun toStackTraceElementSnapshot(element: StackTraceElementProxy): StackTraceElementSnapshot {
            return StackTraceElementSnapshot(
                element.stackTraceElement.className,
                element.stackTraceElement.methodName
            )
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

        fun events(): List<T> {
            lock.withLock { return events.toList() }
        }
    }
}