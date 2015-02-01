// Generated by OABuilder
package com.theice.tsactest2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsactest2.model.oa.filter.*;
import com.theice.tsactest2.model.oa.propertypath.*;
import com.viaoa.util.OADateTime;
 
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
     
    public static final String PROPERTY_DisplayName = "DisplayName";
    public static final String P_DisplayName = "DisplayName";
     
    public static final String PROPERTY_Applications = "Applications";
    public static final String P_Applications = "Applications";
    public static final String PROPERTY_RCServerListDetails = "RCServerListDetails";
    public static final String P_RCServerListDetails = "RCServerListDetails";
    public static final String PROPERTY_Silo = "Silo";
    public static final String P_Silo = "Silo";
     
    protected int id;
    protected OADateTime created;
    protected String name;
    protected String hostName;
    protected String ipAddress;
    protected String dnsName;
    protected String shortDnsName;
     
    // Links to other objects.
    protected transient Hub<Application> hubApplications;
    protected transient Silo silo;
     
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
    @OACalculatedProperty(displayName = "Display Name", displayLength = 22, columnLength = 20, properties = {P_Name, P_HostName, P_IpAddress})
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
        return displayName;
    }
     
    @OAMany(
        toClass = Application.class, 
        owner = true, 
        reverseName = Application.P_Server, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<Application> getApplications() {
        if (hubApplications == null) {
            hubApplications = (Hub<Application>) getHub(P_Applications);
        }
        return hubApplications;
    }
    
    @OAMany(
        displayName = "RCServer List Details", 
        toClass = RCServerListDetail.class, 
        reverseName = RCServerListDetail.P_Server, 
        createMethod = false
    )
    private Hub<RCServerListDetail> getRCServerListDetails() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
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
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        this.name = rs.getString(3);
        this.hostName = rs.getString(4);
        this.ipAddress = rs.getString(5);
        this.dnsName = rs.getString(6);
        this.shortDnsName = rs.getString(7);
        int siloFkey = rs.getInt(8);
        if (!rs.wasNull() && siloFkey > 0) {
            setProperty(P_Silo, new OAObjectKey(siloFkey));
        }
        if (rs.getMetaData().getColumnCount() != 8) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
