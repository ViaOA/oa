// Generated by OABuilder
package com.theice.tsam.model.oa;
 
import java.util.logging.*;
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsam.delegate.oa.*;
import com.theice.tsam.model.oa.filter.*;
import com.theice.tsam.model.oa.propertypath.*;
import com.viaoa.util.OADateTime;
 
@OAClass(
    shortName = "sil",
    displayName = "Silo",
    displayProperty = "siloType",
    rootTreePropertyPaths = {
        "[Site]."+Site.P_Environments+"."+Environment.P_Silos
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "SiloEnvironment", columns = { @OAIndexColumn(name = "EnvironmentId") })
    }
)
public class Silo extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(Silo.class.getName());
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_NetworkMask = "NetworkMask";
    public static final String P_NetworkMask = "NetworkMask";
    public static final String PROPERTY_CurrentTime = "CurrentTime";
    public static final String P_CurrentTime = "CurrentTime";
    public static final String PROPERTY_SchedulerMessage = "SchedulerMessage";
    public static final String P_SchedulerMessage = "SchedulerMessage";
     
     
    public static final String PROPERTY_ApplicationGroups = "ApplicationGroups";
    public static final String P_ApplicationGroups = "ApplicationGroups";
    public static final String PROPERTY_Environment = "Environment";
    public static final String P_Environment = "Environment";
    public static final String PROPERTY_MRADServer = "MRADServer";
    public static final String P_MRADServer = "MRADServer";
    public static final String PROPERTY_Servers = "Servers";
    public static final String P_Servers = "Servers";
    public static final String PROPERTY_SiloConfigs = "SiloConfigs";
    public static final String P_SiloConfigs = "SiloConfigs";
    public static final String PROPERTY_SiloType = "SiloType";
    public static final String P_SiloType = "SiloType";
     
    protected int id;
    protected String networkMask;
    protected OADateTime currentTime;
    protected String schedulerMessage;
     
    // Links to other objects.
    protected transient Hub<ApplicationGroup> hubApplicationGroups;
    protected transient Environment environment;
    protected transient MRADServer mradServer;
    // protected transient Hub<Server> hubServers;
    protected transient Hub<SiloConfig> hubSiloConfigs;
    protected transient SiloType siloType;
     
    public Silo() {
        if (!isLoading()) {
            setMRADServer(new MRADServer());
        }
    }
     
    public Silo(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 3, isProcessed = true)
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
    
    @OAProperty(displayName = "Network Mask", maxLength = 25, displayLength = 12, columnLength = 10)
    @OAColumn(maxLength = 25)
    public String getNetworkMask() {
        return networkMask;
    }
    public void setNetworkMask(String newValue) {
        fireBeforePropertyChange(P_NetworkMask, this.networkMask, newValue);
        String old = networkMask;
        this.networkMask = newValue;
        firePropertyChange(P_NetworkMask, old, this.networkMask);
    }
    
    @OAProperty(displayName = "Current Time", displayLength = 14, columnLength = 12, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getCurrentTime() {
        return currentTime;
    }
    public void setCurrentTime(OADateTime newValue) {
        fireBeforePropertyChange(P_CurrentTime, this.currentTime, newValue);
        OADateTime old = currentTime;
        this.currentTime = newValue;
        firePropertyChange(P_CurrentTime, old, this.currentTime);
    }
    
    @OAProperty(displayName = "Scheduler Message", maxLength = 200, displayLength = 20, columnLength = 25, isProcessed = true)
    public String getSchedulerMessage() {
        return schedulerMessage;
    }
    public void setSchedulerMessage(String newValue) {
        fireBeforePropertyChange(P_SchedulerMessage, this.schedulerMessage, newValue);
        String old = schedulerMessage;
        this.schedulerMessage = newValue;
        firePropertyChange(P_SchedulerMessage, old, this.schedulerMessage);
    }
    
    @OAMany(
        displayName = "Application Groups", 
        toClass = ApplicationGroup.class, 
        owner = true, 
        reverseName = ApplicationGroup.P_Silo, 
        cascadeSave = true, 
        cascadeDelete = true, 
        seqProperty = ApplicationGroup.P_Seq, 
        sortProperty = ApplicationGroup.P_Seq
    )
    public Hub<ApplicationGroup> getApplicationGroups() {
        if (hubApplicationGroups == null) {
            hubApplicationGroups = (Hub<ApplicationGroup>) getHub(P_ApplicationGroups);
        }
        return hubApplicationGroups;
    }
    
    @OAOne(
        reverseName = Environment.P_Silos, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"EnvironmentId"})
    public Environment getEnvironment() {
        if (environment == null) {
            environment = (Environment) getObject(P_Environment);
        }
        return environment;
    }
    
    public void setEnvironment(Environment newValue) {
        fireBeforePropertyChange(P_Environment, this.environment, newValue);
        Environment old = this.environment;
        this.environment = newValue;
        firePropertyChange(P_Environment, old, this.environment);
    }
    
    @OAOne(
        displayName = "MRAD Server", 
        owner = true, 
        reverseName = MRADServer.P_Silo, 
        cascadeSave = true, 
        cascadeDelete = true, 
        autoCreateNew = true, 
        allowAddExisting = false
    )
    public MRADServer getMRADServer() {
        if (mradServer == null) {
            //qqqqqqqqq    used to read older model data.bin
            mradServer = (MRADServer) OAObjectPropertyDelegate.getProperty(getEnvironment(), PROPERTY_MRADServer);
            if (mradServer != null) {
                OAObjectPropertyDelegate.setProperty(this, PROPERTY_MRADServer, mradServer);
                OAObjectPropertyDelegate.setProperty(mradServer, MRADServer.PROPERTY_Silo, this);
                
                OAObjectPropertyDelegate.removeProperty(getEnvironment(), PROPERTY_MRADServer, false);
                OAObjectPropertyDelegate.removeProperty(mradServer, PROPERTY_Environment, false);
            }
            else {
                mradServer = (MRADServer)  getObject(P_MRADServer);
            }
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
    
    @OAMany(
        toClass = Server.class, 
        owner = true, 
        cacheSize = 25, 
        reverseName = Server.P_Silo, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<Server> getServers() {
        Hub<Server> hubServers;
        {
            hubServers = (Hub<Server>) getHub(P_Servers);
        }
        return hubServers;
    }
    
    @OAMany(
        displayName = "Silo Configs", 
        toClass = SiloConfig.class, 
        reverseName = SiloConfig.P_Silo, 
        matchHub = (Silo.P_SiloType+"."+SiloType.P_ApplicationTypes), 
        matchProperty = SiloConfig.P_ApplicationType
    )
    public Hub<SiloConfig> getSiloConfigs() {
        if (hubSiloConfigs == null) {
            hubSiloConfigs = (Hub<SiloConfig>) getHub(P_SiloConfigs);
        }
        return hubSiloConfigs;
    }
    
    @OAOne(
        displayName = "Silo Type", 
        reverseName = SiloType.P_Silos, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"SiloTypeId"})
    public SiloType getSiloType() {
        if (siloType == null) {
            siloType = (SiloType) getObject(P_SiloType);
        }
        return siloType;
    }
    
    public void setSiloType(SiloType newValue) {
        fireBeforePropertyChange(P_SiloType, this.siloType, newValue);
        SiloType old = this.siloType;
        this.siloType = newValue;
        firePropertyChange(P_SiloType, old, this.siloType);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.networkMask = rs.getString(2);
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(3);
        if (timestamp != null) this.currentTime = new OADateTime(timestamp);
        int environmentFkey = rs.getInt(4);
        if (!rs.wasNull() && environmentFkey > 0) {
            setProperty(P_Environment, new OAObjectKey(environmentFkey));
        }
        int siloTypeFkey = rs.getInt(5);
        if (!rs.wasNull() && siloTypeFkey > 0) {
            setProperty(P_SiloType, new OAObjectKey(siloTypeFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
