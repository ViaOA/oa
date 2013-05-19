package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
 
 
@OAClass(
    shortName = "cus",
    displayName = "Customer"
)
@OATable(
)
public class Customer extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_Company = "Company";
    public static final String PROPERTY_Contact = "Contact";
    public static final String PROPERTY_Phone = "Phone";
     
     
    public static final String PROPERTY_Banners = "Banners";
     
    protected int id;
    protected String company;
    protected String contact;
    protected String phone;
     
    // Links to other objects.
    protected transient Hub<Banner> hubBanners;
     
     
    public Customer() {
    }
     
    public Customer(int id) {
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
    
     
    @OAProperty(maxLength = 75, displayLength = 7)
    @OAColumn(maxLength = 75)
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String newValue) {
        String old = company;
        this.company = newValue;
        firePropertyChange(PROPERTY_Company, old, this.company);
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
    
     
    @OAProperty(maxLength = 25, displayLength = 5)
    @OAColumn(maxLength = 25)
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String newValue) {
        String old = phone;
        this.phone = newValue;
        firePropertyChange(PROPERTY_Phone, old, this.phone);
    }
    
     
    @OAMany(toClass = Banner.class, reverseName = Banner.PROPERTY_Customer)
    public Hub<Banner> getBanners() {
        if (hubBanners == null) {
            hubBanners = (Hub<Banner>) getHub(PROPERTY_Banners);
        }
        return hubBanners;
    }
    
     
}
 
