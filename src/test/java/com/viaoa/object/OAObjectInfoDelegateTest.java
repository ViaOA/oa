package com.viaoa.object;

import java.lang.ref.WeakReference;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDelegate;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.*;

public class OAObjectInfoDelegateTest extends OAUnitTest {

    
    /**
     * Test that OALinkInfo.cache > 0 is using weakReferences for storing the Hub, and that
     * it is added to a cache.  If there is a change to hub or it's objs, then it needs to store a "hard" reference by replacing the weakRef.  
     */
    @Test
    public void testHubCache() {
        reset();
        
        // "dummy" datasource.  Obj Cache wont cache if there is not a ds that supports storage.
        OADataSourceObjectCache ds = new OADataSourceObjectCache() {
            @Override
            public boolean supportsStorage() {
                return true;
            }
        };
        
        // setup
        OAObjectInfo oiProgram = OAObjectInfoDelegate.getOAObjectInfo(Program.class);
        OAObjectInfo oiLocation = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
        OAObjectInfo oiEmployee = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);

        OALinkInfo liProgramLocations = oiProgram.getLinkInfo(ProgramPP.locations().pp);
        OALinkInfo liLocationEmployees = oiLocation.getLinkInfo(LocationPP.employees().pp);

        
        // Program.locations cacheSize>0
        assertTrue(OAObjectInfoDelegate.isWeakReferenceable(oiLocation));
        assertTrue(liProgramLocations.getCacheSize() > 0);

        // Location.employees cacheSize=100
        assertTrue(OAObjectInfoDelegate.isWeakReferenceable(oiEmployee));
        assertTrue(liLocationEmployees.getCacheSize() > 0);

        // setup
        Program program = new Program();
        Hub<Location> hubLocs = program.getLocations(); 

        // prog.hubLocations will be stored using a weakRef, and the hub will be add to cache
        Object obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertTrue(obj instanceof WeakReference);
        
        Location loc = new Location();
        hubLocs.add(loc);

        // prog.hubLocations will be stored using a weakRef, and the hub will be add to cache
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertFalse(obj instanceof WeakReference);
        
        hubLocs.saveAll();

        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertTrue(obj instanceof WeakReference);
        
        
        Hub<Employee> hubEmps = loc.getEmployees();
        
        // loc.hubEmployees will be stored using a weakRef, and the hub will be add to cache
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertTrue(obj instanceof WeakReference);
        
        // ... make sure it is in the cache
        assertTrue(OAObjectInfoDelegate.isCached(liProgramLocations, hubLocs));
        assertTrue(OAObjectInfoDelegate.isCached(liLocationEmployees, hubEmps));
        
        // once the hub is changed, then it needs to store loc.hubEmps directly, w/o a weakReference.
        Employee emp = new Employee();
        hubEmps.add(emp);
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertEquals(hubEmps, obj);
        
        // ... and up the hierarchy to program.hubLocations
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertEquals(hubLocs, obj);
        
        // once hub.saveAll, then loc.hubEmps can use weakRef
        hubEmps.saveAll();
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertTrue(obj instanceof WeakReference);
        // ... but program.hubLocations stays the same ...
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertEquals(hubLocs, obj);
        
        // ... until it has a saveAll
        hubLocs.saveAll();
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertTrue(obj instanceof WeakReference);
        
        // if a hub is being loaded, then it should not act like a change, and loc.hubEmps should still be weakRef 
        hubEmps.setLoading(true);
        OAThreadLocalDelegate.setLoading(true);
        emp = new Employee();
        hubEmps.add(emp);
        emp.save();
        hubEmps.setLoading(false);
        OAThreadLocalDelegate.setLoading(false);
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertTrue(obj instanceof WeakReference);
        obj = OAObjectPropertyDelegate.getProperty(program, Location.P_Locations);
        assertTrue(obj instanceof WeakReference);

        // a change to emp in hubEmps, loc.hubEmps needs to not use weakRef
        emp.setLastName("x");
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        if (obj instanceof WeakReference) obj = ((WeakReference) obj).get();
        assertEquals(hubEmps, obj);
        
        // location cache should also change
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        if (obj instanceof WeakReference) obj = ((WeakReference) obj).get();
        assertEquals(hubLocs, obj);
        
        hubEmps.saveAll();
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertTrue(obj instanceof WeakReference);
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        if (obj instanceof WeakReference) obj = ((WeakReference)obj).get();
        assertEquals(hubLocs, obj);

