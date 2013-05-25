package com.theice.remote.multiplexer.annotation;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Remoting information about remote methods.
 * Important:  this annotation needs to be added to the Interface, not the Impl class.
 * @author vvia
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteMethod {
    
    // true if the return value should be compressed when it is transmitted
    boolean compressedReturnValue() default false;

    // true if return value should not be returned (async)
    boolean streaming() default false;
}
