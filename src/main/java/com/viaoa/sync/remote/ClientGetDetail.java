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
package com.viaoa.sync.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.object.OAObjectSerializerCallback;
import com.viaoa.object.OAPerformance;
import com.viaoa.object.OASiblingHelper;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.OANotExist;

/**
 * This is used on the server for each clientSession, that creates a RemoteClientImpl for
 * getDetail(..) remote requests.  This class will return the object/s for the
 * request, and extra objects to include.
 * 
 * This works directly with OASyncClient.getDetail(..), returning a custom serializer for it.
 * @author vvia
 */
public class ClientGetDetail {
    private static Logger LOG = Logger.getLogger(ClientGetDetail.class.getName());
    private final int clientId;
    
    // tracks guid for all oaObjects serialized, the Boolean: true=all references have been sent, false=object has been sent (might not have all references)
    private final TreeMap<Integer, Boolean> treeSerialized = new TreeMap<Integer, Boolean>();
    private final ReentrantReadWriteLock rwLockTreeSerialized = new ReentrantReadWriteLock();

    public ClientGetDetail(int clientId) {
        this.clientId = clientId;
    }
    
    public void removeGuid(int guid) {
        try {
            rwLockTreeSerialized.writeLock().lock();
            treeSerialized.remove(guid);
        }
        finally {
            rwLockTreeSerialized.writeLock().unlock();
        }
    }
    
    public void addGuid(int guid) {
        try {
            rwLockTreeSerialized.writeLock().lock();
            if (treeSerialized.get(guid) == null) {
                treeSerialized.put(guid, false);
            }
        }
        finally {
            rwLockTreeSerialized.writeLock().unlock();
        }
    }
    
    public void close() {
        treeSerialized.clear();
    }
    
//    private static volatile int cntx;
    private static volatile int errorCnt;
    /**
     * called by OASyncClient.getDetail(..), from an OAClient to the OAServer
     * @param masterClass
     * @param masterObjectKey object key that needs to get a prop/reference value
     * @param property name of prop/reference
     * @param masterProps the names of any other properties to get
     * @param siblingKeys any other objects of the same class to get the same property from.  This is
     * usually objects in the same hub of the masterObjectKey
     * @return the property reference, or an OAObjectSerializer that will wrap the reference, along with additional objects
     * that will be sent back to the client.
     */
    public Object getDetail(final int id, final Class masterClass, final OAObjectKey masterObjectKey, 
            final String property, final String[] masterProps, final OAObjectKey[] siblingKeys, final boolean bForHubMerger) {

        if (masterObjectKey == null || property == null) return null;
        final long msStart = System.currentTimeMillis();

        Object masterObject = OAObjectReflectDelegate.getObject(masterClass, masterObjectKey);
        if (masterObject == null) {
            // get from datasource
            masterObject = (OAObject) OADataSource.getObject(masterClass, masterObjectKey);
            if (masterObject == null) {
                if (errorCnt++ < 100) LOG.warning("cant find masterObject in cache or DS.  masterClass=" + masterClass + ", key=" + masterObjectKey + ", property=" + property);
                return null;
            }
        }

        // 20171224 need to put siblings and masterObject in a Hub and call OAThreadLocal.detailHub        
        Hub hubHold = new Hub(masterClass);
        hubHold.add(masterObject);
        if (siblingKeys != null) {
            for (OAObjectKey key : siblingKeys) {
                OAObject obj = OAObjectCacheDelegate.get(masterClass, key);
                if (obj != null) {
                    hubHold.add(obj);
                }
            }
        }

        final OASiblingHelper siblingHelper = new OASiblingHelper(hubHold);
        OAThreadLocalDelegate.addSiblingHelper(siblingHelper); 
        Object detailValue = null;
        try {
            detailValue = OAObjectReflectDelegate.getProperty((OAObject) masterObject, property);
        }
        finally {
            OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        }
        hubHold.clear();
        hubHold = null;
        
        Object returnValue;
        boolean b = ((masterProps == null || masterProps.length == 0) && (siblingKeys == null || siblingKeys.length==0));
        if (b) {
            if (detailValue instanceof Hub) {
                if (((Hub) detailValue).getSize() > 200) b = false;
            }
        }

        int cntMasterPropsLoaded=0;
        if (masterProps != null && masterObject instanceof OAObject) {
            boolean bx = true;
            for (String s : masterProps) {
                bx = bx && (System.currentTimeMillis() < (msStart + 50));
                if (bx) {
                    ((OAObject) masterObject).getProperty(s);
                    cntMasterPropsLoaded++;
                }
                else {
                    loadDataInBackground((OAObject)masterObject, s);
                }
            }
        }
        
        int cntSib=0;
        if (b && cntMasterPropsLoaded == 0) {
            returnValue = detailValue;
        }
        else {
            OAObjectSerializer os = getSerializedDetail(msStart, (OAObject)masterObject, detailValue, property, masterProps, cntMasterPropsLoaded, siblingKeys, bForHubMerger);
            
            os.setClientId(clientId);
            os.setId(id);
            
            os.setMax(1200);  // max number of objects to write
            os.setMaxSize(400000);  // max size of compressed data to write out
    
            Object objx = os.getExtraObject();
            if (objx instanceof HashMap) {
                cntSib = ((HashMap) objx).size();
                if (cntSib > 0 && (masterProps != null && masterProps.length > 0)) cntSib--;
            }
            returnValue = os;
        }

        long diff = System.currentTimeMillis() - msStart;
        String s = (diff > 1000) ? " ALERT" : "";
        
        s = String.format(
            "client=%d, id=%,d, Obj=%s, prop=%s, siblings=%,d/%,d, masterProps=%s, ms=%,d%s",
            clientId, id, 
            masterObject.getClass().getSimpleName(), 
            property, 
            cntSib,        
            (siblingKeys == null)?0:siblingKeys.length,
            masterProps==null?"":(""+cntMasterPropsLoaded+"/"+masterProps.length),
            diff,
            s
        );
        OAPerformance.LOG.fine(s);
        LOG.fine(s);

        return returnValue;
    }
    
