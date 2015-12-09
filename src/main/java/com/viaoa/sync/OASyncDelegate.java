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

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.remote.*;

/**
 * OASync is a group of classes that use OAObject/Hub (observable) and RemoteMultiplexer classes (distributed) to perform remote updating.
 *
 * Used for the OA distributed, synchronization that keeps OAObject/Models/Hubs/etc in sync 
 * across multiple servers.
 * 
 * This is used to:
 *    determine if an app is a client or server, 
 *    to know it's connection information,
 *    used by OAObject for prop changes, etc,
 *    used by Hub for add/remove/etc changes,
 *    to know if the current thread is an OARemoteThread that is processing a sync message,
 *    to get the current RequestInfo, which is the currently executing remote method call 
 *
 * This is internally used by OA to keep application models synchronized between the server and 0 or more clients.
 * @author vvia
 */
public class OASyncDelegate {
    private static Logger LOG = Logger.getLogger(OASyncDelegate.class.getName());

    public static final Package ObjectPackage = Object.class.getPackage();
    
    /**
     * Used client to communicate with server.
     */
    private static final ConcurrentHashMap<Package, RemoteServerInterface> hmRemoteServer = new ConcurrentHashMap<Package, RemoteServerInterface>();
    
    /**
     * Used by OAObject/Hub CS methods to keep the model objects in sync across servers.
     */
    private static final ConcurrentHashMap<Package, RemoteSyncInterface> hmRemoteSync = new ConcurrentHashMap<Package, RemoteSyncInterface>();
    
    /**
     * Client side session methods.
     */
    private static final ConcurrentHashMap<Package, RemoteSessionInterface> hmRemoteSession = new ConcurrentHashMap<Package, RemoteSessionInterface>();

    /**
     * used to get data from the server.
     */
    private static final ConcurrentHashMap<Package, RemoteClientInterface> hmRemoteClient = new ConcurrentHashMap<Package, RemoteClientInterface>();

    /**
     * Sync client that connects to the server and allows OAModels (OAObjects, Hub) to be automatically in sync.
     */
    private static final ConcurrentHashMap<Package, OASyncClient> hmSyncClient = new ConcurrentHashMap<Package, OASyncClient>();
    
    
    /**
     * Sync server that allows client connections, so that OAModels (OAObjects, Hub) are automatically in sync.
     */
    private static final ConcurrentHashMap<Package, OASyncServer> hmSyncServer = new ConcurrentHashMap<Package, OASyncServer>();

    public static OASyncServer getSyncServer() {
        return getSyncServer((Package) null);
    }
    public static OASyncServer getSyncServer(Class c) {
        if (c == null) return getSyncServer((Package) null);
        return getSyncServer(c.getPackage());
    }
    public static OASyncServer getSyncServer(Hub h) {
        if (h == null) return getSyncServer((Package) null);
        return getSyncServer(h.getObjectClass().getPackage());
    }
    public static OASyncServer getSyncServer(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = hmSyncServer.get(p);
        if (ss == null) ss = hmSyncServer.get(ObjectPackage);
        return ss;
    }
    public static void setSyncServer(Package p, OASyncServer ss) {
        if (p == null) p = ObjectPackage;
        if (ss != null)  hmSyncServer.put(p, ss);
    }
    
