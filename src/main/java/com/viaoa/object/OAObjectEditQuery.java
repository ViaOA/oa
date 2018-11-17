/*  Copyright 1999-2018 Vince Via vvia@viaoa.com
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

import javax.swing.JLabel;

import com.viaoa.annotation.OAEditQuery;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAString;


/**
 * Used to allow interaction with OAObject and other (ex: UI) components.
 * 
 * @see OAObjectEditQueryDelegate
 * @author vvia
 */
public class OAObjectEditQuery {
    static final long serialVersionUID = 1L;

    private Type type = Type.Unknown;

    // name of property, method that is being queried
    private String name;
    
    private String confirmTitle;
    private String confirmMessage;
    private String toolTip;
    private String format;
    
    private boolean allowed;
    
    private Object value;  // depends on Type
    private JLabel label;

    private String response;
    private Throwable throwable;

    /**
     * Type of request being made from caller object.
     * 
     * All can use setResponse to include a return message/
     */
    public enum Type {   // properies to use based on type:
        Unknown(false),

        // set: confirmeTitle/Message to have UI interact with user
        AllowEnabled(true, false),    // use: allowEnabled  NOTE: this is also called for all types that have checkEnabledFirst=true
        
        AllowVisible(true),    // use: allowVisible
        
        AllowAdd(true, true),        // use: allowAdd
        AllowRemove(true, true),     // use: allowRemove
        AllowRemoveAll(true, true),  // use: allowRemoveAll
        AllowDelete(true, true),     // use: allowDelete
        AllowSave(false, false),     // dont check parent(s) or if enabled.  Need to be able to save a disabled object
        AllowCopy(false),
        
        VerifyPropertyChange(true, false),// use: value to get new value, name, response, throwable - set allowEnablede=false, or throwable!=null to cancel
        VerifyAdd(true, true),           // use: value to get added object, allowAdd, throwable - set allowAdd=false, or throwable!=null to cancel
        VerifyRemove(true, true),        // use: value to get removed object, allowRemove, throwable - set allowRemove=false, or throwable!=null to cancel
        VerifyRemoveAll(true, true),     // use: allowRemoveAll, response, throwable - set allowRemoveAll=false, or throwable!=null to cancel
        VerifyDelete(true, true),        // use: value to get deleted object, allowDelete, throwable - set allowDelete=false, or throwable!=null to cancel
        VerifySave(false, false),        // dont check parent(s) or if enabled.  Need to be able to save a disabled object
        VerifyCommand(true, true),
        
        GetCopy(false),     // can set allowed(..), or setVale(newObj), or nothing to have OAObject.createCopy(..) called.
        AfterCopy(false),   // value=newObject
        
        SetConfirmForPropertyChange(false),
        SetConfirmForAdd(false),
        SetConfirmForRemove(false),
        SetConfirmForRemoveAll(false),  //todo: qqqq
        SetConfirmForDelete(false),
        SetConfirmForSave(false),
        SetConfirmForCommand(false), //todo: qqqq
        
        GetToolTip(false),      // use: toolTip
        RenderLabel(false),     // use: label and update it's props
        UpdateLabel(false),      // update the label that belongs to a component
        GetFormat(false);       // use: format
        
        public boolean checkOwner;
        public boolean checkEnabledFirst;
        Type(boolean checkOwner) {
            this.checkOwner = checkOwner;
        }
        Type(boolean checkOwner, boolean checkEnabledFirst) {
            this.checkOwner = checkOwner;
            this.checkEnabledFirst = checkEnabledFirst;
        }
    }
    
    public OAObjectEditQuery(Type type) {
        this.type = type;
        this.allowed = true;
    }
    
    public void setType(Type t) {
        this.type = t;
    }
    /**
     * Type of query.  
     * NOTE: Type.AllowEnabled will also be called for all types that have checkEnabledFirst=true
     */
    public Type getType() {
        return this.type;
    }

    // set aresponse to the request.
    public void setResponse(String response) {
        this.response = response;
    }
    public String getResponse() {
        return this.response;
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    public void setThrowable(Throwable t) {
        this.throwable = t;
    }


    public String getDisplayResponse() {
        String s = getResponse();
        Throwable t = getThrowable();
        if (OAString.isEmpty(s) && t != null) {
            if (t != null) {
                for (; t!=null; t=t.getCause()) {
                    s = t.getMessage();
                    if (OAString.isNotEmpty(s)) break;
                }
                if (OAString.isEmpty(s)) s = getThrowable().toString();
            }
        }
        return s;
    }
    

    public String getConfirmTitle() {
        return confirmTitle;
    }
    public void setConfirmTitle(String confirmTitle) {
        this.confirmTitle = confirmTitle;
    }
    public String getConfirmMessage() {
        return confirmMessage;
    }
    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    
    public String getToolTip() {
        return toolTip;
    }
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public boolean getAllowed() {
        return allowed;
    }
    public void setAllowed(boolean enabled) {
        this.allowed = enabled;
    }
    
    
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public boolean getBooleanValue() {
        return OAConv.toBoolean(value);
    }
    public int getIntValue() {
        return OAConv.toInt(value);
    }
    

    public JLabel getLabel() {
        return label;
    }
    public void setLabel(JLabel label) {
        this.label = label;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

}    
	
