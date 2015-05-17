package com.cab404.optionstuck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares key as key.
 * Created at 6:28 on 01.03.15
 *
 * @author cab404
 */

@TypeAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Declare {

}
