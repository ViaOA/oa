// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import java.io.Serializable;
import com.theice.tsac.model.oa.*;
 
public class GSMRServerPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private GCIConnectionPPx gciConnections;
    private GSMRClientPPx gsmrClients;
    private GSMRWarningPPx gsmrWarnings;
    private ServerPPx server;
    private SiloPPx silo;
     
    public GSMRServerPPx(String name) {
        this(null, name);
    }

    public GSMRServerPPx(PPxInterface parent, String name) {
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

    public GCIConnectionPPx gciConnections() {
        if (gciConnections == null) gciConnections = new GCIConnectionPPx(this, GSMRServer.P_GCIConnections);
        return gciConnections;
    }

    public GSMRClientPPx gsmrClients() {
        if (gsmrClients == null) gsmrClients = new GSMRClientPPx(this, GSMRServer.P_GSMRClients);
        return gsmrClients;
    }

    public GSMRWarningPPx gsmrWarnings() {
        if (gsmrWarnings == null) gsmrWarnings = new GSMRWarningPPx(this, GSMRServer.P_GSMRWarnings);
        return gsmrWarnings;
    }

    public ServerPPx server() {
        if (server == null) server = new ServerPPx(this, GSMRServer.P_Server);
        return server;
    }

    public SiloPPx silo() {
        if (silo == null) silo = new SiloPPx(this, GSMRServer.P_Silo);
        return silo;
    }

    public String id() {
        return pp + "." + GSMRServer.P_Id;
    }

    public String instanceNumber() {
        return pp + "." + GSMRServer.P_InstanceNumber;
    }

    public String gemstoneName() {
        return pp + "." + GSMRServer.P_GemstoneName;
    }

    public String gemstoneHost() {
        return pp + "." + GSMRServer.P_GemstoneHost;
    }

    public String gemstoneUserId() {
        return pp + "." + GSMRServer.P_GemstoneUserId;
    }

    public String gemstonePassword() {
        return pp + "." + GSMRServer.P_GemstonePassword;
    }

    public String uniqueName() {
        return pp + "." + GSMRServer.P_UniqueName;
    }

    public String iflLogLevel() {
        return pp + "." + GSMRServer.P_IflLogLevel;
    }

    public String totalConnectionCount() {
        return pp + "." + GSMRServer.P_TotalConnectionCount;
    }

    public String heavyConnectionCount() {
        return pp + "." + GSMRServer.P_HeavyConnectionCount;
    }

    public String usedConnections() {
        return pp + "." + GSMRServer.P_UsedConnections;
    }

    public String maxUsedConnections() {
        return pp + "." + GSMRServer.P_MaxUsedConnections;
    }

    public String allUsedCounections() {
        return pp + "." + GSMRServer.P_AllUsedCounections;
    }

    public String logBinaryInput() {
        return pp + "." + GSMRServer.P_LogBinaryInput;
    }

    public String logBinaryOutput() {
        return pp + "." + GSMRServer.P_LogBinaryOutput;
    }

    public String status() {
        return pp + "." + GSMRServer.P_Status;
    }

    public String logsLoaded() {
        return pp + "." + GSMRServer.P_LogsLoaded;
    }

    public String clientCount() {
        return pp + "." + GSMRServer.P_ClientCount;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
