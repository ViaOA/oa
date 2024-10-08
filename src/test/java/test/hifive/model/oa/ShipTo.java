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
    shortName = "st",
    displayName = "Ship To",
    displayProperty = "name"
)
@OATable(
)
public class ShipTo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Note = "Note";
    public static final String P_Note = "Note";
    public static final String PROPERTY_Email = "Email";
    public static final String P_Email = "Email";
    public static final String PROPERTY_PhoneNumber = "PhoneNumber";
    public static final String P_PhoneNumber = "PhoneNumber";
     
     
    public static final String PROPERTY_Address = "Address";
    public static final String P_Address = "Address";
    public static final String PROPERTY_EmployeeAward = "EmployeeAward";
    public static final String P_EmployeeAward = "EmployeeAward";
    public static final String PROPERTY_InspireOrder = "InspireOrder";
    public static final String P_InspireOrder = "InspireOrder";
     
    protected int id;
    protected OADate created;
    protected String name;
    protected String note;
    protected String email;
    protected String phoneNumber;
     
    // Links to other objects.
    protected transient Address address;
     
    public ShipTo() {
        if (!isLoading()) {
            setCreated(new OADate());
            setAddress(new Address());
        }
    }
     
    public ShipTo(int id) {
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
    @OAProperty(maxLength = 125, displayLength = 35, columnLength = 22)
    @OAColumn(maxLength = 125)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 254, displayLength = 40, columnLength = 20)
    @OAColumn(maxLength = 254)
    public String getNote() {
        return note;
    }
    
    public void setNote(String newValue) {
        fireBeforePropertyChange(P_Note, this.note, newValue);
        String old = note;
        this.note = newValue;
        firePropertyChange(P_Note, old, this.note);
    }
    @OAProperty(maxLength = 125, isUnicode = true, displayLength = 20, columnLength = 15, isEmail = true)
    @OAColumn(maxLength = 125)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        fireBeforePropertyChange(P_Email, this.email, newValue);
        String old = email;
        this.email = newValue;
        firePropertyChange(P_Email, old, this.email);
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
    @OAOne(
        reverseName = Address.P_ShipTos, 
        required = true, 
        autoCreateNew = true, 
        allowAddExisting = false
    )
    @OAFkey(columns = {"AddressId"})
    public Address getAddress() {
        if (address == null) {
            address = (Address) getObject(P_Address);
        }
        return address;
    }
    
    public void setAddress(Address newValue) {
        fireBeforePropertyChange(P_Address, this.address, newValue);
        Address old = this.address;
        this.address = newValue;
        firePropertyChange(P_Address, old, this.address);
    }
    
    @OAOne(
        displayName = "Employee Award", 
        reverseName = EmployeeAward.P_ShipTo, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private EmployeeAward getEmployeeAward() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Inspire Order", 
        reverseName = InspireOrder.P_ShipTo, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    private InspireOrder getInspireOrder() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.name = rs.getString(3);
        this.note = rs.getString(4);
        this.email = rs.getString(5);
        this.phoneNumber = rs.getString(6);
        int addressFkey = rs.getInt(7);
        if (!rs.wasNull() && addressFkey > 0) {
            setProperty(P_Address, new OAObjectKey(addressFkey));
        }
        if (rs.getMetaData().getColumnCount() != 7) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
