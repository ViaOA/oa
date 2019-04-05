package com.viaoa.object;

import com.viaoa.ds.OASelect;
import com.viaoa.sync.OASync;
import com.viaoa.sync.OASyncClient;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.util.OAString;

/**
 * Used to find a object with a unique value, with the option to create one that 
 * is concurrently safe (done on server).
 * @author vvia
 */
public class OAObjectUniqueDelegate {

    private static final Object Lock = new Object();

    /**
     * Find and/or create unique OAObject.
     */
    public static OAObject getUnique(final Class<? extends OAObject> clazz, final String propertyName, final Object uniqueKey, final boolean bAutoCreate) {
        
        if (clazz == null) return null;
        if (uniqueKey == null) return null;
        if (OAString.isEmpty(propertyName)) return null;
        
        OAObject oaObj = (OAObject) OAObjectCacheDelegate.find(clazz, propertyName, uniqueKey);
        if (oaObj != null) return oaObj;
        
        // not found
        if (OASyncDelegate.isClient(clazz)) {
            OASyncClient sc = OASync.getSyncClient();
            RemoteServerInterface rs;
            try {
                rs = sc.getRemoteServer();

                if (rs != null) {
                    oaObj = rs.getUnique(clazz, propertyName, uniqueKey, bAutoCreate);
                    return oaObj;
                }
            }
            catch (Exception e) {
                throw new RuntimeException("getUnique() getRemoteServer() exception", e);
            }
        }
        
        OASelect select = new OASelect(clazz);
        select.setWhere(propertyName+" = ?", new Object[] {uniqueKey});
        oaObj = select.next();
        if (oaObj != null) {
            return oaObj;
        }
        if (!bAutoCreate) return null;

        // need to create new, this needs to be synchronized
        synchronized (Lock) {
            oaObj = getUnique(clazz, propertyName, uniqueKey, false);
            if (oaObj != null) return oaObj;
            oaObj = (OAObject) OAObjectReflectDelegate.createNewObject(clazz);
            try {
                OAThreadLocalDelegate.setLoading(true);
                oaObj.setProperty(propertyName, uniqueKey);
            }
            finally {
                OAThreadLocalDelegate.setLoading(false);
            }
        }
        
        return oaObj;
    }
    
    
    
}
