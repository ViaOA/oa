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
package com.viaoa.ds.cs;

import java.util.*;

import com.viaoa.object.*;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteClientSyncInterface;
import com.viaoa.util.OAFilter;
import com.viaoa.ds.*;
import com.viaoa.ds.jdbc.db.Database;
import com.viaoa.ds.jdbc.delegate.Delegate;
import com.viaoa.ds.objectcache.ObjectCacheIterator;


/**
    Uses OAClient to have all methods sent to OADataSource on OAServer.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OADataSourceClient extends OADataSource {
    private Hashtable hashClass = new Hashtable();
    private RemoteClientSyncInterface remoteClientSync;

    /** internal value to work with OAClient */
    public static final int IS_AVAILABLE = 0;
    /** internal value to work with OAClient */
    public static final int MAX_LENGTH = 1;
    /** internal value to work with OAClient */
    public static final int IS_CLASS_SUPPORTED = 2;

    public static final int INSERT_WO_REFERENCES = 3;
    public static final int UPDATE_MANY2MANY_LINKS = 4;

    
    /** internal value to work with OAClient */
    public static final int INSERT = 5;
    /** internal value to work with OAClient */
    public static final int UPDATE = 6;
    /** internal value to work with OAClient */
    public static final int DELETE = 7;
    /** internal value to work with OAClient */
    public static final int SAVE = 8;
    /** internal value to work with OAClient */
    public static final int ASSIGNNUMBERONCREATE = 9;
    /** internal value to work with OAClient */
    public static final int COUNT = 10;
    /** internal value to work with OAClient */
    public static final int COUNTPASSTHRU = 11;
    /** internal value to work with OAClient */
    public static final int COUNT2 = 12;
    /** internal value to work with OAClient */
    public static final int SUPPORTSSTORAGE = 13;

    
    //public static final int CONVERTTOSTRING = 14;

    //public static final int CONVERTTOSTRING2 = 15;
    
    /** internal value to work with OAClient */
    public static final int EXECUTE = 16;
    /** internal value to work with OAClient */
    public static final int WILLCREATEPROPERTYVALUE = 17;
    /** internal value to work with OAClient */
    public static final int IT_HASNEXT = 18;
    /** internal value to work with OAClient */
    public static final int IT_NEXT = 19;
    /** internal value to work with OAClient */
    public static final int IT_REMOVE = 20;
    /** internal value to work with OAClient */
    public static final int SELECT = 21;
    /** internal value to work with OAClient */
    public static final int SELECTPASSTHRU = 22;
    /** internal value to work with OAClient */
    public static final int SELECTUSINGOBJECT = 23;
    /** internal value to work with OAClient */
    public static final int INITIALIZEOBJECT = 24; 
    /** internal value to work with OAClient */
    public static final int SUPPORTSINITIALIZEOBJECT = 25; 

    public static final int GET_PROPERTY = 26;
    
    /**
        Create new OADataSourceClient that uses OAClient to communicate with OADataSource on OAServer.
    */
    public OADataSourceClient() {
    }

    public RemoteClientSyncInterface getRemoteClientSync() {
        if (remoteClientSync == null) {
            remoteClientSync = OASyncDelegate.getRemoteClientSyncInterface();
        }
        return remoteClientSync;
    }

    public void setAssignNumberOnCreate(boolean b) {
    }
    public boolean getAssignNumberOnCreate() {
        verifyConnection();
        Object obj = getRemoteClientSync().datasource(ASSIGNNUMBERONCREATE, new Object[] {});
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    public boolean isAvailable() {
        verifyConnection();
        Object obj = getRemoteClientSync().datasource(IS_AVAILABLE, null);
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    private HashMap<String, Integer> hmMax = new HashMap<String, Integer>();
    public int getMaxLength(Class c, String propertyName) {
        String key = (c.getName() + "-" + propertyName).toUpperCase();
        Object objx = hmMax.get(key);
        if (objx != null) {
            return ((Integer) objx).intValue();
        }
        
        int iResult;
        verifyConnection();
        Object obj = getRemoteClientSync().datasource(MAX_LENGTH, new Object[] {c, propertyName});
        if (obj instanceof Integer) iResult  = ((Integer)obj).intValue();
        else iResult = -1;
        hmMax.put(key, iResult);
        return iResult;
    }
    
    public void setMaxLength(Class c, String propertyName, int length) {
        if (c != null ||propertyName == null) return;
        String key = (c.getName() + "-" + propertyName).toUpperCase();
        hmMax.put(key, new Integer(length));
    }

    protected void verifyConnection() {
        if (getRemoteClientSync() == null) {
            throw new RuntimeException("connection remote client datasoruce is not set");
        }
    }

    //NOTE: this needs to see if any of "clazz" superclasses are supported
    public boolean isClassSupported(Class clazz) {
        if (clazz == null) return false;
        
        Boolean B = (Boolean) hashClass.get(clazz);
        if (B != null) return B.booleanValue();

        verifyConnection();
        Object obj = getRemoteClientSync().datasource(IS_CLASS_SUPPORTED, new Object[] {clazz});
        boolean b = false;
        if (obj instanceof Boolean) b = ((Boolean)obj).booleanValue();

        hashClass.put(clazz, new Boolean(b));
        return b;
    }

    public void insertWithoutReferences(OAObject obj) {
        if (obj == null) return;
        getRemoteClientSync().datasource(INSERT_WO_REFERENCES, new Object[] { obj });
    }
    
    
    public void insert(OAObject obj) {
        if (obj == null) return;
        getRemoteClientSync().datasource(INSERT, new Object[] { obj });
    }

    public @Override void update(OAObject obj, String[] includeProperties, String[] excludeProperties) {
        if (obj == null) return;
        getRemoteClientSync().datasource(UPDATE, new Object[] { obj, includeProperties, excludeProperties});
    }

    public @Override void save(OAObject obj) {
        if (obj == null) return;
        getRemoteClientSync().datasource(SAVE, new Object[] { obj });
    }

    public @Override void delete(OAObject obj) {
        if (obj == null) return;
        getRemoteClientSync().datasource(DELETE, new Object[] { obj });
    }

    public @Override int count(Class clazz, String queryWhere, int max) {
        Object obj = getRemoteClientSync().datasource(COUNT, new Object[] {clazz, queryWhere});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int count(Class clazz, String queryWhere, Object param, int max) {
    	return count(clazz, queryWhere, new Object[] {param});
    }
    public @Override int count(Class clazz, String queryWhere, Object[] params, int max) {
    	int x = params == null ? 0 : params.length;
    	Object[] objs = new Object[2+x];
    	objs[0] = clazz;
    	objs[1] = queryWhere;
    	for (int i=0; i<x; i++) objs[2+i] = params[i];
        Object obj = getRemoteClientSync().datasource(COUNT, objs);
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int countPassthru(String query, int max) {
        Object obj = getRemoteClientSync().datasource(COUNTPASSTHRU, new Object[] {query});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, int max) {
        Object obj = getRemoteClientSync().datasource(COUNT2, new Object[] {selectClass, extraWhere, args, whereObject, propertyNameFromMaster});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String propertyNameFromMaster, int max) {
        Object obj = getRemoteClientSync().datasource(COUNT2, new Object[] {selectClass, null, whereObject, propertyNameFromMaster});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    /** does this dataSource support selecting/storing/deleting  */
    public @Override boolean supportsStorage() {
        Object obj = getRemoteClientSync().datasource(SUPPORTSSTORAGE, null);
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }


    public @Override Iterator select(Class clazz, String queryWhere, String queryOrder, int max, OAFilter filter) {
        // 20140125
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(clazz, filter);
                return it;
            }
        }
        Object obj = getRemoteClientSync().datasource(SELECT, new Object[] {clazz, queryWhere, queryOrder, filter} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj, filter);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object param, String queryOrder, int max, OAFilter filter) {
    	return this.select(clazz, queryWhere, new Object[] {param}, queryOrder, 0, filter);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object[] params, String queryOrder, int max, OAFilter filter) {
        // 20140125
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(clazz, filter);
                return it;
            }
        }
    	int x = params == null ? 0 : params.length;
    	Object[] objs = new Object[3+x];
    	objs[0] = clazz;
    	objs[1] = queryWhere;
    	objs[2] = queryOrder;
    	
    	for (int i=0; i<x; i++) objs[3+i] = params[i];
    	
    	Object obj = getRemoteClientSync().datasource(SELECT, objs );
        if (obj == null) return null;
        return new MyIterator(clazz, obj, filter);
    }

    public @Override Iterator selectPassthru(Class clazz, String query, int max, OAFilter filter) {
        // 20140125
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(clazz, filter);
                return it;
            }
        }
        Object obj = getRemoteClientSync().datasource(SELECTPASSTHRU, new Object[] {clazz, query, null} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj, filter);
    }


    public @Override Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder,int max, OAFilter filter) {
        // 20140125
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(clazz, filter);
                return it;
            }
        }
        Object obj = getRemoteClientSync().datasource(SELECTPASSTHRU, new Object[] {clazz, queryWhere, queryOrder} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj, filter);
    }

    public @Override Object execute(String command) {
        return getRemoteClientSync().datasource(EXECUTE, new Object[] {command});
    }

    public @Override Iterator select(Class clazz, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max, OAFilter filter) {
        // See if OAObjectKey exists in Object to do a lookup
        if (whereObject instanceof OAObject) {
            Object obj = ((OAObject)whereObject).getProperty("OA_"+propertyNameFromMaster.toUpperCase());
            if (obj instanceof OAObjectKey) {
                return new MyIterator((OAObjectKey)obj);
            }
        }
        // 20140125
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(clazz, filter);
                return it;
            }
        }

        Class whereClass = whereObject == null ? null : whereObject.getClass();
        Object key = OAObjectKeyDelegate.getKey(whereObject);;
        Object obj = getRemoteClientSync().datasource(SELECTUSINGOBJECT, new Object[] {clazz, whereClass, key, extraWhere, args, propertyNameFromMaster, queryOrder} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj, filter);
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder, int max, OAFilter filter) {
        return select(selectClass, whereObject, null, null, propertyNameFromMaster, queryOrder, 0, filter);
    }

    public @Override void initializeObject(OAObject obj) {
        if (bSupportsInitFlag && !bSupportsInit) return;
        verifyConnection();
        if (!bSupportsInitFlag) { 
            initSupportsInitializeObject(obj.getClass());
            if (bSupportsInitFlag && !bSupportsInit) return;
        }
        getRemoteClientSync().datasource(INITIALIZEOBJECT, new Object[] {obj} );  // NOTE WAS: dont use, this calls server.  ObjectId could be changed on server and never be found when returned
    }

    public @Override boolean willCreatePropertyValue(OAObject object, String propertyName) {
        Object obj = getRemoteClientSync().datasource(WILLCREATEPROPERTYVALUE, new Object[] {object, propertyName} );
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    /**
        Iterator Class that is used by select methods, works directly with OADataSource on OAServer.
    */
    class MyIterator implements Iterator {
        Object id;
        Class clazz;
        OAObjectKey key; // object to return
        boolean bKey;
        Object[] cache;
        int cachePos = 0;
        OAFilter filter;

        public MyIterator(Class c, Object id, OAFilter filter) {
            this.clazz = c;
            this.id = id;
            this.filter = filter;
            next20();
        }
        public MyIterator(OAObjectKey key) {
            this.key = key;
            this.bKey = true;
        }

        public synchronized boolean hasNext() {
            if (key != null) return (bKey);
            
            for (;;) {
                if (cache == null) break;
                for ( ; cachePos < cache.length; cachePos++) {
                    if (cache[cachePos] == null) return false;
                    if (filter == null || filter.isUsed(cache[cachePos])) return true;
                }
                next20();
            }
            
            return false;
        }

        protected synchronized void next20() {
            cache = (Object[]) getRemoteClientSync().datasource(IT_NEXT, new Object[] {id} );
            cachePos = 0;
        }

        public synchronized Object next() {
            if (!hasNext()) return null;
            Object obj = null;
            if (key != null) {
                obj = OAObjectCacheDelegate.get(clazz, key);
                if (obj == null) {
                    // not on this system, need to get from server
                    OASyncDelegate.getRemoteServerInterface().getObject(clazz, key);
                }
                bKey = false;
                return obj;
            }

            obj = cache[cachePos++];
            return obj;
        }

        public void remove() {
            getRemoteClientSync().datasource(IT_REMOVE, new Object[] {id} );
        }
    }

	public @Override void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
        getRemoteClientSync().datasource(UPDATE_MANY2MANY_LINKS, new Object[] { masterObject.getClass(), OAObjectKeyDelegate.getKey(masterObject), adds, removes, propertyNameFromMaster });
	}
	
	@Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        Object objx = getRemoteClientSync().datasource(GET_PROPERTY, new Object[] { obj.getClass(), OAObjectKeyDelegate.getKey(obj), propertyName });
        if (objx instanceof byte[]) return (byte[]) objx;
        return null;
    }
	
	private boolean bSupportsInit;
    private boolean bSupportsInitFlag;
	
	@Override
	public boolean supportsInitializeObject() {
        return bSupportsInit;
	}
    protected void initSupportsInitializeObject(Class clazz) {
        if (bSupportsInitFlag) return;
        bSupportsInitFlag = true;
        bSupportsInit = true;
        Object objx = getRemoteClientSync().datasource(SUPPORTSINITIALIZEOBJECT, new Object[] { clazz });
        if (objx instanceof Boolean) bSupportsInit = ((Boolean) objx).booleanValue();
    }
}

