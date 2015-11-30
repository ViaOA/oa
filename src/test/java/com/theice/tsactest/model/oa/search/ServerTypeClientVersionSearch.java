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
public class ServerTypeClientVersionSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<ServerTypeClientVersion> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<ServerTypeClientVersion> f = new OAQueryFilter<ServerTypeClientVersion>(ServerTypeClientVersion.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<ServerTypeClientVersion> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<ServerTypeClientVersion> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<ServerTypeClientVersion> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<ServerTypeClientVersion> sel = new OASelect<ServerTypeClientVersion>(ServerTypeClientVersion.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<ServerTypeClientVersion> filterDataSourceFilter;
    public OAFilter<ServerTypeClientVersion> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<ServerTypeClientVersion>() {
            @Override
            public boolean isUsed(ServerTypeClientVersion serverTypeClientVersion) {
                return ServerTypeClientVersionSearch.this.isUsedForDataSourceFilter(serverTypeClientVersion);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<ServerTypeClientVersion> filterCustomFilter;
    public OAFilter<ServerTypeClientVersion> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<ServerTypeClientVersion>() {
            @Override
            public boolean isUsed(ServerTypeClientVersion serverTypeClientVersion) {
                boolean b = ServerTypeClientVersionSearch.this.isUsedForCustomFilter(serverTypeClientVersion);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(serverTypeClientVersion);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(ServerTypeClientVersion serverTypeClientVersion) {
        return true;
    }
    public boolean isUsedForCustomFilter(ServerTypeClientVersion serverTypeClientVersion) {
        return true;
    }
}
