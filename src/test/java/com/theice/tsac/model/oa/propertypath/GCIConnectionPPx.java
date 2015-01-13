// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class GCIConnectionPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private GSMRServerPPx gsmrServer;
    private GSMRWarningPPx gsmrWarnings;
    private GSRequestPPx gSRequests;
     
    public GCIConnectionPPx(String name) {
        this(null, name);
    }

    public GCIConnectionPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public GSMRServerPPx gsmrServer() {
        if (gsmrServer == null) gsmrServer = new GSMRServerPPx(this, GCIConnection.P_GSMRServer);
        return gsmrServer;
    }

    public GSMRWarningPPx gsmrWarnings() {
        if (gsmrWarnings == null) gsmrWarnings = new GSMRWarningPPx(this, GCIConnection.P_GSMRWarnings);
        return gsmrWarnings;
    }

    public GSRequestPPx gSRequests() {
        if (gSRequests == null) gSRequests = new GSRequestPPx(this, GCIConnection.P_GSRequests);
        return gSRequests;
    }

    public String id() {
        return pp + "." + GCIConnection.P_Id;
    }

    public String created() {
        return pp + "." + GCIConnection.P_Created;
    }

    public String connectionId() {
        return pp + "." + GCIConnection.P_ConnectionId;
    }

    public String isHeavyConnection() {
        return pp + "." + GCIConnection.P_IsHeavyConnection;
    }

    public String connected() {
        return pp + "." + GCIConnection.P_Connected;
    }

    public String reconnectCount() {
        return pp + "." + GCIConnection.P_ReconnectCount;
    }

    public String currentlyUsed() {
        return pp + "." + GCIConnection.P_CurrentlyUsed;
    }

    public String requestCount() {
        return pp + "." + GCIConnection.P_RequestCount;
    }

    public String errorCount() {
        return pp + "." + GCIConnection.P_ErrorCount;
    }

    public String totalTime() {
        return pp + "." + GCIConnection.P_TotalTime;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
