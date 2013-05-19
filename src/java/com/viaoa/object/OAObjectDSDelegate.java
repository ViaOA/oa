/**
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

Copyright (c) 2001-2007 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.object;

import java.util.logging.Logger;

import com.viaoa.ds.*;
import com.viaoa.hub.Hub;


public class OAObjectDSDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectDSDelegate.class.getName());

    /**
     * Initialize a newly created OAObject.
     */
    public static void initialize(OAObject oaObj) {
    	if (oaObj == null) return;
    	// OADataSource is set up to check isLoading() so that it does not initialize the objects that it is creating    	
        OADataSource ds = getDataSource(oaObj);
        if (ds != null) {
        	ds.initializeObject(oaObj);  // datasource might need to set Id property
        }
    }
    
	/**
	    Returns the OADataSource that works with this objects Class.
	*/
	protected static OADataSource getDataSource(Object obj) {
	    return OADataSource.getDataSource(obj.getClass());
	}

    protected static boolean hasDataSource(OAObject oaObj) {
	    return OADataSource.getDataSource(oaObj.getClass()) != null;
	}
	protected static boolean hasDataSource(Class c) {
	    return OADataSource.getDataSource(c) != null;
	}
	
	protected static boolean supportsStorage(Class clazz) {
		OADataSource ds = OADataSource.getDataSource(clazz);
		return (ds != null && ds.supportsStorage());
	}
	
	
    /**
	 * Find the OAObject given a key value.  This will look in the Cache and the DataSource.
	 * @param clazz class of reference of to find.
	 * @param key can be the value of the key or an OAObjectKey
	 */
	public static OAObject getObject(Class clazz, Object key) {
		if (clazz == null || key == null) return null;
		OADataSource ds = OADataSource.getDataSource(clazz);
        OAObject oaObj = null;
		if (ds != null) {
	        if (!(key instanceof OAObjectKey)) {  
	        	key = OAObjectKeyDelegate.convertToObjectKey(clazz, key);
	        }
        	oaObj = (OAObject) ds.getObject(clazz, key);
        }
        return oaObj;
	}
	
	
	protected static Object getObject(Class clazz, OAObjectKey key) {
		return OADataSource.getObject(clazz, key);
	}
    protected static Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key) {
        OADataSource ds = OADataSource.getDataSource(clazz);
        if (ds == null) return null;
        return ds.getObject(oi, clazz, key);
    }

    protected static Object getBlob(OAObject obj, String propName) {
        if (obj == null || propName == null) return null;
        Class clazz = obj.getClass();
        OADataSource ds = OADataSource.getDataSource(clazz);
        return ds.getPropertyBlobValue(obj, propName);
    }
	
    
    
    /** @param bFullSave false=dont flag as unchanged, used when object needs to be saved twice. First to create
	    object in datasource so that reference objects can refer to it
	*/
	protected static void save(OAObject oaObj) {
		OADataSource dataSource = getDataSource(oaObj);
		if (dataSource != null && dataSource.supportsStorage()) {
		   	if (oaObj.getNew()) {
		   		dataSource.insert(oaObj);
		   	}
		   	else {
		       dataSource.update(oaObj);
		   	}
		}
	}
	protected static void saveWithoutReferences(OAObject oaObj) {
		OADataSource dataSource = getDataSource(oaObj);
		if (dataSource != null && dataSource.supportsStorage()) {
		   	if (oaObj.getNew()) {
		   		dataSource.insertWithoutReferences(oaObj);
		   	}
		   	else {
		       // error, should only be used by new objects
		   	}
		}
	}
	

    public static void removeReference(OAObject oaObj, OALinkInfo li) {
        if (li == null) return;
        OADataSource dataSource = getDataSource(oaObj);
        if (dataSource != null && dataSource.supportsStorage()) {
            if (!oaObj.getNew()) {
                dataSource.update(oaObj, new String[] {li.getName()}, null);  // only update the link property name (which is null)
            }
        }
    }

	public static void save(OAObject obj, boolean bInsert) {
		OADataSource dataSource = getDataSource(obj);
		if (dataSource != null && dataSource.supportsStorage()) {
		   	if (bInsert) dataSource.insert(obj);
		   	else dataSource.update(obj);
		}		
	}
	
	/** called after all listeners have been called.
	    It will find the OADataSource to use and call its "delete(this)"
	*/
	public static void delete(OAObject obj) {
		if (obj == null) return;
		OADataSource ds = OADataSource.getDataSource(obj.getClass());
		if (ds != null && ds.supportsStorage()) {
        	ds.delete(obj);
        }
	}
    

	public static boolean allowIdChange(Class c) {
        OADataSource ds = OADataSource.getDataSource(c);
        return (ds == null || ds.getAllowIdChange());
	}
	
	public static Object getObject(OAObject oaObj) {
        OADataSource ds = OADataSource.getDataSource(oaObj.getClass());
        //qqqqqqq todo: check this out: if (ds == null || ds.isAssigningId(oaObj)) return null;  // datasource could be assigning the Id to a unique value
        return ds.getObject(oaObj.getClass(), OAObjectKeyDelegate.getKey(oaObj));
	}

}





