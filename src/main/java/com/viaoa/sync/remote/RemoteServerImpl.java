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

import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.model.ClientInfo;

/**
 * Server side remote object for clients to use.
 */
public abstract class RemoteServerImpl implements RemoteServerInterface {
    private static Logger LOG = Logger.getLogger(RemoteServerImpl.class.getName());
    
    @Override
    public String ping(String msg) {
        return msg;
    }
    @Override
    public void ping2(String msg) {
    }
    @Override
    public String getDisplayMessage() {
        return "OASyncServer";
    }

    @Override
    public boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule) {
        boolean bPrev = OAThreadLocalDelegate.setSendMessages(true);
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.save(iCascadeRule);
            bResult = true;
        }
        else bResult = false;
        
        OAThreadLocalDelegate.setSendMessages(bPrev);
        return bResult;
    }

    @Override
    public int getNextFiftyObjectGuids() {
        return OAObjectDelegate.getNextFiftyGuids();
    }
    
    @Override
    public OAObject getObject(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        if (obj == null) {
            if (OASyncDelegate.isServer(objectClass)) {
                obj = (OAObject) OADataSource.getObject(objectClass, objectKey);
            }
        }
        return obj;
    }

    @Override
    public Object runRemoteMethod(Class clazz, OAObjectKey objKey, String methodName, Object[] args) {
        Object obj = getObject(clazz, objKey);
        if (obj == null) {
            throw new RuntimeException("Object could not be found, class="+clazz+", objKey="+objKey);
        }
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(clazz);
        
        int x = 0;
        if (args != null && args.length > 0) x += args.length;
        Method method = OAObjectInfoDelegate.getMethod(oi, methodName, x);

        if (method == null) {
            throw new RuntimeException("method "+methodName+" not found in class "+clazz.getSimpleName());
        }
        Object objResult = null;
        try {
            objResult = method.invoke(obj, args);
        }
        catch (Exception e) {
            throw new RuntimeException("exception calling method="+methodName+", class="+clazz.getSimpleName(), e);
        }
        return objResult;
    }

    @Override
    public Object runRemoteMethod(Hub hub, String methodName, Object[] args) {
        if (hub == null) return null;
        Class clazz = hub.getObjectClass();
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(clazz);
        
        int x = 1;
        if (args != null && args.length > 0) x += args.length;
        Method method = OAObjectInfoDelegate.getMethod(oi, methodName, x);

        if (method == null) {
            throw new RuntimeException("method "+methodName+" not found in class "+clazz.getSimpleName());
        }
        Object objResult = null;
        try {
            Object[] objs = new Object[x];
            objs[0] = hub;
            if (x > 1) System.arraycopy(args, 0, objs, 1, x-1);
            objResult = method.invoke(null, objs);
        }
        catch (Exception e) {
            throw new RuntimeException("exception calling method="+methodName+", class="+clazz.getSimpleName()+", hub="+hub, e);
        }
        return objResult;
    }
    
    @Override
    public abstract RemoteClientInterface getRemoteClient(ClientInfo clientInfo);    
    
    @Override
    public abstract RemoteSessionInterface getRemoteSession(ClientInfo clientInfo, RemoteClientCallbackInterface callback);

    @Override
    public String performThreadDump(String msg) {
        String s = OAThreadLocalDelegate.getAllStackTraces();
        LOG.warning(msg + "\n" + s);
        return s;
    }
    
}
