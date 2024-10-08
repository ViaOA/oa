// Generated by OABuilder
package test.hifive.model.oa.filter;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.*;
import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.ProgramPP;

import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "Active", displayName = "Active", hasInputParams = false)
public class ProgramActiveFilter extends OAObject implements CustomHubFilter<Program> {
    private static final long serialVersionUID = 1L;

    public static final String PPCode = ":Active()";
    private Hub<Program> hubMaster;
    private Hub<Program> hub;
    private HubFilter<Program> hubFilter;
    private OAObjectCacheFilter<Program> cacheFilter;
    private boolean bUseObjectCache;

    public ProgramActiveFilter(Hub<Program> hub) {
        this(null, hub, true);
    }
    public ProgramActiveFilter(Hub<Program> hubMaster, Hub<Program> hub) {
        this(hubMaster, hub, false);
    }
    public ProgramActiveFilter(Hub<Program> hubMaster, Hub<Program> hubFiltered, boolean bUseObjectCache) {
        this.hubMaster = hubMaster;
        this.hub = hubFiltered;
        this.bUseObjectCache = bUseObjectCache;
        if (hubMaster != null) getHubFilter();
        if (bUseObjectCache) getObjectCacheFilter();
    }


    public void reset() {
    }

    public boolean isDataEntered() {
        return false;
    }
    public void refresh() {
        if (hubFilter != null) getHubFilter().refresh();
        if (cacheFilter != null) getObjectCacheFilter().refresh();
    }

    @Override
    public HubFilter<Program> getHubFilter() {
        if (hubFilter != null) return hubFilter;
        if (hubMaster == null) return null;
        hubFilter = new HubFilter<Program>(hubMaster, hub) {
            @Override
            public boolean isUsed(Program program) {
                return ProgramActiveFilter.this.isUsed(program);
            }
        };
        hubFilter.addDependentProperty(ProgramPP.inactiveDate());
        hubFilter.addDependentProperty(ProgramPP.beginDate());
        hubFilter.addDependentProperty(ProgramPP.endDate());
        return hubFilter;
    }

    public OAObjectCacheFilter<Program> getObjectCacheFilter() {
        if (cacheFilter != null) return cacheFilter;
        if (!bUseObjectCache) return null;
        cacheFilter = new OAObjectCacheFilter<Program>(hubMaster) {
            @Override
            public boolean isUsed(Program program) {
                return ProgramActiveFilter.this.isUsed(program);
            }
        };
        cacheFilter.addDependentProperty(ProgramPP.inactiveDate());
        cacheFilter.addDependentProperty(ProgramPP.beginDate());
        cacheFilter.addDependentProperty(ProgramPP.endDate());
        return cacheFilter;
    }

    @Override
    public boolean isUsed(Program program) {
        OADate now = new OADate();
        OADate d = program.getInactiveDate();
        if (d != null && d.compareTo(now) <= 0) return false;
        
        d = program.getBeginDate();
        if (d == null) return false;
        return true;
    }
}
