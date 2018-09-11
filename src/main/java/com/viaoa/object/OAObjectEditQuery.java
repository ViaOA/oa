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

import com.viaoa.util.OAConv;

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
    
    private boolean allowEnabled=true;  // also works with verifyPropertyChange
    private boolean allowVisible=true;

    private boolean allowAdd=true;    // hubs
    private boolean allowRemove=true; // hubs
    private boolean allowRemoveAll=true; // hubs
    private boolean allowDelete=true; // hubs

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
                             
        VerifyPropertyChange(true, true),// use: value to get new value, name, response, throwable - set allowEnablede=false, or throwable!=null to cancel
        VerifyAdd(true, true),           // use: value to get added object, allowAdd, throwable - set allowAdd=false, or throwable!=null to cancel
        VerifyRemove(true, true),        // use: value to get removed object, allowRemove, throwable - set allowRemove=false, or throwable!=null to cancel
        VerifyRemoveAll(true, true),     // use: allowRemoveAll, response, throwable - set allowRemoveAll=false, or throwable!=null to cancel
        VerifyDelete(true, true),        // use: value to get deleted object, allowDelete, throwable - set allowDelete=false, or throwable!=null to cancel
        
        GetConfirmPropertyChange(false),
        GetConfirmAdd(false),
        GetConfirmRemove(false),
        GetConfirmDelete(false),
        
        GetToolTip(false),      // use: toolTip
        RenderLabel(false),     // use: label and update it's props
        GetFormat(false);        // use: format
        
        public boolean checkOwner, checkEnabledFirst;
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
    }
    
    public void setType(Type t) {
        this.type = t;
    }
    /**
     * Type of query.  
     * 
     * NOTE: Type.AllowEnabled will also be called for all types that have checkEnabledFirst=true
     * 
     * @return
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

    public boolean getAllowEnabled() {
        return allowEnabled;
    }
    public void setAllowEnabled(boolean enabled) {
        this.allowEnabled = enabled;
    }
    public boolean getAllowVisible() {
        return allowVisible;
    }
    public void setAllowVisible(boolean allowVisible) {
        this.allowVisible = allowVisible;
    }
    public boolean getAllowAdd() {
        return allowAdd;
    }
    public void setAllowAdd(boolean allowAdd) {
        this.allowAdd = allowAdd;
    }
    public boolean getAllowRemove() {
        return allowRemove;
    }
    public void setAllowRemove(boolean allowRemove) {
        this.allowRemove = allowRemove;
    }
    public boolean getAllowRemoveAll() {
        return allowRemoveAll;
    }
    public void setAllowRemoveAll(boolean allowRemoveAll) {
        this.allowRemoveAll = allowRemoveAll;
    }
    public boolean getAllowDelete() {
        return allowDelete;
    }
    public void setAllowDelete(boolean allowDelete) {
        this.allowDelete = allowDelete;
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

    // get the return boolean from a "allow" or "verify" query
    public boolean getAllowed() {
        if (getThrowable() != null) return false;
        if (!getAllowEnabled()) return false;  // all are disabled
        
        switch (getType()) {
        case AllowEnabled:
        case VerifyPropertyChange:
            break;
        case AllowVisible:
            if (!getAllowVisible()) return false;
            break;
        case AllowAdd:
        case VerifyAdd:
            if (!getAllowAdd()) return false;
            break;
        case AllowRemove:
        case VerifyRemove:
            if (!getAllowRemove()) return false;
            break;
        case AllowRemoveAll:
        case VerifyRemoveAll:
            if (!getAllowRemoveAll()) return false;
            break;
        case AllowDelete:
        case VerifyDelete:
            if (!getAllowDelete()) return false;
            break;
        }
        return true;
    }

    public void setAllowed(boolean b) {
        switch (getType()) {
        case AllowEnabled:
        case VerifyPropertyChange:
            setAllowEnabled(b);
            break;
        case AllowVisible:
            setAllowVisible(b);
            break;
        case AllowAdd:
        case VerifyAdd:
            setAllowAdd(b);
            break;
        case AllowRemove:
        case VerifyRemove:
            setAllowRemove(b);
            break;
        case AllowRemoveAll:
        case VerifyRemoveAll:
            setAllowRemoveAll(b);
            break;
        case AllowDelete:
        case VerifyDelete:
            setAllowDelete(b);
            break;
        }
    }

}    
	
