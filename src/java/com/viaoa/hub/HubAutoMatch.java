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

import com.viaoa.*;
import java.util.*;
import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.util.*;

/** 
    Makes sure that for each object in a master hub, there exists an object with
    a reference to it in a second hub.
    @see Hub#setAutoMatch
*/
public class HubAutoMatch extends HubListenerAdapter implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    protected Hub hub, hubMaster;
    protected String property;
    protected boolean bManuallyCalled;

    protected transient Method getMethod, setMethod;


    /**
       Create new HubAutoMatch that will automatically create objects in a Hub with references that 
       match the objects in a master hub.
       ex: new HubAutoMatch(hub, itemMain.getItemOptionTypes(), "itemOptionType")
       @param hubMaster hub that has all objects to use
       @param property property in hub that has same type as objects in hubMaster.
       @param bManuallyCalled set to true if the update method will be manually called.  This is used in cases where the hubMaster 
       could be generating events that should not affect the matching.  For example, if the hubMaster is controlled by a HubMerger and
       objects are added/removed.  
    */
    public HubAutoMatch(Hub hub, String property, Hub hubMaster, boolean bManuallyCalled) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        if (hubMaster == null) throw new IllegalArgumentException("hubMaster can not be null");

        this.hub = hub;
        this.hubMaster = hubMaster;
        this.bManuallyCalled = bManuallyCalled;
        if (!bManuallyCalled) {
            hubMaster.addHubListener(this);
        }
        setProperty(property);
    }

    public HubAutoMatch(Hub hub, String property, Hub hubMaster) {
        this(hub, property, hubMaster, false);
    }
    
    
    /**
        Closes HubAutoMatch.
    */
    public void close() {
        hubMaster.removeHubListener(this);
    }
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    protected void setProperty(String property) {
        this.property = property;
        Class c = null;
        if (property == null || property.length() == 0) {
            c = hub.getObjectClass();
            if ( !hubMaster.getObjectClass().equals(c) ) {
                // find property to use
                OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
                ArrayList al = oi.getLinkInfos();
                for (int i=0; i<al.size(); i++) {
                	OALinkInfo li = (OALinkInfo) al.get(i);
                	if (li.getType() == li.ONE && hubMaster.getObjectClass().equals(li.getToClass())) {
                		property = li.getName();
                	}
                }
            }        
        }
        if (property != null) {
            getMethod = OAReflect.getMethod(hub.getObjectClass(), "get"+property);
            if (getMethod == null) {
                throw new RuntimeException("getMethod for property \"" + property + "\" in class "+hub.getObjectClass());
            }
            setMethod = OAReflect.getMethod(hub.getObjectClass(), "set"+property);
            if (setMethod == null) {
                throw new RuntimeException("setMethod for property \"" + property + "\" in class "+hub.getObjectClass());
            }
            c = getMethod.getReturnType();
        }        
        if ( !hubMaster.getObjectClass().equals(c) ) {
            throw new RuntimeException("hubMaster class="+hubMaster.getObjectClass()+" does not match class for update Hub: "+c);
        }
        update();
    }
    

    public synchronized void update() {
        // Step 1: verify that both hubs are using the correct hub 
        //         (in case AO of master hub has been changed, and one of these hubs has not yet been adjusted).
        Hub hubMasterx = HubDetailDelegate.getRealHub(hubMaster);
        Hub hubx = HubDetailDelegate.getRealHub(hub); // in case it is a detailHub and has not been updated yet
        
        // Step 2: see if every object in hubMasterx exists in hubx
        for (int i=0; ;i++) {
            Object obj = hubMasterx.elementAt(i);
            if (obj == null) break;
            // see if object is in hubx
            if (getMethod == null) {
                if (hubx.getObject(obj) == null) hubx.add(obj);
            }
            else {
                for (int j=0; ;j++) {
                    Object o = hubx.elementAt(j);
                    if (o == null) {
                        createNewObject(obj);
                        break;
                    }
                    try {
                        if (getMethod != null) o = getMethod.invoke(o, new Object[] {  });
                    }
                    catch(Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (o != null && o.equals(obj)) break;
                }
            }
        }

        // Step 3: remove objects not in hubMasterx
        for (int i=0; ;i++) {
            Object obj = hubx.elementAt(i);
            if (obj == null) break;

            Object value;
            try {
                if (getMethod != null) value = getMethod.invoke(obj, new Object[] {  });
                else value = obj;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (hubMasterx.getObject(value) == null) {
                if (okToRemove(obj, value)) {
                    if (obj instanceof OAObject) ((OAObject)obj).delete();
                    else hubx.remove(i);
                    i--;
                }
            }
        }
    }
    /**
     * Called before removing an object that does not have a matching value.
     */
    public boolean okToRemove(Object obj, Object propertyValue) {
        return true;
    }
    
    protected void createNewObject(Object obj) {
        try {
            Object object = hub.getObjectClass().newInstance();
            if (setMethod != null) setMethod.invoke(object, new Object[] { obj } );
            hub.add(object);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** HubListener interface method, used to listen to changes to master Hub. */
    public @Override void afterInsert(HubEvent e) {
        if (!hubMaster.isLoading()) {
            update();
        }
    }
    /** HubListener interface method, used to listen to changes to master Hub. */
    public @Override void afterAdd(HubEvent e) {
        if (!hubMaster.isLoading()) {
            update();
        }
    }
    /** HubListener interface method, used to listen to changes to master Hub. */
    public @Override void afterRemove(HubEvent e) {
        if (!hubMaster.isLoading()) {
            update();
        }
    }
    /** HubListener interface method, used to listen to changes to master Hub. */
    public @Override void onNewList(HubEvent e) {
        update();
    }
}

