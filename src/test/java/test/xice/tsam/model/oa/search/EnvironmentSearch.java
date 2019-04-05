// Generated by OABuilder
package test.xice.tsam.model.oa.search;

import java.util.logging.*;

import test.xice.tsam.model.oa.Environment;
import test.xice.tsam.model.oa.search.EnvironmentSearch;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

@OAClass(useDataSource=false, localOnly=true)
public class EnvironmentSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(EnvironmentSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<Environment> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<Environment> f = new OAQueryFilter<Environment>(Environment.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<Environment> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<Environment> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<Environment> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<Environment> sel = new OASelect<Environment>(Environment.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<Environment> filterDataSourceFilter;
    public OAFilter<Environment> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<Environment>() {
            @Override
            public boolean isUsed(Environment environment) {
                return EnvironmentSearch.this.isUsedForDataSourceFilter(environment);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<Environment> filterCustomFilter;
    public OAFilter<Environment> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<Environment>() {
            @Override
            public boolean isUsed(Environment environment) {
                boolean b = EnvironmentSearch.this.isUsedForCustomFilter(environment);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(environment);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(Environment environment) {
        return true;
    }
    public boolean isUsedForCustomFilter(Environment environment) {
        return true;
    }
}
