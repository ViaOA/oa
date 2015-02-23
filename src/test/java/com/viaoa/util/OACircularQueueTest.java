package com.viaoa.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

public class OACircularQueueTest extends OAUnitTest {
    private OACircularQueue<Integer> que;
    private volatile boolean bStop;
    private AtomicInteger ai = new AtomicInteger();
    private final Object lock = new Object();
    
    public OACircularQueueTest() {
        que = new OACircularQueue<Integer>(10000) {};
        que.setName("testQueue");
    }
    
    void runWriter(int id) {
        System.out.println("start writer."+id);
        int cnt = 0;
        for (int i=0; !bStop; i++) {
            synchronized (lock) {
                int x = ai.getAndIncrement();
                que.addMessageToQueue(x);
                cnt++;
            }
            if ((i%100)==0) {
                //if ((i%1000)==0) System.out.printf("\n(W."+i+") ");
                try {
                    Thread.sleep(25);
                }
                catch (Exception e) {}
            }
        }
        System.out.println("end writer."+id+", total queued="+cnt);
    }
    
    void runReader(int id) {
        long pos = que.registerSession(id);
        System.out.println("start reader."+id+", que pos="+pos);
        for (int i=0; !bStop ;i++) {
            try {
                Integer[] ints = que.getMessages(id, pos, 500, 10);
                if (ints == null) continue;
                
                for (int val : ints) {
                    assertEquals(val, pos++);
                }
                
                // pos += sms == null ? 0 : sms.length;
                //if ((i%20)==0) System.out.printf("[R"+id+"."+pos+"] ");
                // Thread.sleep(32);
            }
            catch (Exception e) {
                System.out.println("runReader Exception: "+e);
                e.printStackTrace();
                break;
            }
        }
        System.out.println("end reader."+id+", que pos="+pos);
    }
    
    @Test
    public void runTests() throws Exception {
        for (int i=0; i<10; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    runWriter(id);
                }
            });
            t.setName("Writer."+i);
            Thread.sleep(100);
            t.start();
        }
        for (int i=0; i<10; i++) {
            final int id = i;
            Thread tx = new Thread(new Runnable() {
                @Override
                public void run() {
                    runReader(id);
                }
            });
            tx.setName("Reader."+i);
            Thread.sleep(100);
            tx.start();
        }
        
        for (int i=0; i<5; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
            //if (que.getHeadPostion() > 1000) break;
        }
        System.out.println("stopping ..");
        bStop = true;
        assertTrue(que.getHeadPostion() > 100);
        Thread.sleep(200);
        System.out.println("que.getHeadPostion="+que.getHeadPostion());
        System.out.println("done");
    }
}
