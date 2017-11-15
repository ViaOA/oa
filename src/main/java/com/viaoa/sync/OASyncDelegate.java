/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
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
 * OASync is a group of classes that use OAObject/Hub (observable) and RemoteMultiplexer classes
 * (distributed) to perform remote updating.
 *
 * Used for the OA distributed, synchronization that keeps OAObject/Models/Hubs/etc in sync across
 * multiple servers.
 * 
 * This is used to: determine if an app is a client or server, to know it's connection information, used
 * by OAObject for prop changes, etc, used by Hub for add/remove/etc changes, to know if the current
 * thread is an OARemoteThread that is processing a sync message, to get the current RequestInfo, which
 * is the currently executing remote method call
 *
 * This is internally used by OA to keep application models synchronized between the server and 0 or
 * more clients.
 * 
 * @author vvia
 */
public class OASyncDelegate {
    private static Logger LOG = Logger.getLogger(OASyncDelegate.class.getName());

    public static final Package ObjectPackage = Object.class.getPackage();

    /**
     * Used client to communicate with server.
     */
    private static final ConcurrentHashMap<Package, RemoteServerInterface> hmRemoteServer = new ConcurrentHashMap<Package, RemoteServerInterface>();
    private static RemoteServerInterface remoteServerInterface;

    /**
     * Used by OAObject/Hub CS methods to keep the model objects in sync across servers.
     */
    private static final ConcurrentHashMap<Package, RemoteSyncInterface> hmRemoteSync = new ConcurrentHashMap<Package, RemoteSyncInterface>();
    private static RemoteSyncInterface remoteSyncInterface;
    
    /**
     * Client side session methods.
     */
    private static final ConcurrentHashMap<Package, RemoteSessionInterface> hmRemoteSession = new ConcurrentHashMap<Package, RemoteSessionInterface>();
    private static RemoteSessionInterface remoteSessionInterface;

    /**
     * used to get data from the server.
     */
    private static final ConcurrentHashMap<Package, RemoteClientInterface> hmRemoteClient = new ConcurrentHashMap<Package, RemoteClientInterface>();
    private static RemoteClientInterface remoteClientInterface;

    /**
     * Sync client that connects to the server and allows OAModels (OAObjects, Hub) to be automatically
     * in sync.
     */
    private static final ConcurrentHashMap<Package, OASyncClient> hmSyncClient = new ConcurrentHashMap<Package, OASyncClient>();
    private static OASyncClient oaSyncClient;

    /**
     * Sync server that allows client connections, so that OAModels (OAObjects, Hub) are automatically
     * in sync.
     */
    private static final ConcurrentHashMap<Package, OASyncServer> hmSyncServer = new ConcurrentHashMap<Package, OASyncServer>();
    private static OASyncServer oaSyncServer;

    private static final ConcurrentHashMap<Class, Package> hmClassPackage = new ConcurrentHashMap<Class, Package>();

    public static Package getPackage(Class c) {
        Package p;
        if (c == null) p = ObjectPackage;
        else {
            p = hmClassPackage.get(c);
            if (p == null) {
                p = c.getPackage();
                hmClassPackage.put(c, p);
            }
        }
        return p;
    }

    // ========= SyncServer ============
    public static OASyncServer getSyncServer() {
        if (oaSyncServer != null) return oaSyncServer;
        return getSyncServer(getPackage(null));
    }
    public static OASyncServer getSyncServer(Class c) {
        if (oaSyncServer != null) return oaSyncServer;
        return getSyncServer(getPackage(c));
    }
    public static OASyncServer getSyncServer(OAObject obj) {
        if (oaSyncServer != null) return oaSyncServer;
        Class c = obj == null ? null : obj.getClass();
        return getSyncServer(getPackage(c));
    }
    public static OASyncServer getSyncServer(Hub h) {
        if (oaSyncServer != null) return oaSyncServer;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getSyncServer(getPackage(c));
    }
    public static OASyncServer getSyncServer(Package p) {
        if (oaSyncServer != null) return oaSyncServer;
        if (p == null) p = ObjectPackage;
        OASyncServer ss = hmSyncServer.get(p);
        if (ss == null && p != ObjectPackage) ss = hmSyncServer.get(ObjectPackage);
        return ss;
    }
    public static void setSyncServer(OASyncServer ss) {
        setSyncServer(null, ss);
    }
    public static void setSyncServer(Package p, OASyncServer ss) {
        if (p != null && p != ObjectPackage) {
            if (ss != null) {
                hmSyncServer.put(p, ss);
                if (hmSyncServer.get(ObjectPackage) == null) hmSyncServer.put(ObjectPackage, ss);
                if (oaSyncServer == null) oaSyncServer = ss; 
                else if (oaSyncServer != ss) oaSyncServer = null;
            }
            else {
                OASyncServer ssx = hmSyncServer.remove(p);
                if (hmSyncServer.get(ObjectPackage) == ssx) hmSyncServer.remove(ObjectPackage);
                if (oaSyncServer == ssx) oaSyncServer = null;
            }
        }
        else {
            p = ObjectPackage;
            if (ss != null) {
                if (oaSyncServer == null) oaSyncServer = ss; 
                else if (oaSyncServer != ss) oaSyncServer = null;
                hmSyncServer.put(p, ss);
            }
            else {
                OASyncServer ssx = hmSyncServer.remove(p);
                if (oaSyncServer == ssx) oaSyncServer = null;
            }
        }
    }

