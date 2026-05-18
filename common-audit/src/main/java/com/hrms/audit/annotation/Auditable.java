package com.hrms.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an entity to be audited (INSERT/UPDATE/DELETE). Apply alongside
 * {@code @EntityListeners(AuditEntityListener.class)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Auditable {
    /** Logical name for the audit log (defaults to class simple name). */
    String value() default "";

    /** Comma-separated list of fields to redact (PII, passwords). */
    String redactFields() default "passwordHash,password,mfaSecret,token,refreshToken,otp";
}
