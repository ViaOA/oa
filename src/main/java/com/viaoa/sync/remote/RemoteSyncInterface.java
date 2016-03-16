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


import java.util.Comparator;

import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface(isOASync=true)
public interface RemoteSyncInterface {

    // OAObjectCSDelegate    
    boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob);    

    // HubCSDelegate
    boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj);
    boolean addNewToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, OAObjectSerializer obj);
    
    
    boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos);
    
    boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX);   
    boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName);   
    
    boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo);
    boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp);

    void clearHubChanges(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName);
    
    // moved to remoteClient so that it will be performed on the server
    // boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName);
}


