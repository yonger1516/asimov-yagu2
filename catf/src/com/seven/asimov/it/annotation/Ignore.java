package com.seven.asimov.it.annotation;

import java.lang.annotation.*;

/**
 * Marks a test that should be ignored.
 *
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
@Documented
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {

}
