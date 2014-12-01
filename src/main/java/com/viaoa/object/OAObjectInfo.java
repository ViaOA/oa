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
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.viaoa.hub.*;
import com.viaoa.util.OAPropertyPath;

/** 
    OAObjectInfo contains information about an OAObject.
    This includes object id, links to other objects, calculated properties. 
    <p>
    Note: this will be replaced by the com.viaoa.model Classes
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAObjectInfo { //implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    static final Object vlock = new Object();

    protected Class thisClass; // the Class for this ObjectInfo.  Set when calling OAObjectDelegete.getOAObjectInfo
    
    protected List<OALinkInfo> alLinkInfo;
    protected ArrayList<OACalcInfo> alCalcInfo;
    protected HashSet<String> hsHubCalcInfoName = new HashSet<String>();
    protected String[] idProperties;
    protected ArrayList<OAPropertyInfo> alPropertyInfo;
    
    protected boolean bUseDataSource = true;
    protected boolean bLocalOnly = false;  // dont send to OAServer
    protected boolean bAddToCache = true;  // add object to Cache
    protected boolean bInitializeNewObjects = true;  // initialize object properties (used by OAObject)
    protected String displayName;
    protected String[] rootTreePropertyPaths;

    // this is set by OAObjectInfoDelegate.initialize()
    // All primitive properties, in uppercase and sorted.
    // This is used by OAObject.nulls, to get the bit position for an objects primitive properties.
    protected String[] primitiveProps;

    // 20120827 hubs that have a size of 0
    protected String[] hubProps;

    int weakReferenceable=-1; // flag set/used by OAObjectInfoDelegate.isWeakReferenceable -1=not checked, 0=false, 1=true
    
    
    public OAObjectInfo() {
        this(new String[] { });
    }
    
    public Class getForClass() {
        return thisClass;
    }
    public OAObjectInfo(String objectIdProperty) {
        this(new String[] { objectIdProperty });
    }
    public OAObjectInfo(String[] idProperties) {
    	this.idProperties = idProperties;
    }
    void setPropertyIds(String[] ss) {
        this.idProperties = ss;
    }
    
    public String[] getIdProperties() {
    	if (this.idProperties == null) this.idProperties = new String[0];
    	return this.idProperties;
    }

    public List<OALinkInfo> getLinkInfos() {
    	if (alLinkInfo == null) alLinkInfo = new CopyOnWriteArrayList<OALinkInfo>() {
    	    @Override
    	    public boolean add(OALinkInfo e) {
    	        hmLinkInfo = null;
    	        ownedLinkInfos = null;
    	        return super.add(e);
    	    }
    	};
    	return alLinkInfo;
    }
    public void addLink(OALinkInfo li) {
    	addLinkInfo(li);
    }
    public void addLinkInfo(OALinkInfo li) {
        getLinkInfos().add(li);
    }
    private HashMap<String,OALinkInfo> hmLinkInfo;
    public OALinkInfo getLinkInfo(String propertyName) {
        if (propertyName == null) return null;
        HashMap<String,OALinkInfo> hm = hmLinkInfo;
        if (hm == null) {
            hm = new HashMap<String, OALinkInfo>();
            for (OALinkInfo li : getLinkInfos()) {
                String s = li.getName();
                if (s == null) continue;
                hm.put(s.toUpperCase(), li);
            }
            hmLinkInfo = hm;
        }
        return hm.get(propertyName.toUpperCase());
    }
    
    private OALinkInfo[] ownedLinkInfos;
    public OALinkInfo[] getOwnedLinkInfos() {
        if (ownedLinkInfos == null) {
            int x = 0;
            for (OALinkInfo li : getLinkInfos()) {
                if (li.bOwner) x++;
            }
            OALinkInfo[] temp = new OALinkInfo[x];
            int i = 0;
            for (OALinkInfo li : getLinkInfos()) {
                if (li.bOwner) {
                    if (i == x) {
                        return getOwnedLinkInfos();
                    }
                    temp[i++] = li; 
                }
            }
            ownedLinkInfos = temp;
        }
        return ownedLinkInfos;
    }
    
    public ArrayList<OACalcInfo> getCalcInfos() {
    	if (alCalcInfo == null) alCalcInfo = new ArrayList<OACalcInfo>(5);
    	return alCalcInfo;
    }
    public void addCalc(OACalcInfo ci) {
        addCalcInfo(ci);
    }
    public void addCalcInfo(OACalcInfo ci) {
    	getCalcInfos().add(ci);
    	if (ci.bIsForHub) {
        	String s = ci.getName();
        	if (s != null) {
        	    hsHubCalcInfoName.add(s.toUpperCase());
        	}
    	}
    }
    public boolean isHubCalcInfo(String name) {
        if (name == null) return false;
        return hsHubCalcInfoName.contains(name.toUpperCase());
    }
    
    /**
     * This is for regular properties, and does not include reference properties.
     * @see #getLinkInfos() to get list of reference properties.
     */
    public ArrayList<OAPropertyInfo> getPropertyInfos() {
    	if (alPropertyInfo == null) alPropertyInfo = new ArrayList(5);
    	return alPropertyInfo;
    }
    public void addProperty(OAPropertyInfo ci) {
    	getPropertyInfos().add(ci);
        hmPropertyInfo = null; 
    }
    public void addPropertyInfo(OAPropertyInfo ci) {
    	getPropertyInfos().add(ci);
        hmPropertyInfo = null; 
    }

    private HashMap<String,OAPropertyInfo> hmPropertyInfo;
    
    public OAPropertyInfo getPropertyInfo(String propertyName) {
        if (propertyName == null) return null;
        HashMap<String,OAPropertyInfo> hm = hmPropertyInfo;
        if (hm == null) {
            hm = new HashMap<String, OAPropertyInfo>();
            for (OAPropertyInfo pi : getPropertyInfos()) {
                String s = pi.getName();
                if (s == null) continue;
                hm.put(s.toUpperCase(), pi);
            }
            hmPropertyInfo = hm;
        }
        return hm.get(propertyName.toUpperCase());
    }
    
    
    // set by OAObjectInfoDelegate.initialize().  All primitive properties, in uppercase, sorted - 
    //   used for the bit position for OAObject.nulls
    public String[] getPrimitiveProperties() {
        return primitiveProps;
    }
    
    // 20120827
    public String[] getHubProperties() {
        return hubProps;
    }

    public void setUseDataSource(boolean b) {
        bUseDataSource = b;
    }
    public boolean getUseDataSource() {
        return bUseDataSource;
    }

    public void setLocalOnly(boolean b) {
        bLocalOnly = b;
    }
    public boolean getLocalOnly() {
        return bLocalOnly;
    }

    public void setAddToCache(boolean b) {
        bAddToCache = b;
    }
    public boolean getAddToCache() {
        return bAddToCache;
    }

    public void setInitializeNewObjects(boolean b) {
        bInitializeNewObjects = b;
    }
    public boolean getInitializeNewObjects() {
        return bInitializeNewObjects;
    }

    public String getDisplayName() {
        if (displayName == null && thisClass != null) displayName = thisClass.getName();
        return displayName;
    }
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    public String[] getRootTreePropertyPaths() {
        return rootTreePropertyPaths;
    }
    public void setRootTreePropertyPaths(String[] paths) {
        this.rootTreePropertyPaths = paths;
    }
    
    public void addRequired(String prop) {
    	ArrayList al = getPropertyInfos();
    	for (int i=0; i<al.size(); i++)  {
    		OAPropertyInfo pi = (OAPropertyInfo) al.get(i);
    		if (pi.getName().equalsIgnoreCase(prop)) {
    			pi.setRequired(true);
    		}
    	}
    }

   
 // 2008/01/02 all of these were created to support the old oa.html package    
    public OALinkInfo getRecursiveLinkInfo(int type) {
    	return OAObjectInfoDelegate.getRecursiveLinkInfo(this, type);
    }
}


