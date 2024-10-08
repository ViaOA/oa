// Generated by OABuilder
package test.xice.tsam.model.oa.search;

import java.util.logging.*;

import test.xice.tsam.model.oa.SiloConfigVersioin;
import test.xice.tsam.model.oa.search.SiloConfigVersioinSearch;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.*;

@OAClass(useDataSource=false, localOnly=true)
public class SiloConfigVersioinSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(SiloConfigVersioinSearch.class.getName());


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<SiloConfigVersioin> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<SiloConfigVersioin> f = new OAQueryFilter<SiloConfigVersioin>(SiloConfigVersioin.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<SiloConfigVersioin> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<SiloConfigVersioin> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<SiloConfigVersioin> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<SiloConfigVersioin> sel = new OASelect<SiloConfigVersioin>(SiloConfigVersioin.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<SiloConfigVersioin> filterDataSourceFilter;
    public OAFilter<SiloConfigVersioin> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<SiloConfigVersioin>() {
            @Override
            public boolean isUsed(SiloConfigVersioin siloConfigVersioin) {
                return SiloConfigVersioinSearch.this.isUsedForDataSourceFilter(siloConfigVersioin);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<SiloConfigVersioin> filterCustomFilter;
    public OAFilter<SiloConfigVersioin> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<SiloConfigVersioin>() {
            @Override
            public boolean isUsed(SiloConfigVersioin siloConfigVersioin) {
                boolean b = SiloConfigVersioinSearch.this.isUsedForCustomFilter(siloConfigVersioin);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(siloConfigVersioin);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(SiloConfigVersioin siloConfigVersioin) {
        return true;
    }
    public boolean isUsedForCustomFilter(SiloConfigVersioin siloConfigVersioin) {
        return true;
    }
}
