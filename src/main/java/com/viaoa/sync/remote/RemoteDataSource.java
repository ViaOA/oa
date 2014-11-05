package com.viaoa.sync.remote;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectHubDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;

/**
 * Used by OADataSourceClient to have a client DS methods to be executed on server.
 */
public abstract class RemoteDataSource {
    private static Logger LOG = Logger.getLogger(RemoteDataSource.class.getName());
    
    private ConcurrentHashMap<String, Iterator> hashIterator = new ConcurrentHashMap<String, Iterator>(); // used to store DB
    
    public Object datasource(int command, Object[] objects) {
        Object obj = null;
        Class clazz, masterClass;
        OADataSource ds;
        Object objKey;
        boolean b;
        int x;
        Object whereObject;
        String propFromMaster;

        switch (command) {
        case OADataSourceClient.IT_NEXT:
            obj = datasourceNext((String) objects[0]);
            break;
        case OADataSourceClient.IT_HASNEXT:
            obj = new Boolean(datasourceHasNext((String) objects[0]));
            break;
        case OADataSourceClient.IS_AVAILABLE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.isAvailable();
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.ASSIGNNUMBERONCREATE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.getAssignNumberOnCreate();
                obj = new Boolean(b);
            }
            else obj = Boolean.FALSE;
            break;
        case OADataSourceClient.MAX_LENGTH:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.getMaxLength(clazz, (String) objects[1]);
                //qqqqqqqqq                
                System.out.println("RemoteDataSourceImpl call to MAX_LENGTH when it should be on the client.");                
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.IS_CLASS_SUPPORTED:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            obj = new Boolean((ds != null));
            break;
        case OADataSourceClient.UPDATE_MANY2MANY_LINKS:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                whereObject = getObject(clazz, objects[1]);
                ds.updateMany2ManyLinks((OAObject) whereObject, (OAObject[]) objects[2], (OAObject[]) objects[3], (String) objects[4]);
            }
            break;

