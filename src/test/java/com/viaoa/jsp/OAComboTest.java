package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;

import test.hifive.model.oa.Employee;


public class OAComboTest extends OAUnitTest {

    @Test
    public void test() {
        
        Hub<Employee> hub = new Hub<Employee>();
        assertNull(hub.getObjectClass());

        Employee emp = new Employee();
        hub.add(emp);
        assertNotNull(hub.getObjectClass());
        assertEquals(hub.getObjectClass(), Employee.class);
        

    }
    
}
