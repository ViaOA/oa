// Generated by OABuilder
package test.xice.tsac2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;

import test.xice.tsac2.model.oa.filter.*;
import test.xice.tsac2.model.oa.propertypath.*;
 
@OAClass(
    shortName = "ulh",
    displayName = "User Login History"
)
@OATable(
    indexes = {
        @OAIndex(name = "UserLoginHistoryLladClient", columns = { @OAIndexColumn(name = "LladClientId") }), 
        @OAIndex(name = "UserLoginHistoryUser", columns = { @OAIndexColumn(name = "UserId") })
    }
)
public class UserLoginHistory extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Login = "Login";
    public static final String P_Login = "Login";
    public static final String PROPERTY_Logout = "Logout";
    public static final String P_Logout = "Logout";
     
     
    public static final String PROPERTY_ClientAppType = "ClientAppType";
    public static final String P_ClientAppType = "ClientAppType";
    public static final String PROPERTY_LLADClient = "LLADClient";
    public static final String P_LLADClient = "LLADClient";
    public static final String PROPERTY_User = "User";
    public static final String P_User = "User";
     
    protected int id;
    protected OADateTime login;
    protected OADateTime logout;
     
    // Links to other objects.
    protected transient ClientAppType clientAppType;
    protected transient LLADClient lladClient;
    protected transient User user;
     
    public UserLoginHistory() {
    }
     
    public UserLoginHistory(int id) {
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
    @OAProperty(displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getLogin() {
        return login;
    }
    
    public void setLogin(OADateTime newValue) {
        fireBeforePropertyChange(P_Login, this.login, newValue);
        OADateTime old = login;
        this.login = newValue;
        firePropertyChange(P_Login, old, this.login);
    }
    @OAProperty(displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getLogout() {
        return logout;
    }
    
    public void setLogout(OADateTime newValue) {
        fireBeforePropertyChange(P_Logout, this.logout, newValue);
        OADateTime old = logout;
        this.logout = newValue;
        firePropertyChange(P_Logout, old, this.logout);
    }
    @OAOne(
        displayName = "Client App Type", 
        reverseName = ClientAppType.P_UserLoginHistories, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"ClientAppTypeId"})
    public ClientAppType getClientAppType() {
        if (clientAppType == null) {
            clientAppType = (ClientAppType) getObject(P_ClientAppType);
        }
        return clientAppType;
    }
    
    public void setClientAppType(ClientAppType newValue) {
        fireBeforePropertyChange(P_ClientAppType, this.clientAppType, newValue);
        ClientAppType old = this.clientAppType;
        this.clientAppType = newValue;
        firePropertyChange(P_ClientAppType, old, this.clientAppType);
    }
    
    @OAOne(
        reverseName = LLADClient.P_UserLoginHistories, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"LladClientId"})
    public LLADClient getLLADClient() {
        if (lladClient == null) {
            lladClient = (LLADClient) getObject(P_LLADClient);
        }
        return lladClient;
    }
    
    public void setLLADClient(LLADClient newValue) {
        fireBeforePropertyChange(P_LLADClient, this.lladClient, newValue);
        LLADClient old = this.lladClient;
        this.lladClient = newValue;
        firePropertyChange(P_LLADClient, old, this.lladClient);
    }
    
    @OAOne(
        reverseName = User.P_UserLoginHistories, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"UserId"})
    public User getUser() {
        if (user == null) {
            user = (User) getObject(P_User);
        }
        return user;
    }
    
    public void setUser(User newValue) {
        fireBeforePropertyChange(P_User, this.user, newValue);
        User old = this.user;
        this.user = newValue;
        firePropertyChange(P_User, old, this.user);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.login = new OADateTime(timestamp);
        timestamp = rs.getTimestamp(3);
        if (timestamp != null) this.logout = new OADateTime(timestamp);
        int clientAppTypeFkey = rs.getInt(4);
        if (!rs.wasNull() && clientAppTypeFkey > 0) {
            setProperty(P_ClientAppType, new OAObjectKey(clientAppTypeFkey));
        }
        int lladClientFkey = rs.getInt(5);
        if (!rs.wasNull() && lladClientFkey > 0) {
            setProperty(P_LLADClient, new OAObjectKey(lladClientFkey));
        }
        int userFkey = rs.getInt(6);
        if (!rs.wasNull() && userFkey > 0) {
            setProperty(P_User, new OAObjectKey(userFkey));
        }
        if (rs.getMetaData().getColumnCount() != 6) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
