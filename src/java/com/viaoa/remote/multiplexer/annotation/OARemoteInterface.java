package com.viaoa.remote.multiplexer.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Information about remote Interfaces.
 * Important:  this needs to be added to the Interface, not the Impl class.
 * @author vvia
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OARemoteInterface {
    
    /**
     * Used to have all methods sent async and have the return value
     * use a single queue to return the value.
     */
    boolean async() default false;
}
