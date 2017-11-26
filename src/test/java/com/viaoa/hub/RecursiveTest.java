package com.viaoa.hub;


import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;

public class RecursiveTest extends OAUnitTest {

    
    /**
     * This will test Catalog.sections, which is recursive, and section.sections is recursive.
     */
    @Test
    public void recursiveHubDetailTest() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

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


        hubCatalog.setPos(-1);
        sec = hubCatalog.getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        hubSection.setAO(sec);
        assertEquals(hubCatalog.getAO(), hubCatalog.getAt(0));
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        
        hubCatalog.setPos(1);
        assertNull(hubSection.getAO());
        assertEquals(hubSection.getMasterObject(), hubCatalog.getAO());
        assertEquals(hubSection.getMasterHub(), hubCatalog);
        
        
        hubCatalog.setPos(-1);
        sec = hubCatalog.getAt(0).getSections().getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        hubSection.setAO(sec);
        assertEquals(hubCatalog.getAO(), hubCatalog.getAt(0));
        assertEquals(sec.getCatalog(), hubCatalog.getAO());
        assertNull(hubSection.getMasterHub());  // it should still be a detailHub
        
        hubCatalog.setPos(1);
        assertNull(hubSection.getAO());
        assertEquals(hubSection.getMasterObject(), hubCatalog.getAO());
        assertEquals(hubSection.getMasterHub(), hubCatalog);
        
