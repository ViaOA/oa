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
    public static final String P_Name = "Name";
    public static final String P_DisplayName = "DisplayName";
     
     
    public static final String P_PropertyDefs = "PropertyDefs";
    public static final String P_CalcPropertyDefs = "CalcPropertyDefs";
    public static final String P_LinkPropertyDefs = "LinkPropertyDefs";
    public static final String P_ToLinkPropertyDefs = "ToLinkPropertyDefs";
    public static final String P_OneLinkPropertyDefs = "OneLinkPropertyDefs";
    public static final String P_ManyLinkPropertyDefs = "ManyLinkPropertyDefs";
     
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
    
     
    @OAMany(
        displayName = "Property Defs", 
        reverseName = PropertyDef.P_ObjectDef
    )
    public Hub<PropertyDef> getPropertyDefs() {
        if (hubPropertyDefs == null) {
            hubPropertyDefs = (Hub<PropertyDef>) getHub(P_PropertyDefs);
        }
        return hubPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "Calc Property Defs", 
        reverseName = CalcPropertyDef.P_ObjectDef
    )
    public Hub<CalcPropertyDef> getCalcPropertyDefs() {
        if (hubCalcPropertyDefs == null) {
            hubCalcPropertyDefs = (Hub<CalcPropertyDef>) getHub(P_CalcPropertyDefs);
        }
        return hubCalcPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "Link Property Defs", 
        reverseName = LinkPropertyDef.P_ObjectDef
    )
    public Hub<LinkPropertyDef> getLinkPropertyDefs() {
        if (hubLinkPropertyDefs == null) {
            hubLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(P_LinkPropertyDefs);
        }
        return hubLinkPropertyDefs;
    }
    
     
    @OAMany(
        displayName = "To Link Property Defs", 
        reverseName = LinkPropertyDef.P_ToObjectDef, 
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
            hubOneLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(P_OneLinkPropertyDefs);
            HubFilter hf = new HubFilter(getLinkPropertyDefs(), hubOneLinkPropertyDefs, LinkPropertyDef.P_Type) {
                @Override
                public boolean isUsed(OAObject object) {
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
            hubManyLinkPropertyDefs = (Hub<LinkPropertyDef>) getHub(P_ManyLinkPropertyDefs);
            HubFilter hf = new HubFilter(getLinkPropertyDefs(), hubManyLinkPropertyDefs, LinkPropertyDef.P_Type) {
                @Override
                public boolean isUsed(OAObject object) {
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
 
