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

import java.util.concurrent.atomic.AtomicInteger;
import com.viaoa.object.OAGroupBy;
import com.viaoa.object.OAObject;

/**
 * Combines two hubs into a new  single hub to create the equivalent of a database groupBy, where all
 * of the "left" side objects are in the list.
 * 
 * The combined Hub (see getCombinedHub) uses OAObject OAGroupBy<A,B>, where A is the same class
 * as the left Hub and B is a Hub of the the same as the right Hub.
 *
 * 
 * new HubGroupBy<Dept, Emp>(hubAllDept, hubEmp, "depts")
 * new HubGroupBy<Dept, Emp>(hubEmp, "depts")
 * 
 * @see HubLeftJoin#
 * 
 * @author vvia
 */
public class HubGroupBy<A extends OAObject, B extends OAObject> {
    private Hub<A> hubA;
    private Hub<B> hubB;
    private Hub<OAGroupBy<A, B>> hubCombined;
    private String propertyPath;
    private String listenPropertyName;
    private Hub<A> hubMaster;
    private Hub<B> hubDetail;
    private boolean bIgnoreAOChange;

    private final static AtomicInteger aiCnt = new AtomicInteger();

    
    /**
     * Create a hub of objects that are based on hubB.
     * @param hubB
     * @param propertyPath
     */
    public HubGroupBy(Hub<B> hubB, String propertyPath) {
        this.hubA = null;
        this.hubB = hubB;
        this.propertyPath = propertyPath;
        setup();
    }
    
    
    /**
     * Create a hub on objects that are based on hubB, and are grouped by hubA.
     * This allows a the combined hub to have a full list like a left-join.
     * 
     * @param hubA objects that are to be grouped.
     * @param hubB optional list of objects to have as the master list.  This will act as a left-join
     *            
     * @param propertyPath
     *            pp of the property from the right object to get left object. 
     *            example: if hubDept, hubEmpOrders, then "Employee.Department"
     *            HubGroupBy(hubEmpOrders, hubDept, "Employee.Department")
     *            -or-
     *            HubGroupBy(hubEmpOrders, "Employee.Department")
     */
    public HubGroupBy(Hub<A> hubA, Hub<B> hubB, String propertyPath) {
        this.hubA = hubA;
        this.hubB = hubB;
        this.propertyPath = propertyPath;
        setup();
    }

    
    /**
     * @return Hub of combined objects using OAGroupBy
     */
    public Hub<OAGroupBy<A, B>> getCombinedHub() {
        if (hubCombined != null) return hubCombined;
        hubCombined = new Hub(OAGroupBy.class);
        return hubCombined;
    }

    /**
     * @return Hub<A> of groupBy objects that are in sync (share AO) with combined Hub.
     */
    public Hub<A> getMasterHub() {
        if (hubMaster == null) {
            if (hubA != null) hubMaster = new Hub<A>(hubA.getObjectClass());
            else hubMaster = new Hub<A>();
            new HubMerger(getCombinedHub(), hubMaster, "A", true);
        }
        return hubMaster;
    }
    
    /**
     * @return detail hub from masterHub
     */
    public Hub<B> getDetailHub() {
        if (hubDetail == null) {
            hubDetail = getCombinedHub().getDetailHub("B");
        }
        return hubDetail;
    }

