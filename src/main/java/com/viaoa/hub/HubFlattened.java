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

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.hub.HubListener.InsertLocation;

/**
    Takes a recursive hub (hubRoot) and populates a Hub (hubFlat) with all of the children.
 */
public class HubFlattened<TYPE extends OAObject> {
    private Hub<TYPE> hubRoot;
    private Hub<TYPE> hubFlat;
    private OALinkInfo linkInfo;
    private HubMerger<TYPE, TYPE> hm;

    public HubFlattened(Hub<TYPE> hubRoot, Hub<TYPE> hubFlat) {
        this.hubRoot = hubRoot;
        this.hubFlat = hubFlat;
        setup();
    }

    public Hub getRootHub() {
        return hubRoot;
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        hm.close();
    }
    
    void setup() {
        if (hubFlat == null || hubRoot == null) {
            return;
        }
        
        // must be recursive
        OAObjectInfo oi = hubRoot.getOAObjectInfo();
        OALinkInfo liMany = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        if (liMany == null) {
            throw new RuntimeException(hubRoot+" is not recursive");
        }

        // property that owns hubRoot
        linkInfo = HubDetailDelegate.getLinkInfoFromDetailToMaster(hubRoot);
        
        
        hm = new HubMerger(hubRoot, hubFlat, liMany.getName(), false, null, true, true);
        
        // make sure that any new object added to hubFlat(from a new command)  has correct link to parent/master
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                if (e == null) return;
                if (linkInfo == null) return;
                if (linkInfo.getValue(e.getObject()) != null) return;
                hubRoot.add((TYPE) e.getObject());  // this will set it
            }
        };
        hl.setLocation(InsertLocation.LAST);
        hubFlat.addHubListener(hl);
    }
}
