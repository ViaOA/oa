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

import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.Hub;

public class OAPropertyInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;    

	private String name;
	 
	private int maxLength;
    private int displayLength;
    private int columnLength;
    
	private boolean required;
	private boolean id;
    private boolean unique;
	private Class classType;
	private int decimalPlaces = -1;
	private boolean isBlob;
	private boolean isNameValue;
	private String displayName;
	private String columnName;
    private boolean isUnicode;
    private boolean isImportMatch;
    private boolean isPassword;
    private Hub<String> hubNameValue;
    private boolean isCurrency;
    private Method editQueryMethod;
    private boolean isProcessed;

    private OAProperty oaProperty;    
    
	
	public OAPropertyInfo() {
	}

	public Class getClassType() {
		return classType;
	}
	public void setClassType(Class classType) {
		this.classType = classType;
	}
	public boolean getId() {
		return id;
	}
	public void setId(boolean id) {
		this.id = id;
	}
    public boolean getUnique() {
        return unique;
    }
    public void setUnique(boolean bUnique) {
        this.unique = bUnique;
    }
    public boolean getProcessed() {
        return isProcessed;
    }
    public void setProcessed(boolean b) {
        this.unique = isProcessed;
    }
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
    public int getDisplayLength() {
        return displayLength;
    }
    public void setDisplayLength(int length) {
        this.displayLength = length;
    }
    public int getColumnLength() {
        return columnLength;
    }
    public void setColumnLength(int length) {
        this.columnLength = length;
    }

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String name) {
        this.columnName = name;
    }

	public boolean getRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public void setDecimalPlaces(int x) {
	    this.decimalPlaces = x;
	}
	public int getDecimalPlaces() {
	    return this.decimalPlaces;
	}
	
	public boolean isBlob() {
	    return isBlob;
	}
	public void setBlob(boolean b) {
	    this.isBlob = b;
	}
	
    public boolean isNameValue() {
        return isNameValue;
    }
    public void setNameValue(boolean b) {
        this.isNameValue = b;
    }

    public boolean isUnicode() {
        return isUnicode;
    }
    public void setUnicode(boolean b) {
        this.isUnicode = b;
    }

    public boolean isImportMatch() {
        return isImportMatch;
    }
    public void setImportMatch(boolean b) {
        this.isImportMatch = b;
    }
    public boolean isPassword() {
        return isPassword;
    }
    public void setPassword(boolean b) {
        this.isPassword = b;
    }
    public void setOAProperty(OAProperty p) {
        oaProperty = p;
    }
    public OAProperty getOAProperty() {
        return oaProperty;
    }
    
    public Hub<String> getNameValues() {
        if (hubNameValue == null) hubNameValue = new Hub<String>(String.class);  
        return hubNameValue;
    }
    
    public boolean isCurrency() {
        return isCurrency;
    }
    public void setCurrency(boolean b) {
        this.isCurrency = b;
    }
    
    private String[] viewDependentProperties;
    public void setViewDependentProperties(String[] ss) {
        this.viewDependentProperties = ss;
    }
    public String[] getViewDependentProperties() {
        return this.viewDependentProperties;
    }
    private String[] userDependentProperties;
    public void setUserDependentProperties(String[] ss) {
        this.userDependentProperties = ss;
    }
    public String[] getUserDependentProperties() {
        return this.userDependentProperties;
    }
    
    private String enabledProperty;
    private boolean enabledValue;
    private String visibleProperty;
    private boolean visibleValue;
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

    private String userEnabledProperty;
    private boolean userEnabledValue;
    private String userVisibleProperty;
    private boolean userVisibleValue;
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
}



