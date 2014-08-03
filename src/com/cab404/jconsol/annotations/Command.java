package com.cab404.jconsol.annotations;


import com.cab404.jconsol.ParameterConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command annotation.
 *
 * @author cab404
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String command();
    Class<? extends ParameterConverter>[] params() default {};
}
