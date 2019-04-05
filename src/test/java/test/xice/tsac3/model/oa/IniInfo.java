// Copied from OATemplate project by OABuilder 09/10/14 05:33 PM
package test.xice.tsac3.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "ii",
    displayName = "Ini Info",
    isLookup = true,
    isPreSelect = true,
    useDataSource = false,
    displayProperty = "name"
)
public class IniInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_ResourceValue = "ResourceValue";
    public static final String PROPERTY_IniValue = "IniValue";
     
     
    protected String name;
    protected String resourceValue;
    protected String iniValue;
     
     
    public IniInfo() {
    }
     
    @OAProperty(maxLength = 250, displayLength = 32, columnLength = 25)
    @OAColumn(maxLength = 250)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        fireBeforePropertyChange(PROPERTY_Name, old, newValue);
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    @OAProperty(displayName = "Resource Value", maxLength = 250, displayLength = 40, columnLength = 30)
    @OAColumn(maxLength = 250)
    public String getResourceValue() {
        return resourceValue;
    }
    
    public void setResourceValue(String newValue) {
        String old = resourceValue;
        fireBeforePropertyChange(PROPERTY_ResourceValue, old, newValue);
        this.resourceValue = newValue;
        firePropertyChange(PROPERTY_ResourceValue, old, this.resourceValue);
    }
    
     
    @OAProperty(displayName = "Ini Value", maxLength = 8, displayLength = 40, columnLength = 30)
    @OAColumn(maxLength = 8)
    public String getIniValue() {
        return iniValue;
    }
    
    public void setIniValue(String newValue) {
        String old = iniValue;
        fireBeforePropertyChange(PROPERTY_IniValue, old, newValue);
        this.iniValue = newValue;
        firePropertyChange(PROPERTY_IniValue, old, this.iniValue);
    }
    
     
     
}
 
