/*  Copyright 1999-2016 Vince Via vvia@viaoa.com
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


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.annotation.OAMany;
import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 *  Used by Hub to manage listeners.
 *  Hub listeners are added to an array, and a tree is created for the dependent propertyPaths (if any are used, ex: calc props).
 *  If one of the dependent propertyPath is changed, then a afterPropertyChange is sent for the listener propery.
 *  
 *  NOTE: only the last prop is listened to in for a dependent propertyPath.
 * 
 */
public class HubListenerTree<T> {
    private static Logger LOG = Logger.getLogger(HubListenerTree.class.getName());
    
    private final Hub hub;
    private volatile HubListener[] listeners;
    private final Object lock  = new Object();
    private volatile int lastCount;

    public HubListenerTree(Hub<T> hub) {
        this.hub = hub;
    }
    
    public HubListener<T>[] getHubListeners() {
        return this.listeners;
    }

    public void addListener(HubListener<T> hl) {
        if (hl == null) return;

        synchronized (lock) {
            HubListener.InsertLocation loc = hl.getLocation();
            if (listeners == null || listeners.length==0 || loc == HubListener.InsertLocation.LAST || (loc == null && lastCount==0)) {
                if (loc == HubListener.InsertLocation.LAST) lastCount++;
                listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
            }
            else if (loc == HubListener.InsertLocation.FIRST) {
                listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, 0);
            }
            else {
                // insert before first last
                boolean b = false;
                for (int i=listeners.length-1; i<=0; i--) {
                    if (listeners[i].getLocation() != HubListener.InsertLocation.LAST) {
                        listeners = (HubListener []) OAArray.insert(HubListener.class, listeners, hl, i+1);
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    listeners = (HubListener []) OAArray.add(HubListener.class, listeners, hl);
                }
            }
            if (listeners.length % 50 == 0) {
                LOG.fine("HubListenerTree.listeners.size()=" +listeners.length+", hub="+hub);
            }
        }
    }   
    
    public void addListener(HubListener hl, String property) {
        if (hl == null) return;
        addListener(hl, property, null);
    }

