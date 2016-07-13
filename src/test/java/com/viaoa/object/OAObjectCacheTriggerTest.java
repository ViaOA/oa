package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAFilter;
import com.viaoa.util.filter.OANotEmptyFilter;

import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;
import test.hifive.model.oa.propertypath.EmployeePP;

public class OAObjectCacheTriggerTest extends OAUnitTest {
    
    @Test
    public void test() {
        reset();
        final AtomicInteger ai = new AtomicInteger();
        OAObjectCacheTrigger<Employee> objectCacheTrigger = new OAObjectCacheTrigger<Employee>(Employee.class) {
            @Override
            public void onTrigger(Employee obj) {
                ai.incrementAndGet();
            }
        };
        assertEquals(0, ai.get());
        Employee emp = new Employee();
        assertEquals(1, ai.get());
        emp = new Employee();
        assertEquals(2, ai.get());
        
        emp = new Employee();
        assertEquals(3, ai.get());

        objectCacheTrigger.close();
    }

    @Test
    public void test2() {
        reset();
        ArrayList<Employee> al = new ArrayList<Employee>();
        
        final AtomicInteger ai = new AtomicInteger();
        
        Hub<Employee> hubEmployee = new Hub<Employee>(Employee.class);
        OAObjectCacheTrigger<Employee> objectCacheTrigger = new OAObjectCacheTrigger<Employee>(Employee.class) {
            @Override
            public void onTrigger(Employee obj) {
                ai.incrementAndGet();
            }
        };

        assertEquals(0, ai.get());
        Employee emp = new Employee();
        assertEquals(1, ai.get());
        emp = new Employee();
        assertEquals(2, ai.get());
        objectCacheTrigger.close();
    }

    @Test
    public void test3() {
        reset();
        ArrayList<Employee> al = new ArrayList<Employee>();
        
        final AtomicInteger aiIsUsed = new AtomicInteger();
        final AtomicInteger aiTrigger = new AtomicInteger();
        
        OAObjectCacheTrigger<Employee> objectCacheTrigger = new OAObjectCacheTrigger<Employee>(Employee.class) {
            @Override
            public boolean isUsed(Employee emp) {
                aiIsUsed.incrementAndGet();
                Location loc = emp.getLocation();
                if (loc == null || loc.getName() == null) return false;
                return super.isUsed(emp);
            }
            @Override
            public void onTrigger(Employee emp) {
                aiTrigger.incrementAndGet();
            }
        };
        objectCacheTrigger.addDependentProperty(Employee.P_FirstName);
        objectCacheTrigger.addDependentProperty(Employee.P_LastName);
        objectCacheTrigger.addDependentProperty(EmployeePP.location().name());
        
        objectCacheTrigger.addFilter(new OANotEmptyFilter(Employee.P_FirstLastName));

        Location location = new Location();
        location.setId(0);
        
        assertEquals(0, aiIsUsed.get());
        assertEquals(0, aiTrigger.get());

        for (int i=0; i< 10; i++) {
            Employee emp = new Employee();
            al.add(emp);
            emp.setId(i);
            emp.setFirstName("fn"+i);
            emp.setLastName("ln"+i);

            assertEquals(i*3 + 3, aiIsUsed.get());
            assertEquals(0, aiTrigger.get());
        }
        assertEquals(30, aiIsUsed.get());
        assertEquals(0, aiTrigger.get());

        aiIsUsed.set(0);
        aiTrigger.set(0);
        int i = 0;
        for (Employee emp : al) {
            emp.setLocation(location);
            assertEquals(++i, aiIsUsed.get());
            assertEquals(0, aiTrigger.get());
        }
        
        aiIsUsed.set(0);
        aiTrigger.set(0);
        location.setName("xx");
        assertEquals(al.size(), aiIsUsed.get());
        assertEquals(10, aiTrigger.get());
        
        
        aiIsUsed.set(0);
        aiTrigger.set(0);
        i = 0;
        for (Employee emp : al) {
            emp.setFirstName("x");
            assertEquals(++i, aiIsUsed.get());
            assertEquals(i, aiTrigger.get());
        }
        
        objectCacheTrigger.close();
    }
}
