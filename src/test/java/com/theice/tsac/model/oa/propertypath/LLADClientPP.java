// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class LLADClientPP {
    private static LLADServerPPx lladServer;
    private static ServerPPx server;
    private static UserLoginHistoryPPx userLoginHistories;
    private static UserLoginPPx userLogins;
     

    public static LLADServerPPx lladServer() {
        if (lladServer == null) lladServer = new LLADServerPPx(LLADClient.P_LLADServer);
        return lladServer;
    }

    public static ServerPPx server() {
        if (server == null) server = new ServerPPx(LLADClient.P_Server);
        return server;
    }

    public static UserLoginHistoryPPx userLoginHistories() {
        if (userLoginHistories == null) userLoginHistories = new UserLoginHistoryPPx(LLADClient.P_UserLoginHistories);
        return userLoginHistories;
    }

    public static UserLoginPPx userLogins() {
        if (userLogins == null) userLogins = new UserLoginPPx(LLADClient.P_UserLogins);
        return userLogins;
    }

    public static String id() {
        String s = LLADClient.P_Id;
        return s;
    }

    public static String routerName() {
        String s = LLADClient.P_RouterName;
        return s;
    }

    public static String routerType() {
        String s = LLADClient.P_RouterType;
        return s;
    }

    public static String ipAddress() {
        String s = LLADClient.P_IpAddress;
        return s;
    }

    public static String startedDateTime() {
        String s = LLADClient.P_StartedDateTime;
        return s;
    }

    public static String registeredDateTime() {
        String s = LLADClient.P_RegisteredDateTime;
        return s;
    }

    public static String lastPingDateTime() {
        String s = LLADClient.P_LastPingDateTime;
        return s;
    }

    public static String lastHeartbeatDateTime() {
        String s = LLADClient.P_LastHeartbeatDateTime;
        return s;
    }

    public static String status() {
        String s = LLADClient.P_Status;
        return s;
    }

    public static String activeMode() {
        String s = LLADClient.P_ActiveMode;
        return s;
    }

    public static String viewOnly() {
        String s = LLADClient.P_ViewOnly;
        return s;
    }

    public static String serverVersion() {
        String s = LLADClient.P_ServerVersion;
        return s;
    }

    public static String idlVersion() {
        String s = LLADClient.P_IdlVersion;
        return s;
    }

    public static String enableLLADCommands() {
        String s = LLADClient.P_EnableLLADCommands;
        return s;
    }

    public static String forceLogoutAllUsers() {
        String s = "forceLogoutAllUsers";
        return s;
    }
}
 
