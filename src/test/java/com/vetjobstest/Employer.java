package com.vetjobstest;


import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;

public class Employer extends OAObject {
    private static final long serialVersionUID = 1L;
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
    protected String Note;
    protected String companyAlias;
    
    // Links to other objects
    protected Hub hubEmployerUser;
    protected Hub hubPrivilege;
    protected Hub hubEmployer;
    protected Employer parentEmployer;
    protected Hub hubBatch;
    // protected Hub hubJob;
    
    public Employer() {
        setCreateDate(new OADate());
    }
     
    public Employer(int id) {
        super();
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
    
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        String old = this.company;
        this.company = company;
        firePropertyChange("company",old,company);
    }

    public String getCompanyAlias() {
        return companyAlias;
    }
    public void setCompanyAlias(String companyAlias) {
        String old = this.companyAlias;
        this.companyAlias = companyAlias;
        firePropertyChange("companyAlias",old,companyAlias);
    }
    
    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        String old = this.address1;
        this.address1 = address1;
        firePropertyChange("address1",old,address1);
    }

    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        String old = this.address2;
        this.address2 = address2;
        firePropertyChange("address2",old,address2);
    }
    
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        String old = this.city;
        this.city = city;
        firePropertyChange("city",old,city);
    }
    
    public String getState() {
        return state;
    }
    public void setState(String state) {
        String old = this.state;
        this.state = state;
        firePropertyChange("state",old,state);
    }
    
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        String old = this.zip;
        this.zip = zip;
        firePropertyChange("zip",old,zip);
    }
    
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        String old = this.country;
        this.country = country;
        firePropertyChange("country",old,country);
    }
    
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        String old = this.phone;
        this.phone = phone;
        firePropertyChange("phone",old,phone);
    }
    
    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        String old = this.fax;
        this.fax = fax;
        firePropertyChange("fax",old,fax);
    }
    
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        String old = this.contact;
        this.contact = contact;
        firePropertyChange("contact",old,contact);
    }
    
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        String old = this.email;
        this.email = email;
        firePropertyChange("email",old,email);
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        firePropertyChange("title",old,title);
    }
    
    public String getIndustry() {
        return industry;
    }
    public void setIndustry(String industry) {
        String old = this.industry;
        this.industry = industry;
        firePropertyChange("industry",old,industry);
    }
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        String old = this.url;
        this.url = url;
        firePropertyChange("url",old,url);
    }
    
    public OADate getStartDate() {
        return startDate;
    }
    public void setStartDate(OADate startDate) {
        OADate old = this.startDate;
        this.startDate = startDate;
        firePropertyChange("startDate",old,startDate);
    }
    
    public OADate getEndDate() {
        return endDate;
    }
    public void setEndDate(OADate endDate) {
        OADate old = this.endDate;
        this.endDate = endDate;
        firePropertyChange("endDate",old,endDate);
    }
    
    public OADate getPurchaseDate() {
        return purchaseDate;
    }
    public void setPurchaseDate(OADate purchaseDate) {
        OADate old = this.purchaseDate;
        this.purchaseDate = purchaseDate;
        firePropertyChange("purchaseDate",old,purchaseDate);
    }
    
    public String getNote() {
        return Note;
    }
    public void setNote(String Note) {
        String old = this.Note;
        this.Note = Note;
        firePropertyChange("Note",old,Note);
    }
    
    public Hub getEmployerUsers() {
        if (hubEmployerUser == null) {
            hubEmployerUser = getHub("employerUsers");
        }
        return hubEmployerUser;
    }
    
    public Hub getPrivileges() {
        if (hubPrivilege == null) hubPrivilege = getHub("privileges");
        return hubPrivilege;
    }
    
    public Hub getEmployers() {
        if (hubEmployer == null) hubEmployer = getHub("employers");
        return hubEmployer;
    }
    
    public Employer getParentEmployer() {
        if (parentEmployer == null) parentEmployer = (Employer) getObject("parentEmployer");
        return parentEmployer;
    }
    
    public void setParentEmployer(Employer parentEmployer) {
        Employer old = this.parentEmployer;
        this.parentEmployer = parentEmployer;
        firePropertyChange("parentEmployer",old,parentEmployer);
    }
    
    public Hub getBatches() {
        if (hubBatch == null) hubBatch = getHub("batches");
        return hubBatch;
    }
    
    /* 2008 removed
    public Hub getJobs() {
        if (hubJob == null) hubJob = getHub("jobs");
        return hubJob;
    }
    */
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("employerUsers",EmployerUser.class,OALinkInfo.MANY, true,"employer"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("privileges",Privilege.class,OALinkInfo.MANY, false,"employers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("employers",Employer.class,OALinkInfo.MANY, true,"parentEmployer"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("parentEmployer",Employer.class,OALinkInfo.ONE, false,"employers"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("batches",Batch.class,OALinkInfo.MANY, false,"employer"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("jobs",Job.class,OALinkInfo.MANY, true,"employer"));
    }


    // custom
    public boolean allowPost() {
        // 1 = Deny Post
        return (getPrivileges().getObject(1) == null);
    }
    public boolean allowSearch() {
        // 2 = Deny Search
        return (getPrivileges().getObject(2) == null);
    }
    public boolean allowAccess() {
        // 3 = Deny Access
        return (getPrivileges().getObject(3) == null);
    }
    public boolean allowSubEmployers() {
        // 4 = Deny SubEmployers
        return (getPrivileges().getObject(4) == null);
    }
}

