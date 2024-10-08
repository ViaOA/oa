// Generated by OABuilder
package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.hifive.model.oa.filter.*;
import test.hifive.model.oa.propertypath.*;

import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "icv",
    displayName = "Inspire Core Value",
    displayProperty = "name"
)
@OATable(
    indexes = {
        @OAIndex(name = "InspireCoreValueLocation", columns = { @OAIndexColumn(name = "LocationId") }), 
        @OAIndex(name = "InspireCoreValueProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class InspireCoreValue extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_Employee = "Employee";
    public static final String P_Employee = "Employee";
    public static final String PROPERTY_Inspires = "Inspires";
    public static final String P_Inspires = "Inspires";
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
     
    protected int id;
    protected String name;
    protected int seq;
     
    // Links to other objects.
    protected transient Employee employee;
    protected transient Program program;
     
    public InspireCoreValue() {
    }
     
    public InspireCoreValue(int id) {
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
    @OAProperty(maxLength = 75, displayLength = 22, columnLength = 24)
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
    @OAOne(
        isCalculated = true, 
        reverseName = Employee.P_InspireCoreValues
    )
    public Employee getEmployee() {
        if (employee == null) {
            employee = (Employee) getObject(P_Employee);
        }
        return employee;
    }
    
    public void setEmployee(Employee newValue) {
        fireBeforePropertyChange(P_Employee, this.employee, newValue);
        Employee old = this.employee;
        this.employee = newValue;
        firePropertyChange(P_Employee, old, this.employee);
    }
    
    @OAMany(
        toClass = Inspire.class, 
        reverseName = Inspire.P_InspireCoreValue, 
        createMethod = false
    )
    private Hub<Inspire> getInspires() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Location.P_InspireCoreValues, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "LocationInspireCoreValue", indexName = "LocationInspireCoreValue", columns = {"InspireCoreValueId"})
    private Location getLocation() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Program.P_InspireCoreValues, 
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
        this.name = rs.getString(2);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, InspireCoreValue.P_Seq, true);
        }
        int programFkey = rs.getInt(4);
        if (!rs.wasNull() && programFkey > 0) {
            setProperty(P_Program, new OAObjectKey(programFkey));
        }
        if (rs.getMetaData().getColumnCount() != 4) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
