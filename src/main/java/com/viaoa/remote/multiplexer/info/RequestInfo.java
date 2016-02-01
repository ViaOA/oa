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
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.comm.multiplexer.io.VirtualSocket;

/**
 * This is used to track and capture information for each sync request that is being processed.
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
        
        // send on socket and have it returned on the same socket that the queue uses.
        CtoS_ReturnOnQueueSocket(true, true, true),
        
        CtoS_QueuedRequest(true, true),
        CtoS_QueuedRequestNoResponse(true, false),
        CtoS_QueuedResponse(true, false), // client returning result from stoc_queuedRequest
        
        CtoS_QueuedBroadcast(true, true),   // will return to client, once it is processed (not invoked) on the server

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
            this.bReturnOnQueueThread = false;
        }
        Type(boolean usesQueue, boolean hasReturnValue, boolean bReturnOnQueueThread) {
            this.usesQueue = usesQueue;
            this.hasReturnValue = hasReturnValue;
            this.bReturnOnQueueThread = bReturnOnQueueThread;
        }
        private final boolean usesQueue;
        private final boolean hasReturnValue;
        private final boolean bReturnOnQueueThread;
        
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
//    public boolean isFromRemoteThread; // know if the thread making the remote call is a remoteThread
    
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
                
        if (exception != null) {
            msg += "|"+exception;
        }
        else if (exceptionMessage != null) {
            msg += "|"+exceptionMessage;
        }
        else {
            msg += "|";
        }
        msg += "|";
        
        if (method == null) return msg;
        Class[] cs = method.getParameterTypes();

        if (cs == null || cs.length == 0) return msg;
        
        int i = 0;
        for (Class c : cs) {
            String s;
            if (args != null && args.length > i) {
                Object obj = args[i];
                if (obj == null) s = "";
                else {
                    if (obj instanceof Class) {
                        s = ((Class) obj).getSimpleName();
                    }
                    else {
                        s = obj.toString();
                        if (s.length() > 30) s = s.substring(0,28)+"..";
                    }
                }
            }
            else s = "";
            if (i > 0) msg += "|";
            msg += "["+i+"]="+s;
            i++;
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
        msg += "|exception";
        msg += "|arguments";
        return msg;
    }
    
}
