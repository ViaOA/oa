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
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteClientSyncInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OALogUtil;

import static com.viaoa.sync.OASyncServer.*;

public class OASyncClient {
    protected static Logger LOG = Logger.getLogger(OASyncClient.class.getName());

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

    public OASyncClient(String serverHostName, int serverHostPort) {
        this.serverHostName = serverHostName;
        this.serverHostPort = serverHostPort;
        OASyncDelegate.setSyncClient(this);
    }

    public void startClientUpdateThread(final int seconds) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    getClientInfo().setFreeMemory(Runtime.getRuntime().freeMemory());
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
        if (!OARemoteThreadDelegate.isRemoteThread()) {
            bGetSibs = true;
            // send siblings to return back with same prop
            OAObjectKey[] siblingKeys = null;
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
            if (li == null || !li.getCalculated()) {
                siblingKeys = getDetailSiblings(masterObject, propertyName);
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
        if (true || OAObjectSerializeDelegate.cntNew-xNew > 25 || cntGetDetail % 100 == 0)        
        System.out.println(String.format(
            "%,d) OASyncClient.getDetail() Obj=%s, prop=%s, ref=%s, getSib=%b, newCnt=%d, dupCnt=%d, totNewCnt=%d, totDupCnt=%d",
            cntGetDetail, 
            masterObject, 
            propertyName, 
            result==null?"null":result.getClass().getName(),
            bGetSibs,
            OAObjectSerializeDelegate.cntNew-xNew, 
            OAObjectSerializeDelegate.cntDup-xDup,
            OAObjectSerializeDelegate.cntNew, 
            OAObjectSerializeDelegate.cntDup
        ));        
        
        return result;
    }

    private OAObject[] lastMasterObjects = new OAObject[10];
    private int lastMasterCnter;
    /**
     * Find any other siblings to get the same property for.
     */
    protected OAObjectKey[] getDetailSiblings(OAObject masterObject, String property) {
        Hub siblingHub = null;
        
        Hub hubThreadLocal = OAThreadLocalDelegate.getGetDetailHub();
        if (hubThreadLocal != null && hubThreadLocal.contains(masterObject)) {
            siblingHub = hubThreadLocal;
        }
        
        if (siblingHub == null) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferences(masterObject);
            
            int hits = 0;
            for (int i=0; (refs != null && i < refs.length); i++) {
                WeakReference<Hub<?>> ref = refs[i];
                if (ref == null) continue;
                Hub hub = ref.get();
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
        for (int i=0; i<250; i++) {
            Object obj = siblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == masterObject) continue;

            Object value = OAObjectReflectDelegate.getRawReference((OAObject)obj, property);
            if (value == null) {
                if (!OAObjectPropertyDelegate.isPropertyLoaded((OAObject)obj, property)) {                     
                    OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                    al.add(key);
                    if (++cnt == 50) break;
                }
            }
            else if (value instanceof OAObjectKey) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                al.add(key);
                if (++cnt == 155) break;
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
            remoteClientInterface = getRemoteServerInterface().getRemoteClientInterface(getClientInfo());
            OASyncDelegate.setRemoteClientInterface(remoteClientInterface);
        }
        return remoteClientInterface;
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
        new OADataSourceClient();
        
        clientInfo.setStarted(true);
        LOG.config("startup completed successful");
    }
    public boolean isStarted() {
        return clientInfo.isStarted();
    }
    /** Sets the stop flag, which will stop Gemstone methods from being sent to GSMRServer */
    public void stop() throws Exception {
        LOG.fine("Client stop");
        clientInfo.setStarted(false);
        if (isConnected()) {
            getMultiplexerClient().close();
        }
        multiplexerClient = null;
        remoteMultiplexerClient = null;
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
        multiplexerClient = new MultiplexerClient(clientInfo.getServerHostName(), clientInfo.getServerHostPort()) {
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
                    clientInfo.setRemoteThreadCount(threadCount);
                    super.onRemoteThreadCreated(threadCount);
                    if (threadCount == 60 && !bThreadCountWarning) {
                        String s = OALogUtil.getThreadDump();
                        LOG.warning("RemoteThread count == 60\n"+s);
                        bThreadCountWarning = true;
                    }
                    if (threadCount >= 70) {
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
        LOG.log(Level.WARNING, "max RemoteThread count of 70 exceeded, calling stop");
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }
    

}
