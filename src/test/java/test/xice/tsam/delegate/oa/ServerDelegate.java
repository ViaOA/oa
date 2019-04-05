package test.xice.tsam.delegate.oa;

import test.xice.tsam.model.oa.Environment;
import test.xice.tsam.model.oa.Server;
import test.xice.tsam.model.oa.Silo;
import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;
import com.viaoa.util.filter.OAEqualFilter;

import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.propertypath.ServerPP;
import test.xice.tsam.model.oa.propertypath.SiloPP;

public class ServerDelegate {

    public static Server getServer(Silo silo, String hostName, String ipAddress, boolean bAutoCreate) {
        if (silo == null || ipAddress == null) return null;

        Server server = getServerUsingHostName(silo, hostName, false);
        if (server != null) return server;
        
        server = getServerUsingIPAddress(silo, ipAddress, false);
        if (server != null) return server;
        
        if (server == null && bAutoCreate) {
            server = new Server();
            server.setHostName(hostName);
            server.setIpAddress(ipAddress);
            server.setSilo(silo);
        }
        return server;
    }    
    
    
    public static Server getServerUsingHostName(Silo silo, String hostName, boolean bAutoCreate) {
        if (silo == null || hostName == null) return null;
        Server server = silo.getServers().find(Server.P_HostName, hostName);
        if (server == null && bAutoCreate) {
            server = new Server();
            server.setHostName(hostName);
            server.setSilo(silo);
        }
        return server;
    }    
    
    public static Server getServerUsingHostName(Environment env, String hostName) {
        if (env == null || hostName == null) return null;
        OAFinder<Silo, Server> finder = new OAFinder<Silo, Server>(SiloPP.servers().pp);
        finder.addFilter(new OAEqualFilter(Server.P_HostName, hostName));
        
        Server server = finder.findFirst(env.getSilos());
        return server;
    }
    
    
    public static Server getServerUsingIPAddress(Silo silo, String ipAddress, boolean bAutoCreate) {
        if (silo == null || ipAddress == null) return null;
        
        //qqqqqq verify that lookupName matches IP address format
        
        Server server = silo.getServers().find(Server.P_IpAddress, ipAddress);
        if (server == null && bAutoCreate) {
            server = new Server();
            server.setIpAddress(ipAddress);
            server.setSilo(silo);
        }
        return server;
    }    
    public static Server getServerUsingName(Silo silo, String serverName) {
        if (silo == null || serverName == null) return null;
        Server server = silo.getServers().find(Server.P_Name, serverName);
        return server;
    }    
    public static Server getServerUsingTradingSystemId(Silo silo, int tsId) {
        if (silo == null) return null;
        if (tsId < 999) return null;
        Server server = silo.getServers().find(ServerPP.applications().tradingSystemId(), tsId);
        return server;
    }    
    public static Server getServerUsingDnsName(Silo silo, String lookupName) {
        if (silo == null || lookupName == null) return null;
        Server server = silo.getServers().find(Server.P_DnsName, lookupName);
        if (server == null) {
            server = silo.getServers().find(Server.P_ShortDnsName, lookupName);
        }
        return server;
    }    

    
    public static Server getServerUsingLookup(Silo silo, String lookupName) {
        if (silo == null || lookupName == null) return null;
        
        Server server = null;

        int x = OAString.dcount(lookupName, '.');
        if (x > 0) {
            if (x == 4) {
                server = silo.getServers().find(Server.P_IpAddress, lookupName);
            }
            else {
                server = silo.getServers().find(Server.P_DnsName, lookupName);
            }
        }
        else {
            x = OAString.dcount(lookupName, '-');
            if (x == 4) {
                server = silo.getServers().find(Server.P_HostName, lookupName);
            }
            else {
                server = silo.getServers().find(Server.P_ShortDnsName, lookupName);
            }
        }
        
        return server;
    }    
}




