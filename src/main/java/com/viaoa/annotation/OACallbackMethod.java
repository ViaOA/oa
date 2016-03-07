/*  Copyright 1999-2016 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes an OAObject callback method.
 * 
 * 
 * method signature will be:  
 *      public void callbackName(OAObject fromObject, String propPathFromThis, Object oldValue, Object newValue) 
 *      
 *  example:   
 *     @OACallbackMethod(onlyUseLoadedData=true, runOnServer=true, runInBackgroundThread=true, properties= {"test"})
 *
 *  Note: the callback method is not called if hub.isfetching, or oaobj.isLoading
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface OACallbackMethod {

    /** 
     * Property paths that will automatically call this method when the propPath is changed.
     */
    String[] properties() default {};
    
    
    /**
     * if true (default), then callbacks are only made on objects that are in memory.
     */
    boolean onlyUseLoadedData() default true;
    
    /**
     * if true (default), then only run on the server.
     */
    boolean runOnServer() default true;
    
    /**
     * If true, then this will be ran in another thread. Otherwise false (default), run in the current thread.
     */
    boolean runInBackgroundThread() default false;
}