    // ========= SyncClient ============
    public static OASyncClient getSyncClient() {
        if (oaSyncClient != null) return oaSyncClient;
        return getSyncClient(getPackage(null));
    }
    public static OASyncClient getSyncClient(Class c) {
        if (oaSyncClient != null) return oaSyncClient;
        return getSyncClient(getPackage(c));
    }
    public static OASyncClient getSyncClient(OAObject obj) {
        if (oaSyncClient != null) return oaSyncClient;
        Class c = obj == null ? null : obj.getClass();
        return getSyncClient(getPackage(c));
    }
    public static OASyncClient getSyncClient(Hub h) {
        if (oaSyncClient != null) return oaSyncClient;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getSyncClient(getPackage(c));
    }
    public static OASyncClient getSyncClient(Package p) {
        if (oaSyncClient != null) return oaSyncClient;
        if (p == null) p = ObjectPackage;
        OASyncClient sc = hmSyncClient.get(p);
        if (sc == null && p != ObjectPackage) sc = hmSyncClient.get(ObjectPackage);
        return sc;
    }
    public static void setSyncClient(OASyncClient sc) {
        setSyncClient(null, sc);
    }
    public static void setSyncClient(Package p, OASyncClient sc) {
        if (p != null && p != ObjectPackage) {
            if (sc != null) {
                hmSyncClient.put(p, sc);
                if (hmSyncClient.get(ObjectPackage) == null) hmSyncClient.put(ObjectPackage, sc);
                if (oaSyncClient == null) oaSyncClient = sc; 
                else if (oaSyncClient != sc) oaSyncClient = null;
            }
            else {
                OASyncClient ssx = hmSyncClient.remove(p);
                if (hmSyncClient.get(ObjectPackage) == ssx) hmSyncClient.remove(ObjectPackage);
                if (oaSyncClient == sc) oaSyncClient = null;
            }
        }
        else {
            p = ObjectPackage;
            if (sc != null) {
                if (oaSyncClient == null) oaSyncClient = sc; 
                else if (oaSyncClient != sc) oaSyncClient = null;
                hmSyncClient.put(p, sc);
            }
            else {
                OASyncClient scx = hmSyncClient.remove(p);
                if (oaSyncClient == scx) oaSyncClient = null;
            }
        }
    }

