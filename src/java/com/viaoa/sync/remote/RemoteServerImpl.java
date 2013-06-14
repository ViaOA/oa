package com.viaoa.sync.remote;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.sync.model.ClientInfo;

public class RemoteServerImpl implements RemoteServerInterface {

    @Override
    public String ping(String msg) {
        return msg;
    }
    @Override
    public String getDisplayMessage() {
        return "OASyncServer";
    }

    @Override
    public boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) return false;

        h.deleteAll();
        return true;
    }

    protected Hub getHub(OAObject obj, String hubPropertyName, boolean bAutoLoad) {
        if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }

    @Override
    public boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.save(iCascadeRule);
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }

    @Override
    public boolean delete(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.delete();
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }

    @Override
    public RemoteClientInterface getRemoteClientInterface(ClientInfo clientInfo) {
//qqqqqqqqqqqqqqqqqqqq need to match up with RemoteMultiplexer connections
        // to be able to handle disconnects
        
        return null;
    }



    
    
}
