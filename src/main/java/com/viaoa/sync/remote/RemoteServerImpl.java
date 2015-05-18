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

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.model.ClientInfo;

/**
 * Server side remote object for clients to use.
 */
public abstract class RemoteServerImpl implements RemoteServerInterface {

    @Override
    public String ping(String msg) {
        return msg;
    }
    @Override
    public void ping2(String msg) {
    }
    @Override
    public String getDisplayMessage() {
        return "OASyncServer";
    }

    @Override
    public boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.save(iCascadeRule);
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }

    @Override
    public int getNextFiftyObjectGuids() {
        return OAObjectDelegate.getNextFiftyGuids();
    }
    
    @Override
    public OAObject getObject(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        if (obj == null) {
            if (OASyncDelegate.isServer()) {
                obj = (OAObject) OADataSource.getObject(objectClass, objectKey);
            }
        }
        return obj;
    }

    @Override
    public abstract RemoteClientInterface getRemoteClient(ClientInfo clientInfo);    
    
    @Override
    public abstract RemoteSessionInterface getRemoteSession(ClientInfo clientInfo, RemoteClientCallbackInterface callback);
    
}
