package com.viaoa.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSourceIterator;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.util.OAFilter;

import test.theice.tsac.model.oa.*;

public class OAObjectReflectDelegateTest extends OAUnitTest {
    
    @Test
    public void testOne2OnePrivate() {
        init();
        
        dsCache = new OADataSourceObjectCache() {
            @Override
            public OADataSourceIterator select(Class selectClass, String queryWhere, Object[] params, String queryOrder, OAObject whereObject,
                    String propertyFromWhereObject, String extraWhere, int max, OAFilter filter, boolean bDirty) {
                return super.select(selectClass, queryWhere, params, queryOrder, whereObject, propertyFromWhereObject, extraWhere, max, filter, bDirty);
            }
        };
        
        // one:MRADClientCommand for: one:SSHExecute (reverse method is private)
        MRADClientCommand mc = new MRADClientCommand();
        mc.save();
        
        SSHExecute ssh = new SSHExecute();
        ssh.save();
        
        SSHExecute se = mc.getSSHExecute();
        assertNull(se);
        
        dsCache.close();
        reset();
    }        

    
    
    
    
}
