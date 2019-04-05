// Copied from OATemplate project by OABuilder 01/27/15 08:54 AM
package test.xice.tsac2.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "et",
    displayName = "Exception Type",
    isLookup = true,
    isPreSelect = true,
    useDataSource = false,
    displayProperty = "exceptionClassName",
    sortProperty = "exceptionClassName"
)
public class ExceptionType extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_ExceptionClassName = "ExceptionClassName";
     
     
    public static final String PROPERTY_ErrorInfos = "ErrorInfos";
     
    protected String exceptionClassName;
     
    // Links to other objects.
    protected transient Hub<ErrorInfo> hubErrorInfos;
     
     
    public ExceptionType() {
    }
     
    @OAProperty(displayName = "Exception Class Name", maxLength = 55, displayLength = 40, columnLength = 25)
    @OAColumn(maxLength = 55)
    public String getExceptionClassName() {
        return exceptionClassName;
    }
    
    public void setExceptionClassName(String newValue) {
        String old = exceptionClassName;
        fireBeforePropertyChange(PROPERTY_ExceptionClassName, old, newValue);
        this.exceptionClassName = newValue;
        firePropertyChange(PROPERTY_ExceptionClassName, old, this.exceptionClassName);
    }
    
     
    @OAMany(
        displayName = "Error Infos", 
        toClass = ErrorInfo.class, 
        reverseName = ErrorInfo.PROPERTY_ExceptionType
    )
    public Hub<ErrorInfo> getErrorInfos() {
        if (hubErrorInfos == null) {
            hubErrorInfos = (Hub<ErrorInfo>) getHub(PROPERTY_ErrorInfos);
        }
        return hubErrorInfos;
    }
    
     
     
}
 
