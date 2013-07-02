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


import com.viaoa.hub.*;
import java.util.*;
import java.lang.reflect.*;
import com.viaoa.util.*;
import com.viaoa.object.*;

/** 
    Filter that is used to listene to all objects added to OAObjectCacheDelegate and then add to a specific Hub.
*/
public class HubCacheAdder extends HubListenerAdapter implements java.io.Serializable {
    static final long serialVersionUID = 1L;

    protected Hub hub;

    /**
        Used to create a new HubControllerAdder that will add objects to the supplied Hub.
    */
    public HubCacheAdder(Hub hub) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        this.hub = hub;
        
        Class c = hub.getObjectClass();
        OAObjectCacheDelegate.addListener(c, this);
        
        // need to get objects that already loaded 
        OAObjectCacheDelegate.callback(c, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                if (!HubCacheAdder.this.hub.contains(obj)) HubCacheAdder.this.hub.add((OAObject) obj);
                return true;
            }
        });
    }

    public void close() {
    	OAObjectCacheDelegate.removeListener(hub.getObjectClass(), this);
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    /** HubListener interface method. */
    public @Override void afterInsert(HubEvent e) {
        update(e);
    }
    /** HubListener interface method. */
    public @Override void afterAdd(HubEvent e) {
        update(e);
    }

    protected void update(HubEvent e) {
        Object obj = e.getObject();
        if (obj != null) {
            if (!hub.contains(obj)) hub.add(obj);
        }
    }
}

