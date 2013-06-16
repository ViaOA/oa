package com.viaoa.ds.cs;

import java.util.*;
import java.io.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.ds.*;


/**
    Uses OAClient to have all methods sent to OADataSource on OAServer.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OADataSourceClient extends OADataSource {
    protected RemoteClientInterface client;
    private Hashtable hashClass = new Hashtable();

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

    public static final int GET_PROPERTY = 25;
    
    /**
        Create new OADataSourceClient that uses OAClient to communicate with OADataSource on OAServer.
    */
    public OADataSourceClient(RemoteClientInterface client) {
        this.client = client;
    }

    /**
        Create new OADataSourceClient that uses OAClient to communicate with OADataSource on OAServer.
        Automatically sets OAClient calling OAClient.getClient()
    */
    public OADataSourceClient() {
        this(OASyncDelegate.getRemoteClientInterface());
    }

    /**
        Set OAClient that is used to communicate to OAServer's OADataSource.
    */
    public void setClient(RemoteClientInterface client) {
        this.client = client;
    }

    public void setAssignNumberOnCreate(boolean b) {
    }
    public boolean getAssignNumberOnCreate() {
        Object obj = client.datasource(ASSIGNNUMBERONCREATE, new Object[] {});
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    public boolean isAvailable() {
        Object obj = client.datasource(IS_AVAILABLE, null);
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }

    public int getMaxLength(Class c, String propertyName) {
        Object obj = client.datasource(MAX_LENGTH, new Object[] {c, propertyName});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }


    //NOTE: this needs to see if any of "clazz" superclasses are supported
    public boolean isClassSupported(Class clazz) {
        if (clazz == null) return false;

        Boolean B = (Boolean) hashClass.get(clazz);
        if (B != null) return B.booleanValue();

        Object obj = client.datasource(IS_CLASS_SUPPORTED, new Object[] {clazz});
        boolean b = false;
        if (obj instanceof Boolean) b = ((Boolean)obj).booleanValue();

        hashClass.put(clazz, new Boolean(b));
        return b;
    }

    public void insertWithoutReferences(OAObject obj) {
        if (obj == null) return;
        client.datasource(INSERT_WO_REFERENCES, new Object[] { obj });
    }
    
    
    public void insert(OAObject obj) {
        if (obj == null) return;
        client.datasource(INSERT, new Object[] { obj });
    }

    public @Override void update(OAObject obj, String[] includeProperties, String[] excludeProperties) {
        if (obj == null) return;
        client.datasource(UPDATE, new Object[] { obj, includeProperties, excludeProperties});
    }

    public @Override void save(OAObject obj) {
        if (obj == null) return;
        client.datasource(SAVE, new Object[] { obj });
    }

    public @Override void delete(OAObject obj) {
        if (obj == null) return;
        client.datasource(DELETE, new Object[] { obj });
    }

    public @Override int count(Class clazz, String queryWhere, int max) {
        Object obj = client.datasource(COUNT, new Object[] {clazz, queryWhere});
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
        Object obj = client.datasource(COUNT, objs);
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int countPassthru(String query, int max) {
        Object obj = client.datasource(COUNTPASSTHRU, new Object[] {query});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, int max) {
        Object obj = client.datasource(COUNT2, new Object[] {selectClass, extraWhere, args, whereObject, propertyNameFromMaster});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    public @Override int count(Class selectClass, OAObject whereObject, String propertyNameFromMaster, int max) {
        Object obj = client.datasource(COUNT2, new Object[] {selectClass, null, whereObject, propertyNameFromMaster});
        if (obj instanceof Integer) return ((Integer)obj).intValue();
        return -1;
    }

    /** does this dataSource support selecting/storing/deleting  */
    public @Override boolean supportsStorage() {
        Object obj = client.datasource(SUPPORTSSTORAGE, null);
        if (obj instanceof Boolean) return ((Boolean)obj).booleanValue();
        return false;
    }


    public @Override Iterator select(Class clazz, String queryWhere, String queryOrder, int max) {
        Object obj = client.datasource(SELECT, new Object[] {clazz, queryWhere,queryOrder} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object param, String queryOrder, int max) {
    	return this.select(clazz, queryWhere, new Object[] {param}, queryOrder);
    }

    public @Override Iterator select(Class clazz, String queryWhere, Object[] params, String queryOrder, int max) {
    	int x = params == null ? 0 : params.length;
    	Object[] objs = new Object[3+x];
    	objs[0] = clazz;
    	objs[1] = queryWhere;
    	objs[2] = queryOrder;
    	for (int i=0; i<x; i++) objs[3+i] = params[i];
    	
    	Object obj = client.datasource(SELECT, objs );
        if (obj == null) return null;
        return new MyIterator(clazz, obj);
    }

    public @Override Iterator selectPassthru(Class clazz, String query, int max) {
        Object obj = client.datasource(SELECTPASSTHRU, new Object[] {clazz, query, null} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj);
    }


    public @Override Iterator selectPassthru(Class clazz, String queryWhere, String queryOrder,int max) {
        Object obj = client.datasource(SELECTPASSTHRU, new Object[] {clazz, queryWhere, queryOrder} );
        if (obj == null) return null;
        return new MyIterator(clazz, obj);
    }

    public @Override Object execute(String command) {
        return client.datasource(EXECUTE, new Object[] {command});
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String extraWhere, Object[] args, String propertyNameFromMaster, String queryOrder, int max) {
        // See if OAObjectKey exists in Object to do a lookup
        if (whereObject instanceof OAObject) {
            Object obj = ((OAObject)whereObject).getProperty("OA_"+propertyNameFromMaster.toUpperCase());
            if (obj instanceof OAObjectKey) {
                return new MyIterator((OAObjectKey)obj);
            }
        }

        Class whereClass = whereObject == null ? null : whereObject.getClass();
        Object key = OAObjectKeyDelegate.getKey(whereObject);;
        Object obj = client.datasource(SELECTUSINGOBJECT, new Object[] {selectClass, whereClass, key, extraWhere, args, propertyNameFromMaster, queryOrder} );
        if (obj == null) return null;
        return new MyIterator(selectClass, obj);
    }

    public @Override Iterator select(Class selectClass, OAObject whereObject, String propertyNameFromMaster, String queryOrder, int max) {
        return select(selectClass, whereObject, null, null, propertyNameFromMaster, queryOrder);
    }

    public @Override void initializeObject(OAObject obj) {
        client.datasource(INITIALIZEOBJECT, new Object[] {obj} );  // NOTE WAS: dont use, this calls server.  ObjectId could be changed on server and never be found when returned
    }

    public @Override boolean willCreatePropertyValue(OAObject object, String propertyName) {
        Object obj = client.datasource(WILLCREATEPROPERTYVALUE, new Object[] {object, propertyName} );
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

        public MyIterator(Class c, Object id) {
            this.clazz = c;
            this.id = id;
            next20();
        }
        public MyIterator(OAObjectKey key) {
            this.key = key;
            this.bKey = true;
        }

        public synchronized boolean hasNext() {
            if (key != null) return (bKey);
            if (cache == null) return false;
            return (cache[cachePos] != null);
        }

        protected synchronized void next20() {
            cache = (Object[]) client.datasource(IT_NEXT, new Object[] {id} );
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

            obj = cache[cachePos];
            if (obj == null) return null;

            cachePos++;
            if (cachePos == 20) next20();

            if (obj == null && hasNext()) return next();
            return obj;
        }

        public void remove() {
            client.datasource(IT_REMOVE, new Object[] {id} );
        }
    }

	public @Override void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propertyNameFromMaster) {
        client.datasource(UPDATE_MANY2MANY_LINKS, new Object[] { masterObject.getClass(), OAObjectKeyDelegate.getKey(masterObject), adds, removes, propertyNameFromMaster });
	}
	
	@Override
    public byte[] getPropertyBlobValue(OAObject obj, String propertyName) {
        Object objx = client.datasource(GET_PROPERTY, new Object[] { obj.getClass(), OAObjectKeyDelegate.getKey(obj), propertyName });
        if (objx instanceof byte[]) return (byte[]) objx;
        return null;
    }
}

