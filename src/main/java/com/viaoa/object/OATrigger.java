package com.viaoa.object;

public class OATrigger {
    protected Class rootClass;
    protected String[] propertyPaths;
    protected OATriggerListener triggerListener;
    protected final boolean bOnlyUseLoadedData; 
    protected final boolean bServerSideOnly;
    protected final boolean bUseBackgroundThread;
    protected OATrigger[] dependentTriggers;
    
//qqqqqqqqq flag to know if first prop was skipped???Qqq    
    
    public OATrigger(
        Class rootClass,
        OATriggerListener triggerListener,
        String[] propertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bUseBackgroundThread)
    {
        this.rootClass = rootClass;
        this.propertyPaths = propertyPaths;
        this.triggerListener = triggerListener;
        this.bOnlyUseLoadedData = bOnlyUseLoadedData;
        this.bServerSideOnly = bServerSideOnly;
        this.bUseBackgroundThread = bUseBackgroundThread;
    }
    
    public OATrigger(
            Class rootClass,
            OATriggerListener triggerListener,
            String propertyPath, 
            final boolean bOnlyUseLoadedData, 
            final boolean bServerSideOnly, 
            final boolean bUseBackgroundThread)
        {
            this.rootClass = rootClass;
            this.propertyPaths = new String[] {propertyPath};
            this.triggerListener = triggerListener;
            this.bOnlyUseLoadedData = bOnlyUseLoadedData;
            this.bServerSideOnly = bServerSideOnly;
            this.bUseBackgroundThread = bUseBackgroundThread;
        }

    public OATrigger[] getDependentTriggers() {
        return dependentTriggers;        
    }
    public OATriggerListener getTriggerListener() {
        return triggerListener;
    }
    
}


