package com.viaoa.remote;

import com.viaoa.cs.OAObjectMessage;
import com.viaoa.util.OACircularQueue;

public class OARemteModel implements OARemteModelInterface {
    private static int QueueSize = 15000;

    private OACircularQueue<OAObjectMessage> cque;
    
    public OARemteModel() {
        cque = new OACircularQueue<OAObjectMessage>(QueueSize) {
        };
    }

    @Override
    public void sendMessage(OAObjectMessage objMsg) {
        cque.addMessageToQueue(objMsg);
    }

    
    
    
}






