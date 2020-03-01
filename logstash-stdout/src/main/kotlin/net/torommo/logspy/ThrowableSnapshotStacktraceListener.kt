package net.torommo.logspy

import net.torommo.logspy.SpiedEvent.ThrowableSnapshot
import java.util.*
import kotlin.IllegalStateException

class ThrowableSnapshotStacktraceListener : StacktraceBaseListener() {
    var stackTrace: ThrowableSnapshot? = null
    private var type: String? = null
    private var message: String? = null
    private val stacks =
        ArrayDeque<MutableList<SpiedEvent.StackTraceElementSnapshot>>()
    private val suppresseds =
        ArrayDeque<MutableList<ThrowableSnapshot>>()
    private val causes =
        ArrayDeque<MutableList<ThrowableSnapshot>>()
    private var rootCauseFirst = false

    override fun enterStackTrace(ctx: StacktraceParser.StackTraceContext?) {
        if (ctx != null) {
            createState()
        }
    }

    override fun exitStackTrace(ctx: StacktraceParser.StackTraceContext?) {
        if (ctx != null) {
            type = ctx.declaringClass().text
            message = when {
                ctx.message() == null && ctx.COLON() == null -> null
                ctx.message() == null -> ""
                else -> ctx.message().text
            }
            stackTrace = createSnapshotFromState()
            destroyState()
        }
    }

    private fun reducedCauses(): ThrowableSnapshot {
        return if (rootCauseFirst) {
            reducedCausesFromRootCauseFirst()
        } else {
            reducedCausesFromRootCauseLast()
        }
    }

    private fun reducedCausesFromRootCauseLast(): ThrowableSnapshot {
        return when {
            causes.first.isEmpty() -> {
                throw IllegalStateException("Cause are empty.")
            }
            causes.first.size == 1 -> {
                causes.first.first()
            }
            else -> {
                var result: ThrowableSnapshot? = null
                for (elementNr in (causes.first.size - 1) downTo 0) {
                    result =  causes.first[elementNr].copy(cause = result)
                }
                result!!
            }
        }
    }

    private fun reducedCausesFromRootCauseFirst(): ThrowableSnapshot {
        return when {
            causes.first.isEmpty() -> {
                throw java.lang.IllegalStateException("Causes are empty.")
            }
            causes.first.size == 1 -> {
                causes.first.first()
            }
            else -> {
                var result: ThrowableSnapshot? = null
                for (elementNr in 0 until causes.first.size) {
                    result =  causes.first[elementNr].copy(cause = result)
                }
                result!!
            }
        }
    }

    override fun exitFilledFrame(ctx: StacktraceParser.FilledFrameContext?) {
        if (ctx != null) {
            stacks.first.add(
                SpiedEvent.StackTraceElementSnapshot(
                    declaringClass = ctx.type().declaringClass().text,
                    methodName = ctx.methodName().text
                )
            )
        }
    }

    override fun enterSuppressedBlock(ctx: StacktraceParser.SuppressedBlockContext?) {
        if (ctx != null) {
            createState()
        }
    }

    override fun exitSuppressedBlock(ctx: StacktraceParser.SuppressedBlockContext?) {
        if (ctx != null) {
            type = ctx.type().text
            message = when {
                ctx.message() == null && ctx.COLON().size > 1 -> null
                ctx.message() == null -> ""
                else -> ctx.message().text
            }
            val chain = createSnapshotFromState()
            destroyState()
            suppresseds.first.add(chain)
        }
    }

    override fun enterCause(ctx: StacktraceParser.CauseContext?) {
        if (ctx != null) {
            createState()
        }
    }

    override fun exitCause(ctx: StacktraceParser.CauseContext?) {
        if (ctx != null) {
            type = ctx.type().text
            message = when {
                ctx.message() == null && ctx.COLON().size > 1 -> null
                ctx.message() == null -> ""
                else -> ctx.message().text
            }
            val chain = createSnapshotFromState()
            destroyState()
            causes.first.add(chain)
        }
    }

    override fun enterWrap(ctx: StacktraceParser.WrapContext?) {
        if (ctx != null) {
            rootCauseFirst = true
            createState()
        }
    }

    override fun exitWrap(ctx: StacktraceParser.WrapContext?) {
        if (ctx != null) {
            type = ctx.type().text
            message = when {
                ctx.message() == null && ctx.COLON().size > 1 -> null
                ctx.message() == null -> ""
                else -> ctx.message().text
            }
            val chain = createSnapshotFromState()
            destroyState()
            causes.first.add(chain)
        }
    }

    private fun createSnapshotFromState(): ThrowableSnapshot {
        val root = ThrowableSnapshot(
            type = type!!,
            message = message,
            stackTrace = stacks.first,
            suppressed = suppresseds.first
        )
        causes.first.add(0, root)
        return reducedCauses()
    }

    private fun createState() {
        type = null
        message = null
        causes.addFirst(mutableListOf())
        stacks.addFirst(mutableListOf())
        suppresseds.addFirst(mutableListOf())
    }

    private fun destroyState() {
        stacks.removeFirst()
        causes.removeFirst()
        suppresseds.removeFirst()
    }
}