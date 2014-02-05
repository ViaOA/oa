package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "job",
    displayName = "Job"
)
@OATable(
    indexes = {
        @OAIndex(name = "JobState", columns = {@OAIndexColumn(name = "State")}),
        @OAIndex(name = "JobRefreshDate", columns = {@OAIndexColumn(name = "RefreshDate")}),
        @OAIndex(name = "JobReference", columns = {@OAIndexColumn(name = "Reference")}),
        @OAIndex(name = "JobEmployer", columns = { @OAIndexColumn(name = "EmployerId") }), 
    }
)
public class Job extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Reference = "Reference";
    public static final String PROPERTY_CreateDate = "CreateDate";
    public static final String PROPERTY_RefreshDate = "RefreshDate";
    public static final String PROPERTY_RateFrom = "RateFrom";
    public static final String PROPERTY_RateTo = "RateTo";
    public static final String PROPERTY_Hourly = "Hourly";
    public static final String PROPERTY_Contract = "Contract";
    public static final String PROPERTY_Fulltime = "Fulltime";
    public static final String PROPERTY_Title = "Title";
    public static final String PROPERTY_Benefits = "Benefits";
    public static final String PROPERTY_Description = "Description";
    public static final String PROPERTY_City = "City";
    public static final String PROPERTY_Region = "Region";
    public static final String PROPERTY_State = "State";
    public static final String PROPERTY_Country = "Country";
    public static final String PROPERTY_Contact = "Contact";
    public static final String PROPERTY_Email = "Email";
    public static final String PROPERTY_AutoResponse = "AutoResponse";
    public static final String PROPERTY_PositionsAvailable = "PositionsAvailable";
    public static final String PROPERTY_ViewCount = "ViewCount";
    public static final String PROPERTY_SearchCount = "SearchCount";
    public static final String PROPERTY_ClickCount = "ClickCount";
    public static final String PROPERTY_ViewCountMTD = "ViewCountMTD";
    public static final String PROPERTY_SearchCountMTD = "SearchCountMTD";
    public static final String PROPERTY_ClickCountMTD = "ClickCountMTD";
    public static final String PROPERTY_ViewCountWTD = "ViewCountWTD";
    public static final String PROPERTY_SearchCountWTD = "SearchCountWTD";
    public static final String PROPERTY_ClickCountWTD = "ClickCountWTD";
    public static final String PROPERTY_LastMTD = "LastMTD";
    public static final String PROPERTY_LastWTD = "LastWTD";
     
     
    public static final String PROPERTY_Categories = "Categories";
    public static final String PROPERTY_Locations = "Locations";
    public static final String PROPERTY_Employer = "Employer";
    public static final String PROPERTY_Folder = "Folder";
    public static final String PROPERTY_BatchRows = "BatchRows";
     
    protected int id;
    protected String reference;
    protected OADate createDate;
    protected OADate refreshDate;
    protected float rateFrom;
    protected float rateTo;
    protected boolean hourly;
    protected boolean contract;
    protected boolean fulltime;
    protected String title;
    protected String benefits;
    protected String description;
    protected String city;
    protected String region;
    protected String state;
    protected String country;
    protected String contact;
    protected String email;
    protected boolean autoResponse;
    protected int positionsAvailable;
    protected int viewCount;
    protected int searchCount;
    protected int clickCount;
    protected int viewCountMTD;
    protected int searchCountMTD;
    protected int clickCountMTD;
    protected int viewCountWTD;
    protected int searchCountWTD;
    protected int clickCountWTD;
    protected OADate lastMTD;
    protected OADate lastWTD;
     
    // Links to other objects.
    protected transient Hub<Category> hubCategories;
    protected transient Hub<Location> hubLocations;
    protected transient Employer employer;
    protected transient Folder folder;
    protected transient Hub<BatchRow> hubBatchRows;
     
     
    public Job() {
    }
     
    public Job(int id) {
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
    
     
    @OAProperty(maxLength = 35, displayLength = 9)
    @OAColumn(maxLength = 35)
    public String getReference() {
        return reference;
    }
    
    public void setReference(String newValue) {
        String old = reference;
        this.reference = newValue;
        firePropertyChange(PROPERTY_Reference, old, this.reference);
    }
    
     
    @OAProperty(displayName = "Create Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(OADate newValue) {
        OADate old = createDate;
        this.createDate = newValue;
        firePropertyChange(PROPERTY_CreateDate, old, this.createDate);
    }
    
     
    @OAProperty(displayName = "Refresh Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getRefreshDate() {
        return refreshDate;
    }
    
    public void setRefreshDate(OADate newValue) {
        OADate old = refreshDate;
        this.refreshDate = newValue;
        firePropertyChange(PROPERTY_RefreshDate, old, this.refreshDate);
    }
    
     
    @OAProperty(displayName = "Rate From", decimalPlaces = 2, displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.FLOAT)
    public float getRateFrom() {
        return rateFrom;
    }
    
    public void setRateFrom(float newValue) {
        float old = rateFrom;
        this.rateFrom = newValue;
        firePropertyChange(PROPERTY_RateFrom, old, this.rateFrom);
    }
    
     
    @OAProperty(displayName = "Rate To", decimalPlaces = 2, displayLength = 6)
    @OAColumn(sqlType = java.sql.Types.FLOAT)
    public float getRateTo() {
        return rateTo;
    }
    
    public void setRateTo(float newValue) {
        float old = rateTo;
        this.rateTo = newValue;
        firePropertyChange(PROPERTY_RateTo, old, this.rateTo);
    }
    
     
    @OAProperty(displayLength = 6)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getHourly() {
        return hourly;
    }
    
    public void setHourly(boolean newValue) {
        boolean old = hourly;
        this.hourly = newValue;
        firePropertyChange(PROPERTY_Hourly, old, this.hourly);
    }
    
     
    @OAProperty(displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getContract() {
        return contract;
    }
    
    public void setContract(boolean newValue) {
        boolean old = contract;
        this.contract = newValue;
        firePropertyChange(PROPERTY_Contract, old, this.contract);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getFulltime() {
        return fulltime;
    }
    
    public void setFulltime(boolean newValue) {
        boolean old = fulltime;
        this.fulltime = newValue;
        firePropertyChange(PROPERTY_Fulltime, old, this.fulltime);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 5)
    @OAColumn(maxLength = 75)
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String newValue) {
        String old = title;
        this.title = newValue;
        firePropertyChange(PROPERTY_Title, old, this.title);
    }
    
     
    @OAProperty(maxLength = 8, displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String newValue) {
        String old = benefits;
        this.benefits = newValue;
        firePropertyChange(PROPERTY_Benefits, old, this.benefits);
    }
    
     
    @OAProperty(maxLength = 11, displayLength = 11)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(PROPERTY_Description, old, this.description);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 4)
    @OAColumn(maxLength = 50)
    public String getCity() {
        return city;
    }
    
    public void setCity(String newValue) {
        String old = city;
        this.city = newValue;
        firePropertyChange(PROPERTY_City, old, this.city);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 6)
    @OAColumn(maxLength = 50)
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String newValue) {
        String old = region;
        this.region = newValue;
        firePropertyChange(PROPERTY_Region, old, this.region);
    }
    
     
    @OAProperty(maxLength = 30, displayLength = 5)
    @OAColumn(maxLength = 30)
    public String getState() {
        return state;
    }
    
    public void setState(String newValue) {
        String old = state;
        this.state = newValue;
        firePropertyChange(PROPERTY_State, old, this.state);
    }
    
     
    @OAProperty(maxLength = 45, displayLength = 5)
    @OAColumn(maxLength = 45)
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String newValue) {
        String old = country;
        this.country = newValue;
        firePropertyChange(PROPERTY_Country, old, this.country);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 7)
    @OAColumn(maxLength = 75)
    public String getContact() {
        return contact;
    }
    
    public void setContact(String newValue) {
        String old = contact;
        this.contact = newValue;
        firePropertyChange(PROPERTY_Contact, old, this.contact);
    }
    
     
    @OAProperty(maxLength = 200, displayLength = 5)
    @OAColumn(maxLength = 200)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        String old = email;
        this.email = newValue;
        firePropertyChange(PROPERTY_Email, old, this.email);
    }
    
     
    @OAProperty(displayName = "Auto Response", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAutoResponse() {
        return autoResponse;
    }
    
    public void setAutoResponse(boolean newValue) {
        boolean old = autoResponse;
        this.autoResponse = newValue;
        firePropertyChange(PROPERTY_AutoResponse, old, this.autoResponse);
    }
    
     
    @OAProperty(displayName = "Positions Available", displayLength = 18)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getPositionsAvailable() {
        return positionsAvailable;
    }
    
    public void setPositionsAvailable(int newValue) {
        int old = positionsAvailable;
        this.positionsAvailable = newValue;
        firePropertyChange(PROPERTY_PositionsAvailable, old, this.positionsAvailable);
    }
    
     
    @OAProperty(displayName = "View Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(int newValue) {
        int old = viewCount;
        this.viewCount = newValue;
        firePropertyChange(PROPERTY_ViewCount, old, this.viewCount);
    }
    
     
    @OAProperty(displayName = "Search Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCount() {
        return searchCount;
    }
    
    public void setSearchCount(int newValue) {
        int old = searchCount;
        this.searchCount = newValue;
        firePropertyChange(PROPERTY_SearchCount, old, this.searchCount);
    }
    
     
    @OAProperty(displayName = "Click Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCount() {
        return clickCount;
    }
    
    public void setClickCount(int newValue) {
        int old = clickCount;
        this.clickCount = newValue;
        firePropertyChange(PROPERTY_ClickCount, old, this.clickCount);
    }
    
     
    @OAProperty(displayName = "View Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCountMTD() {
        return viewCountMTD;
    }
    
    public void setViewCountMTD(int newValue) {
        int old = viewCountMTD;
        this.viewCountMTD = newValue;
        firePropertyChange(PROPERTY_ViewCountMTD, old, this.viewCountMTD);
    }
    
     
    @OAProperty(displayName = "Search Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCountMTD() {
        return searchCountMTD;
    }
    
    public void setSearchCountMTD(int newValue) {
        int old = searchCountMTD;
        this.searchCountMTD = newValue;
        firePropertyChange(PROPERTY_SearchCountMTD, old, this.searchCountMTD);
    }
    
     
    @OAProperty(displayName = "Click Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCountMTD() {
        return clickCountMTD;
    }
    
    public void setClickCountMTD(int newValue) {
        int old = clickCountMTD;
        this.clickCountMTD = newValue;
        firePropertyChange(PROPERTY_ClickCountMTD, old, this.clickCountMTD);
    }
    
     
    @OAProperty(displayName = "View Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCountWTD() {
        return viewCountWTD;
    }
    
    public void setViewCountWTD(int newValue) {
        int old = viewCountWTD;
        this.viewCountWTD = newValue;
        firePropertyChange(PROPERTY_ViewCountWTD, old, this.viewCountWTD);
    }
    
     
    @OAProperty(displayName = "Search Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCountWTD() {
        return searchCountWTD;
    }
    
    public void setSearchCountWTD(int newValue) {
        int old = searchCountWTD;
        this.searchCountWTD = newValue;
        firePropertyChange(PROPERTY_SearchCountWTD, old, this.searchCountWTD);
    }
    
     
    @OAProperty(displayName = "Click Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCountWTD() {
        return clickCountWTD;
    }
    
    public void setClickCountWTD(int newValue) {
        int old = clickCountWTD;
        this.clickCountWTD = newValue;
        firePropertyChange(PROPERTY_ClickCountWTD, old, this.clickCountWTD);
    }
    
     
    @OAProperty(displayName = "Last MTD", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLastMTD() {
        return lastMTD;
    }
    
    public void setLastMTD(OADate newValue) {
        OADate old = lastMTD;
        this.lastMTD = newValue;
        firePropertyChange(PROPERTY_LastMTD, old, this.lastMTD);
    }
    
     
    @OAProperty(displayName = "Last WTD", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLastWTD() {
        return lastWTD;
    }
    
    public void setLastWTD(OADate newValue) {
        OADate old = lastWTD;
        this.lastWTD = newValue;
        firePropertyChange(PROPERTY_LastWTD, old, this.lastWTD);
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.PROPERTY_Jobs)
    @OALinkTable(name = "JobCategoryLink", indexName = "CategoryJob", columns = {"JobId"})
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(PROPERTY_Categories);
        }
        return hubCategories;
    }
    
     
    @OAMany(toClass = Location.class, reverseName = Location.PROPERTY_Jobs)
    @OALinkTable(name = "JobLocationLink", indexName = "LocationJob", columns = {"JobId"})
    public Hub<Location> getLocations() {
        if (hubLocations == null) {
            hubLocations = (Hub<Location>) getHub(PROPERTY_Locations);
        }
        return hubLocations;
    }
    
     
    @OAOne(reverseName = Employer.PROPERTY_Jobs, required = true)
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
    
     
    @OAOne(reverseName = Folder.PROPERTY_Jobs)
    @OAFkey(columns = {"FolderId"})
    public Folder getFolder() {
        if (folder == null) {
            folder = (Folder) getObject(PROPERTY_Folder);
        }
        return folder;
    }
    
    public void setFolder(Folder newValue) {
        Folder old = this.folder;
        this.folder = newValue;
        firePropertyChange(PROPERTY_Folder, old, this.folder);
    }
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, reverseName = BatchRow.PROPERTY_Job)
    public Hub<BatchRow> getBatchRows() {
        if (hubBatchRows == null) {
            hubBatchRows = (Hub<BatchRow>) getHub(PROPERTY_BatchRows);
        }
        return hubBatchRows;
    }
    
     
}
 
