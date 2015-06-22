// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class SiloPP {
    private static ApplicationGroupPPx applicationGroups;
    private static EnvironmentPPx environment;
    private static GSMRServerPPx gsmrServers;
    private static LLADServerPPx lladServers;
    private static ServerPPx servers;
    private static SiloConfigPPx siloConfigs;
    private static SiloTypePPx siloType;
     

    public static ApplicationGroupPPx applicationGroups() {
        if (applicationGroups == null) applicationGroups = new ApplicationGroupPPx(Silo.P_ApplicationGroups);
        return applicationGroups;
    }

    public static EnvironmentPPx environment() {
        if (environment == null) environment = new EnvironmentPPx(Silo.P_Environment);
        return environment;
    }

    public static GSMRServerPPx gsmrServers() {
        if (gsmrServers == null) gsmrServers = new GSMRServerPPx(Silo.P_GSMRServers);
        return gsmrServers;
    }

    public static LLADServerPPx lladServers() {
        if (lladServers == null) lladServers = new LLADServerPPx(Silo.P_LLADServers);
        return lladServers;
    }

    public static ServerPPx servers() {
        if (servers == null) servers = new ServerPPx(Silo.P_Servers);
        return servers;
    }

    public static SiloConfigPPx siloConfigs() {
        if (siloConfigs == null) siloConfigs = new SiloConfigPPx(Silo.P_SiloConfigs);
        return siloConfigs;
    }

    public static SiloTypePPx siloType() {
        if (siloType == null) siloType = new SiloTypePPx(Silo.P_SiloType);
        return siloType;
    }

    public static String id() {
        String s = Silo.P_Id;
        return s;
    }

    public static String networkMask() {
        String s = Silo.P_NetworkMask;
        return s;
    }
}
 
