package com.theice.remote.multiplexer.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Information about remote methods parameters.
 * Important:  this annotation needs to be added to the Interface, not the Impl class.
 * @author vvia
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME) 
public @interface RemoteParameter {
    
    // true if the param should be compressed when it is transmitted
    boolean compressed() default false;
}
