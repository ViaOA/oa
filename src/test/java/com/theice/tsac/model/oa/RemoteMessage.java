// Generated by OABuilder
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.theice.tsac.model.oa.filter.*;
import com.theice.tsac.model.oa.propertypath.*;
import com.viaoa.util.OADateTime;
 
@OAClass(
    shortName = "rm",
    displayName = "Remote Message",
    displayProperty = "created"
)
@OATable(
    indexes = {
        @OAIndex(name = "RemoteMessageLladServer", columns = { @OAIndexColumn(name = "LLADServerId") }), 
        @OAIndex(name = "RemoteMessageRemoteClient", columns = { @OAIndexColumn(name = "RemoteClientId") })
    }
)
public class RemoteMessage extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String P_Id = "Id";
    public static final String PROPERTY_Created = "Created";
    public static final String P_Created = "Created";
    public static final String PROPERTY_Name = "Name";
    public static final String P_Name = "Name";
    public static final String PROPERTY_Message = "Message";
    public static final String P_Message = "Message";
    public static final String PROPERTY_Error = "Error";
    public static final String P_Error = "Error";
     
     
    public static final String PROPERTY_LLADServer = "LLADServer";
    public static final String P_LLADServer = "LLADServer";
    public static final String PROPERTY_RemoteClient = "RemoteClient";
    public static final String P_RemoteClient = "RemoteClient";
     
    protected int id;
    protected OADateTime created;
    protected String name;
    protected String message;
    protected String error;
     
    // Links to other objects.
     
    public RemoteMessage() {
        if (!isLoading()) {
            setCreated(new OADateTime());
        }
    }
     
    public RemoteMessage(int id) {
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
    @OAProperty(defaultValue = "new OADateTime()", displayLength = 15, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getCreated() {
        return created;
    }
    
    public void setCreated(OADateTime newValue) {
        fireBeforePropertyChange(P_Created, this.created, newValue);
        OADateTime old = created;
        this.created = newValue;
        firePropertyChange(P_Created, old, this.created);
    }
    @OAProperty(maxLength = 55, displayLength = 40, columnLength = 14, isProcessed = true)
    @OAColumn(maxLength = 55)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    @OAProperty(maxLength = 254, displayLength = 40, columnLength = 22, isProcessed = true)
    @OAColumn(maxLength = 254)
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String newValue) {
        fireBeforePropertyChange(P_Message, this.message, newValue);
        String old = message;
        this.message = newValue;
        firePropertyChange(P_Message, old, this.message);
    }
    @OAProperty(maxLength = 254, displayLength = 40, columnLength = 22, isProcessed = true)
    @OAColumn(maxLength = 254)
    public String getError() {
        return error;
    }
    
    public void setError(String newValue) {
        fireBeforePropertyChange(P_Error, this.error, newValue);
        String old = error;
        this.error = newValue;
        firePropertyChange(P_Error, old, this.error);
    }
    @OAOne(
        reverseName = LLADServer.P_RemoteMessages, 
        allowCreateNew = false, 
        allowAddExisting = false
    )
    @OALinkTable(name = "LLADServerRemoteMessage", indexName = "LLADServerRemoteMessage", columns = {"RemoteMessageId"})
    private LLADServer getLLADServer() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    @OAOne(
        displayName = "Remote Client", 
        reverseName = RemoteClient.P_RemoteMessages, 
        required = true, 
        allowCreateNew = false
    )
    @OALinkTable(name = "RemoteClientRemoteMessage", indexName = "RemoteClientRemoteMessage", columns = {"RemoteMessageId"})
    private RemoteClient getRemoteClient() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
    public void load(ResultSet rs, int id) throws SQLException {
        this.id = id;
        java.sql.Timestamp timestamp;
        timestamp = rs.getTimestamp(2);
        if (timestamp != null) this.created = new OADateTime(timestamp);
        this.name = rs.getString(3);
        this.message = rs.getString(4);
        this.error = rs.getString(5);
        if (rs.getMetaData().getColumnCount() != 5) {
            throw new SQLException("invalid number of columns for load method");
        }

        changedFlag = false;
        newFlag = false;
    }
}
 
