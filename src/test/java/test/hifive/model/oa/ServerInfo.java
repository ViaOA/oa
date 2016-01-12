package test.hifive.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;
 
@OAClass(
    shortName = "si",
    displayName = "Server Info",
    isLookup = true,
    isPreSelect = true,
    useDataSource = false,
    displayProperty = "dateTime"
)
public class ServerInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_DateTime = "DateTime";
    public static final String PROPERTY_SendMessage = "SendMessage";
    public static final String PROPERTY_SendMessageDateTime = "SendMessageDateTime";
    public static final String PROPERTY_Status = "Status";
     
     
    protected OADateTime dateTime;
    protected String sendMessage;
    protected OADateTime sendMessageDateTime;
    protected int status;
    public static final int STATUS_Starting = 0;
    public static final int STATUS_Running = 1;
    public static final int STATUS_Stopping = 2;
    public static final int STATUS_Stopped = 3;
    public static final Hub hubStatus;
    static {
        hubStatus = new Hub(String.class);
        hubStatus.addElement("Starting");
        hubStatus.addElement("Running");
        hubStatus.addElement("Stopping");
        hubStatus.addElement("Stopped");
    }
     
     
    public ServerInfo() {
        if (!isLoading()) {
            setDateTime(new OADateTime());
        }
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
    
     
    @OAProperty(displayName = "Send Message", maxLength = 250, displayLength = 40, columnLength = 25, hasCustomCode = true)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getSendMessage() {
        return sendMessage;
    }
    
    public void setSendMessage(String newValue) {
        String old = sendMessage;
        this.sendMessage = newValue;
        firePropertyChange(PROPERTY_SendMessage, old, this.sendMessage);
    }
    
     
    @OAProperty(displayName = "Send Message Date Time", displayLength = 15)
    @OAColumn(sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getSendMessageDateTime() {
        return sendMessageDateTime;
    }
    
    public void setSendMessageDateTime(OADateTime newValue) {
        OADateTime old = sendMessageDateTime;
        this.sendMessageDateTime = newValue;
        firePropertyChange(PROPERTY_SendMessageDateTime, old, this.sendMessageDateTime);
    }
    
     
    @OAProperty(displayLength = 14, isProcessed = true)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int newValue) {
        int old = status;
        this.status = newValue;
        firePropertyChange(PROPERTY_Status, old, this.status);
    }
    
     
     
    // custom method
    // this will set the dateTime, which the clients will use to know to display the message.
    public void setMessageDateTime() {
        setSendMessageDateTime(new OADateTime());
    }
    
     
}
 
