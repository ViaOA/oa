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
public class GSMRClientSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<GSMRClient> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<GSMRClient> f = new OAQueryFilter<GSMRClient>(GSMRClient.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<GSMRClient> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<GSMRClient> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<GSMRClient> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<GSMRClient> sel = new OASelect<GSMRClient>(GSMRClient.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<GSMRClient> filterDataSourceFilter;
    public OAFilter<GSMRClient> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<GSMRClient>() {
            @Override
            public boolean isUsed(GSMRClient gsmrClient) {
                return GSMRClientSearch.this.isUsedForDataSourceFilter(gsmrClient);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<GSMRClient> filterCustomFilter;
    public OAFilter<GSMRClient> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<GSMRClient>() {
            @Override
            public boolean isUsed(GSMRClient gsmrClient) {
                boolean b = GSMRClientSearch.this.isUsedForCustomFilter(gsmrClient);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(gsmrClient);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(GSMRClient gsmrClient) {
        return true;
    }
    public boolean isUsedForCustomFilter(GSMRClient gsmrClient) {
        return true;
    }
}
