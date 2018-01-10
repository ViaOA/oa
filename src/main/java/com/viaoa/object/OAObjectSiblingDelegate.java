package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Find the closet siblings objects that need the same property loaded.
 * Used by DS and CS to be able to get extra data per request to server/datasource, and increase performance.
 * @author vvia
 */
public class OAObjectSiblingDelegate {
    private static final OAObject[] lastMasterObjects = new OAObject[10];
    private static final AtomicInteger aiLastMasterCnter = new AtomicInteger();

    /**
     * Used to find any siblings that also need the same property loaded.
     */
    public static OAObjectKey[] getSiblings(final OAObject mainObject, final String property, final int maxAmount) {
        return getSiblings(mainObject, property, maxAmount, null);
    }
    
    /**
     * 
     * @param mainObject
     * @param property
     * @param maxAmount
     * @param hmSibling guid of objects to ignore, because they are "inflight"
     * @return
     */
    public static OAObjectKey[] getSiblings(final OAObject mainObject, final String property, final int maxAmount, final ConcurrentHashMap<Integer, Integer> hmIgnoreSibling) {
        if (mainObject == null || OAString.isEmpty(property) || maxAmount < 1) return null;

        final OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
        
        // set by Finder, HubMerger, HubGroupBy, LoadReferences, etc - where it will be loading from a Root Hub using a PropertyPath
        Hub getDetailHub = OAThreadLocalDelegate.getGetDetailHub();
        String getDetailPropertyPath = OAThreadLocalDelegate.getGetDetailPropertyPath();

        String ppPrefix = null;
        boolean bValid = false;
        if (getDetailHub != null && getDetailPropertyPath != null) {
            // see if property is in the detailPP
            OAPropertyPath pp = new OAPropertyPath(getDetailHub.getObjectClass(), getDetailPropertyPath);
            for (OALinkInfo li : pp.getLinkInfos()) {
                if (property.equalsIgnoreCase(li.getName())) {
                    bValid = true;
                    
                    if (li.getRecursive()) {
                        OALinkInfo rli = li.getReverseLinkInfo();
                        Object objx = mainObject;
                        for ( ; rli != null; ) {
                            objx = rli.getValue(objx);
                            if (objx == null) break;
                            if (ppPrefix == null) ppPrefix = li.getName();
                            else ppPrefix += "." + li.getName();
                        }
                    }
                    break;
                }
                if (ppPrefix == null) ppPrefix = li.getName();
                else ppPrefix += "." + li.getName();
            }
            if (!bValid) {
                // see if property is off of the detailPP
                ppPrefix = null;
                for (OALinkInfo li : pp.getLinkInfos()) {
                    Class c = li.getToClass();
                    OALinkInfo lix = OAObjectInfoDelegate.getLinkInfo(c, mainObject.getClass());
                    if (lix != null) {
                        if (!lix.getPrivateMethod()) bValid = true;
                        break;
                    }
                    if (ppPrefix == null) ppPrefix = li.getName();
                    else ppPrefix += "." + li.getName();
                }
            }
        }
        
        if (!bValid && getDetailHub != null && !getDetailHub.getObjectClass().equals(mainObject.getClass())) {
            // need to get to mainObject.class
            Class c = getDetailHub.getObjectClass();
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(c, mainObject.getClass());
            if (li == null || li.getPrivateMethod()) {
                getDetailHub = null;
                ppPrefix = null;
            }
            else {
                ppPrefix = li.getName();
                bValid = true;
            }
        }
        
        final ArrayList<OAObjectKey> alObjectKey = new ArrayList<>();
        final HashMap<OAObjectKey, OAObjectKey> hsKeys = new HashMap<>();
        
        int max = maxAmount;
        Hub hub = getDetailHub;
        
        if (bValid) {
        }
        else if (getDetailHub == null || !getDetailHub.getObjectClass().equals(mainObject.getClass())) {
            hub = findBestSiblingHub(mainObject);
            
            if (hub == null);
            else if (OAObjectHubDelegate.getHubReferences(mainObject).length == 1);
            else if (hub.getMasterHub() != null) max *= .80;
            else if (hub.getMasterObject() != null) max *= .60;
            else max *= .50;
        }
            
        OALinkInfo lix = linkInfo;
        final HashSet<Hub> hsHubVisited = new HashSet<>();
        for (int cnt=0 ; hub!=null; cnt++) {
            if (hsHubVisited.contains(hub)) break;
            hsHubVisited.add(hub);
            
            findSiblings(alObjectKey, hub, ppPrefix, property, linkInfo, mainObject, hsKeys, max, hmIgnoreSibling);

            if (alObjectKey.size() >= max) break;

            lix = HubDetailDelegate.getLinkInfoFromMasterToDetail(hub);
            if (lix == null || lix.getToClass() == null) break;  // could be using GroupBy as hub
            if (ppPrefix == null) ppPrefix = lix.getName();
            else ppPrefix = lix.getName() + "." + ppPrefix;
            
            Hub hx = hub.getMasterHub();
            if (hx != null) {
                if (cnt > 3) break;
                Object objx = hub.getMasterObject();
                if (objx == null) break;
                if (!objx.getClass().equals(hx.getObjectClass())) {
                    break;
                }
                hub = hx;
            }
            else {
                if (cnt > 2) break;
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
    
    protected static void findSiblings(final ArrayList<OAObjectKey> alObjectKey, final Hub hub, String spp, final String property, final OALinkInfo linkInfo, 
            final OAObject mainObject, final HashMap<OAObjectKey, OAObjectKey> hmObjKeyPos, final int max, final ConcurrentHashMap<Integer, Integer> hmIgnoreSibling) {
        final boolean bIsMany = (linkInfo != null) && (linkInfo.getType() == OALinkInfo.TYPE_MANY);
        final boolean bIsOne2One = !bIsMany && (linkInfo != null) && (linkInfo.isOne2One());
        final Class clazz = (linkInfo == null) ? null : linkInfo.getToClass();
        
        final int cntPreviousFound = alObjectKey.size();
        final LinkedList<OAObjectKey> llObjectKey = new LinkedList<>();
        
        OAFinder f = new OAFinder(hub, spp) {
            int cntAfterMain = -1;
            @Override
            protected boolean isUsed(OAObject oaObject) {
                Object propertyValue = OAObjectPropertyDelegate.getProperty(oaObject, property, true, true);

                if (oaObject == mainObject) {
                    if (propertyValue instanceof OAObjectKey) {
                        OAObjectKey ok = (OAObjectKey) propertyValue;
                        OAObjectKey okx  = hmObjKeyPos.put(ok, oaObject.getObjectKey());
                        if (okx != null) {
                            if (llObjectKey.remove(okx)) {
                                if (hmIgnoreSibling != null) hmIgnoreSibling.remove(okx.getGuid());
                            }
                        }
                    }
                    cntAfterMain = 0; 
                    return false;
                }

                OAObjectKey objectKey = oaObject.getObjectKey();
                boolean bAdd = false;
                
                if (bIsMany) {
                    if (!(propertyValue instanceof Hub)) {
                        bAdd = true;
                    }
                }
                else if (linkInfo != null && propertyValue instanceof OAObjectKey) {
                    OAObjectKey ok = (OAObjectKey) propertyValue;
                    OAObjectKey okx  = hmObjKeyPos.put(ok, oaObject.getObjectKey());
                    if (okx != null) {
                        if (cntAfterMain >= 0 && okx.equals(mainObject.getObjectKey())) { 
                            hmObjKeyPos.put(ok, okx);
                        }
                        else {
                            if (llObjectKey.remove(okx)) {
                                if (hmIgnoreSibling != null) hmIgnoreSibling.remove(okx.getGuid());
                                bAdd = true;
                            }
                        }
                    }
                    else {
                        if (OAObjectCacheDelegate.get(clazz, ok) == null) {
                            bAdd = true;
                        }
                    }
                }
                else if (bIsOne2One && propertyValue == null) {
                    bAdd = true;
                }
                else if (linkInfo == null) {  // must be blob
                    if (propertyValue instanceof OANotExist) {
                        bAdd = true;
                    }
                }
                
                if (bAdd && hmIgnoreSibling != null) {
                    int guid = oaObject.getGuid();
                    if (hmIgnoreSibling.contains(guid)) return false;
                }
                
                if (bAdd && !llObjectKey.contains(objectKey) && !alObjectKey.contains(objectKey)) {
                    if (!OAObjectPropertyDelegate.isPropertyLocked(oaObject, property)) {
                        if (cntAfterMain >= 0) cntAfterMain++;
                        llObjectKey.add(objectKey);
                        if (hmIgnoreSibling != null) hmIgnoreSibling.put(objectKey.getGuid(), objectKey.getGuid());
                        if (max > 0) {
                            int x = llObjectKey.size()+cntPreviousFound;
                            if (x >= max) {
                                if (x > max) {
                                    OAObjectKey okx = llObjectKey.remove(0);
                                    if (okx != null) {
                                        if (hmIgnoreSibling != null) hmIgnoreSibling.remove(okx.getGuid());
                                    }
                                }
                                if (cntAfterMain >= ((max-cntPreviousFound)/2)) {
                                    stop();
                                }
                            }
                        }
                    }
                }
                return false; 
            }
        };
        f.setUseOnlyLoadedData(true);
        f.find();
        
        for (OAObjectKey ok : llObjectKey) {
            alObjectKey.add(ok);
        }
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
