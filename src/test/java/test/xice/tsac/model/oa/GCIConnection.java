// Generated by OABuilder
package test.xice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;

import test.xice.tsac.model.oa.filter.*;
import test.xice.tsac.model.oa.propertypath.*;
 
@OAClass(
    shortName = "gcic",
    displayName = "GCIConnection",
    displayProperty = "connectionId",
    sortProperty = "connectionId"
)
@OATable(
    indexes = {
        @OAIndex(name = "GCIConnectionGsmrServer", columns = { @OAIndexColumn(name = "GsmrServerId") })
    }
)
public class GCIConnection extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_ConnectionId = "ConnectionId";
    public static final String P_ConnectionId = "ConnectionId";
    public static final String PROPERTY_IsHeavyConnection = "IsHeavyConnection";
    public static final String P_IsHeavyConnection = "IsHeavyConnection";
    public static final String PROPERTY_Connected = "Connected";
    public static final String P_Connected = "Connected";
    public static final String PROPERTY_ReconnectCount = "ReconnectCount";
    public static final String P_ReconnectCount = "ReconnectCount";
    public static final String PROPERTY_CurrentlyUsed = "CurrentlyUsed";
    public static final String P_CurrentlyUsed = "CurrentlyUsed";
    public static final String PROPERTY_RequestCount = "RequestCount";
    public static final String P_RequestCount = "RequestCount";
    public static final String PROPERTY_ErrorCount = "ErrorCount";
    public static final String P_ErrorCount = "ErrorCount";
    public static final String PROPERTY_TotalTime = "TotalTime";
    public static final String P_TotalTime = "TotalTime";
     
     
    public static final String PROPERTY_GSMRServer = "GSMRServer";
    public static final String P_GSMRServer = "GSMRServer";
    public static final String PROPERTY_GSMRWarnings = "GSMRWarnings";
    public static final String P_GSMRWarnings = "GSMRWarnings";
    public static final String PROPERTY_GSRequests = "GSRequests";
    public static final String P_GSRequests = "GSRequests";
     
    protected int id;
    protected OADateTime created;
    protected int connectionId;
    protected boolean isHeavyConnection;
    protected boolean connected;
    protected int reconnectCount;
    protected boolean currentlyUsed;
    protected int requestCount;
    protected int errorCount;
    protected long totalTime;
     
    // Links to other objects.
    protected transient GSMRServer gsmrServer;
    protected transient Hub<GSMRWarning> hubGSMRWarnings;
    protected transient Hub<GSRequest> hubGSRequests;
     
    public GCIConnection() {
    }
     
    public GCIConnection(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5, isProcessed = true)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        fireBeforePropertyChange(P_Id, this.id, newValue);
        int old = id;
        this.id = newValue;
        firePropertyChange(P_Id, old, this.id);
    }
    @OAProperty(displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getCreated() {
        return created;
    }
    
    public void setCreated(OADateTime newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADateTime old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(displayName = "Connection Id", displayLength = 3, columnName = "Id", isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(int newValue) {
        fireBeforePropertyChange(P_ConnectionId, this.connectionId, newValue);
        int old = connectionId;
        this.connectionId = newValue;
        firePropertyChange(P_ConnectionId, old, this.connectionId);
    }
    @OAProperty(displayName = "Is Heavy Connection", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getIsHeavyConnection() {
        return isHeavyConnection;
    }
    
    public void setIsHeavyConnection(boolean newValue) {
        fireBeforePropertyChange(P_IsHeavyConnection, this.isHeavyConnection, newValue);
        boolean old = isHeavyConnection;
        this.isHeavyConnection = newValue;
        firePropertyChange(P_IsHeavyConnection, old, this.isHeavyConnection);
    }
    @OAProperty(displayLength = 5, columnLength = 10, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getConnected() {
        return connected;
    }
    
    public void setConnected(boolean newValue) {
        fireBeforePropertyChange(P_Connected, this.connected, newValue);
        boolean old = connected;
        this.connected = newValue;
        firePropertyChange(P_Connected, old, this.connected);
    }
    @OAProperty(displayName = "Reconnect Count", description = "total times the GCI was reconnected", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    /**
      total times the GCI was reconnected
    */
    public int getReconnectCount() {
        return reconnectCount;
    }
    
    public void setReconnectCount(int newValue) {
        fireBeforePropertyChange(P_ReconnectCount, this.reconnectCount, newValue);
        int old = reconnectCount;
        this.reconnectCount = newValue;
        firePropertyChange(P_ReconnectCount, old, this.reconnectCount);
    }
    @OAProperty(displayName = "Currently Used", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getCurrentlyUsed() {
        return currentlyUsed;
    }
    
    public void setCurrentlyUsed(boolean newValue) {
        fireBeforePropertyChange(P_CurrentlyUsed, this.currentlyUsed, newValue);
        boolean old = currentlyUsed;
        this.currentlyUsed = newValue;
        firePropertyChange(P_CurrentlyUsed, old, this.currentlyUsed);
    }
    @OAProperty(displayName = "Request Count", displayLength = 5, columnLength = 7, columnName = "Requests", isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getRequestCount() {
        return requestCount;
    }
    
    public void setRequestCount(int newValue) {
        fireBeforePropertyChange(P_RequestCount, this.requestCount, newValue);
        int old = requestCount;
        this.requestCount = newValue;
        firePropertyChange(P_RequestCount, old, this.requestCount);
    }
    @OAProperty(displayName = "Error Count", displayLength = 5, columnLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getErrorCount() {
        return errorCount;
    }
    
    public void setErrorCount(int newValue) {
        fireBeforePropertyChange(P_ErrorCount, this.errorCount, newValue);
        int old = errorCount;
        this.errorCount = newValue;
        firePropertyChange(P_ErrorCount, old, this.errorCount);
    }
    @OAProperty(displayName = "Total Time (ms)", displayLength = 7, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(long newValue) {
        fireBeforePropertyChange(P_TotalTime, this.totalTime, newValue);
        long old = totalTime;
        this.totalTime = newValue;
        firePropertyChange(P_TotalTime, old, this.totalTime);
    }
    @OAOne(
        reverseName = GSMRServer.P_GCIConnections, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"GsmrServerId"})
    public GSMRServer getGSMRServer() {
        if (gsmrServer == null) {
            gsmrServer = (GSMRServer) getObject(P_GSMRServer);
        }
        return gsmrServer;
    }
    
    public void setGSMRServer(GSMRServer newValue) {
        fireBeforePropertyChange(P_GSMRServer, this.gsmrServer, newValue);
        GSMRServer old = this.gsmrServer;
        this.gsmrServer = newValue;
        firePropertyChange(P_GSMRServer, old, this.gsmrServer);
    }
    
    @OAMany(
        toClass = GSMRWarning.class, 
        reverseName = GSMRWarning.P_GCIConnection
    )
    public Hub<GSMRWarning> getGSMRWarnings() {
        if (hubGSMRWarnings == null) {
            hubGSMRWarnings = (Hub<GSMRWarning>) getHub(P_GSMRWarnings);
        }
        return hubGSMRWarnings;
    }
    
    @OAMany(
        toClass = GSRequest.class, 
        reverseName = GSRequest.P_GCIConnection
    )
    public Hub<GSRequest> getGSRequests() {
        if (hubGSRequests == null) {
            hubGSRequests = (Hub<GSRequest>) getHub(P_GSRequests);
        }
        return hubGSRequests;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        this.connectionId = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_ConnectionId, true);
        }
        this.isHeavyConnection = rs.getBoolean(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_IsHeavyConnection, true);
        }
        this.connected = rs.getBoolean(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_Connected, true);
        }
        this.reconnectCount = (int) rs.getInt(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_ReconnectCount, true);
        }
        this.currentlyUsed = rs.getBoolean(7);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_CurrentlyUsed, true);
        }
        this.requestCount = (int) rs.getInt(8);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_RequestCount, true);
        }
        this.errorCount = (int) rs.getInt(9);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_ErrorCount, true);
        }
        this.totalTime = (long) rs.getLong(10);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, GCIConnection.P_TotalTime, true);
        }
        int gsmrServerFkey = rs.getInt(11);
        if (!rs.wasNull() && gsmrServerFkey > 0) {
            setProperty(P_GSMRServer, new OAObjectKey(gsmrServerFkey));
        }
        if (rs.getMetaData().getColumnCount() != 11) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
