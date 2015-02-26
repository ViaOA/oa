package com.viaoa.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import com.viaoa.OAUnitTest;
import static org.junit.Assert.*;

public class OACircularQueueTest extends OAUnitTest {
    private OACircularQueue<Integer> que;
    private volatile boolean bStopWriter;
    private volatile boolean bStopReader;
    private AtomicInteger ai = new AtomicInteger();
    private final Object lock = new Object();
    
    public OACircularQueueTest() {
        que = new OACircularQueue<Integer>(10000) {};
        que.setName("testQueue");
    }
    
    void runReader(int id) {
        long pos = que.registerSession(id);
        
        if (id == 0) {
            que.setPaceSessionId(0);
        }
        
        System.out.println("start reader."+id+", que pos="+pos);
        for (int i=0; !bStopReader;i++) {
            try {
                Integer[] ints = que.getMessages(id, pos, 250, 10);
                if (ints == null) continue;
                
                for (Integer val : ints) {
                    assertNotNull(val);
                    assertEquals(val.intValue(), pos++);
                }
                if (id == 0 && !bStopWriter) {
                    Thread.sleep(25);  // so that writers will have to wait
                }
                
                //if ((i%20)==0) System.out.printf("[R"+id+"."+pos+"] ");
                // Thread.sleep(32);
            }
            catch (Exception e) {
                System.out.println("sessionId="+id+", runReader Exception: "+e);
                e.printStackTrace();
                break;
            }
        }
        System.out.println("end reader."+id+", que pos="+pos);
    }

    void runWriter(int id) {
        System.out.println("start writer."+id);
        int cnt = 0;
        for (int i=0; !bStopWriter; i++) {
            synchronized (lock) {
                int x = ai.getAndIncrement();
                que.addMessageToQueue(x);
                cnt++;
            }
            if ((i%100000)==0) {
                //if ((i%1000)==0) System.out.printf("\n(W."+i+") ");
                try {
                    Thread.sleep(25);
                }
                catch (Exception e) {}
            }
        }
        System.out.println("end writer."+id+", total queued="+cnt);
    }
    
    @Test
    public void runTests() throws Exception {
        for (int i=0; i<5; i++) {
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
        Thread.sleep(300);
        
        for (int i=0; i<5; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    runWriter(id);
                }
            });
            t.setName("Writer."+i);
            t.start();
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
        bStopWriter = true;
        Thread.sleep(20);
        bStopReader = true;
        assertTrue(que.getHeadPostion() > 100);
        Thread.sleep(500);
        System.out.println("que.getHeadPostion="+que.getHeadPostion());
        System.out.println("done");
    }
}
