package com.viaoa;

import com.theice.tsactest.model.Model;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.autonumber.NextNumber;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;

public class OAUnitTest {

    protected OADataSourceAuto dsAuto;
    protected OADataSourceAuto dsCache;
    protected Model model;
    
    protected OADataSource getDataSource() {
        return getAutoDataSource();
    }
    
    protected OADataSource getAutoDataSource() {
        if (dsAuto == null) {
            dsAuto = new OADataSourceAuto();
        }
        return dsAuto;
    }
    protected OADataSource getCacheDataSource() {
        if (dsCache == null) {
            dsCache = new OADataSourceObjectCache();
        }
        return dsCache;
    }
    
    
    protected void reset() {
        model = new Model();
        if (dsCache != null) {
            dsCache.close();
            dsCache.setGlobalNextNumber(null);
            dsCache = null;
        }
        if (dsAuto != null) {
            dsAuto.close();
            dsAuto.setGlobalNextNumber(null);
            dsAuto = null;
        }
        OAObjectCacheDelegate.clearCache(NextNumber.class);
        OADataSource.closeAll();
        OAObjectCacheDelegate.clearCache();
        OAObjectDelegate.setNextGuid(0);
        OAObjectCacheDelegate.removeAllSelectAllHubs();
    }

    public void delay() {
        delay(1);
    }
    public void delay(int ms) {
        try {
            if (ms > 0) Thread.sleep(ms);
            else Thread.yield();
        }
        catch (Exception e) {}
    }
}

