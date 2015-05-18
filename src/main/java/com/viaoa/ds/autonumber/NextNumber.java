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
package com.viaoa.ds.autonumber;

import java.util.*;
import java.util.logging.Logger;
import java.lang.reflect.*;

import com.viaoa.*;
import com.viaoa.object.*;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAId;
import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/** 
    Class used to store sequential numbers for assigning autonumber propeties in Objects. 
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
@OAClass(localOnly=true, useDataSource=false, initialize=false)
public class NextNumber extends OAObject {
    static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(NextNumber.class.getName());
    
    protected String id;   // class name
    protected int nextNum = 1;
    protected String propertyName;
    
    private static int cnter; 
    public NextNumber() {
        cnter++;
    }
    /**
        Returns Identifier for this object, the Class name (including package name).
    */
    @OAProperty(isUnique = true)
    @OAId()
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
        LOG.fine("NextNumber, id="+id);
        if (cnter > 200) {
            LOG.warning("NextNumber over 200, id="+id);
        }
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
    
    
    /*========================= Object Info ============================
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
    */
}

