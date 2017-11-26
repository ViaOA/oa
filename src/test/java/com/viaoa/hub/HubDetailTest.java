package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;
import test.hifive.HifiveDataGenerator;
import test.hifive.HifiveUnitTest;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;

public class HubDetailTest extends HifiveUnitTest {

    @Test
    public void detailHubTest() {
        reset();
        Hub<Program> hubProgram = new Hub<Program>(Program.class); 

        Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        Program program;
        for (int i=0; i<10; i++) {
            program = new Program();
            hubProgram.add(program);
        }
        for (Program p : hubProgram) {
            hubProgram.setAO(p);
            assertEquals(p.getLocations(), hubLocation.getSharedHub());
        }
        
        reset();
    }
    
    @Test
    public void detailHub2Test() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        hubLocation.setPos(0);

        assertNotNull(hubLocation.getAO());
        
        hubProgram.setPos(1);
        
        assertNull(hubLocation.getAO());
        
        reset();
    }
    
    @Test
    public void detailHub3Test() {
        reset();
        
        HifiveDataGenerator data = getDataGenerator();
        data.createSampleData();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.setPos(0);
        assertNotNull(hubLocation.getAO());
        
        
        HubListener hl = new HubListenerAdapter<Location>() {
            @Override
            public void onNewList(HubEvent<Location> e) {
                assertNull(e.getHub().getAO());
            }
        };
        hubLocation.addHubListener(hl);        
        
        hubProgram.setPos(1);
        assertNull(hubLocation.getAO());
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.removeHubListener(hl);        

        reset();
    }
    

    @Test
    public void detailHub4Test() {
        reset();
        
        HifiveDataGenerator data = getDataGenerator();
        data.createSampleData();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.setPos(0);
        assertNotNull(hubLocation.getAO());
        
        
        HubListener hl = new HubListenerAdapter<Location>() {
            @Override
            public void onNewList(HubEvent<Location> e) {
                assertNull(e.getHub().getAO());
            }
        };
        hubLocation.addHubListener(hl);        
        
        hubProgram.setPos(1);
        assertNull(hubLocation.getAO());
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.removeHubListener(hl);        

        reset();
    }
    
    @Test
    public void detailHub5Test() {
        reset();
        
        Hub<Company> hubCompany = new Hub<>();
        Company company = null;
        for (int i=0; i<5; i++) {
            company = new Company();
            if (i < 3) hubCompany.add(company);
            for (int ii=0; ii<3; ii++) {
                Program prog = new Program();
                company.getPrograms().add(prog);
                for (int iii=0; iii<3; iii++) {
                    Location loc = new Location();
                    prog.getLocations().add(loc);
                }
            }
            for (int ii=0; ii<3; ii++) {
                LocationType lt = new LocationType();
                company.getLocationTypes().add(lt);
            }
        }
        
        Hub<Program> hubProgram = hubCompany.getDetailHub(Company.P_Programs);
        Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        Hub<LocationType> hubLocationType = hubCompany.getDetailHub(Company.P_LocationTypes);
        
        hubCompany.setAO(0);
        assertTrue(hubLocationType.getMasterObject() == hubCompany.getAt(0));

        LocationType lt = company.getLocationTypes().getAt(0);
        hubLocationType.setAO(lt);
        
        assertNull(hubLocationType.getAO());
        assertNull(hubCompany.getAO());

        hubLocationType.setLinkHub(hubLocation, Location.P_LocationType);
        assertNull(hubLocationType.getAO());
        
        Program prog = hubCompany.getAt(0).getPrograms().getAt(0);
        hubProgram.setAO(prog);
        assertEquals(hubProgram.getAO(), prog);
        assertEquals(hubCompany.getAO(), hubCompany.getAt(0));
        assertEquals(hubCompany.getPos(), 0);
     
        hubLocation.setPos(0);
        assertEquals(hubLocationType.getMasterObject(), hubCompany.getAO());
        assertNull(hubLocation.getAt(0).getLocationType());
        hubLocationType.setPos(0);
        assertEquals(hubLocationType.getAO(), hubLocation.getAO().getLocationType());
        
        hubLocationType.setAO(company.getLocationTypes().getAt(0));
        assertNull(hubLocationType.getAO());
        
        hubLocationType.setAO(hubCompany.getAt(0).getLocationTypes().getAt(0));
        assertEquals(hubLocationType.getAO(), hubCompany.getAt(0).getLocationTypes().getAt(0));
        assertEquals(hubCompany.getAO(), hubCompany.getAt(0));
    }
    
    @Test
    public void detailHub6Test() {
        reset();
        
        Hub<Company> hubCompany = new Hub<>();
        Company company = null;
        for (int i=0; i<5; i++) {
            company = new Company();
            if (i < 3) hubCompany.add(company);
            for (int ii=0; ii<3; ii++) {
                Program prog = new Program();
                company.getPrograms().add(prog);
                for (int iii=0; iii<3; iii++) {
                    Location loc = new Location();
                    prog.getLocations().add(loc);
                }
            }
            for (int ii=0; ii<3; ii++) {
                LocationType lt = new LocationType();
                company.getLocationTypes().add(lt);
            }
        }
        
        Hub<Location> hubLocation = new Hub<>(Location.class);
        hubLocation.add(hubCompany.getAt(0).getPrograms().getAt(0).getLocations().getAt(0));
        
        Hub<Program> hubProgram = hubLocation.getDetailHub(Location.P_Program);
        hubCompany = hubProgram.getDetailHub(Program.P_Company);
        Hub<LocationType> hubLocationType = hubCompany.getDetailHub(Company.P_LocationTypes);

        assertEquals(hubLocationType.getMasterObject(), hubCompany.getAO());
        assertNull(hubLocationType.getAO());
        
        hubLocation.setAO(0);
        assertNull(hubLocation.getAO().getLocationType());
        assertNull(hubLocationType.getAO());
        
        hubLocationType.setPos(0);
        assertEquals(hubLocationType.getAO(), hubLocationType.getAt(0));
        assertNull(hubLocation.getAO().getLocationType());
        
        hubLocationType.setLinkHub(hubLocation, Location.P_LocationType);
        assertNull(hubLocationType.getAO());
        assertNull(hubLocation.getAO().getLocationType());

        hubLocationType.setPos(0);
        assertEquals(hubLocation.getAO().getLocationType(), hubLocationType.getAO());
        assertEquals(hubLocationType.getAO(), hubLocationType.getAt(0));
        
    }

    @Test
    public void detailHub7Test() {
        reset();
        
        Hub<Company> hubCompany = new Hub<>();
        Company company = null;
        for (int i=0; i<5; i++) {
            company = new Company();
            if (i < 3) hubCompany.add(company);
            for (int ii=0; ii<3; ii++) {
                Program prog = new Program();
                company.getPrograms().add(prog);
                for (int iii=0; iii<3; iii++) {
                    Location loc = new Location();
                    prog.getLocations().add(loc);
                }
            }
            for (int ii=0; ii<3; ii++) {
                LocationType lt = new LocationType();
                company.getLocationTypes().add(lt);
            }
        }
        Hub<Company> hubCompanyFirstThree = hubCompany;
        
        Hub<Location> hubLocation = new Hub<>(Location.class);
        Location loc = hubCompanyFirstThree.getAt(0).getPrograms().getAt(0).getLocations().getAt(0);
        Program prog = loc.getProgram();
        Company comp  = prog.getCompany();

        hubLocation.add(loc);
        hubLocation.setAO(loc);
        
        Hub<Program> hubProgram = hubLocation.getDetailHub(Location.P_Program);
        hubCompany = hubProgram.getDetailHub(Program.P_Company);
        Hub<LocationType> hubLocationType = hubCompany.getDetailHub(Company.P_LocationTypes);

        LocationType lt = company.getLocationTypes().getAt(0);
        loc.setLocationType(lt); // assign to incorrect locType

        assertEquals(loc.getProgram().getCompany(), hubCompanyFirstThree.getAt(0));

        hubLocationType.setLinkHub(hubLocation, Location.P_LocationType);

        assertEquals(loc.getProgram().getCompany(), hubCompanyFirstThree.getAt(0));
        assertNull(hubLocationType.getAO());
        assertEquals(loc.getLocationType(), lt);
        
        
        loc = hubCompanyFirstThree.getAt(0).getPrograms().getAt(0).getLocations().getAt(2);
        hubLocation.add(loc);
        assertNull(loc.getLocationType());
        loc.setLocationType(lt); // assign to incorrect locType
        hubLocation.setAO(loc);
        
        assertEquals(loc.getProgram().getCompany(), hubCompanyFirstThree.getAt(0));
        assertNull(hubLocationType.getAO());
        assertEquals(loc.getLocationType(), lt);
        
        LocationType lt2 = loc.getProgram().getCompany().getLocationTypes().getAt(0);
        loc.setLocationType(lt2);
        assertEquals(hubLocationType.getAO(), lt2);
        
        loc.setLocationType(lt);
        assertEquals(loc.getProgram().getCompany(), hubCompanyFirstThree.getAt(0));
        assertNull(hubLocationType.getAO());
        assertEquals(loc.getLocationType(), lt);
    }
    
    @Test
    public void detailHubATest() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();
        
        final Hub<Program> hubProgram = ModelDelegate.getPrograms();

        Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        Hub<Location> hubLocation2 = new Hub<>(Location.class);
        final Location loc = hubLocation.getAt(0).getLocations().getAt(0);
        hubLocation2.add(loc);
        
        Hub<Location> hubLoc = new Hub<>(Location.class);
        hubLoc.setSharedHub(hubLocation2, true);

        Hub<Employee> hubEmployee = hubLoc.getDetailHub(Location.P_Employees);
        assertEquals(hubEmployee.getMasterHub(), hubLoc);
        
        hubLocation2.setPos(0);
        assertEquals(hubLoc.getAO(), loc);
        assertEquals(hubEmployee.getMasterHub(), hubLoc);
        assertNull(hubEmployee.getAO());
        assertEquals(hubEmployee.getSize(), hubLocation2.getAO().getEmployees().getSize());
        
        hubProgram.setPos(0);
        assertNull(hubLocation.getAO());

        assertEquals(hubLocation2.getAO(), loc);
        assertEquals(hubLoc.getAO(), loc);
        
        assertNull(hubEmployee.getAO());
        assertEquals(hubEmployee.getMasterHub(), hubLoc);

        assertEquals(hubLocation2.getSize(), 1);
        assertEquals(hubLocation2.getAt(0), loc);
        assertEquals(hubLocation2.getAO(), loc);
        assertEquals(hubLoc.getSize(), 1);
        assertEquals(hubLoc.getAt(0), loc);
        assertEquals(hubLoc.getAO(), loc);
        
        final Employee emp = hubProgram.getAt(0).getLocations().getAt(0).getLocations().getAt(0).getEmployees().getAt(0);
        assertFalse(hubEmployee.contains(emp));

        // should not change 
        hubEmployee.setAO(emp);  // should not change the hubLoc
        
        assertFalse(hubEmployee.getAO() == emp);
        assertFalse(hubEmployee.contains(emp));

        assertEquals(hubLoc.getSize(), 1);
        assertEquals(hubLoc.getAt(0), loc);
        

        Hub<Employee> hubEmp = hubLocation.getDetailHub(Location.P_Employees);
        assertNull(hubEmp.getAO());
        hubLocation.setPos(1);
        
        hubEmp.setAO(emp);
        assertEquals(hubEmp.getAO(), emp);
        assertEquals(hubLocation.getPos(), 0);
        
        reset();
    }
    
    
}





