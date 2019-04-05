// Generated by OABuilder
package test.xice.tsam.model.oa;
 
import java.util.logging.*;
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsam.delegate.oa.*;
import test.xice.tsam.model.oa.filter.*;
import test.xice.tsam.model.oa.propertypath.*;

import test.xice.tsam.model.oa.Environment;
import test.xice.tsam.model.oa.EnvironmentType;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "et",
    displayName = "Environment Type",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name"
)
@OATable(
)
public class EnvironmentType extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(EnvironmentType.class.getName());
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Type = "Type";
    public static final String P_Type = "Type";
    public static final String PROPERTY_TypeAsString = "TypeAsString";
    public static final String P_TypeAsString = "TypeAsString";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
     
     
    public static final String PROPERTY_Environments = "Environments";
    public static final String P_Environments = "Environments";
     
    protected int id;
    protected int type;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_AM = 1;
    public static final int TYPE_AM2 = 2;
    public static final int TYPE_AP = 3;
    public static final int TYPE_AT = 4;
    public static final int TYPE_ATMG = 5;
    public static final int TYPE_CSI1 = 6;
    public static final int TYPE_DM = 7;
    public static final int TYPE_DV1 = 8;
    public static final int TYPE_DV2 = 9;
    public static final int TYPE_DV3 = 10;
    public static final int TYPE_DV4 = 11;
    public static final int TYPE_DV5 = 12;
    public static final int TYPE_FT = 13;
    public static final int TYPE_FT2 = 14;
    public static final int TYPE_FT3 = 15;
    public static final int TYPE_LT = 16;
    public static final int TYPE_MRTEST = 17;
    public static final int TYPE_ORD = 18;
    public static final int TYPE_ORDMG = 19;
    public static final int TYPE_PF = 20;
    public static final int TYPE_PL = 21;
    public static final int TYPE_PL2 = 22;
    public static final int TYPE_PMT = 23;
    public static final int TYPE_PS = 24;
    public static final int TYPE_SB1 = 25;
    public static final int TYPE_SB2 = 26;
    public static final int TYPE_SB3 = 27;
    public static final int TYPE_SB4 = 28;
    public static final int TYPE_SB5 = 29;
    public static final int TYPE_SB6 = 30;
    public static final int TYPE_SB7 = 31;
    public static final int TYPE_SB8 = 32;
    public static final int TYPE_ST = 33;
    public static final int TYPE_TECTEST = 34;
    public static final int TYPE_TR1 = 35;
    public static final int TYPE_TR2 = 36;
    public static final int TYPE_TR3 = 37;
    public static final int TYPE_UT1 = 38;
    public static final int TYPE_SR1 = 39;
    public static final int TYPE_PT2 = 40;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("Unknown");
        hubType.addElement("AM");
        hubType.addElement("AM2");
        hubType.addElement("AP");
        hubType.addElement("AT");
        hubType.addElement("AT-MG");
        hubType.addElement("CSI1");
        hubType.addElement("DM");
        hubType.addElement("DV1");
        hubType.addElement("DV2");
        hubType.addElement("DV3");
        hubType.addElement("DV4");
        hubType.addElement("DV5");
        hubType.addElement("FT");
        hubType.addElement("FT2");
        hubType.addElement("FT3");
        hubType.addElement("LT");
        hubType.addElement("MRTest");
        hubType.addElement("ORD");
        hubType.addElement("ORD-MG");
        hubType.addElement("PF");
        hubType.addElement("PL");
        hubType.addElement("PL2");
        hubType.addElement("PMT");
        hubType.addElement("PS");
        hubType.addElement("SB1");
        hubType.addElement("SB2");
        hubType.addElement("SB3");
        hubType.addElement("SB4");
        hubType.addElement("SB5");
        hubType.addElement("SB6");
        hubType.addElement("SB7");
        hubType.addElement("SB8");
        hubType.addElement("ST");
        hubType.addElement("TechTest");
        hubType.addElement("TR1");
        hubType.addElement("TR2");
        hubType.addElement("TR3");
        hubType.addElement("UT1");
        hubType.addElement("SR1");
        hubType.addElement("Pt2");
    }
    protected String name;
     
    // Links to other objects.
     
    public EnvironmentType() {
    }
     
    public EnvironmentType(int id) {
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
    
    @OAProperty(isImportMatch = true, displayLength = 10, columnLength = 6, isProcessed = true, isNameValue = true)
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
     
    @OAProperty(maxLength = 35, displayLength = 10, isProcessed = true)
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
    
    @OAMany(
        toClass = Environment.class, 
        reverseName = Environment.P_EnvironmentType, 
        mustBeEmptyForDelete = true, 
        createMethod = false
    )
    private Hub<Environment> getEnvironments() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.type = (int) rs.getInt(2);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, EnvironmentType.P_Type, true);
        }
        this.name = rs.getString(3);
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
