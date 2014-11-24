/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.sync.remote;

import java.util.ArrayList;
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
 * getDetail remote requests.  This class will return the object/s for the
 * request, and "knows" what extra objects to include.
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
    
    private int cntx;
    private int errorCnt;
    public Object getDetail(Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys) {

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
        
        if (masterProps == null && (siblingKeys == null || siblingKeys.length==0)) return detailValue;
        
        OAObjectSerializer os = getSerializedDetail((OAObject)masterObject, detailValue, property, masterProps, siblingKeys);
        os.setMax(5500);

//TEST qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvvvvvvvvvwwwwwwwwwwbbbbbbbbbb
/*        
        String s = String.format(
                "%,d) ClientGetDetail.getDetail() Obj=%s, prop=%s, ref=%s, getSib=%,d, masterProps=%s",
                ++cntx, 
                masterObject, 
                property, 
                detailValue,
                (siblingKeys == null)?0:siblingKeys.length,
                masterProps==null?"":(""+masterProps.length)
            );
         System.out.println(s);
*/        
        
        return os;
    }
    
    public boolean isOnClient(Object obj) {
        if (!(obj instanceof OAObject)) return false;
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get( ((OAObject)obj).getObjectKey().getGuid());
        rwLockTreeSerialized.readLock().unlock();
        return objx != null;
    }
    
    
    protected boolean wasFullySentToClient(Object obj) {
        if (!(obj instanceof OAObject)) return false;
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get( ((OAObject)obj).getObjectKey().getGuid());
        rwLockTreeSerialized.readLock().unlock();
        if (objx instanceof Boolean) {
            return ((Boolean) objx).booleanValue();
        }
        return false;
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
      
        final long t1 = System.currentTimeMillis();

        int guid = OAObjectKeyDelegate.getKey(masterObject).getGuid();
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get(guid);
        rwLockTreeSerialized.readLock().unlock();
        boolean b = objx != null && ((Boolean) objx).booleanValue();
        final boolean bMasterWasPreviouslySent = b && (masterProperties == null || masterProperties.length == 0);

        if (!bMasterWasPreviouslySent && masterObject instanceof OAObject) {
            OAObjectReflectDelegate.loadReferences((OAObject) masterObject, false, 10);
        }
        
        Hub dHub = null;
        if (detailObject instanceof OAObject) {
            guid = OAObjectKeyDelegate.getKey((OAObject) detailObject).getGuid();
            rwLockTreeSerialized.readLock().lock();
            objx = treeSerialized.get(guid);
            rwLockTreeSerialized.readLock().unlock();
            b = objx != null && ((Boolean) objx).booleanValue();
            if (!b) {
                OAObjectReflectDelegate.loadAllReferences((OAObject) detailObject, 1, 1, false);
            }
        }
        else if (detailObject instanceof Hub) {
            dHub = (Hub) detailObject;
            if (dHub.isOAObject()) {
                int cnt = 0;
                for (Object obj : dHub) {
                    guid = OAObjectKeyDelegate.getKey((OAObject) obj).getGuid();
                    rwLockTreeSerialized.readLock().lock();
                    objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    b = objx != null && ((Boolean) objx).booleanValue();
                    if (b) continue;
                    
                    if (System.currentTimeMillis() - t1 > 100) break;
                    b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                    if (!b) {
                        if (++cnt < 15) {
                            OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 1, 1, false);
                        }
                        else {
                            OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 0, 1, false);
                            if (cnt > 50) break;
                        }
                    }
                }
            }
        }
        final Hub detailHub = dHub;

        ArrayList<OAObject> al = null;
        if (siblingKeys != null) {
            al = new ArrayList<OAObject>(siblingKeys.length+1);
            Class c = masterObject.getClass();
            int cntDs = 0;
            for (OAObjectKey key : siblingKeys) {
                if (System.currentTimeMillis() - t1 > 120) break;
                OAObject obj = OAObjectCacheDelegate.get(c, key);
                if (obj == null) continue;
                
                Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, propFromMaster, true, true);
                if (value instanceof OANotExist) {  // not loaded from ds
                    if (cntDs++ < 10) {
                        OAObjectReflectDelegate.getProperty(obj, propFromMaster);
                        al.add(obj);
                    }
                }
                else if (value != null) {
                    al.add(obj);
                }
            }
        }
        final ArrayList<OAObject> alExtraData = al;
        
        OAObjectSerializer os = new OAObjectSerializer(detailObject, true);
        if (alExtraData != null) {
            if (detailObject == null) alExtraData.add(masterObject); // so master can go 
            os.setExtraObject(alExtraData); 
        }
        else {
            if (detailObject == null) os.setExtraObject(masterObject);  // so master can be sent to client
            else if (!(detailObject instanceof Hub)) os.setExtraObject(masterObject); 
        }

        
        OAObjectSerializerCallback callback = new OAObjectSerializerCallback() {                    
            boolean bMasterSent;
            @Override
            protected void setup(OAObject obj) {
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
                    
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, true);
                    rwLockTreeSerialized.writeLock().unlock();
                    if (masterProperties == null || masterProperties.length == 0) {
                        includeAllProperties();
                    }
                    else includeProperties(masterProperties);                    
                    return;
                }

                if (obj == detailObject) {
                    int level = this.getLevelsDeep();  // obj is pushed to stack, and level is changed after setup() is called
                    if (level > 0) {
                        excludeAllProperties(); // already sent in this batch
                        return;
                    }

                    if (bMasterWasPreviouslySent) {
                        // already had all of master, this is only for a calculated prop
                        excludeAllProperties();
                        return;
                    }
                    
                    // this Object - will send all references (all have been loaded)
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    boolean b = objx != null && ((Boolean) objx).booleanValue();
                    if (b) {
                        excludeAllProperties(); // already sent
                    }
                    else {
                        rwLockTreeSerialized.writeLock().lock();
                        b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                        treeSerialized.put(guid, b);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties();
                    }
                    return;
                }

                if (detailHub != null && detailHub.contains(obj)) {
                    // this Object is a Hub - will send all references (all have been loaded)
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    boolean b = objx != null && ((Boolean) objx).booleanValue();

                    if (b) {
                        excludeAllProperties();  // client has it all
                    }
                    else {
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, true);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties();
                    }
                    return;
                }

                // for siblings, only send the reference property for now
                if (alExtraData != null && alExtraData.contains(obj)) {
                    // sibling object either is not on the client or does not have all references
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, false); 
                    rwLockTreeSerialized.writeLock().unlock();
                    includeProperties(new String[] {propFromMaster});
                    return;
                }
                
                
                // second level object - will send all references that are already loaded
                Object objPrevious = this.getPreviousObject();
                boolean b = (objPrevious == detailObject);
                if (!b && objPrevious == masterObject) b = true; 
                if (!b) b = (detailHub != null && (objPrevious != null && detailHub.contains(objPrevious)));
                
                if (b && !bMasterWasPreviouslySent) {
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();

                    if (objx != null) {
                        excludeAllProperties();  // client already has it, might not be all of it
                    }
                    else {
                        // client does not have it, send whatever is loaded
                        b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, b);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties(); // will send whatever is loaded
                    }
                    return;
                }
                
                // "leaf" reference that client does not have, only include owned references
                int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();

                if (objx == null) {  // never sent to client
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, false); // flag it as: object has been sent, but not references
                    rwLockTreeSerialized.writeLock().unlock();
                }
                excludeAllProperties();
            }

            @Override
            public Object getReferenceValueToSend(Object object) {
                if (!(object instanceof OAObject)) {
                    return object;
                }
                OAObject obj = (OAObject) object;
                OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
                
                int guid = key.getGuid();
                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();
                
                if (objx != null) {
                    return key;
                }
                return obj;
            }
            
            /* this is called when a reference has already been included, by the setup() method.
             * this will see if the object already exists on the client to determine if it will
             * be sent.  Otherwise, oaobject.writeObject will only send the oaKey, so that it will
             * be matched up on the client. 
             */
            @Override
            public boolean shouldSerializeReference(OAObject oaObj, String propertyName, Object obj, boolean bDefault) {
                if (!bDefault) return false;
                if (obj == null) return false;
                
                if (oaObj == masterObject) return !wasFullySentToClient(obj);
                if (oaObj == detailObject) return !wasFullySentToClient(obj);
                if (alExtraData != null && alExtraData.contains(oaObj)) {
                    // sibling object only "ask" for propertyName
                    return true; // propFromMaster.equals(propertyName);
                }
                
                if (obj instanceof Hub) {
                    Hub hub = (Hub) obj;
                    if (hub.getSize() == 0) return false;
                    
                    // dont include hubs with masterObject in it, so that it wont be sending sibling data for masterObj
                    if (hub.contains(masterObject)) {
                        int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, false); // it might have been flagged as true
                        rwLockTreeSerialized.writeLock().unlock();
                        return false;  
                    }

                    // dont send other sibling data
                    if (detailObject != null && detailHub == null && hub.contains(detailObject)) {
                        int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, false); // it might have been flagged as true, need to unflag since this prop wont be sent
                        rwLockTreeSerialized.writeLock().unlock();
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
                                int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                                rwLockTreeSerialized.writeLock().lock();
                                treeSerialized.put(guid, false); // it might have been flagged as true
                                rwLockTreeSerialized.writeLock().unlock();
                                return false;
                            }
                        }
                    }
                    return true;
                }

                if (!(obj instanceof OAObject)) return true;
                
                if (obj == masterObject) {
                    if (bMasterSent) return false;
                    int level = this.getLevelsDeep();
                    if (level > 1) return false; // wait for it to be saved at correct position
                    return true;
                }

                if (obj == detailObject) return false;  // only save as begin obj
                if (detailHub != null && detailHub.contains(obj)) return false; // only save as begin obj

                
                int guid = OAObjectKeyDelegate.getKey((OAObject) obj).getGuid();
                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();
                boolean b = objx != null && ((Boolean) objx).booleanValue();
                if (b) {
                    return false; // already sent with all refs
                }
                
                int level = this.getLevelsDeep();
                if (level < 3) return true;
                return objx == null;
            }
        };
        os.setCallback(callback);
        return os;
    }
}

