package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Privilege extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String description;
    
    // Links to other objects
    
    public Privilege() {
    }
     
    public Privilege(int id) {
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
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("employers",Employer.class,OALinkInfo.MANY, false,"privileges"));
    }
}

