package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OAPropertyPath;

/**
 * Find other siblings objects that need the same property loaded.
 * Used by DS and CS to be able to get extra data and increase performance. 
 * @author vvia
 */
public class OAObjectSiblingDelegate {
    private static final OAObject[] lastMasterObjects = new OAObject[10];
    private static final AtomicInteger aiLastMasterCnter = new AtomicInteger();

    //qqqqqqqqqqqq  ONE: keys for to object to get (one side),
    //qqqqqq MANY: keys are one side
    
    /**
     * Returns first 100 found
     */
    public static OAObjectKey[] getSiblings(final OAObject mainObject, final String property, final int max) {
        Hub hub = OAThreadLocalDelegate.getGetDetailHub();
        final String propertyPath = OAThreadLocalDelegate.getGetDetailPropertyPath();
        
        final OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
        
        final ArrayList<OAObjectKey> alObjectKey = new ArrayList<>();
        final HashSet<OAObjectKey> hsKeys = new HashSet<>();

        if (hub != null && propertyPath != null) {
            // set by HubMerger, HubGroupBy, LoadReferences, etc - where it will be loading from a Root Hub using a PropertyPath
            OAPropertyPath pp = new OAPropertyPath(hub.getObjectClass(), propertyPath);
            String spp = null;
            for (OALinkInfo li : pp.getLinkInfos()) {
                if (property.equalsIgnoreCase(li.getName())) break;
                if (spp == null) spp = li.getName();
                else spp += "." + li.getName();
            }
            findSiblings(alObjectKey, hub, spp, property, linkInfo, mainObject, hsKeys, max);
        }
        else {
            if (hub == null) {
                hub = findBestSiblingHub(mainObject);
            }
            
            OALinkInfo lix = linkInfo;
            String spp = null;
            for ( ; hub!=null; ) {
                findSiblings(alObjectKey, hub, spp, property, linkInfo, mainObject, hsKeys,max);

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
    
    
    /**
     * Find any other siblings to get the same property for sibling objects in same hub.
     */
    protected static OAObjectKey[] getSiblings(final OAObject masterObject, final OALinkInfo linkInfo, final String property, final Hub detailHub) {
        // note: could be for a blob property
        Hub siblingHub = null;
        if (detailHub != null && detailHub.contains(masterObject)) {
            siblingHub = detailHub;
        }
        if (siblingHub == null) {
            siblingHub = findBestSiblingHub(masterObject);
            if (siblingHub == null) return null;
        }
        
        ArrayList<OAObjectKey> al = new ArrayList<OAObjectKey>();
        _findSiblings(new HashSet(), al, masterObject, siblingHub, linkInfo, property, detailHub!=null, detailHub);
        
        lastMasterObjects[aiLastMasterCnter.getAndIncrement()%lastMasterObjects.length] = masterObject;
        
        if (al == null || al.size() == 0) return null;
        int x = al.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        al.toArray(keys);
        return keys;
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
                if (siblingHub.getMasterObject() != null) siblingHits++;
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
            if (hub.getMasterObject() != null) hits++;
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
    
    
    
    private static void _findSiblings(HashSet<Object> hsValues, ArrayList<OAObjectKey> alResults, final OAObject masterObject, 
            final Hub siblingHub, OALinkInfo linkInfo, String propertyName, final boolean bAgressive, final Hub detailHub) {
        
        final boolean bUsesDetail = detailHub != null; 
        
        _findSiblingsA(hsValues, alResults, masterObject, siblingHub, linkInfo, propertyName, bAgressive, detailHub);
        if (alResults.size() > (bUsesDetail?200:25) || linkInfo == null) return;

        // go up to master.parent and get siblings from there
        OAObject parentMasterObject = siblingHub.getMasterObject();
        if (parentMasterObject == null) return;
        
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(siblingHub);
        if (li == null) return;
             
        OALinkInfo liRev = li.getReverseLinkInfo();
        if (liRev == null) return;
        if (liRev.getType() != OALinkInfo.MANY) return;
        
        Hub parentSiblingHub = findBestSiblingHub(parentMasterObject);
        if (parentSiblingHub == null) return;
        
        int pos = parentSiblingHub.getPos(parentMasterObject);
        
        if (pos < 0) pos = 0;
        else if (pos == 0) pos++;
        else {
            // might want to go before
            OAObject obj = (OAObject) parentSiblingHub.getAt(pos-1);
            if (!OAObjectPropertyDelegate.isPropertyLoaded(obj, liRev.getName())) {
                pos -= 20;
                if (pos < 0) pos = 0;
            }
            else {
                Object objx = liRev.getValue(obj);
                if (objx instanceof Hub) {
                    Hub h = (Hub) objx;
                    obj = (OAObject) h.getAt(0);
                    if (obj != null && !OAObjectPropertyDelegate.isPropertyLoaded(obj, liRev.getName())) {
                        pos -= 20;
                        if (pos < 0) pos = 0;
                    }
                    else pos++;
                }
            }
        }
        
        for (int i=0; i<250; i++) {
            Object obj = parentSiblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == parentMasterObject) continue;
            
            if (!OAObjectPropertyDelegate.isPropertyLoaded((OAObject)obj, liRev.getName())) continue;

            Hub h = (Hub) liRev.getValue(obj);
            if (h.getSize() > 0) {
                _findSiblingsA(hsValues, alResults, masterObject, h, linkInfo, propertyName, bAgressive, detailHub);
            }
            if (alResults.size() > (bUsesDetail?200:100)) break;
        }        
    }    
    
    
    private static void _findSiblingsA(HashSet<Object> hsValues, ArrayList<OAObjectKey> alResults, 
            OAObject masterObject, Hub siblingHub, OALinkInfo linkInfo, String property, boolean bAgressive, final Hub detailHub) {
        // get the same property for siblings

        final boolean bUsesDetail = detailHub != null; 
        
        // find best starting pos, either before or after
        int pos = siblingHub.getPos(masterObject);
        if (pos < 0) pos = 0;
        else if (pos == 0) pos++;
        else {
            // find out what direction to start at
            OAObject obj = (OAObject) siblingHub.getAt(pos-1);
            if (!OAObjectPropertyDelegate.isPropertyLoaded(obj, property)) {
                pos -= (linkInfo == null)?5:20;
                if (pos < 0) pos = 0;
            }
            else pos++;
        }
        
        Class valueClass = null;
        boolean bIsOne2One = false;
        boolean bIsMany = false;

        if (linkInfo != null) {
            valueClass = linkInfo.getToClass();
            bIsOne2One = OAObjectInfoDelegate.isOne2One(linkInfo);
            bIsMany = linkInfo.getType() == linkInfo.MANY;
        }
        
        for (int i=0; i<350; i++) {
            Object obj = siblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == masterObject) continue;

            OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
            Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, property, true, true);

            if (value instanceof OANotExist) {
                if (linkInfo == null) {  // must be blob
                    alResults.add(key);
                    if (alResults.size() >= 25) break;  // only get 25 extra blobs, ha
                }
                else if (bIsMany || bIsOne2One) {                
                    alResults.add(key);
                    if (bUsesDetail) {
                        if (alResults.size() > 200) break;
                    }
                    else if (alResults.size() >= (100*(bAgressive?2:1))) break;
                } 
                // otherwise, it must be null
            }
            else if (value instanceof OAObjectKey) {
                if (!hsValues.contains(value)) {
                    hsValues.add(value);
                    value = OAObjectCacheDelegate.get(valueClass, value);
                    if (value == null) { // not on client
                        alResults.add(key);
                        if (bUsesDetail) {
                            if (alResults.size() > 200) break;
                        }
                        else if (alResults.size() > (100*(bAgressive?2:1))) break;
                    }
                }
            }
            // note: if value is null and a Many, then it's value is an empty Hub
        }
    }
    
}
