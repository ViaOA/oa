package com.viaoa.process;


/**
 * Similiar to a future or promise to track a process.
 * @author vvia
 *
 */
public abstract class OAProcess implements Runnable {
    private volatile int currentStep;

    private boolean bBlock;
    private long maxBlockTime;
    
    private boolean bAllowCancel;
    private volatile boolean bCancelled;
    private volatile long cancelTime;
    private volatile String cancelReason;

    private final long createdTime;
    private volatile long doneTime;
    
    private String name, description;

    private String[] steps;
    private volatile long estimatedTime;
    
    private volatile Exception exception;
    
    private long maxTime;

    /**
     * 
     */
    public OAProcess() {
        createdTime = System.currentTimeMillis();
    }

    
    public void setCanCancel(boolean b) {
        this.bAllowCancel = b;
    }
    public boolean getCanCancel() {
        return bAllowCancel;
    }

    public void setBlock(boolean b) {
        this.bBlock = b;
    }
    public boolean getBlock() {
        return bBlock;
    }
    
    protected void setMaxBlockTime(long x) {
        this.maxBlockTime = x;
    }
    public long getMaxBlockTime() {
        return this.maxBlockTime;
    }
    
    
    public String getName() {
        return name;
    }
    public void setName(String s) {
        this.name = s;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String s) {
        this.description = s;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public void cancel(String reason) {
        this.bCancelled = true;
        this.cancelTime = System.currentTimeMillis();
        cancelReason = reason;
    }
    public boolean wasCancelled() {
        return bCancelled;
    }
    public boolean getCancelled() {
        return bCancelled;
    }
    public long getCancelTime() {
        return cancelTime;
    }
    public String getCancelReason() {
        return cancelReason;
    }

    public void done() {
        doneTime = System.currentTimeMillis();
    }
    public boolean isDone() {
        return (doneTime > 0);
    }
    public long getDoneTime() {
        return doneTime;
    }
    
    public Exception getException() {
        return exception;
    }
    protected void setException(Exception ex) {
        exception = ex;;
    }
    
    
    public String[] getSteps() {
        return steps;
    }
    public void setSteps(String... steps) {
        this.steps = steps;
    }
    public int getTotalSteps() {
        return (steps == null ? 0 : steps.length);
    }
    
    protected void setCurrentStep(int x) {
        this.currentStep = x;
    }
    public int getCurrentStep() {
        return this.currentStep;
    }

    protected void setEstimateTime(long x) {
        this.estimatedTime = x;
    }
    public long getEstimateTime() {
        return this.estimatedTime;
    }
    
    public long getMaxTime() {
        return this.maxTime;
    }
    public void setMaxTime(long x) {
        this.maxTime = x;
    }
    
    public boolean isBlockTimedout() {
        long ms = System.currentTimeMillis();
        return ((maxBlockTime + createdTime) > ms);
    }
    
    public boolean isTimedout() {
        if (maxTime < 1) return false;
        long ms = System.currentTimeMillis();
        return ((maxTime + createdTime) > ms);
    }

    
}

