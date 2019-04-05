package com.viaoa.jfc.control;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import javax.swing.*;

import com.viaoa.hub.*;
import com.viaoa.model.oa.VInteger;
import com.viaoa.util.OADate;

import test.hifive.model.oa.*;
import test.hifive.model.oa.Company;
import test.xice.tsac.model.oa.*;

public class OAJfcControllerTest extends OAUnitTest {

    @Test
    public void test() {
        // disabled by editQuery
        JLabel lbl = new JLabel();        
        Hub hub = new Hub<Employee>(Employee.class);
        
        Hub hubDirect = null;
        String directPropertyName = null;
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 0);
        
        OAJfcController jc = new OAJfcController(hub, null, Employee.P_LastName, lbl, HubChangeListener.Type.AoNotNull, false, true);

        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        
        assertEquals(hub, jc.getHub());
        assertEquals(Employee.P_LastName, jc.getPropertyPath());
        
        Employee emp = new Employee();
        assertTrue(emp.isEnabled(Employee.P_LastName));
        
        emp.setInactiveDate(new OADate());
        assertFalse(emp.isEnabled(Employee.P_LastName));
        
        hub.add(emp);
        hub.setAO(emp);
        assertFalse(lbl.isEnabled());

        emp.setInactiveDate(null);
        assertTrue(lbl.isEnabled());

