// OABuilder generated source code
package com.viaoa.jfc.propertypath.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "cpd",
    displayName = "Calc Property Def",
    useDataSource = false,
    localOnly = true,
    addToCache = false
)
public class CalcPropertyDef extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_DisplayName = "DisplayName";
    public static final String PROPERTY_IsForHub = "IsForHub";
     
     
    public static final String PROPERTY_ObjectDef = "ObjectDef";
     
    protected String name;
    protected String displayName;
    /** 20131027
     *  true if this calcProp is for the whole Hub, and the method has a static method with a Hub param
     */
    protected boolean bIsForHub;
     
    // Links to other objects.
    protected transient ObjectDef objectDef;
     
     
    public CalcPropertyDef() {
    }
     
    @OAProperty(maxLength = 4, displayLength = 4)
    @OAColumn(maxLength = 4)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    @OAProperty(displayName = "Display Name", maxLength = 11, displayLength = 11)
    @OAColumn(maxLength = 11)
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String newValue) {
        String old = displayName;
        this.displayName = newValue;
        firePropertyChange(PROPERTY_DisplayName, old, this.displayName);
    }
    
     
    @OAOne(displayName = "Object Def", reverseName = ObjectDef.PROPERTY_CalcPropertyDefs)
    public ObjectDef getObjectDef() {
        if (objectDef == null) {
            objectDef = (ObjectDef) getObject(PROPERTY_ObjectDef);
        }
        return objectDef;
    }
    
    public void setObjectDef(ObjectDef newValue) {
        ObjectDef old = this.objectDef;
        this.objectDef = newValue;
        firePropertyChange(PROPERTY_ObjectDef, old, this.objectDef);
    }

    @OAProperty(displayName = "Is for Hub", displayLength = 10)
    public boolean getIsForHub() {
        return bIsForHub;
    }
    public void setIsForHub(boolean newValue) {
        boolean old = this.bIsForHub;
        this.bIsForHub = newValue;
        firePropertyChange(PROPERTY_IsForHub, old, this.bIsForHub);
    }
    
     
     
}
 
