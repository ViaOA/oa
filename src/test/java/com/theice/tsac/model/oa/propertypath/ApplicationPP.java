// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class ApplicationPP {
    private static ApplicationGroupPPx applicationGroups;
    private static ApplicationStatusPPx applicationStatus;
    private static ApplicationTypePPx applicationType;
    private static ApplicationVersionPPx applicationVersions;
    private static GSMRClientPPx gsmrClient;
    private static GSMRServerPPx gsmrServer;
    private static LLADClientPPx lladClient;
    private static LLADServerPPx lladServer;
    private static MRADClientPPx mradClient;
    private static MRADServerPPx mradServer;
    private static RCServerListDetailPPx rcServerListDetails;
    private static SchedulePPx schedules;
    private static ServerPPx server;
    private static ServerFilePPx serverFiles;
     

    public static ApplicationGroupPPx applicationGroups() {
        if (applicationGroups == null) applicationGroups = new ApplicationGroupPPx(Application.P_ApplicationGroups);
        return applicationGroups;
    }

    public static ApplicationStatusPPx applicationStatus() {
        if (applicationStatus == null) applicationStatus = new ApplicationStatusPPx(Application.P_ApplicationStatus);
        return applicationStatus;
    }

    public static ApplicationTypePPx applicationType() {
        if (applicationType == null) applicationType = new ApplicationTypePPx(Application.P_ApplicationType);
        return applicationType;
    }

    public static ApplicationVersionPPx applicationVersions() {
        if (applicationVersions == null) applicationVersions = new ApplicationVersionPPx(Application.P_ApplicationVersions);
        return applicationVersions;
    }

    public static GSMRClientPPx gsmrClient() {
        if (gsmrClient == null) gsmrClient = new GSMRClientPPx(Application.P_GSMRClient);
        return gsmrClient;
    }

    public static GSMRServerPPx gsmrServer() {
        if (gsmrServer == null) gsmrServer = new GSMRServerPPx(Application.P_GSMRServer);
        return gsmrServer;
    }

    public static LLADClientPPx lladClient() {
        if (lladClient == null) lladClient = new LLADClientPPx(Application.P_LLADClient);
        return lladClient;
    }

    public static LLADServerPPx lladServer() {
        if (lladServer == null) lladServer = new LLADServerPPx(Application.P_LLADServer);
        return lladServer;
    }

    public static MRADClientPPx mradClient() {
        if (mradClient == null) mradClient = new MRADClientPPx(Application.P_MRADClient);
        return mradClient;
    }

    public static MRADServerPPx mradServer() {
        if (mradServer == null) mradServer = new MRADServerPPx(Application.P_MRADServer);
        return mradServer;
    }

    public static RCServerListDetailPPx rcServerListDetails() {
        if (rcServerListDetails == null) rcServerListDetails = new RCServerListDetailPPx(Application.P_RCServerListDetails);
        return rcServerListDetails;
    }

    public static SchedulePPx schedules() {
        if (schedules == null) schedules = new SchedulePPx(Application.P_Schedules);
        return schedules;
    }

    public static ServerPPx server() {
        if (server == null) server = new ServerPPx(Application.P_Server);
        return server;
    }

    public static ServerFilePPx serverFiles() {
        if (serverFiles == null) serverFiles = new ServerFilePPx(Application.P_ServerFiles);
        return serverFiles;
    }

    public static String id() {
        String s = Application.P_Id;
        return s;
    }

    public static String instanceNumber() {
        String s = Application.P_InstanceNumber;
        return s;
    }

    public static String tradingSystemId() {
        String s = Application.P_TradingSystemId;
        return s;
    }

    public static String name() {
        String s = Application.P_Name;
        return s;
    }

    public static String userId() {
        String s = Application.P_UserId;
        return s;
    }

    public static String autocomplete() {
        String s = Application.P_Autocomplete;
        return s;
    }

    public static String start() {
        String s = "start";
        return s;
    }

    public static String stop() {
        String s = "stop";
        return s;
    }

    public static String kill() {
        String s = "kill";
        return s;
    }

    public static String suspend() {
        String s = "suspend";
        return s;
    }

    public static String resume() {
        String s = "resume";
        return s;
    }

    public static String ping() {
        String s = "ping";
        return s;
    }
}
 
