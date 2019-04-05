// Generated by OABuilder
package test.xice.tsac.model.oa.propertypath;
 
import test.xice.tsac.model.oa.*;
 
public class RemoteClientPP {
    private static MRADServerPPx mradServers;
    private static RCExecutePPx rcExecutes;
    private static RCInstalledVersionPPx rcInstalledVersions;
    private static RCRepoVersionPPx rcRepoVersions;
    private static RCServiceListPPx rcServiceLists;
    private static RemoteMessagePPx remoteMessages;
     

    public static MRADServerPPx mradServers() {
        if (mradServers == null) mradServers = new MRADServerPPx(RemoteClient.P_MRADServers);
        return mradServers;
    }

    public static RCExecutePPx rcExecutes() {
        if (rcExecutes == null) rcExecutes = new RCExecutePPx(RemoteClient.P_RCExecutes);
        return rcExecutes;
    }

    public static RCInstalledVersionPPx rcInstalledVersions() {
        if (rcInstalledVersions == null) rcInstalledVersions = new RCInstalledVersionPPx(RemoteClient.P_RCInstalledVersions);
        return rcInstalledVersions;
    }

    public static RCRepoVersionPPx rcRepoVersions() {
        if (rcRepoVersions == null) rcRepoVersions = new RCRepoVersionPPx(RemoteClient.P_RCRepoVersions);
        return rcRepoVersions;
    }

    public static RCServiceListPPx rcServiceLists() {
        if (rcServiceLists == null) rcServiceLists = new RCServiceListPPx(RemoteClient.P_RCServiceLists);
        return rcServiceLists;
    }

    public static RemoteMessagePPx remoteMessages() {
        if (remoteMessages == null) remoteMessages = new RemoteMessagePPx(RemoteClient.P_RemoteMessages);
        return remoteMessages;
    }

    public static String id() {
        String s = RemoteClient.P_Id;
        return s;
    }

    public static String name() {
        String s = RemoteClient.P_Name;
        return s;
    }

    public static String type() {
        String s = RemoteClient.P_Type;
        return s;
    }

    public static String status() {
        String s = RemoteClient.P_Status;
        return s;
    }

    public static String dtStatus() {
        String s = RemoteClient.P_DtStatus;
        return s;
    }

    public static String autoStart() {
        String s = RemoteClient.P_AutoStart;
        return s;
    }

    public static String console() {
        String s = RemoteClient.P_Console;
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
}
 
