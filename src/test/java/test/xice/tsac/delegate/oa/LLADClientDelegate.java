package test.xice.tsac.delegate.oa;

import com.viaoa.util.OAString;

import test.xice.tsac.model.oa.*;

public class LLADClientDelegate {

    public static LLADClient getLLADClient(LLADServer lladServer, 
            String ipAddress, String hostName, String routerName, int tradingSystemId, String routerType, boolean bAutoCreate) {
        
        if (lladServer == null) return null;
        
        Silo silo = lladServer.getSilo();
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
        
        ApplicationType appType = ApplicationTypeDelegate.getApplicationTypeUsingCode(routerType, true);
        
        LLADClient lladClient = null;
        for (LLADClient c : lladServer.getLLADClients()) {
            Application app = c.getApplication();
            if (app == null) continue;
            if (app.getServer() == server) {
                lladClient = c;
                ApplicationType atx = app.getApplicationType();
                if (atx != null) {
                    if (atx == appType) break;
                    lladClient = null;
                }
            }
        }
        if (lladClient != null) return lladClient;
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
        
        lladClient = new LLADClient();
        lladClient.setApplication(application);
        lladClient.setLLADServer(lladServer);
        
        
        lladClient = new LLADClient();
        lladClient.setIpAddress(ipAddress);
        lladClient.setRouterName(routerName);
        lladClient.setRouterType(routerType);
        
        return lladClient;
    }
}
