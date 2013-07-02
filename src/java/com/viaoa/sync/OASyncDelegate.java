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

import com.viaoa.sync.remote.*;

public class OASyncDelegate {
    private static Logger LOG = Logger.getLogger(OASyncDelegate.class.getName());
    
    private static RemoteServerInterface remoteServerInterface;
    private static RemoteSyncInterface remoteSyncInterface;
    private static RemoteClientInterface remoteClientInterface;
    private static RemoteClientSyncInterface remoteClientSyncInterface;
    private static OASyncClient syncClient;
    private static OASyncServer syncServer;
    
    public static OASyncClient getSyncClient() {
        return syncClient;
    }
    public static void setSyncClient(OASyncClient sc) {
        syncClient = sc;
    }
    public static OASyncServer getSyncServer() {
        return syncServer;
    }
    public static void setSyncServer(OASyncServer ss) {
        syncServer = ss;
    }
    
    public static void setRemoteClientInterface(RemoteClientInterface rci) {
        remoteClientInterface = rci;
    }
    public static RemoteClientInterface getRemoteClientInterface() {
        return remoteClientInterface;
    }
    public static void setRemoteClientSyncInterface(RemoteClientSyncInterface rci) {
        remoteClientSyncInterface = rci;
    }
    public static RemoteClientSyncInterface getRemoteClientSyncInterface() {
        return remoteClientSyncInterface;
    }
    public static void setRemoteServerInterface(RemoteServerInterface rsi) {
        remoteServerInterface = rsi;
    }
    public static RemoteServerInterface getRemoteServerInterface() {
        return remoteServerInterface;
    }

    public static void setRemoteSyncInterface(RemoteSyncInterface rsi) {
        remoteSyncInterface = rsi;
    }
    public static RemoteSyncInterface getRemoteSyncInterface() {
        return remoteSyncInterface;
    }
    
    public static boolean isServer() {
        return (syncServer != null) || (syncClient == null);
    }
    
    public static boolean isSingleUser() {
        return syncServer == null && syncClient == null;
    }
    
    public static boolean isConnected() {
        if (syncClient == null) {
            return (syncServer == null);
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
                    nextGuid = getRemoteServerInterface().getNextFiftyObjectGuids();
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
    
}
