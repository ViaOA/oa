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
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Combines two hubs into a new single hub to create the equivalent of a database groupBy.
 * 
 * The combined Hub (see getCombinedHub) uses OAObject OAGroupBy<G,F>, where G is the same class as the
 * groupBy Hub and F is a hub of the from objects.
 *
 * 
 * // group Employees by Dept
 * new HubGroupBy<Emp, Dept>(hubEmp, hubAllDept, "depts") new HubGroupBy<Emp, Dept>(hubEmp, "depts")
 *
 * Split property path - this is when all of the methods in a pp are not public (link that does not
 * create method). HubGroupBy is able to group them by splitting the pp using HubGroupBy and HubFrom to get a
 * combined group. ex: MRADClient.Application.ApplicationType.ApplicationGroup, hubFrom=hubMRADClients,
 * hubGroupBy=hubApplicationGroups note: the method for ApplicationType.getApplicationGroups() is not created
 * (is private)
 *
 * new HubGroupBy(hubMRADClients, hubApplicationGroups
 * "MRADClient.Application.ApplicationType.ApplicationGroup")
 * 
 * internally will create 2 HubGroupBys ... (hubMRADClients, "MRADClient.Application.ApplicationType")
 * (hubApplicationGroups, "ApplicationTypes")
 * 
 * @see HubLeftJoin# to create a "flat" list.
 * @see OAGroupBy# type of object for combined  
 * @author vvia
 */

public class HubGroupBy<F extends OAObject, G extends OAObject> {
    private Hub<F> hubFrom;
    private Hub<G> hubGroupBy;
    private Hub<OAGroupBy<G, F>> hubCombined;
    private String propertyPath;
    private String listenPropertyName;
    private Hub<G> hubMaster;
    private Hub<F> hubDetail;
    private boolean bIgnoreAOChange;

    private final static AtomicInteger aiCnt = new AtomicInteger();

    /**
     * Create a hub of objects that are based on hubB.
     * 
     * @param hubB
     * @param propertyPath
     */
    public HubGroupBy(Hub<F> hubB, String propertyPath) {
        this.hubGroupBy = null;
        this.hubFrom = hubB;
        this.propertyPath = propertyPath;
        setup();
    }

    /**
     * Create a hub on objects that are based on hubB, and are grouped by hubA. This allows a the
     * combined hub to have a full list like a left-join.
     * 
     * @param hubA
     *            objects that are to be grouped.
     * @param hubB
     *            optional list of objects to have as the master list. This will act as a left-join
     * 
     * @param propertyPath
     *            pp of the property from the right object to get left object. example: if hubDept,
     *            hubEmpOrders, then "Employee.Department" HubGroupBy(hubEmpOrders, hubDept,
     *            "Employee.Department") -or- HubGroupBy(hubEmpOrders, "Employee.Department")
     */
    public HubGroupBy(Hub<F> hubB, Hub<G> hubFrom, String propertyPath) {
        this.hubGroupBy = hubFrom;
        this.hubFrom = hubB;
        this.propertyPath = propertyPath;
        setup();
    }

    /**
     * @param hubCombined from another hubGroupBy that uses the same class for A & B, but a different PP.
     * 
     * ex:  ApplicationGroup.applications and ApplicationGroup.applicationType.applications
     */
    public HubGroupBy(Hub<OAGroupBy<G, F>> hubCombined, Hub<F> hubFrom, Hub<G> hubGroupBy, String propertyPath) {
        this.hubCombined = hubCombined;
        this.hubGroupBy = hubGroupBy;
        this.hubFrom = hubFrom;
        this.propertyPath = propertyPath;
        setup();
    }

    
    
    /**
     * @return Hub of combined objects using OAGroupBy
     */
    public Hub<OAGroupBy<G, F>> getCombinedHub() {
        if (hubCombined != null) return hubCombined;
        hubCombined = new Hub(OAGroupBy.class);
        return hubCombined;
    }

    /**
     * @return Hub<G> of groupBy objects that are in sync (share AO) with combined Hub.
     */
    public Hub<G> getMasterHub() {
        if (hubMaster == null) {
            if (hubGroupBy != null) hubMaster = new Hub<G>(hubGroupBy.getObjectClass());
            else hubMaster = new Hub<G>();
            new HubMerger(getCombinedHub(), hubMaster, OAGroupBy.P_GroupBy, true);
        }
        return hubMaster;
    }

    /**
     * @return detail hub from masterHub
     */
    public Hub<F> getDetailHub() {
        if (hubDetail == null) {
            hubDetail = getCombinedHub().getDetailHub(OAGroupBy.P_Hub);
        }
        return hubDetail;
    }

