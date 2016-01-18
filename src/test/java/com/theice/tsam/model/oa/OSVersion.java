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
 
@OAClass(
    shortName = "osv",
    displayName = "OS Version",
    displayProperty = "displayName"
)
@OATable(
    indexes = {
        @OAIndex(name = "OSVersionOperatingSystem", columns = { @OAIndexColumn(name = "OperatingSystemId") })
    }
)
public class OSVersion extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OSVersion.class.getName());
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
     
    public static final String PROPERTY_DisplayName = "DisplayName";
    public static final String P_DisplayName = "DisplayName";
     
    public static final String PROPERTY_OperatingSystem = "OperatingSystem";
    public static final String P_OperatingSystem = "OperatingSystem";
    public static final String PROPERTY_Servers = "Servers";
    public static final String P_Servers = "Servers";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
    protected transient OperatingSystem operatingSystem;
     
    public OSVersion() {
    }
     
    public OSVersion(int id) {
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
    
    @OAProperty(maxLength = 35, displayLength = 20, columnLength = 10)
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
    
    @OACalculatedProperty(displayName = "Name", displayLength = 18, columnLength = 15, properties = {P_Name, P_OperatingSystem+"."+OperatingSystem.P_Name})
    public String getDisplayName() {
        String displayName = "";
    
        // operatingSystem
        OperatingSystem operatingSystem = this.getOperatingSystem();
        if (operatingSystem != null) {
            displayName = operatingSystem.getName();
            if (displayName == null) displayName = " ";
        }
    
        name = this.getName();
        if (name != null) {
            if (displayName.length() > 0) displayName += " ";
            displayName += name;
        }
    
        return displayName;
    }
    
     
    @OAOne(
        displayName = "Operating System", 
        reverseName = OperatingSystem.P_OSVersions, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"OperatingSystemId"})
    public OperatingSystem getOperatingSystem() {
        if (operatingSystem == null) {
            operatingSystem = (OperatingSystem) getObject(P_OperatingSystem);
        }
        return operatingSystem;
    }
    
    public void setOperatingSystem(OperatingSystem newValue) {
        fireBeforePropertyChange(P_OperatingSystem, this.operatingSystem, newValue);
        OperatingSystem old = this.operatingSystem;
        this.operatingSystem = newValue;
        firePropertyChange(P_OperatingSystem, old, this.operatingSystem);
    }
    
    @OAMany(
        toClass = Server.class, 
        reverseName = Server.P_OSVersion, 
        createMethod = false
    )
    private Hub<Server> getServers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        int operatingSystemFkey = rs.getInt(3);
        if (!rs.wasNull() && operatingSystemFkey > 0) {
            setProperty(P_OperatingSystem, new OAObjectKey(operatingSystemFkey));
        }
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
