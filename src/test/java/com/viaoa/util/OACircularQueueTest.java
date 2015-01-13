package com.viaoa.util;

import org.junit.Test;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

public class OACircularQueueTest extends OAUnitTest {
    private OACircularQueue<Short> que;
    private volatile boolean bStop;
    
    public OACircularQueueTest() {
        que = new OACircularQueue<Short>(1000) {};
        que.setName("testQueue");
    }
    
    void runWriter() {
        for (int i=0; !bStop; i++) {
            Short st = new Short((short)i);
            que.addMessageToQueue(st);
            if ((i%100)==0) {
                if ((i%1000)==0) System.out.printf("\n(W."+i+") ");
                try {
                    Thread.sleep(1);
                }
                catch (Exception e) {}
            }
        }
    }
    
    void runReader(int id) {
        long pos = que.registerSession(id);
        for (int i=0; !bStop ;i++) {
            try {
                Short[] sms = que.getMessages(id, pos, 500, 2000);
                pos += sms == null ? 0 : sms.length;
                if ((i%20)==0) System.out.printf("[R"+id+"."+pos+"] ");
                // Thread.sleep(32);
            }
            catch (Exception e) {
                System.out.println("runReader Exception: "+e);
                e.printStackTrace();
                break;
            }
        }
    }
    
    @Test
    public void runTests() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                runWriter();
            }
        });
        t.setName("Writer");
        t.start();
        
        for (int i=0; i<10; i++) {
            final int id = i;
            Thread tx = new Thread(new Runnable() {
                @Override
                public void run() {
                    runReader(id);
                }
            });
            tx.setName("Reader."+i);
            tx.start();
        }
        
        for (int i=0; i<3; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
            if (que.getHeadPostion() > 1000) break;
        }
        bStop = true;
        assertTrue(que.getHeadPostion() > 100);
    }

    
}
