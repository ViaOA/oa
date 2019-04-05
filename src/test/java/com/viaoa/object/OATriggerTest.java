package com.viaoa.object;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.object.OATriggerListener;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OATriggerDelegate;

import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.EmployeePP;
import test.hifive.model.oa.propertypath.LocationPP;
import test.hifive.model.oa.propertypath.ProgramPP;

public class OATriggerTest extends OAUnitTest {

    @Test
    public void test() {
        String[] pps = new String[] {
            ProgramPP.locations().employees().lastName()
        };
        
        final AtomicInteger ai = new AtomicInteger();
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                ai.incrementAndGet();
            }
        };
        
        OATrigger t = new OATrigger("triggertest",Program.class, tl, pps, false, false, false, false);
        OATriggerDelegate.createTrigger(t);
        
        assertEquals(0, ai.get());
        
        Program prog = new Program();
        Location loc = new Location();
        prog.getLocations().add(loc);
        
        assertEquals(1, ai.get());

        Employee emp = new Employee();
        emp.setLocation(loc);
        
        assertEquals(2, ai.get());
        
        emp = new Employee();
        emp.setLocation(loc);
        
        assertEquals(3, ai.get());
        
        EmployeeAward ea = new EmployeeAward();
        emp.setLastName("doe");
        
        assertEquals(4, ai.get());

        OATriggerDelegate.removeTrigger(t);
    }


    @Test
    public void test2() {
        String[] pps = new String[] {
            ProgramPP.locations().employees().employeeAwards().values().pp
        };
        
        final AtomicInteger ai = new AtomicInteger();
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                ai.incrementAndGet();
            }
        };
        
        OATrigger t = new OATrigger("",Program.class, tl, pps, false, false, false, false);
        OATriggerDelegate.createTrigger(t);
        
        assertEquals(0, ai.get());
        
        Program prog = new Program();
        Location loc = new Location();
        prog.getLocations().add(loc);
        
        assertEquals(1, ai.get());

        Employee emp = new Employee();
        emp.setLocation(loc);
        
        assertEquals(2, ai.get());
        
        emp = new Employee();
        emp.setLocation(loc);
        
        assertEquals(3, ai.get());
        
        OATriggerDelegate.removeTrigger(t);
        
/* throws an exception, reverse pp has private method in it  qqqqqqqqq        
        EmployeeAward ea = new EmployeeAward();
        emp.getEmployeeAwards().add(ea);
        assertEquals(4, ai.get());
*/        
    }

    @Test
    public void testTrigger() {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Program.class);
        ArrayList<String> al = oi.getTriggerPropertNames();
        assertTrue(al.size() >= 4);
        
        String[] pps = new String[] {
            ProgramPP.locations().employees().employeeAwards().values().pp
        };
        al = oi.getTriggerPropertNames();
        assertTrue(al.size() >= 4);

        ArrayList<OATrigger> alT = oi.getTriggers("locations");
        assertNull(alT);
        
        final AtomicInteger ai = new AtomicInteger();
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                ai.incrementAndGet();
            }
        };
        
        OATrigger t = new OATrigger("", Program.class, tl, pps, false, false, false, false);
        OATriggerDelegate.createTrigger(t);
        
        alT = oi.getTriggers("locations");
        assertEquals(1, alT.size());
        

        OATriggerDelegate.removeTrigger(t);
    
        alT = oi.getTriggers("locations");
        assertNull(alT);
    }
    
    
    @Test
    public void testWithCalcProp() {

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        ArrayList<String> al = oi.getTriggerPropertNames();
        // assertEquals(2, al.size());
        
        String[] pps = new String[] {
            ProgramPP.locations().employees().fullName()
        };
        
        final AtomicInteger ai = new AtomicInteger();
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
                ai.incrementAndGet();
            }
        };
        
        OATrigger t = new OATrigger("", Program.class, tl, pps, false, false, false, false);
        OATriggerDelegate.createTrigger(t);

        al = oi.getTriggerPropertNames();
        //assertEquals(8, al.size());
        
        
        assertEquals(0, ai.get());
        
        Program prog = new Program();
        Location loc = new Location();
        prog.getLocations().add(loc);
        
        assertEquals(1, ai.get());

        Employee emp = new Employee();
        emp.setLocation(loc);
        
        assertEquals(2, ai.get());
        
        emp = new Employee();
        emp.setLocation(loc);
        assertEquals(3, ai.get());
        
        emp.setLastName("doe");
        assertEquals(4, ai.get());
        
        emp.setFirstName("joe");
        assertEquals(5, ai.get());
        
        //qqqqqq see if employee fullname, fn, ln all have triggers
        
        Employee empx = new Employee();
        empx.setLastName("j");
        empx.setFirstName("joe");
        assertEquals(5, ai.get());
        emp.getEmployees().add(empx);
        assertEquals(6, ai.get());
        empx.setFirstName("joe2");
        assertEquals(7, ai.get());

        OATriggerDelegate.removeTrigger(t);
        empx.setFirstName("joe3");
        assertEquals(7, ai.get());

        al = oi.getTriggerPropertNames();
        assertTrue(al != null && (al.size() == 2 || al.size() == 4));
    }

    
    @Test
    public void testWithCalcProp2() {

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        ArrayList<String> al = oi.getTriggerPropertNames();
        //assertEquals(2, al.size());
        for (String s : al) {
            ArrayList<OATrigger> alt = oi.getTriggers(s);
            assertTrue(alt.size() > 0);
        }
        
        String[] pps = new String[] {
            EmployeePP.fullName()
        };
        
        OATriggerListener tl = new OATriggerListener() {
            @Override
            public void onTrigger(OAObject obj, HubEvent hubEvent, String propertyPath) throws Exception {
            }
        };

        
        OATrigger t = new OATrigger("",Employee.class, tl, pps, false, false, false, false);
        OATriggerDelegate.createTrigger(t);

        al = oi.getTriggerPropertNames();
        assertEquals(8, al.size());
        
        OATriggerDelegate.removeTrigger(t);
        
        al = oi.getTriggerPropertNames();
        assertTrue(al.size() > 1);
        
    }
    
}