        reset();
    }

    @Test
    public void recursiveHubDetailTest2() {
        HifiveDataGenerator data = new HifiveDataGenerator();
        Hub<Section> hubMaster = new Hub<Section>(Section.class);
        data.createSections(null, hubMaster, 0, 4);
        
        Hub<Section> hubDetail = hubMaster.getDetailHub(Section.P_Sections);
        assertEquals(hubDetail.getMasterHub(), hubMaster);  // it should still be a detailHub

        Section sec;
        sec = hubMaster.getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        
        hubDetail.setAO(sec);
        assertEquals(hubDetail.getAO(), sec);
        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertEquals(hubMaster.getAt(0), sec.getParentSection());
        assertEquals(hubDetail.getMasterObject(), sec.getParentSection());
        assertEquals(hubMaster.getAt(0), hubMaster.getActiveObject());
        
        hubMaster.setAO(null);
        assertNull(hubMaster.getAO());
        assertNotNull(hubDetail.getMasterHub());
        assertNull(hubDetail.getAO());
        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertNull(hubDetail.getMasterObject());

        // set hubSection to a lower (recursive) child hub
        sec = hubMaster.getAt(0).getSections().getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        
        hubDetail.setAO(sec);
        assertEquals(hubDetail.getAO(), sec);
        assertNull(hubDetail.getMasterHub());
        assertEquals(hubDetail.getMasterObject(), sec.getParentSection());
        assertEquals(hubMaster.getAt(0), hubMaster.getActiveObject());
        
        hubMaster.setAO(null);
        assertNotNull(hubDetail.getMasterHub());
        assertNull(hubDetail.getAO());
        assertNull(hubMaster.getAO());
        assertEquals(hubDetail.getMasterHub(), hubMaster);  // it should still be a detailHub
        
        
        // set hubSection to a lower (recursive) child hub
        sec = hubMaster.getAt(0).getSections().getAt(0).getSections().getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        
        hubDetail.setAO(sec);
        assertEquals(hubDetail.getAO(), sec);
        assertNull(hubDetail.getMasterHub());
        assertEquals(hubDetail.getMasterObject(), sec.getParentSection());
        assertEquals(hubMaster.getAt(0), hubMaster.getActiveObject());
        
        hubMaster.setAO(null);
        assertNotNull(hubDetail.getMasterHub());
        assertNull(hubDetail.getAO());
        assertNull(hubMaster.getAO());
        assertEquals(hubDetail.getMasterHub(), hubMaster);  // it should still be a detailHub

        // make sure changing ao on ref hub does not adjust it to point to another hub
        Hub<Section> hx = hubMaster.getAt(0).getSections().getAt(0).getSections();
        assertNull(hx.getAO());
        assertNull(hx.getMasterHub());
        assertNotNull(hx.getMasterObject());

        
        sec = hubMaster.getAt(0).getSections().getAt(0).getSections().getAt(0).getSections().getAt(0);
        assertNotNull(sec);
        hx.setAO(sec);  // this should not make hx adjust
        assertNull(hx.getAO());
        assertNull(hx.getMasterHub());
        assertNull(hx.getMasterHub());
        assertNotNull(hx.getMasterObject());
        
        // test with a shared hub off of the ref hub
        Hub<Section> h = new Hub<Section>(Section.class);
        h.setSharedHub(hx);
        h.setAO(sec);  // this should adjust to the hub that has sec
        assertEquals(sec, h.getAO());
        assertNull(hx.getMasterHub());
        assertNull(hx.getMasterHub());
        assertNull(h.getMasterHub());
        assertNotNull(h.getMasterObject());
        assertNotEquals(h.getMasterHub(), hx);
    }    
    
    @Test
    public void recursiveHubDetailTest3() {
        HifiveDataGenerator data = new HifiveDataGenerator();
        Hub<Section> hubMaster = new Hub<Section>(Section.class);
        data.createSections(null, hubMaster, 0, 5);
        
        Hub<Section> hubDetail = hubMaster.getDetailHub(Section.P_Sections);
        assertEquals(hubDetail.getMasterHub(), hubMaster);  // it should still be a detailHub

        // test with a second detail, to make it 3 deep
        Hub<Section> hubDetail2 = hubDetail.getDetailHub(Section.P_Sections);

        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertEquals(hubDetail2.getMasterHub(), hubDetail);
        
        hubMaster.setPos(0);
        assertNotNull(hubMaster.getAO());
        
        hubDetail.setPos(0);
        assertNotNull(hubMaster.getAO());
        assertNotNull(hubDetail.getAO());

        hubDetail2.setPos(0);
        assertNotNull(hubMaster.getAO());
        assertNotNull(hubDetail.getAO());
        assertNotNull(hubDetail2.getAO());
        
        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertEquals(hubDetail2.getMasterHub(), hubDetail);

        assertEquals(hubMaster.getAt(0), hubMaster.getAO());
        assertEquals(hubMaster.getAt(0).getSections().getAt(0), hubDetail.getAO());
        assertEquals(hubMaster.getAt(0).getSections().getAt(0).getSections().getAt(0), hubDetail2.getAO());
        
        
        
        Section sec = hubMaster.getAt(0).getSections().getAt(0).getSections().getAt(0);  // get a lower level sec, below detailHubs
        assertNotNull(sec);
        assertEquals(hubDetail2.getAO(), sec);
        
        sec = sec.getSections().getAt(0);
        assertNotNull(sec);
        
        hubDetail2.setAO(sec);  // this should disconnect it from hubDetail, so that it's a sharedHub with the sec.refHub sections
        assertEquals(sec, hubDetail2.getAO());
        
        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertNull(hubDetail2.getMasterHub());
        assertEquals(sec.getParentSection().getParentSection(), hubDetail.getAO());
        assertEquals(sec.getParentSection().getParentSection().getParentSection(), hubMaster.getAO());
        
        hubMaster.setPos(-1);  // this will reset hubDetail2 masterHub
        assertEquals(hubDetail.getMasterHub(), hubMaster);
        assertEquals(hubDetail2.getMasterHub(), hubDetail);
        
        assertNull(hubMaster.getAO());
        assertNull(hubDetail.getAO());
        assertNull(hubDetail2.getAO());
        
        
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
        data.createSampleData();

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
        assertNotNull(program);
        assertEquals(program.getName(), "program.0");
        
        assertEquals(hubLocation.getMasterObject(), program); 
        assertEquals(hubLocation.getMasterHub(), hubProgram);
        assertNull(hubEmployee.getAO());
        
        hubLocation.setPos(0);
        assertEquals(hubEmployee.getMasterObject(), hubLocation.getAO());
        assertNotNull(hubEmployee.getMasterHub());

        assertEquals(hubProgram.getAO(), program);
        
        hubProgram.setAO(null);
        assertNotNull(hubLocation.getMasterHub());
        assertNull(hubLocation.getMasterObject());
        assertNotNull(hubEmployee.getMasterHub());
        assertNull(hubEmployee.getMasterObject());
        
        loc = hubProgram.getAt(1).getLocations().getAt(0).getLocations().getAt(0);
        Employee emp = loc.getEmployees().getAt(0).getEmployees().getAt(0);
        hubEmployee.setAO(emp);
        assertEquals(emp, hubEmployee.getAO());
        program = emp.getLocation().getProgram();
        assertNotNull(program);
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgram.getAt(1), program);
        assertEquals(hubLocation.getAO(), emp.getLocation());

        Hub<Location> hx = hubProgram.getAt(2).getLocations();
        assertNull(hx.getMasterHub());
        assertEquals(hx.getMasterObject(), hubProgram.getAt(2));
        emp = hx.getAt(1).getEmployees().getAt(0).getEmployees().getAt(0);
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
        assertEquals(emp, hubEmployee.getAO());
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

        // hubLocation.AO to a loc in a child hub
        //   this should set hubLocation as a sharedHub, and not be connected to masterHub (hubProgram).
        loc = hubProgram.getAt(2).getLocations().getAt(1).getLocations().getAt(0);
        program = loc.getProgram();
        hubLocation.setAO(loc);
        assertNull(hubLocation.getMasterHub());  // disconnected from masterHub, since it is shared with a child hub (recursive)
        assertEquals(loc.getParentLocation(), hubLocation.getMasterObject());  // new masterHub
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        assertNull(ea.getEmployee());
        assertEquals(hubLocation.getMasterObject(), loc.getParentLocation());
        
        // at this point, hubLocation is acting as a shared hub
        
        // now change hubLocation.ao to a root level location
        loc = hubProgram.getAt(0).getLocations().getAt(0);
        program = loc.getProgram();
        assertEquals(hubProgram.getAt(0), program);
        
        // this will cause hubLocation to change it's shared hub, and also reconnect to masterHub
        hubLocation.setAO(loc);
//qqqqqqqqq add hubLocation.sharedHubs and test them         
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterHub(), hubProgram);
        
        assertEquals(program, hubLocation.getMasterObject());
        assertEquals(hubLocation.getAO(), loc);
        assertNull(hubEmployee.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), program);
        assertNull(ea.getEmployee());
        assertEquals(loc.getProgram(), program);
        
        loc = null;
        hubLocation.setAO(loc);
        assertNotNull(hubLocation.getMasterHub());
        assertEquals(hubLocation.getMasterObject(), program);
        assertNull(hubEmployee.getAO());
        assertNull(hubLocation.getAO());
        assertNotNull(hubProgram.getAO());
        assertEquals(hubProgram.getAO(), program);
        assertEquals(hubProgramLinked.getAO(), null); // linked to hubLocation
        assertNull(ea.getEmployee());

        
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
        assertTrue(ea.wasDeleted());
        assertNull(ea.getEmployee());
        assertNull(hubEmployeeAward.getAO());
        
        reset();
    }
    
    
}




