package com.viaoa.sync;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.ds.OADataSource;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.*;
import com.viaoa.sync.remote.RemoteSyncInterface;


// 20151103 not completed, on hold
/**
 * This is used to have multiple servers all combined into a single combined instance.
 * This needs to run as a separate java instance, that has a oasyncclient connection for each of
 * individual server instances, and a oasyncclient connection to the combined server.
 *  
 * It's assumed that each has the same object model, but that they each have their 
 * own instances.  The object keys are only unique to each server, so the combined server
 * will have a new unique key, and this class will handle the mapping between the two.
 * 
 * How this works is by using the oasyncclients to redirect requests/messages to the server at the "other" side.
 *  
 * Note: all of the OASyncClients need to be created from this object, so that it can redirect. 
 *
 * Overview:
 * This will combine objects from many servers into one.  The objects will have a different objectId
 * on the source then what is created on the combined server - example:  each server could have an Employee#1
 * and the combined server will have Employee 1,2,3,4...
 * This class will map between the source and the combined.  
 * It will then act as a client between them all and send messages from one to the other.  When it does this,
 * it will need to use the map to get the correct objectId to use.
 * OAObjects readResolve from the source servers will change the objId for the combined server and then store
 * in a map.  Any changes that affect the objId will then update the map.
 * Example: Emp objects from a source server will be be sent to the combined server, and have new objectId assigned
 * on the combined server.
 * Any changes to these objects on the combined server will then be sent (through this class) to the source server.
 * Any changes on the source will be sent to the combined server.  If the id is changed, then the map will be updated.
 *  
 * @author vvia
 */
public class OASyncCombinedClient {
    private static Logger LOG = Logger.getLogger(OASyncCombinedClient.class.getName());

    // connection to the combined server.
    private OASyncClient syncClient;

    // mapping between the object on each server and the combined server
    private ConcurrentHashMap<RemoteMultiplexerClient, ClientSession> hmClientSession = new ConcurrentHashMap<RemoteMultiplexerClient, ClientSession>();
    
    private class ClientSession {
        OASyncClient syncClient;
        ConcurrentHashMap<Class, Mapper> hmClassToMapper = new ConcurrentHashMap<Class, OASyncCombinedClient.Mapper>();
        

        private final Object NextGuidLock = new Object();
        private int nextGuid;
        private int maxNextGuid;
        
        int getNextGuid() {
            int x = 0;
            synchronized (NextGuidLock) {
                if (nextGuid == maxNextGuid) {
                    try {
                        nextGuid = syncClient.getRemoteServer().getNextFiftyObjectGuids();
                        maxNextGuid = nextGuid + 50; 
                    }
                    catch (Exception ex) {
                        LOG.log(Level.WARNING, "", ex);
                    }
                }
                x = nextGuid++;
            }
            return x;
        }
    }
    private class Mapper {
        ConcurrentHashMap<OAObjectKey, OAObjectKey> hmClientToServer = new ConcurrentHashMap<OAObjectKey, OAObjectKey>();
        ConcurrentHashMap<OAObjectKey, OAObjectKey> hmServerToClient = new ConcurrentHashMap<OAObjectKey, OAObjectKey>();
    }
    
    /**
     * @param syncClient client to the combined server.
     */
    public OASyncCombinedClient() {
//        OASyncDelegate.setSyncCombinedClient(this);
    }

