package com.viaoa.object;

import java.lang.reflect.Method;

public class OAPropertyInfo implements java.io.Serializable {
    static final long serialVersionUID = 1L;    

	private String name;
	 
	private int maxLength;
	private boolean required;
	private boolean id;
	private Class classType;
	private int decimalPlaces = -1;
	private boolean isBlob;
	private boolean isNameValue;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
}



