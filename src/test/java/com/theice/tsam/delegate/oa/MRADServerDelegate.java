package com.theice.tsam.delegate.oa;


import com.theice.tsam.model.oa.MRADServer;
import com.theice.tsam.model.oa.Silo;
import com.theice.tsam.model.oa.*;
import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;

public class MRADServerDelegate {

    public static MRADServer getAdminServer(Silo silo, String ipAddress, String hostName, String routerName, int tradingSystemId, boolean bAutoCreate) {

        if (silo == null) return null;

        return silo.getMRADServer();
    }
}
