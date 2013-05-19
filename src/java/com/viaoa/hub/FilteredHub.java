package com.viaoa.hub;

import java.util.Hashtable;

import com.viaoa.object.OAObject;


/**
 * A Hub that combines with a HubFilter.
*/
public abstract class FilteredHub<TYPE> extends Hub<TYPE> {
    
    private HubFilter filter;

    public FilteredHub(Hub<TYPE> hubMaster) {
        super(hubMaster.getObjectClass());
    
        filter = new HubFilter(hubMaster, this) {
            @Override
            public boolean isUsed(Object object) {
                return FilteredHub.this.isUsed((TYPE) object);
            }
        };
    }

    public HubFilter getFilter() {
        return filter;
    }
    
    public void addProperty(String prop) {
        filter.addProperty(prop);
    }
    public void addDependentProperty(OAObject obj, String prop) {
        filter.addDependentProperty(obj, prop);
    }
    public void addDependentProperty(Hub hub, String prop) {
        filter.addDependentProperty(hub, prop);
    }

    public void refresh() {
        getFilter().refresh();
    }
    
    protected abstract boolean isUsed(TYPE obj);
    
    
}

