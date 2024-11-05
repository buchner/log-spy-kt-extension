package net.torommo.logspy.matchers

import net.torommo.logspy.SpiedEvent
import net.torommo.logspy.SpiedEvent.StackTraceElementSnapshot
import net.torommo.logspy.SpiedEvent.ThrowableSnapshot

internal fun spiedEvent(): SpiedEvent = SpiedEvent("test message", SpiedEvent.Level.INFO, throwable(), emptyMap())

internal fun throwable(): ThrowableSnapshot =
    ThrowableSnapshot(
        "net.torommo.logspy.Test",
        "Test message",
        cause(),
        listOf(suppressed1(), suppressed2()),
        listOf(stackTrace()),
    )

private fun cause(): ThrowableSnapshot = ThrowableSnapshot("net.torommo.logspy.Test", "Test message cause")

internal fun suppressed(): ThrowableSnapshot = ThrowableSnapshot("net.torommo.logspy.Test", "Test message suppressed")

internal fun suppressed1(): ThrowableSnapshot = suppressed().copy(message = "Test message suppressed 1")

internal fun suppressed2(): ThrowableSnapshot = suppressed().copy(message = "Test message suppressed 2")

internal fun stackTrace(): StackTraceElementSnapshot =
    StackTraceElementSnapshot(declaringClass = "net.torommo.logspy.Test", methodName = "aTest")

internal fun stackTrace1(): StackTraceElementSnapshot = stackTrace().copy(methodName = "testMethod1")

internal fun stackTrace2(): StackTraceElementSnapshot = stackTrace().copy(methodName = "testMethod2")