        hubLocs.saveAll();
        obj = OAObjectPropertyDelegate.getProperty(program, Location.P_Locations);
        assertTrue(obj instanceof WeakReference);
        
        loc.setName("x");
        obj = OAObjectPropertyDelegate.getProperty(program, Program.P_Locations);
        assertEquals(hubLocs, obj);
        obj = OAObjectPropertyDelegate.getProperty(loc, Location.P_Employees);
        assertTrue(obj instanceof WeakReference);
    }
    
    
   @Test
   public void testRecursive() {
       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
       
       OAObjectHashDelegate.hashObjectInfo.clear();
       
       OALinkInfo li1 = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
       assertNotNull(li1);
       
       OALinkInfo li2 = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
       assertNotNull(li2);
       
       OAObjectHashDelegate.hashObjectInfo.clear();

       li2 = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
       assertNotNull(li2);
       
       li1 = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
       assertNotNull(li1);
   }
   
   @Test
   public void testLinkToOwner() {
       OAObjectHashDelegate.hashObjectInfo.clear();

       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Location.class);
       
       OALinkInfo li = OAObjectInfoDelegate.getLinkToOwner(oi);
       
       assertNotNull(li);
   }

   //@Test
   public void testNulls() {
       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(EmailType.class);
       
       //           7=128  6=64  5=32   4=16  3=8  2=4  1=2 0=1 
       // bit[7]=aTestFlag, 6=id, 5=seq, 4=testflag, 5=type

       String[] ss = oi.getPrimitiveProperties();
       assertNotNull(ss);
       assertEquals(5, ss.length);
       assertEquals("ATESTFLAG", ss[0]);
       assertEquals("ID", ss[1]);
       assertEquals("SEQ", ss[2]);
       assertEquals("TESTFLAG", ss[3]);
       assertEquals("TYPE", ss[4]);
       
       /*
       byte[] bs = oi.getPrimitiveMask();
       assertNotNull(bs);
       assertEquals(1, bs.length);
       assertEquals(111, bs[0]);
       */
       
       EmailType et = new EmailType();

       byte[] bsx = OAObjectInfoDelegate.getNullBitMask(et);
       assertNotNull(bsx);
       assertEquals(bsx.length, 1);
       
       assertEquals(111, bsx[0]);
       
       boolean b = OAObjectInfoDelegate.isPrimitiveNull(et, "Testflag");
       assertFalse(b);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "aTestflag");        
       assertFalse(b);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "type");
       assertTrue(b);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "seq");
       assertTrue(b);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "id");
       assertTrue(b);

       assertEquals(111, bsx[0]);
       
       et.setNull("aTestflag");
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "aTestflag");        
       assertTrue(b);
       assertEquals(-17, bsx[0]);

       et.setATestFlag(true);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "aTestflag");        
       assertFalse(b);
       assertEquals(111, bsx[0]);
       
       et.setSeq(0);
       b = OAObjectInfoDelegate.isPrimitiveNull(et, "seq");
       assertFalse(b);
       assertEquals(79, bsx[0]);
   }
    
   //@Test
   public void testNulls2() {
       OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(CurrencyType.class);

       String[] ss = oi.getPrimitiveProperties();
       assertNotNull(ss);
       assertEquals(9, ss.length);
       assertEquals("EXCHANGERATE", ss[0]);
       assertEquals("ID", ss[1]);
       for (int i=2; i<9; i++) {
           assertEquals("ID"+(i-1), ss[i]);
       }
       
       /*
       byte[] bs = oi.getPrimitiveMask();
       assertNotNull(bs);
       assertEquals(2, bs.length);
       assertEquals(-1, bs[0]);
       assertEquals(-1, bs[1]);
       */

       CurrencyType ct = new CurrencyType();
       byte[] bsx = OAObjectInfoDelegate.getNullBitMask(ct);
       assertNotNull(bsx);
       assertEquals(bsx.length, 2);
       assertEquals(-1, bsx[0]);
       assertEquals(-1, bsx[1]);
       
       boolean b = OAObjectInfoDelegate.isPrimitiveNull(ct, "id7");
       assertTrue(b);
       
       ct.setId7(1);
       b = OAObjectInfoDelegate.isPrimitiveNull(ct, "id7");        
       assertFalse(b);
       assertEquals(-1, bsx[0]);
       assertEquals(127, bsx[1]);

       ct.setId6(1);
       b = OAObjectInfoDelegate.isPrimitiveNull(ct, "id6");        
       assertFalse(b);
       assertEquals(-2, bsx[0]);
       assertEquals(127, bsx[1]);
       
   }
    
}
