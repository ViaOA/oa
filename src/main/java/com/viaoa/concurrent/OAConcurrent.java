package com.viaoa.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows multiple runnables to all start and run at the same time, and waits for them to all complete 
 * before returning.
 * @author vvia
 *
 */
public class OAConcurrent {
    private static Logger LOG = Logger.getLogger(OAConcurrent.class.getName());
    
    private CountDownLatch countDownLatch;
    private CyclicBarrier barrier;
    private Runnable[] runnables;
    
    public OAConcurrent(Runnable[] runnables) {
        this.runnables = runnables;
    }
    
    public void run() throws Exception {
        int max = (runnables == null) ? 0 : runnables.length;
        if (max == 0) return;
        
        countDownLatch = new CountDownLatch(max);
        barrier = new CyclicBarrier(max);
        
        for (int i=0; i<max; i++) {
            final int pos= i;
            Thread t = new Thread() {
                public void run() {
                    try {
                        barrier.await();
                        runnables[pos].run();
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "exception in OAThreadManager", e);
                    }
                    finally {
                        countDownLatch.countDown();
                    }
                }
            };
            t.setName("OAConcurrent."+pos);
            t.start();
        }

        countDownLatch.await();
    }
}
