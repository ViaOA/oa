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
package com.viaoa.jsp;


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
