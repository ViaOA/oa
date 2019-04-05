package test.xice.tsac.delegate.oa;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.*;

/**
 * GSMRServer related functionality
 * @author vvia
 */
public class GSMRServerDelegate {

    public static GSMRServer getGSMRServer(Silo silo, String hostName, int instanceNumber, boolean bAutoCreate) {
        if (silo == null || hostName == null) return null;

        ApplicationType appType = ApplicationTypeDelegate.getGSMRApplicationType();
        
        Server server = ServerDelegate.getServerUsingHostName(silo, hostName, bAutoCreate);
        if (server == null) return null;
        
        Application application = ApplicationDelegate.getApplication(server, appType, instanceNumber, bAutoCreate);
        if (application == null) return null;
        
        for (GSMRServer gs : silo.getGSMRServers()) {
            if (gs.getApplication() == application) {
                return gs;
            }
        }
        if (!bAutoCreate) return null;
        
        GSMRServer gs = new GSMRServer();
        gs.setApplication(application);
        gs.setSilo(silo);

        return gs;
    }

}
