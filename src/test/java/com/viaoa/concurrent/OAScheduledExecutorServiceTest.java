package com.viaoa.concurrent;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.junit.Test;

import com.viaoa.util.OADateTime;
import com.viaoa.util.OATime;

public class OAScheduledExecutorServiceTest {

    @Test
    public void test() throws Exception {
        final AtomicInteger ai = new AtomicInteger();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ai.incrementAndGet();
            }
        };
        
        OAScheduledExecutorService es = new OAScheduledExecutorService();
        OADateTime dt = new OADateTime();
        Thread.sleep(5);
        ScheduledFuture f = es.schedule(r, dt);
        long x = f.getDelay(TimeUnit.MILLISECONDS);
        
        assertTrue(x <= 0);
        
        for ( ;!f.isDone(); ) {
            Thread.sleep(5);
        }
        
        assertEquals(1, ai.get());
    }

    @Test
    public void testA() throws Exception {
        final AtomicInteger ai = new AtomicInteger();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ai.incrementAndGet();
            }
        };
        
        OAScheduledExecutorService es = new OAScheduledExecutorService();
        OADateTime dt = new OADateTime();
        int delay = 5;
        dt = dt.addSeconds(delay);

        long ms = System.currentTimeMillis();
        ScheduledFuture f = es.schedule(r, dt);
        long x = f.getDelay(TimeUnit.SECONDS);
        
        assertTrue(x > (delay-2));
        
        for ( ;!f.isDone(); ) {
            Thread.sleep(5);
            x = f.getDelay(TimeUnit.SECONDS);
        }
        long ms2 = System.currentTimeMillis();
        assertTrue( (ms2 - ms) >= (delay * 1000));
        
        assertEquals(1, ai.get());
    }
    
}
