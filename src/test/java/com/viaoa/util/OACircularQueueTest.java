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
    private int cntCleanup;
    private final Object lockWrite = new Object();

    void runReader(int id) {
        long pos = que.registerSession(id);
        
        String s = "";
        if (id == 0) s = " *** NOTE: this will be a slow reader, but will not get a queueOverrun";
        else if (id == 1) s = " *** NOTE: this reader will get a queueOverrun";
        
        System.out.println("start reader."+id+", que pos="+pos+s);
        for (int i=0; !bStopReader;i++) {
            try {
                Integer[] ints = que.getMessages(id, pos, 500, 10);
                if (ints == null) continue;
                
                if (pos < 499) {
                    // verify that they are in order
                    for (Integer val : ints) {
                        assertNotNull(val);
                        assertEquals(val.intValue(), pos++);
                    }
                }
                else pos += ints.length;
                
                if (id == 0 && !bStopWriter) {
                    Thread.sleep(5);  // so that writers will have to wait on reader#0
                }
                else if (id == 1 && !bStopWriter) {
//                    Thread.sleep(1200);  // let it overrun
                }
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
            int throttleAmt = 0;
            if (id == 0 && (i%25==0)) throttleAmt = 100;
            if (ai.get() < 501) {
                synchronized (lockWrite) {
                    int x = ai.getAndIncrement();
                    que.addMessageToQueue(x, throttleAmt);
                }
            }
            else que.addMessageToQueue(ai.getAndIncrement(), throttleAmt);
            cnt++;
        }
        System.out.println("end writer."+id+", total queued="+cnt);
    }
    
    @Test
    public void runTests() throws Exception {
        OALogUtil.consoleOnly(Level.FINE);

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
        Thread.sleep(50);
        bStopReader = true;
        assertTrue(cntCleanup > 5);
        assertTrue(que.getHeadPostion() > 100);
        Thread.sleep(250);
        System.out.println("que.getHeadPostion="+que.getHeadPostion());
        System.out.println("done");
    }
}
