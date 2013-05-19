package com.vetjobs;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

public class EmpQuery extends OAObject {
    private static final long serialVersionUID = 1L;
    protected int id;
    protected String queryText;
    protected String queryDescription;
    protected OADate date;
    protected OATime time;

    // Links to other objects
    protected EmployerUser employerUser;
    protected Hub hubEmpQueryVet;

    public EmpQuery() {
    	if (!isLoading()) {
    		setDate(new OADate());
    		setTime(new OATime());
    	}
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        int old = this.id;
        this.id = id;
        firePropertyChange("id",old,id);
    }

    public String getQueryText() {
    	return queryText;
    }
    public void setQueryText(String queryText) {
    	String old = queryText;
    	this.queryText = queryText;
    	firePropertyChange("queryText", old, queryText);
    }

    public String getQueryDescription() {
    	return queryDescription;
    }
    public void setQueryDescription(String queryDescription) {
    	String old = queryDescription;
    	this.queryDescription = queryDescription;
    	firePropertyChange("queryDescription", old, queryDescription);
    }
    
    public OADate getDate() {
        return date;
    }
    public void setDate(OADate date) {
        OADate old = this.date;
        this.date = date;
        firePropertyChange("date",old,date);
    }

    public OATime getTime() {
        return time;
    }
    public void setTime(OATime time) {
        OATime old = this.time;
        this.time = time;
        firePropertyChange("time",old,time);
    }
    
    public EmployerUser getEmployerUser() {
        if (employerUser == null) employerUser = (EmployerUser) getObject("employerUser");
        return employerUser;
    }

    public void setEmployerUser(EmployerUser employerUser) {
        EmployerUser old = this.employerUser;
        this.employerUser = employerUser;
        firePropertyChange("employerUser",old,employerUser);
    }

    
    public Hub getEmpQueryVets() {
        if (hubEmpQueryVet == null) {
            hubEmpQueryVet = getHub("empQueryVets");
        }
        return hubEmpQueryVet;
    }
    
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo("id");
        oaObjectInfo.addLinkInfo(new OALinkInfo("EmployerUser",EmployerUser.class,OALinkInfo.ONE, false,"empQueries"));
        oaObjectInfo.addLinkInfo(new OALinkInfo("empQueryVets",EmpQueryVet.class,OALinkInfo.MANY, true,"empQuery"));
    }
}

