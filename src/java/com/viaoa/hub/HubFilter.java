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
package com.viaoa.hub;

import java.util.Hashtable;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.object.*;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAArray;

/**
    HubFilter is used to create a Hub that has objects that are filtered from another Hub.
    <p>
    All that is needed is to subclass the HubFilter and implement the "isUsed()" method 
    to know if an object is to be included in the filtered Hub.
    <p>
    Example<br>
    <pre>
    Hub hubFilter = new Hub(Employee.class)
    new HubFilter(hubAllEmployees, hubFilter) {
        public boolean isUsed(Object obj) {
            // .... code to check if object should be added to hubFilter
        }
    };
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/

public abstract class HubFilter<TYPE> extends HubListenerAdapter implements java.io.Serializable, OAFilter<TYPE> {
    private static final long serialVersionUID = 1L;

    protected Hub hubMaster, hub;
    private Hashtable hashProp;
    private boolean bShareAO;
    private boolean bClosed;
    private boolean bServerSideOnly;

    // listener setup for dependent properties
    private static int UniqueNameCnt;
    private String uniqueName;
    private String[] dependentProperties;
    private HubListener hlDependentProperties;
    
    public boolean DEBUG;
    private boolean bOAObjectCacheDelegateListener;
    private HubListenerAdapter hlHubMaster;
    private boolean bNewListFlag;
    private boolean bClearing;
    private boolean bUpdating;
    
    /** 
        Create a new HubFilter using two supplied Hubs.
        @param hubMaster hub with complete list of objects.
        @param hub that stores filtered objects. 
    */
    public HubFilter(Hub<TYPE> hubMaster, Hub<TYPE> hub) {
        this(false, hubMaster, hub, false, false, null);
    }

    public HubFilter(Hub<TYPE> hubMaster, Hub<TYPE> hub, boolean bShareAO) {
        this(false, hubMaster, hub, bShareAO, false, null);
    }

    public HubFilter(Hub<TYPE> hubMaster, Hub<TYPE> hub, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, false, false, dependentPropertyPaths);
    }    
    public HubFilter(Hub<TYPE> hubMaster, Hub<TYPE> hub, boolean bShareAO, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, bShareAO, false, dependentPropertyPaths);
    }    
    public HubFilter(Hub<TYPE> hubMaster, Hub<TYPE> hub, boolean bShareAO, boolean bRefreshOnLinkChange, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, bShareAO, bRefreshOnLinkChange, dependentPropertyPaths);
    }
    
    public HubFilter(boolean bObjectCache, Hub<TYPE> hubMaster, Hub<TYPE> hub, boolean bShareAO, boolean bRefreshOnLinkChange, String... dependentPropertyPaths) {
        // note: bObjectCache will allow hubMaster to be null, which will then use the oaObjectCache
        if (!bObjectCache && hubMaster == null) {
            throw new IllegalArgumentException("hubMaster can not be null if bObjectCache=false");
        }
        if (hub == null) {  // 20131129 hub can now be null, used by Triggers
            // throw new IllegalArgumentException("hub can not be null");
        }
        this.hubMaster = hubMaster;
        this.hub = hub;
        this.bShareAO = bShareAO;
        this.bRefreshOnLinkChange = bRefreshOnLinkChange;
        setup();
        if (dependentPropertyPaths != null) {
            for (String s : dependentPropertyPaths) {
                addProperty(s);
            }
        }
    }

    /** 
        HubFilter that works with HubController so that all objects of class hub.getObjectClass() are filtered.
        @param hub that stores filtered objects. 
    */
    public HubFilter(Hub<TYPE> hub) {
        this(true, null, hub, false, false, null);
    }

    /**
     * This needs to be set to true if it is only created on the server, but
     * client applications will be using the same Hub that is filtered.
     * This is so that changes on the hub will be published to the clients, even if initiated on OAClientThread. 
     */
    public void setServerSideOnly(boolean b) {
        bServerSideOnly = b;
    }
    
    public void close() {
        // need to make sure that no more events get processed
        this.bClosed = true;
		if (bOAObjectCacheDelegateListener) {
		    Class c = (hub != null) ? hub.getObjectClass() : hubMaster.getObjectClass();
		    OAObjectCacheDelegate.removeListener(c, getMasterHubListener());
		    bOAObjectCacheDelegateListener = false;
		}
        if (hub != null) {
            hub.removeHubListener(this);
        }
        if (hubMaster != null && hlHubMaster != null) {
            hubMaster.removeHubListener(hlHubMaster);
            hlHubMaster = null;
        }
        if (hubLink != null && linkHubListener != null) {
            hubLink.removeHubListener(linkHubListener);
            linkHubListener = null;
        }
        if (hlDependentProperties != null) {
            hubMaster.removeHubListener(hlDependentProperties);
            hlDependentProperties = null;
        }
    }
    

    /**
        Property names to listen for changes.
        @param prop property name or property path (from Hub)
        @see #setRefreshOnLinkChange(boolean) to refresh list when linkTo Hub AO changes
    */
    public void addDependentProperty(String prop) {
        if (bClosed) return;
        addProperty(prop);
    }

    /** 
     * Add a dependent property from an oaObj, which will call refresh.
     */
    public void addDependentProperty(OAObject obj, String prop) {
        if (bClosed) return;
        if (prop == null || prop.length() == 0) return;
        if (obj == null) return;
        Hub h = new Hub(obj);
        addDependentProperty(h, prop);
    }
    /** 
     * Add a dependent property from a Hub, which will call refresh
     * when hub.AO changes
     */
    public void addDependentProperty(Hub hub) {
        if (bClosed) return;
        if (hub == null) return;
        
        if (uniqueName == null) uniqueName = "HubFilter" + (UniqueNameCnt++);
        final String propName = uniqueName;
        
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                HubFilter.this.refresh();
            }
        });
    }
    /** 
     * Add a dependent property from a Hub, which will call refresh
     * when prop changes or when hub.AO changes
     */
    public void addDependentProperty(Hub hub, String prop) {
        if (bClosed) return;
        if (prop == null || prop.length() == 0) return;
        if (hub == null) return;
        
        if (uniqueName == null) uniqueName = "HubFilter" + (UniqueNameCnt++);
        final String propName = prop.indexOf('.') < 0 ? prop : uniqueName;
        
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                HubFilter.this.refresh();
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (propName.equalsIgnoreCase(e.getPropertyName())) {
                    HubFilter.this.refresh();
                }
            }
        }, propName, new String[] {prop});
    }
    
    /**
     * Same as addDependentProperty method
     * @param prop
     * @deprecated use addDependentProperty instead
     */
    public void addProperty(String prop) {
        if (bClosed) return;
        if (prop == null || prop.length() == 0) return;
        
        if (hlDependentProperties != null) {
            hubMaster.removeHubListener(hlDependentProperties);
        }

        // create a "dummy" property (uniqueName) that will have a propertyChangeEvent when
        //   one of the dependent properties is changed.
        dependentProperties = (String[]) OAArray.add(String.class, dependentProperties, prop);

        hlDependentProperties = new HubListenerAdapter();
        if (uniqueName == null) uniqueName = "HubFilter" + (UniqueNameCnt++);
        hubMaster.addHubListener(hlDependentProperties, uniqueName, dependentProperties);

        // hashProp has list of property names that this.hubListener is listening to
        if (hashProp == null) hashProp = new Hashtable(7, .75f);
        hashProp.put(uniqueName.toUpperCase(), "");
        refresh();
    }

    /** 
        Method used to know if object should be in filtered hub.  HubFilter will automatically listen to
        Master hub and call this method when needed.
        @return true to include object
        @return false to exclude object
    */
    public abstract boolean isUsed(TYPE object);

    /** This is called when isUsed() is true, to get the object to use. <br>
        This can be overwritten to replace the object with another object.
        @returns object to insert into hub.  Default is to use object.
    */
    public Object getObject(Object object) {
        return object;
    }
    
    
    protected HubListenerAdapter getMasterHubListener() {
        if (hlHubMaster != null) return hlHubMaster;
        
        hlHubMaster = new HubListenerAdapter() {
            /** HubListener interface method, used to update filter. */
            public @Override void afterPropertyChange(HubEvent e) {
                if (bClosed) return;
                if (hashProp != null) {
                    String s = e.getPropertyName();
                    if (hashProp.get(s.toUpperCase()) == null) return;
                }
                update(e.getObject());
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterInsert(HubEvent e) {
                if (bClosed) return;
                afterAdd(e);
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterAdd(HubEvent e) {
                if (bClosed) return;
                if (!hubMaster.isLoading()) {
                    // 20091020 object could have been added to hub, need to leave in
                    // was: update(e.getObject());
                    if (hub == null || !hub.contains(e.getObject())) {
                        update(e.getObject());
                    }
                }
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterRemove(HubEvent e) {
                if (bClosed) return;
                try {
                    if (bServerSideOnly) { 
                        OARemoteThreadDelegate.sendMessages(true);
                    }
                    removeObject(getObject(e.getObject()));
                }
                finally {
                    if (bServerSideOnly) {
                        OARemoteThreadDelegate.sendMessages(false);
                    }
                }
            }
            
            /** HubListener interface method, used to update filter. */
            public @Override void onNewList(HubEvent e) {
                if (bClosed || bNewListFlag) return;
                initialize();
                if (bShareAO) {
                    afterChangeActiveObject(e);
                }
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterSort(HubEvent e) {
                if (bClosed) return;
                if (hubMaster != null) onNewList(e);
            }
            
            public void afterChangeActiveObject(HubEvent e) {
                if (bShareAO && hub != null) {
                    Object obj = HubFilter.this.hubMaster.getAO();
                    if (obj == null || HubFilter.this.hub.contains(obj)) {
                        HubFilter.this.hub.setAO(obj);
                    }
                }
            }
        };
        return hlHubMaster;
    }
    

    protected void setup() {
        if (bClosed) return;
        if (hubMaster == null) {
            if (hub != null) hub.loadAllData(); // required.  Otherwise HubController would be calling this.add as it was loading the objects
            bOAObjectCacheDelegateListener = true;
            Class c = (hub != null) ? hub.getObjectClass() : hubMaster.getObjectClass();
            OAObjectCacheDelegate.addListener(c, getMasterHubListener());
        }
        else {
            hubMaster.addHubListener(getMasterHubListener());
            getMasterHubListener().onNewList(null);
        }

        if (hub != null) {
            hub.addHubListener(this);
        }
        setupLinkHubListener();
    }


    /** 
        listens to link object that filteredHub is linked to, to "temp" add to filteredHub. 
    */
    private HubListener linkHubListener;
    private Hub hubLink;
    private Object objTemp;  // temp object that is the current linkHub object value

    protected void setupLinkHubListener() {
        if (hubLink != null) hubLink.removeHubListener(linkHubListener);
        if (hub == null) return;
        hubLink = hub.getLinkHub();
        if (hubLink == null) return;
        
        linkHubListener = new HubListenerAdapter() {
            public @Override void afterChangeActiveObject(HubEvent evt) {
                if (bClosed) return;
                if (objTemp != null) {
                    if (!isUsed((TYPE) objTemp)) {
                        objTemp = getObject(objTemp);
                        bUpdating = true;
                        if (objTemp != null) removeObject(objTemp);
                        bUpdating = false;
                    }
                    objTemp = null;
                }
                if (bRefreshOnLinkChange) {
                    refresh(); // 20110930 need to refresh since the linkTo hub has changed                
                }   
                Object obj = hubLink.getAO();
                if (obj != null) {
                    obj = HubLinkDelegate.getPropertyValueInLinkedToHub(hub, obj);
                    if (obj != null) {
                        if (!hub.contains(obj)) {
                            bUpdating = true;
                            objTemp = obj;
                            addObject(obj);
                            bUpdating = false;
                        }
                    }
                }
            }
        };
        
        hubLink.addHubListener(linkHubListener);
    }

    private boolean bRefreshOnLinkChange;
    /**
     * Flag to know if refresh needs to be called when/if the linkTo hub AO is changed.
     * This is false by default.
     */
    public void setRefreshOnLinkChange(boolean b) {
        bRefreshOnLinkChange = b;
    }
    
    public void update(Object obj) {
        if (bClosed) return;
        if (bClearing) return;
        try {
            if (bServerSideOnly) { // 20120425
                OARemoteThreadDelegate.sendMessages(true); // so that events will go out, even if OAClientThread
            }
            bUpdating = true;
            
            if (obj != null) {
                if ( hubMaster.getObjectClass().isAssignableFrom(obj.getClass()) ) {
                    if (isUsed((TYPE)obj)) {
                        obj = getObject(obj);
                        if (obj != null && !hub.contains(obj)) {
                            addObject(obj);
                        }
                    }
                    else {
                        obj = getObject(obj);
                        // 2004/08/07 see if object is used by AO in HubLink
                        if (hubLink != null) {
                            Object objx = hubLink.getAO();
                            if (objx != null) {
                                objx = HubLinkDelegate.getPropertyValueInLinkedToHub(hub, objx);
                                if (objx == obj) {
                                    if (objTemp != null) {
                                        if (!isUsed((TYPE)objTemp)) {
                                            objTemp = getObject(objTemp);
                                            if (objTemp != null) removeObject(objTemp);
                                        }
                                        objTemp = null;
                                    }
                                    objTemp = objx;
                                    obj = null;
                                }
                            }
                        }
                        if (obj != null) {
                            removeObject(obj);
                        }
                    }
                }
            }
        }
        finally {
            bUpdating = false;
            if (bServerSideOnly) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
    }
    



    
    /**
     *  Re-evaluate all objects.
     */
    public void refresh() {
        if (bClosed) return;
        initialize();
    }

    public void refresh(Object obj) {
        boolean b = isUsed((TYPE)obj);
        if (hub == null) return;
        if (b) {
            obj = getObject(obj);
            if (obj != null && !hub.contains(obj)) {
                addObject(obj);
            }
        }
        else {
            obj = getObject(obj);
            if (obj != null) {
                removeObject(obj);
            }
        }
    }

    /**
     * Called when initialize if done.
     */
    public void afterInitialize() {
    }
    
    /** HubListener interface method, used to update filter. */
    public void initialize() {
        if (bClosed) return;
        if (bServerSideOnly) { 
            OARemoteThreadDelegate.sendMessages(true); // so that events will go out, even if OAClientThread
        }
        
        HubData hd = null;
        try {
            if (hub != null) {
                hd = hub.data;
                hd.bInFetch = true;
                bClearing = true;
                // clear needs to be called, so that each oaObj.weakHub[] will be updated correctly
                HubAddRemoveDelegate.clear(hub, false, false);  // false:dont set AO to null,  false: send newList event
                bClearing = false;
            }
	        
    	    try {
                OAThreadLocalDelegate.setLoadingObject(true);
                _initialize();
    	        bNewListFlag = true;                   
    	        if (hub != null) HubEventDelegate.fireOnNewListEvent(hub, true);
    	    }
    	    finally {
    	        bNewListFlag = false;	    	        
                OAThreadLocalDelegate.setLoadingObject(false);
    	    }
	    	afterInitialize();
    	}
    	finally {
    		if (hd != null) hd.bInFetch = false;
            if (bServerSideOnly) {
                OARemoteThreadDelegate.sendMessages(false);
            }
    	}
    }    
    
    private void _initialize() {
        if (bClosed) return;
        for (int i=0; hubMaster!=null;i++) {
            Object obj = hubMaster.elementAt(i);
            if (obj == null) break;
            update(obj);
        }
        if (hub == null) return;

        Object obj = hub.getAO();
        if (bShareAO) {
            obj = hubMaster.getAO();
        }
        if (obj != null && !hub.contains(obj)) {
            if (hub.getLinkHub() != null && HubDelegate.isValid(hub)) {
                hub.add(obj);
            }
            else {
                hub.setAO(null);
            }
        }
        
        // 20120716
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub h) {
                if (h != HubFilter.this.hub && h.dataa != HubFilter.this.hub.dataa) {
                    return true;
                }
                return false;
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(this.hub, filter);
        
        //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(hub);
        for (int i=0; i<hubs.length; i++) {
        	if (hubs[i] != this.hub && hubs[i].dataa != hub.dataa) {
                obj = hubs[i].getAO();
        		if (hubs[i].getLinkHub() != null && HubDelegate.isValid(hub)) hub.add(obj);
            	else hubs[i].setAO(null);
        	}
        }
    }

    public Hub getHub() {
        return this.hub;
    }
    
    public Hub getMasterHub() {
        return this.hubMaster;
    }

    /**
        Called to add an object to the Hub.  This can be overwritten
        to handle a different way (ex: different thread) to handle adding to the hub.
    */
    protected void addObject(Object obj) {
        if (bClosed) return;
        try {
        	if (hub != null) hub.add(obj);
        }
        catch (Exception e) {
        }
    }
    /**
        Called to remove an object from the Hub.  This can be overwritten
        to handle a different way (ex: different thread) to handle removing from the hub.
    */
    protected void removeObject(Object obj) {
        if (bClosed) return;
        try {
            if (hub != null) hub.remove(obj);
        }
        catch (Exception e) {
        }
    }
    
    public boolean isSharingAO() {
        return bShareAO;
    }

    // Hub Listener code for filtered Hub
    //    note: this needs to be here so that HubShareDelegate can find HubFilter for a hub
    
    public @Override void afterAdd(HubEvent e) {
        if (hubMaster != null && !bUpdating) {
            if (!hubMaster.contains(e.getObject())) hubMaster.add(e.getObject());
            // 20091020 removed, since an object can be added to the filtered Hub
            //   otherwise, this add could then call remove for the same object
            //     causing problems with other hubs that are wired with this one.
            // A change to any of the filter properties will then update and possibly remove the object
            //   
            // else update(e.getObject());
        }
    }
    public @Override void afterPropertyChange(HubEvent e) {
        if (e.getPropertyName().equalsIgnoreCase("Link")) {
            setupLinkHubListener();
        }
    }
    @Override
    public void afterInsert(HubEvent e) {
        afterAdd(e);
    }
    @Override
    public void afterRemove(HubEvent e) {
        if (hubMaster != null && !bUpdating && !bClearing) {
            HubFilter.this.afterRemoveFromFilteredHub(e.getObject());
        }
    }
    @Override
    public void afterChangeActiveObject(HubEvent e) {
        if (bShareAO && hub != null && hubMaster != null) {
            Object obj = HubFilter.this.hub.getAO();
            if (obj == null || HubFilter.this.hubMaster.contains(obj)) {
                HubFilter.this.hubMaster.setAO(obj);
            }
            
        }
    }
    
    /**
     * Called when an object is removed from the filtered Hub directly.
     * This is used by HubCopy to then remove the object from the Master Hub.
     * @param obj
     */
    protected void afterRemoveFromFilteredHub(Object obj) {
    }
    
}

