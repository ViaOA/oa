package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;
 
@OAClass(
    shortName = "ci",
    displayName = "Connection Info",
    isLookup = true,
    isPreSelect = true,
    useDataSource = false,
    displayProperty = "id",
    localOnly=true
)
public class ConnectionInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_DateTime = "DateTime";
    public static final String PROPERTY_HostName = "HostName";
    public static final String PROPERTY_IpAddress = "IpAddress";
    public static final String PROPERTY_Location = "Location";
    public static final String PROPERTY_UserId = "UserId";
    public static final String PROPERTY_UserName = "UserName";
    public static final String PROPERTY_CloseDateTime = "CloseDateTime";
    public static final String PROPERTY_TotalMemory = "TotalMemory";
    public static final String PROPERTY_FreeMemory = "FreeMemory";
    public static final String PROPERTY_Status = "Status";
     
     
    public static final String PROPERTY_ErrorInfos = "ErrorInfos";
     
    protected int id;
    protected OADateTime dateTime;
    protected String hostName;
    protected String ipAddress;
    protected String location;
    protected String userId;
    protected String userName;
    protected OADateTime closeDateTime;
    protected long totalMemory;
    protected long freeMemory;
    protected int status;
    public static final int STATUS_Connected = 0;
    public static final int STATUS_ClosedByUser = 1;
    public static final int STATUS_Disconected = 2;
    public static final Hub hubStatus;
    static {
        hubStatus = new Hub(String.class);
        hubStatus.addElement("Connected");
        hubStatus.addElement("Closed By User");
        hubStatus.addElement("Disconected");
    }
     
    // Links to other objects.
    protected transient Hub<ErrorInfo> hubErrorInfos;
     
     
    public ConnectionInfo() {
        if (!isLoading()) {
            setDateTime(new OADateTime());
        }
    }
     
    @OAId
    @OAProperty(displayName = "Number", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    @OAProperty(displayName = "Date Time", defaultValue = "new OADateTime()", displayLength = 15, isProcessed = true)
    @OAColumn(name = "DateTimeValue", sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDateTime() {
        return dateTime;
    }
    
    public void setDateTime(OADateTime newValue) {
        OADateTime old = dateTime;
        this.dateTime = newValue;
        firePropertyChange(PROPERTY_DateTime, old, this.dateTime);
    }
    
     
    @OAProperty(displayName = "Host Name", maxLength = 128, displayLength = 22, columnLength = 14)
    @OAColumn(maxLength = 128)
    public String getHostName() {
        return hostName;
    }
    
    public void setHostName(String newValue) {
        String old = hostName;
        this.hostName = newValue;
        firePropertyChange(PROPERTY_HostName, old, this.hostName);
    }
    
     
    @OAProperty(displayName = "Ip Address", maxLength = 9, displayLength = 16, columnLength = 12)
    @OAColumn(maxLength = 9)
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String newValue) {
        String old = ipAddress;
        this.ipAddress = newValue;
        firePropertyChange(PROPERTY_IpAddress, old, this.ipAddress);
    }
    
     
    @OAProperty(maxLength = 150, displayLength = 20, columnLength = 14)
    @OAColumn(maxLength = 150)
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String newValue) {
        String old = location;
        this.location = newValue;
        firePropertyChange(PROPERTY_Location, old, this.location);
    }
    
     
    @OAProperty(displayName = "User Id", maxLength = 20, displayLength = 20, columnLength = 10)
    @OAColumn(maxLength = 20)
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String newValue) {
        String old = userId;
        this.userId = newValue;
        firePropertyChange(PROPERTY_UserId, old, this.userId);
    }
    
     
    @OAProperty(displayName = "User Name", maxLength = 75, displayLength = 32, columnLength = 20)
    @OAColumn(maxLength = 75)
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String newValue) {
        String old = userName;
        this.userName = newValue;
        firePropertyChange(PROPERTY_UserName, old, this.userName);
    }
    
     
    @OAProperty(displayName = "Close Date Time", displayLength = 15, columnLength = 9, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getCloseDateTime() {
        return closeDateTime;
    }
    
    public void setCloseDateTime(OADateTime newValue) {
        OADateTime old = closeDateTime;
        this.closeDateTime = newValue;
        firePropertyChange(PROPERTY_CloseDateTime, old, this.closeDateTime);
    }
    
     
    @OAProperty(displayName = "Total Memory", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public void setTotalMemory(long newValue) {
        long old = totalMemory;
        this.totalMemory = newValue;
        firePropertyChange(PROPERTY_TotalMemory, old, this.totalMemory);
    }
    
     
    @OAProperty(displayName = "Free Memory", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public long getFreeMemory() {
        return freeMemory;
    }
    
    public void setFreeMemory(long newValue) {
        long old = freeMemory;
        this.freeMemory = newValue;
        firePropertyChange(PROPERTY_FreeMemory, old, this.freeMemory);
    }
    
     
    @OAProperty(displayLength = 18, columnLength = 12, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int newValue) {
        int old = status;
        this.status = newValue;
        firePropertyChange(PROPERTY_Status, old, this.status);
    }
    
     
    @OAMany(
        displayName = "Error Infos", 
        toClass = ErrorInfo.class, 
        owner = true, 
        reverseName = ErrorInfo.PROPERTY_ConnectionInfo, 
        cascadeSave = true, 
        cascadeDelete = true
    )
    public Hub<ErrorInfo> getErrorInfos() {
        if (hubErrorInfos == null) {
            hubErrorInfos = (Hub<ErrorInfo>) getHub(PROPERTY_ErrorInfos);
        }
        return hubErrorInfos;
    }
    
     
     
}
 
