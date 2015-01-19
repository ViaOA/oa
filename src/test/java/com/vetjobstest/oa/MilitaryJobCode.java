package com.vetjobstest.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "mjc",
    displayName = "Military Job Code",
    isLookup = true,
    isPreSelect = true
)
@OATable(
)
public class MilitaryJobCode extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_JobCode = "JobCode";
    public static final String P_Description = "Description";
     
     
    public static final String P_Categories = "Categories";
     
    protected int id;
    protected String jobCode;
    protected String description;
     
    // Links to other objects.
    protected transient Hub<Category> hubCategories;
     
     
    public MilitaryJobCode() {
    }
     
    public MilitaryJobCode(int id) {
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
    
     
    @OAProperty(displayName = "Job Code", maxLength = 35, displayLength = 7)
    @OAColumn(maxLength = 35)
    public String getJobCode() {
        return jobCode;
    }
    
    public void setJobCode(String newValue) {
        String old = jobCode;
        this.jobCode = newValue;
        firePropertyChange(P_JobCode, old, this.jobCode);
    }
    
     
    @OAProperty(maxLength = 70, displayLength = 11)
    @OAColumn(maxLength = 70)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.P_MilitaryJobCodes)
    @OALinkTable(name = "CategoryMilitaryJobCodeLink", indexName = "CategoryMilitaryJobCode", columns = {"MilitaryJobCodeId"})
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(P_Categories);
        }
        return hubCategories;
    }
    
     
}
 
