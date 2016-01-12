package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectCacheFilter;

import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;
import test.hifive.model.oa.propertypath.EmployeePP;
import test.hifive.model.oa.propertypath.ProgramPP;

public class HubTriggerTest extends OAUnitTest {

    @Test
    public void testA() {
        reset();
        // dependents with and w/o "."        
        final AtomicInteger ai = new AtomicInteger();
        Hub<Employee> hubEmployee = new Hub<Employee>(Employee.class);
        
        HubTrigger<Employee> hubTrigger = new HubTrigger<Employee>(hubEmployee) {
            @Override
            public boolean isUsed(Employee object) {
                return super.isUsed(object);
            }
            @Override
            public void onTrigger(Employee emp) {
                ai.incrementAndGet();
            }
        };
        hubTrigger.addDependentProperty(Employee.P_LastName);
        hubTrigger.addDependentProperty(Employee.P_FirstName);
        hubTrigger.addDependentProperty(EmployeePP.location().name());

        
        Location location = new Location();
        location.setId(0);
        
        for (int i=0; i< 10; i++) {
            Employee emp = new Employee();
            emp.setId(i);
            emp.setFirstName("fn"+i);
            emp.setLastName("ln"+i);
            emp.setLocation(location);
            hubEmployee.add(emp);
            assertEquals(i+1, ai.get());
        }
        
        ai.set(0);
        int i = 0;
        for (Employee emp : hubEmployee) {
            assertEquals(i, ai.get());
            emp.setFirstName("fnx"+i);
            assertEquals(++i, ai.get());
        }

        ai.set(0);
        location.setName("x");
        assertEquals(10, ai.get());

        ai.set(0);
        i = 0;
        for (Employee emp : hubEmployee) {
            assertEquals(i, ai.get());
            emp.setLocation(null);
            assertEquals(++i, ai.get());
        }
        
        ai.set(0);
        i = 0;
        for (Employee emp : hubEmployee) {
            assertEquals(i, ai.get());
            emp.setLocation(location);
            assertEquals(++i, ai.get());
        }
        
        hubTrigger.close();
    }
    
    //@Test
    public void testB() {
        reset();
        
        // dependents with and w/o "."        
        final AtomicInteger ai = new AtomicInteger();
        Hub<Employee> hubEmployee = new Hub<Employee>(Employee.class);
        
        HubTrigger<Employee> hubTrigger = new HubTrigger<Employee>(hubEmployee) {
            @Override
            public boolean isUsed(Employee emp) {
                if (emp.getLocation() == null) return false;
                return super.isUsed(emp);
            }
            @Override
            public void onTrigger(Employee emp) {
                ai.incrementAndGet();
            }
        };
        hubTrigger.addDependentProperty(Employee.P_LastName);
        hubTrigger.addDependentProperty(Employee.P_FirstName);
        hubTrigger.addDependentProperty(EmployeePP.location().name());
        

        Location location = new Location();
        location.setId(0);
        
        for (int i=0; i< 10; i++) {
            Employee emp = new Employee();
            emp.setId(i);
            emp.setFirstName("fn"+i);
            emp.setLastName("ln"+i);
            hubEmployee.add(emp);
            assertEquals(0, ai.get());  // location is null, so not triggered
        }
        
        for (Employee emp : hubEmployee) {
            emp.setFirstName("fnx");
            assertEquals(0, ai.get());
        }

        location.setName("x");
        assertEquals(0, ai.get());

        int i = 0;
        for (Employee emp : hubEmployee) {
            emp.setLocation(location);
            assertEquals(++i, ai.get());
        }
        
        ai.set(0);
        location.setName("xx");
        assertEquals(10, ai.get());
        
        
        ai.set(0);
        i = 0;
        for (Employee emp : hubEmployee) {
            assertEquals(i, ai.get());
            emp.setLocation(null); // isUsed will return false
            assertEquals(0, ai.get());
        }
        
        ai.set(0);
        i = 0;
        for (Employee emp : hubEmployee) {
            assertEquals(i, ai.get());
            emp.setLocation(location);
            assertEquals(++i, ai.get());
        }
        
        hubTrigger.close();
    }
}

