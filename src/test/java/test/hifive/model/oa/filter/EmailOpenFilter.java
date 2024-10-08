// Generated by OABuilder
package test.hifive.model.oa.filter;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.*;

import java.util.*;

@OAClass(useDataSource=false, localOnly=true)
@OAClassFilter(name = "Open", displayName = "Open Emails", hasInputParams = false)
public class EmailOpenFilter extends OAObject implements CustomHubFilter<Email> {
    private static final long serialVersionUID = 1L;

    public static final String PPCode = ":Open()";
    private Hub<Email> hubMaster;
    private Hub<Email> hub;
    private HubFilter<Email> hubFilter;
    private OAObjectCacheFilter<Email> cacheFilter;
    private boolean bUseObjectCache;

    public EmailOpenFilter(Hub<Email> hub) {
        this(null, hub, true);
    }
    public EmailOpenFilter(Hub<Email> hubMaster, Hub<Email> hub) {
        this(hubMaster, hub, false);
    }
    public EmailOpenFilter(Hub<Email> hubMaster, Hub<Email> hubFiltered, boolean bUseObjectCache) {
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
    public HubFilter<Email> getHubFilter() {
        if (hubFilter != null) return hubFilter;
        if (hubMaster == null) return null;
        hubFilter = new HubFilter<Email>(hubMaster, hub) {
            @Override
            public boolean isUsed(Email email) {
                return EmailOpenFilter.this.isUsed(email);
            }
        };
        hubFilter.addDependentProperty(EmailPP.sentDateTime(), false);
        hubFilter.addDependentProperty(EmailPP.cancelDate(), false);
        hubFilter.addDependentProperty(EmailPP.fromEmail(), false);
        hubFilter.addDependentProperty(EmailPP.toEmail(), false);
        hubFilter.refresh();
        return hubFilter;
    }

    public OAObjectCacheFilter<Email> getObjectCacheFilter() {
        if (cacheFilter != null) return cacheFilter;
        if (!bUseObjectCache) return null;
        cacheFilter = new OAObjectCacheFilter<Email>(hubMaster) {
            @Override
            public boolean isUsed(Email email) {
                return EmailOpenFilter.this.isUsed(email);
            }
        };
        cacheFilter.addDependentProperty(EmailPP.sentDateTime());
        cacheFilter.addDependentProperty(EmailPP.cancelDate());
        cacheFilter.addDependentProperty(EmailPP.fromEmail());
        cacheFilter.addDependentProperty(EmailPP.toEmail());
        return cacheFilter;
    }

    public boolean isUsed(Email email) {
        return email.isOpen();
    }
}
