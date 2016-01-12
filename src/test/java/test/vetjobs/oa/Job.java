package test.vetjobs.oa;
 
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
    public static final String P_Id = "Id";
    public static final String P_Reference = "Reference";
    public static final String P_CreateDate = "CreateDate";
    public static final String P_RefreshDate = "RefreshDate";
    public static final String P_RateFrom = "RateFrom";
    public static final String P_RateTo = "RateTo";
    public static final String P_Hourly = "Hourly";
    public static final String P_Contract = "Contract";
    public static final String P_Fulltime = "Fulltime";
    public static final String P_Title = "Title";
    public static final String P_Benefits = "Benefits";
    public static final String P_Description = "Description";
    public static final String P_City = "City";
    public static final String P_Region = "Region";
    public static final String P_State = "State";
    public static final String P_Country = "Country";
    public static final String P_Contact = "Contact";
    public static final String P_Email = "Email";
    public static final String P_AutoResponse = "AutoResponse";
    public static final String P_PositionsAvailable = "PositionsAvailable";
    public static final String P_ViewCount = "ViewCount";
    public static final String P_SearchCount = "SearchCount";
    public static final String P_ClickCount = "ClickCount";
    public static final String P_ViewCountMTD = "ViewCountMTD";
    public static final String P_SearchCountMTD = "SearchCountMTD";
    public static final String P_ClickCountMTD = "ClickCountMTD";
    public static final String P_ViewCountWTD = "ViewCountWTD";
    public static final String P_SearchCountWTD = "SearchCountWTD";
    public static final String P_ClickCountWTD = "ClickCountWTD";
    public static final String P_LastMTD = "LastMTD";
    public static final String P_LastWTD = "LastWTD";
     
     
    public static final String P_Categories = "Categories";
    public static final String P_Locations = "Locations";
    public static final String P_Employer = "Employer";
    public static final String P_Folder = "Folder";
    public static final String P_BatchRows = "BatchRows";
     
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
        firePropertyChange(P_Id, old, this.id);
    }
    
     
    @OAProperty(maxLength = 35, displayLength = 9)
    @OAColumn(maxLength = 35)
    public String getReference() {
        return reference;
    }
    
    public void setReference(String newValue) {
        String old = reference;
        this.reference = newValue;
        firePropertyChange(P_Reference, old, this.reference);
    }
    
     
    @OAProperty(displayName = "Create Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(OADate newValue) {
        OADate old = createDate;
        this.createDate = newValue;
        firePropertyChange(P_CreateDate, old, this.createDate);
    }
    
     
    @OAProperty(displayName = "Refresh Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getRefreshDate() {
        return refreshDate;
    }
    
    public void setRefreshDate(OADate newValue) {
        OADate old = refreshDate;
        this.refreshDate = newValue;
        firePropertyChange(P_RefreshDate, old, this.refreshDate);
    }
    
     
    @OAProperty(displayName = "Rate From", decimalPlaces = 2, displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.FLOAT)
    public float getRateFrom() {
        return rateFrom;
    }
    
    public void setRateFrom(float newValue) {
        float old = rateFrom;
        this.rateFrom = newValue;
        firePropertyChange(P_RateFrom, old, this.rateFrom);
    }
    
     
    @OAProperty(displayName = "Rate To", decimalPlaces = 2, displayLength = 6)
    @OAColumn(sqlType = java.sql.Types.FLOAT)
    public float getRateTo() {
        return rateTo;
    }
    
    public void setRateTo(float newValue) {
        float old = rateTo;
        this.rateTo = newValue;
        firePropertyChange(P_RateTo, old, this.rateTo);
    }
    
     
    @OAProperty(displayLength = 6)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getHourly() {
        return hourly;
    }
    
    public void setHourly(boolean newValue) {
        boolean old = hourly;
        this.hourly = newValue;
        firePropertyChange(P_Hourly, old, this.hourly);
    }
    
     
    @OAProperty(displayLength = 8)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getContract() {
        return contract;
    }
    
    public void setContract(boolean newValue) {
        boolean old = contract;
        this.contract = newValue;
        firePropertyChange(P_Contract, old, this.contract);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getFulltime() {
        return fulltime;
    }
    
    public void setFulltime(boolean newValue) {
        boolean old = fulltime;
        this.fulltime = newValue;
        firePropertyChange(P_Fulltime, old, this.fulltime);
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
    
     
    @OAProperty(maxLength = 50, displayLength = 4)
    @OAColumn(maxLength = 50)
    public String getCity() {
        return city;
    }
    
    public void setCity(String newValue) {
        String old = city;
        this.city = newValue;
        firePropertyChange(P_City, old, this.city);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 6)
    @OAColumn(maxLength = 50)
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String newValue) {
        String old = region;
        this.region = newValue;
        firePropertyChange(P_Region, old, this.region);
    }
    
     
    @OAProperty(maxLength = 30, displayLength = 5)
    @OAColumn(maxLength = 30)
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
    
     
    @OAProperty(maxLength = 75, displayLength = 7)
    @OAColumn(maxLength = 75)
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
    
     
    @OAProperty(displayName = "Auto Response", displayLength = 12)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getAutoResponse() {
        return autoResponse;
    }
    
    public void setAutoResponse(boolean newValue) {
        boolean old = autoResponse;
        this.autoResponse = newValue;
        firePropertyChange(P_AutoResponse, old, this.autoResponse);
    }
    
     
    @OAProperty(displayName = "Positions Available", displayLength = 18)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getPositionsAvailable() {
        return positionsAvailable;
    }
    
    public void setPositionsAvailable(int newValue) {
        int old = positionsAvailable;
        this.positionsAvailable = newValue;
        firePropertyChange(P_PositionsAvailable, old, this.positionsAvailable);
    }
    
     
    @OAProperty(displayName = "View Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(int newValue) {
        int old = viewCount;
        this.viewCount = newValue;
        firePropertyChange(P_ViewCount, old, this.viewCount);
    }
    
     
    @OAProperty(displayName = "Search Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCount() {
        return searchCount;
    }
    
    public void setSearchCount(int newValue) {
        int old = searchCount;
        this.searchCount = newValue;
        firePropertyChange(P_SearchCount, old, this.searchCount);
    }
    
     
    @OAProperty(displayName = "Click Count", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCount() {
        return clickCount;
    }
    
    public void setClickCount(int newValue) {
        int old = clickCount;
        this.clickCount = newValue;
        firePropertyChange(P_ClickCount, old, this.clickCount);
    }
    
     
    @OAProperty(displayName = "View Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCountMTD() {
        return viewCountMTD;
    }
    
    public void setViewCountMTD(int newValue) {
        int old = viewCountMTD;
        this.viewCountMTD = newValue;
        firePropertyChange(P_ViewCountMTD, old, this.viewCountMTD);
    }
    
     
    @OAProperty(displayName = "Search Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCountMTD() {
        return searchCountMTD;
    }
    
    public void setSearchCountMTD(int newValue) {
        int old = searchCountMTD;
        this.searchCountMTD = newValue;
        firePropertyChange(P_SearchCountMTD, old, this.searchCountMTD);
    }
    
     
    @OAProperty(displayName = "Click Count MTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCountMTD() {
        return clickCountMTD;
    }
    
    public void setClickCountMTD(int newValue) {
        int old = clickCountMTD;
        this.clickCountMTD = newValue;
        firePropertyChange(P_ClickCountMTD, old, this.clickCountMTD);
    }
    
     
    @OAProperty(displayName = "View Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getViewCountWTD() {
        return viewCountWTD;
    }
    
    public void setViewCountWTD(int newValue) {
        int old = viewCountWTD;
        this.viewCountWTD = newValue;
        firePropertyChange(P_ViewCountWTD, old, this.viewCountWTD);
    }
    
     
    @OAProperty(displayName = "Search Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getSearchCountWTD() {
        return searchCountWTD;
    }
    
    public void setSearchCountWTD(int newValue) {
        int old = searchCountWTD;
        this.searchCountWTD = newValue;
        firePropertyChange(P_SearchCountWTD, old, this.searchCountWTD);
    }
    
     
    @OAProperty(displayName = "Click Count WTD", displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getClickCountWTD() {
        return clickCountWTD;
    }
    
    public void setClickCountWTD(int newValue) {
        int old = clickCountWTD;
        this.clickCountWTD = newValue;
        firePropertyChange(P_ClickCountWTD, old, this.clickCountWTD);
    }
    
     
    @OAProperty(displayName = "Last MTD", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLastMTD() {
        return lastMTD;
    }
    
    public void setLastMTD(OADate newValue) {
        OADate old = lastMTD;
        this.lastMTD = newValue;
        firePropertyChange(P_LastMTD, old, this.lastMTD);
    }
    
     
    @OAProperty(displayName = "Last WTD", displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getLastWTD() {
        return lastWTD;
    }
    
    public void setLastWTD(OADate newValue) {
        OADate old = lastWTD;
        this.lastWTD = newValue;
        firePropertyChange(P_LastWTD, old, this.lastWTD);
    }
    
     
    @OAMany(toClass = Category.class, reverseName = Category.P_Jobs)
    @OALinkTable(name = "JobCategoryLink", indexName = "CategoryJob", columns = {"JobId"})
    public Hub<Category> getCategories() {
        if (hubCategories == null) {
            hubCategories = (Hub<Category>) getHub(P_Categories);
        }
        return hubCategories;
    }
    
     
    @OAMany(toClass = Location.class, reverseName = Location.P_Jobs)
    @OALinkTable(name = "JobLocationLink", indexName = "LocationJob", columns = {"JobId"})
    public Hub<Location> getLocations() {
        if (hubLocations == null) {
            hubLocations = (Hub<Location>) getHub(P_Locations);
        }
        return hubLocations;
    }
    
     
    @OAOne(reverseName = Employer.P_Jobs, required = true)
    @OAFkey(columns = {"EmployerId"})
    public Employer getEmployer() {
        if (employer == null) {
            employer = (Employer) getObject(P_Employer);
        }
        return employer;
    }
    
    public void setEmployer(Employer newValue) {
        Employer old = this.employer;
        this.employer = newValue;
        firePropertyChange(P_Employer, old, this.employer);
    }
    
     
    @OAOne(reverseName = Folder.P_Jobs)
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
    
     
    @OAMany(displayName = "Batch Rows", toClass = BatchRow.class, reverseName = BatchRow.P_Job)
    public Hub<BatchRow> getBatchRows() {
        if (hubBatchRows == null) {
            hubBatchRows = (Hub<BatchRow>) getHub(P_BatchRows);
        }
        return hubBatchRows;
    }
    
     
}
 
