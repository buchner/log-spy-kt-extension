package net.torommo.logspy

/**
 * A log event that was recorded by the spy.
 *
 * Keep in mind that the value of [exception] can be mutable and not thread-safe.
 * If the recorded event is accessed from a different thread than the thread that created the log event,
 * the content of the exception might not be the same. Even its unusual, code might alter an exception after
 * it was logged. In that case the recorded event will
 *
 * @property message The logged message with all placeholders rendered, if possible.
 * @property mdc The mapped diagnostic context, if available.
 */
data class SpiedEvent(
        val message: String?,
        val level: Level,
        val exception: Throwable?,
        val mdc: Map<String, String>) {

    enum class Level {
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE
    }
}