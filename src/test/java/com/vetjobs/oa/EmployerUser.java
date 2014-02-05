package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "eu",
    displayName = "Employer User"
)
@OATable(
    indexes = {
        @OAIndex(name = "EmployerUserLoginId", columns = {@OAIndexColumn(name = "LoginId")}),
        @OAIndex(name = "EmployerUserEmployer", columns = { @OAIndexColumn(name = "EmployerId") })
    }
)
public class EmployerUser extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_LoginId = "LoginId";
    public static final String PROPERTY_Password = "Password";
    public static final String PROPERTY_Email = "Email";
    public static final String PROPERTY_Name = "Name";
    public static final String PROPERTY_Admin = "Admin";
    public static final String PROPERTY_Description = "Description";
     
     
    public static final String PROPERTY_Employer = "Employer";
    public static final String PROPERTY_EmpQueries = "EmpQueries";
     
    protected int id;
    protected String loginId;
    protected String password;
    protected String email;
    protected String name;
    protected boolean admin;
    protected String description;
     
    // Links to other objects.
    protected transient Employer employer;
    protected transient Hub<EmpQuery> hubEmpQueries;
     
     
    public EmployerUser() {
    }
     
    public EmployerUser(int id) {
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
    
     
    @OAProperty(displayName = "Login Id", maxLength = 20, displayLength = 5)
    @OAColumn(maxLength = 20)
    public String getLoginId() {
        return loginId;
    }
    
    public void setLoginId(String newValue) {
        String old = loginId;
        this.loginId = newValue;
        firePropertyChange(PROPERTY_LoginId, old, this.loginId);
    }
    
     
    @OAProperty(maxLength = 20, displayLength = 8)
    @OAColumn(maxLength = 20)
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String newValue) {
        String old = password;
        this.password = newValue;
        firePropertyChange(PROPERTY_Password, old, this.password);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 5)
    @OAColumn(maxLength = 75)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        String old = email;
        this.email = newValue;
        firePropertyChange(PROPERTY_Email, old, this.email);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 4)
    @OAColumn(maxLength = 75)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean newValue) {
        boolean old = admin;
        this.admin = newValue;
        firePropertyChange(PROPERTY_Admin, old, this.admin);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 11)
    @OAColumn(maxLength = 50)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    @OAOne(reverseName = Employer.PROPERTY_EmployerUsers, required = true)
    @OAFkey(columns = {"EmployerId"})
    public Employer getEmployer() {
        if (employer == null) {
            employer = (Employer) getObject(PROPERTY_Employer);
        }
        return employer;
    }
    
    public void setEmployer(Employer newValue) {
        Employer old = this.employer;
        this.employer = newValue;
        firePropertyChange(PROPERTY_Employer, old, this.employer);
    }
    
     
    @OAMany(displayName = "Emp Queries", toClass = EmpQuery.class, owner = true, reverseName = EmpQuery.PROPERTY_EmployerUser, cascadeSave = true, cascadeDelete = true)
    public Hub<EmpQuery> getEmpQueries() {
        if (hubEmpQueries == null) {
            hubEmpQueries = (Hub<EmpQuery>) getHub(PROPERTY_EmpQueries);
        }
        return hubEmpQueries;
    }
    
     
}
 
