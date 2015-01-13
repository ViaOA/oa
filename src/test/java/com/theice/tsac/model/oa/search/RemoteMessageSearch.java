// Generated by OABuilder
package com.theice.tsac.model.oa.search;

import com.theice.tsac.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class RemoteMessageSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<RemoteMessage> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<RemoteMessage> f = new OASelectFilter<RemoteMessage>(RemoteMessage.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<RemoteMessage> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<RemoteMessage> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<RemoteMessage> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<RemoteMessage> sel = new OASelect<RemoteMessage>(RemoteMessage.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<RemoteMessage> filterDataSourceFilter;
    public OAFilter<RemoteMessage> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<RemoteMessage>() {
            @Override
            public boolean isUsed(RemoteMessage remoteMessage) {
                return RemoteMessageSearch.this.isUsedForDataSourceFilter(remoteMessage);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<RemoteMessage> filterCustomFilter;
    public OAFilter<RemoteMessage> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<RemoteMessage>() {
            @Override
            public boolean isUsed(RemoteMessage remoteMessage) {
                boolean b = RemoteMessageSearch.this.isUsedForCustomFilter(remoteMessage);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(remoteMessage);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(RemoteMessage remoteMessage) {
        return true;
    }
    public boolean isUsedForCustomFilter(RemoteMessage remoteMessage) {
        return true;
    }
}
