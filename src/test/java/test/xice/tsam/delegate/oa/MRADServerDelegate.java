package test.xice.tsam.delegate.oa;


import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;

import test.xice.tsam.model.oa.*;

public class MRADServerDelegate {

    public static MRADServer getAdminServer(Silo silo, String ipAddress, String hostName, String routerName, int tradingSystemId, boolean bAutoCreate) {

        if (silo == null) return null;

        return silo.getMRADServer();
    }
}
