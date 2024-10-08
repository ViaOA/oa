// Generated by OABuilder
package test.xice.tsam.model.oa.search;

import java.util.logging.*;

import test.xice.tsam.model.oa.ApplicationGroup;
import test.xice.tsam.model.oa.search.ApplicationGroupSearch;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

@OAClass(useDataSource=false, localOnly=true)
public class ApplicationGroupSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(ApplicationGroupSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<ApplicationGroup> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<ApplicationGroup> f = new OAQueryFilter<ApplicationGroup>(ApplicationGroup.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<ApplicationGroup> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<ApplicationGroup> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<ApplicationGroup> getSelect() {
        String sql = "";
        String sortOrder = ApplicationGroup.P_Seq;
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<ApplicationGroup> sel = new OASelect<ApplicationGroup>(ApplicationGroup.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<ApplicationGroup> filterDataSourceFilter;
    public OAFilter<ApplicationGroup> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<ApplicationGroup>() {
            @Override
            public boolean isUsed(ApplicationGroup applicationGroup) {
                return ApplicationGroupSearch.this.isUsedForDataSourceFilter(applicationGroup);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<ApplicationGroup> filterCustomFilter;
    public OAFilter<ApplicationGroup> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<ApplicationGroup>() {
            @Override
            public boolean isUsed(ApplicationGroup applicationGroup) {
                boolean b = ApplicationGroupSearch.this.isUsedForCustomFilter(applicationGroup);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(applicationGroup);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(ApplicationGroup applicationGroup) {
        return true;
    }
    public boolean isUsedForCustomFilter(ApplicationGroup applicationGroup) {
        return true;
    }
}
