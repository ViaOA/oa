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

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;

/**
    OADataSource that does not support selects or storage.  Can be used to act as a "dummy" datasource.  It will
    assign autoNumbers for new objects that have object Id properties that are numbers and not initialized.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/

public class OADataSourceAuto extends OADataSource {
    private static Hub hubNextNumberGlobal; // new numbers for seq ids
    private boolean bSupportAllClasses = true;
    private Hub hubNextNumber; // new numbers for seq ids

    public OADataSourceAuto() {
        this(true);
    }
    public OADataSourceAuto(boolean bMakeLastDataSource) {
        this(null);
        super.bLast = bMakeLastDataSource;
    }

    /** Hub hubNextNumber must include a separate NextNumber2 object for each class
        that needs to have a seqId assigned to its objectId property. The objects in
        hubNextNumber also need to be saved (OAObject.save() or hubNextNumber.saveAll()).
        The objectId property will be set when the object is created (OAObject constructor)
    */
    public OADataSourceAuto(Hub hubNextNumber) {
        super.bLast = true;
        if (hubNextNumber == null) {
            hubNextNumber = hubNextNumberGlobal;
            if (hubNextNumber == null) {
                hubNextNumber = new Hub(NextNumber.class);
            }
        }
        setGlobalNextNumber(hubNextNumber);
        
        setHub(hubNextNumber);
        setName("OADataSourceAuto DataSource");
    }

    public static void setGlobalNextNumber(Hub hubNextNumber) {
        hubNextNumberGlobal = hubNextNumber;
    }
    public static Hub<NextNumber> getGlobalNextNumber() {
        return hubNextNumberGlobal;
    }
    
    

    /**
        Hub used to store NextNumber2 objects used for assigning new property ids.
    */
    public void setHub(Hub hubNextNumber) {
        if (hubNextNumber == null || !hubNextNumber.getObjectClass().equals(NextNumber.class)) {
            throw new IllegalArgumentException("OADataSourceNextNumber() Hub must be for NextNumber2.class objects");
        }
        this.hubNextNumber = hubNextNumber;
    }
    /**
        Hub used to store NextNumber2 objects used for assigning new property ids.
    */
    public Hub getHub() {
        return hubNextNumber;
    }


    /**
        Overwritten to return false.
    */
    public boolean supportsStorage() {
        return false;
    }


    /**
        Used to know if this DataSource should respond true to all request for service for Classes.
        This can be used to act as a catch all for DataSource requests.
    */
    public boolean getSupportAllClasses() {
        return bSupportAllClasses;
    }

    /**
        Used to know if this DataSource should respond true to all request for service for Classes.
        This can be used to act as a catch all for DataSource requests.
    */
    public void setSupportAllClasses(boolean b) {
        bSupportAllClasses = b;
    }


    /**
        Returns true if NextNumber2 with Class name as Id is in HubNextNumber or if getSupportAllClasses is true.
        @see #getHub
        @see #setSupportAllClasses
    */
    @Override
    public boolean isClassSupported(Class clazz, OAFilter filter) {
        if (clazz == null) return false;
        if (clazz.equals(NextNumber.class)) return true;

        NextNumber nn  = getNextNumber(clazz);
        return (nn != null);
    }

    private Object LOCK = new Object();
    private NextNumber getNextNumber(Class clazz) {
    	NextNumber nn = (NextNumber) hubNextNumber.getObject(clazz.getName());
        if (nn != null) return nn;
        
        if (!bSupportAllClasses) return null;
        
        synchronized (LOCK) {
        	nn = (NextNumber) hubNextNumber.getObject(clazz.getName());
            if (nn != null) return nn;

            nn = new NextNumber();
            nn.setId(clazz.getName());

            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            String[] props = oi.getIdProperties();
            if (props != null && props.length > 0) {
            	nn.setProperty(props[0]);
            }
            hubNextNumber.add(nn);
        }
        return nn;
    }
    
    /**
        Set any objectId properties that are of class Number (or primitive equiv) and
        whose value is "0" to the value in the NextNumber object found in getHub().
        This will also call OAObject.save() if Auto Save is true.

    */
    public void assignId(OAObject oaObj) {
        if (oaObj == null) return;

        NextNumber nn = getNextNumber(oaObj.getClass());
        if (nn == null) return;
        String prop = nn.getProperty();
        if (prop == null) return;

        int id;
        for (;;) {
	        synchronized (nn) {
	        	id = nn.getNext();
	        	nn.setNext(id+1);
	        }
	        // 20141201
            Object test = OAObjectCacheDelegate.getObject(oaObj.getClass(), id);
	        //was: Object test = OAObjectReflectDelegate.getObject(oaObj.getClass(), id);
	        if (test == null) break;
        }
        
        try {
            OAObjectDSDelegate.setAssigningId(oaObj, true);
            oaObj.setProperty(prop, id);
        }
        finally {
            OAObjectDSDelegate.setAssigningId(oaObj, false);
        }
    }

    

    @Override
	public void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
	}

    /**
        Returns true if propertyName is an Object Id property.
    */
    public boolean willCreatePropertyValue(OAObject oaObj, String propertyName) {
        if (oaObj != null && propertyName != null) {
        	NextNumber nn = getNextNumber(oaObj.getClass());
        	if (nn != null) {
        		if (propertyName.equalsIgnoreCase(nn.getProperty())) return true;
        	}        	
        }
        return false;
    }

    /**
        Overwritten to only initialize object.  OADataSourceNextNumber Does not support data storage.
    */
    public void insert(OAObject object) {
        if (!getAssignIdOnCreate()) assignId(object);
    }
    
    public void insertWithoutReferences(OAObject obj) {
        if (!getAssignIdOnCreate()) assignId(obj);
    }

    
    /**
        Overwritten to do nothing.  OADataSourceNextNumber Does not support data storage.
    */
    public void update(OAObject object, String[] includeProperties, String[] excludeProperties) {
    }

    /**
        Does not support data storage.
    */
    public void delete(OAObject object) {
    }


    /**
        Overwritten to always return null.  OADataSourceNextNumber Does not support data storage.
    */
    public Object execute(String command) {
        return null;
    }

    @Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        Object objx = obj.getProperty(propertyName);
        if (objx instanceof byte[]) return (byte[]) objx;
        return null;
    }

    @Override
    public int count(Class selectClass, String queryWhere, Object[] params, OAObject whereObject, String propertyFromWhereObject, String extraWhere, int max) {
        return -1;
    }

    @Override
    public int countPassthru(Class selectClass, String queryWhere, int max) {
        return -1;
    }

    @Override
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, OAObject whereObject, String propertyFromWhereObject, String extraWhere, int max, OAFilter filter, boolean bDirty) {
        return null;
    }

    @Override
    public OADataSourceIterator selectPassthru(Class selectClass, String queryWhere, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return null;
    }
}

