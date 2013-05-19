// OABuilder generated source code
package com.viaoa.jfc.propertypath.model.oa;
 
import java.sql.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.annotation.*;
 
@OAClass(
    shortName = "od",
    displayName = "Object Def",
    useDataSource = false,
    localOnly = true,
    addToCache = false
)
public class ObjectDef extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_DisplayName = "DisplayName";
     
     
    public static final String PROPERTY_PropertyDefs = "PropertyDefs";
    public static final String PROPERTY_CalcPropertyDefs = "CalcPropertyDefs";
    public static final String PROPERTY_LinkPropertyDefs = "LinkPropertyDefs";
    public static final String PROPERTY_ToLinkPropertyDefs = "ToLinkPropertyDefs";
    public static final String PROPERTY_OneLinkPropertyDefs = "OneLinkPropertyDefs";
    public static final String PROPERTY_ManyLinkPropertyDefs = "ManyLinkPropertyDefs";
     
    protected String name;
    protected String displayName;
     
    // Links to other objects.
    protected transient Hub<PropertyDef> hubPropertyDefs;
    protected transient Hub<CalcPropertyDef> hubCalcPropertyDefs;
    protected transient Hub<LinkPropertyDef> hubLinkPropertyDefs;
    protected transient Hub<LinkPropertyDef> hubOneLinkPropertyDefs;
    protected transient Hub<LinkPropertyDef> hubManyLinkPropertyDefs;
     
     
    public ObjectDef() {
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
    
     
    @OAMany(
        displayName = "Property Defs", 
        reverseName = PropertyDef.PROPERTY_ObjectDef
    )
    public Hub<PropertyDef> getPropertyDefs() {
        if (hubPropertyDefs == null) {
            hubPropertyDefs = (Hub<PropertyDef>) getHub(PROPERTY_PropertyDefs);
        }
        return hubPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "Calc Property Defs", 
        reverseName = CalcPropertyDef.PROPERTY_ObjectDef
    )
    public Hub<CalcPropertyDef> getCalcPropertyDefs() {
        if (hubCalcPropertyDefs == null) {
            hubCalcPropertyDefs = (Hub<CalcPropertyDef>) getHub(PROPERTY_CalcPropertyDefs);
        }
        return hubCalcPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "Link Property Defs", 
        reverseName = LinkPropertyDef.PROPERTY_ObjectDef
    )
    public Hub<LinkPropertyDef> getLinkPropertyDefs() {
        if (hubLinkPropertyDefs == null) {
            hubLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(PROPERTY_LinkPropertyDefs);
        }
        return hubLinkPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "To Link Property Defs", 
        reverseName = LinkPropertyDef.PROPERTY_ToObjectDef, 
        createMethod = false
    )
    private Hub<LinkPropertyDef> getToLinkPropertyDefs() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(
        displayName = "One Link Property Defs", 
        isCalculated = true
    )
    public Hub<LinkPropertyDef> getOneLinkPropertyDefs() {
        if (hubOneLinkPropertyDefs == null) {
            hubOneLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(PROPERTY_OneLinkPropertyDefs);
            HubFilter hf = new HubFilter(getLinkPropertyDefs(), hubOneLinkPropertyDefs, LinkPropertyDef.PROPERTY_Type) {
                @Override
                public boolean isUsed(Object object) {
                    LinkPropertyDef lp = (LinkPropertyDef) object;
                    return lp.getType() == LinkPropertyDef.TYPE_One;
                }
            };
        }
        return hubOneLinkPropertyDefs;
    }
     
    @OAMany(
        displayName = "Many Link Property Defs", 
        isCalculated = true
    )
    public Hub<LinkPropertyDef> getManyLinkPropertyDefs() {
        if (hubManyLinkPropertyDefs == null) {
            hubManyLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(PROPERTY_ManyLinkPropertyDefs);
            HubFilter hf = new HubFilter(getLinkPropertyDefs(), hubManyLinkPropertyDefs, LinkPropertyDef.PROPERTY_Type) {
                @Override
                public boolean isUsed(Object object) {
                    LinkPropertyDef lp = (LinkPropertyDef) object;
                    return lp.getType() == LinkPropertyDef.TYPE_Many;
                }
            };
        }
        return hubManyLinkPropertyDefs;
    }
     
     
    private transient Class objectClass;
    public Class getObjectClass(){
        return objectClass;
    }
    public void setObjectClass(Class c) {
        this.objectClass = c;
    }
     
}
 
