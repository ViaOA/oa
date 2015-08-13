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

        if (comparator == null) {
            setupPropertyPaths();
            if (this.comparator == null) {
                this.comparator = new OAComparator(hub.getObjectClass(), propertyPaths, bAscending);
            }
        }
        hub.addHubListener(this); 
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
    
    private boolean bCallingSortMove; // 20141205
    public @Override void afterPropertyChange(HubEvent e) {
        if (bCallingSortMove) return;
        String s = e.getPropertyName();
        if (s != null && s.equalsIgnoreCase(sortPropertyName)) {
            try {
                bCallingSortMove = true;
                OAThreadLocalDelegate.setSuppressCSMessages(true);  // each client will handle it's own sorting
                HubAddRemoveDelegate.sortMove(hub, e.getObject());
            }
            finally {
                bCallingSortMove = false;
                OAThreadLocalDelegate.setSuppressCSMessages(false);
            }
        }
    }

}



