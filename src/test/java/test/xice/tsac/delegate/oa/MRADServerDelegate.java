package test.xice.tsac.delegate.oa;


import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;

import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.*;

public class MRADServerDelegate {

    public static MRADServer getAdminServer(Silo silo, String ipAddress, String hostName, String routerName, int tradingSystemId, boolean bAutoCreate) {

        if (silo == null) return null;

        return silo.getEnvironment().getMRADServer();

        /*
         * qqqqq Server server = ServerDelegate.getServerUsingIPAddress(silo, ipAddress, false); if
         * (server == null) { server = ServerDelegate.getServerUsingHostName(silo, hostName, false); if
         * (server == null) { server = ServerDelegate.getServerUsingName(silo, routerName); if (server
         * == null) { server = ServerDelegate.getServerUsingTradingSystemId(silo, tradingSystemId); } }
         * } if (server == null) { if (!bAutoCreate) return null; server = new Server();
         * server.setSilo(silo); }
         * 
         * if (!OAString.isEmpty(ipAddress) && OAString.isEmpty(server.getIpAddress())) {
         * server.setIpAddress(ipAddress); } if (!OAString.isEmpty(hostName) &&
         * OAString.isEmpty(server.getHostName())) { server.setHostName(hostName); } if
         * (!OAString.isEmpty(routerName) && OAString.isEmpty(server.getName())) {
         * server.setName(routerName); }
         * 
         * ApplicationType appType = ApplicationTypeDelegate.getAdminServerApplicationType();
         * 
         * AdminServer adminServer = null; for (AdminServer serv : silo.getAdminServers()) { Application
         * app = serv.getApplication(); if (app == null) continue; if (app.getServer() == server) {
         * adminServer = serv; ApplicationType atx = app.getApplicationType(); if (atx != null) { if
         * (atx == appType) break; adminServer = null; } } } if (adminServer != null) return
         * adminServer; if (!bAutoCreate) return null;
         * 
         * // find application Application application = null; if (appType != null) { for (Application
         * app : server.getApplications()) { ApplicationType atx = app.getApplicationType(); if (atx !=
         * null && atx == appType) { application = app; break; } } }
         * 
         * if (application == null) { if (appType == null) return null; application = new Application();
         * application.setApplicationType(appType); application.setServer(server); }
         * 
         * adminServer = new AdminServer(); adminServer.setApplication(application);
         * adminServer.setSilo(silo);
         * 
         * return adminServer;
         */
    }
}
