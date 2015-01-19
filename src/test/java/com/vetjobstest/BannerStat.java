package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class BannerStat extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected int displayCount;
    protected int hitCount;
    
    // Links to other objects
    protected Stat stat;
    protected Banner banner;
    
    public BannerStat() {
    }
     
    public BannerStat(int id) {
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
    
    public int getDisplayCount() {
        return displayCount;
    }
    public void setDisplayCount(int displayCount) {
        int old = this.displayCount;
        this.displayCount = displayCount;
        firePropertyChange("displayCount",old,displayCount);
    }
    
    public int getHitCount() {
        return hitCount;
    }
    public void setHitCount(int hitCount) {
        int old = this.hitCount;
        this.hitCount = hitCount;
        firePropertyChange("hitCount",old,hitCount);
    }
    
    public Stat getStat() {
        if (stat == null) stat = (Stat) getObject("stat");
        return stat;
    }
    
    public void setStat(Stat stat) {
        Stat old = this.stat;
        this.stat = stat;
        firePropertyChange("stat",old,stat);
    }
    
    public Banner getBanner() {
        if (banner == null) banner = (Banner) getObject("banner");
        return banner;
    }
    
    public void setBanner(Banner banner) {
        Banner old = this.banner;
        this.banner = banner;
        firePropertyChange("banner",old,banner);
    }
     
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("stat",Stat.class,OALinkInfo.ONE, false,"bannerStats"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("banner",Banner.class,OALinkInfo.ONE, false,"bannerStats"));
    }
}

