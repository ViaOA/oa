package com.viaoa.object;


/**
 * Defines a method to be called from a root class, when any change is made from an object in a propertyPath
 * 
 * see OATriggerDelegate# to have a trigger created. 
 */
public class OATrigger {
    protected String name;
    protected Class rootClass;
    protected String[] propertyPaths;
    protected OATriggerListener triggerListener;
    protected final boolean bOnlyUseLoadedData; 
    protected final boolean bServerSideOnly;
    protected final boolean bUseBackgroundThread;
    protected final boolean bUseBackgroundThreadIfNeeded;
    protected OATrigger[] dependentTriggers;
    
    public OATrigger(
        String name,
        Class rootClass,
        OATriggerListener triggerListener,
        String[] propertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bUseBackgroundThread,
        final boolean bUseBackgroundThreadIfNeeded)
    {
        this.name = name;
        this.rootClass = rootClass;
        this.propertyPaths = propertyPaths;
        this.triggerListener = triggerListener;
        this.bOnlyUseLoadedData = bOnlyUseLoadedData;
        this.bServerSideOnly = bServerSideOnly;
        this.bUseBackgroundThread = bUseBackgroundThread;
        this.bUseBackgroundThreadIfNeeded = bUseBackgroundThreadIfNeeded;
    }
    
    public OATrigger(
        String name,
        Class rootClass,
        OATriggerListener triggerListener,
        String propertyPath, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bUseBackgroundThread,
        final boolean bUseBackgroundThreadIfNeeded)
    {
        this.name = name;
        this.rootClass = rootClass;
        this.propertyPaths = new String[] {propertyPath};
        this.triggerListener = triggerListener;
        this.bOnlyUseLoadedData = bOnlyUseLoadedData;
        this.bServerSideOnly = bServerSideOnly;
        this.bUseBackgroundThread = bUseBackgroundThread;
        this.bUseBackgroundThreadIfNeeded = bUseBackgroundThreadIfNeeded;
    }

    public OATrigger[] getDependentTriggers() {
        return dependentTriggers;        
    }
    public OATriggerListener getTriggerListener() {
        return triggerListener;
    }
}