    public void addListener(HubListener hl, final String property, String[] dependentPropertyPaths) {
        if (hl == null) return;
        
        try {
            OAThreadLocalDelegate.setHubListenerTree(true);
            addListener(hl, property); // this will check for dependent calcProps
            
            OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass());
            String[] calcProps = null;
            for (OACalcInfo ci : oi.getCalcInfos()) {
                if (ci.getName().equalsIgnoreCase(property)) {
                    calcProps = ci.getProperties();
                    break;
                }
            }       
            if (calcProps != null && calcProps.length > 0) {
                addDependentListeners(hl, property, dependentPropertyPaths);
            }

            // now add the additional dependent properties
            if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
                addDependentListeners(hl, property, dependentPropertyPaths);
            }
        }
        finally {
            OAThreadLocalDelegate.setHubListenerTree(false);
            OAThreadLocalDelegate.setIgnoreTreeListenerProperty(null);
        }
    }    
    
    private ArrayList<ListenerInfo> alListenerInfo;
    private static class ListenerInfo {
        HubListener hl;
        String propertyName;
        String[] calcPropertyPaths;
        String[] dependentPropertyPaths;

        ArrayList<DependentPropertyInfo> calcs;
        ArrayList<DependentPropertyInfo> depends;
        
        ArrayList<DependentPropertyInfo> alDependentPropertyInfos;
    }
    
    private ArrayList<DependentPropertyInfo> alDependentPropertyInfo;
    private static class DependentPropertyInfo {
        String dependentPropertyPath;
        OATrigger trigger;
        HubListener extraHubListener;
        HashSet<String> hsListenToProperties;
        ArrayList<String> alSendEventPropertyName;
    }

    
    
    private void addDependentListeners(final HubListener<T> hl, final String propertyName, final String[] dependentPropertyPaths) {
        if (dependentPropertyPaths == null || dependentPropertyPaths.length == 0) return;

        ListenerInfo li = new ListenerInfo();
        li.hl = hl;
        li.propertyName = propertyName;
        
        
        HashSet<String> hs = new HashSet<String>(); // extra hubListener for first property(s)
        for (String dpp : dependentPropertyPaths) {
            String s = _addDependentListener(li, dpp);
            
            
            
            if (hs.size() > 0) {
                
            }
        }
        

        
        // create a hublistener for first prop in dependentPP, if they are ONE or propName
        li.hubListenerExtra = new HubListenerAdapter() {
            public void afterPropertyChange(HubEvent e) {
                String prop = e.getPropertyName();
                if (prop == null) return;
                
                if (hsListenToProps.contains(prop.toUpperCase())) {
                    HubEventDelegate.fireCalcPropertyChange(root.hub, e.getObject(), origPropertyName);
                }
            }
        };
        addListener(li.hubListenerExtra);
    }
    
    /**
     * @param hsListenToProps hashSet of prop names that can be listened to, so that the trigger wont have to.
     */
    private HashSet<String> _addDependentListeners(HashSet<String> hsListenToProps, final ListenerInfo li, final String origPropertyName, final String[] dependentPropertyPaths, final boolean bActiveObjectOnly, final boolean bAllowBackgroundThread) {
        if (dependentPropertyPaths == null) return hsListenToProps;

        boolean b = false;
        for (String spp : dependentPropertyPaths) {
            if (!OAString.isEmpty(spp)) {
                b = true;
                break;
            }
        }
        if (!b) return hsListenToProps;
        
//qqqqqqqqqq might already be listening  qqqqqqqqqqqqqq 
        
        // check to see if the dependPPs first prop can be listened to, so that a trigger wont have to be used.
        boolean bNeedsTrigger = false;
        for (String spp : dependentPropertyPaths) {
            if (OAString.isEmpty(spp)) continue;

            OAPropertyPath pp = new OAPropertyPath(root.hub.getObjectClass(), spp);
            String[] props = pp.getProperties();
            OALinkInfo[] lis = pp.getLinkInfos();
            
            if ((lis.length > 0 && lis[0].getType() == OALinkInfo.ONE) || (lis.length == 0 && props.length == 1)) {
                if (hsListenToProps == null) hsListenToProps = new HashSet<String>();
                hsListenToProps.add(props[0].toUpperCase());
                if (props.length > 1) bNeedsTrigger = true;
                
                if (lis.length == 0) {
                    // could be a calcProp
                    OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(root.hub.getObjectClass());
                    String[] calcProps = null;
                    for (OACalcInfo ci : oi.getCalcInfos()) {
                        if (ci.getName().equalsIgnoreCase(props[0])) {
                            // make recursive
                            _addDependentListeners(hsListenToProps, hl, origPropertyName, ci.getProperties(), bActiveObjectOnly, bAllowBackgroundThread);
                            break;
                        }
                    }       
                }
            }
            else {
                bNeedsTrigger = true;
            }
        }
        
        if (!bNeedsTrigger) return hsListenToProps;

        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                HubEventDelegate.fireCalcPropertyChange(root.hub, obj, origPropertyName);
            }
        };
        
//qqqqqqqqqqq create an option give the hub to trigger, so it can send triggers to only this hub        
        OATrigger t = new OATrigger(root.hub.getObjectClass(), tl, dependentPropertyPaths, true, false, false);
        
        synchronized (hmTrigger) {
            ArrayList<OATrigger> al = hmTrigger.get(hl);;
            if (al == null) {
                al = new ArrayList<OATrigger>();
                hmTrigger.put(hl, al);
            }
            al.add(t);
        }
        
        OATriggerDelegate.createTrigger(t, true);
        
        return hsListenToProps;
    }
    

    public void removeListener(Hub thisHub, HubListener hl) {
        removeListener(hl);
    }
    public void removeListener(HubListener hl) {
        if (hl == null) return;

        synchronized (root) {
            HubListener[] hold = listeners; 
            listeners = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, hl);
            if (hold == listeners) {
                return;
            }
            --ListenerCount;
            if (hl.getLocation() == HubListener.InsertLocation.LAST) lastCount--;
        }
        
        synchronized (alListenerInfo) {
            for (ListenerInfo lix : alListenerInfo) {
                if (lix.alHubListener.contains(hl)) {
                    lix.alHubListener.remove(hl);
                    if (lix.alHubListener.size() == 0) {
                        if (lix.alTrigger != null) {
                            for (OATrigger t : lix.alTrigger) {
                                OATriggerDelegate.removeTrigger(t);
                            }
                        }
                        if (lix.hubListenerExtra != null) {
                            removeListener(lix.hubListenerExtra);
                        }
                        alListenerInfo.remove(lix);
                    }
                    break;
                }
            }
        }
    }    
    
}

