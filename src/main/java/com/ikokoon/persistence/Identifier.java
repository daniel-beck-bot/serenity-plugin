package com.ikokoon.persistence;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation marks a setter method for the identifier in a class. The identifier must be globally unique within the JVM.
 * 
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
@Target( { METHOD })
@Retention(RUNTIME)
public @interface Identifier {
}
