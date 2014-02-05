package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class MilitaryJobCode extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String jobCode;
    protected String description;
    
    // Links to other objects
    protected Hub hubCategory;
    
    public MilitaryJobCode() {
    }
     
    public MilitaryJobCode(int id) {
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
    
    public String getJobCode() {
        return jobCode;
    }
    public void setJobCode(String jobCode) {
        String old = this.jobCode;
        this.jobCode = jobCode;
        firePropertyChange("jobCode",old,jobCode);
    }
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }
    
    public Hub getCategories() {
        if (hubCategory == null) hubCategory = getHub("categories");
        return hubCategory;
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("categories",Category.class,OALinkInfo.MANY, false,"militaryJobCodes"));
    }
}

