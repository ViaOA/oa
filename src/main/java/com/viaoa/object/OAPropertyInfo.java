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
}



