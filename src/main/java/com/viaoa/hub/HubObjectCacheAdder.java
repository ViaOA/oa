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
import com.viaoa.object.*;

/** 
    Filter that is used to listen to all objects added to OAObjectCacheDelegate and then add to a specific Hub.
*/
public class HubObjectCacheAdder<T extends OAObject> implements OAObjectCacheListener<T>, java.io.Serializable {
    static final long serialVersionUID = 1L;

    protected Hub hub;

    /**
        Used to create a new HubControllerAdder that will add objects to the supplied Hub.
    */
    public HubObjectCacheAdder(Hub<T> hub) {
        if (hub == null) throw new IllegalArgumentException("hub can not be null");
        this.hub = hub;
        
        Class c = hub.getObjectClass();
        OAObjectCacheDelegate.addListener(c, this);
        
        // need to get objects that already loaded 
        OAObjectCacheDelegate.callback(c, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                if (!HubObjectCacheAdder.this.hub.contains(obj)) HubObjectCacheAdder.this.hub.add((OAObject) obj);
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
    

    @Override
    public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue) {
    }

    @Override
    public void afterAdd(T obj) {
        if (obj != null) {
            if (!hub.contains(obj)) hub.add(obj);
        }
    }
    
}

