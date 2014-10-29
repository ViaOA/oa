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
package com.viaoa.sync;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.hub.Hub;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectHubDelegate;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectKeyDelegate;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAObjectSerializeDelegate;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.RemoteClientCallbackInterface;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteClientSyncInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OANotExist;

import static com.viaoa.sync.OASyncServer.*;

public class OASyncClient {
    protected static Logger LOG = Logger.getLogger(OASyncClient.class.getName());

    static final int MAX_ThreadCount = 100;
    
    /** this is used to create a connection (socket) to GSMR server. */
    private MultiplexerClient multiplexerClient;

    /** Allow for making remote method calls to an object instance on the server. */
    private RemoteMultiplexerClient remoteMultiplexerClient;

    /** information about this client */ 
    private ClientInfo clientInfo;

    private RemoteServerInterface remoteServerInterface;
    private RemoteClientInterface remoteClientInterface;
    private RemoteClientSyncInterface remoteClientSyncInterface;
    private RemoteSyncInterface remoteSyncInterface;
    private RemoteSyncImpl remoteSyncImpl;
    private String serverHostName;
    private int serverHostPort;

    private OADataSourceClient dataSourceClient;
    
    public OASyncClient(String serverHostName, int serverHostPort) {
        this.serverHostName = serverHostName;
        this.serverHostPort = serverHostPort;
        OASyncDelegate.setSyncClient(this);
    }