    /**
     * Find the client that is the source for the object on the combinedServer.
     */
    private ClientSession getClientSession(Class c, OAObjectKey okServer) {
        for (Map.Entry<RemoteMultiplexerClient, ClientSession> me : hmClientSession.entrySet()) { 
            Mapper m = me.getValue().hmClassToMapper.get(c);
            if (m == null) continue;
            if (m.hmServerToClient.get(okServer) != null) return me.getValue(); 
        }
        return null;
    }
    
    
    /**
     * SyncClient that is connected to combined server
     * @param hostName
     * @param port
     */
    public OASyncClient getCombinedSyncClient(Package packagex, String hostName, int port) {
        if (syncClient != null) return syncClient;
        syncClient = new OASyncClient(packagex, hostName, port, true) {
            RemoteSyncInterface remoteSync;

            // redirect changes from combined server to the correct server
            @Override
            public RemoteSyncInterface getRemoteSyncImpl() throws Exception {
                if (remoteSync != null) return remoteSync;
                
                remoteSync = new RemoteSyncInterface() {
                    
                    RemoteSyncInterface getRemoteSyncInterface(ClientSession cs) {
                        try {
                            return cs.syncClient.getRemoteSync();
                        }
                        catch (Exception e) {
                        }
                        return null;
                    }
                    
                    @Override
                    public boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp) {
                        return true;
                    }
                    @Override
                    public boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX) {
                        ClientSession cs = getClientSession(objectClass, objectKey);
                        if (cs == null) return false;
                        
                        Mapper m = cs.hmClassToMapper.get(objectClass);
                        if (m == null) return false;
                        
                        OAObjectKey k1 = m.hmServerToClient.get(objectKey);
                        if (k1 == null) return false;
                        
                        m = cs.hmClassToMapper.get(objectClassX);
                        if (m == null) return false;

                        OAObjectKey k2 = m.hmServerToClient.get(objectKeyX);
                        if (k2 == null) return false;

                        
                        RemoteSyncInterface rs = getRemoteSyncInterface(cs);
                        if (rs == null) return false;
                        
                        rs.removeFromHub(objectClass, k1, hubPropertyName, objectClassX, k2);
                        
                        return true;
                    }
                    
                    @Override
                    public boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
                        ClientSession cs = getClientSession(objectClass, objectKey);
                        if (cs == null) return false;
                        
                        Mapper m = cs.hmClassToMapper.get(objectClass);
                        if (m == null) return false;
                        
                        OAObjectKey k1 = m.hmServerToClient.get(objectKey);
                        if (k1 == null) return false;

                        RemoteSyncInterface rs = getRemoteSyncInterface(cs);
                        if (rs == null) return false;
                        
                        rs.removeAllFromHub(objectClass, k1, hubPropertyName);
                        return true;
                    }
                    
                    @Override
                    public boolean propertyChange(final Class objectClass, final OAObjectKey origServerKey, final String propertyName, Object newValue, final boolean bIsBlob) {
//qqqqqqq check to see if objectId is changed, if so then use the old value to find match
                        // and update the hm with new valeu
//qqqqqq dont send pkey changes                        
                        if (propertyName == null) return false;
                        
                        ClientSession clientSession = getClientSession(objectClass, origServerKey);
                        if (clientSession == null) return false;
                        
                        final Mapper mapper = clientSession.hmClassToMapper.get(objectClass);
                        if (mapper == null) return false;
                        
                        OAObjectKey clientKey = mapper.hmServerToClient.get(origServerKey);
                        if (clientKey == null) return false;

                        final OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objectClass);
                        if (oi == null) return false;
                        
                        if (oi.isIdProperty(propertyName)) {
                            // dont send pkey changes                        
                            // update key with new value, replace old with new in mapper
                            OAObjectKey newServerKey = new OAObjectKey(new Object[] {newValue}, origServerKey.getGuid(), origServerKey.isNew());
                            mapper.hmServerToClient.remove(origServerKey);
                            mapper.hmServerToClient.put(newServerKey, clientKey);
                        }
                        else if (propertyName.equalsIgnoreCase("new")) {
                            if (!(newValue instanceof Boolean)) return false;
                            mapper.hmServerToClient.remove(origServerKey);
                            OAObjectKey newServerKey = new OAObjectKey(origServerKey.getObjectIds(), origServerKey.getGuid(), ((Boolean)newValue).booleanValue());
                            mapper.hmServerToClient.put(newServerKey, clientKey);
                        }
                        else if (propertyName.equalsIgnoreCase("changed")) {
                            return false;
                        }
                        else {
                            // new value might be another oaObject
                            // change it's key/etc and set to oaObjKey instead 
                            if (newValue instanceof OAObject) {
                                OAObject objValue = (OAObject) newValue;
                                // see if it already exists, then change the value to match orig
                                Mapper m = getMapper(clientSession, objValue.getClass());
                                OAObjectKey clientNewValueKey = m.hmServerToClient.get(objValue.getObjectKey());
                                
                                if (clientNewValueKey == null) {
                                    // new object, does not exist on the source server
                                
                                    OAObjectKey k1 = objValue.getObjectKey();
                                    
                                    // need to change key 
                                    OAObjectCacheDelegate.removeObject(objValue);
                                    OAObjectDelegate.setAsNewObject(objValue, clientSession.getNextGuid());
                                    
                                    // need to add it to mapper
                                    OAObjectKey k2 = objValue.getObjectKey();
                                    m.hmServerToClient.put(k1, k2);
                                    m.hmClientToServer.put(k2, k1);
                                }
                                else {
                                    newValue = clientNewValueKey; // send key only
                                }
                            }
                            else if (newValue instanceof OAObjectKey) {
                                OAObjectKey ok = (OAObjectKey) newValue;
                                OALinkInfo li = oi.getLinkInfo(propertyName);
                                if (li == null) return false;
                                
                                Mapper m3 = clientSession.hmClassToMapper.get(li.getToClass());
                                if (m3 == null) return false;
                                
                                OAObjectKey ok2 = m3.hmServerToClient.get(ok);
                                if (ok2 == null) {
                                    // create new to send
                                }
                                else {
                                    newValue = ok2;
                                }
                            }
                            
                            final RemoteSyncInterface remoteSyncInterface = getRemoteSyncInterface(clientSession);
                            if (remoteSyncInterface == null) return false;
                            
                            remoteSyncInterface.propertyChange(objectClass, clientKey, propertyName, newValue, bIsBlob);
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
        return syncClient;
    }
    
//qqqqqq create a dsCombinedClient that will send requests to this
    //  save, getRef obj/hub ...
    
    
    
    
    
    
    
    public OASyncClient createSyncClient(Package packagex, String hostName, int port) {
        OASyncClient sc = new OASyncClient(packagex, hostName, port, false) {
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
//qqqqqq could be pkey change                        
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

        RemoteMultiplexerClient rmc = sc.getRemoteMultiplexerClient();
        ClientSession session = new ClientSession();
        session.syncClient = sc;
        hmClientSession.put(rmc, session);
        
        return sc;
    }
    
    
    public OASyncClient getCurrentThreadSyncClient() {
        RemoteMultiplexerClient rmc = null;//OAThreadLocalDelegate.getRemoteMultiplexerClient();
        if (rmc == null) {
            return null;
        }
        ClientSession session = hmClientSession.get(rmc);
        if (session == null) {
            return null;
        }
        return session.syncClient;
    }

    
    private Mapper getMapper(ClientSession cs, Class c) {
        Mapper mapper = cs.hmClassToMapper.get(c);
        if (mapper == null) {
            synchronized (cs.hmClassToMapper) {
                mapper = cs.hmClassToMapper.get(c);
                if (mapper == null) {
                    mapper = new Mapper();
                    cs.hmClassToMapper.put(c, mapper);
                }
            }
        }
        return mapper;
    }

    
    /**
     * Called from OAObjectSerialization.resolveObject, to get the correct object that is used on the
     * combined server.
     * @param objClient
     * @return null if this is not used, otherwise it will change the object with new Id for combined server.
     */
    public OAObject resolveObject(final OAObject objClient) {

        RemoteMultiplexerClient rmc = null;//OAThreadLocalDelegate.getRemoteMultiplexerClient();
        
        if (rmc == null || rmc == syncClient.getRemoteMultiplexerClient()) {
            //qqqqqqqqqqq from Server to Client(s)  - there might not be any clients
            return null;
        }

        ClientSession session = hmClientSession.get(rmc);
        if (session == null) {
            return null;
        }

        // from Client to Server
        Mapper mapper = getMapper(session, objClient.getClass());
        
        OAObject objServer;
        OAObjectKey keyClient = objClient.getObjectKey();
        OAObjectKey keyServer = mapper.hmClientToServer.get(keyClient);
        
        // if null create new obj for server
        if (keyServer == null) {
            OAObjectDelegate.setAsNewObject(objClient);
            objServer = objClient;
//qqqqq need to know when key is changed on server and then update the map            
            keyServer = objClient.getObjectKey();
            mapper.hmClientToServer.put(keyClient, keyServer);
            mapper.hmServerToClient.put(keyServer, keyClient);
            
            
            // qqqqqq remap all of the props that are objkeys
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objServer.getClass());
            for (String prop : OAObjectPropertyDelegate.getPropertyNames(objServer)) {
                Object objx = OAObjectPropertyDelegate.getProperty(objServer, prop);
                if (!(objx instanceof OAObjectKey)) continue;
                OAObjectKey k = (OAObjectKey) objx;
                OALinkInfo li = oi.getLinkInfo(prop);
                if (li == null) continue;
                Class c = li.getToClass();
                Mapper m = getMapper(session, c);
                OAObjectKey k2 = mapper.hmClientToServer.get(k);
                if (keyServer == null) {
                    k2 = new OAObjectKey(null, OAObjectDelegate.getNextGuid(), true);
                    mapper.hmClientToServer.put(k, k2);
                    mapper.hmServerToClient.put(k2, k);
                }
                OAObjectPropertyDelegate.setProperty(objServer, prop, k2);
                
            }
            
            
            
        }
        else {
            objServer = OAObjectCacheDelegate.get(objClient.getClass(), keyServer);
            if (objServer == null) {
                // get from original server
//qqqqqqqqq                
                
            }
        }
        return objServer;
    }

        
//qqqqqqqqq also        
//        oi.getImportMatchProperties();
    
    
    // get the key that was created for the combined server
    public OAObjectKey getClientToServerKey(final Class c, final OAObjectKey keyClient) {
        
        RemoteMultiplexerClient rmc = null;//OAThreadLocalDelegate.getRemoteMultiplexerClient();
        
        if (rmc == null || rmc == syncClient.getRemoteMultiplexerClient()) {
            //qqqqqqqqqqq from Server to Client(s)  - there might not be any clients
            return null;
        }
        
        ClientSession session = hmClientSession.get(rmc);
        if (session == null) {
            return null;
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
