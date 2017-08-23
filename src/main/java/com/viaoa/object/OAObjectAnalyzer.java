package com.viaoa.object;

import java.util.HashSet;

import com.viaoa.hub.Hub;

public class OAObjectAnalyzer {

    
    HashSet<Hub> hsHub = new HashSet<Hub>();

    
    public void load() {

        for (Class cs : OAObjectCacheDelegate.getClasses()) {
            System.out.println("Starting class="+cs.getSimpleName()+", total="+OAObjectCacheDelegate.getTotal(cs));
            
            OACallback cb = new OACallback() {
                @Override
                public boolean updateObject(Object object) {
                    OAObject obj = (OAObject) object;
                    Hub[] hubs = OAObjectHubDelegate.getHubReferences(obj);
                    if (hubs == null) return true;
                    int cnt = 0;
                    for (Hub h : hubs) {
                        if (h == null) continue;
                        cnt++;
                        hsHub.add(h);
                    }
                    if (cnt > 10) {
                        System.out.println("   guid="+obj.getObjectKey().getGuid()+", cntHubs="+cnt);
                    }
                    return true;
                }
            };
            OAObjectCacheDelegate.callback(cs, cb);
        }    
        int xx = hsHub.size();
        xx++;
    }

    
    
    
}


