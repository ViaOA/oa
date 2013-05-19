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
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_CreateDate = "CreateDate";
    public static final String PROPERTY_BeginDate = "BeginDate";
    public static final String PROPERTY_EndDate = "EndDate";
    public static final String PROPERTY_ImageUrl = "ImageUrl";
    public static final String PROPERTY_ForwardUrl = "ForwardUrl";
    public static final String PROPERTY_AltTag = "AltTag";
    public static final String PROPERTY_Group = "Group";
    public static final String PROPERTY_Description = "Description";
     
     
    public static final String PROPERTY_BannerStats = "BannerStats";
    public static final String PROPERTY_Customer = "Customer";
     
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
        firePropertyChange(PROPERTY_Id, old, this.id);
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
    
     
    @OAProperty(displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getBeginDate() {
        return beginDate;
    }
    
    public void setBeginDate(OADate newValue) {
        OADate old = beginDate;
        this.beginDate = newValue;
        firePropertyChange(PROPERTY_BeginDate, old, this.beginDate);
    }
    
     
    @OAProperty(displayName = "End Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(OADate newValue) {
        OADate old = endDate;
        this.endDate = newValue;
        firePropertyChange(PROPERTY_EndDate, old, this.endDate);
    }
    
     
    @OAProperty(displayName = "Image Url", maxLength = 200, displayLength = 8)
    @OAColumn(maxLength = 200)
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String newValue) {
        String old = imageUrl;
        this.imageUrl = newValue;
        firePropertyChange(PROPERTY_ImageUrl, old, this.imageUrl);
    }
    
     
    @OAProperty(displayName = "Forward Url", maxLength = 200, displayLength = 10)
    @OAColumn(maxLength = 200)
    public String getForwardUrl() {
        return forwardUrl;
    }
    
    public void setForwardUrl(String newValue) {
        String old = forwardUrl;
        this.forwardUrl = newValue;
        firePropertyChange(PROPERTY_ForwardUrl, old, this.forwardUrl);
    }
    
     
    @OAProperty(displayName = "Alt Tag", maxLength = 75, displayLength = 6)
    @OAColumn(maxLength = 75)
    public String getAltTag() {
        return altTag;
    }
    
    public void setAltTag(String newValue) {
        String old = altTag;
        this.altTag = newValue;
        firePropertyChange(PROPERTY_AltTag, old, this.altTag);
    }
    
     
    @OAProperty(displayLength = 5)
    @OAColumn(sqlType = java.sql.Types.INTEGER)
    public int getGroup() {
        return group;
    }
    
    public void setGroup(int newValue) {
        int old = group;
        this.group = newValue;
        firePropertyChange(PROPERTY_Group, old, this.group);
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
    
     
    @OAMany(displayName = "Banner Stats", toClass = BannerStat.class, reverseName = BannerStat.PROPERTY_Banner, createMethod = false)
    private Hub<BannerStat> getBannerStats() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAOne(reverseName = Customer.PROPERTY_Banners)
    @OAFkey(columns = {"CustomerId"})
    public Customer getCustomer() {
        if (customer == null) {
            customer = (Customer) getObject(PROPERTY_Customer);
        }
        return customer;
    }
    
    public void setCustomer(Customer newValue) {
        Customer old = this.customer;
        this.customer = newValue;
        firePropertyChange(PROPERTY_Customer, old, this.customer);
    }
    
     
}
 
