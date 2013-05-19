package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "ser",
    displayName = "Service",
    isLookup = true,
    isPreSelect = true
)
@OATable(
)
public class Service extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
     
     
    public static final String PROPERTY_VetUsers = "VetUsers";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
     
     
    public Service() {
    }
     
    public Service(int id) {
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
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.PROPERTY_Service, createMethod = false)
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
}
 
