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


