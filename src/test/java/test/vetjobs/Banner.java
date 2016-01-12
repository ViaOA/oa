package test.vetjobs;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import java.util.*;

public class Banner extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected OADate createDate;
    protected OADate beginDate;
    protected OADate endDate;
    protected String imageUrl;
    protected String forwardUrl;
    protected String altTag;
    protected int group;
    protected String description;
    
    // Links to other objects
    protected Customer customer;
    // protected Hub hubBannerStat;
    
    public Banner() {
    }
     
    public Banner(int id) {
        setId(id);
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public OADate getCreateDate() {
        return createDate;
    }
    public void setCreateDate(OADate createDate) {
        OADate old = this.createDate;
        this.createDate = createDate;
        firePropertyChange("createDate",old,createDate);
    }
    
    public OADate getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(OADate beginDate) {
        OADate old = this.beginDate;
        this.beginDate = beginDate;
        firePropertyChange("beginDate",old,beginDate);
    }
    
    public OADate getEndDate() {
        return endDate;
    }
    public void setEndDate(OADate endDate) {
        OADate old = this.endDate;
        this.endDate = endDate;
        firePropertyChange("endDate",old,endDate);
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        String old = this.imageUrl;
        this.imageUrl = imageUrl;
        firePropertyChange("imageUrl",old,imageUrl);
    }
    
    public String getForwardUrl() {
        return forwardUrl;
    }
    public void setForwardUrl(String forwardUrl) {
        String old = this.forwardUrl;
        this.forwardUrl = forwardUrl;
        firePropertyChange("forwardUrl",old,forwardUrl);
    }
    
    public String getAltTag() {
        return altTag;
    }
    public void setAltTag(String altTag) {
        String old = this.altTag;
        this.altTag = altTag;
        firePropertyChange("altTag",old,altTag);
    }
    
    public int getGroup() {
        return group;
    }
    public void setGroup(int group) {
        int old = this.group;
        this.group = group;
        firePropertyChange("group",old,group);
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }
    
    public Customer getCustomer() {
        if (customer == null) customer = (Customer) getObject("customer");
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        Customer old = this.customer;
        this.customer = customer;
        firePropertyChange("customer",old,customer);
    }
    /**2008 was:
    public Hub getBannerStats() {
        if (hubBannerStat == null) hubBannerStat = getHub("bannerStats");
        return hubBannerStat;
    }
    */
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("customer",Customer.class,OALinkInfo.ONE, false,"banners"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("bannerStats",BannerStat.class,OALinkInfo.MANY, false,"banner"));
    }
}

