/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
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

