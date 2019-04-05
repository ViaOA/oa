// Generated by OABuilder
package test.xice.tsac3.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;

import test.xice.tsac3.model.oa.filter.*;
import test.xice.tsac3.model.oa.propertypath.*;
 
@OAClass(
    shortName = "ser",
    displayName = "Server",
    displayProperty = "displayName",
    sortProperty = "displayName",

    rootTreePropertyPaths = {
        "[Site]."+Site.P_Environments+"."+Environment.P_Silos+"."+Silo.P_Servers
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "ServerSilo", columns = { @OAIndexColumn(name = "SiloId") })
    }
)
public class Server extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_LastStart = "LastStart";
    public static final String P_LastStart = "LastStart";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_HostName = "HostName";
    public static final String P_HostName = "HostName";
    public static final String PROPERTY_IpAddress = "IpAddress";
    public static final String P_IpAddress = "IpAddress";
    public static final String PROPERTY_DnsName = "DnsName";
    public static final String P_DnsName = "DnsName";
    public static final String PROPERTY_ShortDnsName = "ShortDnsName";
    public static final String P_ShortDnsName = "ShortDnsName";
    public static final String PROPERTY_ServerId = "ServerId";
    public static final String P_ServerId = "ServerId";
    public static final String PROPERTY_Started = "Started";
    public static final String P_Started = "Started";
    public static final String PROPERTY_ServerFromId = "ServerFromId";
    public static final String P_ServerFromId = "ServerFromId";
    public static final String PROPERTY_VerifiedVersion = "VerifiedVersion";
    public static final String P_VerifiedVersion = "VerifiedVersion";
    public static final String PROPERTY_Installed = "Installed";
    public static final String P_Installed = "Installed";
     
    public static final String PROPERTY_DisplayName = "DisplayName";
    public static final String P_DisplayName = "DisplayName";
     
    public static final String PROPERTY_GSMRClients = "GSMRClients";
    public static final String P_GSMRClients = "GSMRClients";
    public static final String PROPERTY_GSMRServer = "GSMRServer";
    public static final String P_GSMRServer = "GSMRServer";
    public static final String PROPERTY_LLADClient = "LLADClient";
    public static final String P_LLADClient = "LLADClient";
    public static final String PROPERTY_LLADServer = "LLADServer";
    public static final String P_LLADServer = "LLADServer";
    public static final String PROPERTY_MRADClient = "MRADClient";
    public static final String P_MRADClient = "MRADClient";
    public static final String PROPERTY_MRADServer = "MRADServer";
    public static final String P_MRADServer = "MRADServer";
    public static final String PROPERTY_RCInstalledVersionDetails = "RCInstalledVersionDetails";
    public static final String P_RCInstalledVersionDetails = "RCInstalledVersionDetails";
    public static final String PROPERTY_ServerFiles = "ServerFiles";
    public static final String P_ServerFiles = "ServerFiles";
    public static final String PROPERTY_ServerInstalls = "ServerInstalls";
    public static final String P_ServerInstalls = "ServerInstalls";
    public static final String PROPERTY_ServerStatus = "ServerStatus";
    public static final String P_ServerStatus = "ServerStatus";
    public static final String PROPERTY_ServerType = "ServerType";
    public static final String P_ServerType = "ServerType";
    public static final String PROPERTY_ServerTypeVersion = "ServerTypeVersion";
    public static final String P_ServerTypeVersion = "ServerTypeVersion";
    public static final String PROPERTY_Silo = "Silo";
    public static final String P_Silo = "Silo";
    public static final String PROPERTY_Warnings = "Warnings";
    public static final String P_Warnings = "Warnings";
     
    protected int id;
    protected OADateTime created;
    protected OADateTime lastStart;
    protected String name;
    protected String hostName;
    protected String ipAddress;
    protected String dnsName;
    protected String shortDnsName;
    protected int serverId;
    protected OADateTime started;
    protected int serverFromId;
    protected boolean verifiedVersion;
    protected OADateTime installed;
     
    // Links to other objects.
    protected transient Hub<ServerFile> hubServerFiles;
    protected transient Hub<ServerInstall> hubServerInstalls;
    protected transient ServerStatus serverStatus;
    protected transient ServerType serverType;
    protected transient ServerTypeVersion serverTypeVersion;
    protected transient Silo silo;
    protected transient Hub<Warning> hubWarnings;
     
    public Server() {
        if (!isLoading()) {
            setCreated(new OADateTime());
        }
    }
     
    public Server(int id) {
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
    @OAProperty(defaultValue = "new OADateTime()", displayLength = 15)
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
    @OAProperty(displayName = "Last Start", description = "Last Date/Time of last start", displayLength = 15)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    /**
      Last Date/Time of last start
    */
    public OADateTime getLastStart() {
        return lastStart;
    }
    
    public void setLastStart(OADateTime newValue) {
        fireBeforePropertyChange(P_LastStart, this.lastStart, newValue);
        OADateTime old = lastStart;
        this.lastStart = newValue;
        firePropertyChange(P_LastStart, old, this.lastStart);
    }
    @OAProperty(maxLength = 55, displayLength = 34, columnLength = 24)
    @OAColumn(maxLength = 55)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayName = "Host Name", maxLength = 75, displayLength = 15, columnLength = 16)
    @OAColumn(maxLength = 75)
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String newValue) {
        fireBeforePropertyChange(P_HostName, this.hostName, newValue);
        String old = hostName;
        this.hostName = newValue;
        firePropertyChange(P_HostName, old, this.hostName);
    }
    @OAProperty(displayName = "IP Address", maxLength = 24, displayLength = 20, columnLength = 16)
    @OAColumn(maxLength = 24)
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String newValue) {
        fireBeforePropertyChange(P_IpAddress, this.ipAddress, newValue);
        String old = ipAddress;
        this.ipAddress = newValue;
        firePropertyChange(P_IpAddress, old, this.ipAddress);
    }
    @OAProperty(displayName = "DNS Name", maxLength = 50, displayLength = 18, columnLength = 15)
    @OAColumn(maxLength = 50)
    public String getDnsName() {
        return dnsName;
    }
    
    public void setDnsName(String newValue) {
        fireBeforePropertyChange(P_DnsName, this.dnsName, newValue);
        String old = dnsName;
        this.dnsName = newValue;
        firePropertyChange(P_DnsName, old, this.dnsName);
    }
    @OAProperty(displayName = "Short DNS Name", maxLength = 50, displayLength = 40, columnLength = 15)
    @OAColumn(maxLength = 50)
    public String getShortDnsName() {
        return shortDnsName;
    }
    
    public void setShortDnsName(String newValue) {
        fireBeforePropertyChange(P_ShortDnsName, this.shortDnsName, newValue);
        String old = shortDnsName;
        this.shortDnsName = newValue;
        firePropertyChange(P_ShortDnsName, old, this.shortDnsName);
    }
    @OAProperty(displayName = "Server Id", displayLength = 10, columnLength = 14)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getServerId() {
        return serverId;
    }
    
    public void setServerId(int newValue) {
        fireBeforePropertyChange(P_ServerId, this.serverId, newValue);
        int old = serverId;
        this.serverId = newValue;
        firePropertyChange(P_ServerId, old, this.serverId);
    }
    @OAProperty(displayLength = 15)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getStarted() {
        return started;
    }
    
    public void setStarted(OADateTime newValue) {
        fireBeforePropertyChange(P_Started, this.started, newValue);
        OADateTime old = started;
        this.started = newValue;
        firePropertyChange(P_Started, old, this.started);
    }
    @OAProperty(displayName = "Server From Id", displayLength = 10, columnLength = 8)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getServerFromId() {
        return serverFromId;
    }
    
    public void setServerFromId(int newValue) {
        fireBeforePropertyChange(P_ServerFromId, this.serverFromId, newValue);
        int old = serverFromId;
        this.serverFromId = newValue;
        firePropertyChange(P_ServerFromId, old, this.serverFromId);
    }
    @OAProperty(displayName = "Verified Version", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getVerifiedVersion() {
        return verifiedVersion;
    }
    
    public void setVerifiedVersion(boolean newValue) {
        fireBeforePropertyChange(P_VerifiedVersion, this.verifiedVersion, newValue);
        boolean old = verifiedVersion;
        this.verifiedVersion = newValue;
        firePropertyChange(P_VerifiedVersion, old, this.verifiedVersion);
    }
    @OAProperty(displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getInstalled() {
        return installed;
    }
    
    public void setInstalled(OADateTime newValue) {
        fireBeforePropertyChange(P_Installed, this.installed, newValue);
        OADateTime old = installed;
        this.installed = newValue;
        firePropertyChange(P_Installed, old, this.installed);
    }
    @OACalculatedProperty(displayName = "Display Name", displayLength = 22, columnLength = 20, properties = {P_Name, P_HostName, P_IpAddress, P_ServerType})
    public String getDisplayName() {
        String displayName = "";
        if (!OAString.isEmpty(hostName)) {
            if (displayName.length() > 0) displayName += " ";
            displayName += hostName;
        }
        else if (!OAString.isEmpty(name)) {
            if (displayName.length() > 0) displayName += " ";
            displayName += name;
        }
        else if (!OAString.isEmpty(dnsName)) {
            if (displayName.length() > 0) displayName += " ";
            displayName += dnsName;
        }
        else if (!OAString.isEmpty(shortDnsName)) {
            if (displayName.length() > 0) displayName += " ";
            displayName += shortDnsName;
        }
        else if (!OAString.isEmpty(ipAddress)) {
            if (displayName.length() > 0) displayName += " ";
            displayName += ipAddress;
        }
    
        ServerType serverType = this.getServerType();
        if (serverType != null) {
            if (displayName.length() > 0) displayName += " ";
            displayName += "("+serverType.getName()+")";
        }
        return displayName;
    }
     
    @OAMany(
        toClass = GSMRClient.class, 
        reverseName = GSMRClient.P_Server, 
        mustBeEmptyForDelete = true, 
        createMethod = false
    )
    private Hub<GSMRClient> getGSMRClients() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = GSMRServer.P_Server, 
        allowCreateNew = false, 
        allowAddExisting = false, 
        mustBeEmptyForDelete = true
    )
    private GSMRServer getGSMRServer() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = LLADClient.P_Server, 
        allowCreateNew = false, 
        allowAddExisting = false, 
        mustBeEmptyForDelete = true
    )
    private LLADClient getLLADClient() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = LLADServer.P_Server, 
        allowCreateNew = false, 
        allowAddExisting = false, 
        mustBeEmptyForDelete = true
    )
    private LLADServer getLLADServer() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = MRADClient.P_Server, 
        allowCreateNew = false, 
        allowAddExisting = false, 
        mustBeEmptyForDelete = true
    )
    private MRADClient getMRADClient() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = MRADServer.P_Server, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private MRADServer getMRADServer() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "RCInstalled Version Details", 
        toClass = RCInstalledVersionDetail.class, 
        reverseName = RCInstalledVersionDetail.P_Server, 
        createMethod = false
    )
    private Hub<RCInstalledVersionDetail> getRCInstalledVersionDetails() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "Server Files", 
        toClass = ServerFile.class, 
        reverseName = ServerFile.P_Server
    )
    public Hub<ServerFile> getServerFiles() {
        if (hubServerFiles == null) {
            hubServerFiles = (Hub<ServerFile>) getHub(P_ServerFiles);
        }
        return hubServerFiles;
    }
    
    @OAMany(
        displayName = "Server Installs", 
        toClass = ServerInstall.class, 
        reverseName = ServerInstall.P_Server
    )
    public Hub<ServerInstall> getServerInstalls() {
        if (hubServerInstalls == null) {
            hubServerInstalls = (Hub<ServerInstall>) getHub(P_ServerInstalls);
        }
        return hubServerInstalls;
    }
    
    @OAOne(
        displayName = "Server Status", 
        reverseName = ServerStatus.P_Servers, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ServerStatusId"})
    public ServerStatus getServerStatus() {
        if (serverStatus == null) {
            serverStatus = (ServerStatus) getObject(P_ServerStatus);
        }
        return serverStatus;
    }
    
    public void setServerStatus(ServerStatus newValue) {
        fireBeforePropertyChange(P_ServerStatus, this.serverStatus, newValue);
        ServerStatus old = this.serverStatus;
        this.serverStatus = newValue;
        firePropertyChange(P_ServerStatus, old, this.serverStatus);
    }
    
    @OAOne(
        displayName = "Server Type", 
        reverseName = ServerType.P_Servers, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ServerTypeId"})
    public ServerType getServerType() {
        if (serverType == null) {
            serverType = (ServerType) getObject(P_ServerType);
        }
        return serverType;
    }
    
    public void setServerType(ServerType newValue) {
        fireBeforePropertyChange(P_ServerType, this.serverType, newValue);
        ServerType old = this.serverType;
        this.serverType = newValue;
        firePropertyChange(P_ServerType, old, this.serverType);
    }
    
    @OAOne(
        displayName = "Server Type Version", 
        reverseName = ServerTypeVersion.P_Servers, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ServerTypeVersionId"})
    public ServerTypeVersion getServerTypeVersion() {
        if (serverTypeVersion == null) {
            serverTypeVersion = (ServerTypeVersion) getObject(P_ServerTypeVersion);
        }
        return serverTypeVersion;
    }
    
    public void setServerTypeVersion(ServerTypeVersion newValue) {
        fireBeforePropertyChange(P_ServerTypeVersion, this.serverTypeVersion, newValue);
        ServerTypeVersion old = this.serverTypeVersion;
        this.serverTypeVersion = newValue;
        firePropertyChange(P_ServerTypeVersion, old, this.serverTypeVersion);
    }
    
    @OAOne(
        reverseName = Silo.P_Servers, 
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
        toClass = Warning.class, 
        reverseName = Warning.P_Server
    )
    public Hub<Warning> getWarnings() {
        if (hubWarnings == null) {
            hubWarnings = (Hub<Warning>) getHub(P_Warnings);
        }
        return hubWarnings;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(3);
        if (timestamp != null) this.lastStart = new OADateTime(timestamp);
        this.name = rs.getString(4);
        this.hostName = rs.getString(5);
        this.ipAddress = rs.getString(6);
        this.dnsName = rs.getString(7);
        this.shortDnsName = rs.getString(8);
        this.serverId = (int) rs.getInt(9);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Server.P_ServerId, true);
        }
        timestamp = rs.getTimestamp(10);
        if (timestamp != null) this.started = new OADateTime(timestamp);
        this.serverFromId = (int) rs.getInt(11);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Server.P_ServerFromId, true);
        }
        this.verifiedVersion = rs.getBoolean(12);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Server.P_VerifiedVersion, true);
        }
        timestamp = rs.getTimestamp(13);
        if (timestamp != null) this.installed = new OADateTime(timestamp);
        int serverStatusFkey = rs.getInt(14);
        if (!rs.wasNull() && serverStatusFkey > 0) {
            setProperty(P_ServerStatus, new OAObjectKey(serverStatusFkey));
        }
        int serverTypeFkey = rs.getInt(15);
        if (!rs.wasNull() && serverTypeFkey > 0) {
            setProperty(P_ServerType, new OAObjectKey(serverTypeFkey));
        }
        int serverTypeVersionFkey = rs.getInt(16);
        if (!rs.wasNull() && serverTypeVersionFkey > 0) {
            setProperty(P_ServerTypeVersion, new OAObjectKey(serverTypeVersionFkey));
        }
        int siloFkey = rs.getInt(17);
        if (!rs.wasNull() && siloFkey > 0) {
            setProperty(P_Silo, new OAObjectKey(siloFkey));
        }
        if (rs.getMetaData().getColumnCount() != 17) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
