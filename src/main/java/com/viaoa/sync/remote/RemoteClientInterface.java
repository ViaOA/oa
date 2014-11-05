package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

/**
 * Client remote methods that will use the same named msg queue as RemoteSync, when set up (bind) on the server.
 * This is so that changeds can be ordered and instances (clients/server0 will stay in sync.
 * 
 * @author vvia
 */
@OARemoteInterface()
public interface RemoteClientInterface {

    OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys);
    
    Object datasource(int command, Object[] objects);
}