    public void startClientUpdateThread(final int seconds) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                getClientInfo();
                for (;;) {
                    clientInfo.setFreeMemory(Runtime.getRuntime().freeMemory());
                    clientInfo.setTotalMemory(Runtime.getRuntime().totalMemory());
                    try {
                        getRemoteClientInterface().update(clientInfo);
                        Thread.sleep(seconds * 1000);
                    }
                    catch (Exception e) {
                        break;
                    }
                }
            }
        }, "OASync.updateClientInfo."+seconds);
        t.setDaemon(true);
        t.start();
    }
    
    public Object getDetail_OLD(OAObject oaObj, String propertyName) {
        Object objx = null;
        try {
            objx = getRemoteClientSyncInterface().getDetail(oaObj.getClass(), oaObj.getObjectKey(), propertyName);
        }
        catch (Exception e) {
        }
        return objx;
    }

    public int cntGetDetail;
    public Object getDetail(OAObject masterObject, String propertyName) {
        //qqqqqvvvvv        
        //System.out.println("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName+", levels="+levels);
        //LOG.finer("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName);
        
        cntGetDetail++;        
        int xDup = OAObjectSerializeDelegate.cntDup;
        int xNew = OAObjectSerializeDelegate.cntNew;

        // LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        if (masterObject == null || propertyName == null) return null;

        boolean bGetSibs;
        Object result = null;
        boolean b = OARemoteThreadDelegate.shouldMessageBeQueued();
        if (!b) {
            bGetSibs = true;
            // send siblings to return back with same prop
            OAObjectKey[] siblingKeys = null;
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
            if (li == null || !li.getCalculated()) {
                siblingKeys = getDetailSiblings(masterObject, propertyName, li);
            }
            
            String[] props = OAObjectReflectDelegate.getUnloadedReferences(masterObject, false);
            try {
                result = getRemoteClientSyncInterface().getDetail(masterObject.getClass(), masterObject.getObjectKey(), propertyName, props, siblingKeys);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "getDetail error", e);
            }
        }
        else {
            try {
                result = getRemoteClientSyncInterface().getDetail(masterObject.getClass(), masterObject.getObjectKey(), propertyName);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "getDetail error", e);
            }
            bGetSibs = false;
        }
        if (result instanceof OAObjectSerializer) result = ((OAObjectSerializer)result).getObject();
        
        //qqqqqqq        

        if (true || OAObjectSerializeDelegate.cntNew-xNew > 25 || cntGetDetail % 100 == 0) {
            int iNew = OAObjectSerializeDelegate.cntNew; 
            int iDup = OAObjectSerializeDelegate.cntDup;
            
            System.out.println(String.format(
                "%,d) OASyncClient.getDetail() Obj=%s, prop=%s, ref=%s, getSib=%b, " +
                "newCnt=%d, dupCnt=%d, totNewCnt=%d, totDupCnt=%d",
                cntGetDetail, 
                masterObject, 
                propertyName, 
                result==null?"null":result.getClass().getName(),
                bGetSibs,
                iNew-xNew, 
                iDup-xDup,
                iNew, 
                iDup
            ));        
        }
        return result;
    }

    private OAObject[] lastMasterObjects = new OAObject[10];
    private int lastMasterCnter;
    /**
     * Find any other siblings to get the same property for.
     */
    protected OAObjectKey[] getDetailSiblings(OAObject masterObject, String property, OALinkInfo linkInfo) {
        Hub siblingHub = null;
        // note: could be for a blob property

        Hub hubThreadLocal = OAThreadLocalDelegate.getGetDetailHub();
        if (hubThreadLocal != null && hubThreadLocal.contains(masterObject)) {
            siblingHub = hubThreadLocal;
        }
        
        if (siblingHub == null) {
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(masterObject);
            
            int hits = 0;
            for (int i=0; (hubs != null && i < hubs.length); i++) {
                Hub hub = hubs[i];
                if (hub == null) continue;
                OAObject masterx = hub.getMasterObject();
                if (masterx == null && hub.getSelect() == null) continue;
                
                if (siblingHub == null) { 
                    siblingHub = hub;
                    continue;
                }
                // see if one of the previous objects can be found
                if (hits == 0) {
                    hits++;
                    for (OAObject objz : lastMasterObjects) {
                        if (objz != null && siblingHub.contains(objz)) {
                            hits++;
                        }
                    }
                }
                
                int hits2 = 1;
                for (OAObject objz : lastMasterObjects) {
                    if (objz != null && hub.contains(objz)) {
                        hits2++;
                    }
                }
                if (hits2 > hits) {
                    hits = hits2;
                    siblingHub = hub;
                }
                else if (hits2 == hits) {
                    if (hub.getSize() > siblingHub.getSize())  siblingHub = hub;
                }
            }
        }
        lastMasterObjects[lastMasterCnter++%lastMasterObjects.length] = masterObject;
        if (siblingHub == null) {
            return null;
        }
        
        ArrayList<OAObjectKey> al = new ArrayList<OAObjectKey>();
        // load the same property for siblings
        int pos = siblingHub.getPos(masterObject)+1;
        int cnt = 0;

        Class valueClass = null;
        boolean bIsOne2One = false;
        boolean bIsMany = false;

        if (linkInfo != null) {
            valueClass = linkInfo.getToClass();
            bIsOne2One = OAObjectInfoDelegate.isOne2One(linkInfo);
            bIsMany = linkInfo.getType() == linkInfo.MANY;
        }
        
        for (int i=0; i<250; i++) {
            Object obj = siblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == masterObject) continue;

            Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, property, true);
            if (value instanceof OANotExist) {
                if (linkInfo == null) {  // must be blob
                    OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                    al.add(key);
                    if (++cnt == 20) break;
                }
                else if (bIsMany || bIsOne2One) {                
                    OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                    al.add(key);
                    if (++cnt == 50) break;
                } 
                // otherwise, it must be null
            }
            else if (value instanceof OAObjectKey) {
                if (OAObjectCacheDelegate.get(valueClass, value) == null) {
                    OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                    al.add(key);
                    if (++cnt == 75) break;
                }
            }
        }

        if (al == null || al.size() == 0) return null;
        int x = al.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        al.toArray(keys);
        return keys;
    }
    
    
    
    public RemoteServerInterface getRemoteServerInterface() throws Exception {
        if (remoteServerInterface == null) {
            remoteServerInterface = (RemoteServerInterface) getRemoteMultiplexerClient().lookup(ServerLookupName);
            OASyncDelegate.setRemoteServerInterface(remoteServerInterface);
        }
        return remoteServerInterface;
    }
    // used for oasync callback (messages from other computers)
    public RemoteSyncImpl getRemoteSyncImpl() throws Exception {
        if (remoteSyncImpl == null) {
            remoteSyncImpl = new RemoteSyncImpl();
        }
        return remoteSyncImpl;
    }
    public RemoteSyncInterface getRemoteSyncInterface() throws Exception {
        if (remoteSyncInterface == null) {
            remoteSyncInterface = (RemoteSyncInterface) getRemoteMultiplexerClient().lookupBroadcast(SyncLookupName, getRemoteSyncImpl());
            OASyncDelegate.setRemoteSyncInterface(remoteSyncInterface);
        }
        return remoteSyncInterface;
    }
    public RemoteClientInterface getRemoteClientInterface() throws Exception {
        if (remoteClientInterface == null) {

            
            remoteClientInterface = getRemoteServerInterface().getRemoteClientInterface(getClientInfo(), getRemoteClientCallbackInterface());
            OASyncDelegate.setRemoteClientInterface(remoteClientInterface);
        }
        return remoteClientInterface;
    }
    private RemoteClientCallbackInterface remoteCallback;
    
    public RemoteClientCallbackInterface getRemoteClientCallbackInterface() {
        if (remoteCallback == null) {
            remoteCallback = new RemoteClientCallbackInterface() {
                @Override
                public void stop(String title, String msg) {
                    OASyncClient.this.onStopCalled(title, msg);
                }
            };
        }
        return remoteCallback;
    }
    public RemoteClientSyncInterface getRemoteClientSyncInterface() throws Exception {
        if (remoteClientSyncInterface == null) {
            remoteClientSyncInterface = getRemoteServerInterface().getRemoteClientSyncInterface(getClientInfo());
            OASyncDelegate.setRemoteClientSyncInterface(remoteClientSyncInterface);
        }
        return remoteClientSyncInterface;
    }
    

    public Object lookup(String lookupName) throws Exception {
        return getRemoteMultiplexerClient().lookup(lookupName);
    }
    public Object lookupBroadcast(String lookupName, Object callback) throws Exception {
        return getRemoteMultiplexerClient().lookupBroadcast(lookupName, callback);
    }
    
    
    public ClientInfo getClientInfo() {
        if (clientInfo == null) {
            clientInfo = new ClientInfo();
            clientInfo.setCreated(new OADateTime());
            clientInfo.setServerHostName(this.serverHostName);
            clientInfo.setServerHostPort(this.serverHostPort);
            
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                clientInfo.setHostName(localHost.getHostName());
                clientInfo.setIpAddress(localHost.getHostAddress());
            }
            catch (Exception e) {
            }
            
        }
        return clientInfo;
    }
    
    public void start() throws Exception {
        LOG.config("starting");

        getClientInfo();
        getMultiplexerClient().setKeepAlive(115); 
        
        LOG.fine("starting multiplexer client");
        getMultiplexerClient().start(); // this will connect to server using multiplexer
        clientInfo.setConnectionId(getMultiplexerClient().getConnectionId());
        
        LOG.fine("getting remote object for Client Session");
        getRemoteServerInterface();
        getRemoteSyncInterface();
        getRemoteClientInterface();
        getRemoteClientSyncInterface();

        LOG.fine("creating OADataSourceClient for remote database access");
        getOADataSourceClient();
        
        clientInfo.setStarted(true);
        LOG.config("startup completed successful");
    }
    
    public OADataSourceClient getOADataSourceClient() {
        if (dataSourceClient == null) {
            dataSourceClient = new OADataSourceClient();
        }
        return dataSourceClient;
    }
    
    public boolean isStarted() {
        return getClientInfo().isStarted();
    }
    /** Sets the stop flag */
    public void stop() throws Exception {
        if (!isStarted()) return;
        LOG.fine("Client stop");
        getClientInfo().setStarted(false);
        if (isConnected()) {
            getMultiplexerClient().close();
        }
        multiplexerClient = null;
        remoteMultiplexerClient = null;
    }

    public void onStopCalled(String title, String msg) {
        LOG.warning("stopped called by server, title="+title+", msg="+msg);
        try {
            getRemoteClientInterface().sendException(title+", "+msg, new Exception("onStopCalled on client"));
            stop();
        }
        catch (Exception e) {
        }
    }
    
    
    /**
     * checks to see if this client has been connected to the GSMR server.
     */
    public boolean isConnected() {
        if (multiplexerClient == null) return false;
        if (!multiplexerClient.isConnected()) return false;
        return true;
    }

    public int getPort() {
        if (!isConnected()) return -1;
        return getRemoteMultiplexerClient().getMultiplexerClient().getPort();
    }
    public String getHost() {
        if (!isConnected()) return null;
        return getRemoteMultiplexerClient().getMultiplexerClient().getHost();
    }
    
    
    /** the socket connection to GSMR server. 
     * @see #onSocketException(Exception) for connection errors
     * */
    protected MultiplexerClient getMultiplexerClient() {
        if (multiplexerClient != null) return multiplexerClient; 
        multiplexerClient = new MultiplexerClient(getClientInfo().getServerHostName(), clientInfo.getServerHostPort()) {
            @Override
            protected void onSocketException(Exception e) {
                OASyncClient.this.onSocketException(e);
            }
            @Override
            protected void onClose(boolean bError) {
                OASyncClient.this.onSocketClose(bError);
            }
        };
        return multiplexerClient;
    }
    
    /**
     * Called when there is an exception with the real socket.
     */
    protected void onSocketException(Exception e) {
        try {
            LOG.log(Level.WARNING, "exception with connection to server", e);
        }
        catch (Exception ex) {
        }
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }
    protected void onSocketClose(boolean bError) {
        try {
            LOG.fine("closing, isError="+bError);
        }
        catch (Exception ex) {
        }
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }
    
    private boolean bThreadCountWarning;
    /** allows remote method calls to GSMR server. */
    public RemoteMultiplexerClient getRemoteMultiplexerClient() {
        if (remoteMultiplexerClient == null) { 
            remoteMultiplexerClient = new RemoteMultiplexerClient(getMultiplexerClient()) {
                @Override
                protected void onRemoteThreadCreated(int threadCount) {
                    getClientInfo().setRemoteThreadCount(threadCount);
                    super.onRemoteThreadCreated(threadCount);
                    if (threadCount == MAX_ThreadCount && !bThreadCountWarning) {
                        String s = OALogUtil.getThreadDump();
                        LOG.warning("RemoteThread count == "+MAX_ThreadCount+"\n"+s);
                        bThreadCountWarning = true;
                    }
                    if (threadCount >= MAX_ThreadCount) {
                        onRemoteThreadCountExceeded();
                    }
                }                
            };
        }
        return remoteMultiplexerClient;
    }
    public int getConnectionId() {
        return getMultiplexerClient().getConnectionId();
    }
    
    protected void onRemoteThreadCountExceeded() {
        LOG.log(Level.WARNING, "max RemoteThread count of "+MAX_ThreadCount+" exceeded, calling OASyncClient.stop");
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }


}
