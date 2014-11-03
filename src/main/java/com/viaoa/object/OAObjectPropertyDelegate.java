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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.viaoa.hub.Hub;
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
     * Returns true if there is a property = name, even if the value is null 
     */
    public static boolean isPropertyLoaded(OAObject oaObj, String name) {
        if (oaObj == null || name == null) return false;
        Object[] objs = oaObj.properties;
        if (objs == null) return false;

        for (int i=0; i<objs.length; i+=2) {
            if ( oaObj.properties[i] == null || !name.equalsIgnoreCase((String)oaObj.properties[i]) ) continue;
            return true; // any value wlll return true
        }
        return false;
    }

    public static String[] getPropertyNames(OAObject oaObj) {
        Object[] objs = oaObj.properties;
        if (objs == null) return null;
        String[] ss;

        int cnt = 0;
        for (int i=0; i<objs.length; i+=2) {
            if (objs[i] != null) cnt++; 
        }
        ss = new String[cnt];
        int j = 0;
        for (int i=0; i<objs.length; i+=2) {
            if (objs[i] != null) {
                ss[j++] = (String) objs[i];
            }
        }
        return ss;
    }
    static void unsafeSetProperty(OAObject oaObj, String name, Object value) {
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
                        if (matchValue == null) return oaObj.properties[i+1];
                        if (!matchValue.equals(oaObj.properties[i+1])) {
                            if (!(matchValue instanceof OAObjectKey) || !(newValue instanceof OAObject)) return false;
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
        }
        return newValue;
    }

    public static Object getProperty(OAObject oaObj, String name) {
        return getProperty(oaObj, name, false);
    }
    
    /**
     * 
     * @param oaObj
     * @param name name to find, not case sensitive
     * @param bReturnNotExist if true and the property name does not exist, then OANotExist.instance
     * is returned.
     */
    public static Object getProperty(OAObject oaObj, String name, boolean bReturnNotExist) {
        if (oaObj == null || name == null) return null;
        Object[] objs = oaObj.properties;
        if (objs == null) {
            if (bReturnNotExist) return OANotExist.instance; 
            return null;
        }
        for (int i=0; i<objs.length; i+=2) {
            if (objs[i] == null || !name.equalsIgnoreCase((String)objs[i])) continue;
            Object objx = objs[i+1];
            if (objx instanceof WeakReference) {
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
        if (oaObj == null || name == null) return;
        String key = OAObjectKeyDelegate.getKey(oaObj).getGuid() + "." + name.toUpperCase();
        PropertyLock lock;
        synchronized (oaObj) {
            lock = hmLock.get(key);
            if (lock == null) {
                lock = new PropertyLock();
                lock.thread = Thread.currentThread();
                hmLock.put(key, lock);
                return;
            }
        }
        synchronized (lock) {
            if (lock.thread == Thread.currentThread()) return;
            for (;;) {
                if (lock.done) break;
                lock.hasWait = true;
                try {
                    lock.wait();
                }
                catch (Exception e) {
                }
            }
        }
        setPropertyLock(oaObj, name);  // create a new one
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
    
}

