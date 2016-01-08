package com.viaoa.hub;

import com.viaoa.object.OAObject;
import com.viaoa.util.OAFilter;

/**
 * This is used to listen to Hub for objects that match filter criteria and then call the onTrigger method.
 * @author vvia
 */
public abstract class HubTrigger<T extends OAObject> extends HubFilter<T> {
    private static final long serialVersionUID = 1L;
    
    public HubTrigger(Hub<T> hubMaster) {
        super(hubMaster, null);
    }
    public HubTrigger(Hub<T> hubMaster, OAFilter filter, String ... dependentPropertyPaths) {
        super(hubMaster, null, filter, dependentPropertyPaths);
    }

    @Override
    protected void addObject(T obj, boolean bIsInitialzing) {
        super.addObject(obj, bIsInitialzing);
        if (!bIsInitialzing) onTrigger(obj);
    }
    @Override
    protected void removeObject(T obj) {
        super.removeObject(obj);
    }
    
    public abstract void onTrigger(T obj);
}
