// Generated by OABuilder
package test.xice.tsac3.model.oa.search;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsac3.model.oa.*;

import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class ServerGroupSearch extends OAObject {
    private static final long serialVersionUID = 1L;



    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<ServerGroup> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<ServerGroup> f = new OAQueryFilter<ServerGroup>(ServerGroup.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<ServerGroup> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<ServerGroup> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<ServerGroup> getSelect() {
        String sql = "";
        String sortOrder = ServerGroup.P_Seq;
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<ServerGroup> sel = new OASelect<ServerGroup>(ServerGroup.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<ServerGroup> filterDataSourceFilter;
    public OAFilter<ServerGroup> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<ServerGroup>() {
            @Override
            public boolean isUsed(ServerGroup serverGroup) {
                return ServerGroupSearch.this.isUsedForDataSourceFilter(serverGroup);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<ServerGroup> filterCustomFilter;
    public OAFilter<ServerGroup> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<ServerGroup>() {
            @Override
            public boolean isUsed(ServerGroup serverGroup) {
                boolean b = ServerGroupSearch.this.isUsedForCustomFilter(serverGroup);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(serverGroup);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(ServerGroup serverGroup) {
        return true;
    }
    public boolean isUsedForCustomFilter(ServerGroup serverGroup) {
        return true;
    }
}
