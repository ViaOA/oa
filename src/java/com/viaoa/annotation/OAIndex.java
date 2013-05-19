package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines indexes used in datasource.
 * @author vvia
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAIndex {
    String name();
    OAIndexColumn[] columns();
}
