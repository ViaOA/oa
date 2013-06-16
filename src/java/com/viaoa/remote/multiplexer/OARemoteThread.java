package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class OARemoteThread extends Thread {
    
    final Object Lock = new Object();
    
    volatile RequestInfo requestInfo;
    volatile boolean startedNextThread;
    volatile boolean watingOnLock;
    
    volatile boolean sendMessages;  // if false then events are not sent, sinc this is processing a message

    public OARemoteThread(Runnable r) {
        super(r);
    }
    public OARemoteThread() {
    }

    public void startNextThread() {
        startedNextThread = true;
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
    }
    
}
