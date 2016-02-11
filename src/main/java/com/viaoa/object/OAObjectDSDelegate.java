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

    public static boolean supportsInitializeObject(OAObject oaObj) {
        if (oaObj == null) return false;
        // OADataSource is set up to check isLoading() so that it does not initialize the objects that it is creating       
        OADataSource ds = getDataSource(oaObj);
        if (ds == null) return false;
        return ds.supportsInitializeObject();
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
    public static void refreshObject(OAObject obj) {
        if (obj == null) return;
        Class clazz = obj.getClass();
        OADataSource ds = OADataSource.getDataSource(clazz);
        if (ds != null) {
            OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            ds.getObject(oi, clazz, key, true);  // true=reload all props
        }
    }
	
	
	protected static Object getObject(Class clazz, OAObjectKey key) {
		return OADataSource.getObject(clazz, key);
	}
    protected static Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key) {
        OADataSource ds = OADataSource.getDataSource(clazz);
        if (ds == null) return null;
        return ds.getObject(oi, clazz, key, false);
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
		if (dataSource != null) {
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
		if (dataSource != null) {
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
        if (dataSource != null) {
            if (!oaObj.getNew()) {
                dataSource.update(oaObj, new String[] {li.getName()}, null);  // only update the link property name (which is null)
            }
        }
    }

	public static void save(OAObject obj, boolean bInsert) {
		OADataSource dataSource = getDataSource(obj);
		if (dataSource != null) {
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
		if (ds != null) {
        	ds.delete(obj);
        }
	}
    

	public static boolean allowIdChange(Class c) {
        OADataSource ds = OADataSource.getDataSource(c);
        return (ds == null || ds.getAllowIdChange());
	}
	
	public static Object getObject(OAObject oaObj) {
        OADataSource ds = OADataSource.getDataSource(oaObj.getClass());
        // todo: check this out: if (ds == null || ds.isAssigningId(oaObj)) return null;  // datasource could be assigning the Id to a unique value
        return ds.getObject(oaObj.getClass(), OAObjectKeyDelegate.getKey(oaObj));
	}
}
