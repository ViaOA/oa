package com.viaoa.sync.remote;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectHubDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface
public abstract class RemoteDataSourceImpl implements RemoteDataSourceInterface {
    private static Logger LOG = Logger.getLogger(RemoteDataSourceImpl.class.getName());
    
    private Hashtable<String, Iterator> hashIterator = new Hashtable<String, Iterator>(); // used to store DB

    public Object datasource(int command, Object[] objects) {
        Object obj = null;
        Class clazz, masterClass;
        OADataSource ds;
        Object objKey;
        boolean b;
        int x;
        Iterator iterator;
        Object whereObject;
        String propFromMaster;

        switch (command) {
        case OADataSourceClient.IT_NEXT:
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                Object[] objs = new Object[20];
                for (int i = 0; i < 20; i++) {
                    if (!iterator.hasNext()) break;
                    objs[i] = iterator.next();
                    if (objs[i] instanceof OAObject) {
                        OAObject oa = (OAObject) objs[i];
                        if (!OAObjectHubDelegate.isInHub(oa)) {
                            // CACHE_NOTE: need to have OAObject.bCachedOnServer=true set by Client.
                            // see: OAObjectCSDelegate.addedToCache((OAObject) msg.newValue); // flag obj to know that it is cached on server for this client.
                            this.setCached((OAObject) objs[i], true);
                        }
                    }
                }
                obj = objs;
            }
            break;
        case OADataSourceClient.IT_HASNEXT:
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                b = iterator.hasNext();
                if (!b) {
                    iterator.remove();
                    hashIterator.remove(objects[0]);
                }
                obj = new Boolean(b);
            }
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
            break;
        case OADataSourceClient.MAX_LENGTH:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.getMaxLength(clazz, (String) objects[1]);
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
                whereObject = objects[1];
                whereObject = OAObjectCacheDelegate.get(clazz, whereObject);
                ds.updateMany2ManyLinks((OAObject) whereObject, (OAObject[]) objects[2], (OAObject[]) objects[3], (String) objects[4]);
            }
            break;

        case OADataSourceClient.INSERT:
            obj = objects[0];
            if (obj != null) {
                b = ((Boolean) objects[1]).booleanValue();
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
                // 2006/09/14
                int z = objects.length - 2;
                Object[] objs = new Object[z];
                for (int y = 0; y < z; y++)
                    objs[y] = objects[2 + y];
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
            whereObject = objects[1];
            String extraWhere = (String) objects[2];
            Object[] args = (Object[]) objects[3];
            propFromMaster = (String) objects[4];

            if (whereObject instanceof OAObjectKey) {
                whereObject = OAObjectCacheDelegate.get(clazz, whereObject);
                if (whereObject == null) System.out.println("OAObjectServer.count cant find class=" + clazz + " id=" + objects[1]);
            }

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
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                iterator.remove();
                hashIterator.remove(objects[0]);
            }
            break;

        case OADataSourceClient.SELECT:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                int z = objects.length - 3;
                Object[] params = new Object[z];
                for (int y = 0; y < z; y++)
                    params[y] = objects[3 + y];

                iterator = ds.select(clazz, (String) objects[1], params, (String) objects[2]); // where, order
                obj = "client" + (selectCount++);
                if (iterator != null) hashIterator.put((String) obj, iterator);
            }
            break;
        case OADataSourceClient.SELECTPASSTHRU:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.select(clazz, (String) objects[1], (String) objects[2]); // where, order
                obj = "client" + (selectCount++);
                hashIterator.put((String) obj, iterator);
            }
            break;
        case OADataSourceClient.INITIALIZEOBJECT:
            clazz = (Class) objects[0].getClass();
            ds = getDataSource(clazz);
            ds.initializeObject((OAObject) objects[0]);
            break;
        case OADataSourceClient.SELECTUSINGOBJECT:
            clazz = (Class) objects[0]; // class to select
            masterClass = (Class) objects[1];
            whereObject = objects[2];
            extraWhere = (String) objects[3];
            args = (Object[]) objects[4];
            propFromMaster = (String) objects[5];

            //System.out.println("SELECT(w/master) class:"+clazz+" masterClass:"+masterClass+" whereObj:"+whereObject+" propFromMaster:"+propFromMaster);

            if (whereObject instanceof OAObjectKey) {
                whereObject = OAObjectCacheDelegate.get(masterClass, whereObject);
                if (whereObject == null) {
                    // System.out.println("OAObjectServer.select cant find using where object masterclass="+masterClass+" id="+objects[2]+" "+propFromMaster);
                    break;
                }
            }
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.select(clazz, (OAObject) whereObject, extraWhere, args, propFromMaster, (String) objects[6]);
                obj = "client" + (selectCount++);
                if (iterator != null) {
                    hashIterator.put((String) obj, iterator);
                }
            }
            break;
        case OADataSourceClient.INSERT_WO_REFERENCES:
            whereObject = objects[0];
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
                whereObject = OAObjectCacheDelegate.get(clazz, objKey);
                String prop = (String) objects[2];
                obj = ds.getPropertyBlobValue((OAObject) whereObject, prop);
            }
            break;
        }
        return obj;
    }
    protected OADataSource getDataSource(Class c) {
        if (c != null) {
            OADataSource ds = OADataSource.getDataSource(c);
            if (ds != null) return ds;
        }
        if (defaultDataSource == null) {
            OADataSource[] dss = OADataSource.getDataSources();
            if (dss != null && dss.length > 0) return dss[0];
        }
        return defaultDataSource;
    }
    private int selectCount;
    protected OADataSource defaultDataSource;

    protected OADataSource getDataSource() {
        return getDataSource(null);
    }
    
    public abstract void setCached(OAObject obj, boolean b);
}
