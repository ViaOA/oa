package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class EmpQueryVet extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected int viewType;

    // Links to other objects
    protected EmpQuery empQuery;
    protected VetUser vetUser;

    public EmpQueryVet() {
    }

    public void setViewType(int x) {
    	int old = this.viewType;
    	this.viewType = x;
    	firePropertyChange("viewType", old, viewType);
    }
    public int getViewType() {
    	return viewType;
    }
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }

    public EmpQuery getEmpQuery() {
        if (empQuery == null) empQuery = (EmpQuery) getObject("empQuery");
        return empQuery;
    }

    public void setEmpQuery(EmpQuery empQuery) {
        EmpQuery old = this.empQuery;
        this.empQuery = empQuery;
        firePropertyChange("empQuery", old, empQuery);
    }
    
    public VetUser getVetUser() {
        if (vetUser == null) vetUser = (VetUser) getObject("vetUser");
        return vetUser;
    }

    public void setVetUser(VetUser vetUser) {
        VetUser old = this.vetUser;
        this.vetUser = vetUser;
        firePropertyChange("vetUser", old, vetUser);
    }

    
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("empQuery",EmpQuery.class,OALinkInfo.ONE, false,"empQueryVets"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("vetUser",VetUser.class,OALinkInfo.ONE, false,"empQueryVets"));
    }
}

