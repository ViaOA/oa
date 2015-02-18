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
