// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class MRADClientPP {
    private static MRADServerPPx mradServer;
    private static SchedulePPx schedules;
    private static ServerPPx server;
    private static ServerGroupPPx serverGroup;
     

    public static MRADServerPPx mradServer() {
        if (mradServer == null) mradServer = new MRADServerPPx(MRADClient.P_MRADServer);
        return mradServer;
    }

    public static SchedulePPx schedules() {
        if (schedules == null) schedules = new SchedulePPx(MRADClient.P_Schedules);
        return schedules;
    }

    public static ServerPPx server() {
        if (server == null) server = new ServerPPx(MRADClient.P_Server);
        return server;
    }

    public static ServerGroupPPx serverGroup() {
        if (serverGroup == null) serverGroup = new ServerGroupPPx(MRADClient.P_ServerGroup);
        return serverGroup;
    }

    public static String id() {
        String s = MRADClient.P_Id;
        return s;
    }

    public static String directory() {
        String s = MRADClient.P_Directory;
        return s;
    }

    public static String startCommand() {
        String s = MRADClient.P_StartCommand;
        return s;
    }

    public static String stopCommand() {
        String s = MRADClient.P_StopCommand;
        return s;
    }

    public static String requestPending() {
        String s = MRADClient.P_RequestPending;
        return s;
    }

    public static String isOkToStart() {
        String s = MRADClient.P_IsOkToStart;
        return s;
    }

    public static String isRemoteClientConnected() {
        String s = MRADClient.P_IsRemoteClientConnected;
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
 
