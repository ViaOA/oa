package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;
import static org.junit.Assert.*;
import com.viaoa.object.OAFinder;
import com.viaoa.util.OAString;
import com.theicetest.tsactest.model.oa.*;
import com.tmgsc.hifivetest.delegate.ModelDelegate;
import com.tmgsc.hifivetest.model.oa.Employee;
import com.tmgsc.hifivetest.model.oa.Location;
import com.tmgsc.hifivetest.model.oa.Program;
import com.tmgsc.hifivetest.model.oa.propertypath.ProgramPP;

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
