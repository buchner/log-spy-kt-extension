package net.torommo.logspy

/**
 * A log event that was recorded by the spy.
 *
 * @property message the logged message with all placeholders rendered, if possible.
 * @property exception a copy of the exception as it was observed at the time of the logging.
 * @property mdc the mapped diagnostic context, if available.
 */
public data class SpiedEvent(
    val message: String?,
    val level: Level,
    val exception: ThrowableSnapshot?,
    val mdc: Map<String, String>
) {

    /** The level that was used for the log event. */
    public enum class Level {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }

    /**
     * A copy of the information of the exception as observered at the time that the exception was
     * logged.
     *
     * @property type the data type of the exception
     * @property message the message of the exception
     * @property cause the information of the causing exception
     * @property suppressed the information of the suppressed exceptions
     * @property stackTrace the information of the stack trace
     */
    public data class ThrowableSnapshot(
        val type: String,
        val message: String?,
        val cause: ThrowableSnapshot? = null,
        val suppressed: List<ThrowableSnapshot> = listOf(),
        val stackTrace: List<StackTraceElementSnapshot> = listOf()
    )

    /**
     * A copy of the information of a stack trace element as it was observed at the time of the
     * logging.
     *
     * @property declaringClass the class containing the execution point
     * @property methodName the method containing the execution point
     */
    public data class StackTraceElementSnapshot(val declaringClass: String, val methodName: String)
}