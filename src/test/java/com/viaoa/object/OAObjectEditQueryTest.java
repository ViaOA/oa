package com.viaoa.object;

import static org.junit.Assert.*;
import org.junit.Test;
import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObjectEditQuery.Type;
import com.viaoa.util.OADate;

import test.hifive.model.oa.Address;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.EmployeeAward;

public class OAObjectEditQueryTest extends OAUnitTest {

    /**
     * Employee
     *    class is enabled if inactieDate=null
     * Employee.addresses is enabled if createdate
     * 
     * @throws Exception
     */
    
    @Test
    public void test() throws Exception {
        Employee emp = new Employee();
        assertNotNull(emp.getCreated());
        boolean b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertTrue(b);

        assertTrue(emp.isEnabled());

        assertTrue(emp.isEnabled(Employee.P_LastName));
        b = emp.isEnabled(Employee.P_Addresses);
        assertTrue(b);
        b = emp.getAddresses().canAdd();
        assertTrue(b);
        b = emp.getAddresses().canAdd(new Address());
        assertTrue(b);
        
        b = emp.getEmployeeAwards().canAdd();
        assertTrue(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "lastName");
        assertTrue(b);
        
        emp.setInactiveDate(new OADate());
        assertFalse(emp.isEnabled());
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertFalse(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "lastName");
        assertFalse(b);

        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "EmployeeAwards");
        assertFalse(b);

        b = OAObjectEditQueryDelegate.getAllowAdd(emp.getEmployeeAwards());
        assertFalse(b);
        
        b = emp.getEmployeeAwards().canAdd();
        assertFalse(b);

        b = emp.getAddresses().canAdd();
        assertFalse(b);
        
        b = emp.getAddresses().canAdd(new Address());
        assertFalse(b);
        b = emp.getAddresses().canRemove();
        assertFalse(b);
        
        try {
            emp.getAddresses().add(new Address());
            fail("emp.getAddresses.add should not be allowed");
        }
        catch (Exception e) {
        }
        
        
        b = emp.getEmployeeAwards().canAdd();
        assertFalse(b);
        try {
            emp.getEmployeeAwards().add(new EmployeeAward());
            fail("emp.getEmployeeAwards.add should not be allowed");
        }
        catch (Exception e) {
        }
        
        assertFalse(emp.isEnabled(Employee.P_LastName));
        
