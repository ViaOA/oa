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
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;
import com.viaoa.sync.model.ClientInfo;


/**
 * session for client. 
 */
@OARemoteInterface()
public interface RemoteSessionInterface {
    
    /**
     * create a new object on the server,
     * and call addToCache
     */
    OAObject createNewObject(Class clazz);

    /**
     * Used to make sure that object is stored in the server side 
     * @param obj
     * @param bAddToCache
     */
    @OARemoteMethod(noReturnValue=true)
    void addToCache(OAObject obj);

    @OARemoteMethod(noReturnValue=true)
    void removeFromCache(int guid);

    
    boolean setLock(Class objectClass, OAObjectKey objectKey, boolean bLock);
    boolean isLocked(Class objectClass, OAObjectKey objectKey);
    boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey);
    boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey);

    @OARemoteMethod(noReturnValue=true)
    void update(ClientInfo ci); 
    
    @OARemoteMethod(noReturnValue=true)
    void sendException(String msg, Throwable ex);

    @OARemoteMethod(noReturnValue=true)
    /**
     * Objects that have been GDd on the client, so that the server can remove from
     * the session - so that it can be resent if needed.
     * note: if the guid < 0, then the object is also in the serverSide cache (and the guid needs to be absolute value)
     */
    void removeGuids(int[] guids);
}
