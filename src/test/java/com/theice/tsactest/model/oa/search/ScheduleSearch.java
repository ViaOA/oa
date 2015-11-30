// Generated by OABuilder
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;
import com.viaoa.ds.*;

import java.util.Calendar;

@OAClass(useDataSource=false, localOnly=true)
public class ScheduleSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<Schedule> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<Schedule> f = new OAQueryFilter<Schedule>(Schedule.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<Schedule> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<Schedule> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<Schedule> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<Schedule> sel = new OASelect<Schedule>(Schedule.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<Schedule> filterDataSourceFilter;
    public OAFilter<Schedule> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<Schedule>() {
            @Override
            public boolean isUsed(Schedule schedule) {
                return ScheduleSearch.this.isUsedForDataSourceFilter(schedule);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<Schedule> filterCustomFilter;
    public OAFilter<Schedule> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<Schedule>() {
            @Override
            public boolean isUsed(Schedule schedule) {
                boolean b = ScheduleSearch.this.isUsedForCustomFilter(schedule);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(schedule);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(Schedule schedule) {
        return true;
    }
    public boolean isUsedForCustomFilter(Schedule schedule) {
        return true;
    }
}
