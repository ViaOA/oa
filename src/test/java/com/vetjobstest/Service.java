package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Service extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    
    // Links to other objects
    
    public Service() {
    }
     
    public Service(int id) {
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
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange("name",old,name);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUsers",VetUser.class,OALinkInfo.MANY, false,"service"));
    }
}