    /** 20130213
     *  getDetail() requirements
     * load referencs for master object and detail object/hub, and one level of ownedReferences
     * serialize all first level references for master, and detail 
     * send existing references for 1 more level from master, and 2 levels from detail
     * dont send any references that equal master or have master in the hub
     * dont send any references that have detail/hub in it
     * dont send detail if it has already been sent with all references
     * dont send a reference if it has already been sent to client, and has been added to tree
     * send max X objects 
     * 
     */
    protected OAObjectSerializer getSerializedDetail(final long msStart, final OAObject masterObject, final Object detailObject, final String propFromMaster, final String[] masterProperties, final int cntMasterPropsLoaded, final OAObjectKey[] siblingKeys, final boolean bForHubMerger) {
        // at this point, we know that the client does not have all of the master's references,
        // and we know that value != null, since getDetail would not have been called.
        // include the references "around" this object and master object, along with any siblings
      
        // see OASyncClient.getDetail(..)

        boolean b = wasFullySentToClient(masterObject);
        final boolean bMasterWasPreviouslySent = b && (masterProperties == null || masterProperties.length == 0);


        int tot = 0;
        Hub dHub = null;
        if (detailObject instanceof Hub) {
            dHub = (Hub) detailObject;
            tot = dHub.size(); 
            if (dHub.isOAObject()) {
                for (Object obj : dHub) {
                    if (System.currentTimeMillis() > (msStart + 40)) {
                        break;
                    }
                    if (wasFullySentToClient(obj)) continue;
                    if (OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false)) continue;
                    OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 1, 0, false, 2, msStart+40);
                }
            }
        }
        else if ((detailObject instanceof OAObject) && !wasFullySentToClient(detailObject)) {
            OAObjectReflectDelegate.loadAllReferences((OAObject) detailObject, 1, 0, false, 5, msStart+40);
        }

        HashMap<OAObjectKey, Object> hmExtraData = null;
        if (tot < 5000 && siblingKeys != null && siblingKeys.length > 0) {
            hmExtraData = new HashMap<OAObjectKey, Object>();
            // send back a lightweight hashmap (oaObjKey, value)
            Class clazz = masterObject.getClass();
            boolean bLoad = true;
            for (OAObjectKey key : siblingKeys) {
                OAObject obj = OAObjectCacheDelegate.get(clazz, key);
                if (obj == null) {
                    continue;
                }

                Object value = OAObjectPropertyDelegate.getProperty(obj, propFromMaster, true, true);
                if (value instanceof OANotExist || value instanceof OAObjectKey) {  // not loaded from ds
                    if (bLoad) {
                        bLoad = ((System.currentTimeMillis() - msStart) < (bForHubMerger?225:85));
                    }
                    if (!bLoad) {
                        loadDataInBackground(obj, propFromMaster);
                        continue;
                    }
                }
                
                if (bLoad) {
                    value = OAObjectReflectDelegate.getProperty(obj, propFromMaster); // load from DS
                }
                else if (value instanceof OAObjectKey) {
                    continue;
                }

                if (value instanceof Hub) {
                    int x = ((Hub) value).getSize();
                    if (tot != 0) {
                        if (tot+x > (bForHubMerger?5000:1250)) {
                            continue;
                        }
                    }
                    tot += x;
                }
                hmExtraData.put(key, value);
                if (tot > 5000) {
                    break;
                }
            }
        }
        
        b = ((hmExtraData != null && hmExtraData.size() > 5) || (cntMasterPropsLoaded > 5));
        if (!b) {
            if (detailObject instanceof Hub) {
                if (((Hub) detailObject).getSize() > 200) b = true;
            }
        }
        
        OAObjectSerializer os = new OAObjectSerializer(detailObject, b);
        if (hmExtraData != null && hmExtraData.size() > 0) {
            if ((masterProperties != null && masterProperties.length > 0)) {
                hmExtraData.put(masterObject.getObjectKey(), masterObject); // so extra props for master can go 
            }
            os.setExtraObject(hmExtraData); 
        }
        else {
            if ((masterProperties != null && masterProperties.length > 0)) {
                os.setExtraObject(masterObject);  // so master can be sent to client, and include any other masterProps
            }
        }

        OAObjectSerializerCallback cb = createOAObjectSerializerCallback(os, masterObject, bMasterWasPreviouslySent, 
                detailObject, dHub, propFromMaster, masterProperties, siblingKeys, hmExtraData);
        os.setCallback(cb);
        return os;
    }
    
    /**
     * called when a property or sibling data cant be loaded for current request, because of timeout.
     * This can be overwritten to have it done in a background thread.
     */
    protected void loadDataInBackground(OAObject obj, String property) {
       
    }
    
    
    // callback to customize the return values from getDetail(..) 
    private OAObjectSerializerCallback createOAObjectSerializerCallback(
            final OAObjectSerializer os, 
            final OAObject masterObject, final boolean bMasterWasPreviouslySent, 
            final Object detailObject, final Hub detailHub,
            final String propFromMaster, 
            final String[] masterProperties, final OAObjectKey[] siblingKeys,
            final HashMap<OAObjectKey, Object> hmExtraData) 
    {
        
        // this callback is used by OAObjectSerializer to customize what objects will be include in 
        //    the serialized object.
        OAObjectSerializerCallback callback = new OAObjectSerializerCallback() {
            boolean bMasterSent;
            // keep track of which objects are being sent to client in this serialization
            HashSet<Integer> hsSendingGuid = new HashSet<Integer>();
            
            @Override
            protected void afterSerialize(OAObject obj) {
                int guid = OAObjectKeyDelegate.getKey(obj).getGuid();
                boolean bx = hsSendingGuid.remove(guid);
                // update tree of sent objects
                try {
                    rwLockTreeSerialized.writeLock().lock();
                    if (bx || treeSerialized.get(guid) == null) {
                        treeSerialized.put(guid, bx);
                    }
                }
                finally {
                    rwLockTreeSerialized.writeLock().unlock();
                }
            }
            
            // this will "tell" OAObjectSerializer which reference properties to include with each OAobj
            @Override
            protected void beforeSerialize(final OAObject obj) {
                // parent object - will send all references
                if (obj == masterObject) {
                    if (bMasterSent) {
                        excludeAllProperties();
                        return;
                    }
                    bMasterSent = true;
                    if (bMasterWasPreviouslySent) {
                        excludeAllProperties();
                        return;
                    }

                    if (masterProperties == null || masterProperties.length == 0) {
                        if (!os.hasReachedMax()) {
                            hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());  // flag that all masterObject props have been sent to client
                        }
                        includeAllProperties();
                    }
                    else {
                        includeProperties(masterProperties);
                    }
                    return;
                }

                if (obj == detailObject) {
                    if (this.getLevelsDeep() > 0) {
                        excludeAllProperties(); // already sent in this batch
                    }
                    else if (bMasterWasPreviouslySent) {
                        // already had all of master, this is only for a calculated prop
                        excludeAllProperties();
                    }
                    else if (wasFullySentToClient(obj)) {
                        excludeAllProperties(); // already sent
                    }
                    else {
                        boolean b = OAObjectReflectDelegate.areAllReferencesLoaded(obj, false);
                        if (b) {
                            if (!os.hasReachedMax()) {
                                hsSendingGuid.add(obj.getObjectKey().getGuid());
                            }
                        }
                        includeAllProperties();
                    }
                    return;
                }

                if (detailHub != null && detailHub.contains(obj)) {
                    // include all props of first 25
                    boolean b = false;
                    for (int i=0; i<25; i++) {
                        Object objx = detailHub.getAt(i);
                        if (objx == null || objx == obj) {
                            b = true;
                            break;
                        }
                    }
                    if (!b) {
                        excludeAllProperties();
                    }
                    else {
                        // this Object is a Hub - will send all references (all that are been loaded)
                        if (wasFullySentToClient(obj)) {
                            if (!os.hasReachedMax()) {
                                hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
                            }
                            excludeAllProperties();  // client has it all
                        }
                        else {
                            b = OAObjectReflectDelegate.areAllReferencesLoaded(obj, false);
                            if (b) {
                                if (!os.hasReachedMax()) {
                                    hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
                                }
                            }
                            includeAllProperties();
                        }
                    }
                    return;
                }

                // for siblings, only send the reference property for now
                if (hmExtraData != null) {
                    if (obj.getClass().equals(masterObject.getClass())) {
                        if (hmExtraData.get(obj.getObjectKey()) != null) {
                            // sibling object either is not on the client or does not have all references
                            includeProperties(new String[] {propFromMaster});
                            return;
                        }
                    }
                }
                
                // second level object - will send all references that are already loaded
                Object objPrevious = this.getPreviousObject();
                boolean b = (objPrevious != null && objPrevious == detailObject);
                b = b || (objPrevious == masterObject);
                b = b || (detailHub != null && (objPrevious != null && detailHub.contains(objPrevious)));
                
                if (b && !bMasterWasPreviouslySent) {
                    if (isOnClient(obj)) {
                        excludeAllProperties();  // client already has it, might not be all of it
                    }
                    else {
                        // client does not have it, send whatever is loaded
                        b = OAObjectReflectDelegate.areAllReferencesLoaded(obj, false);
                        if (b) {
                            if (!os.hasReachedMax()) {
                                hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
                            }
                        }
                        includeAllProperties(); // will send whatever is loaded
                    }
                    return;
                }
                
                // "leaf" reference that client does not have, only include owned references
                excludeAllProperties();
            }

            /**
             * This allows returning an objKey if the object is already on the client.
             */
            @Override
            public Object getReferenceValueToSend(final Object object) {
                // dont send sibling objects back, use objKey instead
                // called by: OAObjectSerializerDelegate for ref props 
                // called by: HubDataMaster write, so key can be sent instead of masterObject 
                if (!(object instanceof OAObject)) return object;
              
                
                OAObjectKey k = null;
                if (object == masterObject || object == detailObject) {  
                    k = ((OAObject)object).getObjectKey();
                    return k;
                }
                
                if (siblingKeys != null) {
                    k = ((OAObject)object).getObjectKey();
                    for (OAObjectKey k2 : siblingKeys) {
                        if (k.getGuid() == k2.getGuid()) {
                            return k;
                        }
                    }
                }
                
                if (isOnClient(object)) {
                    if (k == null) k = ((OAObject)object).getObjectKey();
                    return k;
                }
                
                return object;
            }
            
            /* this is called when a reference has already been included, by the setup() method.
             * this will see if the object already exists on the client to determine if it will
             * be sent.  Otherwise, oaobject.writeObject will only send the oaKey, so that it will
             * be looked up on the client. 
             */
            @Override
            public boolean shouldSerializeReference(final OAObject oaObj, final String propertyName, final Object referenceValue, final boolean bDefault) {
                if (!bDefault) return false;
                if (referenceValue == null) return false;
                
                if (oaObj == masterObject) {
                    return true;
                }
                
                if (oaObj == detailObject) {
                    return !wasFullySentToClient(referenceValue);
                }
                
                OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
                if (hmExtraData != null) {
                    if (oaObj.getClass().equals(masterObject.getClass())) {
                        if (hmExtraData.get(key) != null) {
                            return true;
                        }
                    }
                }
               
                if (referenceValue instanceof Hub) {
                    Hub hubValue = (Hub) referenceValue;
                    if (hubValue.getSize() == 0) return false;
                    
                    // dont include hubs with masterObject in it, so that it wont be sending sibling data for masterObj
                    if (hubValue.contains(masterObject)) {
                        return false;  
                    }

                    // dont send other sibling data
                    if (detailObject != null && detailHub == null && hubValue.contains(detailObject)) {
                        return false;  
                    }

                    
                    // this will do a quick test to see if this is a Hub with any of the same objects in it.
                    if (detailHub != null) {
                        if (!detailHub.getObjectClass().equals(hubValue.getObjectClass())) {
                            return true;
                        }
                        Hub h1, h2;
                        if (detailHub.getSize() > hubValue.getSize()) {
                            h1 = hubValue;
                            h2 = detailHub;
                        }
                        else {
                            h1 = detailHub;
                            h2 = hubValue;
                        }
                        for (int i=0; i<3; i++) {
                            Object objx = h1.getAt(i);
                            if (objx == null) break;
                            if (h2.contains(objx)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }

                if (!(referenceValue instanceof OAObject)) return true;
                

                int level = this.getLevelsDeep();
                
                if (referenceValue == masterObject) {
                    if (bMasterSent) return false;
                    if (level > 1) return false; // wait for it to be saved at correct position
                    return true;
                }

                if (referenceValue == detailObject) return false;  // only save as begin obj
                if (detailHub != null && detailHub.contains(referenceValue)) {
                    return false; // only save as begin obj
                }

                if (level == 0) {
                    return false; // extra data does not send it's references
                }
                
                int guid = key.getGuid();
                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();
                boolean b = objx != null && ((Boolean) objx).booleanValue();
                if (b) {
                    return false; // already sent with all refs
                }

                // second level object - will send all references that are already loaded
                if (level < 3) {
                    return true;
                }
                return (objx == null);
            }
        };
        return callback;
    }


    private boolean isOnClient(Object obj) {
        if (!(obj instanceof OAObject)) return false;
        rwLockTreeSerialized.readLock().lock();
        Object objx;
        try {
            objx = treeSerialized.get( ((OAObject)obj).getObjectKey().getGuid());
        }
        finally {
            rwLockTreeSerialized.readLock().unlock();
        }
        return (objx != null);
    }
    
    private boolean wasFullySentToClient(Object obj) {
        if (!(obj instanceof OAObject)) return false;
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get( ((OAObject)obj).getObjectKey().getGuid());
        rwLockTreeSerialized.readLock().unlock();
        if (objx instanceof Boolean) {
            return ((Boolean) objx).booleanValue();
        }
        return false;
    }
}

