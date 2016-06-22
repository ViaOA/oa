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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.viaoa.object.*;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAPropertyPath;

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
    private volatile int cntLast;  // listeners that are flagged to be last

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
            if (listeners == null || listeners.length==0 || loc == HubListener.InsertLocation.LAST || (loc == null && cntLast==0)) {
                if (loc == HubListener.InsertLocation.LAST) cntLast++;
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
    
    private static class ListenerInfo {
        HubListener hl;
        ArrayList<String> alExtraListenerProperties;
        ArrayList<OATrigger> alTrigger;
    }
    // list of HubListeners that have dependent prop listeners or triggers created.
    private ArrayList<ListenerInfo> alListenerInfo;
    
    private static class TriggerInfo {
        String propertyPath;
        OATrigger trigger;
    }
    
    private ConcurrentHashMap<String, ArrayList<String>> hsExtraProperties = new ConcurrentHashMap<String, ArrayList<String>>();  // prop.upper
    private HubListener hlExtra; // extra hublistener that will listen to any of the local propertys or one links (not many)
    private HashMap<String, OATrigger> hsTrigger;  // propertyPath.upper
    
    private void addDependentListeners(final HubListener<T> hl, final String propertyName, final String[] dependentPropertyPaths) {
        if (dependentPropertyPaths == null || dependentPropertyPaths.length == 0) return;
        synchronized (lock) {
            _addDependentListeners(hl, propertyName, dependentPropertyPaths);
        }
    }
    private void _addDependentListeners(final HubListener<T> hl, final String propertyName, final String[] dependentPropertyPaths) {
        ListenerInfo li = new ListenerInfo();
        li.hl = hl;
        boolean bUsed = false;
        
        HashSet<String> hs = new HashSet<String>(); // extra hubListener for first property(s)
        for (String dpp : dependentPropertyPaths) {
            if (dpp == null || dpp.length() == 0) continue;
            
            _addDependentListener(li, propertyName, dpp);
            if (!bUsed) {
                if (alListenerInfo == null) alListenerInfo = new ArrayList<HubListenerTree.ListenerInfo>();
                alListenerInfo.add(li);
                bUsed = true;
            }
        }
        
        if (hlExtra == null && hsExtraProperties.size() > 0) {
            hlExtra = new HubListenerAdapter() {
                public void afterPropertyChange(HubEvent e) {
                    String prop = e.getPropertyName();
                    if (prop == null) return;
                    
                    ArrayList<String> al = hsExtraProperties.get(prop.toUpperCase()); 
                    if (al != null) {
                        for (String s : al) {
                            HubEventDelegate.fireCalcPropertyChange(hub, e.getObject(), s);
                        }
                    }
                }
            };
            addListener(hlExtra);
        };
    }

    private void _addDependentListener(final ListenerInfo li, final String propertyName, final String dependentPropertyPath) {
        OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), dependentPropertyPath);
        String[] props = pp.getProperties();
        OALinkInfo[] lis = pp.getLinkInfos();
        
        if ((lis.length > 0 && lis[0].getType() == OALinkInfo.ONE) || (lis.length == 0 && props.length == 1)) {
            ArrayList<String> al = hsExtraProperties.get(props[0].toUpperCase());
            if (al == null) {
                al = new ArrayList<String>();
                hsExtraProperties.put(props[0].toUpperCase(), al);
            }
            al.add(propertyName);
            
            if (li.alExtraListenerProperties == null) {
                li.alExtraListenerProperties = new ArrayList<String>();
            }
            li.alExtraListenerProperties.add(props[0].toUpperCase());
            
            boolean bNeedsTrigger = (props.length > 1);
            
            if (lis.length == 0) {
                // could be a calcProp
                OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass());
                String[] calcProps = null;
                for (OACalcInfo ci : oi.getCalcInfos()) {
                    if (ci.getName().equalsIgnoreCase(props[0])) {
                        // make recursive
                        String[] ps = ci.getProperties();
                        if (ps == null) break;
                        for (String p : ps) {
                            _addDependentListener(li, propertyName, p);
                        }
                        break;
                    }
                }       
            }
            if (!bNeedsTrigger) return;
        }

        // see if a trigger has already been created for this listener
        OATrigger trigger;
        if (hsTrigger == null) {
            hsTrigger = new HashMap<String, OATrigger>();
        }
        else {
            trigger = hsTrigger.get(dependentPropertyPath.toUpperCase());
            if (trigger != null) {
                if (li.alTrigger == null) li.alTrigger = new ArrayList<OATrigger>();
                if (!li.alTrigger.contains(trigger)) li.alTrigger.add(trigger);
                return;
            }
        }
        
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                if (obj == null || !HubListenerTree.this.hub.contains(obj)) return;
                HubEventDelegate.fireCalcPropertyChange(HubListenerTree.this.hub, obj, propertyName);
            }
        };
        
        trigger = new OATrigger(hub.getObjectClass(), tl, dependentPropertyPath, true, false, false);
        OATriggerDelegate.createTrigger(trigger, true);

        hsTrigger.put(dependentPropertyPath.toUpperCase(), trigger);
        
        if (li.alTrigger == null) li.alTrigger = new ArrayList<OATrigger>();
        if (!li.alTrigger.contains(trigger)) li.alTrigger.add(trigger);
        return;
    }
    

    public void removeListener(HubListener hl) {
        synchronized (lock) {
            _removeListener(hl);
        }
    }
    private void _removeListener(HubListener hl) {
        if (hl == null) return;

        // 1: remove hubListener 
        HubListener[] hold = listeners; 
        listeners = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, hl);
        if (hold == listeners) {
            return;
        }
        if (hl.getLocation() == HubListener.InsertLocation.LAST) cntLast--;

        if (alListenerInfo == null) return;

        // 2: remove any listenerInfo
        ListenerInfo li = null;
        for (ListenerInfo lix : alListenerInfo) {
            if (lix.hl != hl) continue;
            li = lix;
            break;
        }

        if (li == null) return; // none required
        alListenerInfo.remove(li);

        // 3: remove any hlExtra properties that this hl had for the hlExtra propertyChange events
        if (hlExtra != null && hsExtraProperties != null && li.alExtraListenerProperties != null) {
            // see if this is the only listener for each of the extra properties
            for (String p : li.alExtraListenerProperties) { 
                boolean b = false;
                // check other listenerInfo
                for (ListenerInfo lix : alListenerInfo) {
                    if (lix.hl == hl) continue;
                    if (lix.alExtraListenerProperties == null) continue;
                    if (lix.alExtraListenerProperties.contains(p.toUpperCase())) {
                        b = true;
                        break;
                    }
                }
                if (!b) {
                    // dont listen to it anymore
                    hsExtraProperties.remove(p.toUpperCase());
                }
            }
        }

        // 4: check to see if the hlExtra is still needed
        if (hlExtra != null && hsExtraProperties != null && hsExtraProperties.size() == 0) {
            hlExtra = null;
            _removeListener(hlExtra);
        }

        // 5: check if any of the triggers can be removed
        if (li.alTrigger != null) {
            // see if this is the last listener for a trigger
            for (OATrigger t : li.alTrigger) {
                boolean b = false;
                for (ListenerInfo lix : alListenerInfo) {
                    if (lix.hl == hl) continue;
                    if (lix.alTrigger == null) continue;
                    if (lix.alTrigger.contains(t)) {
                        b = true;
                        break;
                    }                            
                }                    
                if (!b) {
                    OATriggerDelegate.removeTrigger(t);
                    for (Map.Entry<String, OATrigger> me : hsTrigger.entrySet()) {
                        if (me.getValue() == t) {
                            hsTrigger.remove(me.getKey());
                            break;
                        }
                    }
                }
            }
        }
    }    
}

