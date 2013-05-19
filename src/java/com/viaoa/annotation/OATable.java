package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a DataSource table used by an OAObject. 
 * @author vvia
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OATable {
    String name() default "";
    OAIndex[] indexes() default {};
}
