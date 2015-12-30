/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.hub;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.*;

/**
    HubFilter is used to create a Hub that has objects that are filtered from another Hub.
    <p>
    All that is needed is to subclass the HubFilter and implement the "isUsed()" method 
    to know if an object is to be included in the filtered Hub.
    <p>
    Example<br>
    <pre>
    Hub hubFiltered = new Hub(Employee.class)
    new HubFilter(hubAllEmployees, hubFiltered) {
        public boolean isUsed(Object obj) {
            // .... code to check if object should be added to hubFilter
        }
    };
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/

public class HubFilter<T> extends HubListenerAdapter<T> implements java.io.Serializable, OAFilter<T> {
    private static Logger LOG = Logger.getLogger(HubFilter.class.getName());
    private static final long serialVersionUID = 1L;

    protected Hub<T> hubMaster;
    protected WeakReference<Hub<T>> weakHub;
    private HashSet<String> hashProp;
    private boolean bShareAO;
    private volatile boolean bClosed;
    private boolean bServerSideOnly;

    // listener setup for dependent properties
    private static AtomicInteger aiUniqueNameCnt = new AtomicInteger();
    private String uniqueName;
    private String[] dependentProperties;
    private HubListener hlDependentProperties;
    
    public boolean DEBUG;
    private boolean bOAObjectCacheDelegateListener;
    private HubListenerAdapter<T> hlHubMaster;
    private volatile boolean bNewListFlag;
    
    private final AtomicInteger aiClearing = new AtomicInteger();
    private final AtomicInteger aiUpdating = new AtomicInteger();
    
    /** 
        Create a new HubFilter using two supplied Hubs.
        @param hubMaster hub with complete list of objects.
        @param hub that stores filtered objects. 
    */
    public HubFilter(Hub<T> hubMaster, Hub<T> hub) {
        this(false, hubMaster, hub, false, false, null, null);
    }

    public HubFilter(Hub<T> hubMaster, Hub<T> hub, OAFilter filter) {
        this(false, hubMaster, hub, false, false, filter, null);
    }
    
    public HubFilter(Hub<T> hubMaster, Hub<T> hub, boolean bShareAO) {
        this(false, hubMaster, hub, bShareAO, false, null, null);
    }
    public HubFilter(Hub<T> hubMaster, Hub<T> hub, boolean bShareAO, OAFilter filter) {
        this(false, hubMaster, hub, bShareAO, false, filter, null);
    }

    public HubFilter(Hub<T> hubMaster, Hub<T> hub, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, false, false, null, dependentPropertyPaths);
    }    
    public HubFilter(Hub<T> hubMaster, Hub<T> hub, boolean bShareAO, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, bShareAO, false, null, dependentPropertyPaths);
    }    
    public HubFilter(Hub<T> hubMaster, Hub<T> hub, boolean bShareAO, boolean bRefreshOnLinkChange, String... dependentPropertyPaths) {
        this(false, hubMaster, hub, bShareAO, bRefreshOnLinkChange, null, dependentPropertyPaths);
    }
    
    public HubFilter(boolean bObjectCache, Hub<T> hubMaster, Hub<T> hub, boolean bShareAO, boolean bRefreshOnLinkChange, OAFilter filter, String... dependentPropertyPaths) {
        // note: bObjectCache will allow hubMaster to be null, which will then use the oaObjectCache
        if (!bObjectCache && hubMaster == null) {
            throw new IllegalArgumentException("hubMaster can not be null if bObjectCache=false");
        }
        if (hub == null) {  // 20131129 hub can now be null, used by Triggers
            // throw new IllegalArgumentException("hub can not be null");
        }
        this.hubMaster = hubMaster;
        this.weakHub = new WeakReference(hub);
        this.bShareAO = bShareAO;
        this.bRefreshOnLinkChange = bRefreshOnLinkChange;
        if (filter != null) {
            alFilters = new ArrayList<OAFilter>();
            alFilters.add(filter);
        }
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
    public HubFilter(Hub<T> hub) {
        this(true, null, hub, false, false, null);
    }

    public Hub<T> getHub() {
        Hub<T> h = weakHub.get();
        if (h == null) {
            close();
        }
        return h;
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
        if (bClosed) return;
        this.bClosed = true;
        
        Hub<T> hub = getHub();
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
            if (hubMaster != null) hubMaster.removeHubListener(hlDependentProperties);
            hlDependentProperties = null;
        }
    }
    

    /**
        Property names to listen for changes.
        @param prop property name or property path (from Hub)
        @see #setRefreshOnLinkChange(boolean) to refresh list when linkTo Hub AO changes
    */
    public void addDependentProperty(String prop) {
        _addDependentProperty(prop, true);
    }
    private void _addDependentProperty(String prop, boolean bRefesh) {
        if (bClosed) return;
        _addProperty(prop, false);
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
        
        if (uniqueName == null) uniqueName = "HubFilter" + (aiUniqueNameCnt.incrementAndGet());
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
        
        if (uniqueName == null) uniqueName = "HubFilter" + (aiUniqueNameCnt.incrementAndGet());
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
        _addProperty(prop, true);
    }
    private void _addProperty(String prop, boolean bRefresh) {
        if (bClosed) return;
        if (prop == null || prop.length() == 0) return;
        
        if (hubMaster != null && hlDependentProperties != null) {
            hubMaster.removeHubListener(hlDependentProperties);
        }

        // create a "dummy" property (uniqueName) that will have a propertyChangeEvent when
        //   one of the dependent properties is changed.
        dependentProperties = (String[]) OAArray.add(String.class, dependentProperties, prop);

        hlDependentProperties = new HubListenerAdapter();
        if (uniqueName == null) uniqueName = "HubFilter" + (aiUniqueNameCnt.incrementAndGet());
        if (hubMaster != null) hubMaster.addHubListener(hlDependentProperties, uniqueName, dependentProperties);

        // hashProp has list of property names that this.hubListener is listening to
        if (hashProp == null) hashProp = new HashSet(5, .75f);
        hashProp.add(uniqueName.toUpperCase());
        if (bRefresh) refresh();
    }

    /** 
        Method used to know if object should be in filtered hub.  HubFilter will automatically listen to
        Master hub and call this method when needed.
        @return true to include object (default)
        @return false to exclude object
    */
    public boolean isUsed(T object) {
        if (alFilters == null) return true;
        boolean bIsUsed = true;
        for (OAFilter f : alFilters) {
            bIsUsed = f.isUsed(object);
            if (!bIsUsed) break;
        }
        return bIsUsed;
    }

    /** This is called when isUsed() is true, to get the object to use. <br>
        This can be overwritten to replace the object with another object.
        @returns object to insert into hub.  Default is to use object.
    */
    public T getObject(T object) {
        return object;
    }
    
    
    protected HubListenerAdapter<T> getMasterHubListener() {
        if (hlHubMaster != null) return hlHubMaster;
        
        hlHubMaster = new HubListenerAdapter<T>() {
            /** HubListener interface method, used to update filter. */
            public @Override void afterPropertyChange(HubEvent<T> e) {
                if (bClosed) return;
                if (hashProp != null) {
                    String s = e.getPropertyName();
                    if (!hashProp.contains(s.toUpperCase())) return;
                }
                update(e.getObject());
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterInsert(HubEvent<T> e) {
                if (bClosed) return;
                afterAdd(e);
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterAdd(HubEvent<T> e) {
                if (bClosed) return;
                if (hubMaster == null || !hubMaster.isLoading()) {
                    Hub<T> hub = getHub();
                    if (hub != null && !hub.contains(e.getObject())) {
                        if (hubMaster == null || hubMaster.contains(e.getObject())) {
                            try {
                                aiUpdating.incrementAndGet();
                                update(e.getObject());
                            }
                            finally {
                                aiUpdating.decrementAndGet();
                            }
                        }
                    }
                }
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterRemove(HubEvent<T> e) {
                if (bClosed) return;
                try {
                    if (bServerSideOnly) { 
                        OARemoteThreadDelegate.sendMessages(true);
                    }
                    if (hubMaster == null || !hubMaster.contains(e.getObject())) {
                        removeObject(getObject(e.getObject()));
                    }
                }
                finally {
                    if (bServerSideOnly) {
                        OARemoteThreadDelegate.sendMessages(false);
                    }
                }
            }
            
            /** HubListener interface method, used to update filter. */
            public @Override void onNewList(HubEvent<T> e) {
                if (bClosed || bNewListFlag) {
                    return;
                }
                initialize();
            }

            /** HubListener interface method, used to update filter. */
            public @Override void afterSort(HubEvent<T> e) {
                if (bClosed) return;
                if (hubMaster != null) onNewList(e);
            }
            
            public void afterChangeActiveObject(HubEvent<T> e) {
                Hub<T> hub = getHub();
                if (!bShareAO || hub == null || hubMaster == null) return;
                
                Object obj = HubFilter.this.hubMaster.getAO();
                if (obj != null && !hub.contains(obj)) {
                    obj = null;
                }
                bIgnoreSettingAO = true;
                hub.setAO(obj);
                bIgnoreSettingAO = false;
            }
        };
        return hlHubMaster;
    }

    private boolean bIgnoreSettingAO;
    
    protected void setup() {
        if (bClosed) return;
        Hub<T> hub = getHub();
        if (hub == null) {
            return;
        }
        if (hubMaster == null) {
            hub.loadAllData(); // required.  Otherwise HubController would be calling this.add as it was loading the objects
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
        Hub<T> hub = getHub();
        if (hub == null) return;
        if (hubLink != null) hubLink.removeHubListener(linkHubListener);
        hubLink = hub.getLinkHub();
        if (hubLink == null) return;
        
        linkHubListener = new HubListenerAdapter() {
            public @Override void afterChangeActiveObject(HubEvent evt) {
                Hub<T> hub = getHub();
                if (hub == null || bClosed) return;
                if (objTemp != null) {
                    if (!isUsed((T) objTemp)) {
                        objTemp = getObject((T)objTemp);
                        try {
                            aiUpdating.incrementAndGet();
                            if (objTemp != null) removeObject((T)objTemp);
                        }
                        finally {
                            aiUpdating.decrementAndGet();
                        }
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
                            try {
                                aiUpdating.incrementAndGet();
                                objTemp = obj;
                                addObject((T)obj);
                            }
                            finally {
                                aiUpdating.decrementAndGet();
                            }
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
    
    public void update(T obj) {
        Hub<T> hub = getHub();
        if (hub == null || bClosed) return;
        if (aiClearing.get() != 0) return;
        try {
            if (bServerSideOnly) { // 20120425
                OARemoteThreadDelegate.sendMessages(true); // so that events will go out, even if OAClientThread
            }
            aiUpdating.incrementAndGet();
            obj = getObject(obj);
            if (obj != null) {
                if (hubMaster == null || hubMaster.getObjectClass().isAssignableFrom(obj.getClass()) ) {
                    if (isUsed(obj)) {
                        if (!hub.contains(obj)) {
                            if (obj == objTemp) objTemp = null;
                            if (hubMaster == null || hubMaster.contains(obj)) {
                                addObject(obj);
                            }
                        }
                    }
                    else {
                        // 2004/08/07 see if object is used by AO in HubLink
                        if (hubLink != null) {
                            Object objx = hubLink.getAO();
                            if (objx != null) {
                                objx = HubLinkDelegate.getPropertyValueInLinkedToHub(hub, objx);
                                objx = getObject((T)objx);
                                if (obj == objx) {
                                    if (obj != objTemp) {
                                        if (objTemp != null) {
                                            if (!isUsed((T)objTemp)) {
                                                removeObject((T)objTemp);
                                            }
                                        }
                                        objTemp = obj;
                                        if (hubMaster == null || hubMaster.contains(obj)) {
                                            addObject(obj);
                                        }
                                    }
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
            aiUpdating.decrementAndGet();
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

    public void refresh(T obj) {
        Hub<T> hub = getHub();
        if (hub == null) return;
        boolean b = isUsed(obj);
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
     * Called when initialize is done.
     */
    public void afterInitialize() {
    }
    
    
    private AtomicInteger aiInitializeCount = new AtomicInteger();
    
    /** HubListener interface method, used to update filter. */
    public void initialize() {
        Hub<T> hub = getHub();
        if (hub == null || bClosed) return;
        if (bServerSideOnly) { 
            OARemoteThreadDelegate.sendMessages(true); // so that events will go out, even if OAClientThread
        }
        
        final int cnt = aiInitializeCount.incrementAndGet();

        boolean bCompleted = false;
        HubData hd = null;
        try {
            if (hub != null) {
                hd = hub.data;
                hd.setInFetch(true);
                try {
                    aiClearing.incrementAndGet();
                    // clear needs to be called, so that each oaObj.weakHub[] will be updated correctly
                    bIgnoreSettingAO = true;
                    HubAddRemoveDelegate.clear(hub, false, false);  // false:dont set AO to null,  false: send newList event
                    objTemp = null;
                    bIgnoreSettingAO = false;
                }
                finally {
                    aiClearing.decrementAndGet();
                }
            }
            
            try {
                OAThreadLocalDelegate.setLoadingObject(true);
                bCompleted = _initialize(cnt);
                if (hub != null && bCompleted) {
                    bNewListFlag = true;                   
                    HubEventDelegate.fireOnNewListEvent(hub, true);
                }
            }
            finally {
                if (hub != null && bCompleted) bNewListFlag = false;                   
                OAThreadLocalDelegate.setLoadingObject(false);
    	    }
    	}
    	finally {
    		if (hd != null && bCompleted) hd.setInFetch(false);
            if (bServerSideOnly) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
        if (bCompleted) afterInitialize();
    }    
    
    private boolean _initialize(final int cnt) {
        Hub<T> hub = getHub();
        if (hub == null) return false;
        if (bClosed) return false;
        for (int i=0; hubMaster!=null;i++) {
            T obj = hubMaster.elementAt(i);
            if (obj == null) break;
            if (aiInitializeCount.get() != cnt) return false;
            update(obj);
        }
        
        // get linkToHub.prop value
        if (hubLink != null) {
            Object objx = hubLink.getAO();
            if (objx != null) {
                objx = HubLinkDelegate.getPropertyValueInLinkedToHub(hub, objx);
                if (objx != null) {
                    objx = getObject((T)objx);
                    if (objx != null && !hub.contains(objx)) {
                        addObject((T)objx);
                    }
                }
            }
            hub.setAO(objx);
            if (bShareAO && hubMaster != null) {
                if (hubMaster.getLinkHub() == null) hubMaster.setAO(objx);
            }
        }
        
        if (bShareAO && hubLink == null && hubMaster != null) {
            T obj = hubMaster.getAO();
            if (obj != null && !hub.contains(obj)) {
                obj = null;
            }
            hub.setAO(obj);
        }
        return true;
    }

    
    public Hub getMasterHub() {
        return this.hubMaster;
    }

    /**
        Called to add an object to the Hub.  This can be overwritten
        to handle a different way (ex: use different thread) to handle adding to the hub.
    */
    protected void addObject(T obj) {
        Hub<T> hub = getHub();
        if (hub == null || bClosed) return;
        hub.add(obj);
    }
    
    /**
        Called to remove an object from the Hub.  This can be overwritten
        to handle a different way (ex: use different thread) to handle removing from the hub.
    */
    protected void removeObject(T obj) {
        Hub<T> hub = getHub();
        if (hub == null || bClosed) return;
        hub.remove(obj);
    }
    
    public boolean isSharingAO() {
        return bShareAO;
    }

    // Hub Listener code for filtered Hub
    //    note: this needs to be here so that HubShareDelegate can find HubFilter for a hub
    
    public @Override void afterAdd(HubEvent<T> e) {
        afterAdd(e.getObject());
    }
    public void afterAdd(T obj) {
        if (aiUpdating.get() == 0) {
            if (hubMaster != null && !hubMaster.contains(obj)) {
                hubMaster.add(obj);
            }
        }        
    }
    
    public @Override void afterPropertyChange(HubEvent<T> e) {
        if (e.getPropertyName().equalsIgnoreCase("Link")) {
            setupLinkHubListener();
        }
    }
    @Override
    public void afterInsert(HubEvent<T> e) {
        afterAdd(e);
    }
    
    @Override
    public void afterRemove(HubEvent<T> e) {
        if (aiUpdating.get() == 0 && aiClearing.get() == 0) {
            afterRemove(e.getObject());
        }
    }
    public void afterRemove(T obj) {
        if (hubMaster != null) {
            HubFilter.this.afterRemoveFromFilteredHub(obj);
        }
    }
    
    @Override
    public void afterChangeActiveObject(HubEvent<T> e) {
        Hub<T> hub = getHub();
        if (bShareAO && hub != null && hubMaster != null) {
            Object obj = hub.getAO();
            if (obj != null && !HubFilter.this.hubMaster.contains(obj)) obj = null;
            if (!bIgnoreSettingAO) {
                HubFilter.this.hubMaster.setAO(obj);
            }
        }
    }
    
    /**
     * Called when an object is removed from the filtered Hub directly.
     * This is used by HubCopy to then remove the object from the Master Hub.
     * By default, this does nothing (it does not remove from hubMaster)
     * @param obj
     */
    protected void afterRemoveFromFilteredHub(T obj) {
    }


    private ArrayList<OAFilter> alFilters;
    private int iBlockPos = -1;

    public void startBlock() {
        iBlockPos = alFilters == null ? 0 : alFilters.size();
    }
    public void endBlock() {
        if (iBlockPos >= 0 && alFilters != null) {
            int x = alFilters.size();
            if (x > iBlockPos) {
                OAFilter[] filters = new OAFilter[x-iBlockPos];
                for (int i=iBlockPos; i<x; i++) {
                    filters[i-iBlockPos] = alFilters.remove(iBlockPos);
                }
                OAFilter f = new OABlockFilter(filters);
                addFilter(f);
            }
        }
        iBlockPos = -1;
    }
    public void clearFilters() {
        alFilters = null;
    }

    public void addFilter(OAFilter<T> filter) {
        if (alFilters == null) alFilters = new ArrayList<OAFilter>();
        alFilters.add(filter);
        refresh();
    }
    
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAEqualFilter(value));
    }
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addNotEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OANotEqualFilter(value));
    }

    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addBetweenOrEqualFilter(final String propPath, final Object value1, final Object value2) {
        _addFilter(propPath, new OABetweenOrEqualFilter(value1, value2));
    }
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addBetween(final String propPath, final Object value1, final Object value2) {
        _addFilter(propPath, new OABetweenFilter(value1, value2));
    }
    
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addNullFilter(final String propPath) {
        _addFilter(propPath, new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                return obj == null;
            }
        });
    }
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addNotNullFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                return obj != null;
            }
        });
    }
  
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addEmptyFilter(final String propPath) {
        _addFilter(propPath, new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                return OAString.isEmpty(obj);
            }
        });
    }
    /**
     * The filter will be true if there is a least one matching value in the property path;
     */
    public void addNotEmptyFilter(final String propPath) {
        _addFilter(propPath, new OAFilter() {
            @Override
            public boolean isUsed(Object obj) {
                return !OAString.isEmpty(obj);
            }
        });
    }
    
    
    /**
     * Create a filter that is used on every object for this finder.
     * The filter will be true if there is a least one matching value in the property path;
     * @param propPath property path from this Finder from object to the object that will be compared.
     * @param value value to compare with using OACompare.isLike(..).
     */
    public void addLikeFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALikeFilter(value));
    }
    public void addNotLikeFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OANotLikeFilter(value));
    }
    public void addGreaterFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAGreaterFilter(value));
    }
    public void addGreaterOrEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OAGreaterOrEqualFilter(value));
    }
    public void addLessFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALessFilter(value));
    }
    public void addLessOrEqualFilter(final String propPath, final Object value) {
        _addFilter(propPath, new OALessOrEqualFilter(value));
    }
    public void addBetweenFilter(final String propPath, final Object value1, final Object value2) {
        _addFilter(propPath, new OABetweenFilter(value1, value2));
    }
    
    /**
     * Create a filter that is used on every object.
     */
    private void _addFilter(final String propPath, final OAFilter filter) {
        if (filter == null) return;
        _addDependentProperty(propPath, false);
        
        OAFilter<T> f;
        if (OAString.isEmpty(propPath)) {
            f = filter;
        }
        else if (OAString.dcount(propPath, '.') == 1) {
            f = new OAFilter<T>() {
                @Override
                public boolean isUsed(T obj) {
                    if (obj == null) return false;
                    Object objx = ((OAObject)obj).getProperty(propPath);
                    return filter.isUsed(objx);
                }
            };
        }
        else {
            int dcnt = OAString.dcount(propPath, '.');
            String prop = OAString.field(propPath, '.', 1, dcnt-1);
            final OAFinder find = new OAFinder(prop);
            final String propLast = OAString.field(propPath, '.', dcnt);

            f = new OAFilter() {
                @Override
                public boolean isUsed(Object obj) {
                    if (obj == null) return false;
                    Object objx = ((OAObject)obj).getProperty(propLast);
                    return filter.isUsed(objx);
                }
            };
            
            find.addFilter(f);
            
            f = new OAFilter() {
                public boolean isUsed(Object obj) {
                    return find.canFindFirst((OAObject)obj);
                }
            };
        }
        addFilter(f);
    }
}
