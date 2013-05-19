package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "pri",
    displayName = "Privilege",
    isLookup = true,
    isPreSelect = true
)
@OATable(
)
public class Privilege extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Description = "Description";
     
     
    public static final String PROPERTY_Employers = "Employers";
     
    protected int id;
    protected String description;
     
    // Links to other objects.
    protected transient Hub<Employer> hubEmployers;
     
     
    public Privilege() {
    }
     
    public Privilege(int id) {
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
    
     
    @OAProperty(maxLength = 75, displayLength = 11)
    @OAColumn(maxLength = 75)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    @OAMany(toClass = Employer.class, reverseName = Employer.PROPERTY_Privileges)
    @OALinkTable(name = "EmployerPrivilegeLink", indexName = "EmployerPrivilege", columns = {"PrivilegeId"})
    public Hub<Employer> getEmployers() {
        if (hubEmployers == null) {
            hubEmployers = (Hub<Employer>) getHub(PROPERTY_Employers);
        }
        return hubEmployers;
    }
    
     
}
 
