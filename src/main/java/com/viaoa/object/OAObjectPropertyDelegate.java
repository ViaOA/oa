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
package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OANotExist;

// 20140225 redone to simplify property locking using CAS

/**
 * Manages OAObject.properties, which are used to store references 
 * (OAObjects, Hubs, OAObjectKey) and misc values.
 * Stores as name/value in a flat object array, where even positions are property names and odd positions are the value, which can be null. 
 * This uses a flat array to make it as efficient as possible for the oaObject with as little overhead as possible. 
 */
public class OAObjectPropertyDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectPropertyDelegate.class.getName());

    
    /**
     * @return true if prop is loaded - does not need to be loaded from datasource, etc.
     */
    public static boolean isPropertyLoaded(OAObject oaObj, String name) {
        if (oaObj == null || name == null) return false;
        Object[] props = oaObj.properties;
        if (props == null) return false;

        for (int i=0; i<props.length; i+=2) {
            if ( props[i] == null || !name.equalsIgnoreCase((String)props[i]) ) continue;
            
            Object objx = props[i+1];
            if (objx instanceof WeakReference) {
                objx = ((WeakReference) objx).get();
                if (objx == null) return false;
            }
            else if (objx instanceof OAObjectKey) return false;
            return true; 
        }
        return false;
    }

    public static String[] getPropertyNames(OAObject oaObj) {
        Object[] props = oaObj.properties;
        if (props == null) return null;
        String[] ss;

        int cnt = 0;
        for (int i=0; i<props.length; i+=2) {
            if (props[i] != null) cnt++; 
        }
        ss = new String[cnt];
        int j = 0;
        for (int i=0; i<props.length; i+=2) {
            if (props[i] != null) {
                ss[j++] = (String) props[i];
            }
        }
        return ss;
    }

    
    static void unsafeAddProperty(OAObject oaObj, String name, Object value) {
        unsafeSetProperty(oaObj, name, value, false, false);
    }    
    public static void unsafeSetProperty(OAObject oaObj, String name, Object value) {
        unsafeSetProperty(oaObj, name, value, true, false);
    }    
    static void unsafeSetPropertyIfEmpty(OAObject oaObj, String name, Object value) {
        unsafeSetProperty(oaObj, name, value, true, true);
    }    
    
    private static void unsafeSetProperty(OAObject oaObj, String name, Object value, boolean bCheckFirst, boolean bOnlyIfNotFound) {
        int pos;
        if (oaObj.properties == null) {
            oaObj.properties = new Object[2];
            pos = 0;
        }
        else {
            pos = -1;
            if (bCheckFirst || bOnlyIfNotFound) {
                for (int i=0; i<oaObj.properties.length; i+=2) {
                    if (pos == -1 && oaObj.properties[i] == null) pos = i; 
                    else if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                        if (bOnlyIfNotFound) {
                            return;
                        }
                        pos = i;
                        break;
                    }
                }
            }
            if (pos < 0) {
                pos = oaObj.properties.length;
                oaObj.properties = Arrays.copyOf(oaObj.properties, pos+2);
            }
        }
        oaObj.properties[pos] = name;
        oaObj.properties[pos+1] = value;
    }

    public static void removeProperty(OAObject oaObj, String name, boolean bFirePropertyChange) {
        if (oaObj.properties == null || name == null) return;
        Object value = null;
        boolean bResize = false;
        synchronized (oaObj) {
            for (int i=0; i<oaObj.properties.length; i+=2) {
                if (oaObj.properties[i] == null) bResize = true;
                else if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                    value = oaObj.properties[i+1];
                    oaObj.properties[i] = null;
                    oaObj.properties[i+1] = null;
                    if (bResize) resizeProperties(oaObj);
                    break;
                }
            }
        }
        if (bFirePropertyChange) oaObj.firePropertyChange(name, value, null);
    }
    public static void removePropertyIfNull(OAObject oaObj, String name, boolean bFirePropertyChange) {
        if (oaObj.properties == null || name == null) return;
        Object value = null;
        boolean bResize = false;
        synchronized (oaObj) {
            for (int i=0; i<oaObj.properties.length; i+=2) {
                if (oaObj.properties[i] == null) bResize = true;
                else if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                    value = oaObj.properties[i+1];
                    if (value != null) return;
                    
                    oaObj.properties[i] = null;
                    oaObj.properties[i+1] = null;
                    if (bResize) resizeProperties(oaObj);
                    break;
                }
            }
        }
        if (bFirePropertyChange) oaObj.firePropertyChange(name, value, null);
    }
    
    private static void resizeProperties(OAObject oaObj) {
        int newSize = 0;
        for (int i=0; i<oaObj.properties.length; i+=2) {
            if (oaObj.properties[i] != null) newSize+=2; 
        }
        Object[] objs = new Object[newSize];
        for (int i=0,j=0; i<oaObj.properties.length; i+=2) {
            if (oaObj.properties[i] != null) {
                objs[j++] = oaObj.properties[i]; 
                objs[j++] = oaObj.properties[i+1]; 
            }
        }
        oaObj.properties = objs;
    }

    public static void setProperty(OAObject oaObj, String name, Object value) {
        if (oaObj == null || name == null) return;

        synchronized (oaObj) {
            int pos;
            if (oaObj.properties == null) {
                oaObj.properties = new Object[2];
                pos = 0;
            }           
            else {
                pos = -1;
                for (int i=0; i<oaObj.properties.length; i+=2) {
                    if (pos == -1 && oaObj.properties[i] == null) pos = i; 
                    else if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                        pos = i;
                        break;
                    }
                }
                if (pos < 0) {
                    pos = oaObj.properties.length;
                    oaObj.properties = Arrays.copyOf(oaObj.properties, pos+2);
                }
            }
            oaObj.properties[pos] = name;
            oaObj.properties[pos+1] = value;
        }
        
        // 20141125 in case Hub.datam.masterObject is not set
        Object objx = value;
        if (objx instanceof WeakReference) objx = ((WeakReference) objx).get();
        if (objx instanceof Hub) {
            OAObjectHubDelegate.setMasterObject((Hub) objx, oaObj, name);
        }
    }
    
    
    // used by Hub when reading serialized object
    public static void setPropertyHubIfNotSet(OAObject oaObj, String name, Object value) {
        if (oaObj == null || name == null) return;

        Object[] props = oaObj.properties; 
        if (props != null) {
            for (int i=0; i<props.length; i+=2) {
                if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                    if (props[i+1] != null) return;
                }
            }
        }
        
        synchronized (oaObj) {
            int pos;
            if (oaObj.properties == null) {
                oaObj.properties = new Object[2];
                pos = 0;
            }           
            else {
                pos = -1;
                for (int i=0; i<oaObj.properties.length; i+=2) {
                    if (pos == -1 && oaObj.properties[i] == null) pos = i; 
                    else if (name.equalsIgnoreCase((String)oaObj.properties[i])) {
                        pos = i;
                        break;
                    }
                }
                if (pos < 0) {
                    pos = oaObj.properties.length;
                    oaObj.properties = Arrays.copyOf(oaObj.properties, pos+2);
                }
            }
            if (oaObj.properties[pos+1] == null) {
                oaObj.properties[pos+1] = value;
                oaObj.properties[pos] = name;
            }
        }
    }

    public static Object setPropertyCAS(OAObject oaObj, String name, Object newValue, Object matchValue) {
        return setPropertyCAS(oaObj, name, newValue, matchValue, false, false);
    }

    /**
     * Compare and swap a property. 
     * @param name property name, not case sensitive
     * @param newValue new value to set, if matchValue matches current setting
     * @param matchValue value that it must currently be set to
     * @param bMustNotExist only update if there is not a current value
     * @param bReturnNotExist if true, then return OANotExist.instance if value does not 
     * match and the current value does not exist.
     * @return value that is stored. If the matchValue is not the same as current,
     * then the current value will be returned, else the newValue will be returned.
     */
    public static Object setPropertyCAS(OAObject oaObj, String name, Object newValue, Object matchValue, boolean bMustNotExist, boolean bReturnNotExist) {
        if (oaObj == null || name == null) return null;
        synchronized (oaObj) {
            int pos;
            if (oaObj.properties == null) {
                if (!bMustNotExist) {
                    if (matchValue != null) {
                        if (bReturnNotExist) return OANotExist.instance;
                        return null;
                    }
                }
                oaObj.properties = new Object[2];
                pos = 0;
            }           
            else {
                pos = -1;
                for (int i=0; i<oaObj.properties.length; i+=2) {
                    if (pos == -1 && oaObj.properties[i] == null) {
                        pos = i;
                        continue;
                    }
                    if (!name.equalsIgnoreCase((String)oaObj.properties[i])) continue;
                    
                    if (bMustNotExist) return oaObj.properties[i+1];
                    
                    if (matchValue != oaObj.properties[i+1]) {
                        if (oaObj.properties[i+1] instanceof WeakReference) {
                            Object objx = ((WeakReference) oaObj.properties[i+1]).get();
                            if (matchValue == objx) {
                                pos = i;
                            }
                            break;
                        }
                        
                        if (matchValue == null) return oaObj.properties[i+1];
                        if (!matchValue.equals(oaObj.properties[i+1])) {
                            if (!(matchValue instanceof OAObjectKey) || !(newValue instanceof OAObject)) {
                                return oaObj.properties[i+1];
                            }
                            OAObjectKey k = OAObjectKeyDelegate.getKey((OAObject) newValue);
                            if (!matchValue.equals(k)) {
                                return oaObj.properties[i+1];
                            }
                        }
                    }
                    pos = i;
                    break;
                }
                if (pos < 0) {
                    if (!bMustNotExist) {
                        if (matchValue != null) {
                            if (bReturnNotExist) return OANotExist.instance;
                            return null;
                        }
                    }
                    
                    pos = oaObj.properties.length;
                    oaObj.properties = Arrays.copyOf(oaObj.properties, pos+2);
                }
                else if (oaObj.properties[pos] == null) {
                    if (!bMustNotExist) {
                        if (matchValue != null) {
                            if (bReturnNotExist) return OANotExist.instance;
                            return null;
                        }
                    }
                }
            }
            oaObj.properties[pos] = name;

            if (newValue != null || !(oaObj.properties[pos+1] instanceof Hub)) {  // 20120827 dont set an existing Hub to null (sent that way if size is 0)
                oaObj.properties[pos+1] = newValue;
            }

            // 20141125 in case Hub.datam.masterObject is not set
            Object objx = newValue;
            if (objx instanceof WeakReference) objx = ((WeakReference) objx).get();
            if (objx instanceof Hub) {
                OAObjectHubDelegate.setMasterObject((Hub) objx, oaObj, name);
            }
        }
        return newValue;
    }
    public static Object getProperty(OAObject oaObj, String name) {
        return getProperty(oaObj, name, false, false);
    }

    /**
     * 
     * @param oaObj
     * @param name name to find, not case sensitive
     * @param bReturnNotExist if true and the property name does not exist or it's value has not been loaded, then OANotExist.instance
     * @param bConvertWeakRef if true and the value is a WeakReference, then it's value will be checked and returned.
     * is returned.
     */
    public static Object getProperty(OAObject oaObj, String name, boolean bReturnNotExist, boolean bConvertWeakRef) {
        if (oaObj == null || name == null) return null;
        
        Object[] objs = oaObj.properties;
        if (objs == null) {
            if (bReturnNotExist) return OANotExist.instance; 
            return null;
        }
        for (int i=0; i<objs.length; i+=2) {
            if (objs[i] == null || !name.equalsIgnoreCase((String)objs[i])) continue;
            Object objx = objs[i+1];
            if (bConvertWeakRef && objx instanceof WeakReference) {
                objx = ((WeakReference) objx).get();
                if (objx == null) {
                    if (bReturnNotExist) return OANotExist.instance; 
                    return null;
                }
            }
            return objx;
        }
        if (bReturnNotExist) return OANotExist.instance; 
        return null;
    }

    // property locking
    private static ConcurrentHashMap<String, PropertyLock> hmLock = new ConcurrentHashMap<String, PropertyLock>();

    private static class PropertyLock {
        boolean done;
        boolean hasWait;
        Thread thread;
    }
    public static void setPropertyLock(OAObject oaObj, String name) {
        _setPropertyLock(oaObj, name, true);
    }
    public static boolean attemptPropertyLock(OAObject oaObj, String name) {
        return _setPropertyLock(oaObj, name, false);
    }
    private static boolean _setPropertyLock(OAObject oaObj, String name, boolean bWaitIfNeeded) {
        if (oaObj == null || name == null) return false;
        String key = OAObjectKeyDelegate.getKey(oaObj).getGuid() + "." + name.toUpperCase();
        PropertyLock lock;
        synchronized (oaObj) {
            lock = hmLock.get(key);
            if (lock == null) {
                lock = new PropertyLock();
                lock.thread = Thread.currentThread();
                hmLock.put(key, lock);
                return true;
            }
        }
        synchronized (lock) {
            if (lock.thread == Thread.currentThread()) return true;
            if (!bWaitIfNeeded) return false;
            for (int i=0; ;i++) {
                if (i > 6) {
                    LOG.log(Level.WARNING, "wait time exceeded for lock, obj="+oaObj+", prop="+name+", will continue", new Exception("wait time exceeded"));
                    return false;  // bail out, ouch
                }
                if (i == 0) OARemoteThreadDelegate.startNextThread();
                if (lock.done) break;
                lock.hasWait = true;
                try {
                    lock.wait(250);
                }
                catch (Exception e) {
                }
            }
        }
        return _setPropertyLock(oaObj, name, bWaitIfNeeded);  // create a new one
    }
    public static void releasePropertyLock(OAObject oaObj, String name) {
        if (oaObj == null || name == null) return;
        String key = OAObjectKeyDelegate.getKey(oaObj).getGuid() + "." + name.toUpperCase();
        PropertyLock lock;
        synchronized (oaObj) {
            lock = hmLock.remove(key);
        }
        if (lock != null) {
            synchronized (lock) {
                lock.done = true;
                if (lock.hasWait) {
                    lock.notifyAll();
                }
            }
        }
    }
    public static boolean isPropertyLocked(OAObject oaObj, String name) {
        if (oaObj == null || name == null) return false;
        String key = OAObjectKeyDelegate.getKey(oaObj).getGuid() + "." + name.toUpperCase();
        return (hmLock.get(key) != null);
    }
    
    // 20141108 "flip" a hub property to/from a weakRef.  Used by HubDelegate.setReferenceable
    public static boolean setPropertyWeakRef(OAObject oaObj, String name, boolean bToWeakRef) {
        if (name == null || oaObj == null || oaObj.properties == null) return false;

        boolean b = false;
        synchronized (oaObj) {
            for (int i=0; i<oaObj.properties.length; i+=2) {
                if (!name.equalsIgnoreCase((String)oaObj.properties[i])) continue;
                Object val = oaObj.properties[i+1];
                if (val == null) break; 
                if (bToWeakRef) {
                    if (!(val instanceof WeakReference)) {
                        oaObj.properties[i+1] = new WeakReference(val);
                        b = true;
                    }
                }
                else {
                    if (val instanceof WeakReference) {
                        b = true;
                        val = ((WeakReference)val).get();
                        if (val == null) {
                            removePropertyIfNull(oaObj, name, false);
                        }
                        else {
                            oaObj.properties[i+1] = val;
                        }
                    }
                }
                break;
            }
        }
        return b;
    }
    
    // 20141030
    /**
     * Used on server, this will make sure that a Hub does not get GCd on the Server. This is needed when
     * a hub has a masterObject that has a cacheSize set, which means that it can be GCd.
     * This will recursively set any parent/master objects.
     * This is called by Hub.add, Hub.remove, Hub.firepropchange(), Hub.saveAll.
     * @param bReferenceable true to make sure that it has a hard ref, otherwise a weakRef will be used
     */
    public static void setReferenceable(OAObject obj, boolean bReferenceable) {
        setReferenceable(obj, bReferenceable, null);
    }
   
    private static void setReferenceable(OAObject obj, boolean bReferenceable, OACascade cascade) {
        if (obj == null) return;
        if (!OASyncDelegate.isServer()) return;
        if (cascade != null && cascade.wasCascaded(obj, true)) return;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (!OAObjectInfoDelegate.isWeakReferenceable(oi)) return;
        
        boolean bSupportStorage = oi.getSupportsStorage();
        
        
        for (OALinkInfo li : oi.getLinkInfos()) {
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev == null) continue;
            if (liRev.getType() != OALinkInfo.MANY) continue;
            
            Object parent = li.getValue(obj); // parent
            if (!(parent instanceof OAObject)) continue;

            if (liRev.getCacheSize() > 0) {
                liRev.getValue((OAObject) parent); // need to make sure that the hub is loaded
                if (bReferenceable || bSupportStorage) {
                    boolean b = OAObjectPropertyDelegate.setPropertyWeakRef((OAObject) parent, liRev.getName(), !bReferenceable);
                    if (!b) break;  // already changed, dont need to continue
                }
            }
            if (bReferenceable) {
                if (cascade == null) cascade = new OACascade();
                cascade.add(obj);
                setReferenceable((OAObject)parent, bReferenceable, cascade);
            }
        }
    }

    public static void clearProperties(OAObject oaObj) {
        if (oaObj != null) oaObj.properties = null;
    }
}

