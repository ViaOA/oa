package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class Stat extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected OADate date;
    protected int employerLoginCount;
    protected int vetUserLoginCount;
    
    // Links to other objects
    protected Hub hubBannerStat;
    
    public Stat() {
    }
     
    public Stat(int id) {
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
    
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate date) {
        OADate old = this.date;
        this.date = date;
        firePropertyChange("date",old,date);
    }
    
    public int getEmployerLoginCount() {
        return employerLoginCount;
    }
    public void setEmployerLoginCount(int employerLoginCount) {
        int old = this.employerLoginCount;
        this.employerLoginCount = employerLoginCount;
        firePropertyChange("employerLoginCount",old,employerLoginCount);
    }
    
    public int getVetUserLoginCount() {
        return vetUserLoginCount;
    }
    public void setVetUserLoginCount(int vetUserLoginCount) {
        int old = this.vetUserLoginCount;
        this.vetUserLoginCount = vetUserLoginCount;
        firePropertyChange("vetUserLoginCount",old,vetUserLoginCount);
    }
    
    public Hub getBannerStats() {
        if (hubBannerStat == null) hubBannerStat = getHub("bannerStats");
        return hubBannerStat;
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("bannerStats",BannerStat.class,OALinkInfo.MANY, false,"stat"));
    }
}

