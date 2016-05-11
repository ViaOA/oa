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


import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.*;
import com.viaoa.sync.model.ClientInfo;

@OARemoteInterface
public interface RemoteServerInterface {

    boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule);
    OAObject getObject(Class objectClass, OAObjectKey objectKey);

    RemoteSessionInterface getRemoteSession(
        ClientInfo clientInfo, 
        @OARemoteParameter(dontUseQueue=true) RemoteClientCallbackInterface callback
    );
    
    
    RemoteClientInterface getRemoteClient(ClientInfo clientInfo);
    
    @OARemoteMethod(dontUseQueue=true)
    String ping(String msg);
    
    @OARemoteMethod(noReturnValue=true, dontUseQueue=true)
    void ping2(String msg);
    
    @OARemoteMethod(dontUseQueue=true)
    String getDisplayMessage();
    
    @OARemoteMethod(dontUseQueue=true)
    int getNextFiftyObjectGuids();
    
    @OARemoteMethod(noReturnValue=true, dontUseQueue=true)
    void refresh(Class clazz);

    /**
     * Used by OAObject.remote to run a remote command from an OAObject.
     */
    @OARemoteMethod
    Object runRemoteMethod(Class clazz, OAObjectKey objKey, String methodName, Object[] args);
    
}
