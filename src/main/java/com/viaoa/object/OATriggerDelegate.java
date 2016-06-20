package com.viaoa.object;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and removes Triggers, by setting up in OAObjectInfo.
 * @author vvia
 *
 */
public class OATriggerDelegate {
    private static ConcurrentHashMap<OATriggerListener, OATrigger> hmTrigger = new ConcurrentHashMap<OATriggerListener, OATrigger>();
    
    public static OATrigger createTrigger(
        Class rootClass,
        String propertyName,
        OATriggerListener triggerListener,
        String[] dependentPropertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bBackgroundThread)
    {
        OATrigger t = new OATrigger(rootClass, propertyName, triggerListener, dependentPropertyPaths, bOnlyUseLoadedData, bServerSideOnly, bBackgroundThread);

        hmTrigger.put(triggerListener, t);
        
        createTrigger(t);
        return t;
    }

    public static void createTrigger(OATrigger trigger) {
        if (trigger == null) return;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(trigger.rootClass);
        oi.createTrigger(trigger);
    }
    
    public static boolean removeTrigger(OATrigger trigger) {
        if (trigger == null) return false;
        Object objx = hmTrigger.remove(trigger);
        if (objx == null) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(trigger.rootClass);
        oi.removeTrigger(trigger);
        
        return true;
    }
    
    
    
}
