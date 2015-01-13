package com.viaoa.object;

import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

import com.theice.tsac.model.oa.*;
import com.theice.tsac.model.oa.search.*;
import com.viaoa.OAUnitTest;
import com.viaoa.object.*;
import com.viaoa.util.OANotExist;
import com.viaoa.ds.*;
import com.viaoa.ds.autonumber.NextNumber;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;

public class OAObjectTest extends OAUnitTest {
    
    @Test
    public void constructorTest() {
        reset();
        assertFalse(OAThreadLocalDelegate.isLoadingObject());
        Server server = new Server();
        
        assertFalse(OAThreadLocalDelegate.isLoadingObject());
        assertTrue(server.isNew());
        assertTrue(server.isChanged());
        assertFalse(OAThreadLocalDelegate.isSkipObjectInitialize());
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Server.class);
        String[] ps = oi.getPrimitiveProperties();
        assertTrue(ps != null && ps.length == 4);
        for (String s : ps) {
            assertTrue(server.isNull(s));
        }
        assertTrue(server.isNull(Server.P_Id));

        // test: guid should be 1
        int x = OAObjectDelegate.getGuid(server);
        assertEquals(x, 1);

        assertEquals(server.getId(), 0);

        for (OALinkInfo li : oi.getLinkInfos()) {
            if (li.getCalculated()) continue;
            if (li.getPrivateMethod()) continue;
            if (li.getMatchProperty() != null) continue;
            
            Object objx = OAObjectPropertyDelegate.getProperty(server, li.getName(), true, true);
            assertEquals(objx, null);
        }

        // now with DS
        getDataSource();
        server = new Server();
        assertEquals(server.getId(), 1); // auto assigned
        
