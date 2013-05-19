package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define link tables used by M2M relationships.
 * @author vvia
  */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OALinkTable {
    String name();
    String[] columns();  // these match the pkey columns for the object that this is used in.
    String indexName();
}
