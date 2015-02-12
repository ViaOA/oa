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

import java.io.Serializable;
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

    // types of commands sent between Client and Server
    public static final byte CtoS_Command_RunMethod = 0;
qqqqqqqq changed to    
public static final byte CtoS_Command_SendAsyncRequest = 0;
public static final byte CtoS_Command_SendSyncRequest = 0;
    
    
    public static final byte CtoS_Command_GetLookupInfo = 1;
    public static final byte CtoS_Command_RemoveSessionBroadcastThread = 2;
    public static final byte CtoS_Command_GetBroadcastClass = 3;
qqqq change to SendAsyncResponse    
    public static final byte CtoS_Command_ReturningAsyncResponse = 4;

    public static final byte StoC_Command_CreateNewStoCSocket = 5;
    public static final byte StoC_Command_SendBroadcast = 6;
    public static final byte StoC_Command_SendAsyncResponse = 7;
    public static final byte StoC_Command_SendAsyncRequest = 8;
    public static final byte StoC_Command_SendSyncRequest = 8;
    
    public byte currentCommand;

    final public int cnt;
    public long msStart;
    public long nsStart; 
    public long nsRead; 
    public long nsWrite; 
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
    public boolean responseReturned;
    
    public volatile boolean processedByServer;  // flag set on server after it's processed    
    
    public RequestInfo() {
        this.cnt = aiCount.incrementAndGet();
    }
    
    public String toLogString() {
        String msg = String.format("%1$tm/%1$td|%1$tH:%1$tM:%1$tS.%1$tL", new Date(msStart));
        msg += "|" + connectionId;
        msg += "|" + bindName;
        
        if (method != null) {
            Class c = method.getDeclaringClass();
            String s;
            if (c != null) {
                s = c.getName();
                int x = s.lastIndexOf('.');
                if (x > 0) s = s.substring(x+1);
            }
            else s = "";
            msg += "|" + s;
            msg += "|" + method.getName();
        }
        else {
            msg += "|";
            msg += "|";
        }
        msg += "|" + nsRead;
        msg += "|" + nsWrite;
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
        msg += "|Object";
        msg += "|Method";
        msg += "|nsRead";
        msg += "|nsWrite";
        msg += "|nsTime";
        msg += "[|exception]";
        return msg;
    }
    
}
