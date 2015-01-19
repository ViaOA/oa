package com.viaoa.hub;


import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.HifiveDataGenerator;
import com.tmgsc.hifivetest.delegate.ModelDelegate;
import com.tmgsc.hifivetest.model.oa.*;
import com.tmgsc.hifivetest.model.oa.propertypath.CatalogPP;

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

        hubProgram.setPos(0);
        Program program = hubProgram.getAO();
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
        assertEquals(hubEmployee.getAO(), emp);
        program = emp.getLocation().getProgram();
        assertNotNull(program);
        assertEquals(hubProgram.getAO(), program);
        
        
        
        reset();
    }
}




