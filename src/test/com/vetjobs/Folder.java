package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class Folder extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String name;
    protected String description;
    protected boolean hide;
    
    // Links to other objects
    // protected Hub hubJob;
    
    public Folder() {
    }
     
    public Folder(int id) {
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
    
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        String old = this.description;
        this.description = description;
        firePropertyChange("description",old,description);
    }
    
    public boolean getHide() {
        return hide;
    }
    public void setHide(boolean hide) {
        boolean old = this.hide;
        this.hide = hide;
        firePropertyChange("hide",old,hide);
    }
    
    /**2008 was:
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
        oaObjectInfo.addLinkInfo(new OALinkInfo("batchRows",BatchRow.class,OALinkInfo.MANY, false,"folder"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("jobs",Job.class,OALinkInfo.MANY, false,"folder"));
    }
}

