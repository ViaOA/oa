// Generated by OABuilder
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.propertypath.*;
 
@OAClass(
    shortName = "cat",
    displayName = "Client Application Type",
    isLookup = true,
    isPreSelect = true,
    displayProperty = "name",
    sortProperty = "seq"
)
@OATable(
)
public class ClientAppType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Seq = "Seq";
    public static final String P_Seq = "Seq";
     
     
    public static final String PROPERTY_UserLoginHistories = "UserLoginHistories";
    public static final String P_UserLoginHistories = "UserLoginHistories";
    public static final String PROPERTY_UserLogins = "UserLogins";
    public static final String P_UserLogins = "UserLogins";
     
    protected int id;
    protected String name;
    protected int seq;
     
    // Links to other objects.
     
    public ClientAppType() {
    }
     
    public ClientAppType(int id) {
        this();
        setId(id);
    }
     
    @OAProperty(isUnique = true, displayLength = 5, isProcessed = true)
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
    @OAProperty(maxLength = 35, displayLength = 24, columnLength = 18, isProcessed = true)
    @OAColumn(maxLength = 35)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(displayLength = 5, isProcessed = true, isAutoSeq = true)
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
    @OAMany(
        displayName = "User Login Histories", 
        toClass = UserLoginHistory.class, 
        reverseName = UserLoginHistory.P_ClientAppType, 
        mustBeEmptyForDelete = true, 
        createMethod = false
    )
    private Hub<UserLoginHistory> getUserLoginHistories() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAMany(
        displayName = "User Logins", 
        toClass = UserLogin.class, 
        reverseName = UserLogin.P_ClientAppType, 
        mustBeEmptyForDelete = true, 
        createMethod = false
    )
    private Hub<UserLogin> getUserLogins() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        this.name = rs.getString(2);
        this.seq = (int) rs.getInt(3);
        if (rs.wasNull()) {
            OAObjectInfoDelegate.setPrimitiveNull(this, ClientAppType.P_Seq, true);
        }
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
