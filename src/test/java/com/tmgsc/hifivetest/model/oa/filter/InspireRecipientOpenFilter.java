// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.filter;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "Open", displayName = "Open", hasInputParams = false, description = "Open inspire recipients, waiting on manager approval")
public class InspireRecipientOpenFilter extends OAObject implements CustomHubFilter {
    private static final long serialVersionUID = 1L;


    public static final String PPCode = ":Open()";
    private Hub<InspireRecipient> hubMaster;
    private Hub<InspireRecipient> hub;
    private HubFilter<InspireRecipient> filter;
    private boolean bAllHubs;

    public InspireRecipientOpenFilter(Hub<InspireRecipient> hub) {
        this(true, null, hub);
    }
    public InspireRecipientOpenFilter(Hub<InspireRecipient> hubMaster, Hub<InspireRecipient> hub) {
        this(false, hubMaster, hub);
    }
    public InspireRecipientOpenFilter(boolean bAllHubs, Hub<InspireRecipient> hubMaster, Hub<InspireRecipient> hubFiltered) {
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
                return InspireRecipientOpenFilter.this.isUsed(inspireRecipient);
            }
        };
        filter.addDependentProperty(InspireRecipient.P_CompletedDate);
        filter.addDependentProperty(InspireRecipient.P_Employee);
        filter.addDependentProperty(InspireRecipient.P_Inspire);
        filter.addDependentProperty(OAString.cpp(InspireRecipient.P_Inspire, Inspire.P_Employee));
        filter.addDependentProperty(OAString.cpp(InspireRecipient.P_Inspire, Inspire.P_InspireAwardLevel));
        filter.addDependentProperty(OAString.cpp(InspireRecipient.P_Inspire, Inspire.P_Employee, Employee.P_Inspires));
 
        if (!bAllHubs) return filter;
        // need to listen to all InspireRecipient
        HubObjectCacheAdder hubCacheAdder = new HubObjectCacheAdder(hubMaster);
        return filter;
    }

    public boolean isUsed(InspireRecipient inspireRecipient) {
        if (inspireRecipient == null) return false;
        if (inspireRecipient.getEmployee() == null) return false;
        if (inspireRecipient.getCompletedDate() != null) return false;
        Inspire inspire = inspireRecipient.getInspire();
        if (inspire == null) return false;
        if (!inspire.getWasAddedToEmployee()) return false;
        return true;
    }
}
