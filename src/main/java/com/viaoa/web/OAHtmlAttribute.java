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
import com.viaoa.util.*;

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
            val = getValue(obj, val);
        }
        
        String s;
        
        String aName = getAttrName();
        if (OAString.isEmpty(aName)) return null;
        
        val = OAJspUtil.createJsString(val, '\'');
        
        if (!OAString.isEmpty(val)) s = "$('#"+id+"').attr('"+aName+"', '"+val+"');";
        else s = "$('#"+id+"').removeAttr('"+aName+"');";
        return s;
    }
   
}