        case OADataSourceClient.INSERT:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.insert((OAObject) obj);
                obj = null;
            }
            break;

        case OADataSourceClient.UPDATE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.update((OAObject) obj, (String[]) objects[1], (String[]) objects[2]);
                obj = null;
            }
            break;

        case OADataSourceClient.SAVE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.save((OAObject) obj);
                obj = null;
            }
            break;

        case OADataSourceClient.DELETE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.delete((OAObject) obj);
                obj = null;
            }
            break;
        case OADataSourceClient.COUNT:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                int z = objects.length - 2;
                Object[] objs = new Object[z];
                for (int y = 0; y < z; y++) {
                    objs[y] = objects[2 + y];
                }
                x = ds.count(clazz, (String) objects[1], objs);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.COUNTPASSTHRU:
            ds = getDataSource();
            if (ds != null) {
                x = ds.countPassthru((String) objects[0]);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.COUNT2:
            clazz = (Class) objects[0];
            whereObject = getObject(clazz, objects[1]);
            String extraWhere = (String) objects[2];
            Object[] args = (Object[]) objects[3];
            propFromMaster = (String) objects[4];

            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.count(clazz, (OAObject) whereObject, extraWhere, args, propFromMaster);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.SUPPORTSSTORAGE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.supportsStorage();
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.EXECUTE:
            ds = getDataSource();
            if (ds != null) {
                return ds.execute((String) objects[0]);
            }
            break;
        case OADataSourceClient.IT_REMOVE:
            Iterator iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                iterator.remove();
                hashIterator.remove(objects[0]);
            }
            break;

        case OADataSourceClient.SELECT:
            int z = objects.length - 3;
            Object[] params = new Object[z];
            for (int y = 0; y < z; y++) {
                params[y] = objects[3 + y];
            }
            obj = datasourceSelect((Class) objects[0], (String) objects[1], params, (String) objects[2]);
            break;
        case OADataSourceClient.SELECTPASSTHRU:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.select(clazz, (String) objects[1], (String) objects[2], 0, null); // where, order
                obj = "select" + aiSelectCount.incrementAndGet();
                hashIterator.put((String) obj, iterator);
            }
            break;
        case OADataSourceClient.SUPPORTSINITIALIZEOBJECT: 
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) obj = new Boolean(ds.supportsInitializeObject());
            break;
        case OADataSourceClient.INITIALIZEOBJECT:
            clazz = (Class) objects[0].getClass();
            ds = getDataSource(clazz);
            if (ds != null) {
                OARemoteThreadDelegate.sendMessages(true);
                ds.initializeObject((OAObject) objects[0]);
                OARemoteThreadDelegate.sendMessages(false);
            }
            break;
        case OADataSourceClient.SELECTUSINGOBJECT:
            clazz = (Class) objects[0]; // class to select
            ds = getDataSource(clazz);
            if (ds != null) {
                masterClass = (Class) objects[1];
                whereObject = getObject(masterClass, objects[2]);
                extraWhere = (String) objects[3];
                args = (Object[]) objects[4];
                propFromMaster = (String) objects[5];
                
                iterator = ds.select(clazz, (OAObject) whereObject, extraWhere, args, propFromMaster, (String) objects[6]);
                obj = "select" + aiSelectCount.incrementAndGet();
                if (iterator != null) {
                    hashIterator.put((String) obj, iterator);
                }
            }
            break;
        case OADataSourceClient.INSERT_WO_REFERENCES:
            whereObject = objects[0];
            if (whereObject == null) break;
            clazz = whereObject.getClass();
            ds = getDataSource(clazz);
            if (ds != null) {
                OAObject oa = (OAObject) whereObject;
                ds.insertWithoutReferences((OAObject) oa);
                OAObjectDelegate.setNew(oa, false);
            }
            break;
        case OADataSourceClient.GET_PROPERTY:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                objKey = (OAObjectKey) objects[1];
                whereObject = getObject(clazz, objKey);
                String prop = (String) objects[2];
                obj = ds.getPropertyBlobValue((OAObject) whereObject, prop);
            }
            break;
        }
        return obj;
    }
    
    private OAObject getObject(Class objectClass, Object obj) {
        if (objectClass == null || obj == null) return null;
        if (obj instanceof OAObject) return (OAObject) obj;

        OAObjectKey key = OAObjectKeyDelegate.convertToObjectKey(objectClass, obj);

        OAObject objNew = OAObjectCacheDelegate.get(objectClass, key);
        if (objNew == null) {
            objNew = (OAObject) OADataSource.getObject(objectClass, key);
        }
        return objNew;
    }
    
    protected OADataSource getDataSource(Class c) {
        if (c != null) {
            OADataSource ds = OADataSource.getDataSource(c);
            if (ds != null) return ds;
        }
        if (defaultDataSource == null) {
            OADataSource[] dss = OADataSource.getDataSources();
            if (dss != null && dss.length > 0) defaultDataSource = dss[0];
        }
        return defaultDataSource;
    }
    private AtomicInteger aiSelectCount = new AtomicInteger();
    private OADataSource defaultDataSource;

    protected OADataSource getDataSource() {
        return getDataSource(null);
    }
    
    public abstract void setCached(OAObject obj);


    protected Object[] datasourceNext(String id) {
        Iterator iterator = (Iterator) hashIterator.get(id);
        if (iterator == null) return null;
        
        ArrayList<Object> al = new ArrayList();
        for (int i = 0; i < 20; i++) {
            if (!iterator.hasNext()) break;
            Object obj = iterator.next();
            al.add(obj);
            if (obj instanceof OAObject) {
                OAObject oa = (OAObject) obj;
                if (!OAObjectHubDelegate.isInHubWithMaster(oa)) {
                    // CACHE_NOTE: need to have OAObject.bCachedOnServer=true set by Client.
                    // see: OAObjectCSDelegate.addedToCache((OAObject) msg.newValue); // flag obj to know that it is cached on server for this client.
                    this.setCached((OAObject) obj);
                }
            }
        }
        int x = al.size();
        Object[] objs = new Object[x];
        if (x > 0) al.toArray(objs);
        return objs;
    }

    protected boolean datasourceHasNext(String id) {
        Iterator iterator = (Iterator) hashIterator.get(id);
        return (iterator != null && iterator.hasNext());
    }    

    protected String datasourceSelect(Class clazz, String queryWhere, Object[] params, String queryOrder) {
        OADataSource ds = getDataSource(clazz);
        String selectId;
        if (ds != null) {
            Iterator iterator = ds.select(clazz, (String) queryWhere, params, (String) queryOrder, 0, null); 
            selectId = "select" + aiSelectCount.incrementAndGet();
            if (iterator != null) hashIterator.put(selectId, iterator);
        }
        else selectId = null;
        return selectId;
    }    
}
