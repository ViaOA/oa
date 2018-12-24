/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.hub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.object.OAGroupBy;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OASiblingHelper;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Creates a groupBy hub from a new single hub.  Equivalent of a database groupBy.
 * 
 * The combined Hub (see getCombinedHub) uses OAObject OAGroupBy&lt;F, G&gt;, where G is the same class as the
 * groupBy Hub and F is a hub of the from objects.
 *
 * 
 * // group Employees by Dept
 * new HubGroupBy&lt;Emp, Dept&gt;(hubEmp, hubAllDept, "depts") new HubGroupBy&lt;Emp, Dept&gt;(hubEmp, "depts")
 *
 * Split property path - this is when all of the methods in a pp are not public (link that does not
 * create method). HubGroupBy is able to group them by splitting the pp using HubGroupBy and HubFrom to get a
 * combined group. ex: MRADClient.Application.ApplicationType.ApplicationGroup, hubFrom=hubMRADClients,
 * hubGroupBy=hubApplicationGroups note: the method for ApplicationType.getApplicationGroups() is not created
 * (is private)
 *
 * new HubGroupBy(hubMRADClients, hubApplicationGroups, "MRADClient.Application.ApplicationType.ApplicationGroup")
 * 
 * internally will create 2 HubGroupBys ... (hubMRADClients, "MRADClient.Application.ApplicationType")
 * (hubApplicationGroups, "ApplicationTypes")
 * 
 * @see HubLeftJoin to create a "flat" list.
 * @author vvia
 */
public class HubGroupBy<F extends OAObject, G extends OAObject> {
    // from hub that are to be grouped
    private Hub<F> hubFrom;
    private final Class<F> classFrom;
    private Class<G> classGroupBy;
    
    // optional hub to use as groupBy list.  Required if the propertyPath has a "split/gap" (method does not exist) 
    private Hub<G> hubGroupBy;
    
    // result hub
    private Hub<OAGroupBy<F, G>> hubCombined;
    private String propertyPath;
    
    // internal calc prop created (if needed)
    private String listenPropertyName;
    
    private Hub<G> hubMaster;
    private Hub<F> hubDetail;
    
    private boolean bIgnoreAOChange;
    private boolean bCreateNullList;

    // used to name internally created calcProps
    private final static AtomicInteger aiCnt = new AtomicInteger();

    /**
     * Create a hub of objects that are based on hubB.
     * 
     * @param hubB
     * @param propertyPath
     */
    public HubGroupBy(Hub<F> hubB, String propertyPath, boolean bCreateNullList) {
        this.hubGroupBy = null;
        this.hubFrom = hubB;
        this.classFrom = hubB.getObjectClass();
        this.classGroupBy = null;
        
        this.propertyPath = propertyPath;
        this.bCreateNullList = bCreateNullList; 
        setup();
    }
    public HubGroupBy(Hub<F> hubB, String propertyPath) {
        this(hubB, propertyPath, true);
    }

    /**
     * Create a hub on objects that are based on hubB, and are grouped by hubA. This allows the
     * combined hub to have a full list like a left-join.

     * @param propertyPath
     *            pp of the property from the right object to get left object. example: if hubDept,
     *            hubEmpOrders, then "Employee.Department" HubGroupBy(hubEmpOrders, hubDept,
     *            "Employee.Department") -or- HubGroupBy(hubEmpOrders, "Employee.Department")
     */
    public HubGroupBy(Hub<F> hubB, Hub<G> hubFrom, String propertyPath, boolean bCreateNullList) {
        this.hubFrom = hubB;
        this.hubGroupBy = hubFrom;
        this.classFrom = hubB.getObjectClass();
        this.classGroupBy = null;
        this.propertyPath = propertyPath;
        this.bCreateNullList = bCreateNullList;
        setup();
    }
    public HubGroupBy(Hub<F> hubB, Hub<G> hubFrom, String propertyPath) {
        this(hubB, hubFrom, propertyPath, true);
    }

    /**
     * Combine two hgbs.
     */ 
    public HubGroupBy(HubGroupBy<F, G> hgb1, HubGroupBy<F, G> hgb2, boolean bCreateNullList) {
        if (hgb1 == null || hgb2 ==  null) throw new IllegalArgumentException("hgb1 & hgb2 can not be null");
        this.bCreateNullList = bCreateNullList;
        this.classFrom = hgb1.classFrom;
        this.classGroupBy = hgb1.classGroupBy;
        setupCombined(hgb1, hgb2);
    }
    public HubGroupBy(HubGroupBy<F, G> hgb1, HubGroupBy<F, G> hgb2) {
        this(hgb1, hgb2, true);
    }

    /**
     * create a new hgb that creates a combined groupBy with another.
     */
    public HubGroupBy(HubGroupBy<F, G> hgb, String pp, boolean bCreateNullList) {
        if (hgb == null) throw new IllegalArgumentException("hgb can not be null");
        this.classFrom = hgb.classFrom;
        this.classGroupBy = null;
        this.bCreateNullList = bCreateNullList;
        HubGroupBy<F, G> hgb2 = new HubGroupBy<F, G>(hgb.hubFrom, pp, bCreateNullList);
        setupCombined(hgb, hgb2);
    }
    public HubGroupBy(HubGroupBy<F, G> hgb, String pp) {
        this(hgb, pp, true);
    }
    
    
    /**
     * @return Hub of combined objects using OAGroupBy
     */
    public Hub<OAGroupBy<F, G>> getCombinedHub() {
        if (hubCombined != null) return hubCombined;
        hubCombined = new Hub(OAGroupBy.class);
        return hubCombined;
    }

    
    
