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

import java.lang.reflect.Method;

import com.viaoa.annotation.OAMethod;

public class OAMethodInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;    

	private String name;

    private String enabledProperty;
    private boolean enabledValue;
    private String visibleProperty;
    private boolean visibleValue;
	
    private String contextEnabledProperty;
    private boolean contextEnabledValue;
    private String contextVisibleProperty;
    private boolean contextVisibleValue;
    
    private Method editQueryMethod;
    private OAMethod oaMethod;
    
    
	public OAMethodInfo() {
	}

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
    private String[] viewDependentProperties;
    public void setViewDependentProperties(String[] ss) {
        this.viewDependentProperties = ss;
    }
    public String[] getViewDependentProperties() {
        return this.viewDependentProperties;
    }	

    private String[] contextDependentProperties;
    public void setContextDependentProperties(String[] ss) {
        this.contextDependentProperties = ss;
    }
    public String[] getContextDependentProperties() {
        return this.contextDependentProperties;
    }   
    
    public String getEnabledProperty() {
        return enabledProperty;
    }
    public void setEnabledProperty(String s) {
        enabledProperty = s;
    }
    public boolean getEnabledValue() {
        return enabledValue;
    }
    public void setEnabledValue(boolean b) {
        enabledValue = b;
    }

    public String getVisibleProperty() {
        return visibleProperty;
    }
    public void setVisibleProperty(String s) {
        visibleProperty = s;
    }
    public boolean getVisibleValue() {
        return visibleValue;
    }
    public void setVisibleValue(boolean b) {
        visibleValue = b;
    }

    public String getContextEnabledProperty() {
        return contextEnabledProperty;
    }
    public void setContextEnabledProperty(String s) {
        contextEnabledProperty = s;
    }
    public boolean getContextEnabledValue() {
        return contextEnabledValue;
    }
    public void setContextEnabledValue(boolean b) {
        contextEnabledValue = b;
    }
    public String getContextVisibleProperty() {
        return contextVisibleProperty;
    }
    public void setContextVisibleProperty(String s) {
        contextVisibleProperty = s;
    }
    public boolean getContextVisibleValue() {
        return contextVisibleValue;
    }
    public void setContextVisibleValue(boolean b) {
        contextVisibleValue = b;
    }
    
    
    public void setEditQueryMethod(Method m) {
        this.editQueryMethod = m;
    }
    public Method getEditQueryMethod() {
        return editQueryMethod;
    }
    
    public void setOAMethod(OAMethod m) {
        this.oaMethod = m;
    }
    public OAMethod getOAMethod() {
        return oaMethod;
    }
}
