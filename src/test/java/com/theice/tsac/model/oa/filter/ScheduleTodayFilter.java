// Generated by OABuilder
package com.theice.tsac.model.oa.filter;

import com.theice.tsac.model.oa.*;
import com.theice.tsac.model.oa.propertypath.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;
import com.theice.tsac.delegate.oa.ScheduleDelegate;
import java.util.Calendar;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "Today", displayName = "Today", hasInputParams = false)
public class ScheduleTodayFilter extends OAObject implements CustomHubFilter {
    private static final long serialVersionUID = 1L;


    public static final String PPCode = ":Today()";
    private Hub<Schedule> hubMaster;
    private Hub<Schedule> hub;
    private HubFilter<Schedule> filter;
    private boolean bAllHubs;

    public ScheduleTodayFilter(Hub<Schedule> hub) {
        this(true, null, hub);
    }
    public ScheduleTodayFilter(Hub<Schedule> hubMaster, Hub<Schedule> hub) {
        this(false, hubMaster, hub);
    }
    public ScheduleTodayFilter(boolean bAllHubs, Hub<Schedule> hubMaster, Hub<Schedule> hubFiltered) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        if (hubMaster == null) this.hubMaster = new Hub<Schedule>(Schedule.class);
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
    public HubFilter<Schedule> getHubFilter() {
        if (filter == null) {
            filter = createHubFilter(hubMaster, hub, bAllHubs);
        }
        return filter;
    }
    protected HubFilter<Schedule> createHubFilter(final Hub<Schedule> hubMaster, Hub<Schedule> hub, boolean bAllHubs) {
        HubFilter<Schedule> filter = new HubFilter<Schedule>(hubMaster, hub) {
            @Override
            public boolean isUsed(Schedule schedule) {
                return ScheduleTodayFilter.this.isUsed(schedule);
            }
        };
        filter.addDependentProperty(SchedulePP.isForToday());
 
        if (!bAllHubs) return filter;
        // need to listen to all Schedule
        HubCacheAdder hubCacheAdder = new HubCacheAdder(hubMaster);
        return filter;
    }

    public boolean isUsed(Schedule schedule) {
        boolean bResult = schedule.getIsForToday();
        return bResult;
    }
}
