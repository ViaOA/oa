/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.ds.autonumber;

import java.util.*;
import java.lang.reflect.*;

import com.viaoa.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/** 
    Class used to store sequential numbers for assigning autonumber propeties in Objects. 
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class NextNumber extends OAObject {
    static final long serialVersionUID = 1L;

    protected String id;   // class name
    protected int nextNum = 1;
    protected String propertyName;
    
    
    /**
        Returns Identifier for this object, the Class name (including package name).
    */
    public String getId() {  
        return id;
    }

    /**
        Set automatically to the full Class name when the Class is assigned.
        @see #setClass
    */
    public void setId(String id) {
        String old = this.id;
        this.id = id;
        firePropertyChange("Id", old, this.id);
    }
    
    /**
        Returns the next number to assign.
    */
    public int getNext() {
        return nextNum;
    }
    /**
        Sets the next number to assign.
    */
    public void setNext(int nextNum) {
        int old = this.nextNum;
        this.nextNum = nextNum;
        firePropertyChange("next", old, this.nextNum);
    }
    
    public void setProperty(String prop) {
        String old = this.propertyName;
    	this.propertyName = prop;
        firePropertyChange("property", old, this.propertyName);
    }
    public String getProperty() {
    	return propertyName;
    }
    
    
    //========================= Object Info ============================
    public static OAObjectInfo getOAObjectInfo() {
        return oaObjectInfo;
    }
    protected static OAObjectInfo oaObjectInfo;
    static {
        oaObjectInfo = new OAObjectInfo(new String[] {"Id"});
        oaObjectInfo.setLocalOnly(true);
        oaObjectInfo.setUseDataSource(false);
        oaObjectInfo.setInitializeNewObjects(false);
    }
}

