package com.viaoa.util;


import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;


/**
 * Basic CircularQueue, that is thread safe.
 * There is a single headPostion, and each consumer can have  a tailPosition.
 * Overruns are checked during getMessages, and will throw an exception if an overrun occurs. 
 * @author vvia
 */
public abstract class OACircularQueue<TYPE> {
    private static Logger LOG = Logger.getLogger(OACircularQueue.class.getName());
    
    private int queueSize;
    private Object LOCKQueue = new Object();
    
    private TYPE[] msgQueue;

    // next position to insert a message.  **Note: does not manage hitting long.maxValue
    private long queueHeadPosition;  

    private boolean queueWaitFlag;

    private Class<TYPE> classType;


    public OACircularQueue(Class clazz, int queueSize) {
        this.classType = clazz;
        setSize(queueSize);
    }    
    public OACircularQueue(int queueSize) {
        this();
        setSize(queueSize);
    }
    protected OACircularQueue() {
        Class c = getClass();
        for (; c != null;) {
            Type type = c.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                classType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                break;
            }
            c = c.getSuperclass();
        }
        LOG.fine("classType=" + classType);
        if (classType == null) {
            throw new RuntimeException("class must define <TYPE>, or use construture that accepts 'Class clazz'");
        }
    }
    public void setSize(int queueSize) {
        this.queueSize = queueSize;
        msgQueue = (TYPE[]) Array.newInstance(classType, queueSize);
    }
    
    
    public long getHeadPostion() {
        synchronized(LOCKQueue) {
            return queueHeadPosition;
        }
    }
    
    public void addMessageToQueue(TYPE msg) {
        synchronized(LOCKQueue) {
            int posHead = (int) (queueHeadPosition++ % queueSize);
            
            msgQueue[posHead] = msg;
            if (queueWaitFlag) {
                queueWaitFlag = false;
                LOCKQueue.notifyAll();
            }
        }
    }

    public TYPE[] getMessages(long posTail) throws Exception {
        return getMessages(posTail, -1);
    }
    
    public TYPE[] getMessages(long posTail, int maxReturnAmount) throws Exception {
        for (;;) {
            TYPE[] msgs =  _getMessages(posTail, maxReturnAmount, 0);
            if (msgs != null) {
                return msgs;
            }
        }
    }

    public TYPE[] getMessages(long posTail, int maxReturnAmount, int maxWait) throws Exception {
        for (;;) {
            TYPE[] msgs =  _getMessages(posTail, maxReturnAmount, maxWait);
            if (msgs != null || maxWait == 0) {
                return msgs;
            }
            maxWait = 0;
        }
    }
    
    private TYPE[] _getMessages(long posTail, int maxReturnAmount, int maxWait) throws Exception {
        TYPE[] msgs = null;

        synchronized(LOCKQueue) {
            if ((posTail + queueSize) <= queueHeadPosition) {
                throw new Exception("message queue overrun");
            }
            int amt = (int) (queueHeadPosition - posTail);
            if (maxReturnAmount > 0 && amt > maxReturnAmount) {
                amt = maxReturnAmount;
            }
            
            if (amt == 0 && maxWait == 0) {
                // no-op
            }
            else if (amt > 0) {
                msgs = (TYPE[]) Array.newInstance(classType, amt);
                for (int i=0; i<amt; i++) {
                    msgs[i] = msgQueue[ (int) (posTail++ % queueSize) ]; 
                }
            }
            else {
                // need to wait
                queueWaitFlag = true;
                try {
                    if (maxWait > 0) { 
                        LOCKQueue.wait(maxWait);
                    }
                    else {
                        LOCKQueue.wait();
                    }
                }
                catch (Exception e) {
                }
            }
        }
        return msgs;
    }
}
