package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteClientInterface {
    OAObject createNewObject(Class clazz);

    boolean setCached(Class objectClass, OAObjectKey objectKey, boolean bAddToCache);
    boolean setCached(OAObject obj, boolean bAddToCache);
    
    boolean setLock(Class objectClass, OAObjectKey objectKey, boolean bLock);
    boolean isLocked(Class objectClass, OAObjectKey objectKey);
    boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey);
    boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey);
}