    void setup() {
        OAPropertyPath opp = new OAPropertyPath(propertyPath);

        try {
            opp.setup(hubFrom.getObjectClass(), (hubGroupBy != null));
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

        hgb1 = new HubGroupBy(hubFrom, pp1);
        hubGB1 = hgb1.getCombinedHub();

        hgb2 = new HubGroupBy(hubGroupBy, pp2);
        hubGB2 = hgb2.getCombinedHub();

        setupSplit();
    }

    // used by propertyPath that require a split
    private HubGroupBy hgb1;
    private Hub<OAGroupBy> hubGB1;

    private HubGroupBy hgb2;
    private Hub<OAGroupBy> hubGB2;
    /* <pre><code>
        
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
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = new OAGroupBy();
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
                        gbNewFound = new OAGroupBy();
                        gbNewFound.setGroupBy(objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    for (Object gb1B : gb1.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }
                }
                // remove from gbNew.A=null hubB
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
                for (Object obj : removeObjects) {
                    remove((OAGroupBy) obj);
                }
                removeObjects = null;
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
                OAGroupBy gbNewFound = null;
                for (Object gb1B : gb1.getHub()) {
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
                        gbNewFound = new OAGroupBy();
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                }
            }
        });

        // A.2: listen to changes to hgb1.hubB changes by using a hubmerger to get add/remove events and update this.hubCombined
        Hub<OAObject> hubTemp = new Hub<OAObject>(OAObject.class);
        HubMerger<OAGroupBy, OAObject> hm1 = new HubMerger<OAGroupBy, OAObject>(hubGB1, hubTemp, OAGroupBy.P_Hub, true) {
            protected void afterInsertRealHub(HubEvent e) {
                afterAddRealHub(e);
            }
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
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = new OAGroupBy();
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
                        gbNewFound = new OAGroupBy();
                        gbNewFound.setGroupBy(objGB2b);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }
                    gbNewFound.getHub().add(gb1B);
                }
                //remove from null hub                
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNew.getHub().remove(gb1B);
                        break;
                    }
                }
            }

            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy gb1 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                final OAObject gb1A = gb1.getGroupBy();
                Object gb1B = e.getObject();

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
                
                // see if it needs to be added to gbNew.A=null hubB
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
                        gbNewFound = new OAGroupBy();
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
                            gbNewFound = new OAGroupBy();
                            gbNewFound.setGroupBy((OAObject) gb2B);
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
                        gbNewFound = new OAGroupBy();
                        gbNewFound.setGroupBy((OAObject) gb2B);
                        HubGroupBy.this.getCombinedHub().add(gbNewFound);
                    }

                    for (Object gb1B : gb1Found.getHub()) {
                        gbNewFound.getHub().add(gb1B);
                    }

                    // might have been in gbNew.A=null gbNew.hubB
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
                for (Object obj : removeObjects) {
                    remove((OAGroupBy) obj);
                }
                removeObjects = null;
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
                if (gb1Found == null || gb1Found.getHub().getSize() == 0) return;

                for (Object gb2B : gb2.getHub()) {
                    OAGroupBy gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == gb2B) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
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
                        if (hubGroupBy == null || !hubGroupBy.contains(gbNewFound)) {
                            HubGroupBy.this.getCombinedHub().remove(gbNewFound);
                        }
                    }

                    // add to gbNew.A=null gbNew.hubB
                    gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = new OAGroupBy();
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
if (gb2 == null) {//qqqqqqqqqqqqqqqqqqqqqqqqq
    int xx = 4;
    xx++;
}
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
                        gbNewFound = new OAGroupBy();
                        gbNewFound.setGroupBy((OAObject) gb2B);
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
                    gbNewFound = new OAGroupBy();
                    gbNewFound.setGroupBy((OAObject) gb2B);
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

            @Override
            protected void afterRemoveRealHub(HubEvent e) {
                OAGroupBy gb2 = (OAGroupBy) ((Hub) e.getSource()).getMasterObject();
                final Object gb2A = gb2.getGroupBy();
                Object gb2B = e.getObject(); 

                OAGroupBy gb1Found = null;
                if (gb2A != null) {
                    for (OAGroupBy gb1 : hubGB1) {
                        if (gb1.getGroupBy() == gb2A) {
                            gb1Found = gb1;
                            break;
                        }
                    }
                }
                if (gb1Found == null || gb1Found.getHub().getSize() == 0) return;

                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == gb2B) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound == null) return;

                for (Object gb1B : gb1Found.getHub()) {
                    // ??? note: dont remove from hubB if it's still used for another path
                    gbNewFound.getHub().remove(gb1B);
                }

                if (gbNewFound.getHub().size() == 0) {
                    if (hubGroupBy == null || !hubGroupBy.contains(gbNewFound)) {
                        HubGroupBy.this.getCombinedHub().remove(gbNewFound);
                    }
                }
                
                if (gb2.getHub().size() == 0) {
                    // need to add to gbNew.a=null hubB
                    gbNewFound = null;
                    for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                        if (gbNew.getGroupBy() == null) {
                            gbNewFound = gbNew;
                            break;
                        }
                    }
                    if (gbNewFound == null) {
                        gbNewFound = new OAGroupBy();
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
                OAGroupBy gbNewFound = null;
                for (OAGroupBy gbNew : HubGroupBy.this.getCombinedHub()) {
                    if (gbNew.getGroupBy() == null) {
                        gbNewFound = gbNew;
                        break;
                    }
                }
                if (gbNewFound == null) {
                    gbNewFound = new OAGroupBy();
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
                    gbNewFound = new OAGroupBy();
                    gbNewFound.setGroupBy(objGB2b);
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
                // set the active object in hub A&B when hubCombine.AO is changed
                OAGroupBy obj = (OAGroupBy) e.getObject();
                if (obj == null) {
                    if (hubGroupBy != null) hubGroupBy.setAO(null);
                    hubFrom.setAO(null);
                }
                else {
                    if (hubGroupBy != null) hubGroupBy.setAO(obj.getGroupBy());
                    hubFrom.setAO(null);
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
                    OAGroupBy<G, F> c = new OAGroupBy(a);
                    hubCombined.add(c);
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
                    for (;;) {
                        OAGroupBy c = hubCombined.find(OAGroupBy.P_GroupBy, a);
                        if (c == null) break;
                        hubCombined.remove(c);
                    }
                }

                @Override
                public void onNewList(HubEvent e) {
                    hubCombined.clear();
                    for (G a : hubGroupBy) {
                        hubCombined.add(new OAGroupBy(a));
                    }
                    for (F b : hubFrom) {
                        add(b);
                    }
                }
            });
            for (G a : hubGroupBy) {
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
                hubCombined.clear();
                if (hubGroupBy != null) {
                    for (G a : hubGroupBy) {
                        hubCombined.add(new OAGroupBy(a));
                    }
                }
                for (F b : hubFrom) {
                    add(b);
                }
            }

            @Override
            public void afterChangeActiveObject(HubEvent e) {
                F b = (F) e.getObject();
                if (b != null) {
                    for (OAGroupBy lj : hubCombined) {
                        Hub h = lj.getHub();
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
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(hubFrom.getObjectClass());
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

        for (F bx : hubFrom) {
            add(bx);
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
                for (OAGroupBy gb : hubCombined) {
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
                    OAGroupBy<G, F> c = new OAGroupBy((G) valueA);
                    hubCombined.add(c);
                    c.getHub().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(c);
                    }
                }
            }

            // add to empty hub
            if (h.size() == 0) {
                for (OAGroupBy gb : hubCombined) {
                    if (gb.getGroupBy() != null) continue;
                    gb.getHub().add(b);
                    if (bReturnList) {
                        if (al == null) al = new ArrayList<OAGroupBy>();
                        al.add(gb);
                    }
                    return al;
                }
                // create new
                OAGroupBy<G, F> gb = new OAGroupBy();
                hubCombined.add(gb);
                gb.getHub().add(b);
                if (bReturnList) {
                    if (al == null) al = new ArrayList<OAGroupBy>();
                    al.add(gb);
                }
            }
        }
        else {
            for (OAGroupBy gb : hubCombined) {
                if (gb.getGroupBy() != valueA) continue;
                gb.getHub().add(b);
                if (bReturnList) {
                    if (al == null) al = new ArrayList<OAGroupBy>();
                    al.add(gb);
                }
                return al;
            }

            // create new
            OAGroupBy<G, F> c = new OAGroupBy((G) valueA);
            hubCombined.add(c);
            c.getHub().add(b);
            if (bReturnList) {
                if (al == null) al = new ArrayList<OAGroupBy>();
                al.add(c);
            }
        }
        return al;
    }

    private void remove(G a, F b) {
        for (OAGroupBy gb : hubCombined) {
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
        for (OAGroupBy gb : hubCombined) {
            Hub<F> h = gb.getHub();
            if (h.contains(b)) {
                h.remove(b);
                if (h.size() == 0) {
                    if (hubGroupBy == null || !hubGroupBy.contains(gb.getGroupBy())) {
                        hubCombined.remove(gb);
                    }
                }
            }
            // todo:  if this does not have many, then it can break here
        }
    }

    private void update(F b) {
        ArrayList<OAGroupBy> al = add(b, true);
        for (OAGroupBy gb : hubCombined) {
            Hub<F> h = gb.getHub();
            if (al != null) {
                if (al.contains(gb)) continue;
            }
            if (h.contains(b)) {
                h.remove(b);
            }
        }
    }
}
