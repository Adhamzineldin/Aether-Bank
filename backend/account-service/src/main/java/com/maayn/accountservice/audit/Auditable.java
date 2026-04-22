package com.maayn.accountservice.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as audit-emitting. The {@link AuditableAspect} will
 * publish a SUCCESS event when the method returns normally and a FAILED event
 * when it throws — both via {@link AuditPublisher} to the central audit
 * exchange.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String value() default "";
}

