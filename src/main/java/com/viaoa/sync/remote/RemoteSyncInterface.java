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


import java.util.Comparator;

import com.viaoa.object.OAObjectKey;

public interface RemoteSyncInterface {

    // OAObjectCSDelegate    
    boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob);    
    boolean removeObject(Class objectClass, OAObjectKey objectKey); // Remove object from each workstation.

    // HubCSDelegate
    boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj);
    
    boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos);
    
    boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX);   
    boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName);   
    
    boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo);
    boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp);
}


