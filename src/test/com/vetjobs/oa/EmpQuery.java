package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "eq",
    displayName = "Emp Query"
)
@OATable(
    indexes = {
        @OAIndex(name = "EmpQueryEmployerUser", columns = { @OAIndexColumn(name = "EmployerUserId") })
    }
)
public class EmpQuery extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Date = "Date";
    public static final String PROPERTY_QueryText = "QueryText";
    public static final String PROPERTY_QueryDescription = "QueryDescription";
     
     
    public static final String PROPERTY_EmpQueryVets = "EmpQueryVets";
    public static final String PROPERTY_EmployerUser = "EmployerUser";
     
    protected int id;
    protected OADate date;
    protected String queryText;
    protected String queryDescription;
     
    // Links to other objects.
    protected transient Hub<EmpQueryVet> hubEmpQueryVets;
    protected transient EmployerUser employerUser;
     
     
    public EmpQuery() {
    }
     
    public EmpQuery(int id) {
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
    
     
    @OAProperty(displayLength = 10)
    @OAColumn(name = "DateValue", sqlType = java.sql.Types.DATE)
    public OADate getDate() {
        return date;
    }
    
    public void setDate(OADate newValue) {
        OADate old = date;
        this.date = newValue;
        firePropertyChange(PROPERTY_Date, old, this.date);
    }
    
     
    @OAProperty(displayName = "Query Text", maxLength = 254, displayLength = 9)
    @OAColumn(maxLength = 254)
    public String getQueryText() {
        return queryText;
    }
    
    public void setQueryText(String newValue) {
        String old = queryText;
        this.queryText = newValue;
        firePropertyChange(PROPERTY_QueryText, old, this.queryText);
    }
    
     
    @OAProperty(displayName = "Query Description", maxLength = 16, displayLength = 16)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getQueryDescription() {
        return queryDescription;
    }
    
    public void setQueryDescription(String newValue) {
        String old = queryDescription;
        this.queryDescription = newValue;
        firePropertyChange(PROPERTY_QueryDescription, old, this.queryDescription);
    }
    
     
    @OAMany(displayName = "Emp Query Vets", toClass = EmpQueryVet.class, owner = true, reverseName = EmpQueryVet.PROPERTY_EmpQuery, cascadeSave = true, cascadeDelete = true)
    public Hub<EmpQueryVet> getEmpQueryVets() {
        if (hubEmpQueryVets == null) {
            hubEmpQueryVets = (Hub<EmpQueryVet>) getHub(PROPERTY_EmpQueryVets);
        }
        return hubEmpQueryVets;
    }
    
     
    @OAOne(displayName = "Employer User", reverseName = EmployerUser.PROPERTY_EmpQueries, required = true)
    @OAFkey(columns = {"EmployerUserId"})
    public EmployerUser getEmployerUser() {
        if (employerUser == null) {
            employerUser = (EmployerUser) getObject(PROPERTY_EmployerUser);
        }
        return employerUser;
    }
    
    public void setEmployerUser(EmployerUser newValue) {
        EmployerUser old = this.employerUser;
        this.employerUser = newValue;
        firePropertyChange(PROPERTY_EmployerUser, old, this.employerUser);
    }
    
     
}
 
