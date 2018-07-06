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
package com.viaoa.sync;

import java.lang.ref.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.RemoteClientCallbackInterface;
import com.viaoa.sync.remote.RemoteSessionInterface;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.util.*;

import static com.viaoa.sync.OASyncServer.*;

/**
 * Used to connect to OASyncServer and setup OASync.
 * @author vvia
 * @see OASync
 */
public class OASyncClient {
    protected static Logger LOG = Logger.getLogger(OASyncClient.class.getName());

    /** this is used to create a connection (socket) to GSMR server. */
    private MultiplexerClient multiplexerClient;

    /** Allow for making remote method calls to an object instance on the server. */
    private RemoteMultiplexerClient remoteMultiplexerClient;

    /** information about this client */ 
    private ClientInfo clientInfo;

    private RemoteServerInterface remoteServerInterface;
    private RemoteSessionInterface remoteClientInterface;
    private RemoteClientInterface remoteClientSyncInterface;
    private RemoteSyncInterface remoteSyncInterface;
    private RemoteSyncInterface remoteSyncImpl;
    private String serverHostName;
    private int serverHostPort;
    private final boolean bUpdateSyncDelegate;  // flag to know if this is the main client. Otherwise it could be a combinedSyncClient

    // used by getDetail
    private OAObject[] lastMasterObjects = new OAObject[10];
    private int lastMasterCnter;
    private final Package packagex;
    
    private OADataSourceClient dataSourceClient;

    public OASyncClient(String serverHostName, int serverHostPort) {
        this(null, serverHostName, serverHostPort);
    }
    
    public OASyncClient(Package packagex, String serverHostName, int serverHostPort) {
        this(packagex, serverHostName, serverHostPort, true);
    }

