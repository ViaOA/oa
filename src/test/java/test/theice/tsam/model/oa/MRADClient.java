// Generated by OABuilder
package test.theice.tsam.model.oa;
 
import java.util.logging.*;
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import test.theice.tsam.model.oa.Application;
import test.theice.tsam.model.oa.ApplicationType;
import test.theice.tsam.model.oa.HostInfo;
import test.theice.tsam.model.oa.MRADClient;
import test.theice.tsam.model.oa.MRADClientCommand;
import test.theice.tsam.model.oa.MRADClientMessage;
import test.theice.tsam.model.oa.MRADServer;
import test.theice.tsam.model.oa.Server;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;

import test.theice.tsam.delegate.*;
import test.theice.tsam.delegate.oa.*;
import test.theice.tsam.model.oa.filter.*;
import test.theice.tsam.model.oa.propertypath.*;
 
@OAClass(
    shortName = "mradc",
    displayName = "MRAD Client",
    displayProperty = "application.server"
)
@OATable(
    indexes = {
        @OAIndex(name = "MRADClientMradServer", columns = { @OAIndexColumn(name = "MradServerId") })
    }
)
public class MRADClient extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(MRADClient.class.getName());
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_HostName = "HostName";
    public static final String P_HostName = "HostName";
    public static final String PROPERTY_IpAddress = "IpAddress";
    public static final String P_IpAddress = "IpAddress";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
    public static final String PROPERTY_RouterAbsolutePath = "RouterAbsolutePath";
    public static final String P_RouterAbsolutePath = "RouterAbsolutePath";
    public static final String PROPERTY_StartScript = "StartScript";
    public static final String P_StartScript = "StartScript";
    public static final String PROPERTY_StopScript = "StopScript";
    public static final String P_StopScript = "StopScript";
    public static final String PROPERTY_SnapshotStartScript = "SnapshotStartScript";
    public static final String P_SnapshotStartScript = "SnapshotStartScript";
    public static final String PROPERTY_Directory = "Directory";
    public static final String P_Directory = "Directory";
    public static final String PROPERTY_Version = "Version";
    public static final String P_Version = "Version";
    public static final String PROPERTY_RemoteSocketAddress = "RemoteSocketAddress";
    public static final String P_RemoteSocketAddress = "RemoteSocketAddress";
    public static final String PROPERTY_ApplicationStatus = "ApplicationStatus";
    public static final String P_ApplicationStatus = "ApplicationStatus";
    public static final String PROPERTY_Started = "Started";
    public static final String P_Started = "Started";
    public static final String PROPERTY_Ready = "Ready";
    public static final String P_Ready = "Ready";
    public static final String PROPERTY_ServerTypeId = "ServerTypeId";
    public static final String P_ServerTypeId = "ServerTypeId";
    public static final String PROPERTY_ApplicationTypeCode = "ApplicationTypeCode";
    public static final String P_ApplicationTypeCode = "ApplicationTypeCode";
    public static final String PROPERTY_DtConnected = "DtConnected";
    public static final String P_DtConnected = "DtConnected";
    public static final String PROPERTY_DtDisconnected = "DtDisconnected";
    public static final String P_DtDisconnected = "DtDisconnected";
    public static final String PROPERTY_TotalMemory = "TotalMemory";
    public static final String P_TotalMemory = "TotalMemory";
    public static final String PROPERTY_FreeMemory = "FreeMemory";
    public static final String P_FreeMemory = "FreeMemory";
    public static final String PROPERTY_JavaVendor = "JavaVendor";
    public static final String P_JavaVendor = "JavaVendor";
    public static final String PROPERTY_JavaVersion = "JavaVersion";
    public static final String P_JavaVersion = "JavaVersion";
    public static final String PROPERTY_OsArch = "OsArch";
    public static final String P_OsArch = "OsArch";
    public static final String PROPERTY_OsName = "OsName";
    public static final String P_OsName = "OsName";
    public static final String PROPERTY_OsVersion = "OsVersion";
    public static final String P_OsVersion = "OsVersion";
    public static final String PROPERTY_ProcessId = "ProcessId";
    public static final String P_ProcessId = "ProcessId";
    public static final String PROPERTY_InstalledVersion = "InstalledVersion";
    public static final String P_InstalledVersion = "InstalledVersion";
    public static final String PROPERTY_DtInstall = "DtInstall";
    public static final String P_DtInstall = "DtInstall";
    public static final String PROPERTY_DtLastUpdated = "DtLastUpdated";
    public static final String P_DtLastUpdated = "DtLastUpdated";
    public static final String PROPERTY_LastConnectionId = "LastConnectionId";
    public static final String P_LastConnectionId = "LastConnectionId";
    public static final String PROPERTY_MRADClientVersion = "MRADClientVersion";
    public static final String P_MRADClientVersion = "MRADClientVersion";
     
    public static final String PROPERTY_AutoComplete = "AutoComplete";
    public static final String P_AutoComplete = "AutoComplete";
    public static final String PROPERTY_CalcVersion = "CalcVersion";
    public static final String P_CalcVersion = "CalcVersion";
    public static final String PROPERTY_IsConnected = "IsConnected";
    public static final String P_IsConnected = "IsConnected";
     
    public static final String PROPERTY_Application = "Application";
    public static final String P_Application = "Application";
    public static final String PROPERTY_HostInfo = "HostInfo";
    public static final String P_HostInfo = "HostInfo";
    public static final String PROPERTY_LastMRADClientCommand = "LastMRADClientCommand";
    public static final String P_LastMRADClientCommand = "LastMRADClientCommand";
    public static final String PROPERTY_LastMRADClientMessage = "LastMRADClientMessage";
    public static final String P_LastMRADClientMessage = "LastMRADClientMessage";
    public static final String PROPERTY_MRADClientCommands = "MRADClientCommands";
    public static final String P_MRADClientCommands = "MRADClientCommands";
    public static final String PROPERTY_MRADClientMessages = "MRADClientMessages";
    public static final String P_MRADClientMessages = "MRADClientMessages";
    public static final String PROPERTY_MRADServer = "MRADServer";
    public static final String P_MRADServer = "MRADServer";
     
    protected int id;
    protected OADateTime created;
    protected String hostName;
    protected String ipAddress;
    protected String name;
    protected String description;
    protected String routerAbsolutePath;
    protected String startScript;
    protected String stopScript;
    protected String snapshotStartScript;
    protected String directory;
    protected String version;
    protected String remoteSocketAddress;
    protected String applicationStatus;
    protected OADateTime started;
    protected OADateTime ready;
    protected int serverTypeId;
    protected String applicationTypeCode;
    protected OADateTime dtConnected;
    protected OADateTime dtDisconnected;
    protected long totalMemory;
    protected long freeMemory;
    protected String javaVendor;
    protected String javaVersion;
    protected String osArch;
    protected String osName;
    protected String osVersion;
    protected String processId;
    protected String installedVersion;
    protected OADateTime dtInstall;
    protected OADateTime dtLastUpdated;
    protected int lastConnectionId;
    protected String mradClientVersion;
     
    // Links to other objects.
    protected transient Application application;
    protected transient HostInfo hostInfo;
    protected transient MRADClientCommand lastMRADClientCommand;
    protected transient MRADClientMessage lastMRADClientMessage;
    protected transient Hub<MRADClientCommand> hubMRADClientCommands;
    protected transient Hub<MRADClientMessage> hubMRADClientMessages;
    protected transient MRADServer mradServer;
     
    public MRADClient() {
        if (!isLoading()) {
            setCreated(new OADateTime());
            setHostInfo(new HostInfo());
        }
    }
     
    public MRADClient(int id) {
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
    
    @OAProperty(defaultValue = "new OADateTime()", displayLength = 12, isProcessed = true)
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
    
    @OAProperty(displayName = "Host Name", maxLength = 35, displayLength = 15, columnLength = 12)
    @OAColumn(maxLength = 35)
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String newValue) {
        fireBeforePropertyChange(P_HostName, this.hostName, newValue);
        String old = hostName;
        this.hostName = newValue;
        firePropertyChange(P_HostName, old, this.hostName);
    }
    
    @OAProperty(displayName = "Ip Address", maxLength = 15, displayLength = 12)
    @OAColumn(maxLength = 15)
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String newValue) {
        fireBeforePropertyChange(P_IpAddress, this.ipAddress, newValue);
        String old = ipAddress;
        this.ipAddress = newValue;
        firePropertyChange(P_IpAddress, old, this.ipAddress);
    }
    
    @OAProperty(maxLength = 35, displayLength = 15, columnLength = 12)
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
    
    @OAProperty(maxLength = 75, displayLength = 20, columnLength = 15)
    @OAColumn(maxLength = 75)
    public String getDescription() {
        return description;
    }
    public void setDescription(String newValue) {
        fireBeforePropertyChange(P_Description, this.description, newValue);
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    
    @OAProperty(displayName = "Router Absolute Path", maxLength = 254, displayLength = 20, columnLength = 15)
    @OAColumn(maxLength = 254)
    public String getRouterAbsolutePath() {
        return routerAbsolutePath;
    }
    public void setRouterAbsolutePath(String newValue) {
        fireBeforePropertyChange(P_RouterAbsolutePath, this.routerAbsolutePath, newValue);
        String old = routerAbsolutePath;
        this.routerAbsolutePath = newValue;
        firePropertyChange(P_RouterAbsolutePath, old, this.routerAbsolutePath);
    }
    
    @OAProperty(displayName = "Start Script", maxLength = 254, displayLength = 20, columnLength = 15)
    @OAColumn(maxLength = 254)
    public String getStartScript() {
        return startScript;
    }
    public void setStartScript(String newValue) {
        fireBeforePropertyChange(P_StartScript, this.startScript, newValue);
        String old = startScript;
        this.startScript = newValue;
        firePropertyChange(P_StartScript, old, this.startScript);
    }
    
    @OAProperty(displayName = "Stop Script", maxLength = 254, displayLength = 20, columnLength = 15)
    @OAColumn(name = "StartScript", maxLength = 254)
    public String getStopScript() {
        return stopScript;
    }
    public void setStopScript(String newValue) {
        fireBeforePropertyChange(P_StopScript, this.stopScript, newValue);
        String old = stopScript;
        this.stopScript = newValue;
        firePropertyChange(P_StopScript, old, this.stopScript);
    }
    
    @OAProperty(displayName = "Snapshot Start Script", maxLength = 254, displayLength = 20, columnLength = 15)
    @OAColumn(name = "StartScript", maxLength = 254)
    public String getSnapshotStartScript() {
        return snapshotStartScript;
    }
    public void setSnapshotStartScript(String newValue) {
        fireBeforePropertyChange(P_SnapshotStartScript, this.snapshotStartScript, newValue);
        String old = snapshotStartScript;
        this.snapshotStartScript = newValue;
        firePropertyChange(P_SnapshotStartScript, old, this.snapshotStartScript);
    }
    
    @OAProperty(maxLength = 75, displayLength = 20, columnLength = 15)
    @OAColumn(maxLength = 75)
    public String getDirectory() {
        return directory;
    }
    public void setDirectory(String newValue) {
        fireBeforePropertyChange(P_Directory, this.directory, newValue);
        String old = directory;
        this.directory = newValue;
        firePropertyChange(P_Directory, old, this.directory);
    }
    
    @OAProperty(maxLength = 35, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 35)
    public String getVersion() {
        return version;
    }
    public void setVersion(String newValue) {
        fireBeforePropertyChange(P_Version, this.version, newValue);
        String old = version;
        this.version = newValue;
        firePropertyChange(P_Version, old, this.version);
    }
    
    @OAProperty(displayName = "Remote Socket Address", maxLength = 55, displayLength = 15, columnLength = 14)
    @OAColumn(maxLength = 55)
    public String getRemoteSocketAddress() {
        return remoteSocketAddress;
    }
    public void setRemoteSocketAddress(String newValue) {
        fireBeforePropertyChange(P_RemoteSocketAddress, this.remoteSocketAddress, newValue);
        String old = remoteSocketAddress;
        this.remoteSocketAddress = newValue;
        firePropertyChange(P_RemoteSocketAddress, old, this.remoteSocketAddress);
    }
    
    @OAProperty(displayName = "Application Status", maxLength = 35, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 35)
    public String getApplicationStatus() {
        return applicationStatus;
    }
    public void setApplicationStatus(String newValue) {
        fireBeforePropertyChange(P_ApplicationStatus, this.applicationStatus, newValue);
        String old = applicationStatus;
        this.applicationStatus = newValue;
        firePropertyChange(P_ApplicationStatus, old, this.applicationStatus);
    }
    
    @OAProperty(displayLength = 12)
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
    
    @OAProperty(displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getReady() {
        return ready;
    }
    public void setReady(OADateTime newValue) {
        fireBeforePropertyChange(P_Ready, this.ready, newValue);
        OADateTime old = ready;
        this.ready = newValue;
        firePropertyChange(P_Ready, old, this.ready);
    }
    
    @OAProperty(displayName = "Server Type Id", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getServerTypeId() {
        return serverTypeId;
    }
    public void setServerTypeId(int newValue) {
        fireBeforePropertyChange(P_ServerTypeId, this.serverTypeId, newValue);
        int old = serverTypeId;
        this.serverTypeId = newValue;
        firePropertyChange(P_ServerTypeId, old, this.serverTypeId);
    }
    
    @OAProperty(displayName = "Application Type Code", maxLength = 20, displayLength = 8, columnLength = 5)
    @OAColumn(maxLength = 20)
    public String getApplicationTypeCode() {
        return applicationTypeCode;
    }
    public void setApplicationTypeCode(String newValue) {
        fireBeforePropertyChange(P_ApplicationTypeCode, this.applicationTypeCode, newValue);
        String old = applicationTypeCode;
        this.applicationTypeCode = newValue;
        firePropertyChange(P_ApplicationTypeCode, old, this.applicationTypeCode);
    }
    
    @OAProperty(displayName = "Dt Connected", displayLength = 12, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDtConnected() {
        return dtConnected;
    }
    public void setDtConnected(OADateTime newValue) {
        fireBeforePropertyChange(P_DtConnected, this.dtConnected, newValue);
        OADateTime old = dtConnected;
        this.dtConnected = newValue;
        firePropertyChange(P_DtConnected, old, this.dtConnected);
    }
    
    @OAProperty(displayName = "Dt Disconnected", displayLength = 12, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDtDisconnected() {
        return dtDisconnected;
    }
    public void setDtDisconnected(OADateTime newValue) {
        fireBeforePropertyChange(P_DtDisconnected, this.dtDisconnected, newValue);
        OADateTime old = dtDisconnected;
        this.dtDisconnected = newValue;
        firePropertyChange(P_DtDisconnected, old, this.dtDisconnected);
    }
    
    @OAProperty(displayName = "Total Memory", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getTotalMemory() {
        return totalMemory;
    }
    public void setTotalMemory(long newValue) {
        fireBeforePropertyChange(P_TotalMemory, this.totalMemory, newValue);
        long old = totalMemory;
        this.totalMemory = newValue;
        firePropertyChange(P_TotalMemory, old, this.totalMemory);
    }
    
    @OAProperty(displayName = "Free Memory", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getFreeMemory() {
        return freeMemory;
    }
    public void setFreeMemory(long newValue) {
        fireBeforePropertyChange(P_FreeMemory, this.freeMemory, newValue);
        long old = freeMemory;
        this.freeMemory = newValue;
        firePropertyChange(P_FreeMemory, old, this.freeMemory);
    }
    
    @OAProperty(displayName = "Java Vendor", maxLength = 35, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 35)
    public String getJavaVendor() {
        return javaVendor;
    }
    public void setJavaVendor(String newValue) {
        fireBeforePropertyChange(P_JavaVendor, this.javaVendor, newValue);
        String old = javaVendor;
        this.javaVendor = newValue;
        firePropertyChange(P_JavaVendor, old, this.javaVendor);
    }
    
    @OAProperty(displayName = "Java Version", maxLength = 35, displayLength = 12, columnLength = 11)
    @OAColumn(maxLength = 35)
    public String getJavaVersion() {
        return javaVersion;
    }
    public void setJavaVersion(String newValue) {
        fireBeforePropertyChange(P_JavaVersion, this.javaVersion, newValue);
        String old = javaVersion;
        this.javaVersion = newValue;
        firePropertyChange(P_JavaVersion, old, this.javaVersion);
    }
    
    @OAProperty(displayName = "Os Arch", maxLength = 25, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 25)
    public String getOsArch() {
        return osArch;
    }
    public void setOsArch(String newValue) {
        fireBeforePropertyChange(P_OsArch, this.osArch, newValue);
        String old = osArch;
        this.osArch = newValue;
        firePropertyChange(P_OsArch, old, this.osArch);
    }
    
    @OAProperty(displayName = "Os Name", maxLength = 25, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 25)
    public String getOsName() {
        return osName;
    }
    public void setOsName(String newValue) {
        fireBeforePropertyChange(P_OsName, this.osName, newValue);
        String old = osName;
        this.osName = newValue;
        firePropertyChange(P_OsName, old, this.osName);
    }
    
    @OAProperty(displayName = "Os Version", maxLength = 55, displayLength = 15, columnLength = 10)
    @OAColumn(maxLength = 55)
    public String getOsVersion() {
        return osVersion;
    }
    public void setOsVersion(String newValue) {
        fireBeforePropertyChange(P_OsVersion, this.osVersion, newValue);
        String old = osVersion;
        this.osVersion = newValue;
        firePropertyChange(P_OsVersion, old, this.osVersion);
    }
    
    @OAProperty(displayName = "Process Id", maxLength = 12, displayLength = 5)
    @OAColumn(maxLength = 12)
    public String getProcessId() {
        return processId;
    }
    public void setProcessId(String newValue) {
        fireBeforePropertyChange(P_ProcessId, this.processId, newValue);
        String old = processId;
        this.processId = newValue;
        firePropertyChange(P_ProcessId, old, this.processId);
    }
    
    @OAProperty(displayName = "Installed Version", maxLength = 32, displayLength = 12, columnLength = 8)
    @OAColumn(maxLength = 32)
    public String getInstalledVersion() {
        return installedVersion;
    }
    public void setInstalledVersion(String newValue) {
        fireBeforePropertyChange(P_InstalledVersion, this.installedVersion, newValue);
        String old = installedVersion;
        this.installedVersion = newValue;
        firePropertyChange(P_InstalledVersion, old, this.installedVersion);
    }
    
    @OAProperty(displayName = "Installed", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDtInstall() {
        return dtInstall;
    }
    public void setDtInstall(OADateTime newValue) {
        fireBeforePropertyChange(P_DtInstall, this.dtInstall, newValue);
        OADateTime old = dtInstall;
        this.dtInstall = newValue;
        firePropertyChange(P_DtInstall, old, this.dtInstall);
    }
    
    @OAProperty(displayName = "Dt Last Updated", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDtLastUpdated() {
        return dtLastUpdated;
    }
    public void setDtLastUpdated(OADateTime newValue) {
        fireBeforePropertyChange(P_DtLastUpdated, this.dtLastUpdated, newValue);
        OADateTime old = dtLastUpdated;
        this.dtLastUpdated = newValue;
        firePropertyChange(P_DtLastUpdated, old, this.dtLastUpdated);
    }
    
    @OAProperty(displayName = "Last Connection Id", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getLastConnectionId() {
        return lastConnectionId;
    }
    public void setLastConnectionId(int newValue) {
        fireBeforePropertyChange(P_LastConnectionId, this.lastConnectionId, newValue);
        int old = lastConnectionId;
        this.lastConnectionId = newValue;
        firePropertyChange(P_LastConnectionId, old, this.lastConnectionId);
    }
    
    @OAProperty(displayName = "MRAD Client Version", maxLength = 17, displayLength = 10, columnLength = 8)
    @OAColumn(maxLength = 17)
    public String getMRADClientVersion() {
        return mradClientVersion;
    }
    public void setMRADClientVersion(String newValue) {
        fireBeforePropertyChange(P_MRADClientVersion, this.mradClientVersion, newValue);
        String old = mradClientVersion;
        this.mradClientVersion = newValue;
        firePropertyChange(P_MRADClientVersion, old, this.mradClientVersion);
    }
    
    @OACalculatedProperty(displayName = "Auto Complete", displayLength = 12, columnLength = 10, properties = {P_Application+"."+Application.P_ApplicationType+"."+ApplicationType.P_Code, P_Application+"."+Application.P_Server+"."+Server.P_HostName, P_Application+"."+Application.P_Server+"."+Server.P_IpAddress, P_Application+"."+Application.P_Name})
    public String getAutoComplete() {
        // application.applicationType.code
        // application.name
        String code = null;
        String name = null;
        Application application = this.getApplication();
        if (application != null) {
            name = application.getName();
            ApplicationType applicationType = application.getApplicationType();
            if (applicationType != null) {
                code = applicationType.getCode();
            }
        }
    
        // application.server.hostName
        // application.server.ipAddress
        String hostName = null;
        String ipAddress = null;
        if (application != null) {
            Server server = application.getServer();
            if (server != null) {
                hostName = server.getHostName();
                ipAddress = server.getIpAddress();
            }
        }
    
        String autoComplete = "";
        if (code != null) autoComplete = code;
        if (hostName != null) {
            autoComplete += " " + hostName;
        }
        if (ipAddress != null) {
            autoComplete += " " + ipAddress;
        }
        if (name != null) {
            autoComplete += " " + name;
        }
    
        return autoComplete;
    }
     
    @OACalculatedProperty(displayName = "Version", displayLength = 12, columnLength = 10, properties = {P_Version, P_HostInfo+"."+HostInfo.P_InstallVersion})
    public String getCalcVersion() {
        HostInfo hi = getHostInfo();
        if (hi != null) {
            String s = hi.getInstallVersion();
            if (!OAString.isEmpty(s)) {
                s = OAString.field(s, ":", 2, 99);
                if (s != null && s.toLowerCase().indexOf("error") < 0) {
                    s = s.trim();
                    return s;
                }
            }
        }
        return getVersion();
    }
     
    @OACalculatedProperty(displayName = "Is Connected", displayLength = 5, properties = {P_DtConnected, P_DtDisconnected})
    public boolean getIsConnected() {
        OADateTime dt1 = getDtConnected();
        OADateTime dt2 = getDtDisconnected();
        boolean bConnected = (dt1 != null) && (dt2 == null || dt1.after(dt2));
        return bConnected;
    }
     
    @OAOne(
        reverseName = Application.P_MRADClient, 
        required = true, 
        allowCreateNew = false
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
    
    @OAOne(
        displayName = "Host Info", 
        owner = true, 
        reverseName = HostInfo.P_MRADClient, 
        cascadeSave = true, 
        cascadeDelete = true, 
        autoCreateNew = true, 
        allowAddExisting = false
    )
    public HostInfo getHostInfo() {
        if (hostInfo == null) {
            hostInfo = (HostInfo) getObject(P_HostInfo);
        }
        return hostInfo;
    }
    
    public void setHostInfo(HostInfo newValue) {
        fireBeforePropertyChange(P_HostInfo, this.hostInfo, newValue);
        HostInfo old = this.hostInfo;
        this.hostInfo = newValue;
        firePropertyChange(P_HostInfo, old, this.hostInfo);
    }
    
    @OAOne(
        displayName = "Last MRAD Client Command", 
        isCalculated = true, 
        reverseName = MRADClientCommand.P_MRADClient2
    )
    public MRADClientCommand getLastMRADClientCommand() {
        int x = getMRADClientCommands().getSize();
        return getMRADClientCommands().getAt(x-1);
    }
    public void setLastMRADClientCommand(MRADClientCommand newValue) {
        fireBeforePropertyChange(P_LastMRADClientCommand, this.lastMRADClientCommand, newValue);
        MRADClientCommand old = this.lastMRADClientCommand;
        this.lastMRADClientCommand = newValue;
        firePropertyChange(P_LastMRADClientCommand, old, this.lastMRADClientCommand);
    }
    @OAOne(
        displayName = "Last MRAD Client Message", 
        isCalculated = true, 
        reverseName = MRADClientMessage.P_MRADClient2
    )
    public MRADClientMessage getLastMRADClientMessage() {
        int x = getMRADClientMessages().getSize();
        return getMRADClientMessages().getAt(x-1);
    }
    
    public void setLastMRADClientMessage(MRADClientMessage newValue) {
        fireBeforePropertyChange(P_LastMRADClientMessage, this.lastMRADClientMessage, newValue);
        MRADClientMessage old = this.lastMRADClientMessage;
        this.lastMRADClientMessage = newValue;
        firePropertyChange(P_LastMRADClientMessage, old, this.lastMRADClientMessage);
    }
    @OAMany(
        displayName = "MRAD Client Commands", 
        toClass = MRADClientCommand.class, 
        reverseName = MRADClientCommand.P_MRADClient, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<MRADClientCommand> getMRADClientCommands() {
        if (hubMRADClientCommands == null) {
            hubMRADClientCommands = (Hub<MRADClientCommand>) getHub(P_MRADClientCommands);
            hubMRADClientCommands.addHubListener(new HubListenerAdapter<MRADClientCommand>() {
                @Override
                public void afterAdd(HubEvent<MRADClientCommand> e) {
                    if (isLoading()) return;
                    setLastMRADClientCommand(e.getObject());
                }
            });
        }
        return hubMRADClientCommands;
    }
    @OAMany(
        displayName = "MRAD Client Messages", 
        toClass = MRADClientMessage.class, 
        owner = true, 
        reverseName = MRADClientMessage.P_MRADClient, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<MRADClientMessage> getMRADClientMessages() {
        if (hubMRADClientMessages == null) {
            hubMRADClientMessages = (Hub<MRADClientMessage>) getHub(P_MRADClientMessages);
            hubMRADClientMessages.addHubListener(new HubListenerAdapter<MRADClientMessage>() {
                @Override
                public void afterAdd(HubEvent<MRADClientMessage> e) {
                    if (isLoading()) return;
                    setLastMRADClientMessage(e.getObject());
                }
            });
        }
        return hubMRADClientMessages;
    }
    @OAOne(
        displayName = "MRAD Server", 
        reverseName = MRADServer.P_MRADClients, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"MradServerId"})
    public MRADServer getMRADServer() {
        if (mradServer == null) {
            mradServer = (MRADServer) getObject(P_MRADServer);
        }
        return mradServer;
    }
    
    public void setMRADServer(MRADServer newValue) {
        fireBeforePropertyChange(P_MRADServer, this.mradServer, newValue);
        MRADServer old = this.mradServer;
        this.mradServer = newValue;
        firePropertyChange(P_MRADServer, old, this.mradServer);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        this.hostName = rs.getString(3);
        this.ipAddress = rs.getString(4);
        this.name = rs.getString(5);
        this.description = rs.getString(6);
        this.routerAbsolutePath = rs.getString(7);
        this.startScript = rs.getString(8);
        this.stopScript = rs.getString(9);
        this.snapshotStartScript = rs.getString(10);
        this.directory = rs.getString(11);
        this.version = rs.getString(12);
        this.remoteSocketAddress = rs.getString(13);
        this.applicationStatus = rs.getString(14);
        timestamp = rs.getTimestamp(15);
        if (timestamp != null) this.started = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(16);
        if (timestamp != null) this.ready = new OADateTime(timestamp);
        this.serverTypeId = (int) rs.getInt(17);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, MRADClient.P_ServerTypeId, true);
        }
        this.applicationTypeCode = rs.getString(18);
        timestamp = rs.getTimestamp(19);
        if (timestamp != null) this.dtConnected = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(20);
        if (timestamp != null) this.dtDisconnected = new OADateTime(timestamp);
        this.totalMemory = (long) rs.getLong(21);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, MRADClient.P_TotalMemory, true);
        }
        this.freeMemory = (long) rs.getLong(22);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, MRADClient.P_FreeMemory, true);
        }
        this.javaVendor = rs.getString(23);
        this.javaVersion = rs.getString(24);
        this.osArch = rs.getString(25);
        this.osName = rs.getString(26);
        this.osVersion = rs.getString(27);
        this.processId = rs.getString(28);
        this.installedVersion = rs.getString(29);
        timestamp = rs.getTimestamp(30);
        if (timestamp != null) this.dtInstall = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(31);
        if (timestamp != null) this.dtLastUpdated = new OADateTime(timestamp);
        this.lastConnectionId = (int) rs.getInt(32);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, MRADClient.P_LastConnectionId, true);
        }
        this.mradClientVersion = rs.getString(33);
        int applicationFkey = rs.getInt(34);
        if (!rs.wasNull() && applicationFkey > 0) {
            setProperty(P_Application, new OAObjectKey(applicationFkey));
        }
        int mradServerFkey = rs.getInt(35);
        if (!rs.wasNull() && mradServerFkey > 0) {
            setProperty(P_MRADServer, new OAObjectKey(mradServerFkey));
        }
        if (rs.getMetaData().getColumnCount() != 35) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
