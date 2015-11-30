/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.remote.multiplexer.annotation;

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
public @interface OARemoteMethod {
    
    // true if the return value should be compressed when it is transmitted
    boolean compressedReturnValue() default false;

    // true if return value should not be returned
    boolean noReturnValue() default false;
    
    int timeoutSeconds() default 0;
    
    // if true and the return value is a remote object, then it will not use a queue when messaging (even if parent uses a msg queue)
    boolean dontUseQueueForReturnValue() default false;
    
    // do not use queue (even if parent uses a msg queue)
    boolean dontUseQueue() default false;
    
    // send return value using socket from queued messages
    boolean returnOnQueueSocket() default false;
}

