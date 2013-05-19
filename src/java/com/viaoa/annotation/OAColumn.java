package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to map properties to datasource columns
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OAColumn {
    String name() default "";
    int sqlType() default java.sql.Types.VARCHAR;
    int maxLength() default 0;
}
