package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "vs",
    displayName = "Vet Security",
    isLookup = true,
    isPreSelect = true
)
@OATable(
    indexes = {
        @OAIndex(name = "VetSecurityParentVetSecurity", columns = { @OAIndexColumn(name = "ParentVetSecurityId") })
    }
)
public class VetSecurity extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
     
     
    public static final String PROPERTY_VetUsers = "VetUsers";
    public static final String PROPERTY_VetSecurities = "VetSecurities";
    public static final String PROPERTY_ParentVetSecurity = "ParentVetSecurity";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
    protected transient Hub<VetSecurity> hubVetSecurities;
    protected transient VetSecurity parentVetSecurity;
     
     
    public VetSecurity() {
    }
     
    public VetSecurity(int id) {
        this();
        setId(id);
    }
    @OAProperty(displayLength = 5)
    @OAId()
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getId() {
        return id;
    }
    
    public void setId(int newValue) {
        int old = id;
        this.id = newValue;
        firePropertyChange(PROPERTY_Id, old, this.id);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 4)
    @OAColumn(maxLength = 50)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.PROPERTY_VetSecurity, createMethod = false)
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Vet Securities", toClass = VetSecurity.class, reverseName = VetSecurity.PROPERTY_ParentVetSecurity)
    public Hub<VetSecurity> getVetSecurities() {
        if (hubVetSecurities == null) {
            hubVetSecurities = (Hub<VetSecurity>) getHub(PROPERTY_VetSecurities);
        }
        return hubVetSecurities;
    }
    
     
    @OAOne(displayName = "Parent Vet Security", reverseName = VetSecurity.PROPERTY_VetSecurities)
    @OAFkey(columns = {"ParentVetSecurityId"})
    public VetSecurity getParentVetSecurity() {
        if (parentVetSecurity == null) {
            parentVetSecurity = (VetSecurity) getObject(PROPERTY_ParentVetSecurity);
        }
        return parentVetSecurity;
    }
    
    public void setParentVetSecurity(VetSecurity newValue) {
        VetSecurity old = this.parentVetSecurity;
        this.parentVetSecurity = newValue;
        firePropertyChange(PROPERTY_ParentVetSecurity, old, this.parentVetSecurity);
    }
    
     
}
 
