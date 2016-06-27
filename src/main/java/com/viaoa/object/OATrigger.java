package com.viaoa.object;


/**
 * Defines a method to be called from a root class, when any change is made from an object in a propertyPath(s0 
 * @author vvia
 */
public class OATrigger {
    protected String name;
    protected Class rootClass;
    protected String[] propertyPaths;
    protected OATriggerListener triggerListener;
    protected final boolean bOnlyUseLoadedData; 
    protected final boolean bServerSideOnly;
    protected final boolean bUseBackgroundThread;
    protected OATrigger[] dependentTriggers;
    
    public OATrigger(
        String name,
        Class rootClass,
        OATriggerListener triggerListener,
        String[] propertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bUseBackgroundThread)
    {
        this.name = name;
        this.rootClass = rootClass;
        this.propertyPaths = propertyPaths;
        this.triggerListener = triggerListener;
        this.bOnlyUseLoadedData = bOnlyUseLoadedData;
        this.bServerSideOnly = bServerSideOnly;
        this.bUseBackgroundThread = bUseBackgroundThread;
    }
    
    public OATrigger(
            String name,
            Class rootClass,
            OATriggerListener triggerListener,
            String propertyPath, 
            final boolean bOnlyUseLoadedData, 
            final boolean bServerSideOnly, 
            final boolean bUseBackgroundThread)
        {
            this.name = name;
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
