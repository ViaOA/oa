// Copied from OATemplate project by OABuilder 09/10/14 05:33 PM
package com.theice.tsac.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "si",
    displayName = "System Info",
    isLookup = true,
    isPreSelect = true,
    useDataSource = false,
    displayProperty = "name",
    localOnly = true
)
public class SystemInfo extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Value = "Value";
     
     
    protected String name;
    protected String value;
     
     
    public SystemInfo() {
    }
     
    @OAProperty(maxLength = 250, displayLength = 40, columnLength = 25)
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
    
     
    @OAProperty(maxLength = 250, displayLength = 40, columnLength = 32)
    @OAColumn(maxLength = 250)
    public String getValue() {
        return value;
    }
    
    public void setValue(String newValue) {
        String old = value;
        fireBeforePropertyChange(PROPERTY_Value, old, newValue);
        this.value = newValue;
        firePropertyChange(PROPERTY_Value, old, this.value);
    }
    
     
     
}
 
