// Generated by OABuilder
package test.theice.tsam.model.oa.trigger;

import java.util.logging.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.theice.tsam.model.oa.*;
import test.theice.tsam.model.oa.propertypath.*;

import test.theice.tsam.model.oa.MRADClient;
import test.theice.tsam.model.oa.propertypath.MRADClientPP;
import com.viaoa.ds.OASelect;

public class MRADClientAutoRunGetHostInfoTrigger {
    private static Logger LOG = Logger.getLogger(MRADClientAutoRunGetHostInfoTrigger.class.getName());

    private final Hub<MRADClient> hubMaster;
    private final boolean bUseObjectCache;
    private HubTrigger<MRADClient> hubTrigger;
    private OAObjectCacheTrigger<MRADClient> cacheTrigger;

    public MRADClientAutoRunGetHostInfoTrigger() {
        this(null, true);
    }
    public MRADClientAutoRunGetHostInfoTrigger(Hub<MRADClient> hubMaster) {
        this(hubMaster, false);
    }
    public MRADClientAutoRunGetHostInfoTrigger(Hub<MRADClient> hubMaster, boolean bUseObjectCache) {
        LOG.fine("new trigger, hub="+hubMaster+", bUseObjectCache="+bUseObjectCache);
        this.hubMaster = hubMaster;
        this.bUseObjectCache = bUseObjectCache;
        if (hubMaster != null) getHubTrigger();
        if (bUseObjectCache) getObjectCacheTrigger();
    }


    protected HubTrigger<MRADClient> getHubTrigger() {
        if (hubTrigger != null) return hubTrigger;
        if (hubMaster == null) return null;
        hubTrigger = new HubTrigger<MRADClient>(hubMaster) {
            @Override
            public boolean isUsed(MRADClient mradClient) {
                return MRADClientAutoRunGetHostInfoTrigger.this.isUsed(mradClient);
            }
            @Override
            public void onTrigger(MRADClient mradClient) {
                MRADClientAutoRunGetHostInfoTrigger.this.onTrigger(mradClient);
            }
        };
        hubTrigger.addDependentProperty(MRADClientPP.dtConnected());
        hubTrigger.setServerSideOnly(true);
        return hubTrigger;
    }
 
    protected OAObjectCacheTrigger<MRADClient> getObjectCacheTrigger() {
        if (cacheTrigger != null) return cacheTrigger;
        if (!bUseObjectCache) return null;
        cacheTrigger = new OAObjectCacheTrigger<MRADClient>(MRADClient.class) {
            @Override
            public boolean isUsed(MRADClient mradClient) {
                return MRADClientAutoRunGetHostInfoTrigger.this.isUsed(mradClient);
            }
            public void onTrigger(MRADClient mradClient) {
                MRADClientAutoRunGetHostInfoTrigger.this.onTrigger(mradClient);
            }
        };
        cacheTrigger.addDependentProperty(MRADClientPP.dtConnected());
        cacheTrigger.setServerSideOnly(true);
        return cacheTrigger;
    }

    public void initialize() {
        OASelect<MRADClient> sel = new OASelect<MRADClient>(MRADClient.class);
        sel.select("");
        while (sel.hasMore()) {
            MRADClient mradClient = sel.next();
            if (isUsed(mradClient)) {
                onTrigger(mradClient);
            }
        }
        sel.close();
    }

    // ==================
    // these have custom code that will need to be put in OABuilder

    /** 
     * filter for hub, called when object is added or a dependent propertyPath is changed.
     * @return true to call onTrigger, false otherwise.
     */
    public boolean isUsed(MRADClient mradClient) {
        boolean bResult = true;
        // dtConnected
        OADateTime dtConnected = mradClient.getDtConnected();
    
        if (dtConnected == null) bResult = false;
        return bResult;
    }
    
    /** 
     * called after isUsed() returns true.
     */
    public void onTrigger(MRADClient mradClient) {
        if (mradClient == null) return;
        OADateTime dtConnected = mradClient.getDtConnected();
        if (dtConnected == null) return;
        LOG.fine("trigger called, clientName="+mradClient.getName()+", dtConnected="+dtConnected);
    }
    
}
