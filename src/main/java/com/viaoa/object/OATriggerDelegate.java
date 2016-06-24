package com.viaoa.object;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and removes Triggers, by setting up in OAObjectInfo.
 * @author vvia
 *
 */
public class OATriggerDelegate {
    public static OATrigger createTrigger(
        Class rootClass,
        OATriggerListener triggerListener,
        String[] dependentPropertyPaths, 
        final boolean bOnlyUseLoadedData, 
        final boolean bServerSideOnly, 
        final boolean bBackgroundThread)
    {
        OATrigger t = new OATrigger(rootClass, triggerListener, dependentPropertyPaths, bOnlyUseLoadedData, bServerSideOnly, bBackgroundThread);

        createTrigger(t);
        return t;
    }
    
    public static void createTrigger(OATrigger trigger) {
        createTrigger(trigger, false);
    }

    /**
     * 
     * @param trigger
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
//        qqqqqqqqqq create executorservice, logging, etc
        
        
    }
}