    /**
     * @deprecated use getDetailHub
     * @return detail Hub of hub<a>
     */
    public Hub<B> getGroupByHub() {
        return getDetailHub();
    }
    
    
    void setup() {
        getCombinedHub().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                if (bIgnoreAOChange) return;
                // set the active object in hub A&B when hubCombine.AO is changed
                OAGroupBy obj = (OAGroupBy) e.getObject();
                if (obj == null) {
                    if (hubA != null) hubA.setAO(null);
                    hubB.setAO(null);
                }
                else {
                    if (hubA != null) hubA.setAO(obj.getA());
                    hubB.setAO(null);
                }
            }
        });

        
        if (hubA != null) {
            hubA.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterInsert(HubEvent e) {
                    afterAdd(e);
                }
    
                @Override
                public void afterAdd(HubEvent e) {
                    A a = (A) e.getObject();
                    OAGroupBy<A, B> c = new OAGroupBy(a);
                    hubCombined.add(c);
                }
    
                @Override
                public void afterRemove(HubEvent e) {
                    A a = (A) e.getObject();
                    for (;;) {
                        OAGroupBy c = hubCombined.find(OAGroupBy.P_A, a);
                        if (c == null) break;
                        hubCombined.remove(c);
                    }
                }
    
                @Override
                public void onNewList(HubEvent e) {
                    hubCombined.clear();
                    for (A a : hubA) {
                        hubCombined.add(new OAGroupBy(a));
                    }
                    for (B b : hubB) {
                        add(b);
                    }
                }
            });
            for (A a : hubA) {
                hubCombined.add(new OAGroupBy(a));
            }
        }
        

        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }

            @Override
            public void afterAdd(HubEvent e) {
                B b = (B) e.getObject();
                add(b);
            }

            @Override
            public void afterRemove(HubEvent e) {
                B b = (B) e.getObject();
                remove(b);
            }

            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (!listenPropertyName.equalsIgnoreCase(s)) return;

                remove((B) e.getObject());
                add((B) e.getObject());
            }

            @Override
            public void onNewList(HubEvent e) {
                hubCombined.clear();
                if (hubA != null) {
                    for (A a : hubA) {
                        hubCombined.add(new OAGroupBy(a));
                    }
                }
                for (B b : hubB) {
                    add(b);
                }
            }

            @Override
            public void afterChangeActiveObject(HubEvent e) {
                B b = (B) e.getObject();
                if (b != null) {
                    for (OAGroupBy lj : hubCombined) {
                        Hub h = lj.getHubB();
                        if (h.contains(b)) {
                            try {
                                bIgnoreAOChange = true;
                                hubCombined.setAO(lj);
                                h.setAO(b);
                            }
                            finally {
                                bIgnoreAOChange = false;
                            }
                            return;
                        }
                    }
                }
                hubCombined.setAO(null);
            }
        };

        
        if (propertyPath == null || propertyPath.indexOf('.') < 0) {
            listenPropertyName = propertyPath;
            hubB.addHubListener(hl, propertyPath);
        }
        else {
            listenPropertyName = "hubCombinedDetail" + aiCnt.getAndIncrement();
            hubB.addHubListener(hl, listenPropertyName, new String[] { propertyPath });
        }

        for (B b : hubB) {
            add(b);
        }
    }

    private void add(B b) {
        if (b == null) return;
        Object valueA = b.getProperty(propertyPath);
        
        if (valueA instanceof Hub) {
            Hub h = (Hub) valueA;
            for (int i=0; ;i++) {
                valueA = h.getAt(i);
                if (valueA == null) break;
                
                boolean bFound = false;
                for (OAGroupBy gb : hubCombined) {
                    if (gb.getA() != valueA) continue;
                    gb.getHubB().add(b);
                    bFound = true;
                    break;
                }
                if (!bFound && hubA != null) {
                    // create new
                    OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
                    hubCombined.add(c);
                    c.getHubB().add(b);
                }
            }
            
            // add to empty hub
            if (h.size() == 0) {
                valueA = null;
                for (OAGroupBy gb : hubCombined) {
                    if (gb.getA() != valueA) continue;
                    gb.getHubB().add(b);
                    return;
                }
                if (hubA != null) {
                    // create new
                    OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
                    hubCombined.add(c);
                    c.getHubB().add(b);
                }
            }
        }
        else {
            for (OAGroupBy gb : hubCombined) {
                if (gb.getA() != valueA) continue;
                gb.getHubB().add(b);
                return;
            }
            if (hubA != null) {
                // create new
                OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
                hubCombined.add(c);
                c.getHubB().add(b);
            }
        }
    }

    private void remove(B b) {
        for (OAGroupBy gb : hubCombined) {
            Hub<B> h = gb.getHubB();
            if (h.contains(b)) {
                h.remove(b);
                return;
            }
        }
    }
}
