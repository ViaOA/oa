package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to map properties to datasource pkeys
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAId {
    boolean autoAssign() default true;
    boolean guid() default false;
    int pos() default 0;  // for multiple-part keys
}

