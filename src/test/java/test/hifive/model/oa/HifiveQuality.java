// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
/**
  list of qualities that a user has checked when nominating a Hi5 winner.
*/
@OAClass(
    shortName = "hq",
    displayName = "Hi5 Quality",
    description = "list of qualities that a user has checked when nominating a Hi5 winner.",
    displayProperty = "name",
    rootTreePropertyPaths = {
        "[Company]."+Company.P_Programs+"."+Program.P_HifiveQualities
    }
)
@OATable(
    indexes = {
        @OAIndex(name = "HifiveQualityLocation", columns = { @OAIndexColumn(name = "LocationId") }), 
        @OAIndex(name = "HifiveQualityProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class HifiveQuality extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
     
     
    public static final String PROPERTY_Hifives = "Hifives";
    public static final String P_Hifives = "Hifives";
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
     
    protected int id;
    protected int seq;
    protected String name;
    protected String description;
     
    // Links to other objects.
    protected transient Program program;
     
    public HifiveQuality() {
    }
     
    public HifiveQuality(int id) {
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
    @OAProperty(maxLength = 55, displayLength = 25, columnLength = 20)
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
    @OAProperty(maxLength = 254, displayLength = 35)
    @OAColumn(maxLength = 254)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        fireBeforePropertyChange(P_Description, this.description, newValue);
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    @OAMany(
        toClass = Hifive.class, 
        reverseName = Hifive.P_HifiveQualities, 
        createMethod = false
    )
    @OALinkTable(name = "HifiveHifiveQuality", indexName = "HifiveHifiveQuality", columns = {"HifiveQualityId"})
    private Hub<Hifive> getHifives() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Location.P_HifiveQualities, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "LocationHifiveQuality", indexName = "LocationHifiveQuality", columns = {"HifiveQualityId"})
    private Location getLocation() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Program.P_HifiveQualities, 
        required = true, 
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
        this.seq = (int) rs.getInt(2);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, HifiveQuality.P_Seq, true);
        }
        this.name = rs.getString(3);
        this.description = rs.getString(4);
        int programFkey = rs.getInt(5);
        if (!rs.wasNull() && programFkey > 0) {
            setProperty(P_Program, new OAObjectKey(programFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
