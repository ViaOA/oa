// OABuilder generated source code
package com.viaoa.jfc.propertypath.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "lpd",
    displayName = "Link Property Def",
    useDataSource = false,
    localOnly = true,
    addToCache = false
)
public class LinkPropertyDef extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Name = "Name";
    public static final String P_DisplayName = "DisplayName";
    public static final String P_Type = "Type";
     
     
    public static final String P_ObjectDef = "ObjectDef";  // from objectDef
    public static final String P_ToObjectDef = "ToObjectDef";
     
    protected String name;
    protected String displayName;
    protected int type;
    public static final int TYPE_One = 0;
    public static final int TYPE_Many = 1;
    public static final Hub<String> hubType;
    static {
        hubType = new Hub<String>(String.class);
        hubType.addElement("One");
        hubType.addElement("Many");
    }
     
    // Links to other objects.
    protected transient ObjectDef objectDef;
    protected transient ObjectDef toObjectDef;
     
     
    public LinkPropertyDef() {
    }
     
    @OAProperty(maxLength = 4, displayLength = 4)
    @OAColumn(maxLength = 4)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    
     
    @OAProperty(displayName = "Display Name", maxLength = 11, displayLength = 11)
    @OAColumn(maxLength = 11)
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String newValue) {
        String old = displayName;
        this.displayName = newValue;
        firePropertyChange(P_DisplayName, old, this.displayName);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getType() {
        return type;
    }
    
    public void setType(int newValue) {
        int old = type;
        this.type = newValue;
        firePropertyChange(P_Type, old, this.type);
    }
    
     
    @OAOne(displayName = "Object Def", reverseName = ObjectDef.P_LinkPropertyDefs)
    public ObjectDef getObjectDef() {
        if (objectDef == null) {
            objectDef = (ObjectDef) getObject(P_ObjectDef);
        }
        return objectDef;
    }
    
    public void setObjectDef(ObjectDef newValue) {
        ObjectDef old = this.objectDef;
        this.objectDef = newValue;
        firePropertyChange(P_ObjectDef, old, this.objectDef);
    }
    
     
    @OAOne(displayName = "To Object Def", reverseName = ObjectDef.P_ToLinkPropertyDefs)
    public ObjectDef getToObjectDef() {
        if (toObjectDef == null) {
            toObjectDef = (ObjectDef) getObject(P_ToObjectDef);
        }
        return toObjectDef;
    }
    
    public void setToObjectDef(ObjectDef newValue) {
        ObjectDef old = this.toObjectDef;
        this.toObjectDef = newValue;
        firePropertyChange(P_ToObjectDef, old, this.toObjectDef);
    }
    
     
     
}
 
