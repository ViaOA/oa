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
package com.viaoa.remote.multiplexer.info;

import java.lang.reflect.Method;

/**
 * Internal information used for each remote method.
 * @author vvia
 */
public class MethodInfo {
    
    public MethodInfo() {
        
    }
    
    public Method method;
    // unique name based on methodName and params
    public String methodNameSignature;

    // if return value is a remote object
    public Class remoteReturn;
    // flag to know if return value should be compressed 
    public boolean compressedReturn;
    
    // if any of the params are remote object
    public Class[] remoteParams;
    
    public boolean[] compressedParams;
    public boolean[] dontUseQueues;
    
    // true if dont wait for return value (void methods)
    public boolean noReturnValue;

    public boolean dontUseQueueForReturnValue;
    
    public boolean returnOnQueueSocket;
    
    public boolean dontUseQueue;
    
    public int timeoutSeconds = 0;
}
