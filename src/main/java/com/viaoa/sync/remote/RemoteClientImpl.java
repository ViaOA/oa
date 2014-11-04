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
package com.viaoa.sync.remote;

import java.util.concurrent.ConcurrentHashMap;
import com.viaoa.ds.OADataSource;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;

// see: OAClient

public abstract class RemoteClientImpl implements RemoteClientInterface {
    protected ConcurrentHashMap<Object, Object> hashCache = new ConcurrentHashMap<Object, Object>();
    protected ConcurrentHashMap<Object, Object> hashLock = new ConcurrentHashMap<Object, Object>();
    private ClientGetDetail clientGetDetail = new ClientGetDetail(); 
    private RemoteDataSource remoteDataSource;

    /**
     * called by client when objects are GCd,
     * so that they can be removed from server side session.
     */
    @Override
    public void removeGuids(int[] guids) {
        if (guids == null) return;
        int x = guids.length;
        for (int i=0; i<x; i++) {
            if (guids[i] > 0) {
                clientGetDetail.removeGuid(guids[i]);
            }
        }
    }
    
    @Override
    public Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property, String[] masterProps, OAObjectKey[] siblingKeys) {
        Object obj = clientGetDetail.getDetail(masterClass, masterObjectKey, property, masterProps, siblingKeys);
        return obj;
    }

    @Override
    public Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property) {
        Object obj = clientGetDetail.getDetail(masterClass, masterObjectKey, property, null, null);
        return obj;
    }

    public Object datasource(int command, Object[] objects) {
        if (remoteDataSource == null) {
            remoteDataSource = new RemoteDataSource() {
                @Override
                public void setCached(OAObject obj, boolean b) {
                    RemoteClientImpl.this.setCached(obj, b);
                }
            };            
        }
        Object result = remoteDataSource.datasource(command, objects);
        return result;
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

    @Override
    public OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        if (obj == null) return null;
        OAObject objx = OAObjectReflectDelegate.createCopy(obj, excludeProperties);
        setCached(objx, true);
        return objx;
    }
    
    
    public abstract void setCached(OAObject obj, boolean b);
    
}



