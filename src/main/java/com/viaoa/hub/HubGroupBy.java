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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.object.OAGroupBy;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

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
 * Split property path - this is when all of the methods in a pp are not public (link that does not create method).
 * HubGroupBy is able to group them by splitting the pp using HubA and HubB to get a combined group.
 * ex:  MRADClient.Application.ApplicationType.ApplicationGroup, hubA=hubMRADClients, hubB=hubApplicationGroups
 *    note:  the method for ApplicationType.getApplicationGroups() is not created (is private)
 *
 *    new HubGroupBy(hubApplicationGroups, hubMRADClients, "MRADClient.Application.ApplicationType.ApplicationGroup") 
 *    
 *  internally will create 2 HubGroupBys ... (hubMRADClients, "MRADClient.Application.ApplicationType") 
 *                                           (hubApplicationGroups, "ApplicationTypes")
 *                                    
 * @see HubLeftJoin# to create a "flat" list.
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
     * @param hubCombined from another hubGroupBy that uses the same class for A & B, but a different PP.
     */
    public HubGroupBy(Hub<OAGroupBy<A, B>> hubCombined, Hub<A> hubA, Hub<B> hubB, String propertyPath) {
        this.hubCombined = hubCombined;
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

    void setup() {
        OAPropertyPath opp = new OAPropertyPath(propertyPath);
        
        try {
            opp.setup(hubB.getObjectClass(), (hubA != null));
        }
        catch (Exception e) {
            throw new RuntimeException("PropertyPath setup failed", e);
        }
        
        OALinkInfo[] lis = opp.getLinkInfos();
        Method[] ms = opp.getMethods();

        int posEmpty = 0;
        for (Method m : ms) {
            if (m == null) break;
            posEmpty++;
        }
        if (posEmpty >= ms.length || hubA == null) {
            setupMain();
            return; // does not need to be split
        }
        
        
        // need to have a 2way propPath, one from rootHub, and another from topDown hub
        String pp1 = OAString.field(propertyPath, ".", 1, posEmpty);
        
        String pp2 = "";
        for (int i=ms.length-1; i>=posEmpty; i--) {
            if (pp2.length() > 0) pp2 += ".";
            pp2 += lis[i].getReverseName();
        }

        hgb1 = new HubGroupBy(hubB, pp1);
        hubGB1 = hgb1.getCombinedHub();
        
        hgb2 = new HubGroupBy(hubA, pp2);
        hubGB2 = hgb2.getCombinedHub();
        
        setupSplit(pp1);
    }

    // used by propertyPath that require a split
    private HubGroupBy hgb1;
    private Hub<OAGroupBy> hubGB1;

    private HubGroupBy hgb2;
    private Hub<OAGroupBy> hubGB2;

    /*
        GB1        GB2         GBNew
        appType    appType     appGroup
        mrads      appGroups   mrads
    */
    
    
    /**
     * This will use 2 hgb to update a 3rd
     */
    private void setupSplit(String pp) {
        // listen to changes to hubGB1 B changes
        Hub<OAObject> hubTemp = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy, OAObject> hm1 = new HubMerger<OAGroupBy, OAObject>(hubGB1, hubTemp, "b", true) {
            protected void afterAddRealHub(HubEvent e) {
                OAGroupBy gb = (OAGroupBy) ((Hub)e.getSource()).getMasterObject();
                OAObject objMaster = gb.getA();
                Object objAdd = e.getObject();
                
                boolean bFound = false;
                for (OAGroupBy gb2 : hubGB2) {
                    if (gb2.getA() != objMaster) continue;

                    for (Object obj2x : gb2.getB()) {
                        OAObject objGB2b = (OAObject) obj2x;
                        boolean b = false;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getA() != objGB2b) continue;
                            gbNew.getB().add(objAdd);
                            b = true;
                            bFound = true;
                            break;
                        }
                        if (b) continue;
                        OAGroupBy gbNew = new OAGroupBy();
                        gbNew.setA(objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNew);
                        gbNew.getB().add(objAdd);
                        bFound = true;
                    }
                    break;
                }
                if (bFound) return;
                
                // add to empty list
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getA() != null) continue;
                    gbNew.getB().add(objAdd);
                    bFound = true;
                    break;
                }
                if (bFound) return;
                
                // create and add to empty list
                OAGroupBy gbNew = new OAGroupBy();
                gbNew.getB().add(objAdd);
                HubGroupBy.this.getCombinedHub().add(gbNew);
            }
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy gb = (OAGroupBy) ((Hub)e.getSource()).getMasterObject();
                OAObject objMaster = gb.getA();
                
                Object objRemove = e.getObject();

                boolean bFound = false;
                for (OAGroupBy gb2 : hubGB2) {
                    if (gb2.getA() != objMaster) continue;

                    for (Object obj2x : gb2.getB()) {
                        OAObject objGB2b = (OAObject) obj2x;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getA() != objGB2b) continue;
                            gbNew.getB().remove(objRemove);
                            bFound = true;
                            break;
                        }
                    }
                    break;
                }
                if (bFound) return;
                // remove from empty list
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getA() != null) continue;
                    gbNew.getB().remove(objRemove);
                    break;
                }
            }
        };
    
    
        // initial load
        for (OAGroupBy gb1 : hubGB1) {
            OAObject gb1A = (OAObject) gb1.getA();
            
            boolean bFound = false;
            for (OAGroupBy gb2 : hubGB2) {
                if (gb2.getA() != gb1A) continue;

                for (Object gb2B : gb2.getB()) {
                    OAObject objGB2b = (OAObject) gb2B;
                    boolean b = false;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getA() != objGB2b) continue;
                        for (Object gb1B : gb1.getB()) {
                            gbNew.getB().add(gb1B);
                        }
                        bFound = true;
                        b = true;
                        break;
                    }
                    if (b) continue;
                    
                    OAGroupBy gbNew = new OAGroupBy();
                    gbNew.setA(objGB2b);
                    HubGroupBy.this.getCombinedHub().add(gbNew);
                    for (Object gb1B : gb1.getB()) {
                        gbNew.getB().add(gb1B);
                    }
                    bFound = true;
                }
                break;
            }
            if (bFound) continue;
            
            for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                if (gbNew.getA() != null) continue;
                for (Object gb1B : gb1.getB()) {
                    gbNew.getB().add(gb1B);
                }
                bFound = true;
                break;
            }
            if (bFound) continue;
            
            OAGroupBy gbNew = new OAGroupBy();
            HubGroupBy.this.getCombinedHub().add(gbNew);
            
            for (Object gb1B : gb1.getB()) {
                gbNew.getB().add(gb1B);
            }
        }


        // listen to hubGB1
        hubGB1.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) e.getObject();
                if (gb1.getB().size() == 0) return;
                Object gb1A = gb1.getA();

                boolean bFound = false;
                for (OAGroupBy gb2 : hubGB2) {
                    if (gb2.getA() != gb1A) continue;
                    
                    for (Object gb2B : gb2.getB()) {
                        OAObject objGB2b = (OAObject) gb2B;
                        boolean b = false;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getA() != objGB2b) continue;
                            for (Object gb1B : gb1.getB()) {
                                gbNew.getB().add(gb1B);
                            }
                            bFound = true;
                            b = true;
                            break;
                        }
                        if (b) continue;
                        
                        OAGroupBy gbNew = new OAGroupBy();
                        gbNew.setA(objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNew);
                        for (Object gb1B : gb1.getB()) {
                            gbNew.getB().add(gb1B);
                        }
                        bFound = true;
                    }
                    if (bFound) return;
                    
                    // add to empty list
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getA() != null) continue;
                        for (Object gb1B : gb1.getB()) {
                            gbNew.getB().add(gb1B);
                        }
                        bFound = true;
                        break;
                    }
                    if (bFound) return;
                    
                    // create and add to empty list
                    OAGroupBy gbNew = new OAGroupBy();
                    HubGroupBy.this.getCombinedHub().add(gbNew);
                    for (Object gb1B : gb1.getB()) {
                        gbNew.getB().add(gb1B);
                    }
                }                
            }
            @Override
            public void afterRemove(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) ((Hub)e.getSource()).getMasterObject();
                OAObject gb1A = gb1.getA();
                
                boolean bFound = false;
                for (OAGroupBy gb2 : hubGB2) {
                    if (gb2.getA() != gb1A) continue;

                    for (Object gb2B : gb2.getB()) {
                        OAObject objGB2b = (OAObject) gb2B;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getA() != objGB2b) continue;
                            
                            for (Object gb1B : gb1.getB()) {
                                gbNew.getB().remove(gb1B);
                            }
                            bFound = true;
                            break;
                        }
                    }
                    break;
                }
                if (bFound) return;
                // remove from empty list
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getA() != null) continue;
                    for (Object gb1B : gb1.getB()) {
                        gbNew.getB().remove(gb1B);
                    }
                    break;
                }
            }
            
        });
        
        
