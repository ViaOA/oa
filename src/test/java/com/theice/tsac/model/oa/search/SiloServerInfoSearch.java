// Generated by OABuilder
package com.theice.tsac.model.oa.search;

import com.theice.tsac.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class SiloServerInfoSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<SiloServerInfo> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<SiloServerInfo> f = new OASelectFilter<SiloServerInfo>(SiloServerInfo.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<SiloServerInfo> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<SiloServerInfo> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<SiloServerInfo> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<SiloServerInfo> sel = new OASelect<SiloServerInfo>(SiloServerInfo.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<SiloServerInfo> filterDataSourceFilter;
    public OAFilter<SiloServerInfo> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<SiloServerInfo>() {
            @Override
            public boolean isUsed(SiloServerInfo siloServerInfo) {
                return SiloServerInfoSearch.this.isUsedForDataSourceFilter(siloServerInfo);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<SiloServerInfo> filterCustomFilter;
    public OAFilter<SiloServerInfo> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<SiloServerInfo>() {
            @Override
            public boolean isUsed(SiloServerInfo siloServerInfo) {
                boolean b = SiloServerInfoSearch.this.isUsedForCustomFilter(siloServerInfo);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(siloServerInfo);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(SiloServerInfo siloServerInfo) {
        return true;
    }
    public boolean isUsedForCustomFilter(SiloServerInfo siloServerInfo) {
        return true;
    }
}
