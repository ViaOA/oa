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

import com.viaoa.hub.HubEvent;

/**
 * Code that is called to notify when a change is made to a dependent property. 
*/
public interface OATriggerListener {
    
    /**
     * Called when a change is made to a dependent property. 
     * @param obj root object that is affected by the event
     * @param hubEvent info about the event
     * @param propertyPath path from root class to the object that has the event
     */
    public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception;
}


