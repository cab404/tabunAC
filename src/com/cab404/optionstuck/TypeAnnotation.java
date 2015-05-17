package com.cab404.optionstuck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates annotation, indicating it is a type annotation.
 * Created at 6:30 on 01.03.15
 *
 * @author cab404
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TypeAnnotation {
}
