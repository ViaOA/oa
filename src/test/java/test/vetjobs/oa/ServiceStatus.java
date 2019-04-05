package test.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "ss",
    displayName = "Service Status",
    isLookup = true,
    isPreSelect = true
)
@OATable(
)
public class ServiceStatus extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Name = "Name";
     
     
    public static final String P_VetUsers = "VetUsers";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
     
     
    public ServiceStatus() {
    }
     
    public ServiceStatus(int id) {
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
        firePropertyChange(P_Id, old, this.id);
    }
    
     
    @OAProperty(displayName = "Nam", maxLength = 50, displayLength = 3)
    @OAColumn(maxLength = 50)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.P_ServiceStatus, createMethod = false)
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
}
 
