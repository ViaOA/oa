// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class MRADClientPP {
    private static ApplicationPPx application;
    private static MRADClientCommandPPx lastMRADClientCommand;
    private static MRADClientMessagePPx lastMRADClientMessage;
    private static MRADClientCommandPPx mradClientCommands;
    private static MRADClientMessagePPx mradClientMessages;
    private static MRADServerPPx mradServer;
     

    public static ApplicationPPx application() {
        if (application == null) application = new ApplicationPPx(MRADClient.P_Application);
        return application;
    }

    public static MRADClientCommandPPx lastMRADClientCommand() {
        if (lastMRADClientCommand == null) lastMRADClientCommand = new MRADClientCommandPPx(MRADClient.P_LastMRADClientCommand);
        return lastMRADClientCommand;
    }

    public static MRADClientMessagePPx lastMRADClientMessage() {
        if (lastMRADClientMessage == null) lastMRADClientMessage = new MRADClientMessagePPx(MRADClient.P_LastMRADClientMessage);
        return lastMRADClientMessage;
    }

    public static MRADClientCommandPPx mradClientCommands() {
        if (mradClientCommands == null) mradClientCommands = new MRADClientCommandPPx(MRADClient.P_MRADClientCommands);
        return mradClientCommands;
    }

    public static MRADClientMessagePPx mradClientMessages() {
        if (mradClientMessages == null) mradClientMessages = new MRADClientMessagePPx(MRADClient.P_MRADClientMessages);
        return mradClientMessages;
    }

    public static MRADServerPPx mradServer() {
        if (mradServer == null) mradServer = new MRADServerPPx(MRADClient.P_MRADServer);
        return mradServer;
    }

    public static String id() {
        String s = MRADClient.P_Id;
        return s;
    }

    public static String created() {
        String s = MRADClient.P_Created;
        return s;
    }

    public static String hostName() {
        String s = MRADClient.P_HostName;
        return s;
    }

    public static String ipAddress() {
        String s = MRADClient.P_IpAddress;
        return s;
    }

    public static String name() {
        String s = MRADClient.P_Name;
        return s;
    }

    public static String description() {
        String s = MRADClient.P_Description;
        return s;
    }

    public static String routerAbsolutePath() {
        String s = MRADClient.P_RouterAbsolutePath;
        return s;
    }

    public static String startScript() {
        String s = MRADClient.P_StartScript;
        return s;
    }

    public static String stopScript() {
        String s = MRADClient.P_StopScript;
        return s;
    }

    public static String snapshotStartScript() {
        String s = MRADClient.P_SnapshotStartScript;
        return s;
    }

    public static String directory() {
        String s = MRADClient.P_Directory;
        return s;
    }

    public static String version() {
        String s = MRADClient.P_Version;
        return s;
    }

    public static String applicationStatus() {
        String s = MRADClient.P_ApplicationStatus;
        return s;
    }

    public static String started() {
        String s = MRADClient.P_Started;
        return s;
    }

    public static String ready() {
        String s = MRADClient.P_Ready;
        return s;
    }

    public static String serverTypeId() {
        String s = MRADClient.P_ServerTypeId;
        return s;
    }

    public static String applicationTypeCode() {
        String s = MRADClient.P_ApplicationTypeCode;
        return s;
    }

    public static String dtConnected() {
        String s = MRADClient.P_DtConnected;
        return s;
    }

    public static String dtDisconnected() {
        String s = MRADClient.P_DtDisconnected;
        return s;
    }

    public static String totalMemory() {
        String s = MRADClient.P_TotalMemory;
        return s;
    }

    public static String freeMemory() {
        String s = MRADClient.P_FreeMemory;
        return s;
    }

    public static String javaVendor() {
        String s = MRADClient.P_JavaVendor;
        return s;
    }

    public static String javaVersion() {
        String s = MRADClient.P_JavaVersion;
        return s;
    }

    public static String osArch() {
        String s = MRADClient.P_OsArch;
        return s;
    }

    public static String osName() {
        String s = MRADClient.P_OsName;
        return s;
    }

    public static String osVersion() {
        String s = MRADClient.P_OsVersion;
        return s;
    }

    public static String autoComplete() {
        String s = MRADClient.P_AutoComplete;
        return s;
    }
}
 
