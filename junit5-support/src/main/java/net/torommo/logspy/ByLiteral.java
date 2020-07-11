package net.torommo.logspy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures a log spy to record log events to a logger for given name.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ByLiteral {
    /**
     * The name.
     *
     * @return The name.
     */
    String value();
}
