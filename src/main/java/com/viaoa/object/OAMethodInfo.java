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
	
    private String userEnabledProperty;
    private boolean userEnabledValue;
    private String userVisibleProperty;
    private boolean userVisibleValue;
    
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

    public String getUserEnabledProperty() {
        return userEnabledProperty;
    }
    public void setUserEnabledProperty(String s) {
        userEnabledProperty = s;
    }
    public boolean getUserEnabledValue() {
        return userEnabledValue;
    }
    public void setUserEnabledValue(boolean b) {
        userEnabledValue = b;
    }
    public String getUserVisibleProperty() {
        return userVisibleProperty;
    }
    public void setUserVisibleProperty(String s) {
        userVisibleProperty = s;
    }
    public boolean getUserVisibleValue() {
        return userVisibleValue;
    }
    public void setUserVisibleValue(boolean b) {
        userVisibleValue = b;
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
