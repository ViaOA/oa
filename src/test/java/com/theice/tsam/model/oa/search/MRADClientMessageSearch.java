// Generated by OABuilder
package com.theice.tsam.model.oa.search;

import java.util.logging.*;
import com.theice.tsam.model.oa.*;
import com.theice.tsam.model.oa.propertypath.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

@OAClass(useDataSource=false, localOnly=true)
public class MRADClientMessageSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(MRADClientMessageSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<MRADClientMessage> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<MRADClientMessage> f = new OAQueryFilter<MRADClientMessage>(MRADClientMessage.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<MRADClientMessage> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<MRADClientMessage> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<MRADClientMessage> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<MRADClientMessage> sel = new OASelect<MRADClientMessage>(MRADClientMessage.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<MRADClientMessage> filterDataSourceFilter;
    public OAFilter<MRADClientMessage> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<MRADClientMessage>() {
            @Override
            public boolean isUsed(MRADClientMessage mradClientMessage) {
                return MRADClientMessageSearch.this.isUsedForDataSourceFilter(mradClientMessage);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<MRADClientMessage> filterCustomFilter;
    public OAFilter<MRADClientMessage> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<MRADClientMessage>() {
            @Override
            public boolean isUsed(MRADClientMessage mradClientMessage) {
                boolean b = MRADClientMessageSearch.this.isUsedForCustomFilter(mradClientMessage);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(mradClientMessage);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(MRADClientMessage mradClientMessage) {
        return true;
    }
    public boolean isUsedForCustomFilter(MRADClientMessage mradClientMessage) {
        return true;
    }
}
