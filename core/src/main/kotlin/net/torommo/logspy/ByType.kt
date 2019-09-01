package net.torommo.logspy

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

/**
 * Configures a log spy to record log events to a logger for given type.
 * @param value the type
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@MustBeDocumented
annotation class ByType(val value: KClass<out Any>)
