package com.viaoa.remote.multiplexer.info;

import java.lang.reflect.Method;

/**
 * Internal information used for each remote method.
 * @author vvia
 */
public class MethodInfo {
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
    
    // true if dont wait for return value (void methods)
    public boolean noReturnValue;

    // 20130605 add to async queue            
    public boolean useAsyncQueue;
}
