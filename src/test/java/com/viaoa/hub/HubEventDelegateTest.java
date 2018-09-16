package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;

import test.hifive.model.oa.CountryCode;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.EmployeeAward;

public class HubEventDelegateTest extends OAUnitTest {

    // @Test
    public void testValidateProperty() {
        Employee emp = new Employee();
        emp.validateTestType = 1;  // test setLastName - no validation
        emp.validateTestResult = 0;
        assertEquals(0, emp.validateTestResult);
        emp.setLastName("test");
        assertEquals(1, emp.validateTestResult);
        assertEquals("test", emp.getLastName());
        

        emp.validateTestType = 2;  // test setLastName - last name must be "test" 
        emp.validateTestResult = 0;
        emp.setLastName("test");
        assertEquals(1, emp.validateTestResult);
        assertEquals("test", emp.getLastName());
        boolean b = false;
        try {
            emp.setLastName("x");
        }
        catch (Exception e) {
            b = true;
        }
        assertEquals(2, emp.validateTestResult);
        assertTrue(b);
        assertEquals("test", emp.getLastName());
        

        emp.validateTestType = 3;  // test setLastName - cant change 
        emp.validateTestResult = 0;
        assertEquals("test", emp.getLastName());
        b = false;
        try {
            emp.setLastName("x");
        }
        catch (Exception e) {
            b = true;
        }
        assertEquals(1, emp.validateTestResult);
        assertTrue(b);
        assertEquals("test", emp.getLastName());

        final AtomicInteger ai = new AtomicInteger(); 
        Hub<Employee> hub = new Hub<>();
        hub.add(emp);
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void beforePropertyChange(HubEvent e) {
                ai.incrementAndGet();
            }
        });
        
        emp.validateTestType = 1;  // test setLastName - no validation
        emp.validateTestResult = 0;
        ai.set(0);
        emp.setLastName("x");
        assertEquals(1, emp.validateTestResult);
        assertEquals("x", emp.getLastName());
        assertEquals(1, ai.get());
      
        emp.validateTestType = 3;  // test setLastName - cant change
        emp.validateTestResult = 0;
        ai.set(0);
        try {
            emp.setLastName("xx");
        }
        catch (Exception e) {}
        assertEquals(1, emp.validateTestResult);
        assertEquals("x", emp.getLastName());
        assertEquals(0, ai.get());
    }

    // @Test
    public void testValidateProperty2() {
        CountryCode cc = new CountryCode();
        Employee emp = new Employee();
        emp.validateTestType = 1;  // test setLastName - no validation
        emp.validateTestResult = 0;
        assertEquals(0, emp.validateTestResult);
        emp.setCountryCode(cc);
        assertEquals(1, emp.validateTestResult);
        assertEquals(cc, emp.getCountryCode());
        
        emp.validateTestType = 3;  // cant change 
        emp.validateTestResult = 0;
        assertEquals(cc, emp.getCountryCode());
        boolean b = false;
        try {
            emp.setCountryCode(null);
        }
        catch (Exception e) {
            b = true;
        }
        assertEquals(1, emp.validateTestResult);
        assertTrue(b);
        assertEquals(cc, emp.getCountryCode());

        final AtomicInteger ai = new AtomicInteger(); 
        Hub<Employee> hub = new Hub<>();
        hub.add(emp);
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void beforePropertyChange(HubEvent e) {
                ai.incrementAndGet();
            }
        });
        
        emp.validateTestType = 1;  // no validation
        emp.validateTestResult = 0;
        ai.set(0);
        emp.setCountryCode(null);
        assertEquals(1, ai.get());
        assertEquals(1, emp.validateTestResult);
        
        emp.setLastName("werw");
        assertEquals(2, ai.get());
        assertEquals(null, emp.getCountryCode());
        
        
        emp.validateTestType = 3;  // cant change
        emp.validateTestResult = 0;
        ai.set(0);
        try {
            emp.setCountryCode(cc);
        }
        catch (Exception e) {}
        assertEquals(0, ai.get());
        assertEquals(1, emp.validateTestResult);
        try {
            emp.setLastName("wrrr");
        }
        catch (Exception e) {}
        assertEquals(0, ai.get());
        assertEquals(2, emp.validateTestResult);
    }

    
    // @Test
    public void testValidateHub() {
        Employee emp = new Employee();
        emp.validateTestType = 1;  // no validation
        emp.validateTestResult = 0;

        final AtomicInteger ai = new AtomicInteger(); 
        
        Hub<EmployeeAward> hub = emp.getEmployeeAwards(); 
        
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void beforePropertyChange(HubEvent e) {
                ai.incrementAndGet();
            }
            @Override
            public void beforeAdd(HubEvent e) {
                ai.incrementAndGet();
            }
            @Override
            public void beforeInsert(HubEvent e) {
                ai.incrementAndGet();
            }
            @Override
            public void beforeDelete(HubEvent e) {
                ai.incrementAndGet();
            }
            @Override
            public void beforeRemove(HubEvent e) {
                ai.incrementAndGet();
            }
            @Override
            public void beforeRemoveAll(HubEvent e) {
                ai.incrementAndGet();
            }
        });

        assertEquals(0, emp.validateTestResult);
        
        EmployeeAward ea = new EmployeeAward();
        hub.add(ea);
        
        assertEquals(1, emp.validateTestResult);
        assertEquals(1, hub.getSize());
        
        emp.validateTestType = 3;  // all are blocked
        emp.validateTestResult = 0;

        boolean b = false;
        try {
            hub.remove(ea);
        }
        catch (Exception e) {
            b = true;
        }
        assertEquals(1, emp.validateTestResult);
        assertEquals(1, hub.getSize());
        assertTrue(b);
        
        emp.validateTestType = 2;  // cant removeAll
        emp.validateTestResult = 0;
        
        hub.remove(ea);
        assertEquals(1, emp.validateTestResult);
        assertEquals(0, hub.getSize());
        
        hub.add(ea);
        assertEquals(2, emp.validateTestResult);
        assertEquals(1, hub.getSize());
        
        b = false;
        try {
            hub.removeAll();
        }
        catch (Exception e) {
            b = true;
        }
        assertEquals(3, emp.validateTestResult);
        assertEquals(1, hub.getSize());
        assertTrue(b);
    }

}
