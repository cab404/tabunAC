package com.cab404.optionstuck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sorry for no comments!
 * Created at 6:31 on 01.03.15
 *
 * @author cab404
 */
@TypeAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Booleanize {
}
