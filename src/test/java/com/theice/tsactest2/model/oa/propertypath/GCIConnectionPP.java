// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import com.theice.tsactest2.model.oa.*;
 
public class GCIConnectionPP {
    private static GSMRServerPPx gsmrServer;
    private static GSMRWarningPPx gsmrWarnings;
    private static GSRequestPPx gSRequests;
     

    public static GSMRServerPPx gsmrServer() {
        if (gsmrServer == null) gsmrServer = new GSMRServerPPx(GCIConnection.P_GSMRServer);
        return gsmrServer;
    }

    public static GSMRWarningPPx gsmrWarnings() {
        if (gsmrWarnings == null) gsmrWarnings = new GSMRWarningPPx(GCIConnection.P_GSMRWarnings);
        return gsmrWarnings;
    }

    public static GSRequestPPx gSRequests() {
        if (gSRequests == null) gSRequests = new GSRequestPPx(GCIConnection.P_GSRequests);
        return gSRequests;
    }

    public static String id() {
        String s = GCIConnection.P_Id;
        return s;
    }

    public static String created() {
        String s = GCIConnection.P_Created;
        return s;
    }

    public static String connectionId() {
        String s = GCIConnection.P_ConnectionId;
        return s;
    }

    public static String isHeavyConnection() {
        String s = GCIConnection.P_IsHeavyConnection;
        return s;
    }

    public static String connected() {
        String s = GCIConnection.P_Connected;
        return s;
    }

    public static String reconnectCount() {
        String s = GCIConnection.P_ReconnectCount;
        return s;
    }

    public static String currentlyUsed() {
        String s = GCIConnection.P_CurrentlyUsed;
        return s;
    }

    public static String requestCount() {
        String s = GCIConnection.P_RequestCount;
        return s;
    }

    public static String errorCount() {
        String s = GCIConnection.P_ErrorCount;
        return s;
    }

    public static String totalTime() {
        String s = GCIConnection.P_TotalTime;
        return s;
    }
}
 
