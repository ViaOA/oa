package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class OARemoteThread extends Thread {
    
    final Object Lock = new Object();
    
    volatile RequestInfo requestInfo;
    volatile boolean startedNextThread;
    volatile boolean watingOnLock;
    volatile long msStartNextThread;
    volatile long msLastUsed;

    volatile boolean sendMessages;  // if false then events are not sent, since this is processing a message
    
    public OARemoteThread(Runnable r) {
        super(r);
    }
    public OARemoteThread() {
    }

    // note: this is overwritten to start a new thread
    public void startNextThread() {
        startedNextThread = true;
        msStartNextThread = System.currentTimeMillis();
    }
    public void setSendMessages(boolean b) {
        sendMessages = b;
    }
    public boolean getSendMessages() {
        return sendMessages;
    }
    public void setWaitingOnLock(boolean b) {
        watingOnLock = b;
    }
    public boolean isWaitingOnLock() {
        return watingOnLock;
    }
    
    
    public void reset() {
        sendMessages = false;
        startedNextThread = false;
        watingOnLock = false;
        msStartNextThread = 0l;
    }
    
}
