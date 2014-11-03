package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;

@OARemoteInterface()
public interface RemoteClientInterface {

    OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property);
    
    Object getDetail(Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys);
    

    Object datasource(int command, Object[] objects);
    
    @OARemoteMethod(noReturnValue=true)
    /**
     * Objects that have been GDd on the client, so that the server can remove from
     * the session - so that it can be resent if needed.
     */
    void removeGuids(int[] guids);
    
}
