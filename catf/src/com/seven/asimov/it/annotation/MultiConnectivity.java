package com.seven.asimov.it.annotation;

import java.lang.annotation.*;

/**
 * Marks the multi-connections integration test.
 */
@Documented
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiConnectivity {
}
