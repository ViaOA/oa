package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.model.oa.Application;
import test.xice.tsac.model.oa.ApplicationType;
import test.xice.tsac.model.oa.LLADServer;
import test.xice.tsac.model.oa.Server;
import test.xice.tsac.model.oa.Silo;

public class LLADServerDelegate {

    
    public static LLADServer getLLADServer(Silo silo, 
            String ipAddress, String hostName, String routerName, int tradingSystemId, boolean bAutoCreate) {
        
        if (silo == null) return null;
        
        Server server = ServerDelegate.getServerUsingIPAddress(silo, ipAddress, false);
        if (server == null) {
            server = ServerDelegate.getServerUsingHostName(silo, hostName, false);
            if (server == null) {
                server = ServerDelegate.getServerUsingName(silo, routerName);
                if (server == null) {
                    server = ServerDelegate.getServerUsingTradingSystemId(silo, tradingSystemId);
                }
            }
        }
        if (server == null) {
            if (!bAutoCreate) return null;
            server = new Server();
            server.setSilo(silo); 
        }

        if (!OAString.isEmpty(ipAddress) && OAString.isEmpty(server.getIpAddress())) {
            server.setIpAddress(ipAddress);
        }
        if (!OAString.isEmpty(hostName) && OAString.isEmpty(server.getHostName())) {
            server.setHostName(hostName);
        }
        if (!OAString.isEmpty(routerName) && OAString.isEmpty(server.getName())) {
            server.setName(routerName);
        }
        
        ApplicationType appType = ApplicationTypeDelegate.getLLADApplicationType();
        
        LLADServer lladServer = null;
        for (LLADServer serv : silo.getLLADServers()) {
            Application app = serv.getApplication();
            if (app == null) continue;
            if (app.getServer() == server) {
                lladServer = serv;
                ApplicationType atx = app.getApplicationType();
                if (atx != null) {
                    if (atx == appType) break;
                    lladServer = null;
                }
            }
        }
        if (lladServer != null) return lladServer;
        if (!bAutoCreate) return null;
        
        // find application
        Application application = null;
        if (appType != null) {
            for (Application app : server.getApplications()) {
                ApplicationType atx = app.getApplicationType();
                if (atx != null && atx == appType) {
                    application = app;
                    break;
                }
            }
        }        
        
        if (application == null) {
            if (appType == null) return null;
            application = new Application();
            application.setApplicationType(appType);
            application.setServer(server);
        }
        
        lladServer = new LLADServer();
        lladServer.setApplication(application);
        lladServer.setSilo(silo);
        
        return lladServer;
    }
}
