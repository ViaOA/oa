package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

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

    
    
}
