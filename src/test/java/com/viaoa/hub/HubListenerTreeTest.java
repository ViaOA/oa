package com.viaoa.hub;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OATrigger;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.EmployeePP;
import test.hifive.model.oa.propertypath.LocationPP;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class HubListenerTreeTest extends OAUnitTest {

    @Test
    public void test1() {
        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        assertTrue(h.addHubListener(hl));
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        
        assertFalse(h.addHubListener(hl));
        
        assertTrue(h.removeHubListener(hl));
        
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls == null || hls.length == 0);
    }

    @Test
    public void test1b() {
        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        String[] ss = new String[] {
            EmployeePP.location().program().employees().pp
        };
        
        assertTrue(h.addHubListener(hl, "test", ss));
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 2 && hls[0] == hl);
        
        assertFalse(h.addHubListener(hl));
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 2 && hls[0] == hl);
        
        assertTrue(h.removeHubListener(hl));
        
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls == null || hls.length == 0);
    }
    
    @Test
    public void test2() {
        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        assertTrue(h.addHubListener(hl));
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        assertFalse(h.addHubListener(hl));
        
        String[] ss = new String[] {
            EmployeePP.location().program().employees().pp
        };
        
        h.addHubListener(hl, "prop", ss);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 2 && hls[0] == hl && hls[1] != hl);
        

        h.addHubListener(hl, "prop2", ss);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 2 && hls[0] == hl && hls[1] != hl);
        
        h.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 0);
    }
    
    @Test
    public void test3() {
        Hub<Employee> h = new Hub<Employee>();

        final AtomicInteger ai = new AtomicInteger();
        HubListener hl = new HubListenerAdapter<Employee>() {
            @Override
            public void afterPropertyChange(HubEvent<Employee> e) {
                ai.incrementAndGet();
            }
        };

        assertTrue(h.addHubListener(hl));
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        assertEquals(0, ai.get());

        
        Program prog = new Program();
        Location loc = new Location();
        prog.getLocations().add(loc);
        Employee emp = new Employee();
        emp.setLocation(loc);

        assertEquals(0, ai.get());
        emp.setLastName("a");
        assertEquals(0, ai.get());
        
        h.add(emp);
        assertEquals(0, ai.get());
        
        emp.setLastName("a");
        assertEquals(0, ai.get());
        emp.setLastName("b");
        assertEquals(1, ai.get());
        
        
        ai.set(0);
        String[] ss = new String[] {
            EmployeePP.location().program().name()
        };
        h.addHubListener(hl, "testxx", ss);
        assertEquals(0, ai.get());
        
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 2 && hls[0] == hl);
        
        prog.setName("xx");
        assertEquals(1, ai.get());

        prog.setBirthdayDisplayDays(40);
        assertEquals(1, ai.get());
        
        emp.setLocation(null);
        assertEquals(3, ai.get());
        
        prog.setName("zz");
        assertEquals(3, ai.get());

        
        loc.getEmployees().add(emp);
        // 2 propChange events:  Location, "testxx"
        assertEquals(5, ai.get());
        
        
        h.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls == null || hls.length ==  0);
    }

    @Test
    public void test4() {
        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        assertTrue(h.addHubListener(hl, "test", EmployeePP.fullName()));
        assertFalse(h.addHubListener(hl, EmployeePP.fullName()));

        assertFalse(h.addHubListener(hl, "test", EmployeePP.fullName()));
        assertFalse(h.addHubListener(hl, EmployeePP.fullName()));
        
        assertTrue(h.removeHubListener(hl));
    }

    @Test
    public void test5() {
        Hub<Location> h = new Hub<Location>();
        Location loc = new Location();
        h.add(loc);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        assertTrue(h.addHubListener(hl, "test", LocationPP.employees().fullName()));
        assertFalse(h.addHubListener(hl, LocationPP.employees().fullName()));
        
        assertTrue(h.removeHubListener(hl));
    }

    @Test
    public void test6() {
        Hub<Location> h = new Hub<Location>();
        Location loc = new Location();
        h.add(loc);

        OAObjectInfo oiLoc = OAObjectInfoDelegate.getObjectInfo(Location.class);
        ArrayList<String> al = oiLoc.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 6);
                
        OAObjectInfo oiEmp = OAObjectInfoDelegate.getObjectInfo(Employee.class);
        al = oiEmp.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 2);
        ArrayList<OATrigger> alT = oiEmp.getTriggers("PROGRAM");
        assertNotNull(alT);
        
        final AtomicInteger ai = new AtomicInteger();
        HubListener hl = new HubListenerAdapter<Employee>() {
            @Override
            public void afterPropertyChange(HubEvent<Employee> e) {
                ai.incrementAndGet();
            }
        };

        boolean b = h.addHubListener(hl, "xx", LocationPP.employees().fullName());
        assertTrue(b);
        
        al = oiLoc.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 7);
        
        al = oiEmp.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 8);
        ArrayList<String> al2 = al;
        
        alT = oiEmp.getTriggers("FULLNAME");
        assertNotNull(alT);
        OATrigger t = alT.get(0);
        
        OATrigger[] ts = t.getDependentTriggers();
        assertNotNull(ts);
        assertEquals(ts.length, 1);
        t = ts[0];
        assertNull(t.getDependentTriggers());
        

        Employee emp = new Employee();
        loc.getEmployees().add(emp);
        assertEquals(1, ai.get());

        emp.setFirstName("xx");
        assertEquals(2, ai.get());
        
        assertTrue(h.removeHubListener(hl));
        
        al = oiLoc.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 6);
        
        al = oiEmp.getTriggerPropertNames();
        al2 = null;
        assertTrue(al != null && al.size() == 2);
        alT = oiEmp.getTriggers("PROGRAM");
        assertNotNull(alT);
    }
    
    @Test
    public void test7() {
        Hub<Location> h = new Hub<Location>();
        Location loc = new Location();
        h.add(loc);

        final AtomicInteger ai = new AtomicInteger();
        HubListener hl = new HubListenerAdapter<Employee>() {
            @Override
            public void afterPropertyChange(HubEvent<Employee> e) {
                ai.incrementAndGet();
            }
        };

        h.addHubListener(hl, "fn", LocationPP.employees().firstName());

        Employee emp = new Employee();
        loc.getEmployees().add(emp);
        assertEquals(1, ai.get());

        emp.setFirstName("fnx");
        assertEquals(2, ai.get());
        
        assertTrue(h.removeHubListener(hl));
    }
    
    
    
    public static void main(String[] args) {
        HubListenerTreeTest test = new HubListenerTreeTest();
        test.test6();
        test.test7();
    }
}
