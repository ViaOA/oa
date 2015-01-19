// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa.filter;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class EmployeeAwardFilter extends OAObject {
    private static final long serialVersionUID = 1L;


    public HubFilter createHasNotSentFilter(Hub<EmployeeAward> hubMaster, Hub<EmployeeAward> hub) {
        return createHasNotSentFilter(hubMaster, hub, false);
    }
    public HubFilter createHasNotSentFilter(final Hub<EmployeeAward> hubMaster, Hub<EmployeeAward> hub, boolean bAllHubs) {
        HubFilter filter = new HubFilter(hubMaster, hub) {
            @Override
            public boolean isUsed(Object object) {
                EmployeeAward employeeAward = (EmployeeAward) object;
                return isUsedForHasNotSentFilter(employeeAward);
            }
        };
        filter.addDependentProperty(EmployeeAward.PROPERTY_HasNotSent);
 
        if (!bAllHubs) return filter;
        filter.setServerSideOnly(true); 
        // need to listen to all EmployeeAward
        HubCacheAdder hubCacheAdder = new HubCacheAdder(hubMaster);
        return filter;
    }

    public boolean isUsedForHasNotSentFilter(EmployeeAward employeeAward) {
        // new orders, that have not been sent
        return employeeAward.getHasNotSent();
    }
}
