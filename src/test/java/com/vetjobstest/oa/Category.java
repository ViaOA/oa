package com.vetjobstest.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "cat",
    displayName = "Category",
    isLookup = true,
    isPreSelect = true
)
@OATable(
    indexes = {
        @OAIndex(name = "CategoryParentCategory", columns = { @OAIndexColumn(name = "ParentCategoryId") })
    }
)
public class Category extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Name = "Name";
     
     
    public static final String P_Jobs = "Jobs";
    public static final String P_MilitaryJobCodes = "MilitaryJobCodes";
    public static final String P_VetUsers = "VetUsers";
    public static final String P_BatchRows = "BatchRows";
    public static final String P_Categories = "Categories";
    public static final String P_ParentCategory = "ParentCategory";
     
    protected int id;
    protected String name;
     
    // Links to other objects.
    protected transient Hub<MilitaryJobCode> hubMilitaryJobCodes;
    protected transient Hub<Category> hubCategories;
    protected transient Category parentCategory;
     
     
    public Category() {
    }
     
    public Category(int id) {
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
    
     
    @OAProperty(maxLength = 75, displayLength = 4)
    @OAColumn(maxLength = 75)
    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
    
     
    @OAMany(toClass = Job.class, reverseName = Job.P_Categories, createMethod = false)
    @OALinkTable(name = "JobCategoryLink", indexName = "JobCategory", columns = {"CategoryId"})
    private Hub<Job> getJobs() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Military Job Codes", toClass = MilitaryJobCode.class, reverseName = MilitaryJobCode.P_Categories)
    @OALinkTable(name = "CategoryMilitaryJobCodeLink", indexName = "MilitaryJobCodeCategory", columns = {"CategoryId"})
    public Hub<MilitaryJobCode> getMilitaryJobCodes() {
        if (hubMilitaryJobCodes == null) {
            hubMilitaryJobCodes = (Hub<MilitaryJobCode>) getHub(P_MilitaryJobCodes);
        }
        return hubMilitaryJobCodes;
    }
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.P_Categories, createMethod = false)
    @OALinkTable(name = "VetUserCategoryLink", indexName = "VetUserCategory", columns = {"CategoryId"})
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, reverseName = BatchRow.P_Categories, createMethod = false)
    @OALinkTable(name = "BatchRowCategoryLink", indexName = "BatchRowCategory", columns = {"CategoryId"})
    private Hub<BatchRow> getBatchRows() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.P_ParentCategory)
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(P_Categories);
        }
        return hubCategories;
    }
    
     
    @OAOne(displayName = "Parent Category", reverseName = Category.P_Categories)
    @OAFkey(columns = {"ParentCategoryId"})
    public Category getParentCategory() {
        if (parentCategory == null) {
            parentCategory = (Category) getObject(P_ParentCategory);
        }
        return parentCategory;
    }
    
    public void setParentCategory(Category newValue) {
        Category old = this.parentCategory;
        this.parentCategory = newValue;
        firePropertyChange(P_ParentCategory, old, this.parentCategory);
    }
    
     
}
 
