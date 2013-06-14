package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteClientInterface {

    OAObject createNewObject(Class clazz);
    OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties);
    
    boolean setCached(Class objectClass, OAObjectKey objectKey, boolean bAddToCache);
    
    boolean setLock(Class objectClass, OAObjectKey objectKey, boolean bLock);
    boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey);
    boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey);

    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys);
    
}
