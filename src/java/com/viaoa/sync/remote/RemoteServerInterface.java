package com.viaoa.sync.remote;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;
import com.viaoa.sync.model.ClientInfo;

public interface RemoteServerInterface {

    boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule);
    boolean delete(Class objectClass, OAObjectKey objectKey);
    boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName);
    OAObject getObject(Class objectClass, OAObjectKey objectKey);

    RemoteClientInterface getRemoteClientInterface(ClientInfo clientInfo);
    RemoteClientSyncInterface getRemoteClientSyncInterface(ClientInfo clientInfo);
    
    String ping(String msg);
    String getDisplayMessage();
    
    int getNextFiftyObjectGuids();
}
