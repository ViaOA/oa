package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;

public class OAPoolTest extends OAUnitTest{
    final int maxPoolSize = 5;
    final int secondsToRun = 6;
    final int maxThreads = 5;
    OAPool<String> pool;
    final AtomicInteger aiCounter = new AtomicInteger(); 
    
    @Test
    public void test() throws Exception {
        final AtomicInteger aiCnt = new AtomicInteger(); 
        pool = new OAPool<String>(String.class, 3, maxPoolSize) {
            @Override
            protected void removed(String resource) {
            }
            @Override
            protected String create() {
                return "string." + aiCnt.incrementAndGet(); 
            }
        };

        assertEquals(0, pool.getCurrentUsed());
        assertEquals(0, pool.getCurrentSize());
        pool.loadMinimum();
        assertEquals(3, pool.getCurrentSize());
        assertEquals(3, aiCnt.get());
        assertEquals(0, pool.getCurrentUsed());

        String[] ss = new String[maxPoolSize];
        for (int i=0; i<maxPoolSize; i++) {
            ss[i] = pool.get();
        }
        
        assertEquals(maxPoolSize, pool.getCurrentUsed());
        assertEquals(maxPoolSize, pool.getCurrentSize());
        assertEquals(maxPoolSize, aiCnt.get());
        
        int i = 0;
        for (String s : ss) {
            pool.release(s);
            i++;
            assertEquals(maxPoolSize-i, pool.getCurrentUsed());
            assertEquals(maxPoolSize, pool.getCurrentSize());
            assertEquals(maxPoolSize, aiCnt.get());
        }
        for (i=0; i<maxPoolSize; i++) {
            ss[i] = pool.get();
            assertEquals(i+1, pool.getCurrentUsed());
            assertEquals(maxPoolSize, pool.getCurrentSize());
            assertEquals(maxPoolSize, aiCnt.get());
        }
    }
    
    @Test
    public void test2() throws Exception {
        final CyclicBarrier barrier = new CyclicBarrier(maxPoolSize);
        final CountDownLatch countDownLatch = new CountDownLatch(maxPoolSize);

        final AtomicInteger aiRemoved = new AtomicInteger(); 
        final AtomicInteger aiCreated = new AtomicInteger(); 
        
        pool = new OAPool<String>(String.class, 3, maxPoolSize) {
            @Override
            protected String create() {
                String s = "string." + aiCreated.incrementAndGet();
                return s;
            }
            @Override
            protected void removed(String resource) {
                aiRemoved.getAndIncrement();
            }
        };
        
        System.out.println("Running "+maxThreads+" threads to test for "+secondsToRun+" seconds");
        for (int i=0; i<maxThreads; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();  // wait for all threads to get to this point
                        _test2();
                    }
                    catch (Throwable e) {
                    }
                    finally {
                        countDownLatch.countDown();
                    }
                }
            });
            t.start();
        }

        // wait for all threads to finish        
        boolean b = countDownLatch.await(secondsToRun+5, TimeUnit.SECONDS);
        
        assertTrue(b);
        assertTrue(aiCounter.get() > (secondsToRun * 100));
        assertEquals(maxThreads, pool.getCurrentSize());
        assertEquals(maxThreads, aiCreated.get());
        assertEquals(0, aiRemoved.get());
        
        if (secondsToRun > 5) {
            Thread.sleep(500);
            String s = pool.get();
            assertEquals(maxThreads, aiCreated.get());
            assertEquals(5, pool.getCurrentSize());
            pool.release(s);
            assertEquals(4, pool.getCurrentSize());
            assertEquals(1, aiRemoved.get());
        }        
    }
    
    void _test2() {
        long msEnd = System.currentTimeMillis() + (secondsToRun * 1000);
        do {
            String s = pool.get();
            delay(25);
            pool.release(s);
            aiCounter.incrementAndGet();
        }
        while (System.currentTimeMillis() < msEnd);
    }
}
