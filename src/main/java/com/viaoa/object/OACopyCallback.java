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
package com.viaoa.object;

import com.viaoa.hub.Hub;


/**
 * Used by  OAObjectReflectDelegate.createCopy, copyInto(..) to control how an object is copied.
 * 
 */
public class OACopyCallback {
    
    /**
     * Called when checking to copy owned objects.
     */
    protected boolean shouldCopyOwnedHub(OAObject oaObj, String path, boolean bDefault) {
        return bDefault;
    }
    
    /**
     * Called when adding owned objects to new hub.
     * default is to return currentValue, which will then create a new copy of it.
     */
    protected OAObject createCopy(OAObject oaObj, String path, Hub hub, OAObject currentValue) {
        return currentValue;
    }


    /**
     * Called when copying a property or LinkType=One
     * Default is to return currentValue.
     */
    protected Object getPropertyValue(OAObject oaObj, String path, Object currentValue) {
        return currentValue;
    }
    
}
