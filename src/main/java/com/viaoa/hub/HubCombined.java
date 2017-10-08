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
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.*;

/**
 * Combines multiple hubs into one.
*/
public class HubCombined<T> {
    private static Logger LOG = Logger.getLogger(HubCombined.class.getName());
    private static final long serialVersionUID = 1L;

    protected Hub<T> hubMaster;
    protected ArrayList<Hub<T>> alHub;
    
    public HubCombined(Hub<T> hubMaster, Hub<T> ... hubs) {
        this.hubMaster = hubMaster;
        if (hubs == null) return;
        for (Hub h : hubs) {
            add(h);
        }
    }
    
    public void add(Hub<T> hub) {
        if (alHub == null) alHub = new ArrayList<Hub<T>>();
        alHub.add(hub);

        hub.addHubListener(new HubListenerAdapter<T>() {
            @Override
            public void afterAdd(HubEvent<T> e) {
                hubMaster.add(e.getObject());
            }
            @Override
            public void afterInsert(HubEvent<T> e) {
                afterAdd(e);
            }
            @Override
            public void afterRemove(HubEvent<T> e) {
                T obj = e.getObject();
                for (Hub<T> h :alHub) {
                    if (h.contains(obj)) return;
                }
                hubMaster.remove(obj);
            }
            @Override
            public void beforeRemoveAll(HubEvent<T> e) {
                Hub h = e.getHub();
                for (Object obj : h) {
                    boolean b = false;
                    for (Hub<T> hx :alHub) {
                        if (hx != h && hx.contains(obj)) {
                            b = true;
                            break;
                        }
                    }
                    if (!b) hubMaster.remove(obj);
                }
            }
        });
        
        for (T obj : hub) {
            hubMaster.add(obj);
        }
    }
    
    public void refresh() {
        hubMaster.clear();
        for (Hub<T> h :alHub) {
            for (T obj : h) {
                hubMaster.add(obj);
            }
        }
    }
    
    
}
