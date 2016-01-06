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

import java.lang.ref.WeakReference;
import com.viaoa.object.*;

/** 
    Filter that is used to listen to all objects added to OAObjectCacheDelegate and then add to a specific Hub.
*/
public class HubCacheAdder implements OAObjectCacheListener, java.io.Serializable {
    static final long serialVersionUID = 1L;

    protected WeakReference<Hub> wrHub;
    private Class clazz;

    /**
        Used to create a new HubControllerAdder that will add objects to the supplied Hub.
    */
    public HubCacheAdder(Hub hub) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        this.wrHub = new WeakReference<Hub>(hub);
        
        clazz = hub.getObjectClass();
        OAObjectCacheDelegate.addListener(clazz, this);
        
        // need to get objects that already loaded 
        OAObjectCacheDelegate.callback(clazz, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                Hub h = wrHub.get();
                if (h != null) h.add((OAObject) obj);
                return true;
            }
        });
    }

    public void close() {
        OAObjectCacheDelegate.removeListener(clazz, this);
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void afterPropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
    }
    
    @Override
    public void afterAdd(OAObject obj) {
        if (obj != null) {
            Hub h = wrHub.get();
            if (h != null) h.add(obj);
        }
    }

}