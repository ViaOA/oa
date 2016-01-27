package com.viaoa.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.viaoa.OAUnitTest;
import static org.junit.Assert.*;

/**
 * 
 * @author vvia
 *
 */
public class OACircularQueueTest extends OAUnitTest {
    private OACircularQueue<Integer> que;
    private volatile boolean bStopWriter;
    private volatile boolean bStopReader;
    private AtomicInteger ai = new AtomicInteger();
    private final Object lock = new Object();
    private int cntCleanup;

    
    void runReader(int id) {
        long pos = que.registerSession(id, 0);
        
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
                    Thread.sleep(15);  // so that writers will have to wait on reader#0
                }
                else if (id == 1 && !bStopWriter) {
                    Thread.sleep(1600);  // let it overrun
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
        OALogUtil.consoleOnly(Level.FINER);

        que = new OACircularQueue<Integer>(1000) {
            @Override
            protected void cleanupQueue() {
                ++cntCleanup;
                // System.out.println(cntCleanup+") cleanupQueue() called");
                super.cleanupQueue();
            }
        };
        que.setName("testQueue");
        
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

        
        for (int i=0; i<8; i++) {
            Thread.sleep(1000);
            //if (que.getHeadPostion() > 1000) break;
        }
        System.out.println("stopping ..");
        bStopWriter = true;
        Thread.sleep(20);
        bStopReader = true;
        assertTrue(cntCleanup > 5);
        assertTrue(que.getHeadPostion() > 100);
        Thread.sleep(50);
        System.out.println("que.getHeadPostion="+que.getHeadPostion());
        System.out.println("done");
    }
}
