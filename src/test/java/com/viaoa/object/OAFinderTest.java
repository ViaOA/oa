package com.viaoa.object;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;
import com.viaoa.TsactestDataGenerator;
import com.viaoa.hub.Hub;

import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.ProgramPP;
import test.theice.tsac3.model.oa.*;
import test.theice.tsac3.model.oa.propertypath.*;

public class OAFinderTest extends OAUnitTest {
    
    
    @Test
    public void finderSimpleTest() {
        init();
        TsactestDataGenerator data = new TsactestDataGenerator(modelTsac);
        data.createSampleData1();

        // a finder without a filter should return all objects
        OAFinder f = new OAFinder();
        ArrayList al = f.find(modelTsac.getSites());
        assertEquals(modelTsac.getSites().size(), al.size());

        f = new OAFinder() {
            int cnt;
            @Override
            protected boolean isUsed(OAObject obj) {
                assertEquals(true, super.isUsed(obj));
                return (cnt++ == 0);
            }
        };
        al = f.find(modelTsac.getSites());
        assertEquals(1, al.size());
        
        f = new OAFinder() {
            @Override
            protected boolean isUsed(OAObject obj) {
                assertEquals(true, super.isUsed(obj));
                return false;
            }
        };
        al = f.find(modelTsac.getSites());
        assertEquals(0, al.size());

        f = new OAFinder();
        f.addEqualFilter(null, modelTsac.getSites().getAt(0));
        al = f.find(modelTsac.getSites());
        assertEquals(1, al.size());
        
        int id = modelTsac.getSites().getAt(0).getId();
        f.addEqualFilter("id", id+"");
        al = f.find(modelTsac.getSites());
        assertEquals(1, al.size());
        
        f.addEqualFilter("id", id);
        al = f.find(modelTsac.getSites());
        assertEquals(1, al.size());
        
        reset();
    }
    
    @Test
    public void finderTest() {
        init();
        TsactestDataGenerator data = new TsactestDataGenerator(modelTsac);
        data.createSampleData1();

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);

        Hub<Site> hubSite = modelTsac.getSites();
        Server server = finder.findFirst(hubSite);
        assertEquals(server.getName(), "Server.0.0.0.0");
        assertEquals(server.getId(), 1);
        
        ArrayList<Server> alServer = finder.find(modelTsac.getSites());
        int cnt = 0;
        for (Server ser : alServer) {
            assertEquals(ser.getId(), ++cnt);
        }
        
        finder.clearFilters();
        finder.addEqualFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 1);
        server = alServer.get(0);
        assertEquals(server.getId(), 5);

        finder.clearFilters();
        finder.addLessFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 4);
        
        finder.clearFilters();
        finder.addLessOrEqualFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 5);

        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.id(), 1);
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);
        
        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.silo().environment().name(), "Environment.0.0");
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);

        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.silo().environment().name(), "Environment.0.0");
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);

        reset();
    }
    
    @Test
    public void recursiveFinderTest() {
        init();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();
        
        Employee emp = ModelDelegate.getPrograms().getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        emp.setLastName("xxx");
        
        OAFinder<Program, Employee> finder = new OAFinder<Program, Employee>(ProgramPP.locations().employees().pp);
        finder.addEqualFilter(Employee.P_LastName, "xxx");
        Employee empx = finder.findFirst(ModelDelegate.getPrograms());
        assertEquals(emp, empx);
        
        emp.setLastName("");
        empx = finder.findFirst(ModelDelegate.getPrograms());
        assertNull(empx);
        
        
        reset();
    }

    @Test
    public void equalsTest() {
        init();

        OAFinder<Site, Site> finder = new OAFinder<Site, Site>();
        String pp = SitePP.environments().silos().servers().hostName();
        finder.addEqualFilter(pp, "none");
        
        pp = SitePP.environments().silos().servers().gsmrServer().instanceNumber();
        finder.addEqualFilter(pp, 1);
        
        Hub<Site> hub = new Hub<Site>(Site.class);
        Site site = finder.findFirst(hub);

        assertNull(site);
    }
}
