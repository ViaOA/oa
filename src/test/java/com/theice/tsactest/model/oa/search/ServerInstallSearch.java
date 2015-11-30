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
public class ServerInstallSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<ServerInstall> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<ServerInstall> f = new OAQueryFilter<ServerInstall>(ServerInstall.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<ServerInstall> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<ServerInstall> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<ServerInstall> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<ServerInstall> sel = new OASelect<ServerInstall>(ServerInstall.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<ServerInstall> filterDataSourceFilter;
    public OAFilter<ServerInstall> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<ServerInstall>() {
            @Override
            public boolean isUsed(ServerInstall serverInstall) {
                return ServerInstallSearch.this.isUsedForDataSourceFilter(serverInstall);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<ServerInstall> filterCustomFilter;
    public OAFilter<ServerInstall> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<ServerInstall>() {
            @Override
            public boolean isUsed(ServerInstall serverInstall) {
                boolean b = ServerInstallSearch.this.isUsedForCustomFilter(serverInstall);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(serverInstall);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(ServerInstall serverInstall) {
        return true;
    }
    public boolean isUsedForCustomFilter(ServerInstall serverInstall) {
        return true;
    }
}
