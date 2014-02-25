/*This software and documentation is the confidential and proprietary information of ViaOA, Inc.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with ViaOA, Inc.
 * 
 * ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 * 
 * Copyright (c) 2001-2013 ViaOA, Inc. All rights reserved. */
package com.viaoa.object;

import java.util.logging.Logger;

/**
 * This is used for locking during get or set for oaObject properties.
 * This allows for set methods to be called during a read lock, but only one
 * read lock at a time.  Once a read is completed, it will check to see if the
 * property was updated by a set method, and if so, use that value.
 * @author vvia
 *
 */
public class OAPropertyLockDelegate {

    private static Logger LOG = Logger.getLogger(OAPropertyLockDelegate.class.getName());

    public static class PropertyLock {
        OAObject object;
        String propertyName;
        String key;
        Thread thread; // that has lock
        boolean bWaiting; // if other threads are waiting

        volatile boolean bValueHasBeenSet; // flag to know that the ref has been set
        volatile Object value; // actual property value (could be null)
    }

    /** used to set a lock to synchronize getting reference property */
    protected static PropertyLock getPropertyLock(OAObject oaObj, String linkPropertyName) {
        return getPropertyLock(oaObj, linkPropertyName, true);
    }

    protected static PropertyLock getPropertyLock(OAObject oaObj, String linkPropertyName, boolean bWait) {
        PropertyLock propLock;
        synchronized (OAObjectHashDelegate.hashPropertyLock) {
            String upper = linkPropertyName.toUpperCase();
            String key = oaObj.guid + "." + upper;
            propLock = OAObjectHashDelegate.hashPropertyLock.get(key);
            if (propLock == null) {
                propLock = new PropertyLock();
                propLock.thread = Thread.currentThread();
                propLock.object = oaObj;
                propLock.key = key;
                propLock.propertyName = upper; 
                OAObjectHashDelegate.hashPropertyLock.put(key, propLock);
            }
        }
        if (!bWait) return propLock;
        synchronized (propLock) {
            for (;;) {
                if (propLock.bValueHasBeenSet || propLock.thread == Thread.currentThread()) break;
                propLock.bWaiting = true;
                try {
                    propLock.wait();
                }
                catch (Exception e) {
                }
            }
        }
        return propLock;
    }

    protected static void setValue(PropertyLock propLock, Object newValue) {
        synchronized (propLock) {
            propLock.bValueHasBeenSet = true;
            propLock.value = newValue;
        }        
    }

    protected static void releasePropertyLock(PropertyLock propLock) {
        releasePropertyLock(propLock, false, null);
    }
    protected static void releasePropertyLock(PropertyLock propLock, boolean bStoreProp, Object newValue) {
        synchronized (propLock) {
            if (propLock.bValueHasBeenSet) bStoreProp = false;
            else {
                propLock.bValueHasBeenSet = true;
                propLock.value = newValue;
            }
            if (bStoreProp) {
                OAObjectPropertyDelegate.setProperty(propLock.object, propLock.propertyName, propLock.value, propLock);
            }
            if (propLock.bWaiting) {
                propLock.notifyAll();
            }
        }
        synchronized (OAObjectHashDelegate.hashPropertyLock) {
            OAObjectHashDelegate.hashPropertyLock.remove(propLock.key);
        }
    }
}




