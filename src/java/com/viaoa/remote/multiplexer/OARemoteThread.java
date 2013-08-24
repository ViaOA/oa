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
package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class OARemoteThread extends Thread {
    
    final Object Lock = new Object();
    
    volatile RequestInfo requestInfo;
    volatile boolean startedNextThread;
    volatile boolean watingOnLock;
    volatile long msStartNextThread;
    volatile long msLastUsed;

    // volatile boolean sendMessages;  // if false then events are not sent, since this is processing a message
    
    private volatile int sendMessageCount;
    
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
        if (b) sendMessageCount++;
        else sendMessageCount--;
    }
    public boolean getSendMessages() {
        return sendMessageCount > 0;
    }
    public void setWaitingOnLock(boolean b) {
        watingOnLock = b;
    }
    public boolean isWaitingOnLock() {
        return watingOnLock;
    }
    
    
    public void reset() {
        // sendMessages = false;
        sendMessageCount = 0;
        startedNextThread = false;
        watingOnLock = false;
        msStartNextThread = 0l;
    }
    
}
