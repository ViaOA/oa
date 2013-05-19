package com.vetjobs.oa;
 
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.annotation.*;
import com.viaoa.util.OADate;
 
 
@OAClass(
    shortName = "emp",
    displayName = "Employer"
)
@OATable(
    indexes = {
        @OAIndex(name = "EmployerCompany", columns = {@OAIndexColumn(name = "Company")}),
        @OAIndex(name = "EmployerContact", columns = {@OAIndexColumn(name = "Contact")}),
        @OAIndex(name = "EmployerParentEmployer", columns = { @OAIndexColumn(name = "ParentEmployerId") })
    }
)
public class Employer extends OAObject {
    private static final long serialVersionUID = 1L;
    public static final String PROPERTY_Id = "Id";
    public static final String PROPERTY_CreateDate = "CreateDate";
    public static final String PROPERTY_Company = "Company";
    public static final String PROPERTY_Address1 = "Address1";
    public static final String PROPERTY_Address2 = "Address2";
    public static final String PROPERTY_City = "City";
    public static final String PROPERTY_State = "State";
    public static final String PROPERTY_Zip = "Zip";
    public static final String PROPERTY_Country = "Country";
    public static final String PROPERTY_Phone = "Phone";
    public static final String PROPERTY_Fax = "Fax";
    public static final String PROPERTY_Contact = "Contact";
    public static final String PROPERTY_Email = "Email";
    public static final String PROPERTY_Title = "Title";
    public static final String PROPERTY_Industry = "Industry";
    public static final String PROPERTY_Url = "Url";
    public static final String PROPERTY_StartDate = "StartDate";
    public static final String PROPERTY_EndDate = "EndDate";
    public static final String PROPERTY_PurchaseDate = "PurchaseDate";
    public static final String PROPERTY_Note = "Note";
    public static final String PROPERTY_CompanyAlias = "CompanyAlias";
     
     
    public static final String PROPERTY_EmployerUsers = "EmployerUsers";
    public static final String PROPERTY_Privileges = "Privileges";
    public static final String PROPERTY_Jobs = "Jobs";
    public static final String PROPERTY_Employers = "Employers";
    public static final String PROPERTY_ParentEmployer = "ParentEmployer";
    public static final String PROPERTY_Batches = "Batches";
     
    protected int id;
    protected OADate createDate;
    protected String company;
    protected String address1;
    protected String address2;
    protected String city;
    protected String state;
    protected String zip;
    protected String country;
    protected String phone;
    protected String fax;
    protected String contact;
    protected String email;
    protected String title;
    protected String industry;
    protected String url;
    protected OADate startDate;
    protected OADate endDate;
    protected OADate purchaseDate;
    protected String note;
    protected String companyAlias;
     
    // Links to other objects.
    protected transient Hub<EmployerUser> hubEmployerUsers;
    protected transient Hub<Job> hubJobs;
    protected transient Hub<Employer> hubEmployers;
    protected transient Employer parentEmployer;
    protected transient Hub<Batch> hubBatches;
     
     
    public Employer() {
    }
     
