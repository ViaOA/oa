package com.viaoa.util;

import java.util.concurrent.atomic.*;

/**
 * Used to throttle based on a minimum time (ms) frame.
 * Use the check() method to know if it's time again.
 * @author vvia
 */
public class OAThrottle {

    private final AtomicLong aiMsLast = new AtomicLong();
    private final AtomicLong aiCnt = new AtomicLong();
    private long msDelay;

  
    public OAThrottle(long msDelay) {
        setDelay(msDelay);
    }

    public void setDelay(long msDelay) {
        this.msDelay = msDelay;
    }
    public long getDelay() {
        return msDelay;
    }
    
    /**
     * This will check to see if the the required delay/time has passed since the last call to check.
     * @return
     */
    public boolean check() {
        aiCnt.incrementAndGet();
        long msNow = System.currentTimeMillis();
        if (aiMsLast.get() + msDelay > msNow) {
            return false;
        }
        aiMsLast.set(msNow);
        return true;
    }
    
    public long now() {
        long ms = System.currentTimeMillis();
        return ms;
    }

    /**
     * sets throttle counter to and last valid check time to 0L.
     */
    public void reset() {
        aiMsLast.set(0);
        aiCnt.set(0);
    }
    
    public long getCheckCount() {
        return aiCnt.get();
    }
    /**
     * Returns the last time that a call to check() returned true.
     */
    public long getLastThrottle() {
        return aiMsLast.get();
    }
}
