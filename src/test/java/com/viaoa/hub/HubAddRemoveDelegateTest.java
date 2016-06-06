package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.Employee;
import test.theice.tsac3.model.oa.*;

public class HubAddRemoveDelegateTest extends OAUnitTest {

    @Test
    public void testClear() {
        Hub<Employee> hubEmployee = new Hub<Employee>(Employee.class);
        for (int i=0; i<20; i++) {
            hubEmployee.add(new Employee());
        }
        hubEmployee.setPos(0);
        
        final AtomicInteger ai = new AtomicInteger(); 
        
        hubEmployee.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                ai.incrementAndGet();
            }
        });
        
        hubEmployee.clear();
        assertEquals(1, ai.get());
    }

    @Test
    public void testListener() {
        Hub<Employee> hubEmployee = new Hub<Employee>(Employee.class);
        final AtomicInteger ai = new AtomicInteger();
        final AtomicBoolean ab = new AtomicBoolean(false);
        
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public boolean canAdd(HubEvent e) {
                ai.getAndIncrement();
                return true;
            }
            @Override
            public boolean canRemove(HubEvent e) {
                ai.getAndIncrement();
                return ab.get();
            }
            @Override
            public boolean canDelete(HubEvent e) {
                ai.getAndIncrement();
                return false;
            }
        };
        hubEmployee.addHubListener(hl);
        assertEquals(0, hubEmployee.getSize());
        for (int i=0; i<10; i++) {
            hubEmployee.add(new Employee());
        }
        assertEquals(10, ai.get());
        assertEquals(10, hubEmployee.getSize());

        
        ai.set(0);
        ab.set(false);
        for (Employee e : hubEmployee) {
            try {
                hubEmployee.remove(e);
            }
            catch (Exception ex) {
                ai.getAndIncrement();
            }
        }
        assertEquals(20, ai.get());
        assertEquals(10, hubEmployee.getSize());

        ai.set(0);
        ab.set(false);
        for (Employee e : hubEmployee) {
            try {
                e.delete();
            }
            catch (Exception ex) {
                ai.getAndIncrement();
            }
        }
        assertEquals(20, ai.get());
        assertEquals(10, hubEmployee.getSize());
        
        
        ai.set(0);
        ab.set(true);
        for (Employee e : hubEmployee) {
            hubEmployee.remove(e);
        }
        assertEquals(10, ai.get());
        assertEquals(0, hubEmployee.getSize());
    }
    
    
}
