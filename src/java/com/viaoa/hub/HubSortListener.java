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

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.util.*;


//NOte:  this was called HubSorter.java qqqqqqqqqqq changed it to be a listener for Hub.data.sorter
// 20101219 was using detailHubs to listen for changes.  It now uses just a hub property listener, which
//          has been changed to use HubMerger to listen to any dependent propertyPaths
/**
    HubSortListener is used to keep a Hub sorted by the Hubs sort/select order.  Used internally by
    Hub.sort method.
    
    Note:
    For oa.cs, each client will maintain their own sorting.  If a sort property is changed, then each client will resort,
    without any messages going to/from server.   
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see Hub#sort(String,boolean) Hub.sort
    @see OAComparator that is created based on propertyPaths 
*/
public class HubSortListener extends HubListenerAdapter implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    String sortPropertyName = null;  // uniquely generated PropertyName used by hubListener(..,prop) based on property and sort properties 
    private String[] sortPropertyPaths;  // parsed sort strings, used as dependent propertyPaths for hubListener
    String propertyPaths;  // orig sort string
    
    Hub hub;
    Comparator comparator;
    boolean bAscending;

    /**
      Used by Hub for sorting objects.
      @param propertyPaths list of property paths ( comma or space delimited).  Can include "asc" or "desc" after
      a propertyPath name.
      All property paths will be listened to, so that changes to them will updated the sorted Hub.
      @see OAComparator#OAComparator
      @see Hub#sort instead of using this object directly.
    */
    public HubSortListener(Hub hub, String propertyPaths, boolean bAscending) {
        this(hub, null, propertyPaths, bAscending);
    }
    public HubSortListener(Hub hub, String propertyPaths) {
        this(hub, null, propertyPaths, true);
    }


    public HubSortListener(Hub hub, Comparator comparator, boolean bAscending) {
        this(hub, comparator, null, bAscending);
    }
    public HubSortListener(Hub hub, Comparator comparator) {
        this(hub, comparator, null, true);
    }


    public HubSortListener(Hub hub, Comparator comparator, String propertyPaths, boolean bAscending) {
        this.hub = hub;
        this.comparator = comparator;
        this.propertyPaths = propertyPaths;
        this.bAscending = bAscending;

        if (comparator != null) {
            hub.addHubListener(this); 
        }
        else {
            setupPropertyPaths();
            if (this.comparator == null) {
                this.comparator = new OAComparator(hub.getObjectClass(), propertyPaths, bAscending);
            }
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public String[] getPropeties() {
    	return sortPropertyPaths;
    }

    
    protected void setupPropertyPaths() {
    	if (propertyPaths == null) return;

    	StringTokenizer st = new StringTokenizer(propertyPaths, ", ", true);
        
        sortPropertyPaths = null;
        sortPropertyName = null;
        boolean bAllowType = false;
        
        for ( ; st.hasMoreElements() ; ) {
            String prop = (String) st.nextElement();
            if (prop.equals(" ")) {
                bAllowType = true;
                continue;
            }
            if (prop.equals(",")) {
                bAllowType = false;
                continue;
            }
            if (prop.equalsIgnoreCase("desc")) {
                if (bAllowType) continue;
                // else could be property name
            }
            if (prop.equalsIgnoreCase("asc")) {
                if (bAllowType) continue;
                // else could be property name
            }
            try {
            	OAReflect.getMethods(hub.getObjectClass(), prop);
            }
            catch (RuntimeException e) {
                // ignore
                continue;
            }
            
            sortPropertyPaths = (String[]) OAArray.add(String.class, sortPropertyPaths, prop);
            
            if (sortPropertyName == null) sortPropertyName = "";
            else sortPropertyName += "_";
            sortPropertyName += prop.toUpperCase();
        }

        if (sortPropertyName != null) {
            if (sortPropertyPaths != null && sortPropertyPaths.length == 1 && sortPropertyName.indexOf('.') < 0) {
                hub.addHubListener(this, sortPropertyName); // only sorting on one property in the Hub
            }
            else {
                // use a "dummy" name that with get notified when one of the sortPropertyPaths change
                //   dont use '.' in name
                sortPropertyName = "HUBSORT_" + sortPropertyName;  
                sortPropertyName = sortPropertyName.replace('.', '_');  // cant have '.' in property name
                hub.addHubListener(this, sortPropertyName, sortPropertyPaths);
            }
        }
    }

    public void close() {
        hub.removeHubListener(this);
    }

    // if detail hub changes then one of the properties has changed
    public @Override void onNewList(HubEvent e) {
        Hub h = (Hub) e.getSource();
        if (h == hub) {
            // 20101009 another thread could be making Hub changes, so this could fail - adding try..catch
            for (int i=0; i<3; i++) {
                try {
                    HubSortDelegate.resort(hub);
                    break;
                }
                catch (Exception ex) {
                }
            }
        }
    }
    
    public @Override void afterPropertyChange(HubEvent e) {
        String s = e.getPropertyName();
        if (s != null && s.equalsIgnoreCase(sortPropertyName)) {
            try {
                OAThreadLocalDelegate.setSuppressCSMessages(true);  // each client will handle it's own sorting
                HubAddRemoveDelegate.sortMove(hub, e.getObject());
            }
            finally {
                OAThreadLocalDelegate.setSuppressCSMessages(false);
            }
        }
    }

}



