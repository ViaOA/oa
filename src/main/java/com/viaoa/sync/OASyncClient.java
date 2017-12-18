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

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubMerger;
import com.viaoa.object.OAFinder;
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
import com.viaoa.object.OAPerformance;
import com.viaoa.object.OAThreadLocalDelegate;
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
import com.viaoa.util.OADateTime;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OAPropertyPath;

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
        }, "OASyncClient.update."+seconds);
        t.setDaemon(true);
        t.start();
    }
    
    private final AtomicInteger aiCntGetDetail = new AtomicInteger();
    
    /**
     * This works on the server using ClientGetDetail, by using a customized objectSerializer
     * @param masterObject
     * @param propertyName
     * @return
     */
    public Object getDetail(final OAObject masterObject, final String propertyName) {
//System.out.println("OASyncClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName);
        LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        long ts = System.currentTimeMillis();
        
        final int cntx = aiCntGetDetail.incrementAndGet();        
        int xDup = OAObjectSerializeDelegate.cntDup;
        int xNew = OAObjectSerializeDelegate.cntNew;

        // LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        if (masterObject == null || propertyName == null) return null;

        boolean bGetSibs = false;
        OAObjectKey[] siblingKeys = null;
        String[] additionalMasterProperties = null; 
        Object result = null;

        OALinkInfo li = null;
        try {
            // both Hub && pp are set by HubMerger, HubGroupBy
            final Hub detailHub = OAThreadLocalDelegate.getGetDetailHub();
            final String detailPropertyPath = OAThreadLocalDelegate.getGetDetailPropertyPath();
            final boolean bUsesDetail = (detailHub != null && detailPropertyPath != null); 
            
            if (OARemoteThreadDelegate.isRemoteThread()) {
                    // use annotated version that does not use the msg queue
                result = getRemoteClient().getDetailNow(cntx, masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                        additionalMasterProperties, siblingKeys, bUsesDetail);
            }
            else {
                // this will "ask" for additional data "around" the requested property
                bGetSibs = true;
                // send siblings to return back with same prop
                li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
                if (li == null || !li.getCalculated()) {
                    siblingKeys = getDetailSiblings(masterObject, li, propertyName, detailHub, detailPropertyPath);
                }
                if (!bUsesDetail) {
                    additionalMasterProperties = OAObjectReflectDelegate.getUnloadedReferences(masterObject, false, propertyName);
                }
                
                result = getRemoteClient().getDetailNow(cntx, masterObject.getClass(), masterObject.getObjectKey(), propertyName, 
                        additionalMasterProperties, siblingKeys, bUsesDetail);
            }
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "getDetail error", e);
        }
        
        lastMasterObjects[lastMasterCnter++%lastMasterObjects.length] = masterObject;
        
        int cntSib = 0;
        if (result instanceof OAObjectSerializer) {
            // see ClientGetDetail.getSerializedDetail(..)
            OAObjectSerializer os = (OAObjectSerializer) result;

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

        if (true || OAObjectSerializeDelegate.cntNew-xNew > 25 || cntx % 100 == 0) {
            int iNew = OAObjectSerializeDelegate.cntNew; 
            int iDup = OAObjectSerializeDelegate.cntDup;
            
            ts = System.currentTimeMillis() - ts;
            String s = (ts > 750) ? " ALERT" : "";
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
                iNew-xNew, 
                iDup-xDup,
                iNew, 
                iDup,
                ts,
                s
            );
            OAPerformance.LOG.fine(s);
            LOG.fine(s);
        }
        return result;
    }

    /**
     * Find any other siblings to get the same property for sibling objects in same hub.
     */
    protected OAObjectKey[] getDetailSiblings(final OAObject masterObject, final OALinkInfo linkInfo, final String property, final Hub detailHub, final String detailPropertyPath) {
        // note: could be for a blob property

        if (detailHub != null && detailPropertyPath != null) {
            // set by HubMerger or HubGroupBy, where it will be loading from a Root Hub using a PropertyPath
            Hub h = detailHub;
            String spp = detailPropertyPath;
            
            OAPropertyPath pp = new OAPropertyPath(h.getObjectClass(), spp);
            spp = null;
            for (OALinkInfo li : pp.getLinkInfos()) {
                if (property.equalsIgnoreCase(li.getName())) break;
                if (spp == null) spp = li.getName();
                else spp += "." + li.getName();
            }
            
            OAFinder f = new OAFinder(h, spp) {
                int cnt;
                @Override
                protected boolean isUsed(OAObject obj) {
                    boolean b = !OAObjectPropertyDelegate.isPropertyLoaded(obj, property);
                    if (b) {
                        if (++cnt > 100) {
                            stop();
                        }
                    }
                    return b;
                }
            };
            f.setUseOnlyLoadedData(true);
            ArrayList al = f.find();
            if (al == null || al.size() == 0) return null;
            int x = al.size();
            OAObjectKey[] keys = new OAObjectKey[x];
            for (int i=0; i<x; i++) {
                keys[i] = ((OAObject)al.get(i)).getObjectKey();
            }
            return keys;
        }
        
        Hub siblingHub = null;
        if (detailHub != null && detailHub.contains(masterObject)) {
            siblingHub = detailHub;
        }
        if (siblingHub == null) {
            siblingHub = findBestSiblingHub(masterObject);
            if (siblingHub == null) return null;
        }
        
        ArrayList<OAObjectKey> al = new ArrayList<OAObjectKey>();
        _getDetailSiblings(new HashSet(), al, masterObject, siblingHub, linkInfo, property, detailHub!=null, detailHub, detailPropertyPath);
        
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
            final Hub siblingHub, OALinkInfo linkInfo, String propertyName, final boolean bAgressive, final Hub detailHub, final String detailPropertyPath) {
        
        final boolean bUsesDetail = detailHub != null && detailPropertyPath != null; 
        
        _getDetailSiblingsA(hsValues, alResults, masterObject, siblingHub, linkInfo, propertyName, bAgressive, detailHub, detailPropertyPath);
        if (alResults.size() > (bUsesDetail?200:25) || linkInfo == null) return;

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
                _getDetailSiblingsA(hsValues, alResults, masterObject, h, linkInfo, propertyName, bAgressive, detailHub, detailPropertyPath);
            }
            if (alResults.size() > (bUsesDetail?200:100)) break;
        }        
    }    
    
    
    private void _getDetailSiblingsA(HashSet<Object> hsValues, ArrayList<OAObjectKey> alResults, 
            OAObject masterObject, Hub siblingHub, OALinkInfo linkInfo, String property, boolean bAgressive, final Hub detailHub, final String detailPropertyPath) {
        // get the same property for siblings

        final boolean bUsesDetail = detailHub != null && detailPropertyPath != null; 
        
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
        
        for (int i=0; i<350; i++) {
            Object obj = siblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == masterObject) continue;

            OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
            Object value = OAObjectPropertyDelegate.getProperty((OAObject)obj, property, true, true);

            if (value instanceof OANotExist) {
                if (linkInfo == null) {  // must be blob
                    alResults.add(key);
                    if (alResults.size() >= 25) break;  // only get 25 extra blobs, ha
                }
                else if (bIsMany || bIsOne2One) {                
                    alResults.add(key);
                    if (bUsesDetail) {
                        if (alResults.size() > 200) break;
                    }
                    else if (alResults.size() >= (100*(bAgressive?2:1))) break;
                } 
                // otherwise, it must be null
            }
            else if (value instanceof OAObjectKey) {
                if (!hsValues.contains(value)) {
                    hsValues.add(value);
                    value = OAObjectCacheDelegate.get(valueClass, value);
                    if (value == null) { // not on client
                        alResults.add(key);
                        if (bUsesDetail) {
                            if (alResults.size() > 200) break;
                        }
                        else if (alResults.size() > (100*(bAgressive?2:1))) break;
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
        
        LOG.fine("multiplexer client connected, connectionId="+getMultiplexerClient().getConnectionId());
        
        clientInfo.setConnectionId(getMultiplexerClient().getConnectionId());

        if (bUpdateSyncDelegate) OASyncDelegate.setSyncClient(packagex, this);
        
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
     * called when object is removed from object cache (called by oaObject.finalize)
     * @param bInServerSideCache if the object is in the serverSide cache.
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
}
