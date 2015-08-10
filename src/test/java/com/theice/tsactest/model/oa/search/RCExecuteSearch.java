// Generated by OABuilder
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class RCExecuteSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<RCExecute> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<RCExecute> f = new OASelectFilter<RCExecute>(RCExecute.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<RCExecute> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<RCExecute> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<RCExecute> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<RCExecute> sel = new OASelect<RCExecute>(RCExecute.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<RCExecute> filterDataSourceFilter;
    public OAFilter<RCExecute> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<RCExecute>() {
            @Override
            public boolean isUsed(RCExecute rcExecute) {
                return RCExecuteSearch.this.isUsedForDataSourceFilter(rcExecute);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<RCExecute> filterCustomFilter;
    public OAFilter<RCExecute> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<RCExecute>() {
            @Override
            public boolean isUsed(RCExecute rcExecute) {
                boolean b = RCExecuteSearch.this.isUsedForCustomFilter(rcExecute);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(rcExecute);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(RCExecute rcExecute) {
        return true;
    }
    public boolean isUsedForCustomFilter(RCExecute rcExecute) {
        return true;
    }
}
