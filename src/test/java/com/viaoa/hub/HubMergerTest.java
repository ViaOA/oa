package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import static org.junit.Assert.*;

import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;

import test.hifive.HifiveDataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.Employee;
import test.hifive.model.oa.Location;
import test.hifive.model.oa.Program;
import test.hifive.model.oa.propertypath.ProgramPP;
import test.theice.tsac3.model.oa.*;

public class HubMergerTest extends OAUnitTest {

    @Test
    public void Test() {
        reset();
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

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
        
        assertEquals(x, data.cntEmployee);
        assertEquals(x, cntFinder);
    }
    private int cntFinder;
}
