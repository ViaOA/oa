package com.viaoa.object;

public class OATrigger {
    protected Class rootClass;
    protected String propertyName;
    protected String[] dependentPropertyPaths;
    protected OATriggerListener triggerListener;
    protected final boolean bOnlyUseLoadedData; 
    protected final boolean bServerSideOnly;
    protected final boolean bUseBackgroundThread;
    protected OATrigger[] dependentTriggers;
    
    public OATrigger(
        Class rootClass,
        String propertyName,
        OATriggerListener triggerListener,
        String[] dependentPropertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bUseBackgroundThread)
    {
        this.rootClass = rootClass;
        this.propertyName = propertyName;
        this.dependentPropertyPaths = dependentPropertyPaths;
        this.triggerListener = triggerListener;
        this.bOnlyUseLoadedData = bOnlyUseLoadedData;
        this.bServerSideOnly = bServerSideOnly;
        this.bUseBackgroundThread = bUseBackgroundThread;
    }
    
    
}