        // make sure listeners are removed         
        jc.close();
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
    }

    
    @Test
    public void testA() {
        // detail hub that is disabled by master object
        JLabel lbl = new JLabel();        
        Hub hub = new Hub<Employee>(Employee.class);
        Hub hubDirect = null;
        String directPropertyName = null;

        Hub hubEa = hub.getDetailHub(Employee.P_EmployeeAwards);
        
        OAJfcController jc = new OAJfcController(hubEa, null, "Employee.lastName", lbl, HubChangeListener.Type.AoNotNull, false, true);

        Employee emp = new Employee();
        assertEquals(0, hubEa.getSize());

        EmployeeAward ea = new EmployeeAward();
        emp.getEmployeeAwards().add(ea);
        hub.add(emp);
        hub.setPos(0);
        
        assertEquals(1, hubEa.getSize());
        
        assertTrue(ea.isEnabled(EmployeeAward.P_AwardDate));
        
        emp.setInactiveDate(new OADate());

        assertFalse(ea.isEnabled(EmployeeAward.P_AwardDate));

        emp.setInactiveDate(null);
        assertTrue(ea.isEnabled(EmployeeAward.P_AwardDate));
        
        
        assertEquals(lbl.getText(), "");
        emp.setLastName("xx");
        // assertEquals("xx", lbl.getText());
        
        jc.close();
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
    }

    @Test
    public void testB() {
        // create linked hub and test, making sure it is disabled if link object.enabled=false
        
        JLabel lbl = new JLabel();        
        Hub hub = new Hub<Employee>(Employee.class);
        Hub hubDirect = null;
        String directPropertyName = null;

        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        Hub<EmployeeType> hubEt = new Hub<EmployeeType>(EmployeeType.class);
        for (int i=0; i<5; i++) hubEt.add(new EmployeeType());

        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        hls = HubEventDelegate.getAllListeners(hubEt);
        assertTrue(hls == null || hls.length == 0);
        
        hubEt.setLinkHub(hub, Employee.P_EmployeeType);
        assertNull(hubEt.getAO());
        
        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubEt);
        assertEquals(0, hls.length);
        
        OAJfcController jc = new OAJfcController(hubEt, null, "name", lbl, HubChangeListener.Type.AoNotNull, false, true);

        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubEt);
        assertEquals(1, hls.length);

        
        Employee emp = new Employee();
        hub.add(emp);
        assertTrue(emp.isEnabled());
        assertTrue(hubEt.getAO()==null);

        EmployeeType et = hubEt.getAt(0);
        emp.setEmployeeType(et);
        assertTrue(emp.isEnabled());
        assertTrue(hubEt.getAO()==null);

        hub.setPos(0);
        assertTrue(emp.isEnabled());
        assertTrue(hubEt.getAO()==et);
        
        emp.setInactiveDate(new OADate());
        assertFalse(emp.isEnabled());
        assertTrue(lbl.isEnabled()); 

        et = hubEt.getAt(1);
        try {
            emp.setEmployeeType(et);
        }
        catch (Exception e) {
            // fireBeforeProp change exception
        }

        assertFalse(emp.isEnabled());
        emp.setInactiveDate(null);
        assertTrue(emp.isEnabled());
        emp.setEmployeeType(et);
        
        assertTrue(hubEt.getAO()==et);
        assertTrue(emp.isEnabled());
        
        jc.close();
        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubEt);
        assertEquals(0, hls.length);
    }

    @Test
    public void testC() {
        // linked hub that uses a propertyPath
        // make sure update() is not called too much
        
        JLabel lbl = new JLabel();        
        Hub hub = new Hub<Employee>(Employee.class);
        Hub hubDirect = null;
        String directPropertyName = null;
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);

        Hub<Location> hubLoc = new Hub<Location>(Location.class);
        for (int i=0; i<5; i++) hubLoc.add(new Location());

        hubLoc.setLinkHub(hub, Employee.P_Location);
        
        final VInteger vint = new VInteger();
        assertEquals(0, vint.value);
        
        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        
        //qqqqqq need another test that uses hubDirect, where propName is a pp
        OAJfcController jc = new OAJfcController(hubLoc, null, "program.company.name", lbl, HubChangeListener.Type.AoNotNull, false, true) {
            @Override
            public void update() {
                vint.inc();
                super.update();
            }
        };
        assertEquals(1, vint.value);

        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        
        Employee emp = new Employee();
        hub.add(emp);
        assertTrue(hubLoc.getAO()==null);
        assertEquals(1, vint.value);

        Location loc = hubLoc.getAt(0);
        Program prog = new Program();
        Company comp = new Company();
        loc.setProgram(prog);
        prog.setCompany(comp);
        assertEquals(1, vint.value);
        
        emp.setLocation(loc);
        hub.setPos(0);
        assertEquals(2, vint.value);
        
        assertEquals(loc, hubLoc.getAO());

        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubLoc);
        assertEquals(3, hls.length);
        
        comp.setName("xx");
        assertEquals(3, vint.value);
        
        prog.setCompany(new Company());
        assertEquals(4, vint.value);

        comp.setName("aaa"); // not current one
        assertEquals(4, vint.value);

        prog.getCompany().setName("aaa");
        assertEquals(5, vint.value);
        
        jc.close();
        hls = HubEventDelegate.getAllListeners(hub);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubLoc);
        assertEquals(0, hls.length);
    }

    @Test
    public void testD() {
        // test that uses hubDirect, where propName is a pp
        
        JLabel lbl = new JLabel();        
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());
        
        Hub<Location> hubLoc = new Hub<Location>(Location.class);
        for (int i=0; i<5; i++) hubLoc.add(new Location());
        // dont link it for this test
        //  hubLoc.setLinkHub(hub, Employee.P_Location);
        
        final VInteger vint = new VInteger();
        assertEquals(0, vint.value);
        
        OAJfcController jc = new OAJfcController(hubLoc, null, "program.company.name", lbl, HubChangeListener.Type.HubValid, false, true) {
            @Override
            public void update() {
                vint.inc();
                super.update();
            }
        };
        assertEquals(1, vint.value);

        hubLoc.setPos(0);
        assertEquals(2, vint.value);
        
        hubEmp.setPos(0);
        assertEquals(2, vint.value);
        
        hubEmp.getAO().setInactiveDate(new OADate());
        assertTrue(lbl.isEnabled());
        
        
        jc.close();
        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertEquals(0, hls.length);
        hls = HubEventDelegate.getAllListeners(hubLoc);
        assertEquals(0, hls.length);
    }

    @Test
    public void testE() {
        final JLabel lbl = new JLabel();        
        Hub<Employee> hubEmp = new Hub<Employee>(Employee.class);
        for (int i=0; i<5; i++) hubEmp.add(new Employee());

        HubListener[] hls = HubEventDelegate.getAllListeners(hubEmp);
        assertEquals(0, hls==null?0:hls.length);

        
        Hub<Location> hubLoc = new Hub<Location>(Location.class);
        for (int i=0; i<5; i++) hubLoc.add(new Location());
        hubLoc.setLinkHub(hubEmp, Employee.P_Location);

        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertEquals(1, hls.length);
        
        final VInteger vint = new VInteger();
        assertEquals(0, vint.value);
        
        OAJfcController jc = new OAJfcController(hubLoc, null, "name", lbl, HubChangeListener.Type.HubValid, true, true) {
            @Override
            public void update() {
                vint.inc();
                super.update();
            }
        };

        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertEquals(2, hls.length);

        hls = HubEventDelegate.getAllListeners(hubLoc);
        assertEquals(1, hls.length);
        
        boolean b = jc.updateEnabled();
        assertFalse(b);
        
        hubEmp.setPos(0);
        b = jc.updateEnabled();
        assertTrue(b);
        
        hubEmp.setPos(-1);
        b = jc.updateEnabled();
        assertFalse(b);
        
        hubEmp.setPos(0);
        b = jc.updateEnabled();
        assertTrue(b);
        
        hubEmp.getAt(0).setInactiveDate(new OADate());
        b = jc.updateEnabled();
        assertFalse(b);
        b = lbl.isEnabled();
        assertFalse(b);
        
        hubEmp.getAt(0).setInactiveDate(null);
        b = jc.updateEnabled(lbl, hubLoc.getAO());
        assertTrue(b);
        b = lbl.isEnabled();
        assertTrue(b);

        hubLoc.setPos(0);
        b = jc.updateEnabled(lbl, hubLoc.getAO());
        assertTrue(b);
        b = lbl.isEnabled();
        assertTrue(b);
        assertNotNull(hubLoc.getAO());
        

        hubEmp.getAt(0).setInactiveDate(null);
        b = jc.updateEnabled(lbl, hubLoc.getAO());
        assertTrue(b);
        b = lbl.isEnabled();
        assertTrue(b);
        assertNotNull(hubLoc.getAO());
        assertNotNull(hubEmp.getAO());
        
        jc.close();
        hls = HubEventDelegate.getAllListeners(hubEmp);
        assertEquals(1, hls.length);
        hls = HubEventDelegate.getAllListeners(hubLoc);
        assertEquals(0, hls.length);
    }
    
    @Test
    public void testF() {
        // test calc prop with dependent props
        JLabel lbl = new JLabel();        
        Hub<Site> hubSite = new Hub<Site>(Site.class);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubSite);
        assertTrue(hls != null && hls.length == 0);
        
        final VInteger vint = new VInteger();
        OAJfcController jc = new OAJfcController(hubSite, null, "DisplayName", lbl, HubChangeListener.Type.AoNotNull, false, true) {
            @Override
            public void update() {
                vint.inc();
                super.update();
            }
        };
        assertEquals(1, vint.value);

        hls = HubEventDelegate.getAllListeners(hubSite);
        assertEquals(3, hls.length);
        
        for (int i=0; i<5; i++) hubSite.add(new Site());
        assertEquals(1, vint.value);
        
        hls = HubEventDelegate.getAllListeners(hubSite);
        assertEquals(3, hls.length);
        
        hubSite.getAt(0).setName("aa");
        assertEquals(1, vint.value);
        
        hubSite.setPos(0);
        assertEquals(2, vint.value);

        hubSite.getAt(0).setName("aa");
        assertEquals(2, vint.value);
        
        hubSite.getAt(0).setName("bb");
        assertEquals(3, vint.value);

        hubSite.getAt(0).setAbbrevName("zz");
        assertEquals(4, vint.value);

        hubSite.getAt(1).setAbbrevName("xx");
        assertEquals(4, vint.value);
        
        // make sure listeners are removed         
        jc.close();
        hls = HubEventDelegate.getAllListeners(hubSite);
        assertTrue(hls == null || hls.length == 0);
    }
    
    @Test
    public void testG() {
        // test calc prop with dependent props
        JLabel lbl = new JLabel();        
        Hub<Site> hubSite = new Hub<Site>(Site.class);
        for (int i=0; i<5; i++) hubSite.add(new Site());

        Hub<Environment> hubEnv = new Hub(Environment.class);
        for (int i=0; i<5; i++) {
            Environment env = new Environment();
            env.setSite(hubSite.getAt(0));
            hubEnv.add(env);
        }
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hubSite);
        assertTrue(hls != null && hls.length == 0);
        
        final VInteger vint = new VInteger();
        OAJfcController jc = new OAJfcController(hubEnv, null, "site.DisplayName", lbl, HubChangeListener.Type.AoNotNull, false, true) {
            @Override
            public void update() {
                vint.inc();
                super.update();
            }
        };
        assertEquals(1, vint.value);

        hubSite.getAt(1).setAbbrevName("a");
        assertEquals(1, vint.value);
        
        hubEnv.setPos(0);
        assertEquals(2, vint.value);
        
        hubSite.getAt(1).setAbbrevName("b");
        assertEquals(2, vint.value);
        
        hubSite.getAt(0).setAbbrevName("x");
        assertEquals(3, vint.value);
        
        hubSite.getAt(0).setAbbrevName("z");
        assertEquals(4, vint.value);

        hubSite.getAt(0).setAbbrevName("z");
        assertEquals(4, vint.value);
        
        hubSite.getAt(1).setAbbrevName("A");
        assertEquals(4, vint.value);
        
        // make sure listeners are removed         
        jc.close();
        hls = HubEventDelegate.getAllListeners(hubSite);
        assertTrue(hls == null || hls.length == 0);
    }
    
}
