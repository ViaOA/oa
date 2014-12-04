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
package com.viaoa.ds;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.util.OAFilter;

/**
    Abstract class used for defining sources for Object storage.  <br>
    A dataSource can be anything, including Relational Databases, XML data, legacy database, persistent
    data storage, etc.
    <p>
    There are methods defined for updating, deleting, and using Queries to retreive Objects.
    Queries are based on the structure of Objects and not on the structure of the physical
    dataSource.
    <p>
    The OAObject and Hub Collections have methods to automatically and naturally work with dataSources,
    without requiring any direct access to dataSource methods.
    <p>
    OADataSource has static methods that are used to manage all created OADataSources.
    Subclasses of OADataSource register themselves with the static OADataSource so that they can be <i>found</i> and
    used by Objects based on the Object Class, without requiring direct access to the OADataSource object. <br>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see #getDataSource(Class)
    @see OASelect
*/
public abstract class OADataSource {
    private static Vector vecDataSource = new Vector(5,5);
    protected String name;
    protected boolean bLast;
    protected boolean bAssignNumberOnCreate;  // if true, then Id will be assigned when object is created, else when saved
    protected String guid; // seed value to use when creating GUID for seq assigned object keys
    protected boolean bEnable=true;
    
    //-------- static methods -------------------------------
    /**
        Get all registered/loaded DataSources.
    */
    public static OADataSource[] getDataSources() {
        OADataSource[] ds;
        synchronized(vecDataSource) {
            int x = vecDataSource.size();
            ds = new OADataSource[x];
            vecDataSource.copyInto(ds);
        }
        return ds;
    }

    /**
        Find the dataSource that supports a given Class
        @see #setEnabled
    */
    public static OADataSource getDataSource(Class clazz) {
        OADataSource[] ds = getDataSources();
        int x = ds.length;
        OADataSource dsFound = null;
        for (int i=0; i<x; i++) {
            if (ds[i].bEnable && ds[i].isClassSupported(clazz)) {
                if (dsFound == null || (dsFound.bLast && !ds[i].bLast)) dsFound = ds[i];
            }
        }
        return dsFound;
    }

    /**
        Seed value to use when creating GUID for seq assigned object keys.
        Autonumber properties will prefix new values with the value plus a "-" to separator.
    */
    public void setGuid(String gid) {
        guid = gid;
    }
    /**
        Seed value to use when creating GUID for seq assigned object keys.
        Autonumber properties will prefix new values with the value plus a "-" to separator.
    */
    public String getGuid() {
        return guid;
    }


    /** 
     * Used to turn on/off a DataSource.  If false, then requests to OADataSource.getDataSource will
     * not return a disabled dataSource.
     */
    public void setEnabled(boolean b) {
    	this.bEnable = b;
    }
    public boolean getEnabled() {
    	return this.bEnable;
    }
    
    /**
        Used to retreive a single object from DataSource.
        @param id is the property key value for the object.
    */
    public static Object getObject(Class clazz, String id) {
        OAObjectKey key = new OAObjectKey(id);
        return getObject(clazz, key);
    }

    /**
        Used to retreive a single object from DataSource.
        @param id is the property key value for the object.
    */
    public static Object getObject(Class clazz, int id) {
        OAObjectKey key = new OAObjectKey(id);
        return getObject(clazz, key);
    }

    /**
        Used to retreive a single object from DataSource.
        @param id is the property key value for the object.
    */
    public static Object getObject(Class clazz, long id) {
        OAObjectKey key = new OAObjectKey(id);
        return getObject(clazz, key);
    }

    /**
        Used to retreive a single object from DataSource.
        @param id is the property key value for the object.
    */
    public static Object getObject(Class clazz, Object id) {
    	OAObjectKey key = new OAObjectKey(id);
        return getObject(clazz, key);
    }

