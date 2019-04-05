package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.Company;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;
import test.hifive.model.oa.Program;
import test.hifive.model.oa.propertypath.ProgramPP;

public class HubMergerTest extends OAUnitTest {
    private int cntFinder;

    @Test
    public void Test() {
        reset();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        final Hub<Employee> hubEmployee = hubLocation.getDetailHub(Location.P_Employees);

        final Hub<Employee> hubEmployees = new Hub<Employee>(Employee.class);
        
        HubMerger hm = new HubMerger(hubProgram, hubEmployees, ProgramPP.locations().employees().pp, true);
        
        int x = hubEmployees.getSize();
        
        cntFinder = 0;
        OAFinder<Program, Employee> finder = new OAFinder<Program, Employee>(hubProgram, ProgramPP.locations().employees().pp, true) {
            @Override
            protected boolean isUsed(Employee obj) {
                cntFinder++;
                return false;
            }
        };
        finder.find();
        
        assertEquals(x, cntFinder);
    }
    
    
    @Test
    public void TestBackground() {
        reset();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        final Hub<Employee> hubEmployee = hubLocation.getDetailHub(Location.P_Employees);

        final Hub<Employee> hubEmployees = new Hub<Employee>(Employee.class);
        
        Hub<Program> hubP = new Hub<Program>(Program.class);
        
        
        HubMerger hm = new HubMerger(hubP, hubEmployees, ProgramPP.locations().employees().pp, true);
        hm.setUseBackgroundThread(true);
        
        hubP.setSharedHub(hubProgram);
        hubP.setSharedHub(null);
        hubP.setSharedHub(hubProgram);
        
        int xx = 4;
        xx++;
    }

    
    @Test
    public void testA() {
        Hub<Company> hubCompany = new Hub<Company>(Company.class);
        Company company = new Company();
        hubCompany.add(company);
        
        Hub<Program> hubx = new Hub<Program>();
        Program p = new Program();
        company.getPrograms().add(p);

        Location l = new Location();
        l.setCode("Aaaaa");
        p.getLocations().add(l);

        Location l2 = new Location();
        l2.setCode("Bbbbb");
        p.getLocations().add(l2);

        Location lx = new Location();
        lx.setCode("Cccccccc");
        l.getLocations().add(lx);

        lx = new Location();
        lx.setCode("Dddddd");
        l.getLocations().add(lx);
        
        Hub<Location> hubLoc = hubx.getDetailHub(Program.P_Locations);
        Hub<Location> hubFlattened = new Hub<Location>(Location.class);

        HubMerger hm = new HubMerger(hubCompany, hubFlattened, "Programs.Locations", false);
        assertEquals(0, hubFlattened.getSize());
        
        hubCompany.setPos(0);
        assertEquals(4, hubFlattened.getSize());

        hubCompany.setPos(-1);
        assertEquals(0, hubFlattened.getSize());
    }
    
    @Test
    public void testB() {
        Hub<Program> hubx = new Hub<Program>();
        Program p = new Program();
        hubx.add(p);

        Location l = new Location();
        l.setCode("Aaaaa");
        p.getLocations().add(l);

        Location l2 = new Location();
        l2.setCode("Bbbbb");
        p.getLocations().add(l2);

        l2 = new Location();
        l2.setCode("Bbbbb2");
        p.getLocations().add(l2);
        
        Location lx = new Location();
        lx.setCode("Cccccccc");
        l.getLocations().add(lx);

        lx = new Location();
        lx.setCode("Dddddd");
        l.getLocations().add(lx);
        
        Location lz = new Location();
        lz.setCode("Eeeeee");
        lx.getLocations().add(lz);
        
        Hub<Location> hubLoc = hubx.getDetailHub(Program.P_Locations);
        Hub<Location> hubFlattened = new Hub<Location>(Location.class);

        HubMerger hm = new HubMerger(hubLoc, hubFlattened, Location.P_Locations, true, true, true);
        hm.DEBUG = true;
        
        for (int i=0; i<5; i++) {
            hubx.setPos(0);
            assertEquals(6, hubFlattened.getSize());
            hubx.setPos(-1);
            assertEquals(0, hubFlattened.getSize());
        }        
    }
    
    public static void main(String[] args) throws Exception {
        HubMergerTest test = new HubMergerTest();
        test.testB();
    }
    
}