    /**
     * @return Hub of groupBy objects that are in sync (share AO) with combined Hub.
     */
    public Hub<G> getMasterHub() {
        if (hubMaster == null) {
            if (hubGroupBy != null) hubMaster = new Hub<G>(hubGroupBy.getObjectClass());
            else hubMaster = new Hub<G>();
            new HubMerger(getCombinedHub(), hubMaster, OAGroupBy.P_GroupBy, true);
            
            hubMaster.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    if (bIgnoreAOChange) return;
                    try {
                        bIgnoreAOChange = true;
                        final Object ao = e.getObject();
                        hubFrom.setAO(null);
                        if (hubDetail != null) hubDetail.setAO(null);

                        if (ao == null) {
                            getCombinedHub().setAO(null);
                        }
                        else {
                            boolean bFound = false;
                            for (OAGroupBy<F, G> bg : getCombinedHub()) {
                                if (bg.getGroupBy() == ao) {
                                    getCombinedHub().setAO(bg);
                                    bFound = true;
                                    break;
                                }
                            }
                            if (!bFound) {
                                getCombinedHub().setAO(null);
                            }
                        }
                    }
                    finally {
                        bIgnoreAOChange = false;
                    }
                }
                @Override
                public void afterAdd(HubEvent e) {
                    if (classGroupBy == null) classGroupBy = (Class<G>) e.getObject().getClass();
                }
            });
        }
        return hubMaster;
    }

    /**
     * @return detail hub from compoundHub.hub
     */
    public Hub<F> getDetailHub() {
        if (hubDetail == null) {
            hubDetail = getCombinedHub().getDetailHub(OAGroupBy.P_Hub);
            HubDelegate.setObjectClass(hubDetail, classFrom); 
            hubDetail.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    if (bIgnoreAOChange) return;
                    try {
                        bIgnoreAOChange = true;
                        hubFrom.setAO(e.getObject()); 
                    }
                    finally {
                        bIgnoreAOChange = false;
                    }
                }
            });
            
        }
        return hubDetail;
    }

    void setup() {
        OAPropertyPath opp = new OAPropertyPath(propertyPath);

        try {
            opp.setup(classFrom, (hubGroupBy != null));
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
        if (posEmpty >= ms.length || hubGroupBy == null) {
            setupMain();
            return; // does not need to be split
        }

        // need to have a 2way propPath, one from rootHub, and another from topDown hub
        String pp1 = OAString.field(propertyPath, ".", 1, posEmpty);

        String pp2 = "";
        for (int i = ms.length - 1; i >= posEmpty; i--) {
            if (pp2.length() > 0) pp2 += ".";
            pp2 += lis[i].getReverseName();
        }

        hgb1 = new HubGroupBy(hubFrom, pp1, bCreateNullList);
        hubGB1 = hgb1.getCombinedHub();

        hgb2 = new HubGroupBy(hubGroupBy, pp2, bCreateNullList);
        hubGB2 = hgb2.getCombinedHub();

        setupSplit();
    }

    // used by propertyPath that require a split
    private HubGroupBy hgb1;
    private Hub<OAGroupBy> hubGB1;

    private HubGroupBy hgb2;
    private Hub<OAGroupBy> hubGB2;
    /*  This is used to define the structure that is created for the split.
     *  <pre><code>
        
        Original HubGroupBy  new HubGroupBy(hubApplicationGroup, hubMRADClient, "MRADClient.Application.ApplicationType.ApplicationGroup")
        
        Split:  
           GB1:     new HubGroupBy(hubMRADClient, "MRADClient.Application.ApplicationType")
           GB2:     new HubGroupBy(hubApplicationGroup, "ApplicationTypes") 
           GBNew:   hubCombined is updated using setupSlit
           
      
          OAGroupBy   GB1       GB2          GBNew
          .A          appType   appType      appGroup 
          .hubB       mrads     appGroups    mrads 
    
     </code></pre>
     * This is used when a propertyPath has a link where one of the createMethod=false. By having the source hub
     * for the leftmost HubB, and must also have the source HubA for the rightmost, two separate hgb can be used to update a 3rd
     * hgb. This will set up the listeners for hgb1 & hgb2 to update this.hubCombined.
     */
    private void setupSplit() {

        // A: hubGroup1 (hgb1) left part of pp, using hubB as the root
        // A.1: listen to hgb1 add/removes and update this.hubCombined
        hubGB1.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) e.getObject();
                if (gb1.getHub().size() == 0) return;
                final Object gb1A = gb1.getGroupBy();

                OAGroupBy gb2Found = null;
                if (gb1A != null) {
                    for (OAGroupBy gb2 : hubGB2) {
                        if (gb2.getGroupBy() == gb1A) {
                            gb2Found = gb2;
                            break;
                        }
                    }
                }
                if (gb2Found == null || gb2Found.getHub().getSize() == 0) {
                    // add to empty list
                    if (!bCreateNullList) return;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }
                    return;
                }

                for (Object gb2B : gb2Found.getHub()) {
                    OAObject objGB2b = (OAObject) gb2B;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == objGB2b) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy((G) objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }
                }
                // remove from gbNew.A=null hubB
                if (!bCreateNullList) return;
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound != null) {
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().remove(gb1B);
                    }
                }
            }

            Object[] removeObjects;
            @Override
            public void beforeRemoveAll(HubEvent e) {
                removeObjects = hubGB1.toArray();
            }
            @Override
            public void afterRemoveAll(HubEvent e) {
                if (removeObjects != null) {
                    for (Object obj : removeObjects) {
                        remove((OAGroupBy) obj);
                    }
                    removeObjects = null;
                }
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) e.getObject();
                if (gb1.getHub().size() == 0) return;
                remove(gb1);
            }
            void remove(OAGroupBy gb1) {
                final OAObject gb1A = gb1.getGroupBy();
                OAGroupBy gb2Found = null;
                if (gb1A != null) {
                    for (OAGroupBy gb2 : hubGB2) {
                        if (gb2.getGroupBy() == gb1A) {
                            gb2Found = gb2;
                            break;
                        }
                    }
                }
                if (gb2Found == null || gb2Found.getHub().getSize() == 0) {
                    // remove from empty list
                    if (!bCreateNullList) return;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) return;
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().remove(gb1B);
                    }
                    return;
                }

                for (Object gb2B : gb2Found.getHub()) {
                    OAObject objGB2b = (OAObject) gb2B;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == objGB2b) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) continue;
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().remove(gb1B);
                    }
                }

                // see if it needs to be added to gbNew.A=null hubB
                if (!bCreateNullList) return;
                OAGroupBy gbNewFound = null;
                for (Object gb1B : gb1.getHub()) {
                    if (!hubFrom.contains(gb1B)) continue; // no longer in the From list
                    boolean bFound = false;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            continue;
                        }
                        if (gbNew.getHub().contains(gb1B)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (bFound) continue;
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                }
            }
        });

        // A.2: listen to changes to hgb1.hubB changes by using a hubmerger to get add/remove events and update this.hubCombined
        Hub<OAObject> hubTemp = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy, OAObject> hm1 = new HubMerger<OAGroupBy, OAObject>(hubGB1, hubTemp, OAGroupBy.P_Hub, true) {
            @Override
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
            @Override
            protected void afterAddRealHub(HubEvent e) {
                OAGroupBy gb = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                final OAObject gb1A = gb.getGroupBy();
                Object gb1B = e.getObject(); // object added

                OAGroupBy gb2Found = null;
                if (gb1A != null) {
                    for (OAGroupBy gb2 : hubGB2) {
                        if (gb2.getGroupBy() == gb1A) {
                            gb2Found = gb2;
                            break;
                        }
                    }
                }

                if (gb2Found == null || gb2Found.getHub().getSize() == 0) {
                    // add to empty list
                    if (!bCreateNullList) return;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                    return;
                }

                for (Object gb2B : gb2Found.getHub()) {
                    OAObject objGB2b = (OAObject) gb2B;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == objGB2b) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy((G) objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                }
                //remove from null hub                
                if (!bCreateNullList) return;
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNew.getHub().remove(gb1B);
                        break;
                    }
                }
            }

            private Object[] removeAllObjects;
            @Override
            protected void beforeRemoveAllRealHub(HubEvent e) {
                removeAllObjects = ((Hub) e.getSource()).toArray();
            }
            @Override
            protected void afterRemoveAllRealHub(HubEvent e) {
                if (removeAllObjects == null) return;
                OAGroupBy gb1 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                for (Object obj: removeAllObjects) {
                    remove(gb1, (OAGroupBy) obj);
                }
                removeAllObjects = null;
            }
            
            @Override
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                Object gb1B = e.getObject();
                remove(gb1, gb1B);
            }
            void remove(final OAGroupBy gb1, final Object gb1B) {
                final OAObject gb1A = gb1.getGroupBy();

                OAGroupBy gb2Found = null;
                if (gb1A != null) {
                    for (OAGroupBy gb2 : hubGB2) {
                        if (gb2.getGroupBy() == gb1A) {
                            gb2Found = gb2;
                            break;
                        }
                    }
                }
                if (gb2Found == null || gb2Found.getHub().getSize() == 0) {
                    // remove from empty list
                    if (!bCreateNullList) return;
                    if (hgb1.hubFrom.contains(gb1B)) return;
                    
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) return;
                    gbNewFound.getHub().remove(gb1B);
                    return;
                }

                for (Object gb2B : gb2Found.getHub()) {
                    OAObject objGB2b = (OAObject) gb2B;
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == objGB2b) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) continue;
                    gbNewFound.getHub().remove(gb1B);
                }
                
                if (!HubGroupBy.this.hubFrom.contains(gb1B)) {
                    return;
                }
                
                // see if it needs to be added to gbNew.A=null hubB
                if (!bCreateNullList) return;
                OAGroupBy gbNewFound = null;
                boolean bFound = false;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNewFound = gbNew;
                        continue;
                    }
                    if (gbNew.getHub().contains(gb1B)) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                }                
            }
        };

        // B: hubGroup2 (hgb2) right reverse part of pp, using hubA as the root 
        // B.1: listen to hgb2 add/removes and update this.hubCombined
        // listen to GB2
        hubGB2.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) e.getObject();
                final OAObject gb2A = gb2.getGroupBy();

                OAGroupBy gb1Found = null;
                if (gb2A != null) {
                    for (OAGroupBy gb1 : hubGB1) {
                        if (gb1.getGroupBy() == gb2A) {
                            gb1Found = gb1;
                            break;
                        }
                    }
                }
                if (gb1Found == null) {
                    for (Object gb2B : gb2.getHub()) {
                        OAGroupBy gbNewFound = null;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getGroupBy() == gb2B) {
                                gbNewFound = gbNew;
                                break;
                            }
                        }
                        if (gbNewFound == null) {
                            gbNewFound = createGroupBy((G) gb2B);
                            HubGroupBy.this.getCombinedHub().add(gbNewFound);
                        }
                    }
                    return;
                }

                for (Object gb2B : gb2.getHub()) {
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == gb2B) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }

                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy((G) gb2B);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }

                    for (Object gb1B : gb1Found.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }

                    // might have been in gbNew.A=null gbNew.hubB
                    if (!bCreateNullList) continue;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            for (Object gb1B : gb1Found.getHub()) {
                                gbNew.getHub().remove(gb1B);
                            }
                            break;
                        }
                    }
                }
            }

            Object[] removeObjects;
            @Override
            public void beforeRemoveAll(HubEvent e) {
                removeObjects = hubGB2.toArray();
            }
            @Override
            public void afterRemoveAll(HubEvent e) {
                if (removeObjects != null) {
                    for (Object obj : removeObjects) {
                        remove((OAGroupBy) obj);
                    }
                    removeObjects = null;
                }
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) e.getObject();
                remove(gb2);
            }
            
            void remove(OAGroupBy gb2) {
                final Object gb2A = gb2.getGroupBy();

                OAGroupBy gb1Found = null;
                if (gb2A != null) {
                    for (OAGroupBy gb1 : hubGB1) {
                        if (gb1.getGroupBy() == gb2A) {
                            gb1Found = gb1;
                            break;
                        }
                    }
                }
                if (gb1Found == null || gb1Found.getHub().getSize() == 0) {
                    for (Object gb2B : gb2.getHub()) {
                        if (hubGroupBy != null && hubGroupBy.contains(gb2B)) continue;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getGroupBy() == gb2B) {
                                if (gbNew.getHub().size() == 0) HubGroupBy.this.getCombinedHub().remove(gbNew);
                                break;
                            }
                        }
                    }
                    return;
                }

                for (Object gb2B : gb2.getHub()) {
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == gb2B) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        if (!bCreateNullList) continue;
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getGroupBy() == null) {
                                gbNewFound = gbNew;
                                break;
                            }
                        }
                        if (gbNewFound == null) continue;
                    }

                    for (Object gb1B : gb1Found.getHub()) {
                        // ??? note: dont remove from hubB if it's still used for another path
                        gbNewFound.getHub().remove(gb1B);
                    }
                    
                    if (gbNewFound.getHub().size() == 0) {
                        if (hubGroupBy == null || !hubGroupBy.contains(gbNewFound.getGroupBy())) {
                            HubGroupBy.this.getCombinedHub().remove(gbNewFound);
                        }
                    }

                    // add to gbNew.A=null gbNew.hubB
                    if (!bCreateNullList) continue;
                    gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    for (Object gb1B : gb1Found.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }
                }
            }
        });

        // B.2: listen to changes to hgb2.hubB changes by using a hubmerger to get add/remove events and update this.hubCombined
        Hub<OAObject> hubTemp2 = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy, OAObject> hm2 = new HubMerger<OAGroupBy, OAObject>(hubGB2, hubTemp2, OAGroupBy.P_Hub, true) {
            @Override
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }

            @Override
            protected void afterAddRealHub(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                final Object gb2A = gb2.getGroupBy();
                Object gb2B = e.getObject(); // object added

                OAGroupBy gb1Found = null;
                if (gb2A != null) {
                    for (OAGroupBy gb1 : hubGB1) {
                        if (gb1.getGroupBy() == gb2A) {
                            gb1Found = gb1;
                            break;
                        }
                    }
                }
                if (gb1Found == null) {
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == gb2B) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy((G) gb2B);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    return;
                }
                
                OAGroupBy gbNewFound = null;
                OAGroupBy gbNewNullFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNewNullFound = gbNew;
                        if (gbNewFound != null) break;
                    }
                    else if (gbNew.getGroupBy() == gb2B) {
                        gbNewFound = gbNew;
                        if (gbNewNullFound != null) break;
                    }
                }

                if (gbNewFound == null) {
                    gbNewFound = createGroupBy((G) gb2B);
                    HubGroupBy.this.getCombinedHub().add(gbNewFound);
                }
                if (gb1Found == null) return;
                for (Object gb1B : gb1Found.getHub()) {
                    gbNewFound.getHub().add(gb1B);

                    // remove from null hub
                    if (gbNewNullFound != null) {
                        gbNewNullFound.getHub().remove(gb1B);
                    }
                }
            }

            private Object[] removeAllObjects;
            @Override
            protected void beforeRemoveAllRealHub(HubEvent e) {
                removeAllObjects = ((Hub) e.getSource()).toArray();
            }
            @Override
            protected void afterRemoveAllRealHub(HubEvent e) {
                if (removeAllObjects == null) return;
                OAGroupBy gb2 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                for (Object obj: removeAllObjects) {
                    remove(gb2, (OAGroupBy) obj);
                }
                removeAllObjects = null;
            }
            
            @Override
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                Object gb2B = e.getObject(); 
                remove(gb2, gb2B);
            }
            void remove(OAGroupBy gb2, final Object gb2B) {
                final Object gb2A = gb2.getGroupBy();
                if (gb2A == null) {
                    boolean bFound = false;
                    for (OAGroupBy gb : hubGB2) {
                        if (gb.getGroupBy() == null) continue;
                        if (gb.getHub().contains(gb2B)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                            if (gbNew.getGroupBy() == gb2B) {
                                if (gbNew.getHub().size() == 0) {
                                    HubGroupBy.this.getCombinedHub().remove(gbNew);
                                }
                                break;
                            }
                        }
                    }
                    return;
                }

                OAGroupBy gb1Found = null;
                for (OAGroupBy gb1 : hubGB1) {
                    if (gb1.getGroupBy() == gb2A) {
                        gb1Found = gb1;
                        break;
                    }
                }
                if (gb1Found == null || gb1Found.getHub().getSize() == 0) {
                    if (hubGroupBy.contains(gb2B)) return;
                }

                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == gb2B) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound == null) return;
                
                if (gb1Found != null) {
                    for (Object gb1B : gb1Found.getHub()) {
                        // ??? note: dont remove from hubB if it's still used for another path
                        gbNewFound.getHub().remove(gb1B);
                    }
                }
                
                if (gbNewFound.getHub().size() == 0) {
                    if (hubGroupBy == null || !hubGroupBy.contains(gbNewFound.getGroupBy())) {
                        HubGroupBy.this.getCombinedHub().remove(gbNewFound);
                    }
                }
                
                if (!bCreateNullList) return;
                if (gb1Found != null && gb2.getHub().size() == 0) {
                    // need to add to gbNew.a=null hubB
                    gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = createGroupBy(null);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    for (Object gb1B : gb1Found.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }
                }
            }
        };

        
        // C: initial load for this.hubCombined using GB1 
        for (OAGroupBy gb1 : hubGB1) {
            OAObject gb1A = (OAObject) gb1.getGroupBy();

            boolean bFound = false;
            OAGroupBy gb2Found = null;
            for (OAGroupBy gb2 : hubGB2) {
                if (gb2.getGroupBy() == gb1A) {
                    gb2Found = gb2;
                }
            }
            
            if (gb2Found == null || gb2Found.getHub().getSize() == 0) {
                // add to empty list
                if (!bCreateNullList) continue;
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound == null) {
                    gbNewFound = createGroupBy(null);
                    HubGroupBy.this.getCombinedHub().add(gbNewFound);
                }
                for (Object gb1B : gb1.getHub()) {
                    gbNewFound.getHub().add(gb1B);
                }
                continue;
            }
            
            for (Object gb2B : gb2Found.getHub()) {
                OAObject objGB2b = (OAObject) gb2B;
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == objGB2b) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound == null) {
                    gbNewFound = createGroupBy((G) objGB2b);
                    HubGroupBy.this.getCombinedHub().add(gbNewFound);
                }
                for (Object gb1B : gb1.getHub()) {
                    gbNewFound.getHub().add(gb1B);
                }
            }
        }
    }

    
    // main setup, if not needing a split
    void setupMain() {
        getCombinedHub().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                if (bIgnoreAOChange) return;
                
                try {
                    // set the active object in hub A&B when hubCombine.AO is changed
                    OAGroupBy obj = (OAGroupBy) e.getObject();
                    if (obj == null) {
                        if (hubGroupBy != null) hubGroupBy.setAO(null);
                        if (hubMaster != null) hubMaster.setAO(null);
                    }
                    else {
                        if (hubGroupBy != null) hubGroupBy.setAO(obj.getGroupBy());
                        if (hubMaster != null) hubMaster.setAO(obj.getGroupBy());
                    }
                    hubFrom.setAO(null);
                    if (hubDetail != null) hubDetail.setAO(null);
                }
                finally {
                    bIgnoreAOChange = false;                    
                }
            }
        });

        if (hubGroupBy != null) {
            hubGroupBy.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterInsert(HubEvent e) {
                    afterAdd(e);
                }

                @Override
                public void afterAdd(HubEvent e) {
                    G a = (G) e.getObject();
                    boolean bFound = false;
                    for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                        if (c.getGroupBy() == a) {
                            bFound= true;
                            break;
                        }
                    }
                    if (!bFound) {
                        OAGroupBy gbNewFound = createGroupBy(a);
                        HubGroupBy.this.hubCombined.add(gbNewFound);
                    }
                }

                Object[] removeObjects;
                @Override
                public void beforeRemoveAll(HubEvent e) {
                    removeObjects = hubGroupBy.toArray();
                }
                @Override
                public void afterRemoveAll(HubEvent e) {
                    for (Object obj : removeObjects) {
                        remove((G) obj);
                    }
                    removeObjects = null;
                }
                
                @Override
                public void afterRemove(HubEvent e) {
                    G a = (G) e.getObject();
                    remove(a);
                }
                void remove(G a) {
                    for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                        if (c.getGroupBy() == a) {
                            HubGroupBy.this.hubCombined.remove(c);
                            break;
                        }
                    }
                }

                @Override
                public void onNewList(HubEvent e) {
                    HubGroupBy.this.hubCombined.clear();
                    for (G a : hubGroupBy) {
                        OAGroupBy gbNewFound = createGroupBy(a);
                        HubGroupBy.this.hubCombined.add(gbNewFound);
                    }
                    addAll();
                }
            });
            for (G a : hubGroupBy) {
                boolean bFound = false;
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        bFound= true;
                        break;
                    }
                }
                if (!bFound) {
                    OAGroupBy gbNewFound = createGroupBy(a);
                    HubGroupBy.this.hubCombined.add(gbNewFound);
                }
            }
        }

        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }

            @Override
            public void afterAdd(HubEvent e) {
                F b = (F) e.getObject();
                add(b);
            }

            
            Object[] removeObjects;
            @Override
            public void beforeRemoveAll(HubEvent e) {
                removeObjects = hubFrom.toArray();
            }
            @Override
            public void afterRemoveAll(HubEvent e) {
                if (removeObjects == null)  return;
                for (Object obj : removeObjects) {
                    remove((F) obj);
                }
                removeObjects = null;
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                F b = (F) e.getObject();
                remove(b);
            }

            @Override
            public void afterPropertyChange(HubEvent e) {
                String s = e.getPropertyName();
                if (!listenPropertyName.equalsIgnoreCase(s)) return;
                update((F) e.getObject());
            }

            @Override
            public void onNewList(HubEvent e) {
                try {
                    bIgnoreAOChange = true;
                    HubGroupBy.this.getCombinedHub().clear();
                }
                finally {
                    bIgnoreAOChange = false;
                }
                if (hubGroupBy != null) {
                    for (G a : hubGroupBy) {
                        OAGroupBy gbNewFound = createGroupBy(a);
                        HubGroupBy.this.hubCombined.add(gbNewFound);
                    }
                }
                addAll();
            }

            @Override
            public void afterChangeActiveObject(HubEvent e) {
                if (bIgnoreAOChange) return;
                bIgnoreAOChange = true;
                try {
                    F ao = (F) e.getObject();
                    if (ao == null) {
                        HubGroupBy.this.hubCombined.setAO(null);
                        if (hubMaster != null) hubMaster.setAO(null);
                        if (hubDetail != null) hubDetail.setAO(null);
                    }
                    else {
                        boolean bFound = false;
                        for (OAGroupBy gb : HubGroupBy.this.getCombinedHub()) {
                            Hub h = gb.getHub();
                            if (!h.contains(ao)) continue;
                            bFound = true;
                            
                            HubGroupBy.this.hubCombined.setAO(gb);
                            h.setAO(ao);
                            if (hubMaster != null) hubMaster.setAO(gb.getGroupBy());
                            if (hubDetail != null) hubDetail.setAO(ao);
                            break;
                        }
                        if (!bFound) {
                            HubGroupBy.this.hubCombined.setAO(null);
                            if (hubMaster != null) hubMaster.setAO(null);
                            if (hubDetail != null) hubDetail.setAO(null);
                        }
                    }
                }
                finally {
                    bIgnoreAOChange = false;
                }
            }
        };

        boolean b = false;
        if (propertyPath == null) {
            b = true;
        }
        else if (propertyPath.indexOf('.') < 0) {
            // propertyPath could be a hub
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(classFrom);
            OALinkInfo li = oi.getLinkInfo(propertyPath);
            if (li == null || li.getType() == li.ONE) {
                b = true;
            }
            // else it's a hub
        }

        if (b) {
            listenPropertyName = propertyPath;
            hubFrom.addHubListener(hl, propertyPath);
        }
        else {
            listenPropertyName = "hubGroupBy" + aiCnt.getAndIncrement();
            hubFrom.addHubListener(hl, listenPropertyName, new String[] { propertyPath });
        }
        addAll();
    }

    private void addAll() {
        // this will tell the OASyncClient.getDetail which hub objects are being used
        final OASiblingHelper<F> siblingHelper = new OASiblingHelper<F>(this.hubFrom);
        siblingHelper.add(this.propertyPath);
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper); 
        try {
            OAThreadLocalDelegate.setSuppressCSMessages(true);
            for (F bx : hubFrom) {
                add(bx);
            }
        }
        finally {
            OAThreadLocalDelegate.setSuppressCSMessages(false);
            OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        }
    }
    
    private ArrayList<OAGroupBy> add(F b) {
        return add(b, false);
    }

    private ArrayList<OAGroupBy> add(F b, boolean bReturnList) {
        if (b == null) return null;
        Object valueA = b.getProperty(propertyPath);

        ArrayList<OAGroupBy> al = null;

        if (valueA instanceof Hub) {
            Hub h = (Hub) valueA;
            for (int i = 0;; i++) {
                valueA = h.getAt(i);
                if (valueA == null) break;

                boolean bFound = false;
                for (OAGroupBy gb : HubGroupBy.this.getCombinedHub()) {
                    if (gb.getGroupBy() != valueA) continue;
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gb);
                    }
                    gb.getHub().add(b);
                    bFound = true;
                    break;
                }
                if (!bFound) {
                    // create new
                    OAGroupBy gbNewFound = createGroupBy((G) valueA);
                    HubGroupBy.this.hubCombined.add(gbNewFound);
                    gbNewFound.getHub().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gbNewFound);
                    }
                }
            }

            // add to empty hub
            if (h.size() == 0 && bCreateNullList) {
                for (OAGroupBy gb : HubGroupBy.this.getCombinedHub()) {
                    if (gb.getGroupBy() != null) continue;
                    gb.getHub().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gb);
                    }
                    return al;
                }
                // create new
                OAGroupBy gb = createGroupBy(null);
                HubGroupBy.this.hubCombined.add(gb);
                gb.getHub().add(b);
                if (bReturnList) {
                    if (al == null) al = new ArrayList<OAGroupBy>();
                    al.add(gb);
                }
            }
        }
        else {
            if (!bCreateNullList && valueA == null) return al;
            for (OAGroupBy gb : HubGroupBy.this.getCombinedHub()) {
                if (gb.getGroupBy() != valueA) continue;
                gb.getHub().add(b);
                if (bReturnList) {
                    if (al == null) al = new ArrayList<OAGroupBy>();
                    al.add(gb);
                }
                return al;
            }

            // create new
            OAGroupBy<F,G> c = createGroupBy((G) valueA);
            HubGroupBy.this.hubCombined.add(c);
            c.getHub().add(b);
            if (bReturnList) {
                if (al == null) al = new ArrayList<OAGroupBy>();
                al.add(c);
            }
        }
        return al;
    }

    private void remove(G a, F b) {
        for (OAGroupBy gb : HubGroupBy.this.getCombinedHub()) {
            G ax = (G) gb.getGroupBy();
            if (ax != a) continue;
            Hub<F> h = gb.getHub();
            if (h.contains(b)) {
                h.remove(b);
                return;
            }
        }
    }

    private void remove(F b) {
        for (OAGroupBy gb : getCombinedHub()) {
            Hub<F> h = gb.getHub();
            if (h.contains(b)) {
                h.remove(b);
                if (h.size() == 0) {
                    if (hubGroupBy == null || !hubGroupBy.contains(gb.getGroupBy())) {
                        hubCombined.remove(gb);
                    }
                }
            }
        }
    }

    private void update(F b) {
        ArrayList<OAGroupBy> al = add(b, true);
        for (OAGroupBy gb : getCombinedHub()) {
            Hub<F> h = gb.getHub();
            if (al != null) {
                if (al.contains(gb)) continue;
            }
            if (h.contains(b)) {
                h.remove(b);
            }
        }
    }

