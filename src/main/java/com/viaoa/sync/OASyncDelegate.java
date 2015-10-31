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

import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private static RemoteServerInterface remoteServer;
    
    /**
     * Used by OAObject/Hub CS methods to keep the model objects in sync across servers.
     */
    private static RemoteSyncInterface remoteSync;
    /**
     * Client side session methods.
     */
    private static RemoteSessionInterface remoteSession;
    /**
     * used to get data from the server.
     */
    private static RemoteClientInterface remoteClient;

    /**
     * Sync client that connects to the server and allows OAModels (OAObjects, Hub) to be automatically in sync.
     */
    private static OASyncClient syncClient;
    /**
     * Sync server that allows client connections, so that OAModels (OAObjects, Hub) are automatically in sync.
     */
    private static OASyncServer syncServer;

    private static OASyncCombinedClient syncCombinedClient;
    
    
    
    public static OASyncClient getSyncClient() {
        return syncClient;
    }
    public static void setSyncClient(OASyncClient sc) {
        syncClient = sc;
    }

    
    /**
     * The connectionId (multiplexer)
     */
    public static int getConnectionId() {
        if (syncClient == null) return 0;
        return syncClient.getConnectionId();
    }

    public static OASyncServer getSyncServer() {
        return syncServer;
    }
    /**
     * Set by OASyncServer.
     */
    public static void setSyncServer(OASyncServer ss) {
        syncServer = ss;
    }

    
    /**
     * Created by OASyncServer, and used by OASyncClient to then create a remote session on the server. 
     */
    public static void setRemoteServer(RemoteServerInterface rsi) {
        remoteServer = rsi;
    }
    public static RemoteServerInterface getRemoteServer() {
        return remoteServer;
    }
    
    /**
     * Set by OASyncClient after getting the remote session on the server.
     */
    public static void setRemoteSession(RemoteSessionInterface rci) {
        remoteSession = rci;
    }
    public static RemoteSessionInterface getRemoteSession() {
        return remoteSession;
    }
    /**
     * remote object from OASyncServer by calling/using RemoteSession. Set by OASyncClient, to get data from server.
     * This is internally used by OA to keep apps synchronized.
     */
    public static void setRemoteClient(RemoteClientInterface rci) {
        remoteClient = rci;
    }
    public static RemoteClientInterface getRemoteClient() {
        return remoteClient;
    }

    /**
     * Created by OASyncServer and used remotely by OASyncClient to keep OAObjects and Hubs in sync.
     */
    public static void setRemoteSync(RemoteSyncInterface rsi) {
        remoteSync = rsi;
    }
    public static RemoteSyncInterface getRemoteSync() {
        return remoteSync;
    }

    /**
     * @return if OASyncServer has been created.
     */
    public static boolean isServer() {
        return (syncServer != null) || (syncClient == null);
    }
    /**
     * @return if OASyncClient has been created.
     */
    public static boolean isClient() {
        return (syncServer == null && syncClient != null);
    }
    
    public static boolean isSingleUser() {
        return syncServer == null && syncClient == null;
    }

    /**
     * @return true if OASyncClient has been created and is connected to the server.
     */
    public static boolean isConnected() {
        if (syncClient == null) {
            return (syncServer != null);
        }
        return syncClient.isConnected();
    }


    private final static Object NextGuidLock = new Object();
    private static int nextGuid;
    private static int maxNextGuid;
    /**
        Used by OAObject so that object guid is created/managed on the server.
    */
    public static int getObjectGuid() {
        if (isServer()) {
            return OAObjectDelegate.getNextGuid();
        }
        int x;
        synchronized (NextGuidLock) {
            if (nextGuid == maxNextGuid) {
                try {
                    nextGuid = getRemoteServer().getNextFiftyObjectGuids();
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
    public static boolean beginServerOnly() {
        if (!isServer()) return false;
        sendMessages();
        return true;
    }
    /**
     * @see #beginServerOnly()
     */
    public static void endServerOnly() {
        sendMessages(false);
    }

    public static OASyncCombinedClient getSyncCombinedClient() {
        return syncCombinedClient;
    }
    public static void setSyncCombinedClient(OASyncCombinedClient cc) {
        syncCombinedClient = cc;
    }

}
