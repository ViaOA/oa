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
public abstract class OADataSource implements OADataSourceInterface {
    private static Vector vecDataSource = new Vector(5,5);
    protected String name;
    protected boolean bLast;
    protected boolean bAssignNumberOnCreate=false;  // if true, then Id will be assigned when object is created, else when saved
    protected String guid; // seed value to use when creating GUID for seq assigned object keys
    protected boolean bEnable=true;

    private static volatile OADataSource[] dsAll;
    
    //-------- static methods -------------------------------
    /**
        Get all registered/loaded DataSources.
    */
    public static OADataSource[] getDataSources() {
        if (dsAll == null) {
            synchronized(vecDataSource) {
                if (dsAll == null) {
                    int x = vecDataSource.size();
                    dsAll = new OADataSource[x];
                    vecDataSource.copyInto(dsAll);
                }
            }
        }
        return dsAll;
    }

    /**
        Find the dataSource that supports a given Class
        @see #setEnabled
    */
    public static OADataSource getDataSource(Class clazz) {
        return getDataSource(clazz, (OAFilter) null);
    }
    public static OADataSource getDataSource(Class clazz, OAFilter filter) {
        OADataSource[] ds = getDataSources();
        int x = ds.length;
        OADataSource dsFound = null;
        for (int i=0; ds != null &&  i<x; i++) {
            if (ds[i] != null && ds[i].bEnable && ds[i].isClassSupported(clazz, filter)) {
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
    @Override
    public void setEnabled(boolean b) {
    	this.bEnable = b;
    }
    @Override
    public boolean getEnabled() {
    	return this.bEnable;
    }
    
    /**
        Used to retrieve a single object from DataSource.
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
        Used to retrieve a single object from DataSource.
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
        return ds.getObject(oi, clazz, key, false);
    }

    @Override
    public Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key, boolean bDirty) {
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
        OADataSourceIterator it = ds.select(clazz, query, key.getObjectIds(), "", bDirty);
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
    @Override
    public void setAssignIdOnCreate(boolean b) {
        bAssignNumberOnCreate = b;
    }
    /**
        Used to know if autonumber properties should be assigned on create or on save.
        @see #setAssignNumberOnCreate
    */
    @Override
    public boolean getAssignIdOnCreate() {
        return bAssignNumberOnCreate;
    }

    /**
        Used to know is datasoure is currently available.
    */
    @Override
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
    @Override
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
            synchronized(vecDataSource) {
                dsAll = null;
                vecDataSource.addElement(this);
                dataSourceChangeCnter++;
            }
        }
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
        synchronized(vecDataSource) {
            dataSourceChangeCnter++;
            while (vecDataSource.size() > 0) {
                ((OADataSource) vecDataSource.elementAt(0)).close();
            }
            vecDataSource.removeAllElements();
            dsAll = null;
        }
    }

    /**
        Close this DataSource.
    */
    @Override
    public void close() {
        removeFromList();
    }
    public void removeFromList() {
        synchronized(vecDataSource) {
            vecDataSource.removeElement(this);
            dataSourceChangeCnter++;
            dsAll = null;
        }
    }

    /**
     * This can be called after a close has been done to make the datasoruce available again.
     * @param pos search location in list of datasources.
     */
    @Override
    public void reopen(int pos) {
        synchronized(vecDataSource) {
            if (!vecDataSource.contains(this)) {
                int x = vecDataSource.size();
                pos = Math.max(0, Math.min(x, pos));
                vecDataSource.insertElementAt(this, pos);
                dataSourceChangeCnter++;
                dsAll = null;
            }
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
        synchronized(vecDataSource) {
    	    if (pos < 0) pos = 0;
    	    int x = vecDataSource.indexOf(this);
    	    if (x < 0) return;
    	    if (x == pos) return;
    	    dataSourceChangeCnter++;
    	    vecDataSource.removeElementAt(x);
    	    x = vecDataSource.size();
    	    if (pos > x) pos = x;
    	    vecDataSource.insertElementAt(this, pos);
    	    dsAll = null;
        }
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
        @param clazz class
        @param filter used to query the datasource
    */
    @Override
    public abstract boolean isClassSupported(Class clazz, OAFilter filter);
    
    @Override
    public boolean isClassSupported(Class clazz) {
        return isClassSupported(clazz, null); 
    }
    
    /**
        Used by dataSources to update special requirements for handling Many2Many relationships (ex:Link Table).
        <p>
        Uses the hub.masterObject, Hub.getRemovedObjects(), Hub.getAddedObjects()
        to find out which objects were added or removed.
        <br>
        This is called by OAObject.cascadeSave/Delete methods
     */
    @Override
	public abstract void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster);
    

    /**
        Add/Insert a new Object into DataSource.
        <p>
        Called directly by OAObject.save()
        @param bForce is true, then object should be inserted without verifying.
        @see OAObject#save
    */
    @Override
    public abstract void insert(OAObject obj);
    
    
    /**
	    Add/Insert a new Object into DataSource, without references (fkeys).
	    <p>
	    Called directly by OAObject.saveWithoutReferences() to save a reference while saving another Object.
	    @see OAObject#save
	*/
    @Override
    public abstract void insertWithoutReferences(OAObject obj);
    
    /**
        Update an existing Object to DataSource.
        <p>
        Called directly by OAObject.save()
        @param bForce is true, then object should be inserted without verifying.
        @see OAObject#save
    */
    @Override
    public abstract void update(OAObject obj, String[] includeProperties, String[] excludeProperties);
    @Override
    public void update(OAObject obj) {
        update(obj, null, null);
    }
    /**
        Remove an Object from a DataSource.
    */
    @Override
    public abstract void delete(OAObject obj);


    /**
        Used to save an object to DataSource.
        <p>
        If object is an OAObject, then update() or insert() will be called, else nothing is done.
        @see #insert
        @see #update
    */
    @Override
    public void save(OAObject obj) {
        // if it can be decided to use either insert() or update()
        if (obj == null) return;
        if (obj instanceof OAObject) {
            if ( ((OAObject)obj).getNew() ) {
                insert(obj);
            }
            else update(obj);
        }
    }


    /**
        Perform a count on the DataSource using a query.
        @param clazz Class to perform query on
        @param queryWhere query using property paths based on Object structure.
        @see OASelect
    */
    @Override
    public abstract int count(Class selectClass, 
        String queryWhere, Object[] params,   
        OAObject whereObject, String propertyFromWhereObject, String extraWhere, int max
    );
    public int count(Class selectClass, String queryWhere, int max) {
        int x = count(selectClass,
            queryWhere, null,
            null, null, null, max);
        if (max < 1) return x;
        return Math.min(x, max);
    }
    public int count(Class selectClass, String queryWhere) {
    	return count(selectClass, queryWhere, 0);
    }
	public int count(Class selectClass, String queryWhere, Object[] params, int max) {
	    int x = count(selectClass,
	        queryWhere, params,
	        null, null, null, max);
        if (max < 1) return x;
        return Math.min(x, max);
	}
	public int count(Class selectClass, String queryWhere, Object param, int max) {
        int x = count(selectClass,
                queryWhere, param==null?null:(new Object[] {param}),
                null, null, null, max);
            if (max < 1) return x;
            return Math.min(x, max);
	}
    public int count(Class selectClass, String queryWhere, Object[] params) {
        return count(selectClass,
            queryWhere, params,
            null, null, null, 0);
    }
	public int count(Class selectClass, String queryWhere, Object param) {
        return count(selectClass,
            queryWhere, param==null?null:(new Object[] {param}),
            null, null, null, 0);
	}
    public int count(Class selectClass, OAObject whereObject, String propertyNameFromWhereObject, int max) {
        int x = count(selectClass,
                null, null,
                whereObject, propertyNameFromWhereObject, null, max);
            if (max < 1) return x;
            return Math.min(x, max);
    }
    public int count(Class selectClass, OAObject whereObject, String propertyNameFromWhereObject) {
        return count(selectClass, whereObject, propertyNameFromWhereObject, 0);
    }
    public int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] params, String propertyNameFromWhereObject, int max) {
        int x = count(selectClass,
            null, params,
            whereObject, propertyNameFromWhereObject, extraWhere, max);
        if (max < 1) return x;
        return Math.min(x, max);
    }
    public int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] params, String propertyNameFromWhereObject) {
        return count(selectClass, whereObject, extraWhere, params, propertyNameFromWhereObject, 0);
    }

    
    /**
        Performs a count using native query language for DataSource.
        @param query query based on DataSource structure.
        @see OASelect
    */
	@Override
    public abstract int countPassthru(Class selectClass, 
        String queryWhere, int max  
    );
    public int countPassthru(String queryWhere, int max) {
        int x = countPassthru(null, queryWhere, max);
        if (max < 1) return x;
        return Math.min(x, max);
    }
    public int countPassthru(String queryWhere) {
        return countPassthru(queryWhere, 0);
    }


    /**
        Returns true if this dataSource supports selecting/storing/deleting.
    */
    @Override
    public abstract boolean supportsStorage();


    /**
        Perform a query to retrieve objects from DataSource.
        <p>
        See OASelect for complete description on selects/queriess.
        @param selectClass Class of object to create and return
        @param queryWhere query String using property paths based on Object structure.  DataSource
        @param params list of values to replace '?' in queryWhere clause.
        @param filter the datasource filter, used if the ds does not support queries (ex: sql)
        will convert query to native query language of the datasoure.
        @return OADataSourceIterator that is used to return objects of type selectClass
        @see OASelect
     */
    @Override
    public abstract OADataSourceIterator select(Class selectClass, 
        String queryWhere, Object[] params, String queryOrder, 
        OAObject whereObject, String propertyFromWhereObject, String extraWhere, 
        int max, OAFilter filter, boolean bDirty
    );

    public OADataSourceIterator select(Class selectClass) {
        return select(selectClass, 
                (String) null, (Object[]) null, (String) null, 
                (OAObject) null, (String) null, (String) null,
                0, (OAFilter) null, false);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere) {
        return select(selectClass, 
                queryWhere, (Object[]) null, (String) null, 
                (OAObject) null, (String) null, (String) null,
                0, (OAFilter) null, false);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, String orderBy) {
        return select(selectClass, 
                queryWhere, (Object[]) null, orderBy, 
                (OAObject) null, (String) null, (String) null,
                0, (OAFilter) null, false);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return select(selectClass, 
                queryWhere, null, queryOrder, 
                null, null, null,
                max, filter, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, String queryOrder, int max, boolean bDirty) {
        return select(selectClass, 
                queryWhere, null, queryOrder, 
                null, null, null,
                max, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, String queryOrder, boolean bDirty) {
        return select(selectClass, 
                queryWhere, null, queryOrder, 
                null, null, null,
                0, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, int max, boolean bDirty) {
        return select(selectClass, 
                queryWhere, params, queryOrder, 
                null, null, null,
                max, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, boolean bDirty) {
        return select(selectClass, 
                queryWhere, params, queryOrder, 
                null, null, null,
                0, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return select(selectClass, 
                queryWhere, params, queryOrder, 
                null, null, null,
                max, filter, bDirty);
    }
	public OADataSourceIterator select(Class selectClass, String queryWhere, Object param, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return select(selectClass, 
                queryWhere, param==null?null:(new Object[] {param}), queryOrder, 
                null, null, null,
                max, filter, bDirty);
	}
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object param, String queryOrder, int max, boolean bDirty) {
        return select(selectClass, queryWhere, param, queryOrder, max, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, String queryWhere, Object param, String queryOrder, boolean bDirty) {
		return select(selectClass, queryWhere, param, queryOrder, 0, null, bDirty);
	}
    public OADataSourceIterator select(Class selectClass, 
            OAObject whereObject, String extraWhere, Object[] args, 
            String propertyNameFromWhereObject, String queryOrder, 
            int max, OAFilter filter, boolean bDirty) {
        return select(selectClass, 
            null, args, queryOrder,
            whereObject, propertyNameFromWhereObject, extraWhere,
            max, filter, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromWhereObject, String queryOrder, int max, boolean bDirty) {
        return select(selectClass, whereObject, extraWhere, args, propertyNameFromWhereObject, queryOrder, max, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromWhereObject, String queryOrder, boolean bDirty) {
        return select(selectClass, whereObject, extraWhere, args, propertyNameFromWhereObject, queryOrder, 0, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, OAObject whereObject, String propertyNameFromWhereObject, String queryOrder, int max, OAFilter filter, boolean bDirty) {
        return select(selectClass, 
            null, null, queryOrder,
            whereObject, propertyNameFromWhereObject, null,
            max, filter, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, OAObject whereObject, String propertyNameFromWhereObject, String queryOrder, int max, boolean bDirty) {
        return select(selectClass, whereObject, propertyNameFromWhereObject, queryOrder, max, null, bDirty);
    }
    public OADataSourceIterator select(Class selectClass, OAObject whereObject, String propertyNameFromWhereObject, String queryOrder, boolean bDirty) {
        return select(selectClass, whereObject, propertyNameFromWhereObject, queryOrder, 0, null, bDirty);
    }
    
    
    // hasNext(), next(), remove() (used to close)

    /**
        Performs a select using native query language for DataSource.
        @param selectClass Class of object to create and return
        @param query query based on DataSource structure.
        @see OASelect
        @return OADataSourceIterator that is used to return objects of type selectClass
    */
    public abstract OADataSourceIterator selectPassthru(Class selectClass, 
        String queryWhere, String queryOrder, 
        int max, OAFilter filter, boolean bDirty
    );
    public OADataSourceIterator selectPassthru(Class selectClass, String query, int max, OAFilter filter, boolean bDirty) {
        return selectPassthru(selectClass,
            query, null,
            max, filter, bDirty
        );
    }
    public OADataSourceIterator selectPassthru(Class selectClass, String query, int max, boolean bDirty) {
        return selectPassthru(selectClass,
            query, null,
            max, null, bDirty
        );
    }
    public OADataSourceIterator selectPassthru(Class selectClass, String query, boolean bDirty) {
        return selectPassthru(selectClass,
            query, null,
            0, null, bDirty
        );
    }
    public OADataSourceIterator selectPassthru(Class selectClass, String query, String queryOrder, int max, boolean bDirty) {
        return selectPassthru(selectClass,
            query, queryOrder,
            max, null, bDirty
        );
    }
    public OADataSourceIterator selectPassthru(Class selectClass, String query, String queryOrder, boolean bDirty) {
        return selectPassthru(selectClass,
            query, queryOrder,
            0, null, bDirty
        );
    }


    /**
        Execute a command on the dataSource.
        @param command DataSource native command.
    */
    @Override
    public abstract Object execute(String command);


    /**
        Called by OAObject to initialize a new Object.
    */
    @Override
    public abstract void assignId(OAObject obj);

    
    /**
        Returns true if the dataSource will set the property value before saving.
    */
    @Override
    public boolean willCreatePropertyValue(OAObject object, String propertyName) {
        return false;
    }

    /**
        Defaults to return true, allowing object Id properties to be changed.  Most DataSources that use foreign keys
        for references will not allow the object id to be changed after the object has been saved.
        @see OADataSourceJDBC#getAllowIdChange
    */
    @Override
    public boolean getAllowIdChange() {
        return true;
    }

    /**
     * Select BLOB (large byte[]) property 
     */
    @Override
    public abstract byte[] getPropertyBlobValue(OAObject obj, String propertyName);

    /**
     * Can this datasource get a count of the objects that will be selected.
     */
    @Override
    public boolean getSupportsPreCount() {
        return true;
    }

}


