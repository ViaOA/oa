package test.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "eqv",
    displayName = "Emp Query Vet"
)
@OATable(
    indexes = {
        @OAIndex(name = "EmpQueryVetEmpQuery", columns = { @OAIndexColumn(name = "EmpQueryId") }), 
    }
)
public class EmpQueryVet extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_ViewType = "ViewType";
     
     
    public static final String P_VetUser = "VetUser";
    public static final String P_EmpQuery = "EmpQuery";
     
    protected int id;
    protected int viewType;
     
    // Links to other objects.
    protected transient VetUser vetUser;
    protected transient EmpQuery empQuery;
     
     
    public EmpQueryVet() {
    }
     
    public EmpQueryVet(int id) {
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
    
     
    @OAProperty(displayName = "View Type", displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewType() {
        return viewType;
    }
    
    public void setViewType(int newValue) {
        int old = viewType;
        this.viewType = newValue;
        firePropertyChange(P_ViewType, old, this.viewType);
    }
    
     
    @OAOne(displayName = "Vet User", reverseName = VetUser.P_EmpQueryVets)
    @OAFkey(columns = {"VetUserId"})
    public VetUser getVetUser() {
        if (vetUser == null) {
            vetUser = (VetUser) getObject(P_VetUser);
        }
        return vetUser;
    }
    
    public void setVetUser(VetUser newValue) {
        VetUser old = this.vetUser;
        this.vetUser = newValue;
        firePropertyChange(P_VetUser, old, this.vetUser);
    }
    
     
    @OAOne(displayName = "Emp Query", reverseName = EmpQuery.P_EmpQueryVets, required = true)
    @OAFkey(columns = {"EmpQueryId"})
    public EmpQuery getEmpQuery() {
        if (empQuery == null) {
            empQuery = (EmpQuery) getObject(P_EmpQuery);
        }
        return empQuery;
    }
    
    public void setEmpQuery(EmpQuery newValue) {
        EmpQuery old = this.empQuery;
        this.empQuery = newValue;
        firePropertyChange(P_EmpQuery, old, this.empQuery);
    }
    
     
}
 
