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
package com.viaoa.remote.multiplexer;

import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.info.RequestInfo;

/**
 * Thread that is used to process all remote method calls.
 * @author vvia
 */
public class OARemoteThread extends Thread {
    private static Logger LOG = Logger.getLogger(OARemoteThread.class.getName());
    
    final Object Lock = new Object();
    private static int warningMsgCnt;
    
    volatile boolean stopCalled;
    volatile RequestInfo requestInfo;
    volatile boolean startedNextThread;
    volatile boolean watingOnLock;
    volatile long msStartNextThread;
    volatile long msLastUsed;

    // volatile boolean sendMessages;  // if false then events are not sent, since this is processing a message
    
    private volatile int sendMessageCount;
    
    public OARemoteThread() {
    }
    public OARemoteThread(Runnable r) {
        super(r);
    }
    
    // flag to know if runnable is able to be handled by another thread(pool)
    private boolean bAllowRunnable;
    
    /**
     * Flag to know if the addRunnable was overwritten to handle in another thread (pool).
     * Otherwise, calling addRunnable will call the run() method directly.
     * @return default is false
     */
    public void setAllowRunnable(boolean b) {
        bAllowRunnable = b;
    }
    public boolean getAllowRunnable() {
        return bAllowRunnable;
    }
    
    /**
     * By default, this will call r.run().
     * This can be overwritten to have it ran in another way.
     */
    public void addRunnable(Runnable r) {
        if (r == null) return;
        r.run();
    }
    
    // note: this is overwritten to start a new thread
    public void startNextThread() {
        startedNextThread = true;
        msStartNextThread = System.currentTimeMillis();
    }
    public boolean startedNextThread() {
        return startedNextThread;
    }
    
    /**
     * Any OAsync changes (OAObject/Hub) that are made within an OARemoteThread will not be broadcast to
     * other computers, since they will also be processing the same originating message.
     * 
     * This can be set to true, so that any OASync changes will be sent out.  This is useful when 
     * an event listener will only run on the server, and any sync changes will then be sent to others. 
     * False by default.
     */
    public void setSendMessages(boolean b) {
        if (b) sendMessageCount++;
        else sendMessageCount--;
    }
    public boolean getSendMessages() {
        return sendMessageCount > 0;
    }
    /**
     * Flag to know if this thread is waiting on a lock set by OATreadLocalDelegate.
     * @param b
     */
    public void setWaitingOnLock(boolean b) {
        watingOnLock = b;
    }
    public boolean isWaitingOnLock() {
        return watingOnLock;
    }

    /**
     * called before processing the next OASync message.
     */
    public void reset() {
        // sendMessages = false;
        sendMessageCount = 0;
        startedNextThread = false;
        watingOnLock = false;
        msStartNextThread = 0l;
    }
}
