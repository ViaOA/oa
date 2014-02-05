/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.object;

import java.lang.reflect.*;

/** 
    Defines reference properties between OAObjects.
    <p>
    <b>Note:</b> this will be replaced by com.viaoa.model.OALinkPropertyDef
    <b>WARNING:</b> this object is past with Hub (using RMI), so make sure of transient properties.
*/
public class OALinkInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;    
    public static final int ONE = 0;
    public static final int MANY = 1;

    String name;
    Class toClass;
    Class[] triggerClasses;
    int type;
    boolean cascadeSave;  // save, delete of this object will do same with link hub
    boolean cascadeDelete;  // save, delete of this object will do same with link hub
    // property that needs to be updated in an inserted object.  same as Hub.propertyToMaster
    protected String reverseName;  // reverse property name
    boolean bOwner;  // this object is the owner of the linked to object/hub
    boolean bRecursive; 
    private boolean bTransient;
    private boolean bAutoCreateNew;
    private String matchHub;  // propertyPath to find matching hub
    private String matchProperty;  // propertyPath to match, using HubAutoMatch
    boolean mustBeEmptyForDelete; // this link must be emtpy before other side can be deleted
    
    // runtime
    protected transient int cacheSize;
    protected OALinkInfo revLinkInfo;
    protected boolean bCalculated;
    protected boolean bPrivateMethod; // 20130212 true if the method is not created, or is private
    
    public OALinkInfo(String name, Class toClass, int type) {
    	this(name, toClass, type, false, false, null, false);
    }
    public OALinkInfo(String name, Class toClass, int type, boolean cascade, String reverseName) {
    	this(name, toClass, type, cascade, cascade, reverseName, false);
    }
    public OALinkInfo(String name, Class toClass, int type, boolean cascade, String reverseName, boolean bOwner) {
    	this(name, toClass, type, cascade, cascade, reverseName, bOwner);
    }
    
    public OALinkInfo(String name, Class toClass, int type, boolean cascadeSave, boolean cascadeDelete, String reverseName) {
    	this(name, toClass, type, cascadeSave, cascadeDelete, reverseName, false);
    }
    public OALinkInfo(String name, Class toClass, int type, boolean cascadeSave, boolean cascadeDelete, String reverseName, boolean bOwner) {
        this.name = name;
        this.toClass = toClass;
        this.type = type;
        this.cascadeSave = cascadeSave;
        this.cascadeDelete = cascadeDelete;
        this.reverseName = reverseName;
        this.bOwner = bOwner;
    }
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof OALinkInfo)) return false;
        
        OALinkInfo li = (OALinkInfo) obj;
        
        if (li.toClass == null && this.toClass != null) return false;
        else if (!li.toClass.equals(this.toClass)) return false;

        if (li.name == null) {
            if (this.name != null) return false;
        }
        else {
            if (!li.name.equalsIgnoreCase(this.name)) return false;
        }
        return true;
    }

    public boolean isOwner() {
        return bOwner;
    }

    /** note: a recursive link cant be owned by itself */
    public boolean getOwner() {
        return bOwner;
    }
    public void setOwner(boolean b) {
        bOwner = b;
    }

    public boolean getRecursive() {
        return bRecursive;
    }
    public void setRecursive(boolean b) {
        bRecursive = b;
    }
    public Class getToClass() {
        return toClass;
    }
    public void setToClass(Class c) {
        this.toClass = c;
    }
    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String getReverseName() {
        return reverseName;
    }
    public void setReverseName(String name) {
        this.reverseName = name;        
    }

    public void setTransient(boolean b) {
        this.bTransient = b;
    }
    public boolean getTransient() {
        return bTransient;
    }
    public void setCalculated(boolean b) {
        this.bCalculated = b;
    }
    public boolean getCalculated() {
        return bCalculated;
    }

    public void setPrivateMethod(boolean b) {
        this.bPrivateMethod = b;
    }
    public boolean getPrivateMethod() {
        return this.bPrivateMethod;
    }
        
    public boolean getCascadeSave() {
        return cascadeSave;
    }
    public void setCascadeSave(boolean b) {
        this.cascadeSave = b;
    }

    public boolean getCascadeDelete() {
        return cascadeDelete;
    }
    public void setCascadeDelete(boolean b) {
        this.cascadeDelete = b;
    }
    
    public boolean getAutoCreateNew() {
        return bAutoCreateNew;
    }
    public void setAutoCreateNew(boolean bAutoCreateNew) {
        this.bAutoCreateNew = bAutoCreateNew;
    }

    
    public boolean getMustBeEmptyForDelete() {
        return mustBeEmptyForDelete;
    }
    public void setMustBeEmptyForDelete(boolean b) {
        this.mustBeEmptyForDelete = b;
    }
        
    /**
    Set the number of hubs that will be cached.
	*/
	public void setCacheSize(int x) {
	    this.cacheSize = Math.max(0, x);
	}
	public int getCacheSize() {
	    return this.cacheSize;
	}

// 2008/01/02 all of these were created to support the old oa.html package    
	public Object getValue(Object obj) {
		return OAObjectReflectDelegate.getProperty((OAObject)obj, name);
	}
	
	public void setMatchProperty(String prop) {
	    this.matchProperty = prop;
	}
	public String getMatchProperty() {
	    return this.matchProperty;
	}

	// pp = propertyPath to matchingHub
    public void setMatchHub(String pp) {
        this.matchHub = pp;
    }
    public String getMatchHub() {
        return this.matchHub;
    }

    public void setTriggerClasses(Class[] cs) {
        this.triggerClasses = cs;
    }
    public Class[] getTriggerClasses() {
        return this.triggerClasses;
    }
}

