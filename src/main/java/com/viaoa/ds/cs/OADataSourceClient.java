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
package com.viaoa.ds.cs;

import java.util.*;
import com.viaoa.object.*;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.util.OAFilter;
import com.viaoa.ds.*;
import com.viaoa.ds.objectcache.ObjectCacheIterator;


/**
    Uses OAClient to have all methods invoked on the OADataSource on OAServer.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OADataSourceClient extends OADataSource {
    private Hashtable hashClass = new Hashtable();
    private RemoteClientInterface remoteClientSync;

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
    public static final int INITIALIZEOBJECT = 24; 
    /** internal value to work with OAClient */
    public static final int SUPPORTSINITIALIZEOBJECT = 25; 

    public static final int GET_PROPERTY = 26;
    
    /**
        Create new OADataSourceClient that uses OAClient to communicate with OADataSource on OAServer.
    */
    public OADataSourceClient() {
    }

    public RemoteClientInterface getRemoteClient() {
        if (remoteClientSync == null) {
            remoteClientSync = OASyncDelegate.getRemoteClient();
        }
        return remoteClientSync;
    }

    public void setAssignNumberOnCreate(boolean b) {
    }
    public boolean getAssignNumberOnCreate() {
        verifyConnection();
        Object obj = getRemoteClient().datasource(ASSIGNNUMBERONCREATE, new Object[] {});
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    public boolean isAvailable() {
        verifyConnection();
        Object obj = getRemoteClient().datasource(IS_AVAILABLE, null);
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
        Object obj = getRemoteClient().datasource(MAX_LENGTH, new Object[] {c, propertyName});
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
        if (getRemoteClient() == null) {
            throw new RuntimeException("OADataSourceClient connection is not set");
        }
    }

    //NOTE: this needs to see if any of "clazz" superclasses are supported
    public boolean isClassSupported(Class clazz, OAFilter filter) {
        if (clazz == null) return false;
        
        Boolean B = (Boolean) hashClass.get(clazz);
        if (B != null) return B.booleanValue();

        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(clazz) != null) return true;
        }
        
        verifyConnection();
        Object obj = getRemoteClient().datasource(IS_CLASS_SUPPORTED, new Object[] {clazz});
        boolean b = false;
        if (obj instanceof Boolean) b = ((Boolean)obj).booleanValue();

        hashClass.put(clazz, new Boolean(b));
        return b;
    }

    public void insertWithoutReferences(OAObject obj) {
        if (obj == null) return;
        getRemoteClient().datasource(INSERT_WO_REFERENCES, new Object[] { obj });
    }
    
    
    public void insert(OAObject obj) {
        if (obj == null) return;
        getRemoteClient().datasource(INSERT, new Object[] { obj });
    }

    public @Override void update(OAObject obj, String[] includeProperties, String[] excludeProperties) {
        if (obj == null) return;
        getRemoteClient().datasource(UPDATE, new Object[] { obj, includeProperties, excludeProperties});
    }

    public @Override void save(OAObject obj) {
        if (obj == null) return;
        getRemoteClient().datasource(SAVE, new Object[] { obj });
    }

    public @Override void delete(OAObject obj) {
        if (obj == null) return;
        getRemoteClient().datasource(DELETE, new Object[] { obj });
    }

    
    @Override
    public int count(Class selectClass, 
        String queryWhere, Object[] params,   
        OAObject whereObject, String propertyFromMaster, String extraWhere, int max
    ) 
    {
        Class whereClass = null;
        OAObjectKey whereKey = null;
        if (whereObject != null) {
            whereClass = whereObject.getClass();
            whereKey = OAObjectKeyDelegate.getKey(whereObject);
        }
        
        Object[] objs = new Object[] {selectClass, queryWhere, params, whereClass, whereKey, propertyFromMaster, extraWhere, max};
        
        Object obj = getRemoteClient().datasource(COUNT, objs);
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }
    
    @Override
    public int countPassthru(Class selectClass, String queryWhere, int max) {
        Object obj = getRemoteClient().datasource(COUNTPASSTHRU, new Object[] {selectClass, queryWhere, max});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }


    /** does this dataSource support selecting/storing/deleting  */
    public @Override boolean supportsStorage() {
        Object obj = getRemoteClient().datasource(SUPPORTSSTORAGE, null);
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }


    @Override
    public Iterator select(Class selectClass, 
        String queryWhere, Object[] params, String queryOrder, 
        OAObject whereObject, String propertyFromMaster, String extraWhere, 
        int max, OAFilter filter, boolean bDirty
    )
    {
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(selectClass) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(selectClass, filter);
                it.setMax(max);
                return it;
            }
        }

        Class whereClass = null;
        OAObjectKey whereKey = null;
        if (whereObject != null) {
            whereClass = whereObject.getClass();
            whereKey = OAObjectKeyDelegate.getKey(whereObject);
        }
        
        Object[] objs = new Object[] {
            selectClass, 
            queryWhere, params, queryOrder, 
            whereClass, whereKey,
            propertyFromMaster, extraWhere,
            max, bDirty
        };
        
        Object obj = getRemoteClient().datasource(SELECT, objs);
        if (obj == null) return null;
        // dont send the filter to the server, it could serialize extra data, etc.
        return new MyIterator(selectClass, obj, filter);
    }


    @Override
    public Iterator selectPassthru(Class selectClass, 
        String queryWhere, String queryOrder, 
        int max, OAFilter filter, boolean bDirty
    )
    {
        if (filter != null) {
            if (OAObjectCacheDelegate.getSelectAllHub(selectClass) != null) {
                ObjectCacheIterator it = new ObjectCacheIterator(selectClass, filter);
                it.setMax(max);
                return it;
            }
        }
        Object obj = getRemoteClient().datasource(SELECTPASSTHRU, new Object[] {selectClass, queryWhere, queryOrder, max, bDirty} );
        if (obj == null) return null;
        return new MyIterator(selectClass, obj, filter);
    }



    public @Override Object execute(String command) {
        return getRemoteClient().datasource(EXECUTE, new Object[] {command});
    }


    public @Override void initializeObject(OAObject obj) {
        if (bSupportsInitFlag && !bSupportsInit) return;
        verifyConnection();
        if (!bSupportsInitFlag) { 
            initSupportsInitializeObject(obj.getClass());
            if (bSupportsInitFlag && !bSupportsInit) return;
        }
        getRemoteClient().datasource(INITIALIZEOBJECT, new Object[] {obj} );  // NOTE WAS: dont use, this calls server.  ObjectId could be changed on server and never be found when returned
    }

    public @Override boolean willCreatePropertyValue(OAObject object, String propertyName) {
        Object obj = getRemoteClient().datasource(WILLCREATEPROPERTYVALUE, new Object[] {object, propertyName} );
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
            cachePos = 0;
            cache = (Object[]) getRemoteClient().datasource(IT_NEXT, new Object[] {id} );
            if (cache != null && cache.length == 0) cache = null;
            if (cache == null) return;
            for (Object obj : cache) {
                if (obj == null) break;
                // the server will add the object to the session cache (server side) if it is not in a hub w/master 
                if (!OAObjectHubDelegate.isInHubWithMaster((OAObject) obj)) {
                    OAObjectCSDelegate.addToServerSideCache((OAObject)obj, false);
                }
            }
        }

        public synchronized Object next() {
            if (!hasNext()) return null;
            Object obj = null;
            if (key != null) {
                obj = OAObjectCacheDelegate.get(clazz, key);
                if (obj == null) {
                    // not on this system, need to get from server
                    OASyncDelegate.getRemoteServer().getObject(clazz, key);
                }
                bKey = false;
                return obj;
            }
            obj = cache[cachePos++];
            
            return obj;
        }

        public void remove() {
            getRemoteClient().datasource(IT_REMOVE, new Object[] {id} );
        }
    }

	public @Override void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
        getRemoteClient().datasource(UPDATE_MANY2MANY_LINKS, new Object[] { masterObject.getClass(), OAObjectKeyDelegate.getKey(masterObject), adds, removes, propertyNameFromMaster });
	}
	
	@Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        Object objx = getRemoteClient().datasource(GET_PROPERTY, new Object[] { obj.getClass(), OAObjectKeyDelegate.getKey(obj), propertyName });
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
        Object objx = getRemoteClient().datasource(SUPPORTSINITIALIZEOBJECT, new Object[] { clazz });
        if (objx instanceof Boolean) bSupportsInit = ((Boolean) objx).booleanValue();
    }


}

