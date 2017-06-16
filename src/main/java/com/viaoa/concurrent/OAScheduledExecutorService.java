package com.viaoa.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.util.OADateTime;
import com.viaoa.util.OATime;


/**
 * ExecutorService that will run methods at a specific date/time.
 * @author vvia
 *
 */
public class OAScheduledExecutorService {
    private static Logger LOG = Logger.getLogger(OAScheduledExecutorService.class.getName());
    private ScheduledExecutorService scheduledExecutorService;
    private final AtomicInteger aiTotalSubmitted = new AtomicInteger();
    
    public OAScheduledExecutorService() {
        getScheduledExecutorService();
    }

    public ScheduledFuture<?> schedule(Runnable r, OADateTime dt) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        
        long ms;
        OADateTime dtNow = new OADateTime();
        if (dt == null || dt.before(dtNow)) ms = 0;
        else ms = dt.betweenMilliSeconds(dtNow);
        
        ScheduledFuture<?> f = getScheduledExecutorService().schedule(r, ms, TimeUnit.MILLISECONDS);
        return f;
    }
    
    
    public ScheduledFuture<?> schedule(Runnable r, int delay, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        ScheduledFuture<?> f = getScheduledExecutorService().schedule(r, delay, tu);
        return f;
    }
    public ScheduledFuture<?> schedule(Callable<?> c, int delay, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        ScheduledFuture<?> f = getScheduledExecutorService().schedule(c, delay, tu);
        return f;
    }


    public ScheduledFuture<?> scheduleEvery(Runnable r, OATime time) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        
        final long secDay = (24 * 60 * 60);
        long secDelay;
        OATime tNow = new OATime();
        if (tNow.before(time)) secDelay = time.betweenSeconds(tNow);
        else {
            secDelay = tNow.betweenSeconds(time);
            secDelay = secDay - secDelay;
        }
        TimeUnit tu = TimeUnit.SECONDS;
        ScheduledFuture<?> f = getScheduledExecutorService().scheduleAtFixedRate(r, secDelay, secDay, tu);
        return f;
    }
    public ScheduledFuture<?> scheduleEvery(Runnable r, int initialDelay, int period, TimeUnit tu) throws Exception {
        aiTotalSubmitted.incrementAndGet();
        ScheduledFuture<?> f = getScheduledExecutorService().scheduleWithFixedDelay(r, initialDelay, period, tu);
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
        scheduledExecutorService = Executors.newScheduledThreadPool(1, tf);  // core needs to be > 0
        return scheduledExecutorService;
    }
    
    
    
    
    public static void main(String[] args) throws Exception {
        OAScheduledExecutorService ses = new OAScheduledExecutorService();
        final AtomicInteger ai = new AtomicInteger();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ai.incrementAndGet();
                System.out.println("====> "+ai);
                try {
//                    Thread.sleep(900);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }
        };
        ses.scheduleEvery(r, 1, 1, TimeUnit.MILLISECONDS);
        ses.scheduleEvery(r, 5, 2, TimeUnit.SECONDS);
        ses.scheduleEvery(r, 1, 1, TimeUnit.SECONDS);
        ses.scheduleEvery(r, 1, 1, TimeUnit.MILLISECONDS);
        ses.scheduleEvery(r, 1, 1, TimeUnit.SECONDS);
        
        
        /*        
                ThreadFactory tf = new ThreadFactory() {
                    AtomicInteger ai = new AtomicInteger();
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("ScheduledExecutorService.thread"+ai.getAndIncrement());
                        t.setDaemon(true);
                        t.setPriority(Thread.NORM_PRIORITY);
        System.out.println("NEW THREAD ====> "+ai);
                        return t;
                    }
                };
                ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, tf);
                
                ScheduledFuture<?> f = scheduledExecutorService.scheduleAtFixedRate(r, 1, 5, TimeUnit.SECONDS);
        
                ScheduledFuture<?> fx = scheduledExecutorService.scheduleAtFixedRate(r, 100, 200, TimeUnit.MILLISECONDS);
        */        
        for (;;) {
            Thread.sleep(10 * 1000);
        }
    }
    
}
