package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "ban",
    displayName = "Banner"
)
@OATable(
    indexes = {
        @OAIndex(name = "BannerBeginDate", columns = {@OAIndexColumn(name = "BeginDate")}),
        @OAIndex(name = "BannerCustomer", columns = { @OAIndexColumn(name = "CustomerId") })
    }
)
public class Banner extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String P_Id = "Id";
    public static final String P_CreateDate = "CreateDate";
    public static final String P_BeginDate = "BeginDate";
    public static final String P_EndDate = "EndDate";
    public static final String P_ImageUrl = "ImageUrl";
    public static final String P_ForwardUrl = "ForwardUrl";
    public static final String P_AltTag = "AltTag";
    public static final String P_Group = "Group";
    public static final String P_Description = "Description";
     
     
    public static final String P_BannerStats = "BannerStats";
    public static final String P_Customer = "Customer";
     
    protected int id;
    protected OADate createDate;
    protected OADate beginDate;
    protected OADate endDate;
    protected String imageUrl;
    protected String forwardUrl;
    protected String altTag;
    protected int group;
    protected String description;
     
    // Links to other objects.
    protected transient Customer customer;
     
     
    public Banner() {
    }
     
    public Banner(int id) {
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
    
     
    @OAProperty(displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getBeginDate() {
        return beginDate;
    }
    
    public void setBeginDate(OADate newValue) {
        OADate old = beginDate;
        this.beginDate = newValue;
        firePropertyChange(P_BeginDate, old, this.beginDate);
    }
    
     
    @OAProperty(displayName = "End Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(OADate newValue) {
        OADate old = endDate;
        this.endDate = newValue;
        firePropertyChange(P_EndDate, old, this.endDate);
    }
    
     
    @OAProperty(displayName = "Image Url", maxLength = 200, displayLength = 8)
    @OAColumn(maxLength = 200)
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String newValue) {
        String old = imageUrl;
        this.imageUrl = newValue;
        firePropertyChange(P_ImageUrl, old, this.imageUrl);
    }
    
     
    @OAProperty(displayName = "Forward Url", maxLength = 200, displayLength = 10)
    @OAColumn(maxLength = 200)
    public String getForwardUrl() {
        return forwardUrl;
    }
    
    public void setForwardUrl(String newValue) {
        String old = forwardUrl;
        this.forwardUrl = newValue;
        firePropertyChange(P_ForwardUrl, old, this.forwardUrl);
    }
    
     
    @OAProperty(displayName = "Alt Tag", maxLength = 75, displayLength = 6)
    @OAColumn(maxLength = 75)
    public String getAltTag() {
        return altTag;
    }
    
    public void setAltTag(String newValue) {
        String old = altTag;
        this.altTag = newValue;
        firePropertyChange(P_AltTag, old, this.altTag);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getGroup() {
        return group;
    }
    
    public void setGroup(int newValue) {
        int old = group;
        this.group = newValue;
        firePropertyChange(P_Group, old, this.group);
    }
    
     
    @OAProperty(maxLength = 50, displayLength = 11)
    @OAColumn(maxLength = 50)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String newValue) {
        String old = description;
        this.description = newValue;
        firePropertyChange(P_Description, old, this.description);
    }
    
     
    @OAMany(displayName = "Banner Stats", toClass = BannerStat.class, reverseName = BannerStat.P_Banner, createMethod = false)
    private Hub<BannerStat> getBannerStats() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAOne(reverseName = Customer.P_Banners)
    @OAFkey(columns = {"CustomerId"})
    public Customer getCustomer() {
        if (customer == null) {
            customer = (Customer) getObject(P_Customer);
        }
        return customer;
    }
    
    public void setCustomer(Customer newValue) {
        Customer old = this.customer;
        this.customer = newValue;
        firePropertyChange(P_Customer, old, this.customer);
    }
    
     
}
 
