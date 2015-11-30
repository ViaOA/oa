// Generated by OABuilder
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class TimezoneSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<Timezone> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<Timezone> f = new OAQueryFilter<Timezone>(Timezone.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<Timezone> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<Timezone> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<Timezone> getSelect() {
        String sql = "";
        String sortOrder = Timezone.P_UTCOffset;
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<Timezone> sel = new OASelect<Timezone>(Timezone.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<Timezone> filterDataSourceFilter;
    public OAFilter<Timezone> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<Timezone>() {
            @Override
            public boolean isUsed(Timezone timezone) {
                return TimezoneSearch.this.isUsedForDataSourceFilter(timezone);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<Timezone> filterCustomFilter;
    public OAFilter<Timezone> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<Timezone>() {
            @Override
            public boolean isUsed(Timezone timezone) {
                boolean b = TimezoneSearch.this.isUsedForCustomFilter(timezone);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(timezone);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(Timezone timezone) {
        return true;
    }
    public boolean isUsedForCustomFilter(Timezone timezone) {
        return true;
    }
}
