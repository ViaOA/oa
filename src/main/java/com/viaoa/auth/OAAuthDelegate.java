package com.viaoa.auth;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;

/**
 * System wide service for getting the current User object.
 * @author vvia
 */
public class OAAuthDelegate {

    private static final ArrayList<OAAuthLookupInterface> al = new ArrayList<>();
    private static final ConcurrentHashMap<Object, Hub<OAObject>> hmUserHub = new ConcurrentHashMap<>(); 
    
    
    public static OAObject getCurrentUser() {
        OAObject obj = null;
        for (OAAuthLookupInterface ali : al) {
            obj = ali.getCurrentUser();
            if (obj != null) break;
        }
        return obj;
    }
    
    
    /**
     * Add a new provider, and also update hmUserHub
     */
    public static void add(OAAuthLookupInterface ali) {
        if (ali != null && !al.contains(ali)) al.add(ali);
        OAObject user = getCurrentUser();
        
        for (Object key : hmUserHub.keySet()) {
            Hub hub = hmUserHub.get(key);
            if (hub == null) continue;
            if (hub.size() == 0 || !hub.contains(user)) {
                hub.add(user);
            }
            hub.setAO(user);
        }
    }
    
    /**
     * Holds a Hub<User> with AO=user, that can be shared using a key.
     * @param key ex: OAJfcController.class
     * @param bAutoCreate
     * @return
     */
    public static Hub<OAObject> getCurrentUserHub(Object key, boolean bAutoCreate) {
        if (key == null) return null;
        Hub<OAObject> hub = hmUserHub.get(key);
        if (hub == null && bAutoCreate) {
            hub = new Hub<OAObject>(OAObject.class);
            hmUserHub.put(key, hub);
        }
        if (hub == null) return null;
        OAObject user = getCurrentUser();
        if (hub.size() == 0 || !hub.contains(user)) {
            hub.add(user);
        }
        hub.setAO(user);
        return hub;
    }
    
    
}
