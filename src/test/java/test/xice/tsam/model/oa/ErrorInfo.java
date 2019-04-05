// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import test.xice.tsam.model.oa.ConnectionInfo;
import test.xice.tsam.model.oa.ExceptionType;
import com.viaoa.annotation.*;
import com.viaoa.util.OADateTime;
 
@OAClass(
    shortName = "ei",
    displayName = "Error Info",
    useDataSource = false,
    displayProperty = "dateTIme"
)
public class ErrorInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_DateTime = "DateTime";
    public static final String PROPERTY_Message = "Message";
    public static final String PROPERTY_StackTrace = "StackTrace";
     
     
    public static final String PROPERTY_ConnectionInfo = "ConnectionInfo";
    public static final String PROPERTY_ExceptionType = "ExceptionType";
     
    protected OADateTime dateTime;
    protected String message;
    protected String stackTrace;
     
    // Links to other objects.
    protected transient ConnectionInfo connectionInfo;
    protected transient ExceptionType exceptionType;
     
     
    public ErrorInfo() {
    }
     
    @OAProperty(displayName = "Date TIme", displayLength = 15)
    @OAColumn(name = "DateTimeValue", sqlType = java.sql.Types.TIMESTAMP)
    public OADateTime getDateTIme() {
        return dateTime;
    }
    
    public void setDateTIme(OADateTime newValue) {
        OADateTime old = dateTime;
        fireBeforePropertyChange(PROPERTY_DateTime, old, newValue);
        this.dateTime = newValue;
        firePropertyChange(PROPERTY_DateTime, old, this.dateTime);
    }
    
     
    @OAProperty(maxLength = 250, displayLength = 35, columnLength = 25)
    @OAColumn(maxLength = 250)
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String newValue) {
        String old = message;
        fireBeforePropertyChange(PROPERTY_Message, old, newValue);
        this.message = newValue;
        firePropertyChange(PROPERTY_Message, old, this.message);
    }
    
     
    @OAProperty(displayName = "Stack Trace", maxLength = 250, displayLength = 40, columnLength = 25)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String newValue) {
        String old = stackTrace;
        fireBeforePropertyChange(PROPERTY_StackTrace, old, newValue);
        this.stackTrace = newValue;
        firePropertyChange(PROPERTY_StackTrace, old, this.stackTrace);
    }
    
     
    @OAOne(displayName = "Connection Info", reverseName = ConnectionInfo.PROPERTY_ErrorInfos, required = true)
    public ConnectionInfo getConnectionInfo() {
        if (connectionInfo == null) {
            connectionInfo = (ConnectionInfo) getObject(PROPERTY_ConnectionInfo);
        }
        return connectionInfo;
    }
    
    public void setConnectionInfo(ConnectionInfo newValue) {
        ConnectionInfo old = this.connectionInfo;
        fireBeforePropertyChange(PROPERTY_ConnectionInfo, old, newValue);
        this.connectionInfo = newValue;
        firePropertyChange(PROPERTY_ConnectionInfo, old, this.connectionInfo);
    }
    
     
    @OAOne(displayName = "Exception Type", reverseName = ExceptionType.PROPERTY_ErrorInfos)
    public ExceptionType getExceptionType() {
        if (exceptionType == null) {
            exceptionType = (ExceptionType) getObject(PROPERTY_ExceptionType);
        }
        return exceptionType;
    }
    
    public void setExceptionType(ExceptionType newValue) {
        ExceptionType old = this.exceptionType;
        fireBeforePropertyChange(PROPERTY_ExceptionType, old, newValue);
        this.exceptionType = newValue;
        firePropertyChange(PROPERTY_ExceptionType, old, this.exceptionType);
    }
    
     
     
}
 
