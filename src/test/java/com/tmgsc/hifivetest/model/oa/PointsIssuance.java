// Generated by OABuilder
package com.tmgsc.hifivetest.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.tmgsc.hifivetest.model.oa.filter.*;
import com.tmgsc.hifivetest.model.oa.propertypath.*;
import com.viaoa.util.OADate;
 
@OAClass(
    shortName = "pi",
    displayName = "Points Issuance",
    displayProperty = "created"
)
@OATable(
    indexes = {
        @OAIndex(name = "PointsIssuanceFromEmployee", columns = { @OAIndexColumn(name = "FromEmployeeId") }), 
        @OAIndex(name = "PointsIssuanceToEmployee", columns = { @OAIndexColumn(name = "ToEmployeeId") })
    }
)
public class PointsIssuance extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Points = "Points";
    public static final String P_Points = "Points";
    public static final String PROPERTY_Description = "Description";
    public static final String P_Description = "Description";
     
     
    public static final String PROPERTY_FromEmployee = "FromEmployee";
    public static final String P_FromEmployee = "FromEmployee";
    public static final String PROPERTY_ToEmployee = "ToEmployee";
    public static final String P_ToEmployee = "ToEmployee";
     
    protected int id;
    protected OADate created;
    protected double points;
    protected String description;
     
    // Links to other objects.
    protected transient Employee fromEmployee;
    protected transient Employee toEmployee;
     
    public PointsIssuance() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public PointsIssuance(int id) {
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
    @OAProperty(decimalPlaces = 2, displayLength = 7, hasCustomCode = true)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getPoints() {
        return points;
    }
    public void setPoints(double newValue) {
        if (!isLoading()) {
            Employee employee = getFromEmployee();
            if (employee == null) throw new RuntimeException("from employee must be assigned before issuing points");
            EmployeeType ut = employee.getEmployeeType();
            if (ut == null || ut.getType() != EmployeeType.TYPE_programAdmin) {
                double x = employee.getPointsIssuanceBalance();
                x += this.points;
                if (newValue > x) {
                    throw new RuntimeException("points cant be greater then "+x);
                }
            }
        }
        double old = points;
        fireBeforePropertyChange(PROPERTY_Points, old, newValue);
        this.points = newValue;
        firePropertyChange(PROPERTY_Points, old, this.points);
    }
    @OAProperty(maxLength = 254, displayLength = 40)
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
    @OAOne(
        displayName = "From Employee", 
        reverseName = Employee.P_FromPointsIssuances, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"FromEmployeeId"})
    public Employee getFromEmployee() {
        if (fromEmployee == null) {
            fromEmployee = (Employee) getObject(P_FromEmployee);
        }
        return fromEmployee;
    }
    
    public void setFromEmployee(Employee newValue) {
        fireBeforePropertyChange(P_FromEmployee, this.fromEmployee, newValue);
        Employee old = this.fromEmployee;
        this.fromEmployee = newValue;
        firePropertyChange(P_FromEmployee, old, this.fromEmployee);
    }
    
    @OAOne(
        displayName = "To Employee", 
        reverseName = Employee.P_PointsIssuances, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ToEmployeeId"})
    public Employee getToEmployee() {
        if (toEmployee == null) {
            toEmployee = (Employee) getObject(P_ToEmployee);
        }
        return toEmployee;
    }
    
    public void setToEmployee(Employee newValue) {
        fireBeforePropertyChange(P_ToEmployee, this.toEmployee, newValue);
        Employee old = this.toEmployee;
        this.toEmployee = newValue;
        firePropertyChange(P_ToEmployee, old, this.toEmployee);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.points = (double) rs.getDouble(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, PointsIssuance.P_Points, true);
        }
        this.description = rs.getString(4);
        int fromEmployeeFkey = rs.getInt(5);
        if (!rs.wasNull() && fromEmployeeFkey > 0) {
            setProperty(P_FromEmployee, new OAObjectKey(fromEmployeeFkey));
        }
        int toEmployeeFkey = rs.getInt(6);
        if (!rs.wasNull() && toEmployeeFkey > 0) {
            setProperty(P_ToEmployee, new OAObjectKey(toEmployeeFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
