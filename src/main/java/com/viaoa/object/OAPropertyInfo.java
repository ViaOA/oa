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
package com.viaoa.object;

public class OAPropertyInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;    

	private String name;
	 
	private int maxLength;
    private int displayLength;
    private int columnLength;
    
	private boolean required;
	private boolean id;
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



