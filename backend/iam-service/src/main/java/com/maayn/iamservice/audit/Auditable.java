package com.maayn.iamservice.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method as audit-emitting. The {@link AuditableAspect} will
 * publish a SUCCESS event when the method returns normally and a FAILED event
 * when it throws — both via {@link AuditPublisher} to the central audit
 * exchange.
 *
 * <p>Use for methods with a single conceptual outcome. Methods that need to
 * differentiate between several distinct failure modes (e.g. login: unknown
 * user vs. wrong password vs. locked) should keep using {@code AuditPublisher}
 * imperatively.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Action name (e.g. {@code REGISTER_USER}, {@code OPEN_ACCOUNT}). */
    String action();

    /** Optional human-readable description. Defaults to the method signature. */
    String value() default "";
}

