package com.viaoa.hub;


import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.HifiveDataGenerator;
import com.tmgsc.hifivetest.delegate.ModelDelegate;
import com.tmgsc.hifivetest.model.oa.*;

public class RecursiveTest extends OAUnitTest {

    
    /**
     * This will test Catalog.sections, which is recursive, and section.sections is recursive.
     */
    @Test
    public void recursiveHubDetailTest() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

        final Hub<Catalog> hubCatalog = ModelDelegate.getCatalogs();
        
        // this detail hub is recursive 
        final Hub<Section> hubSection = hubCatalog.getDetailHub(Catalog.P_Sections);

        hubCatalog.setPos(0);
        Catalog catalog = hubCatalog.getAO();
        assertEquals(catalog.getName(), "catalog.0");
        
        assertEquals(hubSection.getMasterObject(), catalog); 
        assertEquals(hubSection.getMasterHub(), hubCatalog); 

        Section sec = hubCatalog.getAt(1).getSections().getAt(2);
        assertNotNull(sec);
        hubSection.setAO(sec);
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        
        sec = hubCatalog.getAt(0).getSections().getAt(0);

        assertNotNull(sec);
        hubSection.setAO(sec);
        assertEquals(hubCatalog.getAO(), hubCatalog.getAt(0));
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        
        
        hubCatalog.setPos(2);
        Catalog cat = hubCatalog.getAO();
        assertEquals(hubSection.getMasterObject(), cat);
        
        
        // get a child section and set AO
        sec = hubCatalog.getAt(0).getSections().getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        cat = sec.getCatalog();
        assertNotNull(cat);
        

        hubSection.setAO(sec);
        assertEquals(hubCatalog.getAO(), cat);
        assertEquals(hubCatalog.getAO(), hubCatalog.getAt(0));
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        assertEquals(hubSection.getMasterObject(), sec.getParentSection());
        assertNull(hubSection.getMasterHub());


        sec = hubCatalog.getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        hubSection.setAO(sec);
        assertEquals(hubCatalog.getAO(), hubCatalog.getAt(0));
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        
        hubCatalog.setPos(1);
        assertNull(hubSection.getAO());
        assertEquals(hubSection.getMasterObject(), hubCatalog.getAO());
        assertEquals(hubSection.getMasterHub(), hubCatalog);
        