        // clean up
        reset();
    }
    

    @Test
    public void localGuidTest() {
        reset();
        ServerSearch serverSearch = new ServerSearch();
        assertTrue(OAObjectDelegate.getGuid(serverSearch) < 0);

        // clean up
        reset();
    }
    
    
    @Test
    public void idAndGuidTest() {
        reset();
        int gidNext = OAObjectDelegate.getNextGuid() + 1;
        
        Server server = new Server();
        
        // test: make sure that it is in the cache
        Server serv = (Server) OAObjectCacheDelegate.get(Server.class, 0);  // should not work, Id is null
        assertEquals(serv, null);

        OAObjectKey key = new OAObjectKey(null, gidNext, true);
        serv = (Server) OAObjectCacheDelegate.get(Server.class, key);
        assertEquals(serv, server);

        
        // test: set Id, changes key, cache pos
        serv.setId(1);
        assertFalse(server.isNull(Server.P_Id));
        assertEquals(server.getId(), 1);
        
        try {
            serv.setId(2);
            assertEquals(server.getId(), 2);
        }
        catch (Exception e) {
            fail();
        }

        server.save();
        assertFalse(server.isNew());
        assertFalse(server.isChanged());
        
        try {
            serv.setId(1);
            fail();
        }
        catch (Exception e) {
        }
        assertEquals(server.getId(), 2); // use Id=2
        
        
        // action: this will auto assign Id
        getDataSource();
        assertEquals(OADataSource.getDataSource(Server.class), dsAuto);
        Server server2 = new Server();
        assertFalse(server2.isNull(Server.P_Id));

        // test: guid should be 2
        gidNext++;
        int x = OAObjectDelegate.getGuid(server2);
        assertEquals(x, gidNext);
        
        assertEquals(server2.getId(), 1);
        
        server2 = new Server();
        gidNext++;
        x = OAObjectDelegate.getGuid(server2);
        assertEquals(x, gidNext);
        
        assertEquals(server2.getId(), 3);  // 2 was already manually assigned
        
        // clean up
        reset();
    }

    @Test 
    public void regularPropertyChangeTest() {
        reset();
        Server server = new Server();
        server.save();
        assertFalse(server.isChanged());
        assertFalse(server.isNew());
        
        assertNull(server.getHostName());
        server.setHostName(null);
        assertNull(server.getHostName());
        assertFalse(server.isChanged());
        assertFalse(server.isNew());
        
        server.setHostName("test");
        assertEquals(server.getHostName(), "test");
        assertTrue(server.isChanged());
        assertFalse(server.isNew());
        
        server.setHostName(null);
        assertNull(server.getHostName());
        assertTrue(server.isChanged());
        assertFalse(server.isNew());
        
        server.save();
        assertFalse(server.isChanged());
        assertFalse(server.isNew());
        
        
        ServerInstall si = new ServerInstall();
        server.getServerInstalls().add(si);
        assertFalse(server.isChanged());
        assertFalse(server.isNew());
        assertFalse(server.isChanged(true));  // serverInstalls is not owned

        Silo silo = new Silo();
        silo.save();
        assertFalse(silo.isChanged(true));
        server.setHostName("");
        silo.getServers().add(server);
        
        assertFalse(silo.isChanged());
        assertTrue(silo.isChanged(true));
        
        silo.save();
        assertFalse(silo.isChanged());
        assertFalse(silo.isChanged(true));
        
        server.save();
        assertFalse(silo.isChanged(true));
        
        // clean up
        reset();
    }

    @Test 
    public void uniquePropertyChangeTest() {
        reset();
        ServerType st = new ServerType();
        st.setCode("1");
        st.setCode("2");
        st.setCode("3");

        ServerType st2 = new ServerType();
        st2.setCode("1");
        try {
            st2.setCode("3");
            fail();
        }
        catch (Exception e) {
        }
        assertEquals(st2.getCode(), "1");

        // clean up
        reset();
    }
    
    @Test 
    public void referenceOnePropertyChangeTest() {
        reset();
        Server server = new Server();
        server.setHostName(null);
        
        Object objx = OAObjectPropertyDelegate.getProperty(server,  Server.P_Silo);
        assertNull(objx);
        
        objx = OAObjectPropertyDelegate.getProperty(server,  Server.P_Silo, true, true);
        assertNull(objx);
        
        Silo silo = new Silo();
        server.setSilo(silo);
        assertEquals(silo, server.getSilo());
        
        objx = OAObjectPropertyDelegate.getProperty(server,  Server.P_Silo, true, true);
        assertEquals(silo, objx);
        
        Silo silox = new Silo();
        server.setSilo(silox);
        assertEquals(silox, server.getSilo());
        assertTrue(silox.getServers().contains(server));
        
        objx = OAObjectPropertyDelegate.getProperty(server,  Server.P_Silo, true, true);
        assertEquals(silox, objx);
        
        server.setSilo(null);
        assertNull(server.getSilo());
        assertFalse(silo.getServers().contains(server));
        
        objx = OAObjectPropertyDelegate.getProperty(server,  Server.P_Silo, true, true);
        assertNull(objx);
        
        // clean up
        reset();
    }

    @Test 
    public void referenceManyPropertyChangeTest() {
        reset();
        Silo silo = new Silo();
        Object objx = OAObjectPropertyDelegate.getProperty(silo,  Silo.P_Servers, true, true);
        assertNull(objx);
        
        Hub h = silo.getServers();
        assertTrue(h != null);
        objx = OAObjectPropertyDelegate.getProperty(silo,  Silo.P_Servers, true, true);
        assertTrue(objx != null);
        assertEquals(h, objx);

        // clean up
        reset();
    }

    @Test 
    public void referenceManyAutoMatchPropertyChangeTest() {
        reset();
        SiloType siloType = new SiloType();
        
        ServerType st = new ServerType();
        siloType.getServerTypes().add(st);
        
        Silo silo = new Silo();
        silo.setSiloType(siloType);
        
        assertEquals(silo.getSiloServerInfos().getSize(), 1);
        
        SiloServerInfo info = silo.getSiloServerInfos().getAt(0);
        assertTrue(info != null);
        
        assertEquals(info.getServerType(), st);
        
        ServerType st2 = new ServerType();
        siloType.getServerTypes().add(st2);
        
        info = silo.getSiloServerInfos().getAt(1);
        assertTrue(info != null);
        assertEquals(info.getServerType(), st2);
        
        assertEquals(silo.getSiloServerInfos().getSize(), 2);

        siloType.getServerTypes().remove(st2);
        assertEquals(silo.getSiloServerInfos().getSize(), 1);
        
        // clean up
        reset();
    }

    @Test 
    public void referenceManySeqPropertyChangeTest() {
        reset();
        Silo silo = new Silo();

        ServerGroup sg = new ServerGroup();
        assertEquals(sg.getSeq(), 0);
        assertTrue(sg.isNull(ServerGroup.P_Seq));
        silo.getServerGroups().add(sg);
        assertEquals(sg.getSeq(), 0);
        assertFalse(sg.isNull(ServerGroup.P_Seq));

        ServerGroup sg2 = new ServerGroup();
        assertEquals(sg2.getSeq(), 0);
        silo.getServerGroups().add(sg2);
        assertEquals(sg.getSeq(), 0);
        assertEquals(sg2.getSeq(), 1);
        assertFalse(sg2.isNull(ServerGroup.P_Seq));

        ServerGroup sg3 = new ServerGroup();
        assertEquals(sg3.getSeq(), 0);
        silo.getServerGroups().insert(sg3, 1);
        assertEquals(sg.getSeq(), 0);
        assertEquals(sg2.getSeq(), 2);
        assertEquals(sg3.getSeq(), 1);
        assertFalse(sg3.isNull(ServerGroup.P_Seq));
        
        silo.getServerGroups().move(2, 1);
        assertEquals(sg.getSeq(), 0);
        assertEquals(sg2.getSeq(), 1);
        assertEquals(sg3.getSeq(), 2);
        
        silo.getServerGroups().removeAt(1);
        assertEquals(sg.getSeq(), 0);
        assertEquals(sg2.getSeq(), 1); // removed, so value never changed
        assertEquals(sg3.getSeq(), 1);

        silo.getServerGroups().add(sg2);
        assertEquals(sg.getSeq(), 0);
        assertEquals(sg2.getSeq(), 2); // re-added
        assertEquals(sg3.getSeq(), 1);
        
        // clean up
        reset();
    }
    
    @Test 
    public void recursivePropertyChangeTest() {
        reset();
        AdminUserCategory catParent = new AdminUserCategory();

        AdminUserCategory catChild1 = new AdminUserCategory();
        
        try {
            catParent.setParentAdminUserCategory(catParent);
            fail();
        }
        catch (Exception e) {
        }
        assertNull(catParent.getParentAdminUserCategory());
        
        catChild1.setParentAdminUserCategory(catParent);
        assertEquals(catChild1.getParentAdminUserCategory(), catParent);
        assertEquals(catParent.getAdminUserCategories().getAt(0), catChild1);
        
        try {
            catParent.setParentAdminUserCategory(catChild1);
            fail();
        }
        catch (Exception e) {
        }
        assertNull(catParent.getParentAdminUserCategory());
        

        AdminUserCategory catChild2 = new AdminUserCategory();
        catChild1.getAdminUserCategories().add(catChild2);
        assertEquals(catChild2.getParentAdminUserCategory(), catChild1);
        
        
        catChild2.setParentAdminUserCategory(catParent);
        assertEquals(catParent.getAdminUserCategories().getSize(), 2);
        assertTrue(catParent.getAdminUserCategories().contains(catChild1));
        assertTrue(catParent.getAdminUserCategories().contains(catChild2));
        assertEquals(catChild1.getParentAdminUserCategory(), catParent);
        assertEquals(catChild2.getParentAdminUserCategory(), catParent);
        
        
        // clean up
        reset();
    }
    
    

    
    
    
    
    
//qqqqqqq create these    
//qqqqqqqq OAObjectCacheDelegate 
    //  finder
// datasource tests 
    // order of finding registered one
    
    
    @Test 
    public void metaDataTest() {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Server.class);
        String[] ps = oi.getPrimitiveProperties();
        
//qqq links, etc        
    }
    

    @Test
    public void propertyTest() {
        
        
    }
    
//qqqqqqq  OAThreadLocalDelegate tests    
    
    
    
}