        assertFalse(emp.isEnabled(Employee.P_Addresses));
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, Employee.P_Addresses);
        assertFalse(b);
        b = OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses());
        assertFalse(b);
        
        try {
            emp.setLastName("test");
        //    fail("setLastName should fail");
            //  firePropertyChange calls editQuery, and will only log.warn (not throw exception)
        }
        catch (Exception e) {
        }

        
        try {
            emp.setInactiveDate(null);
        }
        catch (Exception e) {
            fail("setInactiveDate should not fail");
        }
        
        try {
            emp.setLastName("test");
        }
        catch (Exception e) {
            fail("setLastName should not fail");
        }
        
    }

    @Test
    public void testClass() throws Exception {
        Employee emp = new Employee();
        assertNotNull(emp.getCreated());
        boolean b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertTrue(b);
        
        assertTrue(emp.isEnabled());
        assertTrue(emp.isVisible());
        
        emp.setInactiveDate(new OADate());
        assertFalse(emp.isEnabled());
        assertTrue(emp.isVisible());

        emp.setInactiveDate(null);
        assertTrue(emp.isEnabled());
        assertTrue(emp.isVisible());

        emp.TestEditQuery_Class = new OAObjectEditQuery(Type.AllowEnabled);
        emp.TestEditQuery_Class.setAllowed(false);
        assertFalse(emp.isEnabled());

        emp.TestEditQuery_Class.setAllowed(true);
        assertTrue(emp.isEnabled());
        
        assertTrue(emp.isVisible());
        emp.TestEditQuery_Class = new OAObjectEditQuery(Type.AllowVisible);
        emp.TestEditQuery_Class.setAllowed(false);
        assertFalse(emp.isVisible());

        emp.TestEditQuery_Class = null;
        
        assertTrue(emp.getAddresses().canAdd());
        
        emp.TestEditQuery_Class = new OAObjectEditQuery(Type.AllowEnabled);
        emp.TestEditQuery_Class.setAllowed(false);
        assertFalse(emp.isEnabled());
        
        assertFalse(emp.getEmployeeAwards().canAdd());
        
        try {
            emp.getEmployeeAwards().add(new EmployeeAward());
            fail("emp.getEmployeeAwards.add should not be allowed");
        }
        catch (Exception e) {
        }
        emp.TestEditQuery_Class = null;
        try {
            emp.getEmployeeAwards().add(new EmployeeAward());
        }
        catch (Exception e) {
            fail("emp.getEmployeeAwards.add should be allowed");
        }
        
        emp.TestEditQuery_Class = null;
    }    
    @Test
    public void testProperty() throws Exception {
    }    
    @Test
    public void testOwner() throws Exception {
    }    
    @Test
    public void testCalcProperty() throws Exception {
        Employee emp = new Employee();
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        OACalcInfo ci = oi.getCalcInfo(emp.P_FullName);
        assertNotNull(ci);
        if (ci.getEnabledProperty() != null) assertEquals("", ci.getEnabledProperty());
    }    
    @Test
    public void testLinkOne() throws Exception {
        Employee emp = new Employee();
        assertNull(emp.getEmployeeType());
        assertTrue(emp.isEnabled());
        assertTrue(emp.isEnabled(emp.P_EmployeeType));
        
        EmployeeAward ea = new EmployeeAward();
        assertTrue(ea.isEnabled());
        assertTrue(ea.isVisible());
        ea.setEmployee(emp);
        assertTrue(ea.isEnabled());
        assertTrue(ea.isVisible());
        emp.setInactiveDate(new OADate());
        assertFalse(ea.isEnabled());
        assertTrue(ea.isVisible());
        emp.setInactiveDate(null);
        assertTrue(ea.isEnabled());
        assertTrue(ea.isVisible());
    }    
    @Test
    public void testLinkMany() throws Exception {
        Employee emp = new Employee();
        
        assertTrue(emp.getAddresses().canAdd());
        assertTrue(emp.getAddresses().canRemove());
        assertTrue(emp.getAddresses().canAdd());
        assertTrue(OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses()));
        assertTrue(OAObjectEditQueryDelegate.getAllowEnabled(emp.getAddresses()));

        emp.setCreated(null);
        assertFalse(emp.getAddresses().canAdd());
        assertFalse(emp.getAddresses().canRemove());
        assertFalse(emp.getAddresses().canAdd());
        assertFalse(OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses()));
        assertFalse(OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses()));
        assertFalse(OAObjectEditQueryDelegate.getAllowEnabled(emp.getAddresses()));
        
        emp.setCreated(new OADate());
        assertTrue(emp.getAddresses().canAdd());
        assertTrue(emp.getAddresses().canRemove());
        assertTrue(emp.getAddresses().canAdd());
        
        Address address = new Address();
        emp.getAddresses().add(address);
        
        emp.setCreated(null);
        emp.setInactiveDate(new OADate());
        assertFalse(emp.getAddresses().canAdd());
        assertFalse(emp.getAddresses().canRemove());
        assertFalse(emp.getAddresses().canAdd());
        assertFalse(OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses()));
        assertFalse(OAObjectEditQueryDelegate.getAllowAdd(emp.getAddresses()));
        assertFalse(OAObjectEditQueryDelegate.getAllowEnabled(emp.getAddresses()));
        
        assertFalse(address.isEnabled());
        assertTrue(address.isVisible());
        assertFalse(address.isEnabled(Address.P_Address1));
        assertFalse(emp.getAddresses().canAdd());

        emp.setInactiveDate(null);
        emp.setCreated(null);
        assertFalse(address.isEnabled());
        assertTrue(address.isVisible());
        assertFalse(address.isEnabled(Address.P_Address1));
        assertFalse(emp.getAddresses().canAdd());
        
        emp.setCreated(new OADate());
        assertTrue(address.isEnabled());
        assertTrue(address.isVisible());
        assertTrue(address.isEnabled(Address.P_Address1));
        
    }    
    @Test
    public void testCommand() throws Exception {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        OAMethodInfo mi = oi.getMethodInfo("command");
        assertNotNull(oi);
        assertEquals("", mi.getEnabledProperty());
        assertEquals("birthDate", mi.getVisibleProperty());
        
        Employee emp = new Employee();
        boolean b = OAObjectEditQueryDelegate.getAllowVisible(emp, "command");
        assertFalse(b);

        emp.setBirthDate(new OADate("05/04/99"));
        b = OAObjectEditQueryDelegate.getAllowVisible(emp, "command");
        assertTrue(b);
    }   
    
}




