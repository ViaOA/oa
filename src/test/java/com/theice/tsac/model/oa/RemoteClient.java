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
import com.theice.tsac.delegate.RemoteDelegate;
import com.theice.tsac.delegate.ServerModelDelegate;
import com.theice.tsac.delegate.oa.RemoteClientDelegate;
 
/**
  used by remote clients that then connect to other TS servers to gather 
    information and update the OAModel.
*/
@OAClass(
    shortName = "rc",
    displayName = "Remote Client",
    description = "used by remote clients that then connect to other TS servers to gather      information and update the OAModel.",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "type"
)
@OATable(
)
public class RemoteClient extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
    public static final String PROPERTY_Status = "Status";
    public static final String P_Status = "Status";
    public static final String PROPERTY_StatusAsString = "StatusAsString";
    public static final String P_StatusAsString = "StatusAsString";
    public static final String PROPERTY_DtStatus = "DtStatus";
    public static final String P_DtStatus = "DtStatus";
    public static final String PROPERTY_AutoStart = "AutoStart";
    public static final String P_AutoStart = "AutoStart";
    public static final String PROPERTY_Console = "Console";
    public static final String P_Console = "Console";
     
     
    public static final String PROPERTY_MRADServers = "MRADServers";
    public static final String P_MRADServers = "MRADServers";
    public static final String PROPERTY_RCExecutes = "RCExecutes";
    public static final String P_RCExecutes = "RCExecutes";
    public static final String PROPERTY_RCInstalledVersions = "RCInstalledVersions";
    public static final String P_RCInstalledVersions = "RCInstalledVersions";
    public static final String PROPERTY_RCRepoVersions = "RCRepoVersions";
    public static final String P_RCRepoVersions = "RCRepoVersions";
    public static final String PROPERTY_RCServiceLists = "RCServiceLists";
    public static final String P_RCServiceLists = "RCServiceLists";
    public static final String PROPERTY_RemoteMessages = "RemoteMessages";
    public static final String P_RemoteMessages = "RemoteMessages";
     
    protected int id;
    protected String name;
    protected int type;
    public static final int TYPE_GSMR = 0;
    public static final int TYPE_LLAD = 1;
    public static final int TYPE_MRAD = 2;
    public static final int TYPE_RC = 3;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("GSMR");
        hubType.addElement("LLAD");
        hubType.addElement("MRAD");
        hubType.addElement("Remote Control");
    }
    protected int status;
    public static final int STATUS_Disconnected = 0;
    public static final int STATUS_Connected = 1;
    public static final Hub<String> hubStatus;
    static {
        hubStatus = new Hub<String>(String.class);
        hubStatus.addElement("Disconnected");
        hubStatus.addElement("Connected");
    }
    protected OADateTime dtStatus;
    protected boolean autoStart;
    protected String console;
     
    // Links to other objects.
    protected transient Hub<RemoteMessage> hubRemoteMessages;
     
    public RemoteClient() {
    }
     
    public RemoteClient(int id) {
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
    @OAProperty(maxLength = 35, displayLength = 26, columnLength = 22, isProcessed = true)
    @OAColumn(maxLength = 35)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayLength = 25, columnLength = 18, isProcessed = true, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        fireBeforePropertyChange(P_Type, this.type, newValue);
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    public String getTypeAsString() {
        if (isNull(P_Type)) return "";
        String s = hubType.getAt(getType());
        if (s == null) s = "";
        return s;
    }
    @OAProperty(displayLength = 15, columnLength = 13, isProcessed = true, isNameValue = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getStatus() {
        return status;
    }
    public void setStatus(int newValue) {
        fireBeforePropertyChange(P_Status, this.status, newValue);
        int old = status;
        this.status = newValue;
        firePropertyChange(P_Status, old, this.status);
        if (!isLoading()) setDtStatus(new OADateTime());
    }
    public String getStatusAsString() {
        if (isNull(P_Status)) return "";
        String s = hubStatus.getAt(getStatus());
        if (s == null) s = "";
        return s;
    }
    @OAProperty(displayName = "Status dt", displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDtStatus() {
        return dtStatus;
    }
    
    public void setDtStatus(OADateTime newValue) {
        fireBeforePropertyChange(P_DtStatus, this.dtStatus, newValue);
        OADateTime old = dtStatus;
        this.dtStatus = newValue;
        firePropertyChange(P_DtStatus, old, this.dtStatus);
    }
    @OAProperty(displayName = "Auto Start", displayLength = 5, columnLength = 9)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean newValue) {
        fireBeforePropertyChange(P_AutoStart, this.autoStart, newValue);
        boolean old = autoStart;
        this.autoStart = newValue;
        firePropertyChange(P_AutoStart, old, this.autoStart);
    }
    @OAProperty(maxLength = 7, displayLength = 7)
    public String getConsole() {
        return console;
    }
    
    public void setConsole(String newValue) {
        fireBeforePropertyChange(P_Console, this.console, newValue);
        String old = console;
        this.console = newValue;
        firePropertyChange(P_Console, old, this.console);
    }
    @OAMany(
        displayName = "MRAD Servers", 
        toClass = MRADServer.class, 
        isCalculated = true, 
        reverseName = MRADServer.P_RemoteClient, 
        createMethod = false
    )
    private Hub<MRADServer> getMRADServers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        toClass = RCExecute.class, 
        isCalculated = true, 
        reverseName = RCExecute.P_RemoteClient, 
        createMethod = false
    )
    private Hub<RCExecute> getRCExecutes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "RCInstalled Versions", 
        toClass = RCInstalledVersion.class, 
        isCalculated = true, 
        reverseName = RCInstalledVersion.P_RemoteClient, 
        createMethod = false
    )
    private Hub<RCInstalledVersion> getRCInstalledVersions() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "RCRepo Versions", 
        toClass = RCRepoVersion.class, 
        isCalculated = true, 
        reverseName = RCRepoVersion.P_RemoteClient, 
        createMethod = false
    )
    private Hub<RCRepoVersion> getRCRepoVersions() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "RCService Lists", 
        toClass = RCServiceList.class, 
        isCalculated = true, 
        reverseName = RCServiceList.P_RemoteClient, 
        createMethod = false
    )
    private Hub<RCServiceList> getRCServiceLists() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Remote Messages", 
        toClass = RemoteMessage.class, 
        owner = true, 
        reverseName = RemoteMessage.P_RemoteClient, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    @OALinkTable(name = "RemoteClientRemoteMessage", indexName = "RemoteMessageRemoteClient", columns = {"RemoteClientId"})
    public Hub<RemoteMessage> getRemoteMessages() {
        if (hubRemoteMessages == null) {
            hubRemoteMessages = (Hub<RemoteMessage>) getHub(P_RemoteMessages);
        }
        return hubRemoteMessages;
    }
    
    // start - start the remote client
    public void start() throws Exception {
        RemoteClientDelegate.start(this);
    }
     
    // stop - stop the remote client
    public void stop() throws Exception {
        RemoteClientDelegate.stop(this);
    }
     
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.type = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RemoteClient.P_Type, true);
        }
        this.status = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RemoteClient.P_Status, true);
        }
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(5);
        if (timestamp != null) this.dtStatus = new OADateTime(timestamp);
        this.autoStart = rs.getBoolean(6);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RemoteClient.P_AutoStart, true);
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
