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
    shortName = "hif",
    displayName = "Hi5",
    displayProperty = "created"
)
@OATable(
    indexes = {
        @OAIndex(name = "HifiveEmployee", columns = { @OAIndexColumn(name = "EmployeeId") }), 
        @OAIndex(name = "HifiveFromEmployee", columns = { @OAIndexColumn(name = "FromEmployeeId") }), 
        @OAIndex(name = "HifiveManager", columns = { @OAIndexColumn(name = "ManagerId") })
    }
)
public class Hifive extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_ApprovedDate = "ApprovedDate";
    public static final String P_ApprovedDate = "ApprovedDate";
    public static final String PROPERTY_AckDate = "AckDate";
    public static final String P_AckDate = "AckDate";
    public static final String PROPERTY_Points = "Points";
    public static final String P_Points = "Points";
    public static final String PROPERTY_Comment = "Comment";
    public static final String P_Comment = "Comment";
     
     
    public static final String PROPERTY_Employee = "Employee";
    public static final String P_Employee = "Employee";
    public static final String PROPERTY_EmployeeSurvey = "EmployeeSurvey";
    public static final String P_EmployeeSurvey = "EmployeeSurvey";
    public static final String PROPERTY_FromEmployee = "FromEmployee";
    public static final String P_FromEmployee = "FromEmployee";
    public static final String PROPERTY_HifiveQualities = "HifiveQualities";
    public static final String P_HifiveQualities = "HifiveQualities";
    public static final String PROPERTY_HifiveReason = "HifiveReason";
    public static final String P_HifiveReason = "HifiveReason";
    public static final String PROPERTY_Manager = "Manager";
    public static final String P_Manager = "Manager";
     
    protected int id;
    protected OADate created;
    protected OADate approvedDate;
    protected OADate ackDate;
    protected double points;
    protected String comment;
     
    // Links to other objects.
    protected transient Employee employee;
    protected transient EmployeeSurvey employeeSurvey;
    protected transient Employee fromEmployee;
    protected transient Hub<HifiveQuality> hubHifiveQualities;
    protected transient HifiveReason hifiveReason;
    protected transient Employee manager;
     
    public Hifive() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Hifive(int id) {
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
    @OAProperty(displayName = "Approved Date", displayLength = 8, columnName = "Approved")
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(OADate newValue) {
        fireBeforePropertyChange(P_ApprovedDate, this.approvedDate, newValue);
        OADate old = approvedDate;
        this.approvedDate = newValue;
        firePropertyChange(P_ApprovedDate, old, this.approvedDate);
    }
    @OAProperty(displayName = "Ack Date", description = "The date the hi5 was Acknowledge by nominated user", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    /**
      The date the hi5 was Acknowledge by nominated user
    */
    public OADate getAckDate() {
        return ackDate;
    }
    
    public void setAckDate(OADate newValue) {
        fireBeforePropertyChange(P_AckDate, this.ackDate, newValue);
        OADate old = ackDate;
        this.ackDate = newValue;
        firePropertyChange(P_AckDate, old, this.ackDate);
    }
    @OAProperty(decimalPlaces = 2, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DOUBLE)
    public double getPoints() {
        return points;
    }
    
    public void setPoints(double newValue) {
        fireBeforePropertyChange(P_Points, this.points, newValue);
        double old = points;
        this.points = newValue;
        firePropertyChange(P_Points, old, this.points);
    }
    @OAProperty(displayName = "Comments", maxLength = 8, displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getComment() {
        return comment;
    }
    
    public void setComment(String newValue) {
        fireBeforePropertyChange(P_Comment, this.comment, newValue);
        String old = comment;
        this.comment = newValue;
        firePropertyChange(P_Comment, old, this.comment);
    }
    @OAOne(
        reverseName = Employee.P_Hifives, 
        required = true, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"EmployeeId"})
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
    
    @OAOne(
        displayName = "Employee Survey", 
        reverseName = EmployeeSurvey.P_Hifives, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"EmployeeSurveyId"})
    public EmployeeSurvey getEmployeeSurvey() {
        if (employeeSurvey == null) {
            employeeSurvey = (EmployeeSurvey) getObject(P_EmployeeSurvey);
        }
        return employeeSurvey;
    }
    
    public void setEmployeeSurvey(EmployeeSurvey newValue) {
        fireBeforePropertyChange(P_EmployeeSurvey, this.employeeSurvey, newValue);
        EmployeeSurvey old = this.employeeSurvey;
        this.employeeSurvey = newValue;
        firePropertyChange(P_EmployeeSurvey, old, this.employeeSurvey);
    }
    
    @OAOne(
        displayName = "From Employee", 
        reverseName = Employee.P_HifiveNominations, 
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
    
    @OAMany(
        displayName = "Hi5 Qualities", 
        toClass = HifiveQuality.class, 
        reverseName = HifiveQuality.P_Hifives
    )
    @OALinkTable(name = "HifiveHifiveQuality", indexName = "HifiveQualityHifive", columns = {"HifiveId"})
    public Hub<HifiveQuality> getHifiveQualities() {
        if (hubHifiveQualities == null) {
            hubHifiveQualities = (Hub<HifiveQuality>) getHub(P_HifiveQualities);
        }
        return hubHifiveQualities;
    }
    
    @OAOne(
        displayName = "Hi5 Reason", 
        reverseName = HifiveReason.P_Hifives, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"HifiveReasonId"})
    public HifiveReason getHifiveReason() {
        if (hifiveReason == null) {
            hifiveReason = (HifiveReason) getObject(P_HifiveReason);
        }
        return hifiveReason;
    }
    
    public void setHifiveReason(HifiveReason newValue) {
        fireBeforePropertyChange(P_HifiveReason, this.hifiveReason, newValue);
        HifiveReason old = this.hifiveReason;
        this.hifiveReason = newValue;
        firePropertyChange(P_HifiveReason, old, this.hifiveReason);
    }
    
    @OAOne(
        reverseName = Employee.P_ManagerHifives
    )
    @OAFkey(columns = {"ManagerId"})
    public Employee getManager() {
        if (manager == null) {
            manager = (Employee) getObject(P_Manager);
        }
        return manager;
    }
    
    public void setManager(Employee newValue) {
        fireBeforePropertyChange(P_Manager, this.manager, newValue);
        Employee old = this.manager;
        this.manager = newValue;
        firePropertyChange(P_Manager, old, this.manager);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        date = rs.getDate(3);
        if (date != null) this.approvedDate = new OADate(date);
        date = rs.getDate(4);
        if (date != null) this.ackDate = new OADate(date);
        this.points = (double) rs.getDouble(5);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, Hifive.P_Points, true);
        }
        this.comment = rs.getString(6);
        int employeeFkey = rs.getInt(7);
        if (!rs.wasNull() && employeeFkey > 0) {
            setProperty(P_Employee, new OAObjectKey(employeeFkey));
        }
        int employeeSurveyFkey = rs.getInt(8);
        if (!rs.wasNull() && employeeSurveyFkey > 0) {
            setProperty(P_EmployeeSurvey, new OAObjectKey(employeeSurveyFkey));
        }
        int fromEmployeeFkey = rs.getInt(9);
        if (!rs.wasNull() && fromEmployeeFkey > 0) {
            setProperty(P_FromEmployee, new OAObjectKey(fromEmployeeFkey));
        }
        int hifiveReasonFkey = rs.getInt(10);
        if (!rs.wasNull() && hifiveReasonFkey > 0) {
            setProperty(P_HifiveReason, new OAObjectKey(hifiveReasonFkey));
        }
        int managerFkey = rs.getInt(11);
        if (!rs.wasNull() && managerFkey > 0) {
            setProperty(P_Manager, new OAObjectKey(managerFkey));
        }
        if (rs.getMetaData().getColumnCount() != 11) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
