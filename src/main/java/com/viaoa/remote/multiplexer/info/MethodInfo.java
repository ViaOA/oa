/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
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
}
