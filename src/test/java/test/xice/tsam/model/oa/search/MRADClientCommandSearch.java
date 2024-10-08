// Generated by OABuilder
package test.xice.tsam.model.oa.search;

import java.util.logging.*;

import test.xice.tsam.model.oa.MRADClientCommand;
import test.xice.tsam.model.oa.search.MRADClientCommandSearch;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

@OAClass(useDataSource=false, localOnly=true)
public class MRADClientCommandSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(MRADClientCommandSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<MRADClientCommand> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<MRADClientCommand> f = new OAQueryFilter<MRADClientCommand>(MRADClientCommand.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<MRADClientCommand> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<MRADClientCommand> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<MRADClientCommand> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<MRADClientCommand> sel = new OASelect<MRADClientCommand>(MRADClientCommand.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<MRADClientCommand> filterDataSourceFilter;
    public OAFilter<MRADClientCommand> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<MRADClientCommand>() {
            @Override
            public boolean isUsed(MRADClientCommand mradClientCommand) {
                return MRADClientCommandSearch.this.isUsedForDataSourceFilter(mradClientCommand);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<MRADClientCommand> filterCustomFilter;
    public OAFilter<MRADClientCommand> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<MRADClientCommand>() {
            @Override
            public boolean isUsed(MRADClientCommand mradClientCommand) {
                boolean b = MRADClientCommandSearch.this.isUsedForCustomFilter(mradClientCommand);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(mradClientCommand);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(MRADClientCommand mradClientCommand) {
        return true;
    }
    public boolean isUsedForCustomFilter(MRADClientCommand mradClientCommand) {
        return true;
    }
}
