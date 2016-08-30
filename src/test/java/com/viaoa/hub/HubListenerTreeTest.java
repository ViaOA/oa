package com.viaoa.hub;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OATrigger;
import com.viaoa.util.OADate;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.EmployeePP;
import test.hifive.model.oa.propertypath.LocationPP;
import test.hifive.model.oa.propertypath.ProgramPP;

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

        h.addHubListener(hl);
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        
        h.removeHubListener(hl);
        
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
        
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls == null || hls.length == 0);
        
        h.addHubListener(hl, "test", ss);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 3 && hls[0] == hl);
        
        h.addHubListener(hl);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 3 && hls[0] == hl);
        
        h.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls == null || hls.length == 0);
        
        h.removeHubListener(hl);
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

        h.addHubListener(hl);
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        h.addHubListener(hl);
        h.removeHubListener(hl);
        
        String[] ss = new String[] {
            EmployeePP.location().program().employees().pp
        };
        
        h.addHubListener(hl, "prop", ss);
        hls = HubEventDelegate.getAllListeners(h);
        assertTrue(hls != null && hls.length == 3 && hls[0] == hl);
        

        h.addHubListener(hl, "prop2", ss);
        hls = HubEventDelegate.getAllListeners(h);
        //assertTrue(hls != null && hls.length == 2 && hls[0] == hl && hls[1] != hl);
        
        h.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(h);
        //assertTrue(hls != null && hls.length == 0);
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

        h.addHubListener(hl);
        HubListener[] hls = HubEventDelegate.getAllListeners(h);
        //assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
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
        //assertTrue(hls != null && hls.length == 2 && hls[0] == hl);
        
        assertEquals(0, ai.get());
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
        //assertTrue(hls == null || hls.length ==  0);
    }

    @Test
    public void test4() {
        Employee emp = new Employee();
        
        Hub<Employee> h = new Hub<Employee>();
        h.add(emp);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        h.addHubListener(hl, "test", EmployeePP.fullName());
        h.addHubListener(hl, EmployeePP.fullName());
        h.addHubListener(hl, EmployeePP.fullName());

        h.addHubListener(hl, "test", EmployeePP.fullName());
        h.addHubListener(hl, EmployeePP.fullName());
        
        h.removeHubListener(hl);
    }

    @Test
    public void test5() {
        Hub<Location> h = new Hub<Location>();
        Location loc = new Location();
        h.add(loc);
        
        HubListener hl = new HubListenerAdapter<Employee>() {
        };

        h.addHubListener(hl, "test", LocationPP.employees().fullName());
        h.addHubListener(hl, LocationPP.employees().fullName());
        
        h.removeHubListener(hl);
    }

    @Test
    public void test6() {
        Hub<Location> h = new Hub<Location>();
        Location loc = new Location();
        h.add(loc);

        OAObjectInfo oiLoc = OAObjectInfoDelegate.getObjectInfo(Location.class);
        ArrayList<String> al = oiLoc.getTriggerPropertNames();
        //assertTrue(al != null && al.size() == 6);
                
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

        boolean b;
        h.addHubListener(hl, "xx", LocationPP.employees().fullName());
        
        /*
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
        */

        Employee emp = new Employee();
        loc.getEmployees().add(emp);
        assertEquals(1, ai.get());

        emp.setFirstName("xx");
        assertEquals(2, ai.get());
        
        h.removeHubListener(hl);
        
        al = oiLoc.getTriggerPropertNames();
        assertTrue(al != null && al.size() == 6);
        
        al = oiEmp.getTriggerPropertNames();
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
        
        h.removeHubListener(hl);
    }
    
    @Test
    public void test8() throws Exception {
        Hub<Employee> h = new Hub<Employee>(Employee.class);

        final AtomicInteger ai = new AtomicInteger();
        HubListener hl = new HubListenerAdapter<Employee>() {
            @Override
            public void afterPropertyChange(HubEvent<Employee> e) {
                if ("Testxx".equalsIgnoreCase(e.getPropertyName())) ai.incrementAndGet();
            }
        };

        h.addHubListener(hl, "Testxx", EmployeePP.location().program().name());

        Employee emp = new Employee();
        h.add(emp);
        assertEquals(0, ai.get());
        
        Location loc = new Location();
        emp.setLocation(loc);
        assertEquals(1, ai.get());
        
        emp.setLocation(null);
        assertEquals(2, ai.get());
        emp.setLocation(loc);
        assertEquals(3, ai.get());
        
        Program prog = new Program();
        loc.setProgram(prog);  // NOTE: trigger will later be done in bg thread  qqqqqqqqq
        for (int i=0; i<4; i++) {
            if (ai.get() == 4) break;
            Thread.sleep(50);
        }
        assertEquals(4, ai.get());
        
        prog.setName("xx");
        for (int i=0; i<4; i++) {
            if (ai.get() == 5) break;
            Thread.sleep(50);
        }
        assertEquals(5, ai.get());
        
        int xx = 4;
        xx++;
    }
    
    @Test
    public void test9() {
        Hub<Program> h = new Hub<Program>(Program.class);

        final AtomicInteger ai = new AtomicInteger();
        HubListener hl = new HubListenerAdapter<Program>() {
            @Override
            public void afterPropertyChange(HubEvent<Program> e) {
                if ("TestzzZ".equalsIgnoreCase(e.getPropertyName())) {
                    ai.incrementAndGet();
                }
            }
        };

        h.addHubListener(hl, "TestzzZ", new String[] {
            ProgramPP.locations().employees().employeeAwards().awardType().name(),
            ProgramPP.locations().employees().fullName(),
            ProgramPP.locations().employees().employeeAwards().pp,
            ProgramPP.locations().employees().pp,
            ProgramPP.locations().pp
        });

        int cnt = 0;
        Program prog = new Program();
        h.add(prog);
        assertEquals(cnt, ai.get());
        prog.setName("xx");
        assertEquals(cnt, ai.get());
        
        Location loc = new Location();
        prog.getLocations().add(loc);
        assertEquals(++cnt, ai.get());
        loc.setProgram(prog);
        assertEquals(cnt, ai.get());
        
        Employee emp = new Employee();
        emp.setLocation(loc);
        assertEquals(++cnt, ai.get());
        
        EmployeeAward ea = new EmployeeAward();
        ea.setEmployee(emp);
        assertEquals(++cnt, ai.get());
        
        AwardType at = new AwardType();
        ea.setAwardType(at);
        assertEquals(++cnt, ai.get());
                
        ea.setAwardDate(new OADate());
        assertEquals(cnt, ai.get());

        at.setName("n");
        assertEquals(++cnt, ai.get());
        
        ea.setEmployee(null);
        assertEquals(++cnt, ai.get());

        ea.setEmployee(emp);
        assertEquals(++cnt, ai.get());
        
        emp.setFirstName("fxx");
        assertEquals(++cnt, ai.get());
        emp.setLastName("lxx");
        assertEquals(++cnt, ai.get());
        emp.setMiddleName("mxx");
        assertEquals(++cnt, ai.get());
        
        h.removeHubListener(hl);
        
        //qqqqqqqqqq
        // check triggers qqqqqqq
    }
    
    public static void main(String[] args) throws Exception {
        HubListenerTreeTest test = new HubListenerTreeTest();
        test.test9();
    }
}
