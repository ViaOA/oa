package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.model.oa.*;
import com.viaoa.object.OAObject;

import test.hifive.model.oa.Employee;
import test.hifive.model.oa.EmployeeAward;


public class HubChangeListenerTest extends OAUnitTest {

    @Test
    public void test() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());

        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertEquals(1, vint.value);
        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }
    
    @Test
    public void test2() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());

        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, Employee.P_LastName) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertEquals(1, vint.value);
        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);

        hubEmp.getAt(0).setLastName("aa");
        assertEquals(3, vint.value);

        int cnt = 3;
        for (Employee emp : hubEmp) {
            hubEmp.getAt(0).setLastName("aa"+cnt);
            assertEquals(++cnt, vint.value);
        }
        
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }
    
    @Test
    public void testUnknown() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());

        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, HubChangeListener.Type.Unknown) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertTrue(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertEquals(1, vint.value);
        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);

        hubEmp.getAt(0).setLastName("aa");
        assertEquals(2, vint.value);

        for (Employee emp : hubEmp) {
            hubEmp.getAt(0).setLastName("b");
            assertEquals(2, vint.value);
        }
        
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }
    
    @Test
    public void testHubValid() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        Hub<EmployeeAward> hubEA = hubEmp.getDetailHub(Employee.P_EmployeeAwards);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEA, HubChangeListener.Type.HubValid) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setPos(-1);
        assertEquals(1, vint.value);

        hubEmp.setPos(1);
        assertTrue(hcl.getValue());
        assertEquals(3, vint.value);
        hubEmp.setPos(2);
        assertTrue(hcl.getValue());
        assertEquals(5, vint.value);

        
        hls = HubEventDelegate.getAllListeners(hubEA);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEA);
        assertTrue(hls.length == 0);
    }
    
    @Test
    public void testHubEmpty() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, HubChangeListener.Type.HubEmpty) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(1, vint.value);
        assertTrue(hcl.getValue());

        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        assertFalse(hcl.getValue());
        assertEquals(6, vint.value);
        assertFalse(hcl.getValue());
        
        hubEmp.getAt(0).setLastName("xx");
        assertEquals(7, vint.value);
        assertFalse(hcl.getValue());

        hubEmp.clear();
        assertEquals(8, vint.value);
        assertTrue(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }

    @Test
    public void testHubNotEmpty() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, HubChangeListener.Type.HubNotEmpty) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(1, vint.value);
        assertFalse(hcl.getValue());

        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        assertTrue(hcl.getValue());
        assertEquals(6, vint.value);
        assertTrue(hcl.getValue());
        
        hubEmp.getAt(0).setLastName("xx");
        assertEquals(7, vint.value);
        assertTrue(hcl.getValue());

        hubEmp.clear();
        assertEquals(8, vint.value);
        assertFalse(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }

    @Test
    public void testAoNotNull() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, HubChangeListener.Type.AoNotNull) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setPos(-1);
        assertEquals(1, vint.value);

        hubEmp.setPos(1);
        assertTrue(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }

    @Test
    public void testAoNull() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        Hub<EmployeeAward> hubEA = hubEmp.getDetailHub(Employee.P_EmployeeAwards);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEA, HubChangeListener.Type.AoNull) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertTrue(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertTrue(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setPos(-1);
        assertEquals(1, vint.value);

        hubEmp.setPos(1);
        assertTrue(hcl.getValue());
        assertEquals(3, vint.value);
        hubEA.add(new EmployeeAward());
        assertTrue(hcl.getValue());
        hubEA.setPos(0);
        assertFalse(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEA);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEA);
        assertTrue(hls.length == 0);
    }

    @Test
    public void testAlwaysTrue() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener(hubEmp, HubChangeListener.Type.AlwaysTrue) {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertTrue(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setAO(null);
        assertTrue(hcl.getValue());
        assertEquals(1, vint.value);
        hubEmp.setPos(-1);
        assertEquals(1, vint.value);

        hubEmp.setPos(1);
        assertTrue(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }

    @Test
    public void testAlwaysPropertyNull() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener() {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(0, vint.value);

        hcl.addPropertyNull(hubEmp, Employee.P_LastName);
        
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);

        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        assertTrue(hcl.getValue());

        hubEmp.getAt(0).setLastName("xx");
        assertEquals(3, vint.value);
        assertFalse(hcl.getValue());

        hubEmp.getAt(0).setLastName(null);
        assertEquals(4, vint.value);
        assertTrue(hcl.getValue());

        hubEmp.getAt(0).setLastName("xx");
        assertEquals(5, vint.value);
        assertFalse(hcl.getValue());

        hubEmp.getAt(2).setLastName("xxx");
        assertEquals(5, vint.value);
        assertFalse(hcl.getValue());
        hubEmp.getAt(2).setLastName(null);
        assertEquals(5, vint.value);
        assertFalse(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }
    
    @Test
    public void testAlwaysPropertyNotNull() {
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
        
        VInteger vint = new VInteger();
        HubChangeListener hcl = new HubChangeListener() {
            @Override
            protected void onChange() {
                vint.inc();
            }
        };
        assertEquals(0, vint.value);

        hcl.addPropertyNotNull(hubEmp, Employee.P_LastName);
        
        assertFalse(hcl.getValue());
        assertEquals(1, vint.value);

        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        assertFalse(hcl.getValue());

        hubEmp.getAt(0).setLastName("xx");
        assertEquals(3, vint.value);
        assertTrue(hcl.getValue());

        hubEmp.getAt(0).setLastName(null);
        assertEquals(4, vint.value);
        assertFalse(hcl.getValue());

        hubEmp.getAt(0).setLastName("xx");
        assertEquals(5, vint.value);
        assertTrue(hcl.getValue());

        hubEmp.getAt(2).setLastName("xxx");
        assertEquals(5, vint.value);
        assertTrue(hcl.getValue());
        hubEmp.getAt(2).setLastName(null);
        assertEquals(5, vint.value);
        assertTrue(hcl.getValue());
        
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 1);
        hcl.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertTrue(hls.length == 0);
    }
    
}
