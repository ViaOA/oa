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

import com.viaoa.hub.Hub;
import com.viaoa.object.*;

/** 
    Filter that is used to listen to all objects added to OAObjectCacheDelegate and then add to a specific Hub.
*/
public class OAObjectCacheHubAdder<T extends OAObject> implements OAObjectCacheListener<T> {
    static final long serialVersionUID = 1L;

    protected WeakReference<Hub<T>> wfHub;
    private Class clazz;

    /**
        Used to create a new HubControllerAdder that will add objects to the supplied Hub.
    */
    public OAObjectCacheHubAdder(Hub<T> hub) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        clazz = hub.getObjectClass();
        wfHub = new WeakReference(hub);
        
        OAObjectCacheDelegate.addListener(clazz, this);
        
        // need to get objects that are already loaded 
        OAObjectCacheDelegate.callback(clazz, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                Hub<T> h = wfHub.get();
                if (h != null) {
                    if (!h.contains(obj)) {
                        if (isUsed((T) obj)) {
                            h.add((T) obj);
                        }
                    }
                }
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
    public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue) {
    }

    @Override
    public void afterAdd(T obj) {
        if (obj != null) {
            if (isUsed(obj)) {
                Hub<T> h = wfHub.get();
                if (h != null) h.add(obj);
            }
        }
    }
    
    
    /**
     * determine if a new object will be used.
     */
    public boolean isUsed(T obj) {
        return true;
    }
    
}

