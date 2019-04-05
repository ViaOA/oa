// Generated by OABuilder
package test.xice.tsac3.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsac3.model.oa.filter.*;
import test.xice.tsac3.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "rcc",
    displayName = "RCCommand",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "type"
)
@OATable(
)
public class RCCommand extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
    public static final String PROPERTY_CommandLine = "CommandLine";
    public static final String P_CommandLine = "CommandLine";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
     
     
    public static final String PROPERTY_RCExecutes = "RCExecutes";
    public static final String P_RCExecutes = "RCExecutes";
     
    protected int id;
    protected String description;
    protected String commandLine;
    protected int type;
    public static final int TYPE_download = 0;
    public static final int TYPE_propagate = 1;
    public static final int TYPE_install = 2;
    public static final int TYPE_getVersions = 3;
    public static final int TYPE_getInstalledVersions = 4;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("Download");
        hubType.addElement("Propagate");
        hubType.addElement("Install");
        hubType.addElement("Get Versions");
        hubType.addElement("Get Installed Versions");
    }
     
    // Links to other objects.
     
    public RCCommand() {
    }
     
    public RCCommand(int id) {
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
    @OAProperty(maxLength = 75, displayLength = 22, columnLength = 18)
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
    @OAProperty(displayName = "Command Line", maxLength = 9999, displayLength = 40)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getCommandLine() {
        return commandLine;
    }
    
    public void setCommandLine(String newValue) {
        fireBeforePropertyChange(P_CommandLine, this.commandLine, newValue);
        String old = commandLine;
        this.commandLine = newValue;
        firePropertyChange(P_CommandLine, old, this.commandLine);
    }
    @OAProperty(displayLength = 5, isNameValue = true)
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
    @OAMany(
        toClass = RCExecute.class, 
        reverseName = RCExecute.P_RCCommand, 
        createMethod = false
    )
    private Hub<RCExecute> getRCExecutes() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.description = rs.getString(2);
        this.commandLine = rs.getString(3);
        this.type = (int) rs.getInt(4);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, RCCommand.P_Type, true);
        }
        if (rs.getMetaData().getColumnCount() != 4) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
