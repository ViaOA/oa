// Generated by OABuilder
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.propertypath.*;
import com.viaoa.util.OADateTime;
import com.theice.tsac.delegate.ServerModelDelegate;
import com.theice.tsac.delegate.*;
import com.theice.tsac.delegate.oa.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
 
@OAClass(
    shortName = "llads",
    displayName = "LLADServer",
    displayProperty = "application.server"
)
@OATable(
    indexes = {
        @OAIndex(name = "LLADServerSilo", columns = { @OAIndexColumn(name = "SiloId") })
    }
)
public class LLADServer extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_RemoteStarted = "RemoteStarted";
    public static final String P_RemoteStarted = "RemoteStarted";
    public static final String PROPERTY_Connected = "Connected";
    public static final String P_Connected = "Connected";
     
    public static final String PROPERTY_EnableLLADCommands = "EnableLLADCommands";
    public static final String P_EnableLLADCommands = "EnableLLADCommands";
     
    public static final String PROPERTY_Application = "Application";
    public static final String P_Application = "Application";
    public static final String PROPERTY_LLADClients = "LLADClients";
    public static final String P_LLADClients = "LLADClients";
    public static final String PROPERTY_RemoteMessages = "RemoteMessages";
    public static final String P_RemoteMessages = "RemoteMessages";
    public static final String PROPERTY_Silo = "Silo";
    public static final String P_Silo = "Silo";
    public static final String PROPERTY_Users = "Users";
    public static final String P_Users = "Users";
     
    protected int id;
    protected OADateTime remoteStarted;
    protected OADateTime connected;
     
    // Links to other objects.
    protected transient Application application;
    protected transient Hub<LLADClient> hubLLADClients;
    protected transient Hub<RemoteMessage> hubRemoteMessages;
    protected transient Silo silo;
    protected transient Hub<User> hubUsers;
     
    public LLADServer() {
    }
     
    public LLADServer(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5)
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
    @OAProperty(displayName = "Remote Started", displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getRemoteStarted() {
        return remoteStarted;
    }
    
    public void setRemoteStarted(OADateTime newValue) {
        fireBeforePropertyChange(P_RemoteStarted, this.remoteStarted, newValue);
        OADateTime old = remoteStarted;
        this.remoteStarted = newValue;
        firePropertyChange(P_RemoteStarted, old, this.remoteStarted);
    }
    @OAProperty(displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getConnected() {
        return connected;
    }
    public void setConnected(OADateTime newValue) {
        fireBeforePropertyChange(P_Connected, this.connected, newValue);
        OADateTime old = connected;
        this.connected = newValue;
        firePropertyChange(P_Connected, old, this.connected);
        
        if (newValue != null || !isServer()) return;
        
        try {
            OARemoteThreadDelegate.sendMessages(true);
            for (LLADClient c : getLLADClients()) {
                c.getUserLogins().deleteAll();
            }
        }
        finally {
            OARemoteThreadDelegate.sendMessages(false);
        }
    }
    @OACalculatedProperty(displayName = "Enable LLADCommands", displayLength = 5)
    public boolean getEnableLLADCommands() {
        AdminUser au = ModelDelegate.getLoginUser();
        if (au == null) return false;
        if (!au.getEnableLLADCommands()) return false;
        
        RemoteClient rc = RemoteClientDelegate.getRemoteClient(RemoteClient.TYPE_LLAD);
        if (rc == null) return false;
        return (rc.getStatus() == rc.STATUS_Connected);
    }
     
    @OAOne(
        reverseName = Application.P_LLADServer
    )
    @OAFkey(columns = {"ApplicationId"})
    public Application getApplication() {
        if (application == null) {
            application = (Application) getObject(P_Application);
        }
        return application;
    }
    
    public void setApplication(Application newValue) {
        fireBeforePropertyChange(P_Application, this.application, newValue);
        Application old = this.application;
        this.application = newValue;
        firePropertyChange(P_Application, old, this.application);
    }
    
    @OAMany(
        toClass = LLADClient.class, 
        owner = true, 
        reverseName = LLADClient.P_LLADServer, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<LLADClient> getLLADClients() {
        if (hubLLADClients == null) {
            hubLLADClients = (Hub<LLADClient>) getHub(P_LLADClients);
        }
        return hubLLADClients;
    }
    
    @OAMany(
        displayName = "Remote Messages", 
        toClass = RemoteMessage.class, 
        reverseName = RemoteMessage.P_LLADServer, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    @OALinkTable(name = "LLADServerRemoteMessage", indexName = "RemoteMessageLladServer", columns = {"LLADServerId"})
    public Hub<RemoteMessage> getRemoteMessages() {
        if (hubRemoteMessages == null) {
            hubRemoteMessages = (Hub<RemoteMessage>) getHub(P_RemoteMessages);
        }
        return hubRemoteMessages;
    }
    
    @OAOne(
        reverseName = Silo.P_LLADServers, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SiloId"})
    public Silo getSilo() {
        if (silo == null) {
            silo = (Silo) getObject(P_Silo);
        }
        return silo;
    }
    
    public void setSilo(Silo newValue) {
        fireBeforePropertyChange(P_Silo, this.silo, newValue);
        Silo old = this.silo;
        this.silo = newValue;
        firePropertyChange(P_Silo, old, this.silo);
    }
    
    @OAMany(
        toClass = User.class, 
        reverseName = User.P_LLADServers
    )
    @OALinkTable(name = "LLADServerUserTable", indexName = "UserTableLladServer", columns = {"LLADServerId"})
    public Hub<User> getUsers() {
        if (hubUsers == null) {
            hubUsers = (Hub<User>) getHub(P_Users);
        }
        return hubUsers;
    }
    
    // refreshUserCache - refresh cache for all Users
    public void refreshUserCache() {
    }
     
    // reloadAllUsers - reload all users from LLAdmin
    public void reloadAllUsers() {
    }
     
    // connect - connecting to LLAD server
    public void connect() {
    }
     
    // disconnect - disconnecting from LLAD server
    public void disconnect() {
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.remoteStarted = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(3);
        if (timestamp != null) this.connected = new OADateTime(timestamp);
        int applicationFkey = rs.getInt(4);
        if (!rs.wasNull() && applicationFkey > 0) {
            setProperty(P_Application, new OAObjectKey(applicationFkey));
        }
        int siloFkey = rs.getInt(5);
        if (!rs.wasNull() && siloFkey > 0) {
            setProperty(P_Silo, new OAObjectKey(siloFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
