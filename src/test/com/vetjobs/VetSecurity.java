package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class VetSecurity extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    
    // Links to other objects
    protected Hub hubVetSecurity;
    protected VetSecurity parentVetSecurity;
    
    public VetSecurity() {
    }
     
    public VetSecurity(int id) {
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
    
    public Hub getVetSecurities() {
        if (hubVetSecurity == null) hubVetSecurity = getHub("vetSecurities");
        return hubVetSecurity;
    }
    
    public VetSecurity getParentVetSecurity() {
        if (parentVetSecurity == null) parentVetSecurity = (VetSecurity) getObject("parentVetSecurity");
        return parentVetSecurity;
    }
    
    public void setParentVetSecurity(VetSecurity parentVetSecurity) {
        VetSecurity old = this.parentVetSecurity;
        this.parentVetSecurity = parentVetSecurity;
        firePropertyChange("parentVetSecurity",old,parentVetSecurity);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUsers",VetUser.class,OALinkInfo.MANY, false,"vetSecurities"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetSecurities",VetSecurity.class,OALinkInfo.MANY, false,"parentVetSecurity"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("parentVetSecurity",VetSecurity.class,OALinkInfo.ONE, false,"vetSecurities"));
    }
}

