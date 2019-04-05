// Generated by OABuilder
package test.xice.tsam.model.oa.search;

import java.util.logging.*;

import test.xice.tsam.model.oa.PackageType;
import test.xice.tsam.model.oa.search.PackageTypeSearch;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

@OAClass(useDataSource=false, localOnly=true)
public class PackageTypeSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(PackageTypeSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<PackageType> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<PackageType> f = new OAQueryFilter<PackageType>(PackageType.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<PackageType> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<PackageType> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<PackageType> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<PackageType> sel = new OASelect<PackageType>(PackageType.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<PackageType> filterDataSourceFilter;
    public OAFilter<PackageType> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<PackageType>() {
            @Override
            public boolean isUsed(PackageType packageType) {
                return PackageTypeSearch.this.isUsedForDataSourceFilter(packageType);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<PackageType> filterCustomFilter;
    public OAFilter<PackageType> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<PackageType>() {
            @Override
            public boolean isUsed(PackageType packageType) {
                boolean b = PackageTypeSearch.this.isUsedForCustomFilter(packageType);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(packageType);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(PackageType packageType) {
        return true;
    }
    public boolean isUsedForCustomFilter(PackageType packageType) {
        return true;
    }
}
