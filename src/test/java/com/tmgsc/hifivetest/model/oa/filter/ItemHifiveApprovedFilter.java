// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.filter;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;
import com.tmgsc.hifivetest.model.oa.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "HifiveApproved", displayName = "Hi5 Approved Items", hasInputParams = false, description = "selects all items that  have a hi5 rating of approved")
public class ItemHifiveApprovedFilter extends OAObject implements CustomHubFilter {
    private static final long serialVersionUID = 1L;


    public static final String PPCode = ":HifiveApproved()";
    private Hub<Item> hubMaster;
    private Hub<Item> hub;
    private HubFilter<Item> filter;
    private boolean bAllHubs;

    public ItemHifiveApprovedFilter(Hub<Item> hub) {
        this(true, null, hub);
    }
    public ItemHifiveApprovedFilter(Hub<Item> hubMaster, Hub<Item> hub) {
        this(false, hubMaster, hub);
    }
    public ItemHifiveApprovedFilter(boolean bAllHubs, Hub<Item> hubMaster, Hub<Item> hubFiltered) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        if (hubMaster == null) this.hubMaster = new Hub<Item>(Item.class);
        this.bAllHubs = bAllHubs;
        getHubFilter(); // create filter
    }


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }
    public void refresh() {
        if (filter != null) getHubFilter().refresh();
    }

    @Override
    public HubFilter<Item> getHubFilter() {
        if (filter == null) {
            filter = createHubFilter(hubMaster, hub, bAllHubs);
        }
        return filter;
    }
    protected HubFilter<Item> createHubFilter(final Hub<Item> hubMaster, Hub<Item> hub, boolean bAllHubs) {
        HubFilter<Item> filter = new HubFilter<Item>(hubMaster, hub) {
            @Override
            public boolean isUsed(Item item) {
                return ItemHifiveApprovedFilter.this.isUsed(item);
            }
        };
        filter.addDependentProperty(Item.P_HifiveRating);
 
        if (!bAllHubs) return filter;
        final ArrayList<Hub> alHub = new ArrayList<Hub>();
        alHub.add(hub);
        alHub.add(hubMaster);
 
        OAObjectCacheDelegate.addListener(Item.class, new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                Hub h = e.getHub();
                if (h == null || alHub.contains(h)) return;
                alHub.add(h);
                Hub<Item> h2 = new Hub<Item>(Item.class);
                alHub.add(h2);
                createHubFilter(h, h2, false);
                h2.addHubListener(new HubListenerAdapter() {
                    @Override
                    public void afterAdd(HubEvent e) {
                        hubMaster.add((Item)e.getObject());
                    }
                    @Override
                    public void afterRemove(HubEvent e) {
                        Item obj = (Item) e.getObject();
                        if (!OAObjectHubDelegate.isInHub(obj)) {
                            hubMaster.remove(obj);
                        }
                    }
                });
            }
 
            @Override
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                if (prop.equalsIgnoreCase(Item.P_HifiveRating)) {
                    if (!hubMaster.contains(e.getObject())) hubMaster.add((Item) e.getObject());
                    return;
                }
            }
        });
        return filter;
    }

    public boolean isUsed(Item item) {
        boolean bResult = true;
        // hifiveRating
        bResult = false;
        int hifiveRating = item.getHifiveRating();
        if (hifiveRating == Item.HIFIVERATING_approved) bResult = true;
        return bResult;
    }
    
}
