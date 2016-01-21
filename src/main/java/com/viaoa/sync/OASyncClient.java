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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
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
import com.viaoa.sync.remote.RemoteSessionInterface;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OANotExist;

import static com.viaoa.sync.OASyncServer.*;

/**
 * Used to connect to OASyncServer and setup OASync.
 * @author vvia
 * @see OASync
 */
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
    private RemoteSessionInterface remoteClientInterface;
    private RemoteClientInterface remoteClientSyncInterface;
    private RemoteSyncInterface remoteSyncInterface;
    private RemoteSyncInterface remoteSyncImpl;
    private String serverHostName;
    private int serverHostPort;
    private final boolean bUpdateSyncDelegate;

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
        if (bUpdateSyncDelegate) OASyncDelegate.setSyncClient(packagex, this);
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
                        if (!MultiplexerClient.DEBUG && !MultiplexerServer.DEBUG) {
                            getRemoteSession().update(clientInfo);
                        }
                        Thread.sleep(seconds * 1000);
                    }
                    catch (Exception e) {
                        break;
                    }
                }
            }
        }, "OASyncClient.updateClientInfo."+seconds);
        t.setDaemon(true);
        t.start();
    }
    
    public int cntGetDetail;
    /**
     * This works directly with ClientGetDetail, by using a customized objectSerializer
     * @param masterObject
     * @param propertyName
     * @return
     */
    public Object getDetail(final OAObject masterObject, final String propertyName) {
        //qqqqqvvvvv        
        //System.out.println("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName+", levels="+levels);
        //LOG.finer("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName);
        
        int cntx = ++cntGetDetail;        
        int xDup = OAObjectSerializeDelegate.cntDup;
        int xNew = OAObjectSerializeDelegate.cntNew;

        // LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        if (masterObject == null || propertyName == null) return null;

        boolean bGetSibs;
        OAObjectKey[] siblingKeys = null;
        String[] additionalMasterProperties = null; 
        Object result = null;

/*        
        if (OARemoteThreadDelegate.shouldMessageBeQueued()) {
            // this needs to be fast, and only request the single property
            bGetSibs = false;
            try {
                result = getRemoteClient().getDetail(masterObject.getClass(), masterObject.getObjectKey(), propertyName);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "getDetail error", e);
            }
        }
        else {
*/        
            // this will "ask" for additional data "around" the requested property
            bGetSibs = true;
            // send siblings to return back with same prop
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
            if (li == null || !li.getCalculated()) {
                siblingKeys = getDetailSiblings(masterObject, li, propertyName);
            }
  
            
            additionalMasterProperties = OAObjectReflectDelegate.getUnloadedReferences(masterObject, false, propertyName);
            try {
                if (OARemoteThreadDelegate.isRemoteThread()) {
                    // use annotated version that does not use the msg queue
                    result = getRemoteClient().getDetailNow(masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                            additionalMasterProperties, siblingKeys);
                }
                else {
                    result = getRemoteClient().getDetail(masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                            additionalMasterProperties, siblingKeys);
                }
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "getDetail error", e);
            }
