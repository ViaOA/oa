package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Find other siblings objects that need the same property loaded.
 * Used by DS and CS to be able to get extra data and increase performance.
 * @author vvia
 */
public class OAObjectSiblingDelegate {
    private static final OAObject[] lastMasterObjects = new OAObject[10];
    private static final AtomicInteger aiLastMasterCnter = new AtomicInteger();

    /**
     * Used to find any siblings that also need the same property loaded.
     */
    public static OAObjectKey[] getSiblings(final OAObject mainObject, final String property, final int max) {
        if (mainObject == null || OAString.isEmpty(property) || max < 1) return null;

        final OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
        
        Hub hub = OAThreadLocalDelegate.getGetDetailHub();
        final String propertyPath = OAThreadLocalDelegate.getGetDetailPropertyPath();
        
        final ArrayList<OAObjectKey> alObjectKey = new ArrayList<>();
        final HashSet<OAObjectKey> hsKeys = new HashSet<>();

        String spp = null;
        if (hub != null && propertyPath != null) {
            // set by HubMerger, HubGroupBy, LoadReferences, etc - where it will be loading from a Root Hub using a PropertyPath
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPath);
            for (OALinkInfo li : pp.getLinkInfos()) {
                if (property.equalsIgnoreCase(li.getName())) break;
                if (spp == null) spp = li.getName();
                else spp += "." + li.getName();
            }
        }
        else if (hub == null) {
            hub = findBestSiblingHub(mainObject);
        }
            
        OALinkInfo lix = linkInfo;
        for ( ; hub!=null; ) {
            findSiblings(alObjectKey, hub, spp, property, linkInfo, mainObject, hsKeys, max);

            if (alObjectKey.size() >= max) break;

            lix = HubDetailDelegate.getLinkInfoFromMasterToDetail(hub);
            if (lix == null) break;
            if (spp == null) spp = lix.getName();
            else spp = lix.getName() + "." + spp;
            
            Hub hx = hub.getMasterHub();
            if (hx != null) {
                hub = hx;
            }
            else {
                Object objx = hub.getMasterObject();
                if (objx == null) break;
                hub = findBestSiblingHub((OAObject) objx);
            }
        }
        
        int x = alObjectKey.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        alObjectKey.toArray(keys);
        
        return keys;
    }
    
    protected static void findSiblings(final ArrayList<OAObjectKey> alObjectKey, Hub hub, String spp, final String property, final OALinkInfo linkInfo, final OAObject mainObject, final HashSet<OAObjectKey> hsKeys, final int max) {
        final boolean bIsMany = (linkInfo != null) && (linkInfo.getType() == OALinkInfo.TYPE_MANY);
        final Class clazz = (linkInfo == null) ? null : linkInfo.getToClass();
        
        OAFinder f = new OAFinder(hub, spp) {
            @Override
            protected boolean isUsed(OAObject obj) {
                Object objx = OAObjectPropertyDelegate.getProperty(obj, property, true, true);
                if (bIsMany) {
                    if (!(objx instanceof Hub)) {
                        OAObjectKey key = obj.getObjectKey();
                        if (!alObjectKey.contains(key)) {
                            alObjectKey.add(key);
                            if (alObjectKey.size() >= max) {
                                stop();
                            }
                        }
                    }
                }
                else if (objx instanceof OAObjectKey) {
                    OAObjectKey key = (OAObjectKey) objx;
                    if (!hsKeys.contains(key)) {
                        hsKeys.add(key);
                        if (OAObjectCacheDelegate.get(clazz, key) == null) {
                            alObjectKey.add(obj.getObjectKey());
                            if (alObjectKey.size() >= max) {
                                stop();
                            }
                        }
                    }
                }
                else if (linkInfo == null) {  // must be blob
                    if (objx instanceof OANotExist) {
                        OAObjectKey key = obj.getObjectKey();
                        alObjectKey.add(obj.getObjectKey());
                        if (alObjectKey.size() >= max) {
                            stop();
                        }
                    }
                }
                return false;
            }
        };
        f.setUseOnlyLoadedData(true);
        f.find();
    }
    
    
    // find the Hub that has the best set of siblings
    public static Hub findBestSiblingHub(OAObject masterObject) {
        Hub siblingHub = null;
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(masterObject);
        
        int siblingHits = 0;
        
        for (int i=0; (hubs != null && i < hubs.length); i++) {
            Hub hub = hubs[i];
            if (hub == null) continue;

            if (siblingHub == null) { 
                siblingHub = hub;
                continue;
            }
            
            if (hub.getSize() < 2) continue;
            
            // see if one of the previous objects can be found
            if (siblingHits == 0) {
                siblingHits = 1;  // so it wont be zero
                if (siblingHub.getMasterHub() != null) siblingHits+=3;
                if (siblingHub.getMasterObject() != null) siblingHits+=2;
                for (OAObject objz : lastMasterObjects) {
                    if (objz == null) break;
                    if (masterObject.getClass().equals(objz.getClass())) {
                        if (siblingHub.contains(objz)) {
                            siblingHits++;
                        }
                    }
                }
            }
            
            int hits = 1;
            if (hub.getMasterHub() != null) hits+=3;
            if (hub.getMasterObject() != null) hits+=2;
            for (OAObject objz : lastMasterObjects) {
                if (objz == null) break;
                if (masterObject.getClass().equals(objz.getClass())) {
                    if (hub.contains(objz)) {
                        hits++;
                    }
                }
            }
            
            if (hits > siblingHits) {
                siblingHits = hits;
                siblingHub = hub;
            }
            else if (hits == siblingHits) {
                if (hub.getSize() > siblingHub.getSize())  siblingHub = hub;
            }
        }
        return siblingHub;
    }
}    
