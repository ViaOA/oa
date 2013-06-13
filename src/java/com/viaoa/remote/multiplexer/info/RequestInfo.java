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
    public Exception exception;
    public String exceptionMessage;
    public Object response;
    public boolean responseReturned;
    
    public volatile boolean processedByServer; // flag set on server when it has invoked    
    
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
