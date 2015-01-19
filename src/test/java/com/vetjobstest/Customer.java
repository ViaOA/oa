package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Customer extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String company;
    protected String contact;
    protected String phone;
    
    // Links to other objects
    protected Hub hubBanner;
    
    public Customer() {
    }
     
    public Customer(int id) {
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
    
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        String old = this.company;
        this.company = company;
        firePropertyChange("company",old,company);
    }
    
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        String old = this.contact;
        this.contact = contact;
        firePropertyChange("contact",old,contact);
    }
    
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        String old = this.phone;
        this.phone = phone;
        firePropertyChange("phone",old,phone);
    }
    
    public Hub getBanners() {
        if (hubBanner == null) hubBanner = getHub("banners");
        return hubBanner;
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("banners",Banner.class,OALinkInfo.MANY, false,"customer"));
    }
}

