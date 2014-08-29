package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "res",
    displayName = "Resume"
)
@OATable(
    indexes = {
        @OAIndex(name = "ResumeRefreshDate", columns = {@OAIndexColumn(name = "RefreshDate")})
    }
)
public class Resume extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_Title = "Title";
    public static final String P_CreateDate = "CreateDate";
    public static final String P_Resume = "Resume";
    public static final String P_RefreshDate = "RefreshDate";
    public static final String P_Summary = "Summary";
    public static final String P_Hold = "Hold";
    public static final String P_ViewCount = "ViewCount";
    public static final String P_SearchCount = "SearchCount";
    public static final String P_ClickCount = "ClickCount";
     
     
    public static final String P_VetUser = "VetUser";
     
    protected int id;
    protected String title;
    protected OADate createDate;
    protected String resume;
    protected OADate refreshDate;
    protected String summary;
    protected boolean hold;
    protected int viewCount;
    protected int searchCount;
    protected int clickCount;
     
    // Links to other objects.
    protected transient VetUser vetUser;
     
     
    public Resume() {
    }
     
    public Resume(int id) {
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
    
     
    @OAProperty(maxLength = 255, displayLength = 5)
    @OAColumn(maxLength = 255)
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String newValue) {
        String old = title;
        this.title = newValue;
        firePropertyChange(P_Title, old, this.title);
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
    
     
    @OAProperty(maxLength = 6, displayLength = 6)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getResume() {
        return resume;
    }
    
    public void setResume(String newValue) {
        String old = resume;
        this.resume = newValue;
        firePropertyChange(P_Resume, old, this.resume);
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
    
     
    @OAProperty(maxLength = 7, displayLength = 7)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String newValue) {
        String old = summary;
        this.summary = newValue;
        firePropertyChange(P_Summary, old, this.summary);
    }
    
     
    @OAProperty(displayLength = 4)
    @OAColumn(sqlType = java.sql.Types.BOOLEAN)
    public boolean getHold() {
        return hold;
    }
    
    public void setHold(boolean newValue) {
        boolean old = hold;
        this.hold = newValue;
        firePropertyChange(P_Hold, old, this.hold);
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
    
     
    @OAOne(displayName = "Vet User", reverseName = VetUser.P_Resume)
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
    
     
}
 
