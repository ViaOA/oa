// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class GCIConnectionPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public GCIConnectionPPx(String name) {
        this(null, name);
    }

    public GCIConnectionPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null && name.length() > 0) {
            if (s.length() > 0 && name.charAt(0) != ':') s += ".";
            s += name;
        }
        pp = s;
    }

    public GSMRServerPPx gsmrServer() {
        GSMRServerPPx ppx = new GSMRServerPPx(this, GCIConnection.P_GSMRServer);
        return ppx;
    }

    public GSMRWarningPPx gsmrWarnings() {
        GSMRWarningPPx ppx = new GSMRWarningPPx(this, GCIConnection.P_GSMRWarnings);
        return ppx;
    }

    public GSRequestPPx gSRequests() {
        GSRequestPPx ppx = new GSRequestPPx(this, GCIConnection.P_GSRequests);
        return ppx;
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
 
