package com.vetjobs.oa;
 
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
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Name = "Name";
     
     
    public static final String PROPERTY_Jobs = "Jobs";
    public static final String PROPERTY_MilitaryJobCodes = "MilitaryJobCodes";
    public static final String PROPERTY_VetUsers = "VetUsers";
    public static final String PROPERTY_BatchRows = "BatchRows";
    public static final String PROPERTY_Categories = "Categories";
    public static final String PROPERTY_ParentCategory = "ParentCategory";
     
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
        firePropertyChange(PROPERTY_Id, old, this.id);
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
    
     
    @OAMany(toClass = Job.class, reverseName = Job.PROPERTY_Categories, createMethod = false)
    @OALinkTable(name = "JobCategoryLink", indexName = "JobCategory", columns = {"CategoryId"})
    private Hub<Job> getJobs() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Military Job Codes", toClass = MilitaryJobCode.class, reverseName = MilitaryJobCode.PROPERTY_Categories)
    @OALinkTable(name = "CategoryMilitaryJobCodeLink", indexName = "MilitaryJobCodeCategory", columns = {"CategoryId"})
    public Hub<MilitaryJobCode> getMilitaryJobCodes() {
        if (hubMilitaryJobCodes == null) {
            hubMilitaryJobCodes = (Hub<MilitaryJobCode>) getHub(PROPERTY_MilitaryJobCodes);
        }
        return hubMilitaryJobCodes;
    }
    
     
    @OAMany(displayName = "Vet Users", toClass = VetUser.class, reverseName = VetUser.PROPERTY_Categories, createMethod = false)
    @OALinkTable(name = "VetUserCategoryLink", indexName = "VetUserCategory", columns = {"CategoryId"})
    private Hub<VetUser> getVetUsers() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, reverseName = BatchRow.PROPERTY_Categories, createMethod = false)
    @OALinkTable(name = "BatchRowCategoryLink", indexName = "BatchRowCategory", columns = {"CategoryId"})
    private Hub<BatchRow> getBatchRows() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.PROPERTY_ParentCategory)
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(PROPERTY_Categories);
        }
        return hubCategories;
    }
    
     
    @OAOne(displayName = "Parent Category", reverseName = Category.PROPERTY_Categories)
    @OAFkey(columns = {"ParentCategoryId"})
    public Category getParentCategory() {
        if (parentCategory == null) {
            parentCategory = (Category) getObject(PROPERTY_ParentCategory);
        }
        return parentCategory;
    }
    
    public void setParentCategory(Category newValue) {
        Category old = this.parentCategory;
        this.parentCategory = newValue;
        firePropertyChange(PROPERTY_ParentCategory, old, this.parentCategory);
    }
    
     
}
 
