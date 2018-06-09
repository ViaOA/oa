package com.viaoa.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;
import com.viaoa.util.OAThrottle;

/**
 * Find the closet siblings objects that need the same property loaded.
 * Used by DS and CS to be able to get extra data per request to server/datasource, and increase performance.
 * @author vvia
 */
public class OAObjectSiblingDelegate {

    private final static long MaxMs = 25; // max ms for finding
    
    private static final OAThrottle throttle = new OAThrottle(250);
    
    
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
     * @param hmIgnore ignore list, because they are "inflight" with other concurrent requests
     * @return list of keys that are siblings
     */
    public static OAObjectKey[] getSiblings(final OAObject mainObject, final String property, final int maxAmount, ConcurrentHashMap<Integer, Boolean> hmIgnore) {
        long msStarted = System.currentTimeMillis();
        OAObjectKey[] keys = _getSiblings(mainObject, property, maxAmount, hmIgnore, msStarted);
        long x = (System.currentTimeMillis()-msStarted);         
        if (throttle.check() || x > (MaxMs*2)) {
            if (OAObject.getDebugMode()) {
                System.out.println((throttle.getCheckCount())+") OAObjectSiblingDelegate "+x+"ms, obj="+mainObject.getClass().getSimpleName()+", prop="+property+", hmIgnore="+(hmIgnore==null?0:hmIgnore.size())+", alRemove="+keys.length);
            }
        }
        return keys;
    }
    public static OAObjectKey[] _getSiblings(final OAObject mainObject, final String property, final int maxAmount, ConcurrentHashMap<Integer, Boolean> hmIgnore, final long msStarted) {
        if (mainObject == null || OAString.isEmpty(property) || maxAmount < 1) return null;

        if (hmIgnore == null) hmIgnore = new ConcurrentHashMap<>();
        
        final OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
        
        // set by Finder, HubMerger, HubGroupBy, LoadReferences, etc - where it will be loading from a Root Hub using a PropertyPath
        Hub getDetailHub = OAThreadLocalDelegate.getGetDetailHub();
        String getDetailPropertyPath = OAThreadLocalDelegate.getGetDetailPropertyPath();

        OAPropertyPath ppGetDetailPropertyPath = null;
        if (getDetailHub != null && getDetailPropertyPath != null) {
            try {
                ppGetDetailPropertyPath = new OAPropertyPath(getDetailHub.getObjectClass(), getDetailPropertyPath);
            }
            catch (Exception e) {
                getDetailHub = null;
                getDetailPropertyPath = null;
            }
        }
        
        String ppPrefix = null;
        boolean bValid = false;
        if (ppGetDetailPropertyPath != null) {
            // see if property is in the detailPP
            boolean b = false;
            for (OALinkInfo li : ppGetDetailPropertyPath.getLinkInfos()) {
                if (property.equalsIgnoreCase(li.getName())) {
                    bValid = true;
                    break;
                }
                if (b) {
                    // found mainObj, but next prop was not found
                    b = false;
                    OALinkInfo lix = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
                    if (lix != null) {
                        bValid = true;
                        break;
                    }
                }
                if (mainObject.getClass().equals(li.getToClass())) {
                    b = true;
                }
                
                if (ppPrefix == null) ppPrefix = li.getName();
                else ppPrefix += "." + li.getName();
            }
            if (b) {
                OALinkInfo lix = OAObjectInfoDelegate.getLinkInfo(mainObject.getClass(), property);
                if (lix != null) {
                    bValid = true;
                }
            }
            
            if (!bValid) {
                // see if property is off of the detailPP
                ppPrefix = null;
                for (OALinkInfo li : ppGetDetailPropertyPath.getLinkInfos()) {
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
                bValid = false;
            }
            else {
                ppPrefix = li.getName();
                bValid = true;
            }
        }

        final ArrayList<OAObjectKey> alObjectKey = new ArrayList<>();
        final HashMap<OAObjectKey, OAObjectKey> hsKeys = new HashMap<>();
        
        Hub hub = null;
        OAPropertyPath ppReverse = null;
        
        if (getDetailHub != null && ppPrefix != null) {
            OAPropertyPath ppForward = new OAPropertyPath(getDetailHub.getObjectClass(), ppPrefix);
            OALinkInfo[] lis = ppForward.getLinkInfos();
            boolean b = true;
            if (lis != null) {
                for (OALinkInfo li : lis) {
                    if (li.getType() != OALinkInfo.TYPE_MANY) {
                        b = false;
                        break;
                    }
                }
            }
            if (b) {
                ppReverse = ppForward.getReversePropertyPath();
            }
        }

        OAObject objInHub = mainObject;
        int ppReversePos = -1;

        OALinkInfo lix = null;
        if (ppReverse != null) {
            OALinkInfo[] lis = ppReverse.getLinkInfos();
            if (lis != null && lis.length > 0) lix = lis[0];
            hub = findBestSiblingHub(mainObject, lix);
            ppPrefix = null;  
        }
        else if (getDetailHub != null) {
            hub = getDetailHub;
            if (ppPrefix != null) {
                OAFinder f = new OAFinder(ppPrefix) {
                    @Override
                    protected boolean isUsed(OAObject obj) {
                        return obj == mainObject;
                    }
                };
                f.setUseOnlyLoadedData(true);
                if (f.findFirst(hub) == null) {
                    objInHub = null;
                }
                else {
                    objInHub = (OAObject) hub.getAt(f.getRootHubPos());
                }
            }
        }
        else {
            hub = findBestSiblingHub(mainObject, null);
            ppPrefix = null;  
        }

        int max = maxAmount;
        if (hub == null || getDetailHub == hub) {
        }
        else if (ppReverse != null) {
        }
        else if (hub.getMasterHub() != null) {
            max *= .90;
        }
        else if (hub.getMasterObject() != null) {
            max *= .80;
        }
        else {
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(mainObject);
            if (hubs != null && hubs.length > 1) {
                max *= .75;
            }
        }
        
        final HashSet<Hub> hsHubVisited = new HashSet<>();
        final HashMap<OAObjectKey, OAObject> hmTypeOneObjKey = new HashMap<>();
        
        final OACascade cascade = new OACascade();
        
        for (int cnt=0 ; hub!=null; cnt++) {
            if (hsHubVisited.contains(hub)) break;
            hsHubVisited.add(hub);
            
            int startPosHubRoot = hub.getPos(objInHub);
            int x = max;
            for (int i=0; i<=cnt; i++) {
                x /= 2;
            }
            x = Math.min(x, 25);
            startPosHubRoot = Math.max(0, startPosHubRoot - x);
            
            findSiblings(alObjectKey, hub, startPosHubRoot, ppPrefix, property, linkInfo, mainObject, hmTypeOneObjKey, hmIgnore, max, cascade, msStarted, cnt);
            if (alObjectKey.size() >= max) break;

            if (msStarted > 0) {
                long lx = (System.currentTimeMillis()-msStarted);
                if (lx > MaxMs) { //  && !OAObject.getDebugMode()) {
                    break;
                }
            }
            if (cnt > 3) break;
            if (cascade.getVisitCount() > 750) break;
            
            // find next hub to use
            
            lix = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(hub);
            if (lix == null || lix.getToClass() == null) break;  // could be using GroupBy as hub
            
            
            objInHub = hub.getMasterObject();
            
            Hub hubx = null;
            if (ppReverse != null && objInHub != null) {
                OALinkInfo[] lis = ppReverse.getLinkInfos();

                if (ppReversePos < 0) {
                    ppReversePos = 0;
                    if (ppPrefix == null) ppPrefix = lix.getName();
                    else ppPrefix = lix.getName() + "." + ppPrefix;
                }                
                
                OALinkInfo liz = (lis == null || lis.length <= ppReversePos) ? null : lis[ppReversePos];
                if (liz != null && liz.getToClass().equals(objInHub.getClass())) {
                    // could be recursive
                    OALinkInfo lizRecursive = OAObjectInfoDelegate.getObjectInfo(liz.getToClass()).getRecursiveLinkInfo(OALinkInfo.TYPE_ONE);
                    if (lizRecursive != null) {
                        hubx = findBestSiblingHub(objInHub, lizRecursive);
                        if (hubx != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(hubx) != lizRecursive) {
                            hubx = null;
                        }
                    }
                    if (hubx == null) {
                        ppReversePos++;
                        liz = (lis == null || lis.length <= ppReversePos) ? null : lis[ppReversePos];
                        hubx = findBestSiblingHub(objInHub, liz);
                        if (hubx != null && liz != null) {
                            if (ppPrefix == null) ppPrefix = liz.getName();
                            else ppPrefix = liz.getName() + "." + ppPrefix;
                        }
                    }
                    hub = hubx;
                }
            }
            
            if (hubx == null && hub != null) {
                if (ppPrefix == null) ppPrefix = lix.getName();
                else ppPrefix = lix.getName() + "." + ppPrefix;
                
                hubx = hub.getMasterHub();
                if (hubx != null) {
                    hub = hubx;
                }
                else {
                    if (objInHub == null) break;
                    hub = findBestSiblingHub(objInHub, null);
                }
            }
        }
        
        int x = alObjectKey.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        alObjectKey.toArray(keys);
        
        return keys;
    }
    
    
    protected static void findSiblings(
            final ArrayList<OAObjectKey> alFoundObjectKey, 
            final Hub hubRoot, final int startPosHubRoot, final String finderPropertyPath, final String origProperty, 
            final OALinkInfo linkInfo, 
            final OAObject mainObject, 
            final HashMap<OAObjectKey, OAObject> hmTypeOneObjKey, // for calling thread, refobjs already looked at
            final ConcurrentHashMap<Integer, Boolean> hmIgnore,  // for all threads
            final int max,
            final OACascade cascade,
            final long msStarted,
            final int runCount
            ) 
    {
        
        final String property = origProperty.toUpperCase();
        final boolean bIsMany = (linkInfo != null) && (linkInfo.getType() == OALinkInfo.TYPE_MANY);
        boolean b = !bIsMany && (linkInfo != null) && (linkInfo.isOne2One());
        if (b) {
            OALinkInfo rli = linkInfo.getReverseLinkInfo();
            if (!linkInfo.getPrivateMethod() && rli != null && rli.getPrivateMethod()) b = false;
        }
        final boolean bNormalOne2One = b; 
        
        
        final Class clazz = (linkInfo == null) ? null : linkInfo.getToClass();
        
        OAFinder f = new OAFinder(finderPropertyPath) {
            @Override
            protected boolean isUsed(OAObject oaObject) {
                if (oaObject == mainObject) {
                    return false;
                }

                Object propertyValue = OAObjectPropertyDelegate.getProperty(oaObject, property, true, true);

                if (bIsMany) {
                    if (propertyValue instanceof Hub) return false; 
                }
                else if (linkInfo != null && propertyValue instanceof OAObject) {
                    return false;
                }
                else if (linkInfo != null && propertyValue instanceof OAObjectKey) {
                    if (hmTypeOneObjKey.containsKey((OAObjectKey) propertyValue)) return false;
                    hmTypeOneObjKey.put((OAObjectKey) propertyValue, null);
                    if (OAObjectCacheDelegate.get(clazz, (OAObjectKey) propertyValue) != null) return false;
                }
                else if (linkInfo != null) {
                    if (!bNormalOne2One) return false;
                }
                else if (linkInfo == null) {  // must be blob
                    if (!(propertyValue instanceof OANotExist)) return false;
                }
                
                hmIgnore.put(oaObject.getGuid(), Boolean.TRUE);
                OAObjectKey ok = oaObject.getObjectKey();
                if (ok.guid == 0) {
                    ok.guid = oaObject.getGuid();
                }
                alFoundObjectKey.add(ok);
                if (alFoundObjectKey.size() >= max) {
                    stop();
                }
                if (runCount > 0 && cascade.getVisitCount() > 750) {
                    stop();
                }

                return false; // always returns
            }
            @Override
            protected void find(Object obj, int pos) {
                super.find(obj, pos);
                if (msStarted > 0) {
                    long lx = (System.currentTimeMillis()-msStarted);
                    if (lx > MaxMs) { // && !OAObject.getDebugMode()) {
                        stop();
                    }
                }
                if (runCount > 0 && cascade.getVisitCount() > 750) {
                    stop();
                }
            }
        };
        f.setUseOnlyLoadedData(true);
        f.setCascade(cascade);
        OAObject objx = null;
        if (startPosHubRoot > 0) objx = (OAObject) hubRoot.getAt(startPosHubRoot-1);
        f.find(hubRoot, objx);
    }
    
    
    // find the Hub that has the best set of siblings
    public static Hub findBestSiblingHub(OAObject masterObject, OALinkInfo liToMaster) {
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(masterObject);
        
        int siblingHits = 0;
        Hub siblingHub = null;
        
        for (int i=0; (hubs != null && i < hubs.length); i++) {
            Hub hub = hubs[i];
            if (hub == null) continue;

            if (liToMaster != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(hub) == liToMaster) {
                siblingHub = hub;
                break;
            }

            int hits = 1;
            if (hub.getMasterHub() != null) hits += 3;
            else if (hub.getMasterObject() != null) hits += 2;
            
            if (hits > siblingHits) {
                siblingHits = hits;
                siblingHub = hub;
            }
            else if (hits == siblingHits) {
                if (hub.getSize() > siblingHub.getSize()) siblingHub = hub;
            }
        }
        return siblingHub;
    }
}    