//        }
        
        lastMasterObjects[lastMasterCnter++%lastMasterObjects.length] = masterObject;
        
        if (result instanceof OAObjectSerializer) {
            // see ClientGetDetail.getSerializedDetail(..)
            OAObjectSerializer os = (OAObjectSerializer) result;
            result = os.getObject();
            
            // the custom serializer can send extra objects, and might using objKey instead of the object. 
            Object objx = os.getExtraObject();
            
            if (objx instanceof HashMap) {
                HashMap<OAObjectKey, Object> hmExtraData = (HashMap<OAObjectKey, Object>) objx;
                for (Entry<OAObjectKey, Object> entry : hmExtraData.entrySet()) {
                    Object value = entry.getValue();
                    if (value == masterObject) continue;
                    if (!(value instanceof OAObject)) continue; // all hubs will be added to master props
                    
                    OAObject obj = OAObjectCacheDelegate.getObject(masterObject.getClass(), entry.getKey());
                    
                    // note:  only references that had an oaObjectKey that was not in the cache were in the sibling list
                    OAObject oaValue = (OAObject) value;
                    OAObjectPropertyDelegate.setPropertyCAS(obj, propertyName, oaValue, oaValue.getObjectKey(), false, false);
                }
            }
        }
        if (result instanceof Hub) {
            // 20141125 in case Hub.datam.masterObject needs to be set. (should not happen, since only GC of master could cause this)
            OAObjectHubDelegate.setMasterObject((Hub) result, masterObject, propertyName);
        }

        //qqqqqqq        
        if (true || OAObjectSerializeDelegate.cntNew-xNew > 25 || cntx % 100 == 0) {
            int iNew = OAObjectSerializeDelegate.cntNew; 
            int iDup = OAObjectSerializeDelegate.cntDup;
            
            String s = String.format(
                "%,d) OASyncClient.getDetail() Obj=%s, prop=%s, ref=%s, getSib=%b %,d, moreProps=%d, " +
                "newCnt=%,d, dupCnt=%,d, totNewCnt=%,d, totDupCnt=%,d",
                cntx, 
                masterObject, 
                propertyName, 
                result==null?"null":result.getClass().getName(),
                bGetSibs,
                (siblingKeys == null)?0:siblingKeys.length,
                additionalMasterProperties==null?0:additionalMasterProperties.length,
                iNew-xNew, 
                iDup-xDup,
                iNew, 
                iDup
            );
            //System.out.println(s);
            LOG.fine(s);
        }
        return result;
    }

    /**
     * Find any other siblings to get the same property for sibling objects in same hub.
     */
    protected OAObjectKey[] getDetailSiblings(final OAObject masterObject, final OALinkInfo linkInfo, final String property) {
        // note: could be for a blob property

        Hub siblingHub = null;
        final Hub hubThreadLocal = OAThreadLocalDelegate.getGetDetailHub();
        if (hubThreadLocal != null && hubThreadLocal.contains(masterObject)) {
            siblingHub = hubThreadLocal;
        }
        
        if (siblingHub == null) {
            siblingHub = findBestSiblingHub(masterObject);
            if (siblingHub == null) return null;
        }
        
        ArrayList<OAObjectKey> al = new ArrayList<OAObjectKey>();
        _getDetailSiblings(new HashSet(), al, masterObject, siblingHub, linkInfo, property, hubThreadLocal!=null);

        
        if (al == null || al.size() == 0) return null;
        int x = al.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        al.toArray(keys);
        return keys;
    }


    // find the Hub that has the best set of siblings
    private Hub findBestSiblingHub(OAObject masterObject) {
        Hub siblingHub = null;
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(masterObject);
        
        int siblingHits = 0;
        
        for (int i=0; (hubs != null && i < hubs.length); i++) {
            Hub hub = hubs[i];
            if (hub == null) continue;

            if (siblingHub == null) { 
                siblingHub = hub;
                continue;
            }
            
            if (hub.getSize() < 2) continue;
            
            // see if one of the previous objects can be found
            if (siblingHits == 0) {
                siblingHits = 1;  // so it wont be zero
                if (siblingHub.getMasterObject() != null) siblingHits++;
                for (OAObject objz : lastMasterObjects) {
                    if (objz == null) break;
                    if (masterObject.getClass().equals(objz.getClass())) {
                        if (siblingHub.contains(objz)) {
                            siblingHits++;
                        }
                    }
                }
            }
            
            int hits = 1;
            if (hub.getMasterObject() != null) hits++;
            for (OAObject objz : lastMasterObjects) {
                if (objz == null) break;
                if (masterObject.getClass().equals(objz.getClass())) {
                    if (hub.contains(objz)) {
                        hits++;
                    }
                }
            }
            
            if (hits > siblingHits) {
                siblingHits = hits;
                siblingHub = hub;
            }
            else if (hits == siblingHits) {
                if (hub.getSize() > siblingHub.getSize())  siblingHub = hub;
            }
        }
        return siblingHub;
    }
    
    
    
    private void _getDetailSiblings(HashSet<Object> hsValues, ArrayList<OAObjectKey> alResults, final OAObject masterObject, 
            final Hub siblingHub, OALinkInfo linkInfo, String propertyName, boolean bAgressive) {
        
        _getDetailSiblingsA(hsValues, alResults, masterObject, siblingHub, linkInfo, propertyName, bAgressive);
        if (alResults.size() > 25 || linkInfo == null) return;

        // go up to master.parent and get siblings from there
        OAObject parentMasterObject = siblingHub.getMasterObject();
        if (parentMasterObject == null) return;
        
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(siblingHub);
        if (li == null) return;
             
        OALinkInfo liRev = li.getReverseLinkInfo();
        if (liRev == null) return;
        if (liRev.getType() != OALinkInfo.MANY) return;
        
        Hub parentSiblingHub = findBestSiblingHub(parentMasterObject);
        if (parentSiblingHub == null) return;
        
        int pos = parentSiblingHub.getPos(parentMasterObject);
        
        if (pos < 0) pos = 0;
        else if (pos == 0) pos++;
        else {
            // might want to go before
            OAObject obj = (OAObject) parentSiblingHub.getAt(pos-1);
            if (!OAObjectPropertyDelegate.isPropertyLoaded(obj, liRev.getName())) {
                pos -= 20;
                if (pos < 0) pos = 0;
            }
            else {
                Object objx = liRev.getValue(obj);
                if (objx instanceof Hub) {
                    Hub h = (Hub) objx;
                    obj = (OAObject) h.getAt(0);
                    if (obj != null && !OAObjectPropertyDelegate.isPropertyLoaded(obj, liRev.getName())) {
                        pos -= 20;
                        if (pos < 0) pos = 0;
                    }
                    else pos++;
                }
            }
        }
        
        for (int i=0; i<250; i++) {
            Object obj = parentSiblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == parentMasterObject) continue;
            
            if (!OAObjectPropertyDelegate.isPropertyLoaded((OAObject)obj, liRev.getName())) continue;

            Hub h = (Hub) liRev.getValue(obj);
            if (h.getSize() > 0) {
                _getDetailSiblingsA(hsValues, alResults, masterObject, h, linkInfo, propertyName, bAgressive);
            }
            if (alResults.size() > 100) break;
        }        
    }    
    
    
    private void _getDetailSiblingsA(HashSet<Object> hsValues, ArrayList<OAObjectKey> al, 
            OAObject masterObject, Hub siblingHub, OALinkInfo linkInfo, String property, boolean bAgressive) {
        // get the same property for siblings

        // find best starting pos, either before or after
        int pos = siblingHub.getPos(masterObject);
        if (pos < 0) pos = 0;
        else if (pos == 0) pos++;
        else {
            // find out what direction to start at
            OAObject obj = (OAObject) siblingHub.getAt(pos-1);
            if (!OAObjectPropertyDelegate.isPropertyLoaded(obj, property)) {
                pos -= (linkInfo == null)?5:20;
                if (pos < 0) pos = 0;
            }
            else pos++;
        }
        
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

            OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
            Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, property, true, true);

            if (value instanceof OANotExist) {
                if (linkInfo == null) {  // must be blob
                    al.add(key);
                    if (al.size() >= 10) break;  // only get 10 extra blobs, ha
                }
                else if (bIsMany || bIsOne2One) {                
                    al.add(key);
                    if (al.size() >= (50*(bAgressive?2:1))) break;
                } 
                // otherwise, it must be null
            }
            else if (value instanceof OAObjectKey) {
                if (!hsValues.contains(value)) {
                    hsValues.add(value);
                    value = OAObjectCacheDelegate.get(valueClass, value);
                    if (value == null) { // not on client
                        al.add(key);
                        if (al.size() >= (100*(bAgressive?2:1))) break;
                    }
                }
            }
            // note: if value is null and a Many, then it's value is an empty Hub
        }
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
        clientInfo.setConnectionId(getMultiplexerClient().getConnectionId());
        
        LOG.fine("getting remote object for Client Session");
        getRemoteServer();
        getRemoteSync();
        getRemoteSession();
        getRemoteClient();
        if (bUpdateSyncDelegate) startQueueGuidThread();

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
        
        OASyncDelegate.setSyncClient(packagex, null);
        OASyncDelegate.setRemoteServer(packagex, null);
        OASyncDelegate.setRemoteSync(packagex, null);
        OASyncDelegate.setRemoteSession(packagex, null);
        OASyncDelegate.setRemoteClient(packagex, null);
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
            protected void onRemoteThreadCreated(int threadCount) {
                getClientInfo().setRemoteThreadCount(threadCount);
                super.onRemoteThreadCreated(threadCount);

                if (threadCount > (MAX_ThreadCount * .85)) {
                    long msNow = System.currentTimeMillis();
                    if (msLastThreadCountWarning + 1000 < msNow) {
                        msLastThreadCountWarning = msNow;
                        String s = OALogUtil.getThreadDump();
                        LOG.warning("RemoteThread count="+threadCount+", max="+MAX_ThreadCount+"\n"+s);
                    }
                    else {
                        // slow this thread down, giving others time to catch up before reading another message from queue
                        try {
                            Thread.currentThread().sleep(500);
                        }
                        catch (Exception e) {
                        }
                    }
                }
                if (threadCount >= MAX_ThreadCount) {
                    onRemoteThreadCountExceeded();
                }
            }                
        };
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

    /**
     * called when object is removed from object cache (called by oaObject.finalize)
     * @param bInServerSideCache if the object is in the serverSide cache.
     */
    public void objectRemoved(int guid) {
        try {
            if (guid > 0) {
                if (bUpdateSyncDelegate) queRemoveGuid.add(guid);
            }
        }
        catch (Exception e) {
        }
    }

    private LinkedBlockingQueue<Integer> queRemoveGuid;
    private Thread threadRemoveGuid;
    private void startQueueGuidThread() {
        if (queRemoveGuid != null) return;
        queRemoveGuid = new LinkedBlockingQueue<Integer>();
        threadRemoveGuid = new Thread(new Runnable() {
            long msLastError;
            int cntError;
            int[] guids = new int[50];
            @Override
            public void run() {
                RemoteSessionInterface rsi = null;
                for (int guidPos = 0;;) {
                    try {
                        int guid = queRemoveGuid.take(); 
                        guids[guidPos++ % 50] = guid;
                        if (guidPos % 50 == 0) {
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
}
