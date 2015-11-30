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
public class RemoteClientSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<RemoteClient> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<RemoteClient> f = new OAQueryFilter<RemoteClient>(RemoteClient.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<RemoteClient> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<RemoteClient> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<RemoteClient> getSelect() {
        String sql = "";
        String sortOrder = RemoteClient.P_Type;
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<RemoteClient> sel = new OASelect<RemoteClient>(RemoteClient.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<RemoteClient> filterDataSourceFilter;
    public OAFilter<RemoteClient> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<RemoteClient>() {
            @Override
            public boolean isUsed(RemoteClient remoteClient) {
                return RemoteClientSearch.this.isUsedForDataSourceFilter(remoteClient);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<RemoteClient> filterCustomFilter;
    public OAFilter<RemoteClient> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<RemoteClient>() {
            @Override
            public boolean isUsed(RemoteClient remoteClient) {
                boolean b = RemoteClientSearch.this.isUsedForCustomFilter(remoteClient);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(remoteClient);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(RemoteClient remoteClient) {
        return true;
    }
    public boolean isUsedForCustomFilter(RemoteClient remoteClient) {
        return true;
    }
}
