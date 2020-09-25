package net.torommo.logspy

/**
 * A log event that was recorded by the spy.
 *
 * @property message The logged message with all placeholders rendered, if possible.
 * @property exception A copy of the exception as it was observed at the time of the logging.
 * @property mdc The mapped diagnostic context, if available.
 */
public data class SpiedEvent(
    val message: String?,
    val level: Level,
    val exception: ThrowableSnapshot?,
    val mdc: Map<String, String>
) {

    public enum class Level {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }

    public data class ThrowableSnapshot(
        val type: String,
        val message: String?,
        val cause: ThrowableSnapshot? = null,
        val suppressed: List<ThrowableSnapshot> = listOf(),
        val stackTrace: List<StackTraceElementSnapshot> = listOf()
    )

    public data class StackTraceElementSnapshot(val declaringClass: String, val methodName: String)
}