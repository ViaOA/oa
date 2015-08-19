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
package com.viaoa.util;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Thread safe Circular Queue.
 * There is a single headPostion, and each consumer can have  a tailPosition.
 * Client access (getMessageXxx methods) will check overruns, and will throw an 
 * exception if a queue overrun occurs.
 * 
 * A session id can be used so that entries can be removed from the queue once all sessions have received it.
 * 
 * @author vvia
 * Note: this is made abstract to be able to get the Generic class that is used.
 */

// todo: option to registerSession as a "leader", so that it will never have a queue overrun.  A call to add when then have to 
//         make sure that the leader is not overran, and must wait, etc.

public abstract class OACircularQueue<TYPE> {
    private static Logger LOG = Logger.getLogger(OACircularQueue.class.getName());
    
    private volatile int queueSize;
    private final Object LOCKQueue = new Object();
    
    private volatile TYPE[] msgQueue;
    private String name;

    /** running value that keeps next position to insert a message.
     *  Uses module queueSize to determine the array position.  
    */
    private volatile long queueHeadPosition;  

    // last position that a registered session has used. All previous positions can be set to null
    private volatile long lastUsedPos;

    private volatile int waitingOnSessionId = -1;
    private volatile boolean bWaitingToGet;

    private Class<TYPE> classType;

    private ArrayList<Integer> alSession;
    private ConcurrentHashMap<Integer, Long> hmSessionPosition;
    private ConcurrentHashMap<Integer, Long> hmSessionLastTime;
    
    
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
     * This is used to let the queue know who the consumers are, so that 
     * queue slots can be set to null.
     */
    public long registerSession(int sessionId) {
        if (alSession == null) {
            synchronized(LOCKQueue) {
                if (alSession == null) alSession = new ArrayList<Integer>();
            }
        }
        if (!alSession.contains(sessionId)) alSession.add(sessionId);

        if (hmSessionPosition == null || hmSessionLastTime == null) {
            synchronized(LOCKQueue) {
                if (hmSessionPosition == null) hmSessionPosition = new ConcurrentHashMap<Integer, Long>();
                if (hmSessionLastTime == null) hmSessionLastTime = new ConcurrentHashMap<Integer, Long>();
            }
        }
        long x = queueHeadPosition;
        hmSessionPosition.put(sessionId, x);
        hmSessionLastTime.put(sessionId, System.currentTimeMillis());
        
        return x;
    }
    public void unregisterSession(int sessionId) {
        if (hmSessionPosition != null) {
            hmSessionPosition.remove(sessionId);
        }
        if (alSession != null) {
            alSession.remove(new Integer(sessionId));
        }
    }
    
    // 20141208 null out queue slots, so that they can be GC'd
    //   this is called from a sync block
    
    private void cleanupQueue() {
        if (hmSessionPosition == null) return; // no session registered
        if (queueHeadPosition < 1) return;
        long pos = queueHeadPosition-1;
        boolean bFoundOne = false;
        for (Map.Entry<Integer, Long> entry : hmSessionPosition.entrySet()) {
            long x = entry.getValue();
            if (x < (queueHeadPosition - queueSize)) {
                continue; // overflow
            }
            bFoundOne = true;
            if (x < pos) pos = x; 
        }
        if (bFoundOne && lastUsedPos < pos) {
            lastUsedPos = Math.max(queueHeadPosition-queueSize, lastUsedPos);
            for (long i=lastUsedPos; i<pos; i++) {
                msgQueue[(int)(i % queueSize)] = null;
            }
            lastUsedPos = pos;
        }
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
     * @return position of message in the queue
     */
    public int addMessageToQueue(TYPE msg) {
        return addMessage(msg);
    }
    
    
    public int addMessage(TYPE msg) {
        synchronized(LOCKQueue) {
            
            
            if (alSession != null) {
                // wait up to 1 second for any slow consumer
                for (int i=0; i<10; i++) {
                    int sessionIdFound = -1;
                    for (int id : alSession) {
                        Object obj = hmSessionPosition.get(id);
                        if (obj == null) continue;
                        long x = ((Long) obj).longValue();
                        if ( (x + queueSize) > queueHeadPosition) continue;
                        
                        obj = hmSessionLastTime.get(id);
                        if (obj == null) continue;
                        long ts = ((Long) obj).longValue();
                        long tsNow = System.currentTimeMillis();
                        if (ts + 1000 < tsNow) continue;
                        sessionIdFound = id;
                        break;
                    }
                    if (sessionIdFound < 0) break;
                    waitingOnSessionId = sessionIdFound;
                    try {
                        LOCKQueue.wait(100);
                    }
                    catch (Exception e) {
                    }
                    finally {
                        if (sessionIdFound == waitingOnSessionId) {
                            waitingOnSessionId = -1;
                        }
                    }
                }
            }
            
            int posHead = (int) (queueHeadPosition++ % queueSize);
            
            if (queueHeadPosition < 0) {
                queueHeadPosition = posHead + 1;
            }
            msgQueue[posHead] = msg;
            if (bWaitingToGet) {
                bWaitingToGet = false;
                LOCKQueue.notifyAll();
            }
            
            return posHead;
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
    public TYPE[] getMessages(final int sessionId, final long posTail, final int maxReturnAmount, int maxWait) throws Exception {

        TYPE[] msgs = null;
        for ( ;; ) {
            msgs =  _getMessages(posTail, maxReturnAmount, maxWait);
            if (hmSessionLastTime != null) {
                hmSessionLastTime.put(sessionId, System.currentTimeMillis());
            }

            if (msgs != null || maxWait == 0) {
                break;
            }
            // else it waited until a message was available and then returned w/o message
            //   or waited maxWait and then returned.
            // ... need to loop again w/o a wait to get any added message(s)
            maxWait = 0;
        }

        if (msgs != null && msgs.length > 0) {
            if (hmSessionPosition != null) hmSessionPosition.put(sessionId, posTail+msgs.length);
            
            if (waitingOnSessionId == sessionId) {
                synchronized(LOCKQueue) {
                    LOCKQueue.notifyAll();
                }            
            }
        }        
        return msgs;
    }
    
    private int cleanupCnt;
    private TYPE[] _getMessages(long posTail, final int maxReturnAmount, final int maxWait) throws Exception {
        int amt;
        synchronized(LOCKQueue) {
            if ((posTail + queueSize) < queueHeadPosition) {
                throw new Exception("message queue overrun");
            }
            else {
                if (posTail > queueHeadPosition) {
                    posTail = queueHeadPosition;
                    //throw new IllegalArgumentException("posTail should not be larger then headPos");
                }
            }
            amt = (int) (queueHeadPosition - posTail);
            if (maxReturnAmount > 0 && amt > maxReturnAmount) {
                amt = maxReturnAmount;
            }
            
            if (amt == 0 && maxWait != 0) {
                // need to wait
                if (++cleanupCnt % 50 == 0) {
                    cleanupQueue();
                }
                bWaitingToGet = true;
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
    
    public void setName(String s) {
        this.name = s;
    }
    public String getName() {
        return name;
    }
    
}