        reset();
    }
    
    /**
     * This will test Program.locations.employees,
     * Program.locations is recursive
     * location.locations is recursive 
     * location.employees is not recursive, 
     * employee.employees is recursive.
     */
    @Test
    public void recursiveHubDetail2Test() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        final Hub<Employee> hubEmployee = hubLocation.getDetailHub(Location.P_Employees);

        Program program = new Program();
        int pos = hubProgram.getPos(program);
        assertEquals(pos, -1);
        Location loc = new Location();
        pos = hubLocation.getPos(loc);
        assertEquals(pos, -1);
        pos = hubLocation.getPos(null);
        assertEquals(pos, -1);
        
        hubProgram.setPos(0);
        program = hubProgram.getAO();
        assertEquals(program.getName(), "program.0");
        
        assertEquals(hubLocation.getMasterObject(), program); 
        assertEquals(hubLocation.getMasterHub(), hubProgram);
        assertNull(hubEmployee.getAO());
        
        hubLocation.setPos(0);
        assertEquals(hubEmployee.getMasterObject(), hubLocation.getAO());
        assertNotNull(hubEmployee.getMasterHub());

        assertEquals(hubProgram.getAO(), program);
        
        Employee emp = hubProgram.getAt(1).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0).getEmployees().getAt(0);
        hubEmployee.setAO(emp);
        assertEquals(emp, hubEmployee.getAO());
        program = emp.getLocation().getProgram();
        assertNotNull(program);
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubLocation.getAO(), emp.getLocation());

        emp = hubProgram.getAt(2).getLocations().getAt(1).getEmployees().getAt(0).getEmployees().getAt(0);
        hubEmployee.setAO(emp);
        assertEquals(hubEmployee.getAO(), emp);
        program = emp.getLocation().getProgram();
        assertNotNull(program);
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubLocation.getAO(), emp.getLocation());
        
        
        // link
        Hub<EmployeeAward> hubEmployeeAward = new Hub<EmployeeAward>(EmployeeAward.class);
        EmployeeAward ea = new EmployeeAward();
        hubEmployeeAward.add(ea);
        
        hubEmployee.setLinkHub(hubEmployeeAward, EmployeeAward.P_Employee);
        assertNull(hubEmployee.getAO());

        // create shared hub that is linked 
        Hub<Program> hubProgramLinked = ModelDelegate.getPrograms().createSharedHub();
        hubProgramLinked.setLinkHub(hubLocation, Location.P_Program);
        
        hubEmployeeAward.setAO(ea);
        emp = hubEmployee.getAO();
        assertNull(emp);

        emp = hubProgram.getAt(2).getLocations().getAt(1).getEmployees().getAt(0).getEmployees().getAt(0);
        ea.setEmployee(emp);
        assertNotNull(hubEmployee.getAO());
        assertNotNull(hubLocation.getAO());
        assertEquals(hubLocation.getAO(), emp.getLocation());
        assertNotNull(hubProgram.getAO());
        program = emp.getLocation().getProgram();
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        
        
        assertEquals(hubEmployee.getAO(), emp);
        assertEquals(hubLocation.getAO(), emp.getLocation());

        emp = hubProgram.getAt(1).getLocations().getAt(0).getEmployees().getAt(0);
        hubEmployee.setAO(emp);
        assertEquals(emp, ea.getEmployee());
        assertEquals(hubLocation.getAO(), emp.getLocation());
        assertEquals(hubProgram.getAO(), emp.getLocation().getProgram());
        assertEquals(hubEmployee.getMasterObject(), emp.getLocation());
        assertNotNull(hubEmployee.getMasterHub());
        assertEquals(hubEmployee.getAO(), emp);
        
        program = hubProgram.getAt(2);
        emp = program.getLocations().getAt(0).getEmployees().getAt(0);
        ea.setEmployee(emp);
        assertEquals(hubEmployee.getAO(), emp);
        assertEquals(hubLocation.getAO(), emp.getLocation());
        assertEquals(hubProgram.getAO(), hubProgram.getAt(2));
        assertEquals(hubProgramLinked.getAO(), program);
        
        hubLocation.setAO(null);
        assertEquals(hubProgram.getAO(), program);
        assertNull(hubLocation.getAO());
        assertNull(hubEmployee.getAO());
        assertNull(ea.getEmployee());
        
        ea.setEmployee(emp);
        assertEquals(hubEmployee.getAO(), emp);
        assertEquals(hubLocation.getAO(), emp.getLocation());
        assertEquals(hubProgram.getAO(), hubProgram.getAt(2));

        emp = hubEmployee.getAO();
        program = hubProgram.getAO();
        loc = emp.getLocation();
        emp.delete();
        assertNull(hubEmployee.getAO());
        assertNull(ea.getEmployee());
        assertNull(emp.getLocation());
        assertNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());  // since hubProgramLinked is also being used
        assertNull(hubEmployeeAward.getAO());
        assertEquals(hubEmployeeAward.getSize(), 0);
        
        assertEquals(hubEmployee.getMasterHub(), hubLocation);
        assertEquals(hubLocation.getMasterHub(), hubProgram);

        loc = hubProgram.getAt(2).getLocations().getAt(1).getLocations().getAt(0);
        program = loc.getProgram();
        hubLocation.setAO(loc);
        assertNull(hubLocation.getMasterHub());
        assertEquals(loc.getParentLocation(), hubLocation.getMasterObject());
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        assertNull(ea.getEmployee());

        ea = new EmployeeAward();
        hubEmployeeAward.add(ea);
        hubEmployeeAward.setAO(ea);
        assertNotNull(hubEmployeeAward.getAO());
        
        
        loc = hubProgram.getAt(0).getLocations().getAt(0);
        program = loc.getProgram();
        hubLocation.setAO(loc);
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterObject(), program);
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        assertNull(ea.getEmployee());
        assertEquals(loc.getProgram(), program);
        
        loc = null;
        hubLocation.setAO(loc);
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterObject(), program);
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), null); // linked to hubLocation
        assertNull(ea.getEmployee());
        assertNotNull(hubEmployeeAward.getAO());

        
        loc = hubProgram.getAt(0).getLocations().getAt(0);
        hubLocation.setAO(loc);
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterObject(), program);
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        assertNull(ea.getEmployee());
        assertEquals(loc.getProgram(), program);
        assertNotNull(hubEmployeeAward.getAO());
        
        hubProgramLinked.setPos(1);
        program = hubProgram.getAO();
        assertNotNull(program);
        assertEquals(hubLocation.getAO(), loc);
        assertEquals(hubProgramLinked.getAO(), program);
        assertEquals(loc.getProgram(), program);
        assertEquals(hubLocation.getAO(), loc);
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterObject(), program);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubLocation.getAO());
        assertNull(ea.getEmployee());
        assertEquals(loc.getProgram(), program);

        hubEmployee.setPos(0);
        assertEquals(ea.getEmployee(), hubEmployee.getAO());
        assertNotNull(hubEmployeeAward.getAO());
        
        
        reset();
    }
}




