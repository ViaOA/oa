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
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.comm.multiplexer.io.VirtualSocket;

/**
 * This is used to track and capture information for each request.
 * @author vvia
 */
public class RequestInfo {
    private final static AtomicInteger aiCount = new AtomicInteger();


    public static Type getType(int val) {
        Type[] types = Type.values();
        if (val >= 0 && val < types.length) return types[val];
        return null;
    }
    
    public enum Type {
        CtoS_GetLookupInfo(false, true),
        CtoS_RemoveSessionBroadcastThread (false, false),
        CtoS_GetBroadcastClass(false, true),
        
        CtoS_SocketRequest(false, true),
        CtoS_SocketRequestNoResponse(false, false),
        
        CtoS_QueuedRequest(true, true),
        CtoS_QueuedRequestNoResponse(true, false),
        CtoS_QueuedResponse(true, false), // client returning result from stoc_queuedRequest
        
        CtoS_QueuedBroadcast(true, true),          // will return to client, once it is processed (not invoked) on the server

        StoC_CreateNewStoCSocket(false, false),
        StoC_QueuedBroadcast(true, false),
        
        StoC_QueuedRequest(true, true),   // server calling remote method on client, and get queued response CtoS_ResponseForQueuedRequest
        StoC_QueuedRequestNoResponse(true, false),  
        StoC_SocketRequest(false, true),     // send request on socket.output and get result from socket.input
        StoC_SocketRequestNoResponse(false, false),
        StoC_QueuedResponse(true, false);
        
        
        Type(boolean usesQueue, boolean hasReturnValue) {
            this.usesQueue = usesQueue;
            this.hasReturnValue = hasReturnValue;
        }
        private final boolean usesQueue;
        private final boolean hasReturnValue;
        
        public boolean usesQueue() {
            return this.usesQueue;
        }
        public boolean hasReturnValue() {
            return this.hasReturnValue;
        }
    }
    
    public Type type;

    final public int cnt;
    public long msStart;
    public long nsStart; 
    public long nsEnd; 

    public BindInfo bind;
    public VirtualSocket socket;
    public int connectionId;
    public int messageId;
    public int vsocketId;
    public int threadId;  // if StoC, then the Thread #
    
    public String bindName;
    public Object object;  // object that is being invoked 
    public Method method;
    public String methodNameSignature;  // unique name for method, so that method overloading can be supported.
    public MethodInfo methodInfo;
    public Object[] args;
    public boolean bSent;  // false if a local call, ex: "hashCode(), toString(), etc"
    
    public String responseBindName;
    public boolean responseBindUsesQueue;
    public Exception exception;
    public String exceptionMessage;
    public Object response;

    public volatile boolean methodInvoked;  // set to true with the method has been invoked
    public volatile boolean processedByServerQueue;  // flag set on server after it's processed    
    
    public RequestInfo() {
        this.cnt = aiCount.incrementAndGet();
    }

    public String toLogString() {
        String msg = String.format("%1$tm/%1$td|%1$tH:%1$tM:%1$tS.%1$tL", new Date(msStart));
        msg += "|" + connectionId;
        msg += "|" + bindName;
        msg += "|" + type;

        if (method == null && methodInfo != null) {
            method = methodInfo.method;
        }
        
        if (method != null) {
            Class c = method.getDeclaringClass();
            String s;
            if (c != null) {
                s = c.getSimpleName();
            }
            else s = "";
            msg += "|" + s;
            msg += "|" + method.getName();
        }
        else {
            msg += "|";
            msg += "|";
        }
        msg += "|" + (nsEnd-nsStart);
                
        if (exception != null) {
            msg += "|"+exception;
        }
        else if (exceptionMessage != null) {
            msg += "|"+exceptionMessage;
        }
        return msg;
    }
    
    public static String getLogHeader() {
        String msg = "Date|Time";
        msg += "|ConnectionId";
        msg += "|BindName";
        msg += "|Type";
        msg += "|Object";
        msg += "|Method";
        msg += "|nsRead";
        msg += "[|exception]";
        return msg;
    }
    
}
