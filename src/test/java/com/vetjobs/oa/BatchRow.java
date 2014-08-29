package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "br",
    displayName = "Batch Row"
)
@OATable(
    indexes = {
        @OAIndex(name = "BatchRowJob", columns = { @OAIndexColumn(name = "JobId") }), 
        @OAIndex(name = "BatchRowBatch", columns = { @OAIndexColumn(name = "BatchId") }), 
    }
)
public class BatchRow extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Reference = "Reference";
    public static final String P_RateFrom = "RateFrom";
    public static final String P_RateTo = "RateTo";
    public static final String P_Hourly = "Hourly";
    public static final String P_Benefits = "Benefits";
    public static final String P_City = "City";
    public static final String P_Region = "Region";
    public static final String P_State = "State";
    public static final String P_Country = "Country";
    public static final String P_Title = "Title";
    public static final String P_Description = "Description";
    public static final String P_Contact = "Contact";
    public static final String P_Email = "Email";
    public static final String P_Positions = "Positions";
    public static final String P_OrigCategory1 = "OrigCategory1";
    public static final String P_OrigCategory2 = "OrigCategory2";
    public static final String P_OrigCategory3 = "OrigCategory3";
    public static final String P_OrigCategory4 = "OrigCategory4";
    public static final String P_OrigCategory5 = "OrigCategory5";
    public static final String P_Contract = "Contract";
    public static final String P_ErrorFlag = "ErrorFlag";
    public static final String P_NewFlag = "NewFlag";
    public static final String P_Error = "Error";
    public static final String P_FullTime = "FullTime";
     
     
    public static final String P_Categories = "Categories";
    public static final String P_Job = "Job";
    public static final String P_Batch = "Batch";
    public static final String P_Location = "Location";
    public static final String P_Folder = "Folder";
     
    protected int id;
    protected String reference;
    protected String rateFrom;
    protected String rateTo;
    protected String hourly;
    protected String benefits;
    protected String city;
    protected String region;
    protected String state;
    protected String country;
    protected String title;
    protected String description;
    protected String contact;
    protected String email;
    protected String positions;
    protected String origCategory1;
    protected String origCategory2;
    protected String origCategory3;
    protected String origCategory4;
    protected String origCategory5;
    protected String contract;
    protected boolean errorFlag;
    protected boolean newFlag;
    protected String error;
    protected String fullTime;
     
    // Links to other objects.
    protected transient Hub<Category> hubCategories;
    protected transient Job job;
    protected transient Batch batch;
    protected transient Location location;
    protected transient Folder folder;
     
     
    public BatchRow() {
    }
     
    public BatchRow(int id) {
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
    
     
    @OAProperty(maxLength = 25, displayLength = 9)
    @OAColumn(maxLength = 25)
    public String getReference() {
        return reference;
    }
    
    public void setReference(String newValue) {
        String old = reference;
        this.reference = newValue;
        firePropertyChange(P_Reference, old, this.reference);
    }
    
     
    @OAProperty(displayName = "Rate From", maxLength = 15, displayLength = 8)
    @OAColumn(maxLength = 15)
    public String getRateFrom() {
        return rateFrom;
    }
    
    public void setRateFrom(String newValue) {
        String old = rateFrom;
        this.rateFrom = newValue;
        firePropertyChange(P_RateFrom, old, this.rateFrom);
    }
    
     
    @OAProperty(displayName = "Rate To", maxLength = 15, displayLength = 6)
    @OAColumn(maxLength = 15)
    public String getRateTo() {
        return rateTo;
    }
    
    public void setRateTo(String newValue) {
        String old = rateTo;
        this.rateTo = newValue;
        firePropertyChange(P_RateTo, old, this.rateTo);
    }
    
     
    @OAProperty(maxLength = 15, displayLength = 6)
    @OAColumn(maxLength = 15)
    public String getHourly() {
        return hourly;
    }
    
    public void setHourly(String newValue) {
        String old = hourly;
        this.hourly = newValue;
        firePropertyChange(P_Hourly, old, this.hourly);
    }
    
     
    @OAProperty(maxLength = 8, displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String newValue) {
        String old = benefits;
        this.benefits = newValue;
        firePropertyChange(P_Benefits, old, this.benefits);
    }
    
     
    @OAProperty(maxLength = 30, displayLength = 4)
    @OAColumn(maxLength = 30)
    public String getCity() {
        return city;
    }
    
    public void setCity(String newValue) {
        String old = city;
        this.city = newValue;
        firePropertyChange(P_City, old, this.city);
    }
    
     
    @OAProperty(displayName = "State", maxLength = 30, displayLength = 5)
    @OAColumn(maxLength = 30)
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String newValue) {
        String old = region;
        this.region = newValue;
        firePropertyChange(P_Region, old, this.region);
    }
    
     
    @OAProperty(displayName = "Region", maxLength = 25, displayLength = 6)
    @OAColumn(maxLength = 25)
    public String getState() {
        return state;
    }
    
    public void setState(String newValue) {
        String old = state;
        this.state = newValue;
        firePropertyChange(P_State, old, this.state);
    }
    
     
    @OAProperty(maxLength = 45, displayLength = 5)
    @OAColumn(maxLength = 45)
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String newValue) {
        String old = country;
        this.country = newValue;
        firePropertyChange(P_Country, old, this.country);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 5)
    @OAColumn(maxLength = 75)
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String newValue) {
        String old = title;
        this.title = newValue;
        firePropertyChange(P_Title, old, this.title);
    }
    
     
    @OAProperty(maxLength = 11, displayLength = 11)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 7)
    @OAColumn(maxLength = 50)
    public String getContact() {
        return contact;
    }
    
    public void setContact(String newValue) {
        String old = contact;
        this.contact = newValue;
        firePropertyChange(P_Contact, old, this.contact);
    }
    
     
    @OAProperty(maxLength = 200, displayLength = 5)
    @OAColumn(maxLength = 200)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        String old = email;
        this.email = newValue;
        firePropertyChange(P_Email, old, this.email);
    }
    
     
    @OAProperty(maxLength = 15, displayLength = 9)
    @OAColumn(maxLength = 15)
    public String getPositions() {
        return positions;
    }
    
    public void setPositions(String newValue) {
        String old = positions;
        this.positions = newValue;
        firePropertyChange(P_Positions, old, this.positions);
    }
    
     
    @OAProperty(displayName = "Orig Category1", maxLength = 100, displayLength = 13)
    @OAColumn(maxLength = 100)
    public String getOrigCategory1() {
        return origCategory1;
    }
    
    public void setOrigCategory1(String newValue) {
        String old = origCategory1;
        this.origCategory1 = newValue;
        firePropertyChange(P_OrigCategory1, old, this.origCategory1);
    }
    
     
    @OAProperty(displayName = "Orig Category2", maxLength = 100, displayLength = 13)
    @OAColumn(maxLength = 100)
    public String getOrigCategory2() {
        return origCategory2;
    }
    
    public void setOrigCategory2(String newValue) {
        String old = origCategory2;
        this.origCategory2 = newValue;
        firePropertyChange(P_OrigCategory2, old, this.origCategory2);
    }
    
     
    @OAProperty(displayName = "Orig Category3", maxLength = 100, displayLength = 13)
    @OAColumn(maxLength = 100)
    public String getOrigCategory3() {
        return origCategory3;
    }
    
    public void setOrigCategory3(String newValue) {
        String old = origCategory3;
        this.origCategory3 = newValue;
        firePropertyChange(P_OrigCategory3, old, this.origCategory3);
    }
    
     
    @OAProperty(displayName = "Orig Category4", maxLength = 100, displayLength = 13)
    @OAColumn(maxLength = 100)
    public String getOrigCategory4() {
        return origCategory4;
    }
    
    public void setOrigCategory4(String newValue) {
        String old = origCategory4;
        this.origCategory4 = newValue;
        firePropertyChange(P_OrigCategory4, old, this.origCategory4);
    }
    
     
    @OAProperty(displayName = "Orig Category5", maxLength = 100, displayLength = 13)
    @OAColumn(maxLength = 100)
    public String getOrigCategory5() {
        return origCategory5;
    }
    
    public void setOrigCategory5(String newValue) {
        String old = origCategory5;
        this.origCategory5 = newValue;
        firePropertyChange(P_OrigCategory5, old, this.origCategory5);
    }
    
     
    @OAProperty(maxLength = 30, displayLength = 8)
    @OAColumn(maxLength = 30)
    public String getContract() {
        return contract;
    }
    
    public void setContract(String newValue) {
        String old = contract;
        this.contract = newValue;
        firePropertyChange(P_Contract, old, this.contract);
    }
    
     
    @OAProperty(displayName = "Error Flag", displayLength = 9)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getErrorFlag() {
        return errorFlag;
    }
    
    public void setErrorFlag(boolean newValue) {
        boolean old = errorFlag;
        this.errorFlag = newValue;
        firePropertyChange(P_ErrorFlag, old, this.errorFlag);
    }
    
     
    @OAProperty(displayName = "New Flag", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getNewFlag() {
        return newFlag;
    }
    
    public void setNewFlag(boolean newValue) {
        boolean old = newFlag;
        this.newFlag = newValue;
        firePropertyChange(P_NewFlag, old, this.newFlag);
    }
    
     
    @OAProperty(maxLength = 200, displayLength = 5)
    @OAColumn(maxLength = 200)
    public String getError() {
        return error;
    }
    
    public void setError(String newValue) {
        String old = error;
        this.error = newValue;
        firePropertyChange(P_Error, old, this.error);
    }
    
     
    @OAProperty(displayName = "Full Time", maxLength = 30, displayLength = 5)
    @OAColumn(maxLength = 30)
    public String getFullTime() {
        return fullTime;
    }
    
    public void setFullTime(String newValue) {
        String old = fullTime;
        this.fullTime = newValue;
        firePropertyChange(P_FullTime, old, this.fullTime);
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.P_BatchRows)
    @OALinkTable(name = "BatchRowCategoryLink", indexName = "CategoryBatchRow", columns = {"BatchRowId"})
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(P_Categories);
        }
        return hubCategories;
    }
    
     
    @OAOne(reverseName = Job.P_BatchRows)
    @OAFkey(columns = {"JobId"})
    public Job getJob() {
        if (job == null) {
            job = (Job) getObject(P_Job);
        }
        return job;
    }
    
    public void setJob(Job newValue) {
        Job old = this.job;
        this.job = newValue;
        firePropertyChange(P_Job, old, this.job);
    }
    
     
    @OAOne(reverseName = Batch.P_BatchRows, required = true)
    @OAFkey(columns = {"BatchId"})
    public Batch getBatch() {
        if (batch == null) {
            batch = (Batch) getObject(P_Batch);
        }
        return batch;
    }
    
    public void setBatch(Batch newValue) {
        Batch old = this.batch;
        this.batch = newValue;
        firePropertyChange(P_Batch, old, this.batch);
    }
    
     
    @OAOne(reverseName = Location.P_BatchRows)
    @OAFkey(columns = {"LocationId"})
    public Location getLocation() {
        if (location == null) {
            location = (Location) getObject(P_Location);
        }
        return location;
    }
    
    public void setLocation(Location newValue) {
        Location old = this.location;
        this.location = newValue;
        firePropertyChange(P_Location, old, this.location);
    }
    
     
    @OAOne(reverseName = Folder.P_BatchRows)
    @OAFkey(columns = {"FolderId"})
    public Folder getFolder() {
        if (folder == null) {
            folder = (Folder) getObject(P_Folder);
        }
        return folder;
    }
    
    public void setFolder(Folder newValue) {
        Folder old = this.folder;
        this.folder = newValue;
        firePropertyChange(P_Folder, old, this.folder);
    }
    
     
}
 
