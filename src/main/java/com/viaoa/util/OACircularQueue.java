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
import java.util.concurrent.atomic.AtomicInteger;
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

    private volatile boolean bWaitingToGet;

    private Class<TYPE> classType;

    private volatile ConcurrentHashMap<Integer, Session> hmSession;
    
    private static class Session {
        int id;
        long queuePos;
        int maxFallBehind;
        long msLastRead;
    }
    
    
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
    
    
    public long registerSession(int sessionId) {
        return registerSession(sessionId, 0);
    }
    /**
     * This is used to let the queue know who the consumers are, so that 
     * queue slots can be set to null once they are not needed.  
     * It is also used so that queue writers dont go too fast and overrun the readers.
     * @param sessionId identifier for the session
     * @param maxFallBehind max amount that it can fall behind the head, else an addMessage will wait for up to 1 second.
     * @return current queueHeadPosition
     */
    public long registerSession(int sessionId, int maxFallBehind) {
        Session session = new Session();
        session.id = sessionId;
        session.maxFallBehind = maxFallBehind;
        session.msLastRead = System.currentTimeMillis();
        
        if (hmSession == null) {
            synchronized(LOCKQueue) {
                if (hmSession == null) hmSession = new ConcurrentHashMap<Integer, Session>();
            }
        }
        long x = queueHeadPosition;
        session.queuePos = x;
        hmSession.put(session.id, session);
        return x;
    }
    public void unregisterSession(int sessionId) {
        if (hmSession != null) {
            hmSession.remove(sessionId);
        }
    }
    
    // 20141208 null out queue slots, so that they can be GC'd
    //   this is called from a sync block
    
    protected void cleanupQueue() {
        if (hmSession == null) return; // no session registered
        if (queueHeadPosition < 1) return;
        long pos = queueHeadPosition-1;
        boolean bFoundOne = false;
        for (Map.Entry<Integer, Session> entry : hmSession.entrySet()) {
            Session session = entry.getValue();
            if (session.queuePos < (queueHeadPosition - queueSize)) {
                continue; // overflow
            }
            bFoundOne = true;
            pos = Math.min(pos, session.queuePos); 
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
    
    private int cntQueueWait; // number of times a addMessage has called wait.    
    private long tsLastLog;

    public int addMessage(TYPE msg) {
        synchronized(LOCKQueue) {

            if (hmSession != null) {
                // wait up to 1 second for any slow consumer
                for (int i=0; i<20; i++) {
                    
                    Session sessionFound = null;
                    for (Map.Entry<Integer, Session> entry : hmSession.entrySet()) {
                        Session session = entry.getValue();
                        if (session.queuePos < (queueHeadPosition - queueSize)) {
                            continue; // overflow
                        }

                        // check to see if it is getting close to a queue overrun
                        if ( (session.queuePos + queueSize) > (queueHeadPosition + Math.min(100,(queueSize/10))) ) {
                            
                            if (session.maxFallBehind == 0) continue;
                            if ( (session.queuePos + session.maxFallBehind) > queueHeadPosition) {
                                continue;
                            }
                        }
                    
                        long ts = session.msLastRead;
                        long tsNow = System.currentTimeMillis();
                        if (ts + 1500 < tsNow) {
                            if (tsNow > tsLastLog + 1500) {
                                LOG.fine("session over 1.5 seconds getting last msg, queSize="+queueSize+
                                        ", currentHeadPos="+queueHeadPosition+", session="+session.id+", sessionPos="+session.queuePos);
                                tsLastLog = tsNow;
                            }
                            continue;  // too slow, dont wait for this one
                        }
                        sessionFound = session;
                        break;
                    }
                    if (sessionFound == null) {
                        break;
                    }
                    try {
                        LOCKQueue.wait(50);
                        ++cntQueueWait;
                        
                        long tsNow = System.currentTimeMillis();
                        if (tsNow > tsLastLog + 2500) {
                            LOG.fine("avoiding queue overrun, queSize="+queueSize+", queHeadPos="+queueHeadPosition+
                                    ", totalSessions="+hmSession.size() +
                                    ", slowSession="+sessionFound.id +
                                    ", qpos="+sessionFound.queuePos +
                                    ", totalWaits="+cntQueueWait);
                            tsLastLog = tsNow;
                        }
                    }
                    catch (Exception e) {
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

        Session session;
        if (hmSession != null) {
            session = hmSession.get(sessionId);
        }
        else session = null;
        
        for ( ;; ) {
            msgs =  _getMessages(posTail, maxReturnAmount, maxWait);
            if (session != null) session.msLastRead = System.currentTimeMillis();

            if (msgs != null || maxWait == 0) {
                break;
            }
            // else it waited until a message was available and then returned w/o message
            //   or waited maxWait and then returned.
            // ... need to loop again w/o a wait to get any added message(s)
            maxWait = 0;
        }

        if (msgs != null && msgs.length > 0) {
            if (session != null) session.queuePos = (posTail + msgs.length);
            
            /* 20160115 dont notify, it needs to wait and give slow thread time 
            if (waitingOnSessionId == sessionId) {
                synchronized(LOCKQueue) {
                    LOCKQueue.notifyAll();
                }            
            }
            */
        }        
        return msgs;
    }
    
    private AtomicInteger  aiCleanupQueue = new AtomicInteger(); 
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
                if (aiCleanupQueue.incrementAndGet() % 50 == 0) {
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
