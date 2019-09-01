package net.torommo.logspy

/**
 * Configures a log spy to record log events to a logger for given name.
 * @param value the type
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ByLiteral(val value: String)
