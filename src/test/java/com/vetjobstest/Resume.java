package com.vetjobstest;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class Resume extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String title;
    protected OADate createDate;
    protected String resume;
    protected OADate refreshDate;
    protected String summary;
    protected boolean hold;
    protected int searchCount, viewCount, clickCount;
    
    // Links to other objects
    protected VetUser vetUser;

    public Resume() {
    	if (!isLoading()) {
    		setCreateDate(new OADate());
    		setHold(false);
    	}
    }

    public int getSearchCount() {
        return searchCount;
    }
    public void setSearchCount(int searchCount) {
        int old = this.searchCount;
        this.searchCount = searchCount;
        firePropertyChange("searchCount",old,searchCount);
    }
    
    public int getViewCount() {
        return viewCount;
    }
    public void setViewCount(int viewCount) {
        int old = this.viewCount;
        this.viewCount = viewCount;
        firePropertyChange("viewCount",old,viewCount);
    }

    public int getClickCount() {
        return clickCount;
    }
    public void setClickCount(int clickCount) {
        int old = this.clickCount;
        this.clickCount = clickCount;
        firePropertyChange("clickCount",old,clickCount);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        String old = this.title;
        this.title = title;
        firePropertyChange("title",old,title);
    }
    
    public OADate getCreateDate() {
        return createDate;
    }
    public void setCreateDate(OADate createDate) {
        OADate old = this.createDate;
        this.createDate = createDate;
        firePropertyChange("createDate",old,createDate);
    }
    
    public String getResume() {
        return resume;
    }
    public void setResume(String resume) {
        String old = this.resume;
        this.resume = resume;
        firePropertyChange("resume",old,resume);
    }
    
    public OADate getRefreshDate() {
        return refreshDate;
    }
    public void setRefreshDate(OADate refreshDate) {
        OADate old = this.refreshDate;
        this.refreshDate = refreshDate;
        firePropertyChange("refreshDate",old,refreshDate);
    }
    
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        String old = this.summary;
        this.summary = summary;
        firePropertyChange("summary",old,summary);
    }
    /** return first 120 chars of summary */
    public String getShortSummary() {
        String s = getSummary();
        if (s == null) return "";
        if (s.length() > 120) return s.substring(0,116) + " ...";
        return s;
    }
    
    public VetUser getVetUser() {
        if (vetUser == null) vetUser = (VetUser) getObject("vetUser");
        return vetUser;
    }
    
    public void setVetUser(VetUser vetUser) {
        VetUser old = this.vetUser;
        this.vetUser = vetUser;
        firePropertyChange("vetUser",old,vetUser);
    }

    public boolean getHold() {
        return hold;
    }
    public void setHold(boolean b) {
        boolean old = this.hold;
        this.hold = b;
        firePropertyChange("hold",old,hold);
    }
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUser",VetUser.class,OALinkInfo.ONE, false,"resume"));
    }

/* 20090703 was:    
    @Override
    protected void saved(boolean fullSave, boolean force) {
        if (isNew()) {
        	VetUser vu = getVetUser();
        	if (vu != null) {
        		int idx = vu.getId();
        		if (idx != 0) this.setId(idx);
        	}
        }
    	super.onSave(fullSave, force);
    }
*/    
    
/*qqqqqqqqqqqqq
    public boolean canSave() {
        if (isNull("id")) {
            VetUser user = getVetUser();
            if (user != null) setId(user.getId());
            else throw new OAException(Resume.class,"Resume not assigned to a VetUser - cant save");
        }
        return true;
    }
*?    
/***
    public void beforeSave() { 
        String[] props = getChangedProperties();
        boolean b = (props == null || props.length == 0);
        for (int i=0; !b && i < props.length; i++) {
            if (props[i] == null) b = true;
            else if (props[i].toUpperCase().indexOf("COUNT") < 0 && !props[i].equalsIgnoreCase("CHANGED")) {
                b = true;
                break;
            }
        }
        setRefreshDate(new Date());
    }
*****/        

}

