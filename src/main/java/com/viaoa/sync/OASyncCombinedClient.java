package com.viaoa.sync;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.*;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;

/**
 * This is used to have multiple servers all combined into a central server.
 * It's assumed that each has the same object model, but that they each have their 
 * own instances.  The object keys are only unique to each server, so the combined server
 * will have a new unique key, and this class will handle the mapping between the two. 
 * @author vvia
 *
 */
public class OASyncCombinedClient {
    private static Logger LOG = Logger.getLogger(OASyncCombinedClient.class.getName());

    // client that is connected to the combined server.
    private final OASyncClient syncClient;

    // mapping between the object on each server and the combined server
    private ConcurrentHashMap<RemoteMultiplexerClient, ClientSession> hmClientSession = new ConcurrentHashMap<RemoteMultiplexerClient, ClientSession>();
    
    private class ClientSession {
        RemoteMultiplexerClient rmClient;
        ConcurrentHashMap<Class, Mapper> hmClassToMapper = new ConcurrentHashMap<Class, OASyncCombinedClient.Mapper>();
    }
    private class Mapper {
        ConcurrentHashMap<OAObjectKey, OAObjectKey> hmClientToServer = new ConcurrentHashMap<OAObjectKey, OAObjectKey>();
        ConcurrentHashMap<OAObjectKey, OAObjectKey> hmServerToClient = new ConcurrentHashMap<OAObjectKey, OAObjectKey>();
    }
    

    /**
     * @param syncClient client to the combined server.
     */
    public OASyncCombinedClient(OASyncClient syncClient) {
        if (syncClient == null) throw new IllegalArgumentException("syncClient can not be null");
        this.syncClient = syncClient;
        OASyncDelegate.setSyncCombinedClient(this);
    }
  
    public OASyncClient createSyncClient(String hostName, int port) {
        OASyncClient sc = new OASyncClient(hostName, port, false) {
            RemoteSyncInterface remoteSync;
            
            // redirect changes from one server to the combined server
            @Override
            public RemoteSyncInterface getRemoteSyncImpl() throws Exception {
                if (remoteSync != null) return remoteSync;
                
                remoteSync = new RemoteSyncInterface() {
                    @Override
                    public boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp) {
                        return true;
                    }
                    @Override
                    public boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX) {
                        OAObjectKey k1 = getClientToServerKey(objectClass, objectKey);
                        if (k1 == null) return false;

                        OAObjectKey k2 = getClientToServerKey(objectClassX, objectKeyX);
                        if (k2 == null) return false;
                            
                        try {
                            syncClient.getRemoteSync().removeFromHub(objectClass, k1, hubPropertyName, objectClassX, k2);
                        }
                        catch (Exception e) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
                        OAObjectKey k1 = getClientToServerKey(objectClass, objectKey);
                        if (k1 == null) return false;

                        try {
                            syncClient.getRemoteSync().removeAllFromHub(objectClass, k1, hubPropertyName);
                        }
                        catch (Exception e) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
                        OAObjectKey k1 = getClientToServerKey(objectClass, origKey);
                        if (k1 == null) return false;
                        try {
                            syncClient.getRemoteSync().propertyChange(objectClass, k1, propertyName, newValue, bIsBlob);
                        }
                        catch (Exception e) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        return true;
                    }
                    
                    @Override
                    public boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, int posFrom, int posTo) {
                        return false;
                    }
                    
                    @Override
                    public boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos) {
                        OAObjectKey k1 = getClientToServerKey(masterObjectClass, masterObjectKey);
                        if (k1 == null) return false;

                        try {
                            syncClient.getRemoteSync().insertInHub(masterObjectClass, k1, hubPropertyName, obj, pos);
                        }
                        catch (Exception e) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        return true;
                    }
                    
                    @Override
                    public void clearHubChanges(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName) {
                    }
                    
                    @Override
                    public boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj) {
                        OAObjectKey k1 = getClientToServerKey(masterObjectClass, masterObjectKey);
                        if (k1 == null) return false;

                        try {
                            syncClient.getRemoteSync().addToHub(masterObjectClass, k1, hubPropertyName, obj);
                        }
                        catch (Exception e) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        return true;
                    }
                };
                return remoteSync;
            }
        };
        return sc;
    }
    
    
    
    
    /**
     * Called from OAObjectSerialization.resolveObject, to get the correct object that is used on the
     * combined server.
     * @param objClient
     * @return null if this is not used, otherwise it will change the object with new Id for combined server.
     */
    public OAObject resolveObject(final OAObject objClient) {

        RemoteMultiplexerClient rmc = OAThreadLocalDelegate.getRemoteMultiplexerClient();
        
        if (rmc == null || rmc == syncClient.getRemoteMultiplexerClient()) {
            //qqqqqqqqqqq from Server to Client(s)  - there might not be any clients
            return null;
        }

        ClientSession session = hmClientSession.get(rmc);
        if (session == null) {
            synchronized (hmClientSession) {
                session = hmClientSession.get(objClient.getClass());
                if (session == null) {
                    session = new ClientSession();
                    session.rmClient = rmc;
                    hmClientSession.put(rmc, session);
                }                
            }
        }

        // from Client to Server
        Mapper mapper = session.hmClassToMapper.get(objClient.getClass());
        if (mapper == null) {
            synchronized (session.hmClassToMapper) {
                mapper = session.hmClassToMapper.get(objClient.getClass());
                if (mapper == null) {
                    mapper = new Mapper();
                    session.hmClassToMapper.put(objClient.getClass(), mapper);
                }
            }
        }
        
        OAObject objServer;
        OAObjectKey keyClient = objClient.getObjectKey();
        OAObjectKey keyServer = mapper.hmClientToServer.get(keyClient);
        
        // if null create new obj for server
        if (keyServer == null) {
            OAObjectDelegate.setAsNew(objClient);
            objServer = objClient;
//qqqqq need to know when key is changed on server and then update the map            
            keyServer = objClient.getObjectKey();
            mapper.hmClientToServer.put(keyClient, keyServer);
            mapper.hmServerToClient.put(keyServer, keyClient);
        }
        else {
            objServer = OAObjectCacheDelegate.get(objClient.getClass(), keyServer);
            if (objServer == null) {
                // get from server
                objServer = (OAObject) OADataSource.getObject(getClass(), keyServer);
            }
        }
        return objServer;
    }

        
//qqqqqqqqq also        
//        oi.getImportMatchProperties();
    
    
    // get the key that was created for the combined server
    public OAObjectKey getClientToServerKey(final Class c, final OAObjectKey keyClient) {
        
        RemoteMultiplexerClient rmc = OAThreadLocalDelegate.getRemoteMultiplexerClient();
        
        if (rmc == null || rmc == syncClient.getRemoteMultiplexerClient()) {
            //qqqqqqqqqqq from Server to Client(s)  - there might not be any clients
            return null;
        }
        
        ClientSession session = hmClientSession.get(rmc);
        if (session == null) {
            synchronized (hmClientSession) {
                session = hmClientSession.get(rmc);
                if (session == null) {
                    session = new ClientSession();
                    session.rmClient = rmc;
                    hmClientSession.put(rmc, session);
                }                
            }
        }
        
        Mapper mapper = session.hmClassToMapper.get(c);
        if (mapper == null) {
            synchronized (session.hmClassToMapper) {
                mapper = session.hmClassToMapper.get(c);
                if (mapper == null) {
                    mapper = new Mapper();
                    session.hmClassToMapper.put(c, mapper);
                }
            }
        }
        
        OAObjectKey keyServer = mapper.hmClientToServer.get(keyClient);
        return keyServer;
    }
}
