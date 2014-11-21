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
package com.viaoa.util;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;

/**
 * Thread safe Circular Queue.
 * There is a single headPostion, and each consumer can have  a tailPosition.
 * Client access (getMessageXxx methods) will check overruns, and will throw an 
 * exception if a queue overrun occurs. 
 * @author vvia
 */
public abstract class OACircularQueue<TYPE> {
    private static Logger LOG = Logger.getLogger(OACircularQueue.class.getName());
    
    private volatile int queueSize;
    private final Object LOCKQueue = new Object();
    
    private volatile TYPE[] msgQueue;

    /** running value that keeps next position to insert a message.
     *  Uses module queueSize to determine the array position.  
    */
    private volatile long queueHeadPosition;  

    // flag to know if there are threads waiting to get a message
    private volatile boolean queueWaitFlag;

    private Class<TYPE> classType;

    /**
     * Create a new circular queue. 
     * @param queueSize actual size of the array that backs the queue.
     */
    public OACircularQueue(int queueSize) {
        this();
        setSize(queueSize);
    }
    public OACircularQueue(Class clazz, int queueSize) {
        this.classType = clazz;
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
    /**
     * set the size of the array behind the circular queue 
     */
    public void setSize(int queueSize) {
        synchronized(LOCKQueue) {
            this.queueSize = queueSize;
            msgQueue = (TYPE[]) Array.newInstance(classType, queueSize);
        }
    }
    public int getSize() {
        return queueSize;
    }
    
    
    /**
     * current position where the next message will be added. 
     */
    public long getHeadPostion() {
        synchronized(LOCKQueue) {
            return queueHeadPosition;
        }
    }
    
    /**
     * Add a new message to the queue.
     */
    public void addMessageToQueue(TYPE msg) {
        addMessage(msg);
    }
    public void addMessage(TYPE msg) {
        synchronized(LOCKQueue) {
            int posHead = (int) (queueHeadPosition++ % queueSize);
/*qqqqqqqqqqqqqqqqqqqqqq            
if (queueHeadPosition % 1000 == 0) {
    System.out.println("OACircularQueue position="+queueHeadPosition);
}
*/
            if (queueHeadPosition < 0) {
                queueHeadPosition = posHead + 1;
            }
            msgQueue[posHead] = msg;
            if (queueWaitFlag) {
                queueWaitFlag = false;
                LOCKQueue.notifyAll();
            }
        }
    }
    
    public final int WaitUntilNotified = -1;
    
    /**
     * will block until a message is available.
     */
    public TYPE getMessage(long posTail) throws Exception {
        TYPE[] vals = getMessages(posTail, 1, WaitUntilNotified);
        return vals[0];
    }
    /**
     * Get next message, with a timeout
     * @param posTail current position to pull messages from
     * @param maxWait max number of milliseconds to wait
     */
    public TYPE getMessage(long posTail, int maxWait) throws Exception {
        TYPE[] vals = getMessages(posTail, 1, maxWait);
        if (vals == null || vals.length == 0) return null;
        return vals[0];
    }
    public int getAmountAvailable(long posTail) throws Exception {
        int amt;
        synchronized(LOCKQueue) {
            if ((posTail + queueSize) <= queueHeadPosition) {
                throw new Exception("message queue overrun");
            }
            amt = (int) (queueHeadPosition - posTail);
        }
        return amt;
    }
    
    /**
     * will block until at least one message is available.
     */
    public TYPE[] getMessages(long posTail) throws Exception {
        return getMessages(posTail, 0, WaitUntilNotified);
    }
    
    /**
     * will block until at least one message is available.
     */
    public TYPE[] getMessages(long posTail, int maxReturnAmount) throws Exception {
        return getMessages(posTail, maxReturnAmount, WaitUntilNotified);
    }

    /**
     * @param posTail current position to use to get next message
     * @param maxReturnAmount 
     * @param maxWait if no messages are available, wait this amount of miliseconds for an available message.
     */
    public TYPE[] getMessages(long posTail, int maxReturnAmount, int maxWait) throws Exception {
        TYPE[] msgs = null;
        for ( ;; ) {
            msgs =  _getMessages(posTail, maxReturnAmount, maxWait);

            if (msgs != null || maxWait == 0) {
                break;
            }
            // else it waited until a message was available and then returned w/o message
            //   or waited maxWait and then returned.
            // ... need to loop again w/o a wait to get any added message(s)
            maxWait = 0;
        }
        return msgs;
    }
    
    private TYPE[] _getMessages(long posTail, final int maxReturnAmount, final int maxWait) throws Exception {
        int amt;
        synchronized(LOCKQueue) {
            if ((posTail + queueSize) <= queueHeadPosition) {
                throw new Exception("message queue overrun");
            }
            else {
                if (posTail > queueHeadPosition) {
                    throw new IllegalArgumentException("posTail should not be larger then headPos");
                }
            }
            amt = (int) (queueHeadPosition - posTail);
            if (maxReturnAmount > 0 && amt > maxReturnAmount) {
                amt = maxReturnAmount;
            }
            
            if (amt == 0 && maxWait != 0) {
                // need to wait
                queueWaitFlag = true;
                if (maxWait > 0) { 
                    LOCKQueue.wait(maxWait);
                }
                else {
                    for (;;) {
                        LOCKQueue.wait();
                        if (posTail != queueHeadPosition) break; // protect from spurious wakeup (yes, it happens)
                    }
                }
            }
        }
        TYPE[] msgs;
        if (amt > 0) {
            msgs = (TYPE[]) Array.newInstance(classType, amt);
            for (int i=0; i<amt; i++) {
                msgs[i] = msgQueue[ (int) (posTail++ % queueSize) ]; 
            }
        }
        else msgs = null;
        return msgs;
    }

    /**
     * Get message at actual position in queue
     * @param pos is actual array position, must be less then queSize, else null is returned. 
     * @return
     */
    public TYPE getMessagesAtPos(int pos) {
        if (pos < 0 || pos >= msgQueue.length) return null;
        TYPE x = msgQueue[pos];
        return x;
    }   
}
