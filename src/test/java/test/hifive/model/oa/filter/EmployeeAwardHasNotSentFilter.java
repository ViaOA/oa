// Generated by OABuilder
package test.hifive.model.oa.filter;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.*;

import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "HasNotSent", displayName = "New orders", hasInputParams = false)
public class EmployeeAwardHasNotSentFilter extends OAObject implements CustomHubFilter<EmployeeAward> {
    private static final long serialVersionUID = 1L;


    public static final String PPCode = ":HasNotSent()";
    private Hub<EmployeeAward> hubMaster;
    private Hub<EmployeeAward> hub;
    private HubFilter<EmployeeAward> filter;
    private boolean bAllHubs;

    public EmployeeAwardHasNotSentFilter(Hub<EmployeeAward> hub) {
        this(true, null, hub);
    }
    public EmployeeAwardHasNotSentFilter(Hub<EmployeeAward> hubMaster, Hub<EmployeeAward> hub) {
        this(false, hubMaster, hub);
    }
    public EmployeeAwardHasNotSentFilter(boolean bAllHubs, Hub<EmployeeAward> hubMaster, Hub<EmployeeAward> hubFiltered) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        if (hubMaster == null) this.hubMaster = new Hub<EmployeeAward>(EmployeeAward.class);
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
    public HubFilter<EmployeeAward> getHubFilter() {
        if (filter == null) {
            filter = createHubFilter(hubMaster, hub, bAllHubs);
        }
        return filter;
    }
    protected HubFilter<EmployeeAward> createHubFilter(final Hub<EmployeeAward> hubMaster, Hub<EmployeeAward> hub, boolean bAllHubs) {
        HubFilter<EmployeeAward> filter = new HubFilter<EmployeeAward>(hubMaster, hub) {
            @Override
            public boolean isUsed(EmployeeAward employeeAward) {
                return EmployeeAwardHasNotSentFilter.this.isUsed(employeeAward);
            }
        };
        filter.addDependentProperty(EmployeeAward.P_ItemSentDate);
        filter.addDependentProperty(EmployeeAward.P_Product);
        filter.addDependentProperty(OAString.cpp(EmployeeAward.P_AwardCardOrders, AwardCardOrder.P_SentDate));
 
        if (!bAllHubs) return filter;
        // need to listen to all EmployeeAward
        OAObjectCacheHubAdder hubCacheAdder = new OAObjectCacheHubAdder(hubMaster);
        return filter;
    }

    public boolean isUsed(EmployeeAward employeeAward) {
        // new orders, that have not been sent
        return employeeAward.getHasNotSent();
    }
}
