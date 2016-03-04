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
import com.viaoa.util.OANotExist;

/**
 * This is used for each clientSession, that creates a RemoteClientImpl for
 * getDetail(..) remote requests.  This class will return the object/s for the
 * request, and extra objects to include.
 * 
 * This works directly with OASyncClient.getDetail(..), returning a custom serializer for it.
 * @author vvia
 */
public class ClientGetDetail {
    private static Logger LOG = Logger.getLogger(ClientGetDetail.class.getName());

    // tracks guid for all oaObjects serialized, the Boolean: true=all references have been sent, false=object has been sent (might not have all references)
    private TreeMap<Integer, Boolean> treeSerialized = new TreeMap<Integer, Boolean>();
    private ReentrantReadWriteLock rwLockTreeSerialized = new ReentrantReadWriteLock();

    public void removeGuid(int guid) {
        rwLockTreeSerialized.writeLock().lock();
        treeSerialized.remove(guid);
        rwLockTreeSerialized.writeLock().unlock();
    }
    
    public void close() {
        treeSerialized.clear();
    }
    
    private int cntx;
    private int errorCnt;
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
    public Object getDetail(final Class masterClass, final OAObjectKey masterObjectKey, 
            final String property, final String[] masterProps, final OAObjectKey[] siblingKeys) {

        if (masterObjectKey == null || property == null) return null;

        Object masterObject = OAObjectReflectDelegate.getObject(masterClass, masterObjectKey);
        if (masterObject == null) {
            // get from datasource
            masterObject = (OAObject) OADataSource.getObject(masterClass, masterObjectKey);
            if (masterObject == null) {
                if (errorCnt++ < 100) LOG.warning("cant find masterObject in cache or DS.  masterClass=" + masterClass + ", key=" + masterObjectKey + ", property=" + property);
                return null;
            }
        }

        Object detailValue = OAObjectReflectDelegate.getProperty((OAObject) masterObject, property);
        
        if ((masterProps == null || masterProps.length == 0) && (siblingKeys == null || siblingKeys.length==0)) return detailValue;
        
        OAObjectSerializer os = getSerializedDetail((OAObject)masterObject, detailValue, property, masterProps, siblingKeys);
        os.setMax(2500);

// qqqqqqqqqqqqqq
        String s = String.format(
            "%,d) ClientGetDetail.getDetail() Obj=%s, prop=%s, returnValue=%s, getSib=%,d, masterProps=%s",
            ++cntx, 
            masterObject, 
            property, 
            detailValue,
            (siblingKeys == null)?0:siblingKeys.length,
            masterProps==null?"":(""+masterProps.length)
        );
//        System.out.println(s);
        LOG.fine(s);
        
        return os;
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
    protected OAObjectSerializer getSerializedDetail(final OAObject masterObject, final Object detailObject, final String propFromMaster, final String[] masterProperties, final OAObjectKey[] siblingKeys) {
        // at this point, we know that the client does not have all of the master's references,
        // and we know that value != null, since getDetail would not have been called.
        // include the references "around" this object and master object, along with any siblings
      
        // see OASyncClient.getDetail(..)
        final long t1 = System.currentTimeMillis();

        boolean b = wasFullySentToClient(masterObject);
        final boolean bMasterWasPreviouslySent = b && (masterProperties == null || masterProperties.length == 0);

        if (masterProperties != null && masterObject instanceof OAObject) {
            for (String s : masterProperties) {
                ((OAObject) masterObject).getProperty(s);
            }
        }
        
        Hub dHub = null;
        if (detailObject instanceof Hub) {
            dHub = (Hub) detailObject;
            if (dHub.isOAObject()) {
                for (Object obj : dHub) {
                    if (wasFullySentToClient(obj)) continue;
                    
                    long tDiff = System.currentTimeMillis() - t1;
                    if (OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false)) continue;
                    if (tDiff < 3L) {
                        OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 1, 1, false);
                    }
                    else {
                        OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 0, 1, false);
                        if (tDiff > 8L) break;
                    }
                }
            }
        }
        else if ((detailObject instanceof OAObject) && !wasFullySentToClient(detailObject)) {
            OAObjectReflectDelegate.loadAllReferences((OAObject) detailObject, 1, 1, false);
        }
        
        HashMap<OAObjectKey, Object> hmExtraData = null;
        
        if (siblingKeys != null && siblingKeys.length > 0) {
            hmExtraData = new HashMap<OAObjectKey, Object>();
            // send back a lightweight hashmap (oaObjKey, value)
            Class clazz = masterObject.getClass();
            for (OAObjectKey key : siblingKeys) {
                OAObject obj = OAObjectCacheDelegate.get(clazz, key);
                if (obj == null) continue;
                
                Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, propFromMaster, true, true);
                if (value instanceof OANotExist) {  // not loaded from ds
                    if (System.currentTimeMillis() - t1 > 100) break;
                }
                value = OAObjectReflectDelegate.getProperty(obj, propFromMaster); // load from DS
                // value will never be on the client, since it would not have included it in the siblings
                hmExtraData.put(key, value);
            }
        }
        
        b = ((hmExtraData != null && hmExtraData.size() > 0) || (masterProperties != null && masterProperties.length > 0));
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
    
        OAObjectSerializerCallback cb = createOAObjectSerializerCallback(masterObject, bMasterWasPreviouslySent, 
                detailObject, dHub, propFromMaster, masterProperties, siblingKeys, hmExtraData);
        os.setCallback(cb);

        return os;
    }
    
    
    // callback to customize the return values from getDetail(..) 
    private OAObjectSerializerCallback createOAObjectSerializerCallback(
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
                rwLockTreeSerialized.writeLock().lock();
                if (bx || treeSerialized.get(guid) == null) {
                    treeSerialized.put(guid, bx);
                }
                rwLockTreeSerialized.writeLock().unlock();
                
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
                    
                    hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());  // flag that all masterObject props have been sent to client
                    if (masterProperties == null || masterProperties.length == 0) includeAllProperties();
                    else includeProperties(masterProperties);                    
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
                        if (b) hsSendingGuid.add(obj.getObjectKey().getGuid());
                        includeAllProperties();
                    }
                    return;
                }

                if (detailHub != null && detailHub.contains(obj)) {
                    // this Object is a Hub - will send all references (all have been loaded)
                    if (wasFullySentToClient(obj)) {
                        hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
                        excludeAllProperties();  // client has it all
                    }
                    else {
                        boolean b = OAObjectReflectDelegate.areAllReferencesLoaded(obj, false);
                        if (b) hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
                        includeAllProperties();
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
                        if (b) hsSendingGuid.add(OAObjectKeyDelegate.getKey(obj).getGuid());
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
                
                if (isOnClient(object) || object == masterObject || object == detailObject) {  
                    // even have masterObject send key, so that hub.datam will use it to resolve on client and make it faster
                    return ((OAObject)object).getObjectKey();
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
                
                if (oaObj == masterObject) return true;
                if (oaObj == detailObject) return !wasFullySentToClient(referenceValue);
                
                OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
                if (hmExtraData != null) {
                    if (oaObj.getClass().equals(masterObject.getClass())) {
                        if (hmExtraData.get(key) != null) {
                            // sibling objects only "ask" for propertyName
                            return propFromMaster.equals(propertyName);
                        }
                    }
                }
                if (referenceValue instanceof Hub) {
                    Hub hub = (Hub) referenceValue;
                    if (hub.getSize() == 0) return false;
                    
                    // dont include hubs with masterObject in it, so that it wont be sending sibling data for masterObj
                    if (hub.contains(masterObject)) {
                        return false;  
                    }

                    // dont send other sibling data
                    if (detailObject != null && detailHub == null && hub.contains(detailObject)) {
                        return false;  
                    }

                    
                    // this will do a quick test to see if this is a Hub with any of the same objects in it.
                    if (detailHub != null) {
                        if (!detailHub.getObjectClass().equals(hub.getObjectClass())) {
                            return true;
                        }
                        Hub h1, h2;
                        if (detailHub.getSize() > hub.getSize()) {
                            h1 = hub;
                            h2 = detailHub;
                        }
                        else {
                            h1 = detailHub;
                            h2 = hub;
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
                if (detailHub != null && detailHub.contains(referenceValue)) return false; // only save as begin obj

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
                return objx == null;
            }
        };
        return callback;
    }


    private boolean isOnClient(Object obj) {
        if (!(obj instanceof OAObject)) return false;
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get( ((OAObject)obj).getObjectKey().getGuid());
        rwLockTreeSerialized.readLock().unlock();
        return objx != null;
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

