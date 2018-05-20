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
package com.viaoa.web;


import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;



// <input type="radio" name="group1" value="Bread" checked>
// <input type="radio" name="group1" value="Cheese" checked>

/**
 * Controls an html input type=radio
 * 
 * bind to hub, property
 * show/hide, that can be bound to property
 * enabled, that can be bound to property
 * ajax submit on change
 * 
 * @author vvia
 */
public class OARadio extends OACheckBox {

    /**
     * @param groupName "name" attribute used to group radios together
     */
    public OARadio(String id, String groupName, Hub hub, String propertyPath, Object onValue) {
        super(id, hub, propertyPath);
        setGroupName(groupName);
        this.setOnValue(onValue);
    }
    public OARadio(String id) {
        super(id);
    }

    @Override
    public void setOffValue(Object obj) {
        // no-op.  No such thing for a radio
    }
    
    // this will only update the submitted radio
    @Override
    protected void updateProperty(OAObject obj, boolean bSelected) {
        if (bSelected) {
            super.updateProperty(obj, bSelected);
        }
    }

    
}
