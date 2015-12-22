package com.viaoa.object;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSourceIterator;
import com.viaoa.ds.OASelect;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.OAFilter;
import com.tmgsc.hifivetest.DataSource;
import com.tmgsc.hifivetest.Resource;
import com.tmgsc.hifivetest.model.oa.*;

public class OAObjectSaveDelegateTest extends OAUnitTest {
    Company company;
    Program program;
    AwardType at;

    @Test
    public void testSave() {
        
        init();
        
        final AtomicInteger ai1 = new AtomicInteger();
        final AtomicInteger ai2 = new AtomicInteger();
        final AtomicInteger ai3 = new AtomicInteger();
        
        /* save company:
         * 1: insert company
           2: insertW/ORef program
           3: insert awardType
           4: update program
        
        */
        dsCache = new OADataSourceObjectCache() {
            @Override
            public boolean supportsStorage() {
                return true;
            }
            @Override
            public void insertWithoutReferences(OAObject obj) {
                assertEquals(1, ai3.get());  // after company insert, before at insert 
                assertEquals(program, obj);
                ai1.incrementAndGet();
                super.insertWithoutReferences(obj);
            }
            @Override
            public void update(OAObject obj) {
                assertEquals(2, ai3.get());  // after company and at are inserted 
                assertEquals(program, obj);
                ai2.incrementAndGet();
                super.update(obj);
            }
            @Override
            public void insert(OAObject object) {
                if (ai3.get() == 0) {
                    assertEquals(company, object);
                }
                else {
                    assertEquals(at, object);
                    assertEquals(1, ai3.get()); 
                }
                
                ai3.incrementAndGet();
                super.insert(object);
            }
        };
        
        company = new Company();
        assertTrue(company.isNew());
        assertTrue(company.isChanged());
        program = new Program();
        assertTrue(program.isNew());
        assertTrue(program.isChanged());
        at = program.getInspireAwardType();
        assertTrue(at.isNew());
        assertTrue(at.isChanged());
        company.getPrograms().add(program);

        company.save();

        assertFalse(company.isNew());
        assertFalse(company.isChanged());
        
        assertFalse(program.isNew());
        assertFalse(program.isChanged());
        
        assertFalse(at.isNew());
        assertFalse(at.isChanged());

        assertEquals(1, ai1.get());
        assertEquals(1, ai2.get());
        assertEquals(2, ai3.get());
    }        
    
    
}










