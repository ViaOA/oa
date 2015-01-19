// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.filter;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "Recent", displayName = "Recent Recipient", hasInputParams = false)
public class InspireRecipientRecentFilter extends OAObject implements CustomHubFilter {
    private static final long serialVersionUID = 1L;


    public static final String PPCode = ":Recent()";
    private Hub<InspireRecipient> hubMaster;
    private Hub<InspireRecipient> hub;
    private HubFilter<InspireRecipient> filter;
    private boolean bAllHubs;

    public InspireRecipientRecentFilter(Hub<InspireRecipient> hub) {
        this(true, null, hub);
    }
    public InspireRecipientRecentFilter(Hub<InspireRecipient> hubMaster, Hub<InspireRecipient> hub) {
        this(false, hubMaster, hub);
    }
    public InspireRecipientRecentFilter(boolean bAllHubs, Hub<InspireRecipient> hubMaster, Hub<InspireRecipient> hubFiltered) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        if (hubMaster == null) this.hubMaster = new Hub<InspireRecipient>(InspireRecipient.class);
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
    public HubFilter<InspireRecipient> getHubFilter() {
        if (filter == null) {
            filter = createHubFilter(hubMaster, hub, bAllHubs);
        }
        return filter;
    }
    protected HubFilter<InspireRecipient> createHubFilter(final Hub<InspireRecipient> hubMaster, Hub<InspireRecipient> hub, boolean bAllHubs) {
        HubFilter<InspireRecipient> filter = new HubFilter<InspireRecipient>(hubMaster, hub) {
            @Override
            public boolean isUsed(InspireRecipient inspireRecipient) {
                return InspireRecipientRecentFilter.this.isUsed(inspireRecipient);
            }
        };
        filter.addDependentProperty(InspireRecipient.P_CompletedDate);
 
        if (!bAllHubs) return filter;
        final ArrayList<Hub> alHub = new ArrayList<Hub>();
        alHub.add(hub);
        alHub.add(hubMaster);
 
        OAObjectCacheDelegate.addListener(InspireRecipient.class, new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                Hub h = e.getHub();
                if (h == null || alHub.contains(h)) return;
                alHub.add(h);
                Hub<InspireRecipient> h2 = new Hub<InspireRecipient>(InspireRecipient.class);
                alHub.add(h2);
                createHubFilter(h, h2, false);
                h2.addHubListener(new HubListenerAdapter() {
                    @Override
                    public void afterAdd(HubEvent e) {
                        hubMaster.add((InspireRecipient)e.getObject());
                    }
                    @Override
                    public void afterRemove(HubEvent e) {
                        InspireRecipient obj = (InspireRecipient) e.getObject();
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
                if (prop.equalsIgnoreCase(InspireRecipient.P_CompletedDate)) {
                    if (!hubMaster.contains(e.getObject())) hubMaster.add((InspireRecipient) e.getObject());
                    return;
                }
            }
        });
        return filter;
    }

    public boolean isUsed(InspireRecipient inspireRecipient) {
        if (inspireRecipient == null) return false;
        OADate completedDate = inspireRecipient.getCompletedDate();
        if (completedDate == null) return false;
        if (inspireRecipient.getApprovalStatus() != InspireApproval.STATUS_Approved) return false;
        
        OADate today = new OADate();
        OADate d = (OADate) completedDate.addDays(30);
        if (today.before(d)) return false; // wait 30 days from completed date
        
        // show up to 60 days from completed date
        d = (OADate) (completedDate).addDays(60);
        return today.before(d);
    }
}
