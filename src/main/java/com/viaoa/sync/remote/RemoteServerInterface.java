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
