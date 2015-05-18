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
import com.viaoa.util.OAPropertyPath;

/**
 * Takes a single Hub<A>, and a property path to create two new Hubs that are
 * are master/detail, where the master is the groupBy hub, and the detail is group of
 * objects in Hub<A> that are under the AO in the groupBy hub.
 *
 * Example: from hubOrders, propPath: "employee.department"
 *    getGroupByHub: hub of Departments
 *    getDetailHub: hub of Orders for the groupByHub.AO 
 *  
 * Similar to a database "group by".
 * @param <A> type of objects for the seed Hub that supplies the objects that need to be grouped.
 * @param <B> type of objects that will be in the group by Hub.
 */
public class HubGroupBy<A extends OAObject, B extends OAObject> {
    // 20141117 support for reverse propertyPaths that dont have methods
    
    private Hub<A> hubA;
    private Hub<B> hubB;
    private Hub<A> hubDetail; // detail hub from hubB, using reverse propertyPath
    private Hub<A> hubDetailFiltered;  // filtered using hubDetail as root, and filtering only objects that exist in hubA
    private String propertyPath;
    private HubFilter<A> hubFilter;
    
    private boolean bInitializedCalled;
    
    /**
     * @param hubA hub of objects that are to be grouped.
     * @param propertyPath path to the property that is the groupBy
     */
    public HubGroupBy(Hub<A> hubA, String propertyPath) {
        this.hubA = hubA;
        this.propertyPath = propertyPath;
        setup();
    }

    public Hub<B> getGroupByHub() {
        return hubB;
    }
    /**
     * This is the detail from the hubGroupBy, with only the objects
     * that are under hubGroupBy, and are also in the original Hub hubA
     * @return
     */
    public Hub<A> getDetailHub() {
        return hubDetailFiltered; // from hubA
    }

    void setup() throws RuntimeException {
        
        OAPropertyPath pp = new OAPropertyPath(hubA.getObjectClass(), propertyPath);
        Class<?>[] cs = pp.getClasses();
        if (cs == null || cs.length == 0) {
            throw new RuntimeException("propertyPath is invalid, "+propertyPath);
        }
        
        // create master/groupBy hub
        hubB = new Hub<B>((Class<B>) cs[cs.length-1]);
        HubMerger hm = new HubMerger(hubA, hubB, propertyPath, false, true);
        
        
        OAPropertyPath ppRev;
        try {
            ppRev = pp.getReversePropertyPath();
        }
        catch (Exception e) {
            ppRev = null;
        }
        
        if (ppRev != null) {
            hubDetail = hubB.getDetailHub(ppRev.getPropertyPath());
            hubDetailFiltered = new Hub(hubA.getObjectClass());
            
            hubFilter = new HubFilter<A>(hubDetail, hubDetailFiltered) {
                @Override
                public boolean isUsed(A object) {
                    return hubA.contains(object);
                }
                
                // custom: if the filtered groupBy hub has an add/remove, then add/remove from the HubA
                
                @Override
                public void afterAdd(A obj) {
                    hubA.add(obj);
                }
                @Override
                public void afterRemove(A obj) {
                    hubA.remove(obj);
                }
            };

        
            hubA.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterInsert(HubEvent e) {
                    hubFilter.refresh();
                }
                @Override
                public void afterAdd(HubEvent e) {
                    hubFilter.refresh();
                }
                @Override
                public void afterRemove(HubEvent e) {
                    hubFilter.refresh();
                }
                @Override
                public void onNewList(HubEvent e) {
                    hubFilter.refresh();
                }
            });
        }
        else {
            hubDetail = null; // not used
            hubDetailFiltered = new Hub(hubA.getObjectClass());
            
            hubFilter = new HubFilter<A>(hubA, hubDetailFiltered) {
                @Override
                public boolean isUsed(A object) {
                    Object objx = object.getProperty(propertyPath);
                    return (objx == hubB.getAO());
                }
                @Override
                public void afterAdd(A obj) {
                    hubA.add(obj);
                }
                @Override
                public void afterRemove(A obj) {
                    hubA.remove(obj);
                }
            };
            
            hubB.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    hubFilter.refresh();
                }
            });            
        }
    }
}