    // ========= RemoteServerInterface ============
    public static RemoteServerInterface getRemoteServer() {
        if (remoteServerInterface != null) return remoteServerInterface;
        return getRemoteServer(getPackage(null));
    }
    public static RemoteServerInterface getRemoteServer(Class c) {
        if (remoteServerInterface != null) return remoteServerInterface;
        return getRemoteServer(getPackage(c));
    }
    public static RemoteServerInterface getRemoteServer(OAObject obj) {
        if (remoteServerInterface != null) return remoteServerInterface;
        Class c = obj == null ? null : obj.getClass();
        return getRemoteServer(getPackage(c));
    }
    public static RemoteServerInterface getRemoteServer(Hub h) {
        if (remoteServerInterface != null) return remoteServerInterface;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getRemoteServer(getPackage(c));
    }
    public static RemoteServerInterface getRemoteServer(Package p) {
        if (remoteServerInterface != null) return remoteServerInterface;
        if (p == null) p = ObjectPackage;
        RemoteServerInterface rs = hmRemoteServer.get(p);
        if (rs == null && p != ObjectPackage) rs = hmRemoteServer.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteServer(RemoteServerInterface rs) {
        setRemoteServer(null, rs);
    }
    public static void setRemoteServer(Package p, RemoteServerInterface rs) {
        if (p != null && p != ObjectPackage) {
            if (rs != null) {
                hmRemoteServer.put(p, rs);
                if (hmRemoteServer.get(ObjectPackage) == null) hmRemoteServer.put(ObjectPackage, rs);
                if (remoteServerInterface == null) remoteServerInterface = rs; 
                else if (remoteServerInterface != rs) remoteServerInterface = null;
            }
            else {
                RemoteServerInterface rsx = hmRemoteServer.remove(p);
                if (hmRemoteServer.get(ObjectPackage) == rsx) hmRemoteServer.remove(ObjectPackage);
                if (remoteServerInterface == rsx) remoteServerInterface = null;
            }
        }
        else {
            p = ObjectPackage;
            if (rs != null) {
                hmRemoteServer.put(p, rs);
                if (remoteServerInterface == null) remoteServerInterface = rs; 
                else if (remoteServerInterface != rs) remoteServerInterface = null;
            }
            else {
                RemoteServerInterface rsx = hmRemoteServer.remove(p);
                if (remoteServerInterface == rsx) remoteServerInterface = null;
            }
        }
    }
    
    
    
    // ========= RemoteSessionInterface ============
    public static RemoteSessionInterface getRemoteSession() {
        if (remoteSessionInterface != null) return remoteSessionInterface;
        return getRemoteSession(getPackage(null));
    }
    public static RemoteSessionInterface getRemoteSession(Class c) {
        if (remoteSessionInterface != null) return remoteSessionInterface;
        return getRemoteSession(getPackage(c));
    }
    public static RemoteSessionInterface getRemoteSession(OAObject obj) {
        if (remoteSessionInterface != null) return remoteSessionInterface;
        Class c = obj == null ? null : obj.getClass();
        return getRemoteSession(getPackage(c));
    }
    public static RemoteSessionInterface getRemoteSession(Hub h) {
        if (remoteSessionInterface != null) return remoteSessionInterface;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getRemoteSession(getPackage(c));
    }
    public static RemoteSessionInterface getRemoteSession(Package p) {
        if (remoteSessionInterface != null) return remoteSessionInterface;
        if (p == null) p = ObjectPackage;
        RemoteSessionInterface rs = hmRemoteSession.get(p);
        if (rs == null && p != ObjectPackage) rs = hmRemoteSession.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteSession(RemoteSessionInterface rs) {
        setRemoteSession(null, rs);
    }
    public static void setRemoteSession(Package p, RemoteSessionInterface rs) {
        if (p != null && p != ObjectPackage) {
            if (rs != null) {
                hmRemoteSession.put(p, rs);
                if (hmRemoteSession.get(ObjectPackage) == null) hmRemoteSession.put(ObjectPackage, rs);
                if (remoteSessionInterface == null) remoteSessionInterface = rs; 
                else if (remoteSessionInterface != rs) remoteSessionInterface = null;
            }
            else {
                RemoteSessionInterface rsx = hmRemoteSession.remove(p);
                if (hmRemoteSession.get(ObjectPackage) == rsx) hmRemoteSession.remove(ObjectPackage);
                if (remoteSessionInterface == rsx) remoteSessionInterface = null;
            }
        }
        else {
            p = ObjectPackage;
            if (rs != null) {
                hmRemoteSession.put(p, rs);
                if (remoteSessionInterface == null) remoteSessionInterface = rs; 
                else if (remoteSessionInterface != rs) remoteSessionInterface = null;
            }
            else {
                RemoteSessionInterface rsx = hmRemoteSession.remove(p);
                if (remoteSessionInterface == rsx) remoteSessionInterface = null;
            }
        }
    }

    // ========= RemoteClientInterface ============
    public static RemoteClientInterface getRemoteClient() {
        if (remoteClientInterface != null) return remoteClientInterface;
        return getRemoteClient(getPackage(null));
    }
    public static RemoteClientInterface getRemoteClient(Class c) {
        if (remoteClientInterface != null) return remoteClientInterface;
        return getRemoteClient(getPackage(c));
    }
    public static RemoteClientInterface getRemoteClient(OAObject obj) {
        if (remoteClientInterface != null) return remoteClientInterface;
        Class c = obj == null ? null : obj.getClass();
        return getRemoteClient(getPackage(c));
    }
    public static RemoteClientInterface getRemoteClient(Hub h) {
        if (remoteClientInterface != null) return remoteClientInterface;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getRemoteClient(getPackage(c));
    }
    public static RemoteClientInterface getRemoteClient(Package p) {
        if (remoteClientInterface != null) return remoteClientInterface;
        if (p == null) p = ObjectPackage;
        RemoteClientInterface rs = hmRemoteClient.get(p);
        if (rs == null && p != ObjectPackage) rs = hmRemoteClient.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteClient(RemoteClientInterface rc) {
        setRemoteClient(null, rc);
    }
    public static void setRemoteClient(Package p, RemoteClientInterface rc) {
        if (p != null && p != ObjectPackage) {
            if (rc != null) {
                hmRemoteClient.put(p, rc);
                if (hmRemoteClient.get(ObjectPackage) == null) hmRemoteClient.put(ObjectPackage, rc);
                if (remoteClientInterface == null) remoteClientInterface = rc; 
                else if (remoteClientInterface != rc) remoteClientInterface = null;
                
            }
            else {
                RemoteClientInterface rcx = hmRemoteClient.remove(p);
                if (hmRemoteClient.get(ObjectPackage) == rcx) hmRemoteClient.remove(ObjectPackage);
                if (remoteClientInterface == rcx) remoteClientInterface = null;
            }
        }
        else {
            p = ObjectPackage;
            if (rc != null) {
                hmRemoteClient.put(p, rc);
                if (remoteClientInterface == null) remoteClientInterface = rc; 
                else if (remoteClientInterface != rc) remoteClientInterface = null;
            }
            else {
                RemoteClientInterface rcx = hmRemoteClient.remove(p);
                if (remoteClientInterface == rcx) remoteClientInterface = null;
            }
        }
    }

    // ========= RemoteSyncInterface ============
    public static RemoteSyncInterface getRemoteSync() {
        if (remoteSyncInterface != null) return remoteSyncInterface;
        return getRemoteSync(getPackage(null));
    }
    public static RemoteSyncInterface getRemoteSync(Class c) {
        if (remoteSyncInterface != null) return remoteSyncInterface;
        return getRemoteSync(getPackage(c));
    }
    public static RemoteSyncInterface getRemoteSync(OAObject obj) {
        if (remoteSyncInterface != null) return remoteSyncInterface;
        Class c = obj == null ? null : obj.getClass();
        return getRemoteSync(getPackage(c));
    }
    public static RemoteSyncInterface getRemoteSync(Hub h) {
        if (remoteSyncInterface != null) return remoteSyncInterface;
        Class c;
        if (h != null) c = h.getObjectClass();
        else c = null;
        return getRemoteSync(getPackage(c));
    }
    public static RemoteSyncInterface getRemoteSync(Package p) {
        if (remoteSyncInterface != null) return remoteSyncInterface;
        if (p == null) p = ObjectPackage;
        RemoteSyncInterface rs = hmRemoteSync.get(p);
        if (rs == null && p != ObjectPackage) rs = hmRemoteSync.get(ObjectPackage);
        return rs;
    }
    public static void setRemoteSync(RemoteSyncInterface rs) {
        setRemoteSync(null, rs);
    }
    public static void setRemoteSync(Package p, RemoteSyncInterface rs) {
        if (p != null && p != ObjectPackage) {
            if (rs != null) {
                hmRemoteSync.put(p, rs);
                if (hmRemoteSync.get(ObjectPackage) == null) hmRemoteSync.put(ObjectPackage, rs);
                if (remoteSyncInterface == null) remoteSyncInterface = rs; 
                else if (remoteSyncInterface != rs) remoteSyncInterface = null;
            }
            else {
                RemoteSyncInterface rsx = hmRemoteSync.remove(p);
                if (hmRemoteSync.get(ObjectPackage) == rsx) hmRemoteSync.remove(ObjectPackage);
                if (remoteSyncInterface == rsx) remoteSyncInterface = null;
            }
        }
        else {
            p = ObjectPackage;
            if (rs != null) {
                hmRemoteSync.put(p, rs);
                if (remoteSyncInterface == null) remoteSyncInterface = rs; 
                else if (remoteSyncInterface != rs) remoteSyncInterface = null;
            }
            else {
                RemoteSyncInterface rsx = hmRemoteSync.remove(p);
                if (remoteSyncInterface == rsx) remoteSyncInterface = null;
            }
        }
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
        if (oaSyncServer != null) return true;
        return isServer((Class) null);
    }

    public static boolean isServer(Class c) {
        if (c == null) return isServer((Package) null);
        return isServer(getPackage(c));
    }

    public static boolean isServer(OAObject obj) {
        if (obj == null) return isServer((Package) null);
        return isServer(getPackage(obj.getClass()));
    }

    public static boolean isServer(Hub h) {
        if (h != null) {
            Class c = h.getObjectClass();
            if (c != null) {
                return isServer(getPackage(c));
            }
        }
        return isServer((Package) null);
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
        return isClient(getPackage(c));
    }

    public static boolean isClient(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = getSyncServer(p);
        OASyncClient sc = getSyncClient(p);
        return (ss == null && sc != null);
    }

    public static boolean isClient(OAObject obj) {
        if (obj == null) return isClient((Package) null);
        return isClient(getPackage(obj.getClass()));
    }

    public static boolean isSingleUser() {
        return isSingleUser((Class) null);
    }
    public static boolean isSingleUser(Class c) {
        return isSingleUser(getPackage(c));
    }
    public static boolean isSingleUser(Hub h) {
        return isSingleUser(getPackage(h==null?null:h.getObjectClass()));
    }
    public static boolean isSingleUser(Package p) {
        if (p == null) p = ObjectPackage;
        OASyncServer ss = getSyncServer(p);
        OASyncClient sc = getSyncClient(p);
        return (ss == null && sc == null);
    }

    public static boolean isConnected() {
        return isConnected((Package)null);
    }
    public static boolean isConnected(Class c) {
        return isConnected(getPackage(c));
    }
    public static boolean isConnected(OAObject obj) {
        return isConnected(getPackage(obj==null?null:obj.getClass()));
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
     * Used by OAObject so that object guid is created/managed on the server.
     */
    public static int getObjectGuid(Class c) {
        if (c == null) return getObjectGuid((Package) null);
        return getObjectGuid(getPackage(c));
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
     * If the currentThread is an OARemoteThead, then this is used to have sync changes (OAObject/Hub)
     * sent to other computers.
     * 
     * By default, all msgs processed by OARemoteThreads will not send out any sync changes to other
     * computers (since they will receive the same msg).
     * 
     * This will set a flag in the current OARemoteThread to allow any further changes during the
     * current msg processing to be sent to the server/other clients.
     * 
     * @see OARemoteThread
     */
    public static boolean sendMessages() {
        return OARemoteThreadDelegate.sendMessages();
    }

    public static boolean sendMessages(boolean b) {
        return OARemoteThreadDelegate.sendMessages(b);
    }

    /**
     * Used to determine if the current thread is OARemoteThread, which is used to process sync
     * messages.
     */
    public static boolean isRemoteThread() {
        return OARemoteThreadDelegate.isRemoteThread();
    }

    /**
     * Checks to see if any sync changes will be sent to other computers. This will be true if the
     * current thread is not an OARemoteThread, or if sendMessages([true]) was set.
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
     * If the current thread is an OARemoteThread, then this will return information about the currently
     * processed sync message.
     */
    public static RequestInfo getRequestInfo() {
        return OARemoteThreadDelegate.getRequestInfo();
    }

    /**
     * If the current thread is an OARemoteThread, then this will return the connection Id of the
     * client. If not, then -1 is returned.
     */
    public static int getRequestConnectionId() {
        RequestInfo ri = OARemoteThreadDelegate.getRequestInfo();
        if (ri == null) return -1;
        return ri.connectionId;
    }

    /**
     * used to create a block of code that will only process on the server. Send messages if this is the
     * server.
     * 
     * example: if (!OASync.beginServerOnly()) return; ... OASync.endServerOnly();
     * 
     * @return true if this is the server, else false.
     * @see #endServerOnly()
     * 
     *      public static boolean beginServerOnly(Package p) { if (!isServer(p)) return false;
     *      sendMessages(true); return true; } / **
     * @see #beginServerOnly() / public static boolean endServerOnly(Package p) { if (!isServer(p))
     *      return false; sendMessages(false); return true; }
     */

    /* later private static OASyncCombinedClient syncCombinedClient; public static OASyncClient
     * getSyncClient() { if (syncCombinedClient != null) { OASyncClient sc =
     * syncCombinedClient.getCurrentThreadSyncClient(); if (sc != null) return sc; } return syncClient;
     * }
     * 
     * public static OASyncCombinedClient getSyncCombinedClient() { return syncCombinedClient; } public
     * static void setSyncCombinedClient(OASyncCombinedClient cc) { syncCombinedClient = cc; } */

    public static void setLoading() {
        OAThreadLocalDelegate.setLoading(true);
    }
    public static void setLoading(boolean b) {
        OAThreadLocalDelegate.setLoading(b);
    }
}
