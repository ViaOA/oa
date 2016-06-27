package com.viaoa.object;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates and removes Triggers, by setting up in OAObjectInfo.
 * @author vvia
 *
 */
public class OATriggerDelegate {
    
    public static OATrigger createTrigger(
        String name,
        Class rootClass,
        OATriggerListener triggerListener,
        String[] dependentPropertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bBackgroundThread,
        final boolean bBackgroundThreadIfNeeded)
    {
        OATrigger t = new OATrigger(name, rootClass, triggerListener, dependentPropertyPaths, bOnlyUseLoadedData, bServerSideOnly, bBackgroundThread, bBackgroundThreadIfNeeded);

        createTrigger(t);
        return t;
    }
    
    public static void createTrigger(OATrigger trigger) {
        createTrigger(trigger, false);
    }

    /**
     * @param bSkipFirstNonManyProperty if true, then if the first prop of the propertyPath is not Type=many, then it will not be used.  This
     * is used when there is a HubListener already listening to the objects.
     */
    public static void createTrigger(OATrigger trigger, boolean bSkipFirstNonManyProperty) {
        if (trigger == null) return;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(trigger.rootClass);
        oi.createTrigger(trigger, bSkipFirstNonManyProperty);
    }
    
    public static boolean removeTrigger(OATrigger trigger) {
        if (trigger == null) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(trigger.rootClass);
        oi.removeTrigger(trigger);
        
        return true;
    }

    public static void runTrigger(Runnable r) {
        getExecutorService().submit(r);
    }
    
    private static ThreadPoolExecutor executorService;
    
    // thread pool to handle tasks that can run in the background.
    protected static ExecutorService getExecutorService() {
        if (executorService != null) return executorService;

        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger ai = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("OATrigger.thread"+ai.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        
        // min/max must be equal, since new threads are only created when queue is full
        executorService = new ThreadPoolExecutor(5, 5, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE), tf); 
        executorService.allowCoreThreadTimeOut(true);
        
        return executorService;
    }
}
