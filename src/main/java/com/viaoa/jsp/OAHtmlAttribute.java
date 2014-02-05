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
import com.viaoa.util.OAString;

/**
 * Used to set the Attribute value of an Html Element. 
 * 
 * @see OAHtmlElement#addAttribute(OAHtmlAttribute)
 */
public class OAHtmlAttribute {
    private static final long serialVersionUID = 1L;

    private String attrName;
    private String propertyPath;
    private Hub<?> hub;
    
    /**
     * Create an Html attribute controller, that will set the attr value to the value of the Hub.activeObject.  If the value 
     * is empty, then the attribute will be removed.
     * @param attrName name of html attr that is to be changed.  
     * @param hub the hub to use to get the value of activeObject.
     * @param propertyPath used to get the value of the hub.getAO (active object) 
     */
    public OAHtmlAttribute(String attrName, Hub<?> hub, String propertyPath) {
        this.attrName = attrName;
        this.hub = hub;
        this.propertyPath = propertyPath;
    }

    /**
     * Create an Html attribute controller, that will set the value of the attribute to "value"
     * @param attrName
     * @param value
     */
    public OAHtmlAttribute(String attrName, String value) {
        this.attrName = attrName;
        this.propertyPath = value;
    }
    
    public void setAttrName(String name) {
        this.attrName = name;
    }
    public String getAttrName() {
        return this.attrName;
    }

    public String getValue() {
        return propertyPath;
    }
    public void setValue(String value) {
        this.propertyPath = value;
    }
    /**
     * This is called to get the value to set for the Html attribute.
     */
    public String getValue(Object currentObject, String currentValue) {
        return currentValue;
    }
    
    public String getScript(String id) {
        String val = getValue();
        
        if (hub == null) {
            val = getValue(null, propertyPath);
        }
        else {
            Object obj = hub.getAO();
            if (obj instanceof OAObject) {
                val = ((OAObject) obj).getPropertyAsString(propertyPath);
            }
            else {
                if (obj == null) val = "";
                else val = "" + obj;
            }
        }
        
        String s;
        
        String aName = getAttrName();
        if (OAString.isEmpty(aName)) return null;
        
        if (OAString.isEmpty(val)) s = "$('#"+id+"').attr('"+aName+"', '"+val+"');";
        else s = "$('#"+id+"').removeAttr('"+aName+"');";
        return s;
    }
   
}
