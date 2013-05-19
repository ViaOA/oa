/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.hub;

/** 
    A filter object that is used to populate a hub with all the objects from a master hub, minus
    the objects from another hub.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class HubMinusHubFilter {
    protected Hub hubMaster, hubMinus, hub;

    
    /** 
        Create a new HubMinusHubFilter using 3 Hubs.
        @param hubMaster hub of all objects
        @param hubMinus hub of object to exclude
        @param hub objects from master hub minus the objects in minus hub
    */
    public HubMinusHubFilter(Hub hubMaster, Hub hubMinus, Hub hub) {
        if (hubMaster == null || hub == null || hubMinus == null) throw new IllegalArgumentException("hubMaster and hub can not be null");
        this.hubMaster = hubMaster;
        this.hubMinus = hubMinus;
        this.hub = hub;
        init();
        populate();
    }

    protected void populate() {
        hub.clear();
        for (int i=0; ;i++) {
            Object obj = hubMaster.elementAt(i);
            if (obj == null) break;
            if (!hubMinus.contains(obj)) hub.add(obj);
        }
    }
    
    protected void init() {
        hubMaster.addHubListener( new HubListenerAdapter() {
            public @Override void afterAdd(HubEvent e) {
                Object obj = e.getObject();
                if (obj != null && !hubMinus.contains(obj)) hub.add(obj);
            }
            public @Override void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            public @Override void afterRemove(HubEvent e) {
                Object obj = e.getObject();
                if (obj != null) hub.remove(obj);
            }
            public @Override void onNewList(HubEvent e) {
                populate();
            }
        });
        hubMinus.addHubListener( new HubListenerAdapter() {
            public @Override void afterAdd(HubEvent e) {
                Object obj = e.getObject();
                if (obj != null && hub.contains(obj)) hub.remove(obj);
            }
            public @Override void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            public @Override void afterRemove(HubEvent e) {
                Object obj = e.getObject();
                if (hubMaster.contains(obj)) hub.add(obj);
            }
            public @Override void onNewList(HubEvent e) {
                populate();
            }
        });
    }
   
}


