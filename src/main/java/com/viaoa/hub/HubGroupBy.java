/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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
