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
    shortName = "cd",
    displayName = "Custom Data",
    displayProperty = "name"
)
@OATable(
    indexes = {
        @OAIndex(name = "CustomDataProgram", columns = { @OAIndexColumn(name = "ProgramId") })
    }
)
public class CustomData extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Code = "Code";
    public static final String P_Code = "Code";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
     
     
    public static final String PROPERTY_EmployeeCustomDatas = "EmployeeCustomDatas";
    public static final String P_EmployeeCustomDatas = "EmployeeCustomDatas";
    public static final String PROPERTY_Program = "Program";
    public static final String P_Program = "Program";
     
    protected int id;
    protected String code;
    protected String name;
    protected String description;
     
    // Links to other objects.
    protected transient Program program;
     
    public CustomData() {
    }
     
    public CustomData(int id) {
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
    @OAProperty(maxLength = 6, displayLength = 6)
    @OAColumn(maxLength = 6)
    public String getCode() {
        return code;
    }
    
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    @OAProperty(maxLength = 25, displayLength = 20)
    @OAColumn(maxLength = 25)
    public String getName() {
        return name;
    }
    public void setName(String newValue) {
        String old = name;
        fireBeforePropertyChange(PROPERTY_Name, old, newValue);
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    @OAProperty(maxLength = 125, displayLength = 40, columnLength = 24)
    @OAColumn(maxLength = 125)
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
        displayName = "Employee Custom Datas", 
        toClass = EmployeeCustomData.class, 
        reverseName = EmployeeCustomData.P_CustomData, 
        createMethod = false
    )
    private Hub<EmployeeCustomData> getEmployeeCustomDatas() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        reverseName = Program.P_CustomData, 
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
        this.code = rs.getString(2);
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
 
