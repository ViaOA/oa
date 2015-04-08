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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.remote.*;

public class OASyncDelegate {
    private static Logger LOG = Logger.getLogger(OASyncDelegate.class.getName());
    
    private static RemoteServerInterface remoteServer;
    private static RemoteSyncInterface remoteSync;
    private static RemoteSessionInterface remoteSession;
    private static RemoteClientInterface remoteClient;
    private static OASyncClient syncClient;
    private static OASyncServer syncServer;
    
    public static OASyncClient getSyncClient() {
        return syncClient;
    }
    public static void setSyncClient(OASyncClient sc) {
        syncClient = sc;
    }
    public static int getConnectionId() {
        if (syncClient == null) return 0;
        return syncClient.getConnectionId();
    }
    public static OASyncServer getSyncServer() {
        return syncServer;
    }
    public static void setSyncServer(OASyncServer ss) {
        syncServer = ss;
    }
    
    public static void setRemoteSession(RemoteSessionInterface rci) {
        remoteSession = rci;
    }
    public static RemoteSessionInterface getRemoteSession() {
        return remoteSession;
    }
    public static void setRemoteClient(RemoteClientInterface rci) {
        remoteClient = rci;
    }
    public static RemoteClientInterface getRemoteClient() {
        return remoteClient;
    }
    public static void setRemoteServer(RemoteServerInterface rsi) {
        remoteServer = rsi;
    }
    public static RemoteServerInterface getRemoteServer() {
        return remoteServer;
    }

    public static void setRemoteSync(RemoteSyncInterface rsi) {
        remoteSync = rsi;
    }
    public static RemoteSyncInterface getRemoteSync() {
        return remoteSync;
    }
    
    public static boolean isServer() {
        return (syncServer != null) || (syncClient == null);
    }
    public static boolean isClient() {
        return syncClient != null;
    }
    
    public static boolean isSingleUser() {
        return syncServer == null && syncClient == null;
    }
    
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
        Used by OAObject so that object guid is created on server.
    */
    public static int getObjectGuid() {
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

    public static boolean sendMessages() {
        return OARemoteThreadDelegate.sendMessages();
    }
    public static boolean sendMessages(boolean b) {
        return OARemoteThreadDelegate.sendMessages(b);
    }
    public static boolean isRemoteThread() {
        return OARemoteThreadDelegate.isRemoteThread();
    }
    public static boolean shouldSendMessages() {
        return OARemoteThreadDelegate.shouldSendMessages();
    }
    public static RequestInfo getRequestInfo() {
        return OARemoteThreadDelegate.getRequestInfo();
    }
    /**
     * If the current thread is processing a remote request, then this
     * will return the connection Id of the client.  If not, then -1 is returned.
     */
    public static int getRequestConnectionId() {
        RequestInfo ri = OARemoteThreadDelegate.getRequestInfo();
        if (ri == null) return -1;
        return ri.connectionId;
    }
}
