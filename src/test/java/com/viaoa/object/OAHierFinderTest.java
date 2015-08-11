package com.viaoa.object;

import static org.junit.Assert.*;

import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.theice.tsactest2.model.oa.propertypath.*;
import com.theice.tsactest2.model.oa.*;
import com.tmgsc.hifivetest.model.oa.*;
import com.tmgsc.hifivetest.model.oa.propertypath.EmployeeAwardPP;
import com.tmgsc.hifivetest.model.oa.propertypath.EmployeePP;
import com.tmgsc.hifivetest.model.oa.propertypath.LocationPP;

public class OAHierFinderTest extends OAUnitTest {
    
    @Test
    public void hierFinderTest() {
        reset();
        
        Site site = new Site();
        Environment env = new Environment();
        env.setSite(site);
        Silo silo = new Silo();
        silo.setEnvironment(env);
        Server server = new Server();
        server.setSilo(silo);
        Application app = new Application();
        app.setServer(server);
        ApplicationType at = new ApplicationType();
        app.setApplicationType(at);
        
        OAHierFinder<Application> f = new OAHierFinder<Application>(
            ApplicationPP.applicationType().name(), 
            ApplicationPP.server().name(),
            ServerPP.silo().networkMask(),
            SiloPP.environment().name(),
            EnvironmentPP.site().name()
        );
        
        Object obj = f.findFirstValue(app);
        assertNull(obj);
        
        site.setName("site");
        obj = f.findFirstValue(app);
        assertEquals(obj, "site");
        
        server.setName("server");
        obj = f.findFirstValue(app);
        assertEquals(obj, "server");
        
        at.setName("appType");
        obj = f.findFirstValue(app);
        assertEquals(obj, "appType");
    }
    
    @Test
    public void recursiveHierFinderTest() {
        reset();
        EmployeeAward ea = new EmployeeAward();
        Employee emp = new Employee();
        ea.setEmployee(emp);
        Location loc = new Location();
        Location loc2 = new Location();
        loc2.setParentLocation(loc);
        Location loc3 = new Location();
        loc3.setParentLocation(loc2);
        emp.setLocation(loc3);
        Program prog = new Program();
        loc.setProgram(prog);
        
        assertNotNull(loc2.getProgram());
        assertNotNull(loc3.getProgram());
        
        assertEquals(prog, loc.getProgram());
        assertEquals(prog, loc2.getProgram());
        assertEquals(prog, loc3.getProgram());
        
        OAHierFinder<EmployeeAward> f = new OAHierFinder<EmployeeAward>(
                EmployeeAwardPP.paidDate(),
                EmployeeAwardPP.employee().lastName(),
                EmployeePP.location().code(),
                LocationPP.program().code()
            );
            
        Object obj = f.findFirstValue(ea);
        assertNull(obj);
        
        prog.setCode("code");
        obj = f.findFirstValue(ea);
        assertEquals("code", obj);

        loc.setCode("loc");
        obj = f.findFirstValue(ea);
        assertEquals("loc", obj);
        
        
        emp.setLastName("ln");
        obj = f.findFirstValue(ea);
        assertEquals("ln", obj);
    }
    
    public static void main(String[] args) throws Exception {
        OAHierFinderTest test = new OAHierFinderTest();
        test.hierFinderTest();
    }
    
}
