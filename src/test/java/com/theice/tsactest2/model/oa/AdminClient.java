// Generated by OABuilder
package com.theice.tsactest2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsactest2.model.oa.filter.*;
import com.theice.tsactest2.model.oa.propertypath.*;
 
@OAClass(
    shortName = "ac",
    displayName = "Admin Client",
    displayProperty = "application.server"
)
@OATable(
    indexes = {
        @OAIndex(name = "AdminClientAdminServer", columns = { @OAIndexColumn(name = "AdminServerId") })
    }
)
public class AdminClient extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
     
     
    public static final String PROPERTY_AdminServer = "AdminServer";
    public static final String P_AdminServer = "AdminServer";
    public static final String PROPERTY_Application = "Application";
    public static final String P_Application = "Application";
     
    protected int id;
     
    // Links to other objects.
    protected transient AdminServer adminServer;
    protected transient Application application;
     
    public AdminClient() {
    }
     
    public AdminClient(int id) {
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
    @OAOne(
        displayName = "Admin Server", 
        reverseName = AdminServer.P_AdminClients, 
        required = true, 
        allowCreateNew = false
    )
    @OAFkey(columns = {"AdminServerId"})
    public AdminServer getAdminServer() {
        if (adminServer == null) {
            adminServer = (AdminServer) getObject(P_AdminServer);
        }
        return adminServer;
    }
    
    public void setAdminServer(AdminServer newValue) {
        fireBeforePropertyChange(P_AdminServer, this.adminServer, newValue);
        AdminServer old = this.adminServer;
        this.adminServer = newValue;
        firePropertyChange(P_AdminServer, old, this.adminServer);
    }
    
    @OAOne(
        reverseName = Application.P_AdminClient
    )
    @OAFkey(columns = {"ApplicationId"})
    public Application getApplication() {
        if (application == null) {
            application = (Application) getObject(P_Application);
        }
        return application;
    }
    
    public void setApplication(Application newValue) {
        fireBeforePropertyChange(P_Application, this.application, newValue);
        Application old = this.application;
        this.application = newValue;
        firePropertyChange(P_Application, old, this.application);
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        int adminServerFkey = rs.getInt(2);
        if (!rs.wasNull() && adminServerFkey > 0) {
            setProperty(P_AdminServer, new OAObjectKey(adminServerFkey));
        }
        int applicationFkey = rs.getInt(3);
        if (!rs.wasNull() && applicationFkey > 0) {
            setProperty(P_Application, new OAObjectKey(applicationFkey));
        }
        if (rs.getMetaData().getColumnCount() != 3) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
