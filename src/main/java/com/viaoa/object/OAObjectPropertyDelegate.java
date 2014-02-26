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
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAPropertyLockDelegate.PropertyLock;
import com.viaoa.util.OANullObject;

// 20140225

/**
 * Manages OAObject.properties, which are used to store references 
 * (OAObjects, Hubs, OAObjectKey) and misc values.
 * Stores as name/value in a flat object array, where even positions are property names and odd positions are the value, which can be null. 
 * This uses a flat array to make it as efficient as possible for the oaObject with as little overhead as possible. 
 */
public class OAObjectPropertyDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectPropertyDelegate.class.getName());
    /** 
     * returns OANullObject.instance if the prop is found and the value is null
     * returns null if there is no property with name
     * @param bIfNullReturnOANullObject, if true and the property is found with value=null, then OANullObject is returned
     */
    public static Object getProperty(OAObject oaObj, String name, boolean bIfNullReturnOANullObject) {
        if (oaObj == null || name == null) return null;
        Object[] objs = oaObj.properties;
        if (objs == null) return null;
        for (int i=0; i<objs.length; i+=2) {
            if (objs[i] != null && name.equalsIgnoreCase((String)objs[i])) {
                Object objx = objs[i+1];
                if (objx instanceof WeakReference) objx = ((WeakReference) objx).get();
                if (objx == null && bIfNullReturnOANullObject) objx = OANullObject.instance; 
                return objx;
            }
        }
        return null;
    }
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
    public static void setProperty(OAObject oaObj, String name, Object value) {
        setProperty(oaObj, name, value, null);
    }

    public static void setProperty(OAObject oaObj, String name, Object value, PropertyLock propLock) {
        if (oaObj == null || name == null) return;

        boolean bCreateLock = (propLock == null);
        if (bCreateLock) {
            propLock = OAPropertyLockDelegate.getPropertyLock(oaObj, name, false, false);
        }
        
        synchronized (oaObj) {
            OAPropertyLockDelegate.setValue(propLock, value, false);
            if (oaObj.properties == null) {
                oaObj.properties = new Object[2];
            }                
            int pos = -1;
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
            oaObj.properties[pos] = name;
            if (value != null || !(oaObj.properties[pos+1] instanceof Hub)) {  // 20120827 dont set an existing Hub to null (sent that way if size is 0)
                oaObj.properties[pos+1] = value;
            }
        }        
        if (bCreateLock) {
            OAPropertyLockDelegate.releasePropertyLock(propLock, value, false);
        }
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
}


