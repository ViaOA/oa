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
    
    
    
    public HubListenerTree(Hub<T> hub) {
        this.hub = hub;
    }
    
    public HubListener<T>[] getHubListeners() {
        return this.listeners;
    }

    public boolean addListener(HubListener<T> hl) {
        if (hl == null) return false;

        synchronized (lock) {
            if (OAArray.containsExact(listeners, hl)) return false;

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
        return true;
    }   
    
    public boolean addListener(HubListener hl, String property) {
        if (hl == null) return false;
        return addListener(hl, property, null);
    }

    public boolean addListener(HubListener hl, final String property, String[] dependentPropertyPaths) {
        if (hl == null) return false;

        String s = "";
        if (dependentPropertyPaths != null) {
            for (String triggerPropPath : dependentPropertyPaths) {
                if (s.length() > 0) s += ", ";
                s += triggerPropPath;
            }
        }
        s = (hub.getObjectClass().getSimpleName()+", property="+property+", ppDepend=["+s+"]");
        LOG.fine(s);
        if (OAPerformance.IncludeHubListeners) {
            OAPerformance.LOG.fine(s);
        }
        
        boolean bWasAdded = addListener(hl);
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass());
        String[] calcProps = null;
        for (OACalcInfo ci : oi.getCalcInfos()) {
            if (ci.getName().equalsIgnoreCase(property)) {
                calcProps = ci.getProperties();
                break;
            }
        }       
        if (calcProps != null && calcProps.length > 0) {
            if (addDependentListeners(hl, property, dependentPropertyPaths)) bWasAdded = true;
        }

        // now add the additional dependent properties
        if (dependentPropertyPaths != null && dependentPropertyPaths.length > 0) {
            if (addDependentListeners(hl, property, dependentPropertyPaths)) bWasAdded = true;
        }
        return bWasAdded;
    }    
    
    private boolean addDependentListeners(final HubListener<T> hl, final String propertyName, final String[] dependentPropertyPaths) {
        if (dependentPropertyPaths == null || dependentPropertyPaths.length == 0) return false;
        synchronized (lock) {
            return _addDependentListeners(hl, propertyName, dependentPropertyPaths);
        }
    }
    private boolean _addDependentListeners(final HubListener<T> hl, final String propertyName, final String[] dependentPropertyPaths) {
        ListenerInfo li = null;

        if (alListenerInfo != null) {
            for (ListenerInfo lix : alListenerInfo) {
                if (lix.hl == hl) {
                    li = lix;
                    break;
                }
            }
        }
        if (li == null) {
            li = new ListenerInfo();
            li.hl = hl;
        }
        
        boolean bUsed = false;
        
        boolean bWasAdded = false;

        for (String dpp : dependentPropertyPaths) {
            if (dpp == null || dpp.length() == 0) continue;
            
            if (_addDependentListener(0, li, propertyName, dpp)) bWasAdded = true;;
            if (bWasAdded && !bUsed) {
                if (alListenerInfo == null) alListenerInfo = new ArrayList<HubListenerTree.ListenerInfo>();
                if (!alListenerInfo.contains(li)) alListenerInfo.add(li);
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
            if (addListener(hlExtra)) bWasAdded = true;
        };
        return bWasAdded;
    }

    private boolean _addDependentListener(final int cnter, final ListenerInfo li, final String propertyName, final String dependentPropertyPath) {
        if (cnter > 15) return false;
        
        OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), dependentPropertyPath);
        String[] props = pp.getProperties();
        OALinkInfo[] lis = pp.getLinkInfos();
        boolean bWasAdded = false;
        
        if ((lis.length > 0 && lis[0].getType() == OALinkInfo.ONE) || (lis.length == 0 && props.length == 1)) {
            ArrayList<String> al = hsExtraProperties.get(props[0].toUpperCase());
            if (al == null) {
                al = new ArrayList<String>();
                hsExtraProperties.put(props[0].toUpperCase(), al);
            }
            if (propertyName != null && !al.contains(propertyName.toUpperCase())) {
                al.add(propertyName.toUpperCase());
                bWasAdded = true;
            }
            
            if (li.alExtraListenerProperties == null) {
                li.alExtraListenerProperties = new ArrayList<String>();
            }
            if (!li.alExtraListenerProperties.contains(props[0].toUpperCase())) {
                li.alExtraListenerProperties.add(props[0].toUpperCase());
                bWasAdded = true;
            }
            
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
                            if (_addDependentListener(cnter+1, li, propertyName, p)) bWasAdded = true;;
                        }
                        break;
                    }
                }       
            }
            if (!bNeedsTrigger) return bWasAdded;
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
                if (!li.alTrigger.contains(trigger)) {
                    li.alTrigger.add(trigger);
                    bWasAdded = true;
                }
                return bWasAdded;
            }
        }
        
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                if (obj == null || !(obj.getClass().equals(HubListenerTree.this.hub.getObjectClass())) || !HubListenerTree.this.hub.contains(obj)) {
                    // could be an add or remove from a many reference (hub)
                    //   ex: this=location,  prop=employees, event: loc.emps.add(emp)
                    Hub h = hubEvent.getHub();
                    if (h == null) return;
                    Object objx = h.getMasterObject();
                    if (objx == null) return;
                    if (!(objx.getClass().equals(HubListenerTree.this.hub.getObjectClass()))) return;
                    if (!HubListenerTree.this.hub.contains(objx)) return;
                    obj = (OAObject) objx;
                }
                HubEventDelegate.fireCalcPropertyChange(HubListenerTree.this.hub, obj, propertyName);
            }
        };
        
        trigger = new OATrigger(hub.getObjectClass(), tl, dependentPropertyPath, true, false, false);
        OATriggerDelegate.createTrigger(trigger, true);

        hsTrigger.put(dependentPropertyPath.toUpperCase(), trigger);
        
        if (li.alTrigger == null) li.alTrigger = new ArrayList<OATrigger>();
        if (!li.alTrigger.contains(trigger)) li.alTrigger.add(trigger);
        return true;
    }
    

    public boolean removeListener(HubListener hl) {
        if (hl == null) return false;
        synchronized (lock) {
            return _removeListener(hl);
        }
    }
    private boolean _removeListener(HubListener hl) {
        HubListener[] hold = listeners; 
        listeners = (HubListener[]) OAArray.removeValue(HubListener.class, listeners, hl);
        if (hold == listeners) {
            return false;
        }

        // 1: remove hubListener 
        if (hl.getLocation() == HubListener.InsertLocation.LAST) cntLast--;

        if (alListenerInfo == null) return true;

        // 2: remove any listenerInfo
        ListenerInfo li = null;
        for (ListenerInfo lix : alListenerInfo) {
            if (lix.hl != hl) continue;
            li = lix;
            break;
        }

        if (li == null) return true; // none required
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
            HubListener hlx = hlExtra;
            hlExtra = null;
            _removeListener(hlx);
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
        return true;
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (hsTrigger != null) {
            for (OATrigger t : hsTrigger.values()) {
                OATriggerDelegate.removeTrigger(t);
            }
        }
        super.finalize();
    }
}

