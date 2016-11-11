package com.viaoa.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.util.OADateTime;
import com.viaoa.util.OATime;

public class OAScheduledExecutorService {
    private static Logger LOG = Logger.getLogger(OAScheduledExecutorService.class.getName());
    private ScheduledExecutorService scheduledExecutorService;
    private final AtomicInteger aiTotalSubmitted = new AtomicInteger();
    
    public OAScheduledExecutorService() {
        getScheduledExecutorService();
    }

    public Future<?> schedule(Runnable r, OADateTime dt) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        
        long ms;
        OADateTime dtNow = new OADateTime();
        if (dt == null || dt.before(dtNow)) ms = 0;
        else ms = dt.betweenMilliSeconds(dtNow);
        
        Future<?> f = getScheduledExecutorService().schedule(r, ms, TimeUnit.MILLISECONDS);
        return f;
    }
    
    
    public Future<?> schedule(Runnable r, int delay, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        Future<?> f = getScheduledExecutorService().schedule(r, delay, tu);
        return f;
    }
    public Future<?> schedule(Callable<?> c, int delay, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        Future<?> f = getScheduledExecutorService().schedule(c, delay, tu);
        return f;
    }


    public Future<?> scheduleEvery(Runnable r, OATime time) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        
        long ms;
        OATime tNow = new OATime();
        if (tNow.before(time)) ms = time.betweenMilliSeconds(tNow);
        else {
            ms = tNow.betweenMilliSeconds(time);
            ms = (24 * 60 * 60 * 1000) - ms;
        }
        Future<?> f = getScheduledExecutorService().scheduleWithFixedDelay(r, ms, 1, TimeUnit.DAYS);
        return f;
    }
    public Future<?> scheduleEvery(Runnable r, int initialDelay, int period, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        Future<?> f = getScheduledExecutorService().scheduleWithFixedDelay(r, initialDelay, period, tu);
        return f;
    }
    

    
    public ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService != null) return scheduledExecutorService;
        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger ai = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ScheduledExecutorService.thread"+ai.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        scheduledExecutorService = Executors.newScheduledThreadPool(0, tf);
        return scheduledExecutorService;
    }
}
