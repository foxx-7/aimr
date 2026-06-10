package com.aimr.notify.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to trigger cross-tenant security validation.
 * The AOP aspect will ensure the resource being accessed belongs to the current authenticated tenant.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateTenant {
}
