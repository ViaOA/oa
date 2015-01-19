// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
 
@OAClass(
    shortName = "pcv",
    displayName = "Points Core Value",
    displayProperty = "name",
    sortProperty = "seq"
)
@OATable(
    indexes = {
        @OAIndex(name = "PointsCoreValueLocation", columns = { @OAIndexColumn(name = "LocationId") }), 
        @OAIndex(name = "PointsCoreValueProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class PointsCoreValue extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
     
     
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_PointsRecords = "PointsRecords";
    public static final String P_PointsRecords = "PointsRecords";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
     
    protected int id;
    protected String name;
    protected int seq;
    protected String description;
     
    // Links to other objects.
    protected transient Location location;
    protected transient Hub<PointsRecord> hubPointsRecords;
    protected transient Program program;
     
    public PointsCoreValue() {
    }
     
    public PointsCoreValue(int id) {
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
    @OAProperty(maxLength = 40, displayLength = 40)
    @OAColumn(maxLength = 40)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayLength = 5, isAutoSeq = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSeq() {
        return seq;
    }
    
    public void setSeq(int newValue) {
        fireBeforePropertyChange(P_Seq, this.seq, newValue);
        int old = seq;
        this.seq = newValue;
        firePropertyChange(P_Seq, old, this.seq);
    }
    @OAProperty(maxLength = 200, displayLength = 40)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        fireBeforePropertyChange(P_Description, this.description, newValue);
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    @OAOne(
        reverseName = Location.P_PointsCoreValues, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"LocationId"})
    public Location getLocation() {
        if (location == null) {
            location = (Location) getObject(P_Location);
        }
        return location;
    }
    
    public void setLocation(Location newValue) {
        fireBeforePropertyChange(P_Location, this.location, newValue);
        Location old = this.location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    
    @OAMany(
        displayName = "Points Records", 
        toClass = PointsRecord.class, 
        reverseName = PointsRecord.P_PointsCoreValue
    )
    public Hub<PointsRecord> getPointsRecords() {
        if (hubPointsRecords == null) {
            hubPointsRecords = (Hub<PointsRecord>) getHub(P_PointsRecords);
        }
        return hubPointsRecords;
    }
    
    @OAOne(
        reverseName = Program.P_PointsCoreValues, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ProgramId"})
    public Program getProgram() {
        if (program == null) {
            program = (Program) getObject(P_Program);
        }
        return program;
    }
    
    public void setProgram(Program newValue) {
        fireBeforePropertyChange(P_Program, this.program, newValue);
        Program old = this.program;
        this.program = newValue;
        firePropertyChange(P_Program, old, this.program);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, PointsCoreValue.P_Seq, true);
        }
        this.description = rs.getString(4);
        int locationFkey = rs.getInt(5);
        if (!rs.wasNull() && locationFkey > 0) {
            setProperty(P_Location, new OAObjectKey(locationFkey));
        }
        int programFkey = rs.getInt(6);
        if (!rs.wasNull() && programFkey > 0) {
            setProperty(P_Program, new OAObjectKey(programFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