//qqqqqqqqqqqqqqqqqqqqq
//    HubGroupBy hgb1x, hgb2x;;
    
    
    void setupCombined(HubGroupBy<F, G> hgb1, HubGroupBy<F, G> hgb2) {
        final Hub<OAGroupBy<F, G>> hub1 = hgb1.getCombinedHub();
        final Hub<OAGroupBy<F, G>> hub2 = hgb2.getCombinedHub();
//qqqqqqqqqqqqqq
//hgb1x = hgb1;
//hgb2x = hgb2;
        
        getCombinedHub();
        HubListener<OAGroupBy<F, G>> hl = new HubListenerAdapter<OAGroupBy<F, G>>() {
            /*
            Hub<OAGroupBy<F, G>> getOtherHub(HubEvent e) {
                if (e.getSource() == hub1) return hub2;
                return hub1;
            }
            */
            
            @Override
            public void afterAdd(HubEvent<OAGroupBy<F, G>> e) {
                final OAGroupBy<F, G> gb = e.getObject();
                OAGroupBy gbFound = null;
                G a = (G) gb.getGroupBy();
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                
                if (gbFound == null) {
                    gbFound = createGroupBy(a);
                    HubGroupBy.this.getCombinedHub().add(gbFound);
                }
                for (OAObject obj : gb.getHub()) {
                    gbFound.getHub().add(obj);
                }
            }
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterRemove(HubEvent<OAGroupBy<F, G>> e) {
                final OAGroupBy<F, G> gb = e.getObject();
                OAGroupBy gbFound = null;
                G a = (G) gb.getGroupBy();

                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound = c;
                        break;
                    }
                }
                if (gbFound == null) return;
                for (OAObject obj : gb.getHub()) {
                    boolean bStillNeeded = false;
                    for (OAGroupBy<F, G> gbx : hub1) {
                        if (gbx.getGroupBy() == a) {
                            if (gbx.getHub().contains(obj)) {
                                bStillNeeded = true;
                                break;
                            }
                        }
                    }
                    for (OAGroupBy<F, G> gbx : hub2) {
                        if (gbx.getGroupBy() == a) {
                            if (gbx.getHub().contains(obj)) {
                                bStillNeeded = true;
                                break;
                            }
                        }
                    }
                    if (!bStillNeeded) gbFound.getHub().remove(obj);
                }
            }
            
            void rebuild() {
                HubGroupBy.this.getCombinedHub().clear();
                OAGroupBy<F, G> gbFound = null;
                
                for (OAGroupBy<F, G> gb : hub1) {
                    gbFound = createGroupBy(gb.getGroupBy());
                    HubGroupBy.this.hubCombined.add(gbFound);
                    for (F obj : gb.getHub()) {
                        gbFound.getHub().add(obj);
                    }
                }
                
                for (OAGroupBy<F, G> gb : hub2) {
                    gbFound = null;
                    G a = (G) gb.getGroupBy();
                    for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                        if (c.getGroupBy() == a) {
                            gbFound= c;
                            break;
                        }
                    }
                    if (gbFound == null) {
                        gbFound = createGroupBy(a);
                        HubGroupBy.this.hubCombined.add(gbFound);
                    }
                    for (F obj : gb.getHub()) {
                        gbFound.getHub().add(obj);
                    }
                }
            }
            
            @Override
            public void afterRemoveAll(HubEvent<OAGroupBy<F, G>> e) {
                rebuild();
            }
            @Override
            public void onNewList(HubEvent e) {
                rebuild();
            }
        };

        hub1.addHubListener(hl);
        hub2.addHubListener(hl);

        
        // set up hubMergers
        