    public Employer(int id) {
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
    
     
    @OAProperty(maxLength = 75, displayLength = 8)
    @OAColumn(maxLength = 75)
    public String getAddress1() {
        return address1;
    }
    
    public void setAddress1(String newValue) {
        String old = address1;
        this.address1 = newValue;
        firePropertyChange(PROPERTY_Address1, old, this.address1);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 8)
    @OAColumn(maxLength = 75)
    public String getAddress2() {
        return address2;
    }
    
    public void setAddress2(String newValue) {
        String old = address2;
        this.address2 = newValue;
        firePropertyChange(PROPERTY_Address2, old, this.address2);
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
    
     
    @OAProperty(maxLength = 25, displayLength = 5)
    @OAColumn(maxLength = 25)
    public String getState() {
        return state;
    }
    
    public void setState(String newValue) {
        String old = state;
        this.state = newValue;
        firePropertyChange(PROPERTY_State, old, this.state);
    }
    
     
    @OAProperty(maxLength = 18, displayLength = 3)
    @OAColumn(maxLength = 18)
    public String getZip() {
        return zip;
    }
    
    public void setZip(String newValue) {
        String old = zip;
        this.zip = newValue;
        firePropertyChange(PROPERTY_Zip, old, this.zip);
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
    
     
    @OAProperty(maxLength = 25, displayLength = 3)
    @OAColumn(maxLength = 25)
    public String getFax() {
        return fax;
    }
    
    public void setFax(String newValue) {
        String old = fax;
        this.fax = newValue;
        firePropertyChange(PROPERTY_Fax, old, this.fax);
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
    
     
    @OAProperty(maxLength = 75, displayLength = 5)
    @OAColumn(maxLength = 75)
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String newValue) {
        String old = email;
        this.email = newValue;
        firePropertyChange(PROPERTY_Email, old, this.email);
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
    
     
    @OAProperty(maxLength = 75, displayLength = 8)
    @OAColumn(maxLength = 75)
    public String getIndustry() {
        return industry;
    }
    
    public void setIndustry(String newValue) {
        String old = industry;
        this.industry = newValue;
        firePropertyChange(PROPERTY_Industry, old, this.industry);
    }
    
     
    @OAProperty(maxLength = 75, displayLength = 3)
    @OAColumn(maxLength = 75)
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String newValue) {
        String old = url;
        this.url = newValue;
        firePropertyChange(PROPERTY_Url, old, this.url);
    }
    
     
    @OAProperty(displayName = "Start Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(OADate newValue) {
        OADate old = startDate;
        this.startDate = newValue;
        firePropertyChange(PROPERTY_StartDate, old, this.startDate);
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
    
     
    @OAProperty(displayName = "Purchase Date", displayLength = 10)
    @OAColumn(sqlType = java.sql.Types.DATE)
    public OADate getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(OADate newValue) {
        OADate old = purchaseDate;
        this.purchaseDate = newValue;
        firePropertyChange(PROPERTY_PurchaseDate, old, this.purchaseDate);
    }
    
     
    @OAProperty(maxLength = 4, displayLength = 4)
    @OAColumn(sqlType = java.sql.Types.CLOB)
    public String getNote() {
        return note;
    }
    
    public void setNote(String newValue) {
        String old = note;
        this.note = newValue;
        firePropertyChange(PROPERTY_Note, old, this.note);
    }
    
     
    @OAProperty(displayName = "Company Alias", maxLength = 75, displayLength = 12)
    @OAColumn(maxLength = 75)
    public String getCompanyAlias() {
        return companyAlias;
    }
    
    public void setCompanyAlias(String newValue) {
        String old = companyAlias;
        this.companyAlias = newValue;
        firePropertyChange(PROPERTY_CompanyAlias, old, this.companyAlias);
    }
    
     
    @OAMany(displayName = "Employer Users", toClass = EmployerUser.class, owner = true, reverseName = EmployerUser.PROPERTY_Employer, cascadeSave = true, cascadeDelete = true)
    public Hub<EmployerUser> getEmployerUsers() {
        if (hubEmployerUsers == null) {
            hubEmployerUsers = (Hub<EmployerUser>) getHub(PROPERTY_EmployerUsers);
        }
        return hubEmployerUsers;
    }
    
     
    @OAMany(toClass = Privilege.class, reverseName = Privilege.PROPERTY_Employers, createMethod = false)
    @OALinkTable(name = "EmployerPrivilegeLink", indexName = "PrivilegeEmployer", columns = {"EmployerId"})
    private Hub<Privilege> getPrivileges() {
        // oamodel has createMethod set to false, this method exists only for annotations.
        return null;
    }
    
     
    @OAMany(toClass = Job.class, owner = true, reverseName = Job.PROPERTY_Employer, cascadeSave = true, cascadeDelete = true)
    public Hub<Job> getJobs() {
        if (hubJobs == null) {
            hubJobs = (Hub<Job>) getHub(PROPERTY_Jobs);
        }
        return hubJobs;
    }
    
     
    @OAMany(toClass = Employer.class, reverseName = Employer.PROPERTY_ParentEmployer)
    public Hub<Employer> getEmployers() {
        if (hubEmployers == null) {
            hubEmployers = (Hub<Employer>) getHub(PROPERTY_Employers);
        }
        return hubEmployers;
    }
    
     
    @OAOne(displayName = "Parent Employer", reverseName = Employer.PROPERTY_Employers)
    @OAFkey(columns = {"ParentEmployerId"})
    public Employer getParentEmployer() {
        if (parentEmployer == null) {
            parentEmployer = (Employer) getObject(PROPERTY_ParentEmployer);
        }
        return parentEmployer;
    }
    
    public void setParentEmployer(Employer newValue) {
        Employer old = this.parentEmployer;
        this.parentEmployer = newValue;
        firePropertyChange(PROPERTY_ParentEmployer, old, this.parentEmployer);
    }
    
     
    @OAMany(toClass = Batch.class, owner = true, reverseName = Batch.PROPERTY_Employer, cascadeSave = true, cascadeDelete = true)
    public Hub<Batch> getBatches() {
        if (hubBatches == null) {
            hubBatches = (Hub<Batch>) getHub(PROPERTY_Batches);
        }
        return hubBatches;
    }
    
     
}
 
