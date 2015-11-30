// Generated by OABuilder
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;
import com.viaoa.ds.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;

@OAClass(useDataSource=false, localOnly=true)
public class LLADServerSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<LLADServer> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<LLADServer> f = new OAQueryFilter<LLADServer>(LLADServer.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<LLADServer> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<LLADServer> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<LLADServer> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<LLADServer> sel = new OASelect<LLADServer>(LLADServer.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<LLADServer> filterDataSourceFilter;
    public OAFilter<LLADServer> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<LLADServer>() {
            @Override
            public boolean isUsed(LLADServer lladServer) {
                return LLADServerSearch.this.isUsedForDataSourceFilter(lladServer);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<LLADServer> filterCustomFilter;
    public OAFilter<LLADServer> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<LLADServer>() {
            @Override
            public boolean isUsed(LLADServer lladServer) {
                boolean b = LLADServerSearch.this.isUsedForCustomFilter(lladServer);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(lladServer);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(LLADServer lladServer) {
        return true;
    }
    public boolean isUsedForCustomFilter(LLADServer lladServer) {
        return true;
    }
}
