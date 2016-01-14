package com.viaoa.ds;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAFinder;
import com.viaoa.util.OAFilter;

import test.theice.tsac3.model.Model;
import test.theice.tsac3.Tsac3DataGenerator;
import test.theice.tsac3.model.oa.*;
import test.theice.tsac3.model.oa.propertypath.SitePP;

public class OASelectTest extends OAUnitTest {

    @Test
    public void Test() {
        reset();

        OASelect<Site> selSite = new OASelect<Site>(Site.class);
        assertFalse(selSite.getDirty());
        selSite.setDirty(true);
        assertTrue(selSite.getDirty());
        selSite.setDirty(false);
        assertFalse(selSite.getDirty());
        
        assertNull(selSite.getOrder());
        selSite.setOrder("xxx");
        assertEquals(selSite.getOrder(), "xxx");
        selSite.setOrder(null);
        assertNull(selSite.getOrder());
        
        
        // specific tests
        Model modelTsac = new Model();
        Tsac3DataGenerator data = new Tsac3DataGenerator(modelTsac);
        data.createSampleData();

        selSite = new OASelect<Site>(Site.class);
        selSite.select();
        assertFalse(selSite.hasMore());
        selSite.cancel();
        
        
        selSite = new OASelect<Site>(Site.class);
        selSite.setSearchHub(modelTsac.getSites());
        selSite.select();
        assertTrue(selSite.hasMore());
        for ( ;;) {
            assertNotNull(selSite.next());
            if (!selSite.hasMore()) break;
        }
        selSite.reset();
        selSite.select();
        assertTrue(selSite.hasMore());
        for ( ;;) {
            assertNotNull(selSite.next());
            if (!selSite.hasMore()) break;
        }
        
        selSite.reset();
        // add filter that wont return any matches
        selSite.setFilter(new OAFilter<Site>() {
            @Override
            public boolean isUsed(Site obj) {
                return false;
            }
        });
        assertFalse(selSite.hasMore());
        assertFalse(selSite.isCancelled());

        selSite.reset();
        assertNotNull(selSite.getFilter());
        selSite.setFilter(null);
        assertNull(selSite.getFilter());
        selSite.select();
        assertTrue(selSite.hasMore());
        assertFalse(selSite.isCancelled());
        selSite.cancel();
        assertFalse(selSite.hasMore());
        assertTrue(selSite.isCancelled());
        
        
        OASelect<Server> selServer = new OASelect<Server>(Server.class);
        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(modelTsac.getSites(), SitePP.environments().silos().servers().pp);
        selServer.setFinder(finder);
        selServer.setFilter(new OAFilter<Server>() {
            @Override
            public boolean isUsed(Server obj) {
                return obj != null && obj.getId() == 5;
            }
        });
        selServer.select();
        assertTrue(selServer.hasMore());
        Server serx = selServer.next();
        assertNotNull(serx);
        assertEquals(serx.getId(), 5);
        assertFalse(selSite.hasMore());
        
        reset();
    }
    
}
