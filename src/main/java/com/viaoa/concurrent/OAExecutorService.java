package com.viaoa.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.util.OAString;

/**
 * creates and ExecutorService to await commands to run.
 * @author vvia
 *
 */
public class OAExecutorService {
    private static Logger LOG = Logger.getLogger(OAExecutorService.class.getName());
    private ThreadPoolExecutor executorService;
    private final AtomicInteger aiTotalSubmitted = new AtomicInteger();
    private final int size;
    private final String name;
    
    public OAExecutorService() {
        this(10, null);
    }
    
    public OAExecutorService(int size, String name) {
        this.size = size;
        this.name = name;
        getExecutorService();
    }
    
    public Future submit(Runnable r) {
        if (executorService == null) throw new RuntimeException("executorService has been shutdown");
        aiTotalSubmitted.incrementAndGet();
        Future f = getExecutorService().submit(r);
        return f;
    }
    public Future submitAndWait(Runnable r, int maxWait, TimeUnit tu) throws Exception {
        if (executorService == null) throw new RuntimeException("executorService has been shutdown");
        aiTotalSubmitted.incrementAndGet();
        Future f = getExecutorService().submit(r);
        Object objx = f.get(maxWait, tu);
        return f;
    }
    
    public Future submit(Callable c) {
        if (executorService == null) throw new RuntimeException("executorService has been shutdown");
        aiTotalSubmitted.incrementAndGet();
        Future f = getExecutorService().submit(c);
        return f;
    }
    public Future submitAndWait(Callable c, int maxWait, TimeUnit tu) throws Exception {
        if (executorService == null) throw new RuntimeException("executorService has been shutdown");
        aiTotalSubmitted.incrementAndGet();
        Future f = getExecutorService().submit(c);
        Object objx = f.get(maxWait, tu);
        return f;
    }

    public void close() {
        if (executorService == null) return;
        executorService.shutdown();
    }
    
    public ThreadPoolExecutor getExecutorService() {
        if (executorService != null) return executorService;
        
        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger ai = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                String s = "";
                if (OAString.isNotEmpty(name)) s = name+".";
                t.setName("OAExecutorService.thread."+s+ai.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        
        // min/max must be equal, since new threads are only created when queue is full
        executorService = new ThreadPoolExecutor(size, size, 60L, TimeUnit.SECONDS, 
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE), tf) 
        {
            @Override
            public Future<?> submit(Runnable task) {
                LOG.fine("running task in thread="+Thread.currentThread().getName());
                return super.submit(task);
            }
        };
        executorService.allowCoreThreadTimeOut(true);
        
        return executorService;
    }
}