//qqqqqq this fails        
        //Hub<F> hubTemp1 = new Hub<F>();
//qq this works        
        Hub<OAObject> hubTemp1 = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy<F, G>, F> hm1 = new HubMerger<OAGroupBy<F, G>, F>(hub1, (Hub<F>)hubTemp1, OAGroupBy.P_Hub, true) {
            @Override
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
            @Override
            protected void afterAddRealHub(HubEvent e) {
                final OAGroupBy<F, G> gb = (OAGroupBy<F, G>) e.getHub().getMasterObject();
                final F objAdd = (F) e.getObject();
                
                OAGroupBy<F, G> gbFound = null;
                G a = (G) gb.getGroupBy();
                if (a == null) {
                    if (!bCreateNullList) return;
                    // only add if its in the other hgb null
                    for (OAGroupBy<F, G> gbx : hub2) {
                        if (gbx.getGroupBy() == null) {
                            if (!gbx.getHub().contains(objAdd)) {
                                return;
                            }
                        }
                    }
                }
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                
                if (gbFound == null) {
                    gbFound = createGroupBy(a);
                    HubGroupBy.this.getCombinedHub().add(gbFound);
                }
                gbFound.getHub().add(objAdd);
            }

            @Override
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy<F, G> gb = (OAGroupBy<F, G>) e.getHub().getMasterObject();
                F objRemove = (F) e.getObject();
                remove(gb, objRemove);
            }
            void remove(final OAGroupBy<F, G> gb, final F objRemove) {
                OAGroupBy<F, G> gbFound = null;
                final G a = (G) gb.getGroupBy();
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                if (gbFound == null) return;

                boolean bStillNeeded = false;
                if (a != null) {
                    for (OAGroupBy<F, G> gbx : hub2) {
                        if (gbx.getGroupBy() == a) {
                            if (gbx.getHub().contains(objRemove)) {
                                bStillNeeded = true;
                                break;
                            }
                        }
                    }
                }
                if (!bStillNeeded) gbFound.getHub().remove(objRemove);
            }
            private Object[] removeAllObjects;
            @Override
            protected void beforeRemoveAllRealHub(HubEvent e) {
                removeAllObjects = ((Hub) e.getSource()).toArray();
            }
            @Override
            protected void afterRemoveAllRealHub(HubEvent e) {
                if (removeAllObjects == null) return;
                OAGroupBy gb1 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                for (Object obj: removeAllObjects) {
                    remove(gb1, (F) obj);
                }
                removeAllObjects = null;
            }
            
        };
    
        Hub<OAObject> hubTemp2 = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy<F, G>, F> hm2 = new HubMerger<OAGroupBy<F, G>, F>(hub2, (Hub<F>)hubTemp2, OAGroupBy.P_Hub, true) {
            @Override
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
            @Override
            protected void afterAddRealHub(HubEvent e) {
                final OAGroupBy<F, G> gb = (OAGroupBy<F, G>) e.getHub().getMasterObject();
                final F objAdd = (F) e.getObject();
                
                OAGroupBy<F, G> gbFound = null;
                G a = (G) gb.getGroupBy();
                if (a == null) {
                    if (!bCreateNullList) return;
                    // only add if its in the other hgb null
                    for (OAGroupBy<F, G> gbx : hub2) {
                        if (gbx.getGroupBy() == null) {
                            if (!gbx.getHub().contains(objAdd)) {
                                return;
                            }
                        }
                    }
                }
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                
                if (gbFound == null) {
                    gbFound = createGroupBy(a);
                    HubGroupBy.this.hubCombined.add(gbFound);
                }
                gbFound.getHub().add(objAdd);
            }

            @Override
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy<F, G> gb = (OAGroupBy<F, G>) e.getHub().getMasterObject();
                F objRemove = (F) e.getObject();
                remove(gb, objRemove);
            }
            void remove(final OAGroupBy<F, G> gb, final F objRemove) {
                OAGroupBy<F, G> gbFound = null;
                final G a = (G) gb.getGroupBy();
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                if (gbFound == null) return;

                boolean bStillNeeded = false;
                if (a != null) {
                    for (OAGroupBy<F, G> gbx : hub1) {
                        if (gbx.getGroupBy() == a) {
                            if (gbx.getHub().contains(objRemove)) {
                                bStillNeeded = true;
                                break;
                            }
                        }
                    }
                }
                if (!bStillNeeded) gbFound.getHub().remove(objRemove);
            }
            private Object[] removeAllObjects;
            @Override
            protected void beforeRemoveAllRealHub(HubEvent e) {
                removeAllObjects = ((Hub) e.getSource()).toArray();
            }
            @Override
            protected void afterRemoveAllRealHub(HubEvent e) {
                if (removeAllObjects == null) return;
                OAGroupBy gb1 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                for (Object obj: removeAllObjects) {
                    remove(gb1, (F) obj);
                }
                removeAllObjects = null;
            }
        };

        
        // initial load
        for (int i=0; i<2; i++) {
            Hub<OAGroupBy<F, G>> hub;
            if (i == 0) hub = hub1;
            else hub = hub2;
            for (OAGroupBy<F, G> gb : hub) {
                OAGroupBy<F, G> gbFound = null;
                G a = (G) gb.getGroupBy();
                if (!bCreateNullList && a == null) continue;
                for (OAGroupBy c : HubGroupBy.this.getCombinedHub()) {
                    if (c.getGroupBy() == a) {
                        gbFound= c;
                        break;
                    }
                }
                if (gbFound == null) {
                    gbFound = createGroupBy(a);
                    HubGroupBy.this.getCombinedHub().add(gbFound);
                }
                for (F obj : gb.getHub()) {
                    gbFound.getHub().add(obj);
                }
            }
        }        
    }
    
    private OAGroupBy<F,G> createGroupBy(G grpBy) {
        OAGroupBy<F,G> gb = new OAGroupBy<F, G>();
        if (grpBy != null) gb.setGroupBy(grpBy);
        HubDelegate.setObjectClass(gb.getHub(), classFrom);
        return gb;
    }
    
    public String getGroupByPP() {
        String s = "("+classGroupBy.toString() + ")GroupBy";
        return s;
    }
    public String getHubByPP() {
        return "("+classFrom.toString() + ")hub";
    }
    
}
