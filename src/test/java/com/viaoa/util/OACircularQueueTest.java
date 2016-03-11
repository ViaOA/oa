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
        if (id == 1) s = " *** NOTE: this will be a slow reader, but will not get a queueOverrun";
        else if (id == 2) s = " *** NOTE: this reader will get a queueOverrun";
        
        System.out.println("start reader."+id+", que pos="+pos+s);

        for (int i=0; !bStopReader;i++) {
            try {
                Integer[] ints = que.getMessages(id, pos, 10, 2000);
                if (ints == null) continue;
                
                if (pos < 499) {
                    // verify that they are in order
                    for (Integer val : ints) {
                        assertNotNull(val);
                        assertEquals(val.intValue(), pos);
                        ++pos;
                    }
                }
                else pos += ints.length;
                  
                if (id == 1 && !bStopWriter) {
                    Thread.sleep(3);  // so that writers will have to wait on reader#1
                }
                else if (id == 2 && !bStopWriter) {
                    Thread.sleep(1005);  // let it overrun
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
            throttleAmt = 350;
            // if (id == 0 && (i%25==0)) throttleAmt = 100;
            
            if (ai.get() < 600) {
                synchronized (lockWrite) {
                    int x = ai.getAndIncrement();
                    que.addMessageToQueue(x, throttleAmt);
                }
            }
            else {
                que.addMessageToQueue(ai.getAndIncrement(), throttleAmt, 0);
            }
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
                if (cntCleanup % 500 == 0) System.out.println(cntCleanup+") cleanupQueue() called, "+Thread.currentThread().getName());
                super.cleanupQueue();
            }
        };
        que.setName("testQueue");
        
        for (int i=0; i<3; i++) {
            final int id = i;
            Thread tx = new Thread(new Runnable() {
                @Override
                public void run() {
                    runReader(id+1);
                }
            });
            tx.setName("Reader."+(i+1));
            tx.start();
        }
        Thread.sleep(250);
        
        for (int i=0; i<10; i++) {
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

        
        for (int i=0; i<15; i++) {
            Thread.sleep(1000);
            //if (que.getHeadPostion() > 1000) break;
        }
        System.out.println("stopping ..");
        bStopWriter = true;
        Thread.sleep(150);
        bStopReader = true;
        assertTrue(cntCleanup > 0);
        assertTrue(que.getHeadPostion() > 100);
        Thread.sleep(650);
        System.out.println("que.getHeadPostion="+que.getHeadPostion());
        System.out.println("done");
    }
    
    public static void main(String[] args) throws Exception {
        OACircularQueueTest test = new OACircularQueueTest();
        test.runTests();
    }
}
