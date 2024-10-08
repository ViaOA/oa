// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;
 
@OAClass(
    shortName = "com",
    displayName = "Company",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "name"
)
@OATable(
)
public class Company extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
     
     
    public static final String PROPERTY_LocationTypes = "LocationTypes";
    public static final String P_LocationTypes = "LocationTypes";
    public static final String PROPERTY_Programs = "Programs";
    public static final String P_Programs = "Programs";
     
    protected int id;
    protected OADate created;
    protected String name;
     
    // Links to other objects.
    protected transient Hub<LocationType> hubLocationTypes;
    protected transient Hub<Program> hubPrograms;
     
    public Company() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Company(int id) {
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
    @OAProperty(defaultValue = "new OADate()", displayLength = 8, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreated() {
        return created;
    }
    
    public void setCreated(OADate newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADate old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(maxLength = 75, displayLength = 30)
    @OAColumn(maxLength = 75)
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
        displayName = "Location Types", 
        toClass = LocationType.class, 
        owner = true, 
        reverseName = LocationType.P_Company, 
        cascadeSave = true, 
        cascadeDelete = true, 
        mustBeEmptyForDelete = true, 
        seqProperty = LocationType.P_Seq, 
        sortProperty = LocationType.P_Seq
    )
    public Hub<LocationType> getLocationTypes() {
        if (hubLocationTypes == null) {
            hubLocationTypes = (Hub<LocationType>) getHub(P_LocationTypes);
        }
        return hubLocationTypes;
    }
    
    @OAMany(
        toClass = Program.class, 
        owner = true, 
        reverseName = Program.P_Company, 
        cascadeSave = true, 
        cascadeDelete = true, 
        mustBeEmptyForDelete = true, 
        seqProperty = Program.P_Seq, 
        sortProperty = Program.P_Seq
    )
    public Hub<Program> getPrograms() {
        if (hubPrograms == null) {
            hubPrograms = (Hub<Program>) getHub(P_Programs);
        }
        return hubPrograms;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
