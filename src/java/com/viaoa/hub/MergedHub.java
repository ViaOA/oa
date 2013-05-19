package com.viaoa.hub;

import com.viaoa.object.OAObject;

/**
 * Creates a Hub using HubMerger
*/
public class MergedHub<TYPE> extends Hub<TYPE> {
    
    private HubMerger hm;

    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath) {
        super(clazz);
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, true); 
    }

    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath, boolean bUseAll) {
        super(clazz);
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, bUseAll); 
    }
    
    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath, boolean bShareActiveObject, String selectOrder, boolean bUseAll) {
    	super(clazz);
    	this.hm = new HubMerger(hubMasterRoot, this, propertyPath, bShareActiveObject, selectOrder, bUseAll); 
    }

    public HubMerger getHubMerger() {
        return this.hm;
    }

    public MergedHub(Class<TYPE> clazz, OAObject obj, String propertyPath) {
        super(clazz);
        
        Hub hubMasterRoot = new Hub(obj.getClass());
        hubMasterRoot.add(obj);
        hubMasterRoot.setPos(0);
        
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, true);
    }

}

