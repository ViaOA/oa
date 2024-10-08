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
    shortName = "use",
    displayName = "User",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "fullName"
)
@OATable(
    name = "UserTable",
    indexes = {
        @OAIndex(name = "UserTableLastName", columns = {@OAIndexColumn(name = "LastName")})
    }
)
public class User extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_FirstName = "FirstName";
    public static final String P_FirstName = "FirstName";
    public static final String PROPERTY_LastName = "LastName";
    public static final String P_LastName = "LastName";
    public static final String PROPERTY_LoginId = "LoginId";
    public static final String P_LoginId = "LoginId";
    public static final String PROPERTY_Password = "Password";
    public static final String P_Password = "Password";
    public static final String PROPERTY_InactiveDate = "InactiveDate";
    public static final String P_InactiveDate = "InactiveDate";
    public static final String PROPERTY_Admin = "Admin";
    public static final String P_Admin = "Admin";
    public static final String PROPERTY_Email = "Email";
    public static final String P_Email = "Email";
    public static final String PROPERTY_LoggedIn = "LoggedIn";
    public static final String P_LoggedIn = "LoggedIn";
    public static final String PROPERTY_Location = "Location";
    public static final String P_Location = "Location";
    public static final String PROPERTY_EditProcessed = "EditProcessed";
    public static final String P_EditProcessed = "EditProcessed";
     
    public static final String PROPERTY_FullName = "FullName";
    public static final String P_FullName = "FullName";
     
    public static final String PROPERTY_ApprovedPointsRequests = "ApprovedPointsRequests";
    public static final String P_ApprovedPointsRequests = "ApprovedPointsRequests";
     
    protected int id;
    protected OADate created;
    protected String firstName;
    protected String lastName;
    protected String loginId;
    protected String password;
    protected OADate inactiveDate;
    protected boolean admin;
    protected String email;
    protected boolean loggedIn;
    protected String location;
    protected boolean editProcessed;
     
    // Links to other objects.
    protected transient Hub<PointsRequest> hubApprovedPointsRequests;
     
    public User() {
        if (!isLoading()) {
            setCreated(new OADate());
        }
    }
     
    public User(int id) {
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
    @OAProperty(displayName = "First Name", maxLength = 75, displayLength = 20, columnLength = 12)
    @OAColumn(maxLength = 75)
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String newValue) {
        fireBeforePropertyChange(P_FirstName, this.firstName, newValue);
        String old = firstName;
        this.firstName = newValue;
        firePropertyChange(P_FirstName, old, this.firstName);
    }
    @OAProperty(displayName = "Last Name", maxLength = 75, displayLength = 20, columnLength = 12)
    @OAColumn(maxLength = 75)
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String newValue) {
        fireBeforePropertyChange(P_LastName, this.lastName, newValue);
        String old = lastName;
        this.lastName = newValue;
        firePropertyChange(P_LastName, old, this.lastName);
    }
    @OAProperty(displayName = "Login", maxLength = 75, displayLength = 40, columnLength = 10)
    @OAColumn(maxLength = 75)
    public String getLoginId() {
        return loginId;
    }
    
    public void setLoginId(String newValue) {
        fireBeforePropertyChange(P_LoginId, this.loginId, newValue);
        String old = loginId;
        this.loginId = newValue;
        firePropertyChange(P_LoginId, old, this.loginId);
    }
    @OAProperty(maxLength = 50, displayLength = 15, columnLength = 10, isPassword = true)
    @OAColumn(maxLength = 50)
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String newValue) {
        fireBeforePropertyChange(P_Password, this.password, newValue);
        String old = password;
        this.password = newValue;
        firePropertyChange(P_Password, old, this.password);
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
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean newValue) {
        fireBeforePropertyChange(P_Admin, this.admin, newValue);
        boolean old = admin;
        this.admin = newValue;
        firePropertyChange(P_Admin, old, this.admin);
    }
    @OAProperty(maxLength = 125, displayLength = 20, columnLength = 15)
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
    @OAProperty(displayName = "Logged In", displayLength = 5, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getLoggedIn() {
        return loggedIn;
    }
    
    public void setLoggedIn(boolean newValue) {
        fireBeforePropertyChange(P_LoggedIn, this.loggedIn, newValue);
        boolean old = loggedIn;
        this.loggedIn = newValue;
        firePropertyChange(P_LoggedIn, old, this.loggedIn);
    }
    @OAProperty(description = "Office location", maxLength = 45, displayLength = 24, columnLength = 12)
    @OAColumn(maxLength = 45)
    /**
      Office location
    */
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String newValue) {
        fireBeforePropertyChange(P_Location, this.location, newValue);
        String old = location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    @OAProperty(displayName = "Edit Processed", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getEditProcessed() {
        return editProcessed;
    }
    
    public void setEditProcessed(boolean newValue) {
        fireBeforePropertyChange(P_EditProcessed, this.editProcessed, newValue);
        boolean old = editProcessed;
        this.editProcessed = newValue;
        firePropertyChange(P_EditProcessed, old, this.editProcessed);
    }
    @OACalculatedProperty(displayName = "Full Name", displayLength = 30, columnLength = 25, properties = {P_FirstName, P_LastName})
    public String getFullName() {
        String fullname = "";
    
        // firstName
        String firstName = this.getFirstName();
        if (!OAString.isEmpty(firstName)) {
            if (fullname.length() > 0) fullname += " ";
            fullname += firstName;
        }
    
        // lastName
        String lastName = this.getLastName();
        if (!OAString.isEmpty(lastName)) {
            if (fullname.length() > 0) fullname += " ";
            fullname += lastName;
        }
        return fullname;
    }
     
    @OAMany(
        displayName = "Approved Points Requests", 
        toClass = PointsRequest.class, 
        reverseName = PointsRequest.P_ApprovingUser
    )
    public Hub<PointsRequest> getApprovedPointsRequests() {
        if (hubApprovedPointsRequests == null) {
            hubApprovedPointsRequests = (Hub<PointsRequest>) getHub(P_ApprovedPointsRequests);
        }
        return hubApprovedPointsRequests;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Date date;
        date = rs.getDate(2);
        if (date != null) this.created = new OADate(date);
        this.firstName = rs.getString(3);
        this.lastName = rs.getString(4);
        this.loginId = rs.getString(5);
        this.password = rs.getString(6);
        date = rs.getDate(7);
        if (date != null) this.inactiveDate = new OADate(date);
        this.admin = rs.getBoolean(8);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, User.P_Admin, true);
        }
        this.email = rs.getString(9);
        this.loggedIn = rs.getBoolean(10);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, User.P_LoggedIn, true);
        }
        this.location = rs.getString(11);
        this.editProcessed = rs.getBoolean(12);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, User.P_EditProcessed, true);
        }
        if (rs.getMetaData().getColumnCount() != 12) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