    /**
        Used to retreive a single object from DataSource.
        @param key is the object key for the object.
    */
    public static Object getObject(Class clazz, OAObjectKey key) {
        if (clazz == null || key == null) return null;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        OADataSource ds = getDataSource(clazz);
        if (ds == null) return null;
        return ds.getObject(oi, clazz, key);
    }
    public Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key) {
        if (clazz == null || key == null || oi == null) return null;
        OADataSource ds = getDataSource(clazz);
        if (ds == null) return null;
        if (!ds.supportsStorage()) return null;

        String[] props = oi.getIdProperties();
        
        String query = "";
        for (int i=0; props != null && i < props.length; i++) {
            if (i > 0) query += " && ";
            query += props[i] + " == ?";
        }

        Object obj = null;
        Iterator it = ds.select(clazz, query, key.getObjectIds(), "");
        if (it != null && it.hasNext()) {
            obj = it.next();
            it.remove();
        }
        return obj;
    }


    /**
        Used to know if autonumber properties should be assigned on create or on save.
        @param b if true, assign autonumber property when object is created, else assign when object is saved.
    */
    public void setAssignNumberOnCreate(boolean b) {
        bAssignNumberOnCreate = b;
    }
    /**
        Used to know if autonumber properties should be assigned on create or on save.
        @see #setAssignNumberOnCreate
    */
    public boolean getAssignNumberOnCreate() {
        return bAssignNumberOnCreate;
    }

    /**
        Used to know is datasoure is currently available.
    */
    public boolean isAvailable() {
        return true;
    }

    /**
        Returns a Vector of Strings listing all registered OADataSources and status.
    */
    public static Vector getInfo() {
        Vector vec = new Vector(20,20);
        vec.addElement("OADataSource Info --- ");
        OADataSource[] dss = getDataSources();
        for (int i=0 ; i<dss.length; i++) {
            vec.addElement("OADataSource #"+i);
            dss[i].getInfo(vec);
        }
        return vec;
    }

    /**
        Adds Strings to Vector, listing information about DataSource.
    */
    public void getInfo(Vector vec) {
    }


    //-------------------------------------------------------
    //-------------------------------------------------------
    //-------------------------------------------------------

    /**
        Returns max length allowed for a property.  returns "-1" for any length.
    */
    public int getMaxLength(Class c, String propertyName) {
        return -1;
    }

    /**
        Default constructor that will add this DataSource to list of DataSources
        @see #getDataSources
    */
    public OADataSource() {
    	this(true);
    }
    public OADataSource(boolean bRegister) {
        if (bRegister) {
            vecDataSource.addElement(this);
        }
        dataSourceChangeCnter++;
    }

    protected static int dataSourceChangeCnter;
    public static int getChangeCounter() {
        return dataSourceChangeCnter;
    }
    
    
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
        Static method to close all registered DataSources.
    */
    public static void closeAll() {
        dataSourceChangeCnter++;
        while (vecDataSource.size() > 0) {
            ((OADataSource) vecDataSource.elementAt(0)).close();
        }
        vecDataSource.removeAllElements();
    }

    /**
        Close this DataSource.
    */
    public void close() {
        vecDataSource.removeElement(this);
        dataSourceChangeCnter++;
    }

    /**
     * This can be called after a close has been done to make the datasoruce available again.
     * @param pos search location in list of datasources.
     */
    public void reopen(int pos) {
        if (!vecDataSource.contains(this)) {
            int x = vecDataSource.size();
            pos = Math.max(0, Math.min(x, pos));
            vecDataSource.insertElementAt(this, pos);
            dataSourceChangeCnter++;
        }
    }
    
    /**
        Used to have a DataSource search last when finding a DataSource.  This is used when you
        want to create a <i>catch all</i> DataSource.
        @param b If true, then this dataSource will be used last in list of DataSources
    */
    public void setLast(boolean b) {
        bLast = b;
    }

    /**
	    Sets the position of this OADataSource within the list of datasources. (0 based).
	*/
	public void setPosition(int pos) {
	    if (pos < 0) pos = 0;
	    int x = vecDataSource.indexOf(this);
	    if (x < 0) return;
	    if (x == pos) return;
	    dataSourceChangeCnter++;
	    vecDataSource.removeElementAt(x);
	    x = vecDataSource.size();
	    if (pos > x) pos = x;
	    vecDataSource.insertElementAt(this, pos);
	}
	
	/**
	    Returns the position of this OADataSource within the list of registered datasources.
	    @returns -1 if not found, else position (0 based)
	*/
	public int getPosition() {
	    return vecDataSource.indexOf(this);
	}

    
    /**
        Name of this dataSource
    */
    public void setName(String name) {
        this.name = name;
    }
    /**
        Name of this dataSource
    */
    public String getName() {
        return name;
    }

    /**
        Returns the Name of this dataSource.
    */
    public String toString() {
        if (name == null) return super.toString();
        else return name;
    }

    /**
        Used by static OADataSource to know if a registered OADataSource subclass
        supports a specific Class.
    */
    public abstract boolean isClassSupported(Class clazz);

    /**
        Used by dataSources to update special requirements for handling Many2Many relationships (ex:Link Table).
        <p>
        Uses the hub.masterObject, Hub.getRemovedObjects(), Hub.getAddedObjects()
        to find out which objects were added or removed.
        <br>
        This is called by OAObject.cascadeSave/Delete methods
     */
	public abstract void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster);
    

    /**
        Add/Insert a new Object into DataSource.
        <p>
        Called directly by OAObject.save()
        @param bForce is true, then object should be inserted without verifying.
        @see OAObject#save
    */
    public abstract void insert(OAObject obj);
    
    
    /**
	    Add/Insert a new Object into DataSource, without references (fkeys).
	    <p>
	    Called directly by OAObject.saveWithoutReferences() to save a reference while saving another Object.
	    @see OAObject#save
	*/
    public abstract void insertWithoutReferences(OAObject obj);
    
    /**
        Update an existing Object to DataSource.
        <p>
        Called directly by OAObject.save()
        @param bForce is true, then object should be inserted without verifying.
        @see OAObject#save
    */
    public abstract void update(OAObject obj, String[] includeProperties, String[] excludeProperties);
    public void update(OAObject obj) {
        update(obj, null, null);
    }
    /**
        Remove an Object from a DataSource.
    */
    public abstract void delete(OAObject obj);


    /**
        Used to save an object to DataSource.
        <p>
        If object is an OAObject, then update() or insert() will be called, else nothing is done.
        @see #insert
        @see #update
    */
    public void save(OAObject obj) {
        // if it can be decided to use either insert() or update()
        if (obj == null) return;
        if (obj instanceof OAObject) {
            if ( ((OAObject)obj).getNew() ) insert(obj);
            else update(obj);
        }
    }


    /**
        Perform a count on the DataSource using a query.
        @param clazz Class to perform query on
        @param queryWhere query using property paths based on Object structure.
        @see OASelect
    */
    public abstract int count(Class clazz, String queryWhere, int max);
    public int count(Class clazz, String queryWhere) {
    	return count(clazz, queryWhere, 0);
    }

    /**
    Perform a count on the DataSource using a query.
    @param clazz Class to perform query on
    @param queryWhere query using property paths based on Object structure.
    @param params list of values to replac '?' within the queryWhere clause.
    @see OASelect
	*/
	public abstract int count(Class clazz, String queryWhere, Object[] params, int max);
	
	public int count(Class clazz, String queryWhere, Object[] params) {
		return count(clazz, queryWhere, params, 0);
	}
	public abstract int count(Class clazz, String queryWhere, Object param, int max);
	public int count(Class clazz, String queryWhere, Object param) {
		return count(clazz, queryWhere, param, 0);
	}

    
    /**
        Performs a count using native query language for DataSource.
        @param query query based on DataSource structure.
        @see OASelect
    */
    public abstract int countPassthru(String query, int max);
    public int countPassthru(String query) {
    	return countPassthru(query, 0);
    }

    /**
        Perform a count on the DataSource using an object for query.
        @param selectClass Class to perform query on
        @param whereObject parent object that is used to build where clause for
        @param propertyNameFromMaster name of property from where Object.
        @see OASelect
    */
    public abstract int count(Class selectClass, OAObject whereObject, String propertyNameFromMaster, int max);
    public int count(Class selectClass, OAObject whereObject, String propertyNameFromMaster) {
    	return count(selectClass, whereObject, propertyNameFromMaster, 0);
    }

    /**
        Perform a count on the DataSource using an object for query.
        @param selectClass Class to perform query on
        @param whereObject parent object that is used to build where clause for
        @param extraWhere additional where query to add (using AND) to query string.
        @param propertyNameFromMaster name of property from where Object.
        @see OASelect
    */
    public abstract int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] params, String propertyNameFromMaster, int max);

    public int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] params, String propertyNameFromMaster) {
    	return count(selectClass, whereObject, extraWhere, params, propertyNameFromMaster, 0);
    }


    /**
        Returns true if this dataSource supports selecting/storing/deleting.
    */
    public abstract boolean supportsStorage();


    /**
        Perform a query to retrieve objects from DataSource.
        <p>
        See OASelect for complete description on selects/queriess.
        @param selectClass Class of object to create and return
        @param queryWhere query String using property paths based on Object structure.  DataSource
        will convert query to native query language of the datasoure.
        @return Iterator that is used to return objects of type selectClass
        @see OASelect
     */
    public abstract Iterator select(Class selectClass, String queryWhere, String queryOrder, int max, OAFilter filter);
    public Iterator select(Class selectClass, String queryWhere, String queryOrder, int max) {
        return select(selectClass, queryWhere, queryOrder, max, null);
    }
    public Iterator select(Class selectClass, String queryWhere, String queryOrder) {
    	return select(selectClass, queryWhere, queryOrder, 0, null);
    }

    /**
    Perform a query to retrieve objects from DataSource.
    <p>
    See OASelect for complete description on selects/queriess.
    @param selectClass Class of object to create and return
    @param queryWhere query String using property paths based on Object structure.  DataSource
    @param params list of values to replace '?' in queryWhere clause.
    will convert query to native query language of the datasoure.
    @return Iterator that is used to return objects of type selectClass
    @see OASelect
	 */
	public abstract Iterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, int max, OAFilter filter);
    public Iterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, int max) {
        return select(selectClass, queryWhere, params, queryOrder, max, null);
    }
    public Iterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder) {
    	return select(selectClass, queryWhere, params, queryOrder, 0, null);
    }

	public abstract Iterator select(Class selectClass, String queryWhere, Object param, String queryOrder, int max, OAFilter filter);
    public Iterator select(Class selectClass, String queryWhere, Object param, String queryOrder, int max) {
        return select(selectClass, queryWhere, param, queryOrder, max, null);
    }
    public Iterator select(Class selectClass, String queryWhere, Object param, String queryOrder) {
		return select(selectClass, queryWhere, param, queryOrder, 0, null);
	}

    
    
    // hasNext(), next(), remove() (used to close)

    /**
        Performs a select using native query language for DataSource.
        @param selectClass Class of object to create and return
        @param query query based on DataSource structure.
        @see OASelect
        @return Iterator that is used to return objects of type selectClass
    */
    public abstract Iterator selectPassthru(Class selectClass, String query, int max, OAFilter filter);
    public Iterator selectPassthru(Class selectClass, String query, int max) {
        return selectPassthru(selectClass, query, max, null);
    }
    public Iterator selectPassthru(Class selectClass, String query) {
    	return selectPassthru(selectClass, query, 0, null);
    }

    /**
        Performs a select using native query language for DataSource.
        <p>
        queryWhere should include everything including "FROM", and "WHERE".
        The selected columns will be done automatically. <br>
        "ORDER BY" will be supplied if queryOrder exists.

        @param selectClass Class of object to create and return
        @param query query based on DataSource structure.
        @see OASelect
        @return Iterator that is used to return objects of type selectClass
    */
    public abstract Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder, int max, OAFilter filter);
    public Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder, int max) {
        return selectPassthru(clazz, queryWhere, queryOrder, max, null);
    }
    public Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder) {
    	return selectPassthru(clazz, queryWhere, queryOrder, 0, null);
    }


   
    /**
        Execute a command on the dataSource.
        @param command DataSource native command.
    */
    public abstract Object execute(String command);

    /**
        Perform a query based on a where object to retrieve objects from DataSource.
        <p>
        See OASelect for complete description on selects/queriess.
        @param selectClass Class of object to create and return
        @param whereObject parent object that is used to build where clause for.  Uses convertToString()
        @param extraWhere query String using property paths based on Object structure.  DataSource
        @param queryOrder comma separated list of property paths for sorting.  "DESC" or "ASC" can
        be used to determin descending or ascending order.
        will convert query to native query language of the datasoure.
        @param propertyNameFromMaster name of property from where object.
        @return Iterator that is used to return objects of type selectClass
        @see #convertToString(String,Object)
        @see OASelect
    */
    public abstract Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max, OAFilter filter);
    public Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max) {
        return select(selectClass, whereObject, extraWhere, null, propertyNameFromMaster, queryOrder, max, null);
    }
    public Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder) {
    	return select(selectClass, whereObject, extraWhere, null, propertyNameFromMaster, queryOrder, 0, null);
    }

    /**
        Perform a query based on a where object to retrieve objects from DataSource.
        <p>
        See OASelect for complete description on selects/queriess.
        @param selectClass Class of object to create and return
        @param whereObject parent object that is used to build where clause for.  Uses convertToString()
        @param extraWhere query String using property paths based on Object structure.  DataSource
        @param queryOrder comma separated list of property paths for sorting.  "DESC" or "ASC" can
        be used to determin descending or ascending order.
        will convert query to native query language of the datasoure.
        @param propertyNameFromMaster name of property from where object.
        @return Iterator that is used to return objects of type selectClass
        @see #convertToString(String,Object)
        @see OASelect
    */
    public abstract Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder, int max, OAFilter filter);
    public Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder, int max) {
        return select(selectClass, whereObject, propertyNameFromMaster, queryOrder, max, null);
    }
    public Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder) {
    	return select(selectClass, whereObject, propertyNameFromMaster, queryOrder, 0, null);
    }

    /**
        Called by OAObject to initialize a new Object.
    */
    public abstract void initializeObject(OAObject obj);

    /**
        Called by OAObject to initialize a new Object.
    */
    public boolean supportsInitializeObject() {
        return true;
    }
    
    /**
        Returns true if the dataSource will set the property value before saving.
    */
    public boolean willCreatePropertyValue(OAObject object, String propertyName) {
        return false;
    }

    /**
        Defaults to return true, allowing object Id properties to be changed.  Most DataSources that use foreign keys
        for references will not allow the object id to be changed after the object has been saved.
        @see OADataSourceJDBC#getAllowIdChange
    */
    public boolean getAllowIdChange() {
        return true;
    }

    /**
     * Select BLOB (large byte[]) property 
     */
    public abstract byte[] getPropertyBlobValue(OAObject obj, String propertyName);

    /**
     * Can this datasource get a count of the objects that will be selected.
     */
    public boolean getSupportsPreCount() {
        return true;
    }
}