    public static OASyncClient getSyncClient() {
        return getSyncClient((Package) null);
    }
    public static OASyncClient getSyncClient(Class c) {
        if (c == null) return getSyncClient((Package) null);
        return getSyncClient(c.getPackage());
    }
    public static OASyncClient getSyncClient(Hub h) {
        if (h == null) return getSyncClient((Package) null);
        return getSyncClient(h.getObjectClass().getPackage());
    }
    public static OASyncClient getSyncClient(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncClient sc = hmSyncClient.get(p);
        if (sc == null) sc = hmSyncClient.get(ObjectPackage);
        return sc;
    }
    public static void setSyncClient(Package p, OASyncClient sc) {
        if (p == null) p = ObjectPackage;
        if (sc != null) hmSyncClient.put(p, sc);
    }

    
    public static RemoteServerInterface getRemoteServer(Class c) {
        if (c == null) return getRemoteServer((Package) null);
        return getRemoteServer(c.getPackage());
    }
    public static RemoteServerInterface getRemoteServer(Hub h) {
        if (h == null) return getRemoteServer((Package) null);
        return getRemoteServer(h.getObjectClass().getPackage());
    }
    /**
     * Created by OASyncServer, and used by OASyncClient to then create a remote session on the server. 
     */
    public static RemoteServerInterface getRemoteServer(Package p) {
        if (p == null) p = ObjectPackage;
        RemoteServerInterface rs = hmRemoteServer.get(p);
        if (rs == null) rs = hmRemoteServer.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteServer(Package p, RemoteServerInterface ss) {
        if (p == null) p = ObjectPackage;
        if (ss != null) hmRemoteServer.put(p, ss);
    }

    
    public static RemoteSessionInterface getRemoteSession() {
        return getRemoteSession((Package) null);
    }
    public static RemoteSessionInterface getRemoteSession(Class c) {
        if (c == null) return getRemoteSession((Package) null);
        return getRemoteSession(c.getPackage());
    }
    public static RemoteSessionInterface getRemoteSession(Hub h) {
        if (h == null) return getRemoteSession((Package) null);
        return getRemoteSession(h.getObjectClass().getPackage());
    }
    /**
     * Set by OASyncClient after getting the remote session on the server.
     */
    public static RemoteSessionInterface getRemoteSession(Package p) {
        if (p == null) p = ObjectPackage;
        RemoteSessionInterface rs = hmRemoteSession.get(p);
        if (rs == null) rs = hmRemoteSession.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteSession(Package p, RemoteSessionInterface rs) {
        if (p == null) p = ObjectPackage;
        if (rs != null) hmRemoteSession.put(p, rs);
    }

    public static RemoteClientInterface getRemoteClient(Class c) {
        if (c == null) getRemoteClient((Package) null);
        return getRemoteClient(c.getPackage());
    }
    public static RemoteClientInterface getRemoteClient(Hub h) {
        if (h == null) getRemoteClient((Package) null);
        return getRemoteClient(h.getObjectClass().getPackage());
    }
    /**
     * remote object from OASyncServer by calling/using RemoteSession. Set by OASyncClient, to get data from server.
     * This is internally used by OA to keep apps synchronized.
     */
    public static RemoteClientInterface getRemoteClient(Package p) {
        if (p == null) p = ObjectPackage;
        RemoteClientInterface rc = hmRemoteClient.get(p);
        if (rc == null) rc = hmRemoteClient.get(ObjectPackage);
        return rc;
    }
    public static void setRemoteClient(Package p, RemoteClientInterface rc) {
        if (p == null) p = ObjectPackage;
        if (rc != null) hmRemoteClient.put(p, rc);
    }
    

    public static RemoteSyncInterface getRemoteSync(Class c) {
        if (c == null) return getRemoteSync((Package) null);
        return getRemoteSync(c.getPackage());
    }
    public static RemoteSyncInterface getRemoteSync(Hub h) {
        if (h == null) return getRemoteSync((Package) null);
        return getRemoteSync(h.getObjectClass().getPackage());
    }
    /**
     * Created by OASyncServer and used remotely by OASyncClient to keep OAObjects and Hubs in sync.
     */
    public static RemoteSyncInterface getRemoteSync(Package p) {
        if (p == null) p = ObjectPackage;
        RemoteSyncInterface rs = hmRemoteSync.get(p);
        if (rs == null) rs = hmRemoteSync.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteSync(Package p, RemoteSyncInterface rs) {
        if (p == null) p = ObjectPackage;
        if (rs != null) hmRemoteSync.put(p, rs);
    }
    public static void setRemoteSync(RemoteSyncInterface rs) {
        setRemoteSync((Package)null, rs);
    }


    /**
     * The connectionId (multiplexer)
     */
    public static int getConnectionId(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncClient sc = getSyncClient(p);
        if (sc == null) return -1;
        return sc.getConnectionId();
    }
    public static int getConnectionId() {
        return getConnectionId((Package) null);
    }
    
    
    /**
     * @return if OASyncServer has been created.
     */
    public static boolean isServer() {
        return isServer((Package) null);
    }
    public static boolean isServer(Class c) {
        if (c == null) return isServer((Package) null);
        return isServer(c.getPackage());
    }
    public static boolean isServer(OAObject obj) {
        if (obj == null) return isServer((Package) null);
        return isServer(obj.getClass().getPackage());
    }
    public static boolean isServer(Hub h) {
        if (h == null) return isServer((Package) null);
        return isServer(h.getObjectClass().getPackage());
    }
    public static boolean isServer(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = getSyncServer(p);
        OASyncClient sc = getSyncClient(p);
        return (ss != null) || (sc == null);
    }
    
    /**
     * @return if OASyncClient has been created.
     */
    public static boolean isClient(Class c) {
        if (c == null) return isClient((Package) null);
        return isClient(c.getPackage());
    }
    public static boolean isClient(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = getSyncServer(p);
        OASyncClient sc = getSyncClient(p);
        return (ss == null && sc != null);
    }
    public static boolean isClient(OAObject obj) {
        if (obj == null) return isClient((Package) null);
        return isClient(obj.getClass().getPackage());
    }
    
    public static boolean isSingleUser() {
        return isSingleUser((Class) null);
    }
    public static boolean isSingleUser(Class c) {
        if (c == null) return isSingleUser((Package) null);
        return isSingleUser(c.getPackage());
    }
    public static boolean isSingleUser(Hub h) {
        if (h == null) return isSingleUser((Package) null);
        return isSingleUser(h.getObjectClass().getPackage());
    }
    public static boolean isSingleUser(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = getSyncServer(p);
        OASyncClient sc = getSyncClient(p);
        return (ss == null && sc == null);
    }

    public static boolean isConnected() {
        return isConnected(null);
    }
    
    /**
     * @return true if OASyncClient has been created and is connected to the server.
     */
    public static boolean isConnected(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncClient sc = getSyncClient(p);

        if (sc == null) {
            OASyncServer ss = getSyncServer(p);
            return (ss != null);
        }
        return sc.isConnected();
    }


    private final static Object NextGuidLock = new Object();
    private static int nextGuid;
    private static int maxNextGuid;
    /**
        Used by OAObject so that object guid is created/managed on the server.
    */
    public static int getObjectGuid(Class c) {
        if (c == null) return getObjectGuid((Package) null); 
        return getObjectGuid(c.getPackage());
    }
    public static int getObjectGuid(Package p) {
        if (p == null) p = ObjectPackage;
        if (isServer(p)) {
            return OAObjectDelegate.getNextGuid();
        }
        int x;
        synchronized (NextGuidLock) {
            if (nextGuid == maxNextGuid) {
                try {
                    nextGuid = getRemoteServer(p).getNextFiftyObjectGuids();
                    maxNextGuid = nextGuid + 50; 
                }
                catch (Exception ex) {
                    LOG.log(Level.WARNING, "", ex);
                    throw new RuntimeException("OAClient.getObjectGuid Error:", ex);
                }
            }
            x = nextGuid++;
        }
        return x;
    }

    /**
     * If the currentThread is an OARemoteThead, then this is used to have sync changes (OAObject/Hub) sent to other computers.
     * 
     * By default, all msgs processed by OARemoteThreads will not send out any sync changes
     * to other computers (since they will receive the same msg).
     * 
     * This will set a flag in the current OARemoteThread to allow any further changes during the current msg processing to be
     * sent to the server/other clients.
     * @see OARemoteThread
     */
    public static boolean sendMessages() {
        return OARemoteThreadDelegate.sendMessages();
    }
    public static boolean sendMessages(boolean b) {
        return OARemoteThreadDelegate.sendMessages(b);
    }
    /**
     * Used to determine if the current thread is OARemoteThread, which is used to process sync messages.
     */
    public static boolean isRemoteThread() {
        return OARemoteThreadDelegate.isRemoteThread();
    }
    /**
     * Checks to see if any sync changes will be sent to other computers.  
     * This will be true if the current thread is not an OARemoteThread, or if sendMessages([true]) was set. 
     */
    public static boolean shouldSendMessages() {
        return OARemoteThreadDelegate.shouldSendMessages();
    }
    
    public static void setSuppressCSMessages(boolean b) {
        OAThreadLocalDelegate.setSuppressCSMessages(b);
    }
    public static boolean getSuppressCSMessages() {
        return OAThreadLocalDelegate.isSuppressCSMessages();
    }

    
    /**
     * If the current thread is an OARemoteThread, then this will return information about the currently processed
     * sync message. 
     */
    public static RequestInfo getRequestInfo() {
        return OARemoteThreadDelegate.getRequestInfo();
    }
    /**
     * If the current thread is an OARemoteThread, then this
     * will return the connection Id of the client.  If not, then -1 is returned.
     */
    public static int getRequestConnectionId() {
        RequestInfo ri = OARemoteThreadDelegate.getRequestInfo();
        if (ri == null) return -1;
        return ri.connectionId;
    }

    /**
     * used to create a block of code that will only process on the server.
     * Send messages if this is the server.
     * 
     * example:
     * if (!OASync.beginServerOnly()) return;
     *   ...
     * OASync.endServerOnly();
     * 
     * @return true if this is the server, else false.
     * @see #endServerOnly()
     */
    public static boolean beginServerOnly(Package p) {
        if (!isServer(p)) return false;
        sendMessages();
        return true;
    }
    /**
     * @see #beginServerOnly()
     */
    public static void endServerOnly() {
        sendMessages(false);
    }

    /* later
    private static OASyncCombinedClient syncCombinedClient;
    public static OASyncClient getSyncClient() {
        if (syncCombinedClient != null) {
            OASyncClient sc = syncCombinedClient.getCurrentThreadSyncClient();
            if (sc != null) return sc;
        }
        return syncClient;
    }

    public static OASyncCombinedClient getSyncCombinedClient() {
        return syncCombinedClient;
    }
    public static void setSyncCombinedClient(OASyncCombinedClient cc) {
        syncCombinedClient = cc;
    }
    */
}