//qqqqqqqqqqqqqqqqqqq
        
        Hub<OAObject> hubTemp2 = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy, OAObject> hm2 = new HubMerger<OAGroupBy, OAObject>(hubGB2, hubTemp2, "b", true) {
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
            protected void afterAddRealHub(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) ((Hub)e.getSource()).getMasterObject();
                Object gb2A = gb2.getA();
                Object gb2B = e.getObject();
                
                OAGroupBy gb1Found = null;
                for (OAGroupBy gb1 : hubGB1) {
                    if (gb1.getA() == gb2A) {
                        gb1Found = gb1;
                        break;
                    }
                }

                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getA() == gb2B) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                
qqqqqqq                
                if (gbNewFound)
                
                
                    for (Object gb1B : gb1.getB()) {
                        gbNew.getB().add(gb1B);
                    }
                }
                
                if (gb1Found != null) {
                    
                }
                
                
                   
                    
                    
                    
                    
                }
        
            }
            protected void afterRemoveRealHub(HubEvent e) {
                
            }
        };
                
        
        // listen to GB2
        hubGB2.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
            }
        });
        
    }
    
    
    
    
    // main setup
    void setupMain() {
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
                update((B) e.getObject());
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

        boolean b = false;
        if (propertyPath == null) {
            b = true;
        }
        else if (propertyPath.indexOf('.') < 0) {
            // propertyPath could be a hub
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(hubB.getObjectClass());
            OALinkInfo li = oi.getLinkInfo(propertyPath);
            if (li == null || li.getType() == li.ONE) {
                b = true;
            }
            // else it's a hub
        }        
        
        if (b) {
            listenPropertyName = propertyPath;
            hubB.addHubListener(hl, propertyPath);
        }
        else {
            listenPropertyName = "hubGroupBy" + aiCnt.getAndIncrement();
            hubB.addHubListener(hl, listenPropertyName, new String[] { propertyPath });
        }

        for (B bx : hubB) {
            add(bx);
        }
    }

    private ArrayList<OAGroupBy> add(B b) {
        return add(b, false);
    }
    private ArrayList<OAGroupBy> add(B b, boolean bReturnList) {
        if (b == null) return null;
        Object valueA = b.getProperty(propertyPath);
        
        ArrayList<OAGroupBy> al = null;
        
        if (valueA instanceof Hub) {
            Hub h = (Hub) valueA;
            for (int i=0; ;i++) {
                valueA = h.getAt(i);
                if (valueA == null) break;
                
                boolean bFound = false;
                for (OAGroupBy gb : hubCombined) {
                    if (gb.getA() != valueA) continue;
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gb);
                    }
                    gb.getHubB().add(b);
                    bFound = true;
                    break;
                }
                if (!bFound) {
                    // create new
                    OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
                    hubCombined.add(c);
                    c.getHubB().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(c);
                    }
                }
            }
            
            // add to empty hub
            if (h.size() == 0) {
                valueA = null;
                for (OAGroupBy gb : hubCombined) {
                    if (gb.getA() != valueA) continue;
                    gb.getHubB().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gb);
                    }
                    return al;
                }
                if (hubA != null) {
                    // create new
                    OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
                    hubCombined.add(c);
                    c.getHubB().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(c);
                    }
                }
            }
        }
        else {
            for (OAGroupBy gb : hubCombined) {
                if (gb.getA() != valueA) continue;
                gb.getHubB().add(b);
                if (bReturnList) {
                    if (al == null) al = new ArrayList<OAGroupBy>();
                    al.add(gb);
                }
                return al;
            }

            // create new
            OAGroupBy<A, B> c = new OAGroupBy((A) valueA);
            hubCombined.add(c);
            c.getHubB().add(b);
            if (bReturnList) {
                if (al == null) al = new ArrayList<OAGroupBy>();
                al.add(c);
            }
        }
        return al;
    }

    private void remove(A a, B b) {
        for (OAGroupBy gb : hubCombined) {
            A ax = (A) gb.getA();
            if (ax != a) continue;
            Hub<B> h = gb.getHubB();
            if (h.contains(b)) {
                h.remove(b);
                return;
            }
        }
    }
    private void remove(B b) {
        for (OAGroupBy gb : hubCombined) {
            Hub<B> h = gb.getHubB();
            if (h.contains(b)) {
                h.remove(b);
                if (h.size() == 0) {
                    if (hubA == null || !hubA.contains(gb.getA())) {
                        hubCombined.remove(gb);
                    }
                }
            }
            // todo:  if this does not have many, then it can break here
        }
    }
    
    private void update(B b) {
        ArrayList<OAGroupBy> al = add(b, true);
        for (OAGroupBy gb : hubCombined) {
            Hub<B> h = gb.getHubB();
            if (al != null) {
                if (al.contains(gb)) continue;
            }
            if (h.contains(b)) {
                h.remove(b);
            }
        }
    }
}
