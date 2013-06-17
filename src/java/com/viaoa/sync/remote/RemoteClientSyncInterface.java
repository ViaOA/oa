package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteClientSyncInterface {

    OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys);
    

    Object datasource(int command, Object[] objects);
}
