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

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Combines multiple hubs into one.
*/
public class HubCombined<T> {
    private static Logger LOG = Logger.getLogger(HubCombined.class.getName());
    private static final long serialVersionUID = 1L;

    protected final Hub<T> hubMaster;
    protected final ArrayList<Hub<T>> alHub = new ArrayList<>();
    protected ArrayList<HubListener<T>> alHubListener;
    protected final HubListener<T> hlMaster;
    protected Hub<T> hubFirst;
    
    public HubCombined(final Hub<T> hubMaster, final Hub<T> ... hubs) {
        this.hubMaster = hubMaster;
        
        if (hubs != null) {
            for (Hub h : hubs) {
                add(h);
            }
        }
        
        hlMaster = new HubListenerAdapter<T>() {
            @Override
            public void afterAdd(HubEvent<T> e) {
                T objx = e.getObject();
                boolean bUsed = true;
                for (Hub h : hubs) {
                    if (h.contains(objx)) {
                        bUsed = false;
                        break;
                    }
                }
                if (bUsed && hubFirst != null) {
                    if (hubFirst.isValid()) {
                        hubFirst.add(e.getObject());
                    }
                    else {
                        //int xx = 4;
                        //xx++;
                    }
                }
            }
            @Override
            public void afterInsert(HubEvent<T> e) {
                afterAdd(e);
            }
            @Override
            public void afterRemove(HubEvent<T> e) {
                T obj = e.getObject();
                for (Hub<T> h :alHub) {
                    h.remove(obj);
                }
            }
            @Override
            public void beforeRemoveAll(HubEvent<T> e) {
                for (T obj : hubMaster) {
                    for (Hub<T> h :alHub) {
                        h.remove(obj);
                    }
                }
            }
        };
        hubMaster.addHubListener(hlMaster);
    }
    
    public void close() {
        int i = 0;
        if (alHubListener != null) {
            for (Hub h : alHub) {
                h.removeHubListener(alHubListener.get(i++));
            }
            alHubListener.clear();
        }
        alHub.clear();
        if (hlMaster != null) {
            hubMaster.removeHubListener(hlMaster);
        }
    }
    
    public void add(Hub<T> hub) {
        if (alHub.size() == 0) hubFirst = hub;
        alHub.add(hub);

        HubListener hl = new HubListenerAdapter<T>() {
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
                boolean bUsed = false;
                for (Hub<T> hx : alHub) {
                    if (hx.contains(obj)) {
                        bUsed = true;
                        break;
                    }
                }
                if (!bUsed) hubMaster.remove(obj);
            }
            @Override
            public void afterRemoveAll(HubEvent<T> e) {
                onNewList(e);
            }
            @Override
            public void onNewList(HubEvent<T> e) {
                for (Object obj : hubMaster) {
                    boolean bUsed = false;
                    for (Hub<T> hx : alHub) {
                        if (hx.contains(obj)) {
                            bUsed = true;
                            break;
                        }
                    }
                    if (!bUsed) hubMaster.remove(obj);
                }
                for (T obj : e.getHub()) {
                    hubMaster.add(obj);
                }
            }
        };
        hub.addHubListener(hl);
        if (alHubListener == null) alHubListener = new ArrayList<HubListener<T>>();
        alHubListener.add(hl);
        
        for (T obj : hub) {
            hubMaster.add(obj);
        }
    }
    public void refresh() {
        for (Object obj : hubMaster) {
            boolean bUsed = false;
            for (Hub<T> hx : alHub) {
                if (hx.contains(obj)) {
                    bUsed = true;
                    break;
                }
            }
            if (!bUsed) hubMaster.remove(obj);
        }
        for (Hub<T> hx : alHub) {
            for (T obj : hx) {
                hubMaster.add(obj);
            }
        }
    }
    
    
}
