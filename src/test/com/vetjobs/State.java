package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.object.*;

public class State extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String abbrev;
    protected String name;
    
    
    public State() {
    }
     
    public State(int id) {
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
    
    public String getAbbrev() {
        return abbrev;
    }
    public void setAbbrev(String abbrev) {
        String old = this.abbrev;
        this.abbrev = abbrev;
        firePropertyChange("abbrev",old,abbrev);
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
    }
}

