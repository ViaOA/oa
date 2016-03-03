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
import com.viaoa.util.OAFilter;

/**
 * Used by OADataSourceClient to have a client DS methods to be executed on server.
 */
public abstract class RemoteDataSource {
    private static Logger LOG = Logger.getLogger(RemoteDataSource.class.getName());
    
    private ConcurrentHashMap<String, Iterator> hashIterator = new ConcurrentHashMap<String, Iterator>(); // used to store DB
    
    public Object datasource(int command, Object[] objects) {
        //LOG.finer("command="+command);
        Object obj = null;
        Class clazz, masterClass;
        OADataSource ds;
        Object objKey;
        boolean b;
        int x;
        Object whereObject;
        String propFromWhereObject;

        switch (command) {
        case OADataSourceClient.ASSIGN_ID:
            clazz = (Class) objects[0].getClass();
            ds = getDataSource(clazz);
            if (ds != null) {
                //not needed
                //OARemoteThreadDelegate.sendMessages(true);
                ds.assignId((OAObject) objects[0]);
                //OARemoteThreadDelegate.sendMessages(false);
            }
            break;
        
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
        case OADataSourceClient.GET_ASSIGN_ID_ON_CREATE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.getAssignIdOnCreate();
                obj = new Boolean(b);
            }
            else obj = Boolean.FALSE;
            break;
        case OADataSourceClient.MAX_LENGTH:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.getMaxLength(clazz, (String) objects[1]);
                // System.out.println("note: RemoteDataSource call to MAX_LENGTH when it should be on the client.");                
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
                String queryWhere = (String) objects[1];
                Object[] params = (Object[]) objects[2];
                Class whereClass = (Class) objects[3];
                OAObjectKey whereKey = (OAObjectKey) objects[4];
                propFromWhereObject = (String) objects[5];
                String extraWhere = (String) objects[6];
                int max = (Integer) objects[7];
                
                whereObject = null;
                if (whereClass != null && whereKey != null) {
                    whereObject = getObject(whereClass, whereKey);
                }
                
                x = ds.count(clazz, queryWhere, params, (OAObject) whereObject, propFromWhereObject, extraWhere, max);
                obj = new Integer(x);
            }
            else obj = new Integer(-1);
            break;
            
        case OADataSourceClient.COUNTPASSTHRU:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.countPassthru(clazz, (String) objects[1], (Integer) objects[2]);
                obj = new Integer(x);
            }
            else obj = new Integer(-1);
            break;
            
        case OADataSourceClient.SUPPORTSSTORAGE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.supportsStorage();
                obj = new Boolean(b);
            }
            else obj = null;
            break;
        case OADataSourceClient.EXECUTE:
            ds = getDataSource();
            if (ds != null) {
                return ds.execute((String) objects[0]);
            }
            else obj = null;
            break;
        case OADataSourceClient.IT_REMOVE:
            Iterator iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                iterator.remove();
                hashIterator.remove(objects[0]);
                LOG.finer("remove iterator, size="+hashIterator.size());
            }
            break;

        case OADataSourceClient.SELECT:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            String selectId;
            if (ds != null) {
                String queryWhere = (String) objects[1];
                Object[] params = (Object[]) objects[2];
                String queryOrder = (String) objects[3];
                Class whereClass = (Class) objects[4];
                OAObjectKey whereKey = (OAObjectKey) objects[5];
                propFromWhereObject = (String) objects[6];
                String extraWhere = (String) objects[7];
                int max = (Integer) objects[8];
                boolean bDirty = (Boolean) objects[9];
                boolean bHasFilter = (Boolean) objects[10];
                
                whereObject = null;
                if (whereClass != null && whereKey != null) {
                    whereObject = getObject(whereClass, whereKey);
                }

                
                OAFilter filter = null;
                if (bHasFilter) {
                    // if client has a filter, then create a dummy one here
                    filter = new OAFilter() {
                        public boolean isUsed(Object obj) {
                            return true;
                        };
                    };
                }                
                iterator = ds.select(clazz, 
                    queryWhere, params, queryOrder,
                    (OAObject) whereObject, propFromWhereObject, extraWhere,
                    max, filter, bDirty);
                        
                selectId = "select" + aiSelectCount.incrementAndGet();
                if (iterator != null) {
                    hashIterator.put(selectId, iterator);
                    LOG.finer("add iterator, size="+hashIterator.size());
                }
            }
            else selectId = null;
            return selectId;

        case OADataSourceClient.SELECTPASSTHRU:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.selectPassthru(clazz, (String) objects[1], (String) objects[2], (Integer) objects[3], null, (Boolean) objects[4]); 
                obj = "select" + aiSelectCount.incrementAndGet();
                hashIterator.put((String) obj, iterator);
                LOG.finer("add iterator, size="+hashIterator.size());
            }
            else obj = null;
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
        if (x == 0) {
            iterator.remove();
            hashIterator.remove(id);
            LOG.finer("remove iterator, size="+hashIterator.size());
        }
        Object[] objs = new Object[x];
        if (x > 0) al.toArray(objs);
        return objs;
    }

    protected boolean datasourceHasNext(String id) {
        Iterator iterator = (Iterator) hashIterator.get(id);
        return (iterator != null && iterator.hasNext());
    }    
}
