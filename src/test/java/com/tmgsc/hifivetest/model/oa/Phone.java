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
    shortName = "pho",
    displayName = "Phone",
    displayProperty = "phoneNumber"
)
@OATable(
    indexes = {
        @OAIndex(name = "PhoneEmployee", columns = { @OAIndexColumn(name = "EmployeeId") })
    }
)
public class Phone extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_PhoneNumber = "PhoneNumber";
    public static final String P_PhoneNumber = "PhoneNumber";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String P_InactiveDate = "InactiveDate";
     
     
    public static final String PROPERTY_Employee = "Employee";
    public static final String P_Employee = "Employee";
    public static final String PROPERTY_PhoneType = "PhoneType";
    public static final String P_PhoneType = "PhoneType";
     
    protected int id;
    protected OADate created;
    protected String phoneNumber;
    protected OADate inactiveDate;
     
    // Links to other objects.
    protected transient PhoneType phoneType;
     
    public Phone() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public Phone(int id) {
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
    @OAProperty(displayName = "Phone Number", maxLength = 50, displayLength = 18, isPhone = true)
    @OAColumn(maxLength = 50)
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String newValue) {
        fireBeforePropertyChange(P_PhoneNumber, this.phoneNumber, newValue);
        String old = phoneNumber;
        this.phoneNumber = newValue;
        firePropertyChange(P_PhoneNumber, old, this.phoneNumber);
    }
    @OAProperty(displayName = "Inactive Date", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getInactiveDate() {
        return inactiveDate;
    }
    
    public void setInactiveDate(OADate newValue) {
        fireBeforePropertyChange(P_InactiveDate, this.inactiveDate, newValue);
        OADate old = inactiveDate;
        this.inactiveDate = newValue;
        firePropertyChange(P_InactiveDate, old, this.inactiveDate);
    }
    @OAOne(
        reverseName = Employee.P_Phones, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "EmployeePhone", indexName = "EmployeePhone", columns = {"PhoneId"})
    private Employee getEmployee() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Phone Type", 
        reverseName = PhoneType.P_Phones, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"PhoneTypeId"})
    public PhoneType getPhoneType() {
        if (phoneType == null) {
            phoneType = (PhoneType) getObject(P_PhoneType);
        }
        return phoneType;
    }
    
    public void setPhoneType(PhoneType newValue) {
        fireBeforePropertyChange(P_PhoneType, this.phoneType, newValue);
        PhoneType old = this.phoneType;
        this.phoneType = newValue;
        firePropertyChange(P_PhoneType, old, this.phoneType);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.phoneNumber = rs.getString(3);
        date = rs.getDate(4);
        if (date != null) this.inactiveDate = new OADate(date);
        int phoneTypeFkey = rs.getInt(5);
        if (!rs.wasNull() && phoneTypeFkey > 0) {
            setProperty(P_PhoneType, new OAObjectKey(phoneTypeFkey));
        }
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