    protected OASyncClient(Package packagex, String serverHostName, int serverHostPort, boolean bUpdateSyncDelegate) {
        if (packagex == null) packagex = Object.class.getPackage();
        this.packagex = packagex;
        this.serverHostName = serverHostName;
        this.serverHostPort = serverHostPort;
        this.bUpdateSyncDelegate = bUpdateSyncDelegate;
        //was: if (bUpdateSyncDelegate) OASyncDelegate.setSyncClient(packagex, this);
    }
    
    
    public void startUpdateThread(final int seconds) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                getClientInfo();
                for (;;) {
                    clientInfo.setFreeMemory(Runtime.getRuntime().freeMemory());
                    clientInfo.setTotalMemory(Runtime.getRuntime().totalMemory());
                    try {
                        if (!OAObject.getDebugMode()) {
                            getRemoteSession().update(clientInfo);
                        }
                        Thread.sleep(seconds * 1000);
                    }
                    catch (Exception e) {
                        break;
                    }
                }
            }
        }, "OASyncClient.update."+seconds);
        t.setDaemon(true);
        t.start();
    }
    
    private final AtomicInteger aiCntGetDetail = new AtomicInteger();
    private final ConcurrentHashMap<Integer, Boolean> hmIgnoreSibling = new ConcurrentHashMap<Integer, Boolean>(); 
    
    /**
     * This is sent to the server using ClientGetDetail, by using a customized objectSerializer
     * @param masterObject
     * @param propertyName
     */
    public Object getDetail(final OAObject masterObject, final String propertyName) {
        LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        long ts = System.currentTimeMillis();
        
        final int cntx = aiCntGetDetail.incrementAndGet();        

        // LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        if (masterObject == null || propertyName == null) return null;

        boolean bGetSibs = false;
        OAObjectKey[] siblingKeys = null;
        String[] additionalMasterProperties = null; 
        Object result = null;

        int cntNew=0;
        int cntDup=0;
        
        OALinkInfo li = null;
        try {
            // both Hub && pp are set by HubMerger, HubGroupBy
            final boolean bHasSiblingHelper = OAThreadLocalDelegate.hasSiblingHelpers();
            
            if (OARemoteThreadDelegate.isRemoteThread()) {
                    // use annotated version that does not use the msg queue
                cntDup = OAObjectSerializeDelegate.cntDup;
                cntNew = OAObjectSerializeDelegate.cntNew;
                result = getRemoteClient().getDetailNow(cntx, masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                        additionalMasterProperties, siblingKeys, bHasSiblingHelper);
            }
            else {
                // this will "ask" for additional data "around" the requested property
                bGetSibs = true;
                // send siblings to return back with same prop
                li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
                
                int max;
                if (li == null) max = 20;
                else if (li.getType() == OALinkInfo.TYPE_MANY) {
                    if (li.getCouldBeLarge()) max = 5;
                    else max = 50;
                }
                else max = 100;
                
                if (bHasSiblingHelper) max *= 3;
                
                siblingKeys = OAObjectSiblingDelegate.getSiblings(masterObject, propertyName, max, hmIgnoreSibling);
                
/*qqqqqqqqqqqqqqqqqqqqq                
if (siblingKeys == null || siblingKeys.length == 0) {
    siblingKeys = OAObjectSiblingDelegate.getSiblings(masterObject, propertyName, max, hmIgnoreSibling);
    int xx = 4;
    xx++;
}
*/                
                additionalMasterProperties = OAObjectReflectDelegate.getUnloadedReferences(masterObject, false, propertyName, false);
              
                try {
                    cntDup = OAObjectSerializeDelegate.cntDup;
                    cntNew = OAObjectSerializeDelegate.cntNew;
                    result = getRemoteClient().getDetailNow(cntx, masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                        additionalMasterProperties, siblingKeys, bHasSiblingHelper);
                }
                finally {
                    for (OAObjectKey ok : siblingKeys) {
                        hmIgnoreSibling.remove(ok.getGuid());
                    }
                }
            }
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "getDetail error", e);
        }
        
        lastMasterObjects[lastMasterCnter++%lastMasterObjects.length] = masterObject;
        int cntSib = 0;
        
        Object resultHold = result;

        if (result instanceof OAObjectSerializer) {
            // see ClientGetDetail.getSerializedDetail(..)
            OAObjectSerializer os = (OAObjectSerializer) result;

            cntDup = os.dupCount;
            cntNew = os.newCount;
            
            result = os.getObject();
            
            // the custom serializer can send extra objects, and might use objKey instead of the object. 
            Object objx = os.getExtraObject();
            
            if (objx instanceof HashMap) {
                HashMap<OAObjectKey, Object> hmExtraData = (HashMap<OAObjectKey, Object>) objx;
                cntSib = hmExtraData.size();
                for (Entry<OAObjectKey, Object> entry : hmExtraData.entrySet()) {
                    Object value = entry.getValue();
                    if (value == masterObject) {
                        if (cntSib > 0) cntSib--;
                        continue;
                    }
                    
                    OAObject obj = OAObjectCacheDelegate.getObject(masterObject.getClass(), entry.getKey());
                    if (obj == null) continue; 

                    if (value instanceof Hub) {
                        Hub hub = (Hub) value;    
                        if (li == null) li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
                        if (li != null) {
                            if (OAObjectInfoDelegate.cacheHub(li, hub)) {
                                value = new WeakReference(hub);
                            }
                        }
                    }
                    OAObjectPropertyDelegate.setProperty(obj, propertyName, value);  // this will also set the hub.masterObj+li
                }
            }
        }
        else {
            cntDup = OAObjectSerializeDelegate.cntDup - cntDup;
            cntNew = OAObjectSerializeDelegate.cntNew - cntNew;
        }
        
        if (result instanceof Hub) {
            Hub hub = (Hub) result;
            if (li == null) li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
            if (OAObjectInfoDelegate.cacheHub(li, hub)) {
                OAObjectPropertyDelegate.setProperty(masterObject, propertyName, new WeakReference(hub));
            }
            else {
                OAObjectPropertyDelegate.setProperty(masterObject, propertyName, hub);  // this will also set the hub.masterObj+li
            }
        }

        if (true || OAObjectSerializeDelegate.cntNew-cntNew > 25 || cntx % 100 == 0) {
            
            ts = System.currentTimeMillis() - ts;
            String s = "";
            if (ts > 750) {
                if (cntNew > 1000) {
                    if (ts > 2000) s = " ALERT";
                }
                else s = " ALERT";
            }
            s = String.format(
                "client=%d, id=%,d, Obj=%s, prop=%s, ref=%s, getSib=%,d/%,d, moreProps=%d, " +
                "newCnt=%,d, dupCnt=%,d, totNewCnt=%,d, totDupCnt=%,d, ms=%,d%s",
                getConnectionId(),
                cntx, 
                masterObject.getClass().getSimpleName()+"."+masterObject.getProperty("id"), 
                propertyName, 
                result==null?"null":result.getClass().getSimpleName(),
                cntSib,
                (siblingKeys == null)?0:siblingKeys.length,
                additionalMasterProperties==null?0:additionalMasterProperties.length,
                cntNew, 
                cntDup,
                OAObjectSerializeDelegate.cntNew, 
                OAObjectSerializeDelegate.cntDup,
                ts,
                s
            );
            OAPerformance.LOG.fine(s);
            LOG.fine(s);
            if (OAObject.getDebugMode()) {
                System.out.println("OASyncClient.getDetail: "+s);
            }
        }
        return result;
    }

    
    public RemoteServerInterface getRemoteServer() throws Exception {
        if (remoteServerInterface == null) {
            remoteServerInterface = (RemoteServerInterface) getRemoteMultiplexerClient().lookup(ServerLookupName);
            if (bUpdateSyncDelegate) OASyncDelegate.setRemoteServer(packagex, remoteServerInterface);
        }
        return remoteServerInterface;
    }
    // used for oasync callback (messages from other computers)
    public RemoteSyncInterface getRemoteSyncImpl() throws Exception {
        if (remoteSyncImpl == null) {
            remoteSyncImpl = new RemoteSyncImpl();
        }
        return remoteSyncImpl;
    }
    public RemoteSyncInterface getRemoteSync() throws Exception {
        if (remoteSyncInterface == null) {
            remoteSyncInterface = (RemoteSyncInterface) getRemoteMultiplexerClient().lookupBroadcast(SyncLookupName, getRemoteSyncImpl());
            if (bUpdateSyncDelegate) OASyncDelegate.setRemoteSync(packagex, remoteSyncInterface);
        }
        return remoteSyncInterface;
    }
    public RemoteSessionInterface getRemoteSession() throws Exception {
        if (remoteClientInterface == null) {
            remoteClientInterface = getRemoteServer().getRemoteSession(getClientInfo(), getRemoteClientCallback());
            if (bUpdateSyncDelegate) OASyncDelegate.setRemoteSession(packagex, remoteClientInterface);
        }
        return remoteClientInterface;
    }
    private RemoteClientCallbackInterface remoteCallback;
    
    public RemoteClientCallbackInterface getRemoteClientCallback() {
        if (remoteCallback == null) {
            remoteCallback = new RemoteClientCallbackInterface() {
                @Override
                public void stop(String title, String msg) {
                    OASyncClient.this.onStopCalled(title, msg);
                }
                @Override
                public String ping(String msg) {
                    return "client recvd "+msg;
                }
                @Override
                public String performThreadDump(String msg) {
                    String s = OAThreadLocalDelegate.getAllStackTraces();
                    LOG.warning(msg + "\n" + s);
                    return s;
                }
            };
        }
        return remoteCallback;
    }
    public RemoteClientInterface getRemoteClient() throws Exception {
        if (remoteClientSyncInterface == null) {
            remoteClientSyncInterface = getRemoteServer().getRemoteClient(getClientInfo());
            if (bUpdateSyncDelegate) OASyncDelegate.setRemoteClient(packagex, remoteClientSyncInterface);
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
        
        LOG.fine("multiplexer client connected, connectionId="+getMultiplexerClient().getConnectionId());
        
        clientInfo.setConnectionId(getMultiplexerClient().getConnectionId());

        if (bUpdateSyncDelegate) OASyncDelegate.setSyncClient(packagex, this);
        
        LOG.fine("getting remote object for Client Session");
        getRemoteServer();
        getRemoteSync();
        getRemoteSession();
        getRemoteClient();
        if (bUpdateSyncDelegate) {
            startQueueGuidThread();
            startQueueGuidThread2();
        }

        if (bUpdateSyncDelegate) {
            LOG.fine("creating OADataSourceClient for remote database access");
            getOADataSourceClient();
        }
        
        clientInfo.setStarted(true);

        
        LOG.config("startup completed successful");
    }
    
    public OADataSourceClient getOADataSourceClient() {
        if (dataSourceClient == null) {
            dataSourceClient = new OADataSourceClient(packagex);
        }
        return dataSourceClient;
    }
    
    public boolean isStarted() {
        return getClientInfo().isStarted();
    }
    /** Sets the stop flag */
    public void stop() throws Exception {
        stop(true);
    }
    public void stop(boolean bCallClose) throws Exception {
        if (!isStarted()) return;
        LOG.fine("Client stop");
        getClientInfo().setStarted(false);
        if (bCallClose && isConnected()) {
            getMultiplexerClient().close();
        }
        multiplexerClient = null;
        remoteMultiplexerClient = null;
        
        if (bUpdateSyncDelegate) {        
            OASyncDelegate.setSyncClient(packagex, null);
            OASyncDelegate.setRemoteServer(packagex, null);
            OASyncDelegate.setRemoteSync(packagex, null);
            OASyncDelegate.setRemoteSession(packagex, null);
            OASyncDelegate.setRemoteClient(packagex, null);
            OADataSource ds = getOADataSourceClient();
            if (ds != null) ds.close();
        }
    }

    public void onStopCalled(String title, String msg) {
        LOG.warning("stopped called by server, title="+title+", msg="+msg);
        try {
            getRemoteSession().sendException(title+", "+msg, new Exception("onStopCalled on client"));
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
            stop(false);
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
            stop(!bError);
        }
        catch (Exception ex) {
        }
    }
    
    private long msLastThreadCountWarning;
    /** allows remote method calls to GSMR server. */
    public RemoteMultiplexerClient getRemoteMultiplexerClient() {
        if (remoteMultiplexerClient != null) return remoteMultiplexerClient; 
        remoteMultiplexerClient = new RemoteMultiplexerClient(getMultiplexerClient()) {
            @Override
            protected void onRemoteThreadCreated(int totalCount, int liveCount) {
                getClientInfo().setRemoteThreadCount(liveCount);
                if (liveCount > 80) {
                    long msNow = System.currentTimeMillis();
                    if (msLastThreadCountWarning + 2500 < msNow) {
                        msLastThreadCountWarning = msNow;
                        String s = OALogUtil.getThreadDump();
                        LOG.warning("RemoteThread liveCount="+liveCount+", totalCreated="+totalCount+"\n"+s);
                    }
                }
            }
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                OASyncClient.this.afterInvokeRemoteMethod(ri);
            }
            @Override
            public void afterInvokForStoC(RequestInfo ri) {
                OASyncClient.this.afterInvokeRemoteMethod(ri);
            }
            
        };
        return remoteMultiplexerClient;
    }
    public int getConnectionId() {
        return getMultiplexerClient().getConnectionId();
    }
    
    protected void afterInvokeRemoteMethod(RequestInfo ri) {
        if (ri == null) return;
        
        // dont log oasync msgs
        if (ri.bind == null) return;
        if (ri.bind.isOASync) {
            if (ri.exception == null && ri.exceptionMessage == null) {
                return;
            }
        }
        logRequest(ri);
    }
    protected void logRequest(RequestInfo ri) {
        /* debug        
        if (ri.exception != null || ri.exceptionMessage != null) {
            if (ri.exception != null) {
                ri.exception.printStackTrace();
            }
        }
        */
        LOG.fine(ri.toLogString());
    }


    /**
     * called when object is removed from object cache 
     * 
     * called by oaObject.finalize, and removeFromServerSideCache
     * 
     */
    public void objectRemoved(int guid) {
        try {
            if (guid > 0 && bUpdateSyncDelegate) {
                LinkedBlockingQueue<Integer> q = queRemoveGuid;
                if (q != null) q.add(guid);
            }
        }
        catch (Exception e) {
        }
    }

    private volatile LinkedBlockingQueue<Integer> queRemoveGuid;
    private Thread threadRemoveGuid;
    private void startQueueGuidThread() {
        if (queRemoveGuid != null) return;
        queRemoveGuid = new LinkedBlockingQueue<Integer>();
        threadRemoveGuid = new Thread(new Runnable() {
            long msLastError;
            int cntError;
            int[] guids = new int[150];
            @Override
            public void run() {
                RemoteSessionInterface rsi = null;
                for (int guidPos = 0;;) {
                    try {
                        int guid = queRemoveGuid.take(); 
                        guids[guidPos++ % 150] = guid;
                        if (guidPos % 150 == 0) {
                            if (rsi == null) {
                                rsi = OASyncClient.this.getRemoteSession();
                            }
                            if (rsi != null) {
                                rsi.removeGuids(guids);
                            }
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "Error in removeGuid thread", e);
                        long ms = System.currentTimeMillis();
                        if (++cntError > 5) {
                            if (ms - 2000 < msLastError) {
                                LOG.warning("too many errors, will stop this GuidRemove thread (not critical)");
                                queRemoveGuid = null;
                                break;
                            }
                            else {
                                cntError = 0;
                            }
                        }
                        msLastError = ms;
                    }
                }
            }
        }, "OASyncClient.RemoveGuid");
        threadRemoveGuid.setPriority(Thread.MIN_PRIORITY);
        threadRemoveGuid.setDaemon(true);
        threadRemoveGuid.start();
    }
    
    
    /**
     * called when object is removed from object cache 
     * 
     * called by oaObject.finalize, and removeFromServerSideCache
     * 
     */
    public void removeFromServerCache(int guid) {
        try {
            if (guid > 0 && bUpdateSyncDelegate) {
                LinkedBlockingQueue<Integer> q = queRemoveGuid2;
                if (q != null) q.add(guid);
            }
        }
        catch (Exception e) {
        }
    }

    private volatile LinkedBlockingQueue<Integer> queRemoveGuid2;
    private Thread threadRemoveGuid2;
    private void startQueueGuidThread2() {
        if (queRemoveGuid2 != null) return;
        queRemoveGuid2 = new LinkedBlockingQueue<Integer>();
        threadRemoveGuid2 = new Thread(new Runnable() {
            long msLastError;
            int cntError;
            int[] guids = new int[150];
            @Override
            public void run() {
                RemoteSessionInterface rsi = null;
                for (int guidPos = 0;;) {
                    try {
                        int guid = queRemoveGuid2.take(); 
                        guids[guidPos++ % 150] = guid;
                        if (guidPos % 150 == 0) {
                            if (rsi == null) {
                                rsi = OASyncClient.this.getRemoteSession();
                            }
                            if (rsi != null) {
                                rsi.removeFromServerCache(guids);
                            }
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "Error in removeGuid thread", e);
                        long ms = System.currentTimeMillis();
                        if (++cntError > 5) {
                            if (ms - 2000 < msLastError) {
                                LOG.warning("too many errors, will stop this GuidRemove thread (not critical)");
                                queRemoveGuid2 = null;
                                break;
                            }
                            else {
                                cntError = 0;
                            }
                        }
                        msLastError = ms;
                    }
                }
            }
        }, "OASyncClient.RemoveGuid2");
        threadRemoveGuid2.setPriority(Thread.MIN_PRIORITY);
        threadRemoveGuid2.setDaemon(true);
        threadRemoveGuid2.start();
    }
    
    
}
