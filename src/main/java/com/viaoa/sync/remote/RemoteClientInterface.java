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
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;

/**
 * Client remote methods that will use the same named msg queue as RemoteSync, when set up (bind) on the server.
 * This is so that changes can be ordered and instances (clients/server0 will stay in sync.
 * 
 * @author vvia
 */
@OARemoteInterface()
public interface RemoteClientInterface {

    OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties);
    
    Object getDetail(int id, Class masterClass, OAObjectKey masterObjectKey, String property, boolean bForHubMerger);
    
    Object getDetail(int id, Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys, boolean bForHubMerger);

    // dont put in queue, but have it returned on vsocket for queued messages  
    //     All of the other methods are put in queue to be processed and have the return value set.
    @OARemoteMethod(returnOnQueueSocket=true)
    Object getDetailNow(int id, Class masterClass, OAObjectKey masterObjectKey, 
            String property, String[] masterProps, OAObjectKey[] siblingKeys, boolean bForHubMerger);

    Object datasource(int command, Object[] objects);
    
    @OARemoteMethod(noReturnValue=true)
    void datasourceNoReturn(int command, Object[] objects);

    boolean delete(Class objectClass, OAObjectKey objectKey);
    
    boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName);
}
